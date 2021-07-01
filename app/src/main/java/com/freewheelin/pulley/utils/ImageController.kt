package com.freewheelin.pulley.utils

//import android.graphics.drawable.Animatable
//import com.facebook.drawee.controller.BaseControllerListener
//import com.facebook.drawee.view.SimpleDraweeView
//import com.facebook.imagepipeline.image.ImageInfo
//
//const val NG_MAX_WIDTH_PX = 900f
//
//class ImageController(private val drawee: SimpleDraweeView, private val scale: Float = 1f) : BaseControllerListener<ImageInfo>() {
//
//
//    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
//
//        if(imageInfo == null)
//            return
//
//        drawee.layoutParams.height = (imageInfo.height.toFloat() * scale).toInt()
//        drawee.requestLayout()
//
//    }
//}
//
//class ProblemImageController(private val drawee: SimpleDraweeView): BaseControllerListener<ImageInfo>() {
//    override fun onFinalImageSet(id: String?, imageInfo: ImageInfo?, animatable: Animatable?) {
//        if(imageInfo == null)
//            return
//
//        val maxWidth = drawee.maxWidth - drawee.paddingLeft - drawee.paddingRight
//
//        if(imageInfo.width < maxWidth) {
//            drawee.layoutParams.width = imageInfo.width + drawee.paddingLeft + drawee.paddingRight
//            drawee.layoutParams.height = imageInfo.height + drawee.paddingTop + drawee.paddingBottom
//        } else {
//            val scale = maxWidth.toFloat() / imageInfo.width
//            drawee.layoutParams.width = (imageInfo.width * scale).toInt() + drawee.paddingLeft + drawee.paddingRight
//            drawee.layoutParams.height = (imageInfo.height * scale).toInt() + drawee.paddingTop + drawee.paddingBottom
//        }
//        drawee.requestLayout()
//    }
//}


