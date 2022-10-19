package com.freewheelin.pulley.revision2021.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.freewheelin.pulley.utils.setCookingImageURL
import com.freewheelin.pulley.utils.setImageUrlGlide
import com.freewheelin.pulley.utils.setImageUrlPicasso

class AdaterUtils {
}

@BindingAdapter("cookingImgRes")
fun loadImage(view: ImageView, imageUrl: String?) {
    println("imgRes, url :${imageUrl}")
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setImageUrlGlide(it)
    }
}
@BindingAdapter("cookingImgResOnPicasso")
fun loadImagePicasso(view: ImageView, imageUrl: String?) {
    println("imgRes, url :${imageUrl}")
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setImageUrlPicasso(it)
    }
}

@BindingAdapter("imgResAtQuiz")
fun loadImage2(view: ImageView, imageUrl: String?) {

    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setCookingImageURL(it)
    }
}

