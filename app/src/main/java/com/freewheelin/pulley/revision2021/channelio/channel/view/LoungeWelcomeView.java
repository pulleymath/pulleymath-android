package com.freewheelin.pulley.revision2021.channelio.channel.view;


import android.app.Dialog;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.viewbinding.ViewBinding;

import com.freewheelin.pulley.R;
import com.zoyi.channel.plugin.android.bind.BinderControllerInterface;
import com.zoyi.channel.plugin.android.databinding.ChViewLoungeWelcomeBinding;
import com.zoyi.channel.plugin.android.enumerate.ButtonType;
import com.zoyi.channel.plugin.android.enumerate.StartButtonState;
import com.zoyi.channel.plugin.android.store.ChannelStore;
import com.zoyi.channel.plugin.android.util.ResUtils;
import com.zoyi.channel.plugin.android.util.TimeUtils;
import com.zoyi.channel.plugin.android.view.dialog.BaseDialog;
import com.zoyi.channel.plugin.android.view.dialog.ChannelDialog;
import com.zoyi.channel.plugin.android.view.textview.EllipsizeTextView;
import com.zoyi.rx.functions.Action2;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

//import io.channel.plugin.android.base.view.BaseView;
import io.channel.plugin.android.enumerate.BindingLifecycle;
import io.channel.plugin.android.enumerate.ExpectedResponseDelay;
import io.channel.plugin.android.extension.CommonExtensionsKt;
import io.channel.plugin.android.extension.ImageViewExtensionsKt;
import io.channel.plugin.android.extension.TextViewExtensionsKt;
import io.channel.plugin.android.model.api.Channel;
import io.channel.plugin.android.model.entity.ProfileEntity;
import io.channel.plugin.android.selector.ChannelSelectorKt;
import io.channel.plugin.android.selector.LoungeSelectorKt;
import kotlin.Unit;
import kotlin.jvm.JvmOverloads;
import kotlin.jvm.functions.Function4;
import kotlin.jvm.internal.DefaultConstructorMarker;
import kotlin.jvm.internal.Intrinsics;
//import kotlin.jvm.internal.StringCompanionObject;

public final class LoungeWelcomeView extends BaseView {
    private boolean hasChat;
    private boolean activateButton;
    private Dialog dialog;
    @Nullable
    private OnClickListener onStartButtonClickListener;

    @NotNull
    public ChViewLoungeWelcomeBinding initCHBinding() {
        ChViewLoungeWelcomeBinding var10000 = ChViewLoungeWelcomeBinding.inflate(LayoutInflater.from(this.getContext()), (ViewGroup)this, true);
        Intrinsics.checkNotNullExpressionValue(var10000, "ChViewLoungeWelcomeBindi…rom(context), this, true)");
        return var10000;
    }

    // $FF: synthetic method
    // $FF: bridge method
    public ViewBinding initBinding() {
        return (ViewBinding)this.initCHBinding();
    }

    @NotNull
    protected BindingLifecycle getBindingLifecycle() {
        return BindingLifecycle.VIEW;
    }

    public final boolean getHasChat() {
        return this.hasChat;
    }

    public final void setHasChat(boolean value) {
        this.hasChat = value;
        EllipsizeTextView var10000 = ((ChViewLoungeWelcomeBinding)this.getBinding()).chTextLoungeWelcomePreviewMessage;
        Intrinsics.checkNotNullExpressionValue(var10000, "binding.chTextLoungeWelcomePreviewMessage");
        var10000.setMaxLines(((Number) CommonExtensionsKt.ifTrue(value, 2, 8)).intValue());
        this.resolveStartButton();
    }

    private final void setActivateButton(boolean value) {
        this.activateButton = value;
        this.resolveStartButton();
    }

    @Nullable
    public final OnClickListener getOnStartButtonClickListener() {
        return this.onStartButtonClickListener;
    }

