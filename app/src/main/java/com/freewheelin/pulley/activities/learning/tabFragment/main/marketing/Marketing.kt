package com.freewheelin.pulley.activities.learning.tabFragment.main.marketing

import com.freewheelin.pulley.revision2023.model.PaidServiceType

data class Marketing(
    var banners: MutableList<Banner>,
    val marketingCode: Int,
    val marketingTitle: String
)

data class Banner(
        val code: Int,
        val title: String,
        val imageURL: String,
        val link: String,
        val startDate: String,
        val endDate: String,
        val userSegment: List<PaidServiceType>,
        val studentSegment: List<StudentSegment>
)

enum class StudentSegment {
    Middle1, Middle2, Middle3, High1, High2, High3, N, All
}

enum class UserSegment {
    None, Paid_Ing, Basic_C, Basic_P, Standard, Premium, All
}