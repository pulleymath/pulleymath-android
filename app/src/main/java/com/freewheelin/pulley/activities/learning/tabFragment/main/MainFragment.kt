package com.freewheelin.pulley.activities.learning.tabFragment.main


import android.Manifest
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.freewheelin.pulley.BuildConfig
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.activities.learning.LearningTabFragment
import com.freewheelin.pulley.activities.learning.LearningTabInterface
import com.freewheelin.pulley.activities.learning.tabFragment.main.component.*
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.core.manage.UserManager
import com.freewheelin.pulley.utils.*
import com.freewheelin.pulley.views.DaebakToast
import com.freewheelin.pulley.views.textViews.HashTagTextView
import kotlinx.android.synthetic.main.fragment_main_2.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.concurrent.thread


class MainFragment : LearningTabFragment(), ShareProfileDialogListener, ProblemCountSettingDialogListener, DDaySettingDialogListener, LifecycleObserver {

    override var screenName: String = "메인"
    var mainProfile: MainProfile? = null

    var profileReceiver: BroadcastReceiver? = null
    lateinit var learningTabInterface: LearningTabInterface

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if(context is LearningTabInterface) learningTabInterface = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        profileReceiver = object: BroadcastReceiver() {
            override fun onReceive(p0: Context?, p1: Intent?) {
                syncProfile()
            }
        }
    }

    override fun onDestroy() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)
        super.onDestroy()

        if(profileReceiver != null)
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(profileReceiver!!)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main_2, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        LocalBroadcastManager.getInstance(requireContext()).registerReceiver(profileReceiver!!, IntentFilter(UserManager.EVENT_USER_MODIFYING))

        initUI()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun initUI() {
        try {
            setupUI()
        }catch (e:Exception){
            Log.e("화면크래쉬", "error==>${e.localizedMessage}")
        }
    }

    override fun onFragmentSelected() {
        super.onFragmentSelected()
        syncProfile()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForeground() {
        syncProfile()
    }

    fun setupUI() {
        profileIv.setOnClickListener {
            setProgress()
        }

        shareBtn.setOnClickListener {
            onShareBtnClicked()
        }

        nameTv.setOnClickListener { onNameBtnClicked() }
//        nameArrowIv.setOnClickListener { onNameBtnClicked() }
        targetSettingBtn.setOnClickListener { onTargetSettingClicked() }
        dDayTv.setOnClickListener { onDDayBtnClicked() }
        dDayErrowIv.setOnClickListener { onDDayBtnClicked() }
        startBtn.setOnClickListener { onStartBtnClicked() }
        bgIv.layoutParams.width = (DisplayUtils.getScreenWidth(requireContext()) * 0.5).toInt()
        reportTv.setOnClickListener { onReportBtnClicked() }
//        profileIv.setOnClickListener { onProfileBtnClicked() }

//        syncProfile()

        Log.d("마케팅", "setupUI() is called!!!")
    }

    private fun setReportType() {
        val dessertType = user?.studentType
        if(dessertType != null) {
            reportTv.text = "${dessertType.dessertName} 타입에 관한 심층 보고서"
        }
    }

    var loadFirst = true

    fun syncProfile() {

        Log.d("마케팅", "syncProfile() is called!!!")

        if(loadFirst) {
            loadingContainer.visibility = View.VISIBLE
            loadFirst = false
        }

        UserManager.getProfile(requireContext(), user!!) { mainProfile ->
            try {
                this.mainProfile = mainProfile
                nameTv.text = mainProfile.studentName
                guideTv.text = mainProfile.curation
                countTv.text = "${mainProfile.dailySolvedProblemCount}"
                freeGuideTv.text = mainProfile.getFreeGuideText()

                if (mainProfile.profileImageUrl.isNotEmpty())
                    profileIv.setImageURL(mainProfile.profileImageUrl)

                setReportType()

                bgIv.setImageURLBackground(mainProfile.backgroundImageUrl)
                problemCntTv.text = "${mainProfile.totalSolvedProblemCount}(${mainProfile.totalSolvedWeakProblemCount})"
                setProgress()
                tagFl.removeAllViewsInLayout()
                continuousDayTv.text = mainProfile.getContinuousText()
                dailyProblemGuideTv.text = mainProfile.getProblemCountGuideText(user!!)

                val existTarget = getTargetTitleAndDate()

                Log.d("테스트", "Dday=$existTarget")
                Log.d("테스트", "getDDay=${mainProfile.getDDay(existTarget?.third)}")

                if(mainProfile.getDDay(existTarget?.third) < 0 ) {
                    targetTv.text = mainProfile.getDDayTitle(null)
                    dDayTv.text = mainProfile.getDDayText(null)
                } else {
                    targetTv.text = mainProfile.getDDayTitle(existTarget?.second)
                    dDayTv.text = mainProfile.getDDayText(existTarget?.third)
                }

                mainProfile.getUserHashtag(user).forEach {
                    tagFl.addView(HashTagTextView(requireContext(), "#${it}"))
                }

                if (mainProfile.getUserHashtag(user).isEmpty()) {
                    tagFl.visibility = View.GONE
                    freeUserContainer.setPaddingTop(resources.getDimension(R.dimen.dp48).toInt())
                } else {
                    tagFl.visibility = View.VISIBLE
                    freeUserContainer.setPaddingTop(resources.getDimension(R.dimen.dp32).toInt())
                }

                if (user?.hasPulleyPlus == true)
                    setAvailableUI()
                else
                    setDisabailableUI()

                // 여기서 마케팅 팝업처리
                if(loadingContainer.visibility == View.VISIBLE) {
                    learningTabInterface.openMarketingDialog(mainProfile)
                }

            } catch(e:Exception) {
                Log.e("화면크래쉬", "error==>${e.localizedMessage}")
            }

            thread(start=true) {
                Thread.sleep(500)
                activity?.runOnUiThread {
                    try {
                        loadingContainer?.visibility = View.GONE
                    }catch(e:Exception){}
                }
            }
        }
    }

    private fun setAvailableUI() {
        phraseTv.visibility = View.INVISIBLE
        freeUserContainer.visibility = View.GONE
        payUserContainer.visibility = View.VISIBLE

        phraseTv.visibility = View.INVISIBLE
        continuousDayTv.visibility = View.VISIBLE
        dailyProblemGuideTv.visibility = View.VISIBLE
        targetSettingBtn.visibility = View.VISIBLE
        targetSettingBtnBorder.visibility = View.VISIBLE
        progressBar.visibility = View.VISIBLE
        labelContainer.visibility = View.VISIBLE
    }

    private fun setDisabailableUI() {
        phraseTv.visibility = View.VISIBLE
        freeUserContainer.visibility = View.VISIBLE
        payUserContainer.visibility = View.INVISIBLE

        phraseTv.visibility = View.VISIBLE
        continuousDayTv.visibility = View.INVISIBLE
        dailyProblemGuideTv.visibility = View.INVISIBLE
        targetSettingBtn.visibility = View.INVISIBLE
        targetSettingBtnBorder.visibility = View.INVISIBLE
        progressBar.visibility = View.INVISIBLE
        labelContainer.visibility = View.INVISIBLE
    }

    private fun setProgress() {
        val value = mainProfile!!.progressValue
        progressBar.value = value
        val lp = (progressIndicator.layoutParams as ConstraintLayout.LayoutParams)
        lp.horizontalBias = value
        progressIndicator.layoutParams = lp
        labelContainer.requestLayout()


        if(value >= 0)
            labelContainer.visibility = View.VISIBLE
        else
            labelContainer.visibility = View.INVISIBLE
    }

    private fun onNameBtnClicked() {
//        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "이름 옆의 꺽쇠")
//        val intent = MyPageActivity.getIntent(requireContext())
//        startActivity(intent)
    }

    private fun onTargetSettingClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "목표수정")
        if (mainProfile != null) {
            val dialog = ProblemCountSettingDialog(requireContext(), mainProfile!!.goalProblemCount)
            dialog.listener = this
            dialog.show()
        }
    }

    private fun onDDayBtnClicked() {
        val spyCount = (activity as LearningTabActivity).spyCount
        val setOnSpyMode = { (activity as LearningTabActivity).setOnSpyMode() }
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "디데이꺽쇠")
        val dialog = DDaySettingDialog(requireContext(), setOnSpyMode)
        dialog.listener = this
        dialog.show()
    }

    private fun onStartBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "구독하기버튼")
        FacebookEvent.log(requireContext(), FacebookEvent.SUBSCRIBE_STARTED)

        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(URL.구매촉구_메인)
        startActivity(intent)
    }

    private fun onReportBtnClicked() {
        startActivityForResult(SnackReportActivity.getIntent(requireContext(), user!!.studentType!!), SnackReportActivity.REQUEST_SNACK_ACTIVITY)
    }

    private fun onShareBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "공유하기")
        if(mainProfile != null) {
            val dialog = ShareProfileDialog(requireContext(), mainProfile!!)
            dialog.listener = this
            dialog.show()
        }
    }

    private fun onProfileBtnClicked() {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "보고서아이콘")
        startActivityForResult(SnackReportActivity.getIntent(requireContext(), user!!.studentType!!), SnackReportActivity.REQUEST_SNACK_ACTIVITY)
    }

    override fun onDownloadClicked(dialog: ShareProfileDialog, bitmap: Bitmap) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "다운로드버튼")
        val permission = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(permission == PackageManager.PERMISSION_GRANTED) {
            saveImage(bitmap)
            DaebakToast.show(requireContext(), "저장되었습니다.", bottomOffset = 64.toPx(), overDialog = true)
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 100)
        }
    }

    override fun onShareBtnClicked(dialog: ShareProfileDialog, bitmap: Bitmap) {
        LogUtils.logEvent(requireContext(), user, PulleyEvent.BUTTON_CLICK, "메인", "공유하기버튼")
        val uri = saveImageAsCache(bitmap)
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, uri)
            type = "image/*"
        }

        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }

    fun saveImage(bitmap: Bitmap) {
        val dateStr = DateTimeUtils.yyyy_MM_dd.format(Date())
        val title = "${dateStr} 데일리 프로필"
        MediaStore.Images.Media.insertImage(requireContext().contentResolver, bitmap, title, "")
    }

    private fun saveImageAsCache(image: Bitmap): Uri? {
        val imagesFolder = File(requireContext().cacheDir, "images")
        var uri: Uri? = null
        try {
            imagesFolder.mkdirs()
            val file = File(imagesFolder, "shared_image.png")
            val stream = FileOutputStream( file)
            image.compress(Bitmap.CompressFormat.PNG, 90, stream)
            stream.flush()
            stream.close()

            val provider = if(BuildConfig.FLAVOR == "beta") "com.freewheelin.beta.fileprovider" else "com.freewheelin.fileprovider"
            uri = FileProvider.getUriForFile(requireContext(), provider, file)
        } catch (e: IOException) {
            LogUtils.assert(false, "saveImageAsCache 실패")
        }

        return uri
    }

    fun getTargetTitleAndDate(): Triple<Int, String, Date>? {
        val targetTitle = Preferences.targetDateTitle.get()
        val targetDate = Preferences.targetDate.get()
        val targetID = Preferences.targetID.get()
        if(targetTitle.isEmpty() || targetDate == 0L) {
            return null
        } else {
            return Triple(targetID, targetTitle, Date(targetDate))
        }
    }

    override fun onOnDDaySettingCompleted() {


        val existTarget = getTargetTitleAndDate()
        val profile = mainProfile ?: return

        targetTv.text = profile.getDDayTitle(existTarget?.second)
        dDayTv.text = profile.getDDayText(existTarget?.third)
    }

    override fun onModifyCompleted(cnt: Int) {
        val profile = mainProfile ?: return
        dailyProblemGuideTv.text = profile.getProblemCountGuideText(user!!)
        setProgress()
    }
}
