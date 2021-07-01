package com.freewheelin.pulley.assets

class LocationConfig(val id: Int, val type: Type, val location: String) {

    enum class Type {
        Special_City,
        Metropolitan_City,
        Province,
        Self_governing_City,
        Self_Governing_Province;

        val titleSuffix: String
            get() {
                return when (this) {
                    Special_City -> "특별시"
                    Metropolitan_City -> "광역시"
                    Province -> "도"
                    Self_Governing_Province -> "특별자치도"
                    Self_governing_City -> "특별자치시"
                }
            }
    }

    companion object {
        val seoul = LocationConfig(1, Type.Special_City, "서울")

        val busan = LocationConfig(2, Type.Metropolitan_City, "부산")
        val daegu = LocationConfig(3, Type.Metropolitan_City, "대구")
        val incheon = LocationConfig(4, Type.Metropolitan_City, "인천")
        val gwangju = LocationConfig(5, Type.Metropolitan_City, "광주")
        val daejeon = LocationConfig(6, Type.Metropolitan_City, "대전")
        val ulsan = LocationConfig(7, Type.Metropolitan_City, "울산")

        val gyeonggi = LocationConfig(8, Type.Province, "경기")
        val gangwon = LocationConfig(9, Type.Province, "강원")
        val chungbuk = LocationConfig(10, Type.Province, "충청북")
        val chungnam = LocationConfig(11, Type.Province, "충청남")
        val jeonbuk = LocationConfig(12, Type.Province, "전라북")
        val jeonnam = LocationConfig(13, Type.Province, "전라남")
        val gyeongbuk = LocationConfig(14, Type.Province, "경상북")
        val gyeongnam = LocationConfig(15, Type.Province, "경상남")

        val jeju = LocationConfig(16, Type.Self_Governing_Province, "제주")
        val sejong = LocationConfig(17, Type.Self_governing_City, "세종")


        val locations = listOf(seoul, busan, daegu, incheon, gwangju, daejeon, ulsan, gyeonggi, gangwon, chungbuk, chungnam, jeonbuk, jeonnam, gyeongbuk, gyeongnam, jeju, sejong)

    }

    val title: String = location + type.titleSuffix


}