package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.constraintlayout.widget.ConstraintSet
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.designfactory.*
import com.freewheelin.pulley.assets.DessertType
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API_V2
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.LogUtils
import com.freewheelin.pulley.utils.PulleyEvent
import com.freewheelin.pulley.utils.show
import com.freewheelin.pulley.views.DaebakToast
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_snack_report.*
import kotlinx.android.synthetic.main.activity_snack_report.backBtn
import kotlinx.android.synthetic.main.activity_snack_report.mailBtn
import kotlinx.android.synthetic.main.activity_snack_report.mailGuideText
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SnackReportActivity : AppCompatActivity(), ReportDesignListener {

    companion object {
        const val REQUEST_SNACK_ACTIVITY = 19000

        const val RESULT_SNACK_TEST = 19001
        const val RESULT_SNACK_UNIT = 19002
        const val RESULT_SNACK_MOCK = 19003
        const val RESULT_SNACK_WRONG = 19004
        const val RESULT_SNACK_ANALYSIS = 19005

        fun getIntent(context: Context, type: DessertType): Intent {
            val intent = Intent(context, SnackReportActivity::class.java)
            intent.putExtra(UserManager.ARG_DESSERT_TYPE, type)
            return intent
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snack_report)
        val type = intent.getSerializableExtra(UserManager.ARG_DESSERT_TYPE) as DessertType
        val designFactory = ReportDesignFactory.createReportDesignFactory(type, this)
        setUpUI(designFactory)

        backBtn.setOnClickListener {
            onBackPressed()
        }

        mailGuideText.setOnClickListener {
            onMailBtnClicked()
        }
        mailBtn.setOnClickListener { onMailBtnClicked() }
    }


    fun setUpUI(factory: ReportDesignFactory) {
        Picasso.get().load(factory.getIllustResource()).resize(DisplayUtils.getScreenWidth(this), 0).into(illustIv, object: com.squareup.picasso.Callback {
            override fun onSuccess() {
                scrollView.show()
                mailContainerCl.show()
            }

            override fun onError(e: Exception?) {}
        })

        topGuideIv.setImageResource(factory.getReportTopImageResource())

        bottomGuideIv.setImageResource(factory.getReportBottomImageResource())

        val topLayout = factory.createReportTopUIComponent(this)
        val bottomLayout = factory.createReportBottomUIComponent(this)

        topLayout.id = View.generateViewId()
        bottomLayout.id = View.generateViewId()
        rootView.addView(topLayout)
        rootView.addView(bottomLayout)

        val topMargin = resources.getDimension(R.dimen.dp120)
        val bottomMargin = resources.getDimension(R.dimen.dp200)
        val startMargin = resources.getDimension(R.dimen.dp56)

        val set = ConstraintSet()
        set.clone(rootView)

        set.connect(topLayout.id, ConstraintSet.TOP, illustIv.id, ConstraintSet.BOTTOM, topMargin.toInt())
        set.connect(topLayout.id, ConstraintSet.START, topGuideIv.id, ConstraintSet.END, startMargin.toInt())
        set.connect(topGuideIv.id, ConstraintSet.TOP, topLayout.id, ConstraintSet.TOP)
        set.connect(topGuideIv.id, ConstraintSet.BOTTOM, topLayout.id, ConstraintSet.BOTTOM)

        set.connect(bottomLayout.id, ConstraintSet.TOP, topLayout.id, ConstraintSet.BOTTOM, topMargin.toInt())
        set.connect(bottomLayout.id, ConstraintSet.BOTTOM, rootView.id, ConstraintSet.BOTTOM, bottomMargin.toInt())
        set.connect(bottomLayout.id, ConstraintSet.START, topLayout.id, ConstraintSet.START)
        set.connect(bottomGuideIv.id, ConstraintSet.TOP, bottomLayout.id, ConstraintSet.TOP)
        set.connect(bottomGuideIv.id, ConstraintSet.BOTTOM, bottomLayout.id, ConstraintSet.BOTTOM)

        set.applyTo(rootView)
    }

    fun onMailBtnClicked() {
        LogUtils.logEvent(this, user, PulleyEvent.BUTTON_CLICK, "스낵보고서", "메일보내기")
        API_V2.postReportMail(user!!.studentID).enqueue(object: Callback<Void> {
            override fun onFailure(call: Call<Void>, t: Throwable) {
                DaebakToast.show(this@SnackReportActivity, "메일 보내기에 실패했습니다. 문제가 지속되면 고객센터에 문의해주세요.")
            }

            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.code() == 200) {
                    DaebakToast.show(this@SnackReportActivity, "메일이 발송되었습니다. 네트워크 환경에 따라 시간이 다소 소요될 수 있습니다.")
                } else {
                    DaebakToast.show(this@SnackReportActivity, "메일 보내기에 실패했습니다. 문제가 지속되면 고객센터에 문의해주세요.")
                }
            }
        })
    }

    override fun onTestBtnClicked() {
        setResult(RESULT_SNACK_TEST, intent)
        finish()
    }

    override fun onAnalysisBtnClicked() {
        setResult(RESULT_SNACK_ANALYSIS, intent)
        finish()
    }

    override fun onWrongNoteBtnClicked() {
        setResult(RESULT_SNACK_WRONG, intent)
        finish()
    }

    override fun onUnitStudyBtnClicked() {
        setResult(RESULT_SNACK_UNIT, intent)
        finish()
    }

    override fun onMockExamBtnClicked() {
        setResult(RESULT_SNACK_MOCK, intent)
        finish()
    }
}
