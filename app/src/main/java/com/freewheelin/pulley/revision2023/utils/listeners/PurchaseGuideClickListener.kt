package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer

fun interface PurchaseGuideClickListener {
    fun onGuideImageClick(item: PurchaseGuideOffer)
}

fun interface PurchaseGuideCompareClickListener {
    fun onCompareTextClick()
}