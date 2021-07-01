package com.freewheelin.pulley.revision2021.activity.binding

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

@BindingAdapter("bind_img_url")
fun setImageUrl(v: ImageView, url: String) {
    Glide.with(v.context)
            .load(url)
            .apply(RequestOptions().centerCrop())
            .into(v)
}