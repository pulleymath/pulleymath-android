package com.freewheelin.pulley.revision2021.channelio.channel;


import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

//import com.zoyi.channel.plugin.android.action.EventAction;
import com.freewheelin.pulley.revision2021.channelio.channel.action.EventAction;
import com.zoyi.channel.plugin.android.action.TagAction;
import com.zoyi.channel.plugin.android.action.UserAction;
import com.zoyi.channel.plugin.android.bind.BinderControllerInterface;
import com.zoyi.channel.plugin.android.bind.EmptyBinderController;
import com.zoyi.channel.plugin.android.enumerate.ActionType;
import com.zoyi.channel.plugin.android.global.Action;
import com.zoyi.channel.plugin.android.global.PrefSupervisor;
import com.zoyi.channel.plugin.android.model.etc.Event;
import com.zoyi.channel.plugin.android.open.callback.BootCallback;
import com.zoyi.channel.plugin.android.open.callback.UserUpdateCallback;
import com.zoyi.channel.plugin.android.open.config.BootConfig;
import com.zoyi.channel.plugin.android.open.enumerate.BootStatus;
import com.zoyi.channel.plugin.android.open.exception.ChannelException;
import com.zoyi.channel.plugin.android.open.listener.ChannelPluginListener;
import com.zoyi.channel.plugin.android.open.model.User;
import com.zoyi.channel.plugin.android.open.model.UserData;
import com.zoyi.channel.plugin.android.push.ChannelPushClient;
import com.zoyi.channel.plugin.android.push.ChannelPushManager;
import com.zoyi.channel.plugin.android.store.ChannelStore;
import com.zoyi.channel.plugin.android.store.PageStore;
import com.zoyi.channel.plugin.android.store.SettingsStore;
import com.zoyi.channel.plugin.android.util.ListUtils;

import java.util.List;
import java.util.Map;

public class PChannelIO {
    private static boolean isDebugMode = false;
    private static boolean attachChannelView = true;
    @Nullable
    private static PRealChannelIO realChannelIO;
    private static EmptyBinderController emptyBinderController = new EmptyBinderController();

    public PChannelIO() {
    }

    public static void initialize(@Nullable Application application) {
        initialize(application, true);
    }

    public static void initialize(@Nullable Application application, boolean attachView) {
        if (application == null) {
            Log.e("ChannelIO", "Fail to 'initialize', Application can't be NULL");
        } else if (Build.VERSION.SDK_INT < 21) {
            Log.e("ChannelIO", "You can use SDK from OS version 21");
        } else if (realChannelIO != null) {
            Log.e("ChannelIO", "Fail to 'initialize', Channel plugin already initialized");
        } else {
            attachChannelView = attachView;
            realChannelIO = new PRealChannelIO(application);
        }

    }

    public static boolean isAttachChannelView() {
        return attachChannelView;
    }

    public static void setDebugMode(boolean enable) {
        isDebugMode = enable;
    }

    public static void boot(@Nullable BootConfig bootConfig) {
        boot(bootConfig, (BootCallback)null);
    }

    public static void boot(@Nullable BootConfig bootConfig, @Nullable BootCallback bootCallback) {
        if (realChannelIO == null) {
            Log.e("ChannelIO", "Fail to boot, Initialize first");
            if (bootCallback != null) {
                bootCallback.onComplete(BootStatus.NOT_INITIALIZED, (User)null);
            }
        } else if (bootConfig != null && bootConfig.getPluginKey() != null) {
            realChannelIO.boot(bootConfig, bootCallback);
        } else {
            Log.e("ChannelIO", "Fail to boot, Check boot configuration");
            if (bootCallback != null) {
                bootCallback.onComplete(BootStatus.NOT_INITIALIZED, (User)null);
            }
        }

    }

    public static void sleep() {
        if (realChannelIO != null) {
            realChannelIO.sleep();
        }

    }

    public static void shutdown() {
        if (realChannelIO != null) {
            realChannelIO.shutdown();
        }

    }

    public static void showMessenger(@Nullable Activity activity) {
        if (activity != null && realChannelIO != null) {
            realChannelIO.showMessenger(activity);
        }

    }

    public static void hideMessenger() {
        Action.invoke(ActionType.EXIT);
    }

    public static void openChat(@Nullable Activity activity, @Nullable String chatId, @Nullable String message) {
        if (activity != null && realChannelIO != null) {
            realChannelIO.openChat(activity, chatId, message);
        }

    }

