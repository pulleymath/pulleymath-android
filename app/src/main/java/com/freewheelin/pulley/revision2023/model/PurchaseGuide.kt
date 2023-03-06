package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem

data class PurchaseGuide (
    val offers: List<PurchaseGuideOffer>
)

data class PurchaseGuideOffer (
    val offerId: Int,
    val commonImageUrl: String,
    val selectedImageUrl : String,
    val isSelected: Boolean = false
): BaseDiffItem {
    override fun getId(): String {
        return "$offerId"
    }
}