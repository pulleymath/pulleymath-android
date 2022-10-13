package com.freewheelin.pulley.revision2021.channelio.channel;


import android.app.Activity;
import androidx.annotation.Nullable;

import com.freewheelin.pulley.revision2021.channelio.channel.view.ChatActivity;
import com.zoyi.channel.plugin.android.util.IntentUtils;

public class ChatUtils {
    public ChatUtils() {
    }

    public static IntentUtils createChatActivityIntent(Activity activity, @Nullable String page) {
        return IntentUtils.setNextActivity(activity, ChatActivity.class).putExtra("page", page).setFlag(67108864);
    }
}
