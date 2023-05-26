package com.freewheelin.pulley.revision2023.utils.listeners

import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer

fun interface PurchaseGuideClickListener {
    fun onGuideClick(item: PurchaseGuideOffer, position: Int)
}

fun interface PurchaseGuideCompareClickListener {
    fun onCompareTextClick()
}