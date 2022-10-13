package com.freewheelin.pulley.revision2021.channelio.channel.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.viewbinding.ViewBinding;

import com.zoyi.channel.plugin.android.bind.BinderCollection;
import com.zoyi.channel.plugin.android.bind.BinderController;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.channel.plugin.android.base.view.ViewBinder;
import io.channel.plugin.android.enumerate.BindingLifecycle;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;

public abstract class BaseView extends FrameLayout implements ViewBinder, BinderController {
    private boolean inflated;
    private int configurationOrientation;
    private final BinderCollection binderCollection;
    @NotNull
    private final ViewBinding binding;

    /** @deprecated */
    // $FF: synthetic method
    public static void getBinding$annotations() {
    }

    @NotNull
    public final ViewBinding getBinding() {
        return this.binding;
    }

    @NotNull
    protected BindingLifecycle getBindingLifecycle() {
        return BindingLifecycle.MANUAL;
    }

    @NotNull
    public BinderCollection getBinderCollection() {
        return this.binderCollection;
    }

    protected final void onConfigurationChanged(@Nullable Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig != null) {
            boolean var3 = false;
            boolean var4 = false;
//            int var6 = false;
            if (this.configurationOrientation != newConfig.orientation) {
                this.onOrientationChanged();
                this.configurationOrientation = newConfig.orientation;
            }
        }

    }

    public final void addView(@Nullable View child, int index, @NotNull LayoutParams params) {
        Intrinsics.checkNotNullParameter(params, "params");
        if (!this.inflated) {
            super.addView(child, index, params);
        } else if (child != null) {
            this.onChildViewAdd(child, params);
        }

    }

    protected void onDetachedFromWindow() {
        if (this.getBindingLifecycle() == BindingLifecycle.VIEW) {
            this.unbindAll();
        }

        super.onDetachedFromWindow();
    }

    @NotNull
    public abstract ViewBinding initBinding();

    public void onOrientationChanged() {
    }

    public void onChildViewAdd(@NotNull View child, @NotNull LayoutParams params) {
        Intrinsics.checkNotNullParameter(child, "child");
        Intrinsics.checkNotNullParameter(params, "params");
        super.addView(child, -1, params);
    }

    @JvmOverloads
    public BaseView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Intrinsics.checkNotNullParameter(context, "context");
        Resources var10001 = context.getResources();
        Intrinsics.checkNotNullExpressionValue(var10001, "context.resources");
        this.configurationOrientation = var10001.getConfiguration().orientation;
        this.binderCollection = new BinderCollection();
        this.binding = this.initBinding();
        this.inflated = true;
    }

    // $FF: synthetic method
//    public BaseView(Context var1, AttributeSet var2, int var3, int var4, DefaultConstructorMarker var5) {
//        if ((var4 & 2) != 0) {
//            var2 = (AttributeSet)null;
//        }
//
//        if ((var4 & 4) != 0) {
//            var3 = 0;
//        }
//
//        this(var1, var2, var3);
//    }

    @JvmOverloads
    public BaseView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @JvmOverloads
    public BaseView(@NotNull Context context) {
        this(context, (AttributeSet)null, 0);
    }
}