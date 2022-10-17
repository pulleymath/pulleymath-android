package com.freewheelin.pulley.revision2021.channelio.channel.view;


import android.content.Intent;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.cardview.widget.CardView;
import androidx.core.widget.NestedScrollView;

import com.freewheelin.pulley.R;
import com.freewheelin.pulley.revision2021.channelio.channel.ChatUtils;
import com.freewheelin.pulley.revision2021.channelio.channel.PResUtils;
import com.zoyi.channel.plugin.android.activity.base.navigation.GlobalNavigation;
import com.zoyi.channel.plugin.android.activity.chats.ChatsActivity;
import com.zoyi.channel.plugin.android.activity.chats.enumerate.ChatsReadState;
import com.zoyi.channel.plugin.android.activity.common.chat.ChatContentType;
import com.zoyi.channel.plugin.android.activity.common.userchat.listener.OnChatClickListener;
import com.zoyi.channel.plugin.android.activity.common.userchat.model.ChatItem;
import com.zoyi.channel.plugin.android.activity.lounge.LoungePresenter;
import com.zoyi.channel.plugin.android.activity.lounge.contract.LoungeContract;
import com.zoyi.channel.plugin.android.activity.lounge.enumerate.PreviewState;
import com.zoyi.channel.plugin.android.activity.lounge.view.app_messenger.LoungeAppButtonView;
import com.zoyi.channel.plugin.android.activity.lounge.view.app_messenger.OnIntegrationClickListener;
import com.zoyi.channel.plugin.android.activity.lounge.view.lounge_media.LoungeMediaErrorCardView;
import com.zoyi.channel.plugin.android.bind.Binder;
import com.zoyi.channel.plugin.android.bind.SubscriptionBinder;
import com.zoyi.channel.plugin.android.component.bezier.BezierButton;
import com.zoyi.channel.plugin.android.enumerate.ActionType;
import com.zoyi.channel.plugin.android.enumerate.ButtonType;
import com.zoyi.channel.plugin.android.enumerate.FetchState;
import com.zoyi.channel.plugin.android.enumerate.LoadState;
import com.zoyi.channel.plugin.android.enumerate.Transition;
import com.zoyi.channel.plugin.android.extension.Views;
import com.zoyi.channel.plugin.android.global.Action;
import com.zoyi.channel.plugin.android.model.entity.Contact;
import com.zoyi.channel.plugin.android.model.entity.LoungeMedia;
import com.zoyi.channel.plugin.android.model.rest.media.instagram.Instagram;
import com.zoyi.channel.plugin.android.open.option.Language;
import com.zoyi.channel.plugin.android.selector.AppMessengerSelector;
import com.zoyi.channel.plugin.android.selector.ChannelSelector;
import com.zoyi.channel.plugin.android.store.PopupStore;
import com.zoyi.channel.plugin.android.util.ClipboardUtils;
import com.zoyi.channel.plugin.android.util.Executor;
import com.zoyi.channel.plugin.android.util.HeightProcessor;
import com.zoyi.channel.plugin.android.util.Initializer;
import com.zoyi.channel.plugin.android.util.IntentUtils;
import com.zoyi.channel.plugin.android.util.Utils;
import com.zoyi.channel.plugin.android.view.dialog.ChannelDialog;
import com.zoyi.channel.plugin.android.view.integrations.instagram.InstagramView;
import com.zoyi.channel.plugin.android.view.layout.ChBorderLayout;
import com.zoyi.com.annimon.stream.Stream;
import com.zoyi.rx.Observable;
import com.zoyi.rx.android.schedulers.AndroidSchedulers;

import java.util.Iterator;
import java.util.List;

import io.channel.plugin.android.feature.lounge.view.LoungeChatsView;
import io.channel.plugin.android.feature.lounge.view.LoungeWelcomeView;
import io.channel.plugin.android.feature.lounge.view.listener.OnLoungeChatsLabelClickListener;
import io.channel.plugin.android.feature.settings.SettingsActivity;
import io.channel.plugin.android.lounge.app_messenger.AppMessengerBottomSheetDialog;
import io.channel.plugin.android.view.ChLoadWrapperLayout;

public class LoungeActivity extends CHBaseActivity implements LoungeContract.View, OnChatClickListener, OnIntegrationClickListener {
    private View buttonCloseLounge;
    private AppCompatTextView textDescription;
    private GlobalNavigation navigation;
    private ChLoadWrapperLayout loadWrapperLayout;
    private View viewError;
    private BezierButton buttonRefresh;
    private CardView cardChats;
    private LoungeWelcomeView welcomeView;
    private LoungeChatsView loungeChats;
    private CardView cardAppMessengers;
    private LinearLayout layoutAppMessengers;
    private ChBorderLayout layoutMoreMessengers;
    @Nullable
    private AppMessengerBottomSheetDialog bottomSheetAppMessenger;
    private ChLoadWrapperLayout loadWrapperInstagram;
    private InstagramView viewInstagram;
    private LoungeMediaErrorCardView viewInstagramError;
    private NestedScrollView scrollViewLounge;
    private LoungeContract.Presenter presenter;
    @Nullable
    private Binder appMessengersBinder;
    @Nullable
    private Binder channelBinder;
    @Nullable
    private String chatId = null;
    @Nullable
    private String presetMessage = null;
    private boolean handleOpenChat = false;
    @Nullable
    private String page;
    @Nullable
    private List<Contact> contacts;

