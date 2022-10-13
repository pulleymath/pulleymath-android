package com.freewheelin.pulley.revision2021.channelio.channel.view;


import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.cardview.widget.CardView;

import com.freewheelin.pulley.R;
import com.zoyi.channel.plugin.android.extension.ImageViews;
import com.zoyi.channel.plugin.android.util.Initializer;

public class ChannelButton extends FrameLayout {
    private CardView cardRoot;
    private FrameLayout layoutChannelButton;
    private AppCompatImageView imageRoot;
    private AppCompatImageView imageBody;
    private AppCompatImageView imageFace;
    private Animation launcherAnimation;

    public ChannelButton(@NonNull Context context) {
        super(context);
        this.init(context);
    }

    public ChannelButton(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.init(context);
    }

    public ChannelButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(context);
    }

    @Initializer
    private void init(Context context) {
        View root = LayoutInflater.from(context).inflate(R.layout.ch_view_channel_button, this, true);
        this.launcherAnimation = AnimationUtils.loadAnimation(context, R.anim.ch_plugin_anim_launcher);
        this.cardRoot = (CardView)root.findViewById(R.id.ch_cardChannelButton);
        this.layoutChannelButton = (FrameLayout)root.findViewById(R.id.ch_layoutChannelButton);
        this.imageRoot = (AppCompatImageView)root.findViewById(R.id.ch_imageChannelButtonRoot);
        this.imageBody = (AppCompatImageView)root.findViewById(R.id.ch_imageChannelButtonBody);
        this.imageFace = (AppCompatImageView)root.findViewById(R.id.ch_imageChannelButtonFace);
    }

    public void setTint(int colorId) {
        ImageViews.setTint(this.imageRoot, colorId);
        ImageViews.setTint(this.imageBody, colorId);
        ImageViews.setTint(this.imageFace, colorId);
    }

    public void showChannelButton() {
        this.cardRoot.startAnimation(this.launcherAnimation);
        this.layoutChannelButton.startAnimation(this.launcherAnimation);
    }

    public void hideChannelButton() {
        this.cardRoot.clearAnimation();
        this.layoutChannelButton.clearAnimation();
    }
}

