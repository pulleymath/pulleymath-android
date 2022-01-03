package com.freewheelin.pulley.core.API.ResponseModel.mypage

data class SummaryCouponResponse (
    var data : List<SummaryCouponItem>,
    var error: String?,
    var message: String?
)


data class SummaryCouponItem (
    var couponDetailID: Long,
    var couponCampaignTitle: String,
    var description: String,
    var endAt : String,
    var couponGiveType: String,
    var couponType: String
) {
    fun canUse(): Boolean {
        return couponType == "PLUS" && (couponGiveType == "DAY" || couponGiveType == "MONTH")
    }
}