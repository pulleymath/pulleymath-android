package com.freewheelin.pulley.revision2023.model

import androidx.databinding.ObservableBoolean
import com.freewheelin.pulley.revision2021.activity.base.BaseDiffItem

data class PurchaseGuide (
    val single: List<PurchaseGuideOffer>,
    val regular: List<PurchaseGuideOffer>,
)

class PurchaseGuideOffer (
//    val offerId: Int,
//    val offerType: SubType,
//    val productSubType: PaidServiceType,
//    val defaultPrice: String,
//    val eventPrice: String,
//    val discountRate: Int,
//    val detailImageUrl: String

): BaseDiffItem {
    val offerId: Int = 0
    val offerType: SubType = SubType.PLUS
    val productSubType: PaidServiceType = PaidServiceType.PREMIUM
    lateinit var defaultPrice: String
    lateinit var eventPrice: String
    var subPrice: String? = null
    val discountRate: Int = 0
    lateinit var detailImageUrl: String


    var isSelected: ObservableBoolean = ObservableBoolean(false)
    override fun getId(): String {
        return "$offerId"
    }
    enum class SubType {
        PLUS_SINGLE, PLUS
    }
}