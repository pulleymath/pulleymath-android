package com.freewheelin.pulley.utils

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.util.Log
import com.freewheelin.pulley.views.DaebakToast

class IntentUtils {

    companion object {
        fun openWebLink(context: Context, urlString: String, pm: PackageManager) {
            val viewIntent = Intent(Intent.ACTION_VIEW)
            viewIntent.data = Uri.parse(urlString)

            val chooserIntent = Intent.createChooser(viewIntent, "실행할 앱을 선택해주세요.")
            if (viewIntent.resolveActivity(pm) != null) {
                context.startActivity(chooserIntent)
            } else {
                DaebakToast.show(context, "사용할 수 있는 브라우저 앱이 없습니다.")
            }
        }
        fun openMarketLink(context: Context, packageName: String) {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse("market://details?id=${packageName}")
            val chooserIntent = Intent.createChooser(intent, "실행할 앱을 선택해주세요.")
            context.startActivity(chooserIntent)
        }
    }

}