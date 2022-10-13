package com.freewheelin.pulley.revision2021.channelio.channel;

import android.app.Activity;
import android.app.Application;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.ProcessLifecycleOwner;

import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity;
import com.zoyi.channel.plugin.android.ActivityInterceptor;
import com.zoyi.channel.plugin.android.ChannelActionHandler;
import com.zoyi.channel.plugin.android.action.BootAction;
import com.zoyi.channel.plugin.android.action.ChatAction;
import com.zoyi.channel.plugin.android.action.EventAction;
import com.zoyi.channel.plugin.android.activity.chat.manager.ChatManager;
import com.zoyi.channel.plugin.android.bind.BinderCollection;
import com.zoyi.channel.plugin.android.bind.BinderController;
import com.zoyi.channel.plugin.android.enumerate.ActionType;
import com.zoyi.channel.plugin.android.enumerate.Transition;
import com.zoyi.channel.plugin.android.global.Action;
import com.zoyi.channel.plugin.android.global.LifecycleController;
import com.zoyi.channel.plugin.android.global.PrefSupervisor;
import com.zoyi.channel.plugin.android.manager.ChatVideoManager;
import com.zoyi.channel.plugin.android.model.repo.PluginRepo;
import com.zoyi.channel.plugin.android.model.rest.User;
import com.zoyi.channel.plugin.android.network.RestSubscriber;
import com.zoyi.channel.plugin.android.network.RetrofitException;
import com.zoyi.channel.plugin.android.open.callback.BootCallback;
import com.zoyi.channel.plugin.android.open.config.BootConfig;
import com.zoyi.channel.plugin.android.open.enumerate.BootStatus;
import com.zoyi.channel.plugin.android.open.listener.ChannelPluginListener;
import com.zoyi.channel.plugin.android.push.ChannelPushManager;
import com.zoyi.channel.plugin.android.selector.PageSelector;
import com.zoyi.channel.plugin.android.store.ChannelStore;
import com.zoyi.channel.plugin.android.store.GlobalStore;
import com.zoyi.channel.plugin.android.store.SettingsStore;
import com.zoyi.channel.plugin.android.store.TimerStore;
import com.zoyi.channel.plugin.android.store.base.Store;
import com.zoyi.channel.plugin.android.util.BootManager;
import com.zoyi.channel.plugin.android.util.IntentUtils;
import com.zoyi.channel.plugin.android.util.L;
import com.zoyi.channel.plugin.android.util.TimeUtils;
import com.zoyi.com.annimon.stream.Optional;

import java.util.concurrent.RejectedExecutionException;

import io.channel.plugin.android.model.api.Channel;
import io.channel.plugin.android.socket.SocketManager;

class PRealChannelIO implements BinderController {
    private Application application;
    private Thread.UncaughtExceptionHandler uncaughtExceptionHandler;
    private ChannelActionHandler handler;
    private ActivityInterceptor activityInterceptor;
    private LifecycleController lifecycleController;
    private BinderCollection binderCollection = new BinderCollection();

