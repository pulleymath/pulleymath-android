package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem

data class PurchaseGuide (
    val offers: List<PurchaseGuideOffer>,
    val backgroundColor: String
)

data class PurchaseGuideOffer (
    val offerId: Int,
    val productSubType: PaidServiceType,
    val imageUrl: String
): BaseDiffItem {
    override fun getId(): String {
        return "$offerId"
    }
}