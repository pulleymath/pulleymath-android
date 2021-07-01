package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.databinding.DataBindingUtil.setContentView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setImageURL
import com.freewheelin.pulley.utils.spToPx
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.TextViews.HashTagTextView
import kotlinx.android.synthetic.main.view_profile_share_contents.view.*
import java.util.*

class ProfileShareContentsView : ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile_share_contents, this)
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(Date())
    }

    fun setProfileUI(mainProfile: MainProfile) {
        nameTv.text = mainProfile.studentName
        typeLabel.text = "${user!!.studentType!!.dessertName} 타입"
        profileIv.setImageURL(mainProfile.profileImageUrl)
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(Date())
        problemCntTv.text = "${mainProfile.totalSolvedProblemCount}(${mainProfile.totalSolvedWeakProblemCount})"
        tagFl.removeAllViewsInLayout()
        mainProfile.hashTag.forEach {
            if(it.isNotEmpty()) {
                val tagView = HashTagTextView(context!!, "#${it}")
                tagView.setPadding(12.toPx(),6.toPx(),12.toPx(),6.toPx())
                tagView.setTextSize(TypedValue.COMPLEX_UNIT_PX, resources.getDimension(R.dimen.sp12))
                tagFl.addView(tagView)
            }
        }
    }
}