    public final void setOnStartButtonClickListener(@Nullable OnClickListener var1) {
        this.onStartButtonClickListener = var1;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        ((ChViewLoungeWelcomeBinding)this.getBinding()).chButtonLoungeWelcomeStart.setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View it) {
                OnClickListener var10000 = LoungeWelcomeView.this.getOnStartButtonClickListener();
                if (var10000 != null) {
                    var10000.onClick(it);
                }

            }
        }));
        ((ChViewLoungeWelcomeBinding)this.getBinding()).chLayoutLoungeWelcomeOutOfWorking.setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View it) {
                OnClickListener var10000 = LoungeWelcomeView.this.getOnStartButtonClickListener();
                if (var10000 != null) {
                    var10000.onClick(it);
                }

            }
        }));
        ((ChViewLoungeWelcomeBinding)this.getBinding()).chLayoutLoungeWelcomePreview.setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View it) {
                OnClickListener var10000 = LoungeWelcomeView.this.getOnStartButtonClickListener();
                if (var10000 != null) {
                    var10000.onClick(it);
                }

            }
        }));
        ((ChViewLoungeWelcomeBinding)this.getBinding()).chButtonLoungeWelcomeWorkingTime.setOnClickListener((OnClickListener)(new OnClickListener() {
            public final void onClick(View it) {
                LoungeWelcomeView.this.hideDialog();
                Channel var10000 = (Channel) ChannelStore.get().channelState.get();
                if (var10000 != null) {
                    Channel var2 = var10000;
                    boolean var3 = false;
                    boolean var4 = false;
//                    int var6 = false;
                    LoungeWelcomeView var14 = LoungeWelcomeView.this;
                    BaseDialog var7 = ((ChannelDialog)(new ChannelDialog(LoungeWelcomeView.this.getContext())).setTitle(ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.business_hours"))).setDescription(var2.getWorkingTimeText()).addButton(ButtonType.OK);
                    boolean var8 = false;
                    boolean var9 = false;
                    ChannelDialog itx = (ChannelDialog)var7;
                    LoungeWelcomeView var11 = var14;
//                    int var12 = false;
                    itx.show();
                    Unit var13 = Unit.INSTANCE;
                    var11.dialog = (Dialog)var7;
                }

            }
        }));
        ChannelSelectorKt.INSTANCE.bindOperationState((Function4)(new Function4() {
            // $FF: synthetic method
            // $FF: bridge method
            public Object invoke(Object var1, Object var2, Object var3, Object var4) {
                this.invoke((Boolean)var1, (ExpectedResponseDelay)var2, (Boolean)var3, (Long)var4);
                return Unit.INSTANCE;
            }

            public final void invoke(boolean inOperation, @NotNull ExpectedResponseDelay expectedResponseDelay, boolean active, @Nullable Long nextOperatingAt) {
                Intrinsics.checkNotNullParameter(expectedResponseDelay, "expectedResponseDelay");
                LoungeWelcomeView.this.setActivateButton(inOperation || active);
                LinearLayout var10000 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chButtonLoungeWelcomeWorkingTime;
                Intrinsics.checkNotNullExpressionValue(var10000, "binding.chButtonLoungeWelcomeWorkingTime");
                var10000.setEnabled(!inOperation && nextOperatingAt != null);
                View $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chButtonLoungeWelcomeWorkingTime;
                boolean condition$iv = true;
                int invisibleValue$ivx = 1;
                int $i$f$showIf = 0;
                $this$showIf$iv.setVisibility(View.VISIBLE);
                $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo;
                condition$iv = true;
                invisibleValue$ivx = 1;
                $i$f$showIf = 0;
                $this$showIf$iv.setVisibility(View.VISIBLE);
                AppCompatTextView var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
                TextViewExtensionsKt.setBold((TextView)var15, false);
                $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeChevron;
                condition$iv = false;
                byte invisibleValue$iv = 8;
                $i$f$showIf = 0;
                $this$showIf$iv.setVisibility(View.GONE);
                boolean var8;
//                StringCompanionObject var11;
                String var13;
                Object[] var14;
                String var10001;
                if (!inOperation && !active) {
                    if (nextOperatingAt != null) {
                        $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomePreview;
                        condition$iv = false;
                        invisibleValue$iv = 8;
                        $i$f$showIf = 0;
                        $this$showIf$iv.setVisibility(invisibleValue$iv);
                        $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomeOutOfWorking;
                        condition$iv = true;
                        invisibleValue$ivx = 1;
                        $i$f$showIf = 0;
                        $this$showIf$iv.setVisibility(View.VISIBLE);
                        var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeOutOfWorkingMessage;
                        Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeOutOfWorkingMessage");
//                        var11 = StringCompanionObject.INSTANCE;
                        var10001 = ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.chat.expect_response_delay.out_of_working.time_scheduling");
                        Intrinsics.checkNotNullExpressionValue(var10001, "ResUtils.getString(conte…working.time_scheduling\")");
                        var13 = var10001;
                        var14 = new Object[]{ResUtils.getString(LoungeWelcomeView.this.getContext(), TimeUtils.getWeek(nextOperatingAt).getKey()) + ' ' + TimeUtils.get(TimeUtils.TIME_24, nextOperatingAt)};
                        var8 = false;
                        var10001 = String.format(var13, Arrays.copyOf(var14, var14.length));
                        Intrinsics.checkNotNullExpressionValue(var10001, "java.lang.String.format(format, *args)");
                        var15.setText((CharSequence)var10001);
                    } else {
                        $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomePreview;
                        condition$iv = false;
                        invisibleValue$iv = 8;
                        $i$f$showIf = 0;
                        $this$showIf$iv.setVisibility(invisibleValue$iv);
                        $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomeOutOfWorking;
                        condition$iv = true;
                        invisibleValue$ivx = 1;
                        $i$f$showIf = 0;
                        $this$showIf$iv.setVisibility(View.VISIBLE);
                        var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeOutOfWorkingMessage;
                        Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeOutOfWorkingMessage");
                        var15.setText((CharSequence)ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.message_input.disabled.new_chat"));
                    }
                } else {
                    $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomePreview;
                    condition$iv = true;
                    invisibleValue$ivx = 1;
                    $i$f$showIf = 0;
                    $this$showIf$iv.setVisibility(View.VISIBLE);
                    $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chLayoutLoungeWelcomeOutOfWorking;
                    condition$iv = false;
                    invisibleValue$iv = 8;
                    $i$f$showIf = 0;
                    $this$showIf$iv.setVisibility(invisibleValue$iv);
                }

                if (inOperation) {
                    ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo.setImageResource(expectedResponseDelay.getIconId());
                    ImageViewExtensionsKt.setTint((ImageView)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo, ResUtils.getColor(LoungeWelcomeView.this.getContext(), expectedResponseDelay.getIconColorId()));
                    var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                    Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
                    var15.setText((CharSequence)ResUtils.getString(LoungeWelcomeView.this.getContext(), expectedResponseDelay.getShortKey()));
                } else if (active && nextOperatingAt != null) {
                    ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo.setImageResource(R.drawable.ch_icon_chat_error_filled);
                    ImageViewExtensionsKt.setTint((ImageView)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo, ResUtils.getColor(LoungeWelcomeView.this.getContext(), R.color.ch_bgtxt_yellow_normal));
                    var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                    Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
//                    var11 = StringCompanionObject.INSTANCE;
                    var10001 = ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.next_operating_time");
                    Intrinsics.checkNotNullExpressionValue(var10001, "ResUtils.getString(conte…\"ch.next_operating_time\")");
                    var13 = var10001;
                    var14 = new Object[]{ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.chat.expect_response_delay.out_of_working"), " ・ " + ResUtils.getString(LoungeWelcomeView.this.getContext(), TimeUtils.getWeek(nextOperatingAt).getKey()) + ' ' + TimeUtils.get(TimeUtils.TIME_24, nextOperatingAt)};
                    var8 = false;
                    var10001 = String.format(var13, Arrays.copyOf(var14, var14.length));
                    Intrinsics.checkNotNullExpressionValue(var10001, "java.lang.String.format(format, *args)");
                    var15.setText((CharSequence)var10001);
                } else if (!active && nextOperatingAt != null) {
                    $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo;
                    condition$iv = false;
                    invisibleValue$iv = 8;
                    $i$f$showIf = 0;
                    $this$showIf$iv.setVisibility(invisibleValue$iv);
                    var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                    Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
                    TextViewExtensionsKt.setBold((TextView)var15, true);
                    $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeChevron;
                    condition$iv = true;
                    invisibleValue$ivx = 1;
                    $i$f$showIf = 0;
                    $this$showIf$iv.setVisibility(View.VISIBLE);
                    var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                    Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
                    var15.setText((CharSequence)ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.chat.expect_response_delay.out_of_working.detail"));
                } else if (active && nextOperatingAt == null) {
                    ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo.setImageResource(R.drawable.ch_icon_chat_error_filled);
                    ImageViewExtensionsKt.setTint((ImageView)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chIconLoungeWelcomeWorkingTimeInfo, ResUtils.getColor(LoungeWelcomeView.this.getContext(), R.color.ch_bgtxt_yellow_normal));
                    var15 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomeWorkingTime;
                    Intrinsics.checkNotNullExpressionValue(var15, "binding.chTextLoungeWelcomeWorkingTime");
                    var15.setText((CharSequence)ResUtils.getString(LoungeWelcomeView.this.getContext(), "ch.chat.expect_response_delay.out_of_working"));
                } else if (!active && nextOperatingAt == null) {
                    $this$showIf$iv = (View)((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chButtonLoungeWelcomeWorkingTime;
                    condition$iv = false;
                    invisibleValue$iv = 8;
                    $i$f$showIf = 0;
                    $this$showIf$iv.setVisibility(invisibleValue$iv);
                }

            }
        })).bind((BinderControllerInterface)this);
        LoungeSelectorKt.bindWelcomeMessage((Action2)(new Action2() {
            // $FF: synthetic method
            // $FF: bridge method
            public void call(Object var1, Object var2) {
                this.call((CharSequence)var1, (ProfileEntity)var2);
            }

            public final void call(CharSequence text, @Nullable ProfileEntity profile) {
                EllipsizeTextView var10000 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomePreviewMessage;
                Intrinsics.checkNotNullExpressionValue(var10000, "binding.chTextLoungeWelcomePreviewMessage");
                var10000.setText(text);
                ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chAvatarLoungeWelcomePreview.set(profile);
                AppCompatTextView var3 = ((ChViewLoungeWelcomeBinding) LoungeWelcomeView.this.getBinding()).chTextLoungeWelcomePreviewName;
                Intrinsics.checkNotNullExpressionValue(var3, "binding.chTextLoungeWelcomePreviewName");
                var3.setText((CharSequence)(profile != null ? profile.getName() : null));
            }
        })).bind((BinderControllerInterface)this);
    }

    private final void resolveStartButton() {
        ((ChViewLoungeWelcomeBinding)this.getBinding()).chButtonLoungeWelcomeStart.setState(!this.activateButton ? StartButtonState.DISABLED : (this.hasChat ? StartButtonState.DIMMED : StartButtonState.ENABLED));
    }

    public final void hideDialog() {
        Dialog var10000 = this.dialog;
        if (var10000 != null) {
            Dialog var1 = var10000;
            boolean var2 = false;
            boolean var3 = false;
//            int var5 = false;
            var10000 = var1.isShowing() ? var1 : null;
            if (var10000 != null) {
                var10000.dismiss();
            }
        }

    }

    @JvmOverloads
    public LoungeWelcomeView(@NotNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Intrinsics.checkNotNullParameter(context, "context");
    }

    // $FF: synthetic method
//    public LoungeWelcomeView(Context var1, AttributeSet var2, int var3, int var4, DefaultConstructorMarker var5) {
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
    public LoungeWelcomeView(@NotNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    @JvmOverloads
    public LoungeWelcomeView(@NotNull Context context) {
        this(context, (AttributeSet)null, 0);
    }

    // $FF: synthetic method
    public static final Dialog access$getDialog$p(LoungeWelcomeView $this) {
        return $this.dialog;
    }

    // $FF: synthetic method
    public static final boolean access$getActivateButton$p(LoungeWelcomeView $this) {
        return $this.activateButton;
    }
}
