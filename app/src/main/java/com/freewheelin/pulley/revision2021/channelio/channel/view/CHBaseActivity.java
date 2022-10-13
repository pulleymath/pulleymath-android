package com.freewheelin.pulley.revision2021.channelio.channel.view;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.AnimRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.freewheelin.pulley.R;
import com.zoyi.channel.plugin.android.activity.base.ActivityFrameView;
import com.zoyi.channel.plugin.android.activity.base.navigation.GlobalNavigation;
import com.zoyi.channel.plugin.android.activity.base.navigation.OnGlobalNavigationButtonClickListener;
import com.zoyi.channel.plugin.android.bind.BinderCollection;
import com.zoyi.channel.plugin.android.bind.BinderController;
import com.zoyi.channel.plugin.android.contract.BasePresenter;
import com.zoyi.channel.plugin.android.contract.BaseView;
import com.zoyi.channel.plugin.android.enumerate.ActionType;
import com.zoyi.channel.plugin.android.enumerate.Transition;
import com.zoyi.channel.plugin.android.extension.Views;
import com.zoyi.channel.plugin.android.global.Action;
import com.zoyi.channel.plugin.android.selector.SocketSelector;
import com.zoyi.channel.plugin.android.util.Initializer;
import com.zoyi.channel.plugin.android.util.ProgressHelper;
import com.zoyi.channel.plugin.android.util.ResUtils;
import com.zoyi.channel.plugin.android.util.draw.Display;
import com.zoyi.rx.Subscription;

import io.channel.plugin.android.socket.SocketManager;

public abstract class CHBaseActivity extends AppCompatActivity implements BaseView, BinderController, OnGlobalNavigationButtonClickListener {
    @Nullable
    private Subscription actionSubscription;
    @Nullable
    private BasePresenter presenter;
    private Transition outTransition;
    @Nullable
    private Dialog dialog;
    private BinderCollection binderCollection;
    private ActivityFrameView activityFrameView;

    public CHBaseActivity() {
        this.outTransition = Transition.SLIDE_FROM_RIGHT;
        this.binderCollection = new BinderCollection();
    }

    protected final void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.finish();
        } else {
            if (this.onCreate()) {
                this.actionSubscription = Action.observable().subscribe(this::handleBaseActions);
            } else {
                this.finish();
            }

        }
    }

    public GlobalNavigation initNavigation() {
        return this.initNavigation(false);
    }

    public GlobalNavigation initNavigation(boolean floating) {
        GlobalNavigation navigation = this.activityFrameView.getNavigation();
        Views.setVisibility(navigation, true);
        navigation.setButtonClickListener(this);
        this.activityFrameView.setFloating(floating);
        return navigation;
    }

    public GlobalNavigation getNavigation() {
        return this.activityFrameView.getNavigation();
    }

    public void onButtonClick(GlobalNavigation.Button button) {
        switch(button) {
            case EXIT:
                Action.invoke(ActionType.EXIT);
                break;
            case BACK:
                this.onBackPressed();
        }

    }

    /** @deprecated */
    @Deprecated
    protected void handleBaseActions(ActionType actionType) {
        switch(actionType) {
            case EXIT:
                this.finish(Transition.SLIDE_FROM_BOTTOM);
                break;
            case SHUTDOWN:
                this.finish(Transition.NONE);
        }

    }

    protected void onResume() {
        super.onResume();
        if (!Display.isLocked() && SocketSelector.doNothing()) {
            SocketManager.get().connect();
        }

    }

    protected void onDestroy() {
        if (this.presenter != null) {
            this.presenter.release();
            this.presenter = null;
        }

        if (this.actionSubscription != null) {
            if (!this.actionSubscription.isUnsubscribed()) {
                this.actionSubscription.unsubscribe();
            }

            this.actionSubscription = null;
        }

        try {
            this.getNavigation().setButtonClickListener((OnGlobalNavigationButtonClickListener)null);
        } catch (Exception var2) {
        }

        this.unbindAll();
        super.onDestroy();
    }

    protected void bindPresenter(BasePresenter presenter) {
        this.presenter = presenter;
        this.presenter.init();
    }

    @Initializer
    protected void init(@LayoutRes int layoutResId) {
        this.activityFrameView = new ActivityFrameView(this);
        this.activityFrameView.setContentView(LayoutInflater.from(this).inflate(layoutResId, (ViewGroup)null, false));
        super.setContentView(this.activityFrameView);
    }

    protected abstract boolean onCreate();

    public void setOutTransition(Transition outTransition) {
        this.outTransition = outTransition;
    }

    public void finish() {
        super.finish();
        this.overridePendingTransition(this.getEnterAnimOfFinish(), this.getExistAnimOfFinish());
    }

    public void finish(Transition transition) {
        this.outTransition = transition;
        this.finish();
    }

    public void finish(int resultCode) {
        this.setResult(resultCode);
        this.finish();
    }

    public void finish(int resultCode, Transition transition) {
        this.setResult(resultCode);
        this.outTransition = transition;
        this.finish();
    }

    @AnimRes
    private int getEnterAnimOfFinish() {
        return R.anim.ch_plugin_idle;
    }

    @AnimRes
    private int getExistAnimOfFinish() {
        switch(this.outTransition) {
            case NONE:
                return R.anim.ch_plugin_idle;
            case SLIDE_FROM_BOTTOM:
                return R.anim.ch_plugin_slide_out_bottom;
            default:
                return R.anim.ch_plugin_slide_out_right;
        }
    }

    @Nullable
    public String getString(String key) {
        Intent intent = this.getIntent();
        return intent != null ? intent.getStringExtra(key) : null;
    }

    @Nullable
    public Integer getInteger(String key) {
        Intent intent = this.getIntent();
        if (intent != null) {
            int result = intent.getIntExtra(key, -2147483648);
            if (result != -2147483648) {
                return result;
            }
        }

        return null;
    }

    public void showProgress() {
        this.showProgress(ResUtils.getString(this, "ch.settings.changing_message"));
    }

    public void showProgress(String message) {
        this.hideProgress();
        this.dialog = ProgressHelper.show(this, message, false);
    }

    public void hideProgress() {
        if (this.dialog != null && this.dialog.isShowing()) {
            this.dialog.dismiss();
            this.dialog = null;
        }

    }

    public BinderCollection getBinderCollection() {
        return this.binderCollection;
    }
}
