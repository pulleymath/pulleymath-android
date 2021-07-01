package com.freewheelin.pulley.utils

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TextUtilsTest {

    @Test
    fun `순수 한글 글자로 이루어진 스트링만 이름이다`() {
        assertTrue("김현태".isValidName())
        assertTrue("남소정".isValidName())
        assertTrue("val adio".isValidName())
        assertTrue("val adio".isValidName())
        assertTrue("Kim hyuntae".isValidName())
        assertTrue("Kim".isValidName())

        assertFalse("".isValidName())
        assertFalse("  ".isValidName())
        assertFalse(",".isValidName())
        assertFalse("남소정1".isValidName())
        assertFalse("남소정a".isValidName())
    }

    @Test
    fun `입력된 문자열이 전화번호 형식인지 아닌지 적절히 리턴해야한다`() {
        assertTrue("01066373355".isValidPhoneNum())
        assertTrue("0116637335".isValidPhoneNum())
        assertTrue("01092321969".isValidPhoneNum())
        assertTrue("01011112222".isValidPhoneNum())
        assertTrue("0166520609".isValidPhoneNum())

        assertFalse("".isValidPhoneNum())
        assertFalse(" ".isValidPhoneNum())
        assertFalse("0166520609222".isValidPhoneNum())
        assertFalse("남소정".isValidPhoneNum())
        assertFalse("김현태".isValidPhoneNum())
    }

    @Test
    fun `입력된 문자열이 6자리 이상 20자리 이하, 숫자 + 문자 + 특수문자 형식이 맞는지 확인해야한다`() {
        assertTrue("00000r".isValidPW())
        assertTrue("r00000".isValidPW())
        assertTrue("00RR00".isValidPW())
        assertTrue("rrrrr_".isValidPW())
        assertTrue("_rrrrr".isValidPW())
        assertTrue("rr__rㅉㅈr".isValidPW())
        assertTrue("_00000".isValidPW())
        assertTrue("00000_".isValidPW())
        assertTrue("00__00".isValidPW())
        assertTrue("11@@AA".isValidPW())
        assertTrue("11AA@@".isValidPW())
        assertTrue("AA11@@".isValidPW())
        assertTrue("AA@@11".isValidPW())

        assertTrue("a0123456789012345678".isValidPW())
        assertFalse("a01234567890123456789".isValidPW())

        assertTrue("!0123456789012345678".isValidPW())
        assertFalse("!01234567890123456789".isValidPW())

        assertTrue("!a012345678901234567".isValidPW())
        assertFalse("!a0123456789012345678".isValidPW())

        assertFalse("000000".isValidPW())
        assertFalse("".isValidPW())
        assertFalse("AAAAAA".isValidPW())
        assertFalse("!!!!!!".isValidPW())
        assertFalse("    1a".isValidPW())
    }

    @Test
    fun `입력된 문자열에 숫자가 있는지 정확히 리턴한다`() {
        assertTrue("1111111".isContainDigit())
        assertTrue("1AAAAAAA".isContainDigit())
        assertTrue("1".isContainDigit())
        assertTrue("AA1".isContainDigit())

        assertFalse("AAA".isContainDigit())
        assertFalse("".isContainDigit())
        assertFalse(" ".isContainDigit())
    }

    @Test
    fun `입력된 문자열에 알파벳이 있는지 정확히 리턴한다`() {
        assertTrue("aaaaaa1".isContainAlphabet())
        assertTrue("11111a1".isContainAlphabet())
        assertTrue("1AAAAAAA1".isContainAlphabet())
        assertTrue("a".isContainAlphabet())
        assertTrue("AA1".isContainAlphabet())

        assertFalse("111".isContainAlphabet())
        assertFalse("".isContainAlphabet())
        assertFalse(" ".isContainAlphabet())
    }

    @Test
    fun `입력된 문자열에 특수문자가 있는지 정확히 리턴한다`() {
        assertTrue("!!!!!!1".isContainSpecial())
        assertTrue("11111!1".isContainSpecial())
        assertTrue("1!!!!!!1".isContainSpecial())
        assertTrue("!".isContainSpecial())
        assertTrue("!!1".isContainSpecial())

        assertFalse("111".isContainSpecial())
        assertFalse("".isContainSpecial())
        assertFalse(" ".isContainSpecial())
    }
}