package com.freewheelin.pulley.core.API.ResponseModel.mypage

data class CouponItem (
    var couponDetailID: Long,
    var couponCampaignTitle: String,
    var description: String,
    var endAt : String,
    var couponGiveType: String,
    var couponType: CouponType,
    var canApplyNow: Boolean
) {
    fun canUse(): Boolean {
        return couponType == CouponType.PLUS && (couponGiveType == "DAY" || couponGiveType == "MONTH")
    }

    enum class CouponType {
        PLUS,
        LESSON,
        GOODS,
        BOOKS
    }
}