    public LoungeActivity() {
    }

    @Initializer
    protected boolean onCreate() {
        this.init(R.layout.ch_plugin_activity_lounge);
        this.setOutTransition(Transition.SLIDE_FROM_BOTTOM);
        this.navigation = (GlobalNavigation)this.findViewById(R.id.ch_navigationLounge);
        this.navigation.setButtonClickListener(this).activateAvatar().bindChannel().setTitleSize(18).addButton(GlobalNavigation.Button.SETTINGS).addButton(GlobalNavigation.Button.EXIT);
        this.textDescription = (AppCompatTextView)this.findViewById(R.id.ch_textLoungeChannelDescription);
        ChannelSelector.bindDescription((description) -> {
            Views.setVisibility(this.textDescription, description != null);
            this.textDescription.setText(description);
        }).bind(this);
        this.buttonCloseLounge = this.findViewById(R.id.ch_buttonLoungeClose);
        this.buttonCloseLounge.setOnClickListener((v) -> {
            this.onButtonClick(GlobalNavigation.Button.EXIT);
        });
        this.loadWrapperLayout = (ChLoadWrapperLayout)this.findViewById(R.id.ch_loadWrapperLounge);
        this.viewError = this.findViewById(R.id.ch_viewLoungeError);
        this.buttonRefresh = (BezierButton)this.findViewById(R.id.ch_buttonLoungeRefresh);
        this.buttonRefresh.setOnClickListener((view) -> {
            this.presenter.fetchLoungeData();
        });
        this.cardChats = (CardView)this.findViewById(R.id.ch_cardLoungeChats);
        this.loungeChats = (LoungeChatsView)this.findViewById(R.id.ch_viewLoungeChats);
        this.loungeChats.setOnChatClickListener(this);
        this.loungeChats.setOnLoungeChatsLabelClickListener(new OnLoungeChatsLabelClickListener() {
            public void onChatsOpenClick() {
                LoungeActivity.this.showChats();
            }

            public void onReadAllChats() {
                LoungeActivity.this.presenter.readAllChats();
            }
        });
        this.welcomeView = (LoungeWelcomeView)this.findViewById(R.id.ch_viewLoungeWelcome);
        this.welcomeView.setOnStartButtonClickListener((v) -> {
            this.startChat(Transition.SLIDE_FROM_RIGHT);
        });
        this.cardAppMessengers = (CardView)this.findViewById(R.id.ch_cardLoungeIntegrations);
        this.layoutAppMessengers = (LinearLayout)this.findViewById(R.id.ch_layoutLoungeAppMessenger);
        this.layoutMoreMessengers = (ChBorderLayout)this.findViewById(R.id.ch_layoutLoungeMoreMessenger);
        int closeButtonThreshold = (int) Utils.dpToPx(this, 50.0F);
        int closeButtonSize = (int)Utils.dpToPx(this, 30.0F);
        this.scrollViewLounge = (NestedScrollView)this.findViewById(R.id.ch_scrollViewLounge);
        this.scrollViewLounge.setOnScrollChangeListener((NestedScrollView.OnScrollChangeListener) (v, scrollX, scrollY, oldScrollX, oldScrollY) -> {
            Views.setVisibility(this.buttonCloseLounge, true);
            if (scrollY < closeButtonThreshold) {
                this.buttonCloseLounge.setAlpha(0.0F);
            } else if (scrollY > closeButtonThreshold + closeButtonSize) {
                this.buttonCloseLounge.setAlpha(1.0F);
            } else {
                this.buttonCloseLounge.setAlpha((float)(scrollY - closeButtonThreshold) / (float)closeButtonSize);
            }

        });
        this.bind(new SubscriptionBinder(Observable.combineLatest(HeightProcessor.create(this.scrollViewLounge).observe(), HeightProcessor.create(this.navigation).observe(), Pair::new).subscribeOn(AndroidSchedulers.mainThread()).observeOn(AndroidSchedulers.mainThread()).subscribe((pair) -> {
            ViewGroup.LayoutParams params = this.viewError.getLayoutParams();
            params.height = (Integer)pair.first - (Integer)pair.second;
            this.viewError.setLayoutParams(params);
        })));
        this.appMessengersBinder = AppMessengerSelector.bindIntegrations(this, (contacts) -> {
            this.contacts = contacts;
            this.layoutAppMessengers.removeAllViews();
            if (contacts.size() > 0) {
                this.cardAppMessengers.setVisibility(View.VISIBLE);
                this.layoutAppMessengers.setVisibility(View.VISIBLE);
                Views.setVisibility(this.layoutMoreMessengers, contacts.size() > 3);
                Stream.of(contacts).limit(contacts.size() > 3 ? 2L : (long)contacts.size()).forEach((contact) -> {
                    this.layoutAppMessengers.addView(new LoungeAppButtonView(this, contact, this));
                });
            } else {
                this.cardAppMessengers.setVisibility(View.GONE);
                this.layoutAppMessengers.setVisibility(View.GONE);
                this.layoutMoreMessengers.setVisibility(View.GONE);
            }

        });
        this.layoutMoreMessengers.setOnClickListener((v) -> {
            if (this.bottomSheetAppMessenger != null && this.bottomSheetAppMessenger.isShowing()) {
                this.bottomSheetAppMessenger.dismiss();
            }

            this.bottomSheetAppMessenger = new AppMessengerBottomSheetDialog(this, this, this.contacts);
            this.bottomSheetAppMessenger.show();
        });
        this.loadWrapperInstagram = (ChLoadWrapperLayout)this.findViewById(R.id.ch_loadWrapperLoungeMediaInstagram);
        this.viewInstagram = (InstagramView)this.findViewById(R.id.ch_viewLoungeInstagram);
        this.viewInstagramError = (LoungeMediaErrorCardView)this.findViewById(R.id.ch_viewLoungeInstagramError);
        this.viewInstagramError.setOnErrorRefreshClickListener(() -> {
            this.presenter.fetchLoungeMediaData("instagram");
        });
        this.setLoungeMediaErrorViewDescription(this.viewInstagramError, "instagram");
        this.page = this.getString("page");
        this.presenter = new LoungePresenter(this, this.page);
        this.bindPresenter(this.presenter);
        PopupStore.get().popupMessage.set(null);
        Intent intent = this.getIntent();
        if (intent != null) {
            this.chatId = intent.getStringExtra("chatId");
            this.presetMessage = intent.getStringExtra("chatPresetMessage");
            this.handleOpenChat = intent.getBooleanExtra("handleOpenChat", false);
        }
//        final Handler handler = new Handler();
//        handler.postDelayed(new Runnable() {
//            @Override
//            public void run() {
//                startChat(Transition.SLIDE_FROM_RIGHT);
//            }
//        }, 1000);

        return true;
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (intent != null) {
            this.chatId = intent.getStringExtra("chatId");
            this.presetMessage = intent.getStringExtra("chatPresetMessage");
            this.handleOpenChat = intent.getBooleanExtra("handleOpenChat", false);
        }

    }

