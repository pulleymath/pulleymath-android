package com.freewheelin.pulley.dialogs

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.widget.ImageView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.bases.is10InchUI
import com.freewheelin.pulley.core.manage.VersionManager
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class UpdateDialog(context: Context) : Dialog(context) {

    var imageView: ImageView
    init {
        setContentView(R.layout.dialog_update)
        imageView = findViewById(R.id.imageView)

        setCancelable(false)
        window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        Picasso.get().load(VersionManager.info?.updateImageUrl)
                .into(imageView, object: Callback {
                    override fun onSuccess() {
                        val bitmap = (imageView.drawable as BitmapDrawable).bitmap
                        if(bitmap == null)
                            return


                        val width = if(context.is10InchUI) bitmap.width else (bitmap.width * 0.75).toInt()
                        val height = if(context.is10InchUI) bitmap.height else (bitmap.height * 0.75).toInt()

                        val lp = imageView.layoutParams
                        lp.width = width
                        lp.height = height

                        imageView.requestLayout()
                    }

                    override fun onError(e: Exception?) {}
                })
        imageView.setOnClickListener {
            dismiss()
            val intent = VersionManager.getUpdateIntent()
            context.startActivity(intent)
        }
    }
}