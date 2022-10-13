package com.freewheelin.pulley.revision2021.channelio.channel.view;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freewheelin.pulley.R;
import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO;
import com.zoyi.channel.plugin.android.bind.Binder;
import com.zoyi.channel.plugin.android.selector.GlobalSelector;
import com.zoyi.channel.plugin.android.selector.PopupSelector;
import com.zoyi.channel.plugin.android.store.PopupStore;
import com.zoyi.channel.plugin.android.util.ContextUtils;
import com.zoyi.channel.plugin.android.util.Initializer;
import com.zoyi.channel.plugin.android.view.listener.OnPopupClickListener;

import io.channel.plugin.android.view.popup.BubblePopupView;
import io.channel.plugin.android.view.popup.FullScreenPopupView;

public class ChannelView extends FrameLayout implements OnPopupClickListener {
    private Context context;
    private ChannelLauncherView launcherView;
    private BubblePopupView viewBubblePopup;
    private FullScreenPopupView viewFullscreenPopup;
    @Nullable
    private Binder visibilityBinder;
    @Nullable
    private Binder launcherVisibilityBinder;
    @Nullable
    private Binder popupBinder;

    public ChannelView(Context context) {
        super(context);
        this.init(context);
    }

    public ChannelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public ChannelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    @Initializer
    private void init(Context context) {
        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.ch_plugin_layout_channel_view, this, true);
        this.launcherView = (ChannelLauncherView)view.findViewById(R.id.ch_viewLauncher);
        this.launcherView.show();

        this.viewBubblePopup = (BubblePopupView)view.findViewById(R.id.ch_viewBubblePopup);
        this.viewBubblePopup.init();
        this.viewBubblePopup.setOnPopupClickListener(this);
        this.viewFullscreenPopup = (FullScreenPopupView)view.findViewById(R.id.ch_viewFullScreenPopup);
        this.viewFullscreenPopup.init();
        this.viewFullscreenPopup.setOnPopupClickListener(this);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.visibilityBinder = GlobalSelector.bindBootState((booted) -> {
            this.setVisibility(booted ? View.VISIBLE : View.GONE);
        });
        this.launcherVisibilityBinder = GlobalSelector.bindLauncherVisibility((showLauncher) -> {
            if (showLauncher) {
                this.launcherView.show();
            } else {
                this.launcherView.hide();
            }

        });
        this.popupBinder = PopupSelector.bindPopup((message) -> {
            if (message != null) {
                if (message.getMarketing() != null && message.getMarketing().isFullScreen()) {
                    this.viewBubblePopup.hide();
                    this.viewFullscreenPopup.show(message);
                } else {
                    this.viewFullscreenPopup.hide();
                    this.viewBubblePopup.show(message);
                }
            } else {
                this.viewBubblePopup.hide();
                this.viewFullscreenPopup.hide();
            }

        });
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.visibilityBinder != null) {
            this.visibilityBinder.unbind();
        }

        if (this.launcherVisibilityBinder != null) {
            this.launcherVisibilityBinder.unbind();
        }

        if (this.popupBinder != null) {
            this.popupBinder.unbind();
        }

    }

    public void onPopupClick(@NonNull String chatId) {
        PopupStore.get().popupMessage.set(null);
        Activity activity = ContextUtils.getActivity(this.context);
        if (activity != null) {
            PChannelIO.openChat((Activity)this.context, chatId, (String)null);
        }

    }
}