    public static void showChannelButton() {
        if (!SettingsStore.get().showLauncher.get()) {
            SettingsStore.get().showLauncher.set(true);
            if (isBooted() && SettingsStore.get().trackDefaultEvent.get()) {
                EventAction.trackPageView();
            }
        }
    }

    public static void hideChannelButton() {
        SettingsStore.get().showLauncher.set(false);
    }

    public static void setListener(ChannelPluginListener listener) {
        if (realChannelIO != null) {
            realChannelIO.setListener(listener);
        }

    }

    public static void clearListener() {
        if (realChannelIO != null) {
            realChannelIO.clearListener();
        }

    }

    @Nullable
    public static ChannelPluginListener getListener() {
        return realChannelIO != null ? realChannelIO.getListener() : null;
    }

    public static void track(@NonNull @Size(min = 1L,max = 30L) String eventName) {
        track(eventName, (Map)null);
    }

    public static void track(@NonNull @Size(min = 1L,max = 30L) String eventName, @Nullable Map<String, Object> eventProperty) {
        if (TextUtils.isEmpty(eventName)) {
            Log.e("ChannelIO", "Fail to track event. Event name can't be blank or null.");
        } else if (eventName.length() > 30) {
            Log.e("ChannelIO", "Fail to track event. Event name must be 30 characters or less.");
        } else {
            String jwt = PrefSupervisor.getJwt(getAppContext());
            if (jwt == null) {
                Log.e("ChannelIO", "Fail to track event. Unauthorized access.");
            } else {
                EventAction.track(new Event(eventName, eventProperty));
            }
        }
    }

    public static void initPushToken(String token) {
        Context context = getAppContext();
        if (context != null) {
            PrefSupervisor.setDeviceToken(context, token);
        }

        if (realChannelIO != null) {
            realChannelIO.registerPushToken();
        }

    }

    public static boolean isChannelPushNotification(Map<String, String> message) {
        return ChannelPushManager.isChannelPushNotification(message);
    }

    public static boolean hasStoredPushNotification(@Nullable Activity activity) {
        return activity != null ? ChannelPushClient.hasStoredPushNotification(activity) : false;
    }

    public static void openStoredPushNotification(@Nullable Activity activity) {
        if (activity != null) {
            ChannelPushClient.openStoredPushNotification(activity);
        }

    }

    public static void receivePushNotification(Context context, Map<String, String> message) {
        if (context != null) {
            ChannelPushManager.receivePushNotification(context, message);
        }

    }

    public static void updateUser(@Nullable UserData userData, @Nullable UserUpdateCallback callback) {
        if (isBooted()) {
            UserAction.updateUser(userData, callback);
        } else if (callback != null) {
            callback.onComplete(ChannelException.newInstance("Please boot first"), (User)null);
        }

    }

    public static void addTags(String... tags) {
        if (tags != null) {
            addTags(ListUtils.newArrayList(tags), (UserUpdateCallback)null);
        }

    }

    public static void addTags(List<String> tags, @Nullable UserUpdateCallback callback) {
        if (isBooted()) {
            TagAction.addTags(tags, callback);
        } else if (callback != null) {
            callback.onComplete(ChannelException.newInstance("Please boot first"), (User)null);
        }

    }

    public static void removeTags(String... tags) {
        if (tags != null) {
            removeTags(ListUtils.newArrayList(tags), (UserUpdateCallback)null);
        }
    }

    public static void removeTags(List<String> tags, @Nullable UserUpdateCallback callback) {
        if (isBooted()) {
            TagAction.removeTags(tags, callback);
        } else if (callback != null) {
            callback.onComplete(ChannelException.newInstance("Please boot first"), (User)null);
        }

    }

    public static void setPage(@Nullable String page) {
        PageStore.get().page.set(page);
        PageStore.get().isPageApplied.set(true);
    }

    public static void resetPage() {
//        PageStore.get().page.set((Object)null); // 원본
        PageStore.get().page.set((String) null);

        PageStore.get().isPageApplied.set(false);
    }

    public static boolean isInitialized() {
        return realChannelIO != null;
    }

    @Nullable
    public static Context getAppContext() {
        return realChannelIO != null ? realChannelIO.getApplication() : null;
    }

    public static boolean isDebugMode() {
        return isDebugMode;
    }

    public static boolean isBooted() {
        return ChannelStore.get().channelState.get() != null;
    }

    public static BinderControllerInterface getBinderController() {
        return (BinderControllerInterface)(isBooted() && realChannelIO != null ? realChannelIO : emptyBinderController);
    }
}

