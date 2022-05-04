package com.pulleymath.android.pdf.utils

import android.graphics.*
import android.util.Base64
import android.view.View
import java.io.ByteArrayOutputStream

object ViewUtils {
}

fun View.getImageToBase64(): String {
    val bm = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    canvas.drawColor(Color.TRANSPARENT);
    this.draw(canvas)

    val outputStream = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return Base64.encodeToString(outputStream.toByteArray(), Base64.DEFAULT)
}

fun View.getImageToByteArray(): ByteArray {
    val bm = Bitmap.createBitmap(this.width, this.height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bm)
    canvas.drawColor(Color.TRANSPARENT);
    this.draw(canvas)

    val outputStream = ByteArrayOutputStream()
    bm.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

fun View.onDebounceClick(action: (v: View) -> Unit) {
    val listener = View.OnClickListener { action(it) }
    setOnClickListener(OnDebounceClickListener(listener))
}

fun ByteArray.toBitmap(): Bitmap {
    return BitmapFactory.decodeByteArray(this, 0, size)
}
//fun View.getImage(): String {
//
//    val file = File("/storage/emulated/0/Android/data/com.freewheelin.pulley.beta/files/DCIM/drawPic1.png")
//    val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)
//    val body = MultipartBody.Part.createFormData("image", file.name, requestFile)
//    val id = RequestBody.create(MediaType.parse("multipart/form-data"), "Your Name")
//
//
//}