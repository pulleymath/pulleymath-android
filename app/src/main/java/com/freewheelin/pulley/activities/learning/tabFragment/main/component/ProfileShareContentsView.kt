package com.freewheelin.pulley.activities.learning.tabFragment.main.component

import android.content.Context
import android.util.AttributeSet
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.user
import com.freewheelin.pulley.core.API.ResponseModel.MainProfile
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setImageURL
import com.freewheelin.pulley.utils.toPx
import com.freewheelin.pulley.views.TextViews.HashTagTextView
import com.google.android.flexbox.FlexboxLayout
import java.util.*

class ProfileShareContentsView : ConstraintLayout {
    constructor(context: Context): super(context)
    constructor(context: Context, attributeSet: AttributeSet): super(context, attributeSet)

    var dateTv: TextView
    var nameTv: TextView
    var typeLabel: TextView
    var profileIv: ImageView
    var problemCntTv: TextView
    var tagFl: FlexboxLayout


    init {
        LayoutInflater.from(context).inflate(R.layout.view_profile_share_contents, this)

        dateTv = findViewById(R.id.dateTv)
        nameTv = findViewById(R.id.nameTv)
        typeLabel = findViewById(R.id.typeLabel)
        profileIv = findViewById(R.id.profileIv)
        problemCntTv = findViewById(R.id.problemCntTv)
        tagFl = findViewById(R.id.tagFl)

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