package com.freewheelin.pulley.revision2021.utils

import android.widget.ImageView
import androidx.databinding.BindingAdapter
import com.freewheelin.pulley.utils.setCookingImageURL
import com.freewheelin.pulley.utils.setImageUrlGlide
import com.freewheelin.pulley.utils.setImageUrlPicasso
import com.freewheelin.pulley.utils.setImageUrlPicassoDownScale

class AdaterUtils {
}

@BindingAdapter("cookingImgRes")
fun loadImage(view: ImageView, imageUrl: String?) {
    imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setImageUrlGlide(it)
    }
}
@BindingAdapter("cookingImgResOnPicasso")
fun loadImagePicasso(view: ImageView, imageUrl: String?) {
    imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setImageUrlPicasso(it)
    }
}
@BindingAdapter("cookingImgResOnPicassoDownScale")
fun loadImagePicassoDownScale(view: ImageView, imageUrl: String?) {
    imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setImageUrlPicassoDownScale(it)
    }
}

@BindingAdapter("imgResAtQuiz")
fun loadImage2(view: ImageView, imageUrl: String?) {
    imageUrl?.split("https://")?.let { println("imgRes, url : ${it}") }
    if (imageUrl?.isEmpty() == true) return
    imageUrl?.let {
        view.setCookingImageURL(it)
    }
}