    PRealChannelIO(Application application) {
        this.application = application;
        this.uncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
        this.handler = new ChannelActionHandler();
        SocketManager.initialize(application);
        this.activityInterceptor = new ActivityInterceptor();
        application.registerActivityLifecycleCallbacks(this.activityInterceptor);
        this.lifecycleController = new LifecycleController();
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this.lifecycleController);
        Thread.setDefaultUncaughtExceptionHandler((thread, ex) -> {
            if (this.uncaughtExceptionHandler != null) {
                this.uncaughtExceptionHandler.uncaughtException(thread, ex);
            }

            if (!SocketManager.get().isReady() && Looper.getMainLooper().getThread() != thread && ex instanceof RejectedExecutionException) {
                SocketManager.get().reconnect();
            }

        });
    }

    Application getApplication() {
        return this.application;
    }

    void boot(BootConfig bootConfig, @Nullable BootCallback bootCallback) {
        this.shutdown();
        BootAction.boot(bootConfig, bootCallback, new RestSubscriber<PluginRepo>() {
            public void onError(RetrofitException e) {
                BootManager.sendNetworkError(bootCallback, e);
            }

            public void onNext(PluginRepo repo) {
                repo.set();
                PRealChannelIO.this.onBoot(bootConfig, repo.getChannel(), repo.getUser(), bootCallback);
                Optional.ofNullable(repo).map(PluginRepo::getUser).map(User::getPopUpChatId).ifPresent((chatId) -> {
                    ChatAction.fetchPopUpChat(repo.getUser().getPopUpChatId());
                });
            }
        });
    }

    private void onBoot(BootConfig bootConfig, Channel channel, User user, @Nullable BootCallback bootCallback) {
        PrefSupervisor.setLatestBootData(this.application, bootConfig, channel.getId(), user.getId());
        SettingsStore.get().language.set(user.getSystemLanguage());
        SettingsStore.get().channelButtonOptionState.set(bootConfig.getChannelButtonOption());
        SettingsStore.get().hidePopup.set(bootConfig.isHidePopup());
        SettingsStore.get().trackDefaultEvent.set(bootConfig.isTrackDefaultEvent());
        SettingsStore.get().showTranslation.set(PrefSupervisor.canTranslateMessage(this.application));
        SettingsStore.get().raiseSoundVibrate.set(PrefSupervisor.isEnabledPopupAlarm(this.application));
        SettingsStore.get().bubbleOptionState.set(bootConfig.getBubbleOption());
        ChannelStore.get().color.set(channel.getColor());
        ChannelStore.get().safeColor.set(channel.getSafeColor());
        ChannelStore.get().gradientColor.set(channel.getGradientColor());
        ChannelStore.get().textColor.set(channel.getTextColor());
        ChannelStore.get().bright.set(channel.getBright());
        ChatManager.initialize();

        if (GlobalStore.get().jwt.get() == null) {
            GlobalStore.get().jwt.set(PrefSupervisor.getJwt(this.application));
        }

        this.registerPushToken();
        SocketManager.get().setChannelId(channel.getId());
        if (SettingsStore.get().showLauncher.get() && SettingsStore.get().trackDefaultEvent.get()) {
            EventAction.trackPageView();
        }

        this.handler.handle();
        if (this.lifecycleController.isForeground()) {
            this.lifecycleController.doOnActivated();
        }

        TimerStore.get().now.set(TimeUtils.getCurrentTime());
        GlobalStore.get().bootState.set(true);
        if (bootCallback != null) {
            bootCallback.onComplete(BootStatus.SUCCESS, com.zoyi.channel.plugin.android.open.model.User.newInstance(user));
        }

        PrefSupervisor.clearLatestPushData(this.application);
    }

    void registerPushToken() {
        ChannelPushManager.registerPushToken(this.application);
    }

    void sleep() {
        this.handler.unHandle();
        this.lifecycleController.doOnDeactivated();
        SocketManager.get().setChannelId((String)null);
        SocketManager.get().disconnect();
        Action.invoke(ActionType.SHUTDOWN);
        Action.release();
        ChatManager.release();
        ChatVideoManager.get().clear();
        Store.destroy();
        GlobalStore.get().bootState.set(false);
    }

    void shutdown() {
        ChannelPushManager.deregisterPushToken(this.application);
        PrefSupervisor.setJwt(this.application, (String)null);
        PrefSupervisor.clearLatestBootData(this.application);
        this.clear();
        this.sleep();
    }

    public void showMessenger(Activity activity) {
        if (!GlobalStore.get().bootState.get()) {
            L.e("Fail to start messenger, please 'Boot' first");
        } else {
            ((LearningCourseActivity)activity).beginLoungeFragment();
//            IntentUtils.setNextActivity(activity, LoungeActivity.class).putExtra("handleOpenChat", false).putExtra("page", PageSelector.getPage()).setFlag(603979776).setTransition(Transition.SLIDE_FROM_BOTTOM).startActivity();
        }
    }

    public void openChat(Activity activity, @Nullable String chatId, @Nullable String message) {
        if (!GlobalStore.get().bootState.get()) {
            L.e("Fail to start messenger, please 'Boot' first");
        } else {
            ((LearningCourseActivity)activity).beginChatFragment(chatId, message);
//            IntentUtils.setNextActivity(activity, LoungeActivity.class).putExtra("chatId", chatId).putExtra("chatPresetMessage", message).putExtra("handleOpenChat", true).putExtra("page", PageSelector.getPage()).setFlag(603979776).setTransition(Transition.SLIDE_FROM_BOTTOM).startActivity();
        }
    }

    void setListener(@Nullable ChannelPluginListener listener) {
        this.handler.setListener(listener);
    }

    void clearListener() {
        this.handler.clearListener();
    }

    @Nullable
    ChannelPluginListener getListener() {
        return this.handler.getListener();
    }

    public BinderCollection getBinderCollection() {
        return this.binderCollection;
    }
}