    protected void onResume() {
        super.onResume();
        if (this.viewInstagram != null) {
            this.viewInstagram.onResume();
        }

    }

    protected void onPause() {
        super.onPause();
        if (this.viewInstagram != null) {
            this.viewInstagram.onPause();
        }

        if (this.bottomSheetAppMessenger != null) {
            this.bottomSheetAppMessenger.hide();
        }

        if (this.welcomeView != null) {
            this.welcomeView.hideDialog();
        }

        if (this.isFinishing()) {
            Action.invoke(ActionType.MESSENGER_CLOSED);
        }

    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.viewInstagram != null) {
            this.viewInstagram.clear();
        }

        if (this.appMessengersBinder != null) {
            this.appMessengersBinder.unbind();
            this.appMessengersBinder = null;
        }

        if (this.channelBinder != null) {
            this.channelBinder.unbind();
            this.channelBinder = null;
        }

    }

    public void onButtonClick(GlobalNavigation.Button button) {
        switch(button) {
            case SETTINGS:
                IntentUtils.setNextActivity(this, SettingsActivity.class).startActivity();
                break;
            case EXIT:
                Action.invoke(ActionType.MESSENGER_CLOSED);
                this.finish();
        }

    }

    public void onPreviewStateChange(PreviewState previewState) {
        switch(previewState) {
            case CHATS:
            case WELCOME:
                this.loadWrapperLayout.setLoadState(LoadState.IDLE);
                break;
            case LOADING:
                this.loadWrapperLayout.setLoadState(LoadState.LOADING);
                break;
            case FAILED:
                this.loadWrapperLayout.setLoadState(LoadState.ERROR);
        }

        if ((previewState == PreviewState.WELCOME || previewState == PreviewState.CHATS) && this.handleOpenChat) {
            if (this.chatId != null) {
                this.startChat(ChatContentType.USER_CHAT, this.chatId, Transition.NONE);
            } else if (this.presetMessage != null) {
                this.startChat(this.presetMessage, Transition.NONE);
            } else {
                this.startChat(Transition.NONE);
            }

            this.chatId = null;
            this.presetMessage = null;
            this.handleOpenChat = false;
        }

        if (this.welcomeView != null) {
            this.welcomeView.setHasChat(previewState == PreviewState.CHATS);
        }

    }

    public void onChatsChange(List<ChatItem> items, int lessItemCount, int userAlertCount, int hiddenAlertCount) {
        if (this.cardChats != null && this.loungeChats != null) {
            Views.setVisibility(this.cardChats, items.size() > 0);
            this.loungeChats.updateChatItems(items, lessItemCount, userAlertCount, hiddenAlertCount);
        }

    }

    public void onReadStateChange(ChatsReadState state) {
        this.loungeChats.setChatsReadState(state);
    }

    public void onLoungeMediaStateChange(String type, FetchState fetchState) {
        switch(fetchState) {
            case COMPLETE:
            case FAILED:
            case LOADING:
                if ("instagram".equals(type)) {
                    this.loadWrapperInstagram.setVisibility(View.VISIBLE);
                    this.loadWrapperInstagram.setLoadState(LoadState.fromFetchState(fetchState));
                }
                break;
            case EMPTY:
                if ("instagram".equals(type)) {
                    this.loadWrapperInstagram.setVisibility(View.GONE);
                }
        }

    }

    public void onFetchLoungeMediaInstagram(List<LoungeMedia> loungeMedia) {
        Iterator var2 = loungeMedia.iterator();

        while(var2.hasNext()) {
            LoungeMedia media = (LoungeMedia)var2.next();
            if (media instanceof Instagram) {
                if (!((Instagram)media).getData().isEmpty()) {
                    this.viewInstagram.setVisibility(View.VISIBLE);
                    this.viewInstagram.setInstagram((Instagram)media);
                } else {
                    this.viewInstagram.setVisibility(View.GONE);
                }
            }
        }

    }

    public void onChatItemClick(@NonNull ChatItem chatItem) {
        this.startChat(chatItem.getType(), chatItem.getSubKey(), Transition.SLIDE_FROM_RIGHT);
    }

    public void onChatItemLongClick(@NonNull ChatItem chatItem) {
        (new ChannelDialog(this)).setTitle(PResUtils.getString("ch.chat.menu.leave_chat")).setDescription(PResUtils.getString("ch.chat.menu.leave_chat.content")).addButton(ButtonType.CANCEL).addButton(PResUtils.getString("ch.chat.menu.leave"), PResUtils.getColor(R.color.ch_bgtxt_red_normal), PResUtils.getColor(R.color.ch_bgtxt_absolute_white_dark), (v) -> {
            this.presenter.leaveChat(chatItem);
        }).allowBackpress(true).show();
    }

    public void onLinkClick(String link) {
        if (ClipboardUtils.copyToClipBoard(link)) {
            Toast.makeText(this, PResUtils.getString("ch.integrations.copy_link.success"), Toast.LENGTH_SHORT).show();
        }

    }

    public void onCallClick(String number) {
        if (number != null && Executor.canCall(this)) {
            Executor.openCall(this, number);
        } else {
            this.showPermissionDeniedToast();
        }

    }

    public void onMessengerClick(String name) {
        this.presenter.fetchConnect(name);
    }

    public void onConnectFetch(String url) {
        IntentUtils.setApp(this, url).startActivity();
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 21 && resultCode == 22) {
            this.startChat(Transition.NONE);
        }

    }

    private void showChats() {
        IntentUtils.setNextActivity(this, ChatsActivity.class).putExtra("page", this.page).startActivity();
    }

    private void startChat(Transition transition) {
        this.startChat((String)null, transition);
    }

    private void startChat(@Nullable String presetMessage, Transition transition) {
        ChatUtils.createChatActivityIntent(this, this.page).putExtra("chatPresetMessage", presetMessage).setTransition(transition).startActivityForResult(21);
    }

    private void startChat(ChatContentType contentType, @Nullable String chatId, Transition transition) {
        if (contentType == ChatContentType.USER_CHAT && chatId != null) {
            ChatUtils.createChatActivityIntent(this, this.page).putExtra("chatId", chatId).setTransition(transition).startActivityForResult(21);
        }

    }

    private void showPermissionDeniedToast() {
        Toast.makeText(this, PResUtils.getString("ch.permission.denied"), Toast.LENGTH_LONG).show();
    }

    private void setLoungeMediaErrorViewDescription(@Nullable LoungeMediaErrorCardView errorView, String type) {
        if (errorView != null) {
            errorView.setLoungeMediaType(type);
        }
    }

    public void onLanguageChange(Language language) {
        this.buttonRefresh.setText(PResUtils.getString(this, "ch.error.button"));
    }
}
