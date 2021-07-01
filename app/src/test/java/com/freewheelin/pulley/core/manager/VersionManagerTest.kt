package com.freewheelin.pulley.core.manager

import com.freewheelin.pulley.core.manage.VersionInfo
import com.freewheelin.pulley.core.manage.VersionManager
import junit.framework.Assert.assertFalse
import junit.framework.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito
import org.mockito.internal.configuration.injection.MockInjection
import java.util.*

class VersionManagerTest {

    @Test
    fun `버전 info에 따라 isNeedUpdateDialog는 적절한 값을 리턴해야함`() {
        val info = Mockito.mock(VersionInfo::class.java)
        val suspendInfo = Mockito.mock(VersionInfo::class.java)
        Mockito.`when`(info.versionCode).thenReturn(VersionManager.appVersionCode + 5)

        assertTrue(VersionManager.isNeedUpdateDialog(info, null))

        Mockito.`when`(suspendInfo.versionCode).thenReturn(VersionManager.appVersionCode + 5)
        Mockito.`when`(suspendInfo.suspendStartDate).thenReturn(
                Date(1540190110000)
        )
        assertTrue(VersionManager.isNeedUpdateDialog(info, suspendInfo))

        Mockito.`when`(suspendInfo.suspendStartDate).thenReturn(
                Date()
        )
        assertFalse(VersionManager.isNeedUpdateDialog(info, suspendInfo))
        Mockito.`when`(suspendInfo.versionCode).thenReturn(VersionManager.appVersionCode + 4)
        assertTrue(VersionManager.isNeedUpdateDialog(info, suspendInfo))


        Mockito.`when`(info.versionCode).thenReturn(VersionManager.appVersionCode - 2)
        assertFalse(VersionManager.isNeedUpdateDialog(info, null))

        Mockito.`when`(info.versionCode).thenReturn(VersionManager.appVersionCode + 2)
        Mockito.`when`(info.requiredMinVersion).thenReturn(VersionManager.appVersionCode + 1)
        Mockito.`when`(suspendInfo.versionCode).thenReturn(VersionManager.appVersionCode + 1)
        assertTrue(VersionManager.isNeedUpdateDialog(info, suspendInfo))

    }
}