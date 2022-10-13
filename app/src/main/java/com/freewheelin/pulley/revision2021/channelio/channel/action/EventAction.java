package com.freewheelin.pulley.revision2021.channelio.channel.action;

import android.util.Log;

import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO;
import com.zoyi.channel.plugin.android.ChannelIO;
import com.zoyi.channel.plugin.android.global.Api;
import com.zoyi.channel.plugin.android.global.PrefSupervisor;
import com.zoyi.channel.plugin.android.model.etc.Event;
import com.zoyi.channel.plugin.android.store.GlobalStore;

import java.util.HashMap;

public class EventAction {
    public EventAction() {
    }

    public static void trackPageView() {
        track(new Event("PageView", new HashMap()));
    }

    public static void track(Event event) {
        String channelId = PrefSupervisor.getLatestBootedChannelId(PChannelIO.getAppContext());
        String jwt = PrefSupervisor.getJwt(PChannelIO.getAppContext());
        if (jwt == null) {
            jwt = GlobalStore.get().jwt.get();
        }

        if (channelId != null && jwt != null) {
            Api.trackEvent(channelId, event.getName(), event.getPropertyToJson(), jwt).run();
        }

    }
}
