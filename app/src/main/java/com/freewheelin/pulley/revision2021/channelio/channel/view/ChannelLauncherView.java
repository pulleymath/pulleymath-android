package com.freewheelin.pulley.revision2021.channelio.channel.view;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freewheelin.pulley.R;
import com.freewheelin.pulley.revision2021.channelio.channel.PChannelIO;
import com.zoyi.channel.plugin.android.bind.Binder;
import com.zoyi.channel.plugin.android.open.option.ChannelButtonOption;
import com.zoyi.channel.plugin.android.selector.GlobalSelector;
import com.zoyi.channel.plugin.android.selector.SettingsSelector;
import com.zoyi.channel.plugin.android.util.ContextUtils;
import com.zoyi.channel.plugin.android.util.Utils;

public class ChannelLauncherView extends FrameLayout {
    public static final int BOTTOM_LEFT = 0;
    public static final int BOTTOM_RIGHT = 1;
    private Context context;
    private ChannelButton channelButton;
    private TextView textBadgeCount;
    private int defaultMargin;
    private Animation badgeAnimation;
    @Nullable
    private Binder launcherConfigBinder;
    @Nullable
    private Binder styleBinder;

    public ChannelLauncherView(@NonNull Context context) {
        super(context);
        this.initView(context);
    }

    public ChannelLauncherView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.initView(context);
    }

    public ChannelLauncherView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.initView(context);
    }

    private void initView(Context context) {

        this.context = context;
        View view = LayoutInflater.from(context).inflate(R.layout.ch_plugin_view_launcher, this, true);
        this.channelButton = (ChannelButton)view.findViewById(R.id.ch_fabLauncher);
        this.channelButton.setOnClickListener((v) -> {
            Activity activity = ContextUtils.getActivity(context);
            if (activity != null) {
                PChannelIO.showMessenger(activity);
            } else {

            }

        });

        this.textBadgeCount = (TextView)view.findViewById(R.id.ch_textLauncherBadge);
        this.defaultMargin = context.getResources().getDimensionPixelSize(R.dimen.ch_default_channel_button_margin);
        this.badgeAnimation = AnimationUtils.loadAnimation(context, R.anim.ch_plugin_anim_launcher_badge);
        this.setVisibility(View.VISIBLE);
        this.textBadgeCount.setVisibility(View.GONE);

    }
    public void msgShowViews() {
        boolean isChannelBtnShow = this.channelButton.getVisibility() == View.VISIBLE;
        boolean isBadgeShow = this.textBadgeCount.getVisibility() == View.VISIBLE;

    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.launcherConfigBinder = SettingsSelector.bindLauncherConfig(this::setLauncherLayout);
        this.styleBinder = GlobalSelector.bindLauncherStyle((color, alert, hasMarketingChat) -> {
            this.setLauncherBackgroundColor(color);
            this.setBadgeCount(alert);
        });
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.hide();
        if (this.launcherConfigBinder != null) {
            this.launcherConfigBinder.unbind();
        }

        if (this.styleBinder != null) {
            this.styleBinder.unbind();
        }

    }

    public void show() {
        if (this.getVisibility() == View.GONE) {
            this.setVisibility(View.VISIBLE);
            this.channelButton.showChannelButton();
            this.textBadgeCount.startAnimation(this.badgeAnimation);
        } else {

        }

    }

    public void hide() {
        this.channelButton.hideChannelButton();
        this.textBadgeCount.clearAnimation();
        this.setVisibility(View.GONE);
    }

    private void setLauncherLayout(@Nullable ChannelButtonOption channelButtonOption) {
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(-2, -2);
        params.addRule(12);
        params.addRule(10, 0);
        switch(this.getGravity(channelButtonOption)) {
            case 0:
                if (Build.VERSION.SDK_INT >= 17) {
                    params.addRule(20);
                    params.addRule(21, 0);
                }

                params.addRule(9);
                params.addRule(11, 0);
                params.setMargins(this.getXMargin(channelButtonOption), 0, 0, this.getYMargin(channelButtonOption));
                break;
            case 1:
                if (Build.VERSION.SDK_INT >= 17) {
                    params.addRule(20, 0);
                    params.addRule(21);
                }

                params.addRule(9, 0);
                params.addRule(11);
                params.setMargins(0, 0, this.getXMargin(channelButtonOption), this.getYMargin(channelButtonOption));
        }

        this.setLayoutParams(params);
        this.requestLayout();
        this.textBadgeCount.bringToFront();
    }

    private int getXMargin(@Nullable ChannelButtonOption option) {
        return option != null ? (int) Utils.dpToPx(this.context, option.getXMargin()) : this.defaultMargin;
    }

    private int getYMargin(@Nullable ChannelButtonOption option) {
        return option != null ? (int)Utils.dpToPx(this.context, option.getYMargin()) : this.defaultMargin;
    }

    private int getGravity(@Nullable ChannelButtonOption option) {
        return option != null ? option.getLauncherGravity() : 1;
    }

    private void setLauncherBackgroundColor(int backgroundColor) {
        this.channelButton.setTint(backgroundColor);
    }

    private void setBadgeCount(Integer chatCount) {
        if (chatCount != null && chatCount > 0) {
            this.textBadgeCount.setVisibility(View.VISIBLE);
            this.textBadgeCount.setText(Utils.getCount(chatCount, true));
        } else {
            this.textBadgeCount.setVisibility(View.GONE);
        }

    }
}
