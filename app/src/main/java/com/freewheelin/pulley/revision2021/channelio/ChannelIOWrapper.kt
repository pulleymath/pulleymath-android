package com.freewheelin.pulley.revision2021.channelio

import android.app.Application
import com.freewheelin.pulley.legacy.bases.user
import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO
import com.freewheelin.pulley.legacy.utils.Preferences
import com.zoyi.channel.plugin.android.ChannelIO
import com.zoyi.channel.plugin.android.global.PrefSupervisor
import com.zoyi.channel.plugin.android.open.config.BootConfig
import com.zoyi.channel.plugin.android.open.enumerate.BootStatus
import com.zoyi.channel.plugin.android.open.enumerate.ChannelButtonPosition
import com.zoyi.channel.plugin.android.open.listener.ChannelPluginListener
import com.zoyi.channel.plugin.android.open.model.Profile
import com.zoyi.channel.plugin.android.open.option.ChannelButtonOption
import com.zoyi.channel.plugin.android.store.ChannelStore
import io.channel.plugin.android.enumerate.BubblePosition
import io.channel.plugin.android.open.option.BubbleOption

object ChannelIOWrapper {
    fun initialize(application: Application, listener: ChannelPluginListener) {
        ChannelIO.initialize(application)
        ChannelIO.setListener(listener);

//        if (ChannelIO.isBooted()) {
//            ChannelIO.showChannelButton()
//            return
//        }
        PChannelIO.initialize(application)
        PChannelIO.setListener(listener);

//        ChannelIO.initialize(application)
//        ChannelIO.setListener(listener);


        val studentId = user?.studentID
        val name = user?.fullName
        val email = user?.email
        val profile: Profile = Profile.create()
            .setName("$name")
            .setEmail(email)
            .setProperty("platform", "android")

        val buttonOption = ChannelButtonOption(ChannelButtonPosition.LEFT, 16f, 23f)
        val bubbleOption = BubbleOption(BubblePosition.BOTTOM, 30f)

//        val bootConfig = BootConfig.create("bdb138aa-8f94-4615-be4f-be2c654fa08a")
        val bootConfig = BootConfig.create("f5184964-6f2c-4948-a0e6-3e16196797be")
            .setMemberId(studentId)
            .setProfile(profile)
            .setChannelButtonOption(buttonOption)
            .setBubbleOption(bubbleOption)

        ChannelIO.boot(bootConfig) { bootStatus, user ->
            val jwt = PrefSupervisor.getJwt(PChannelIO.getAppContext())
            val channelId = ChannelStore.get().channelState.get()?.id
            Preferences.channelTalkUserId.set(user?.id ?: "")
            if (bootStatus == BootStatus.SUCCESS && user != null) {
                ChannelIO.showChannelButton()
            } else {
                println("error, boot 몬가일어나고잇다.. ")
            }
        }
    }
}