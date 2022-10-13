package com.freewheelin.pulley.revision2021.channelio.channel.view;


import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.util.Log;
import android.util.Pair;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.freewheelin.pulley.R;
import com.freewheelin.pulley.revision2021.channelio.channel.PResUtils;
import com.zoyi.channel.plugin.android.action.MarketingAction;
import com.zoyi.channel.plugin.android.action.TranslateAction;
import com.zoyi.channel.plugin.android.activity.base.navigation.GlobalNavigation;
import com.zoyi.channel.plugin.android.activity.chat.ChatAdapter;
import com.zoyi.channel.plugin.android.activity.chat.dialog.ReactionsDialog;
import com.zoyi.channel.plugin.android.activity.chat.listener.TypingListener;
import com.zoyi.channel.plugin.android.activity.chat.listener.view.ChatInteractionActionListener;
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnAttachmentUploadContentActionListener;
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnMessageActionListener;
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnSendingActionListener;
import com.zoyi.channel.plugin.android.activity.chat.model.ChatMessageItem;
import com.zoyi.channel.plugin.android.activity.chat.model.MessageItem;
import com.zoyi.channel.plugin.android.activity.chat.model.SendFileItem;
import com.zoyi.channel.plugin.android.activity.chat.model.SendItem;
import com.zoyi.channel.plugin.android.activity.chat.utils.KeyboardUtils;
import com.zoyi.channel.plugin.android.activity.download.DownloadActivity;
import com.zoyi.channel.plugin.android.activity.photo_picker.PhotoPickerActivity;
import com.zoyi.channel.plugin.android.databinding.ChPluginActivityChatBinding;
import com.zoyi.channel.plugin.android.enumerate.ActionType;
import com.zoyi.channel.plugin.android.enumerate.ButtonType;
import com.zoyi.channel.plugin.android.enumerate.ChatInteractionState;
import com.zoyi.channel.plugin.android.enumerate.LinkType;
import com.zoyi.channel.plugin.android.enumerate.LoadState;
import com.zoyi.channel.plugin.android.enumerate.Transition;
import com.zoyi.channel.plugin.android.enumerate.TranslationState;
import com.zoyi.channel.plugin.android.global.Action;
import com.zoyi.channel.plugin.android.model.etc.TranslationInfo;
import com.zoyi.channel.plugin.android.model.rest.File;
import com.zoyi.channel.plugin.android.model.rest.Marketing;
import com.zoyi.channel.plugin.android.model.rest.Message;
import com.zoyi.channel.plugin.android.model.rest.Reaction;
import com.zoyi.channel.plugin.android.model.rest.UserChat;
import com.zoyi.channel.plugin.android.open.option.Language;
import com.zoyi.channel.plugin.android.selector.TranslationSelector;
import com.zoyi.channel.plugin.android.store.ChatStore;
import com.zoyi.channel.plugin.android.store.ManagerStore;
import com.zoyi.channel.plugin.android.store.SettingsStore;
import com.zoyi.channel.plugin.android.store.TranslationStore;
import com.zoyi.channel.plugin.android.util.ClipboardUtils;
import com.zoyi.channel.plugin.android.util.Executor;
import com.zoyi.channel.plugin.android.util.Initializer;
import com.zoyi.channel.plugin.android.util.IntentUtils;
//import com.zoyi.channel.plugin.android.util.ResUtils;
import com.zoyi.channel.plugin.android.util.io.Keyboard;
import com.zoyi.channel.plugin.android.view.dialog.ChannelDialog;
import com.zoyi.channel.plugin.android.view.dialog.bottom_sheet.IconButtonBottomSheetDialog;
import com.zoyi.channel.plugin.android.view.handler.InfiniteScrollListener;
import com.zoyi.channel.plugin.android.viewbinding.JavaBaseActivity;
import com.zoyi.com.annimon.stream.Optional;
import com.zoyi.rx.Observable;
import com.zoyi.rx.Subscription;
import com.zoyi.rx.android.schedulers.AndroidSchedulers;
import com.zoyi.rx.subjects.PublishSubject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.channel.plugin.android.activity.PhotoAlbumActivity;
import io.channel.plugin.android.activity.PhotoAlbumStorage;
import io.channel.plugin.android.extension.ViewExtensionsKt;
import io.channel.plugin.android.feature.chat.ChatPresenter;
import io.channel.plugin.android.feature.chat.contract.ChatContract;
import io.channel.plugin.android.feature.chat.provider.keyboard.KeyboardHeightProvider;
import io.channel.plugin.android.feature.chat.view.ManagerProfileBottomSheet;
import io.channel.plugin.android.model.api.Manager;
import io.channel.plugin.android.model.entity.Form;
import io.channel.plugin.android.model.entity.PersonEntity;
import io.channel.plugin.android.model.entity.ProfileEntity;
import io.channel.plugin.android.util.ScreenUtilsKt;
import io.channel.plugin.android.view.form.group.FocusableFormGroup;
import kotlin.Unit;

public class ChatActivity extends JavaBaseActivity<ChPluginActivityChatBinding> implements ChatContract.View, ChatInteractionActionListener, OnAttachmentUploadContentActionListener, OnMessageActionListener, OnSendingActionListener, TypingListener, KeyboardHeightProvider.KeyboardListener {
    private ChatAdapter adapter;
    private LinearLayoutManager layoutManager;
    private ChatContract.Presenter presenter;
    @Nullable
    private Subscription toastSubscription;
    @Nullable
    private Subscription focusSubscription;
    private PublishSubject<Integer> toastPublishSubject = PublishSubject.create();
    private PublishSubject<Integer> bottomPaddingPublishSubject = PublishSubject.create();
    private PublishSubject<Integer> focusedViewPublishSubject = PublishSubject.create();
    @Nullable
    private KeyboardHeightProvider keyboardHeightProvider;
    private int topFocusMargin;
    private int bottomFocusMargin;

    public ChatActivity() {
    }

    @Initializer
    protected boolean onCreateActivity() {
        String chatId = (String) Optional.ofNullable(this.getIntent()).map((intent) -> {
            return intent.getStringExtra("chatId");
        }).orElse((String) null);
        String presetMessage = (String)Optional.ofNullable(this.getIntent()).map((intent) -> {
            return intent.getStringExtra("chatPresetMessage");
        }).orElse((String) null);
        this.initNavigation(true).activateBackpress().activateBackground().bindChannel().bindUserAlertCount().addButton(GlobalNavigation.Button.MORE, GlobalNavigation.ButtonState.HIDDEN).addButton(GlobalNavigation.Button.EXIT);
        ((ChPluginActivityChatBinding)this.binding).chLoaderChat.setOnErrorRefreshClickListener(() -> {
            this.presenter.fetchMessages();
            return Unit.INSTANCE;
        });
        ((ChPluginActivityChatBinding)this.binding).chViewNewMessageAlert.setListener(() -> {
            this.scrollToBottom();
            ((ChPluginActivityChatBinding)this.binding).chViewNewMessageAlert.hide();
        });
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.setChatInteractionActionListener(this);
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.setTypingListener(this);
        this.adapter = new ChatAdapter();
        this.adapter.setOnMessageActionListener(this);
        this.adapter.setOnSendingActionListener(this);
        this.adapter.setOnAttachmentUploadContentActionListener(this);
        this.presenter = new ChatPresenter(this, this.adapter, chatId, this.getString("page"));
        this.presenter.init();
        this.layoutManager = new LinearLayoutManager(this) {
            public boolean requestChildRectangleOnScreen(@NonNull RecyclerView parent, @NonNull android.view.View child, @NonNull Rect rect, boolean immediate, boolean focusedChildVisible) {
                return false;
            }
        };
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.setLayoutManager(this.layoutManager);
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.setAdapter(this.adapter);
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.setPreserveFocusAfterLayout(false);
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.setItemAnimator((RecyclerView.ItemAnimator)null);
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.addOnScrollListener(new InfiniteScrollListener() {
            public void scrollAttachedToBottom() {
                ((ChPluginActivityChatBinding) ChatActivity.this.binding).chViewNewMessageAlert.hide();
            }

            public void scrollAttachedToTop() {
                ChatActivity.this.presenter.fetchPrevMessages();
            }
        });
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.getViewTreeObserver().addOnGlobalFocusChangeListener((oldView, newView) -> {
            if (newView != null) {
                this.focusedViewPublishSubject.onNext(newView.hashCode());
            }

        });
        ((ChPluginActivityChatBinding)this.binding).chBottomLayoutChat.setRecyclerView(((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat);
        ((ChPluginActivityChatBinding)this.binding).chBottomLayoutChat.setStackFromEnd(true);
        ((ChPluginActivityChatBinding)this.binding).chBottomLayoutChat.setOnSizeChangeListener((height) -> {
            ViewGroup.LayoutParams params = ((ChPluginActivityChatBinding)this.binding).chViewNewMessageAlert.getLayoutParams();
            if (params instanceof ViewGroup.MarginLayoutParams) {
                ((ViewGroup.MarginLayoutParams)params).setMargins(0, 0, 0, height);
            }

        });
        this.keyboardHeightProvider = KeyboardHeightProvider.newInstance(this);
        this.keyboardHeightProvider.addKeyboardListener(this);
        this.toastSubscription = this.toastPublishSubject.throttleLast(250L, TimeUnit.MILLISECONDS).onBackpressureLatest().observeOn(AndroidSchedulers.mainThread()).subscribe((integer) -> {
            this.showFirstMessageDate(this.adapter.getItem(integer));
        });
        this.focusSubscription = Observable.combineLatest(this.bottomPaddingPublishSubject, this.focusedViewPublishSubject, Pair::new).distinctUntilChanged().observeOn(AndroidSchedulers.mainThread()).subscribe((pair) -> {
            this.scrollToFocusedForm();
        });
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy != 0 && ChatActivity.this.toastPublishSubject != null && recyclerView.getLayoutManager() != null) {
                    ChatActivity.this.toastPublishSubject.onNext(((LinearLayoutManager)recyclerView.getLayoutManager()).findFirstVisibleItemPosition());
                }

            }
        });
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.setText(presetMessage);
        this.topFocusMargin = PResUtils.getDimen(this, R.dimen.ch_chat_message_top_focus_margin);
        this.bottomFocusMargin = PResUtils.getDimen(this, R.dimen.ch_chat_message_bottom_focus_margin);
        ((ChPluginActivityChatBinding)this.binding).chChatRoot.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            this.bottomPaddingPublishSubject.onNext(((ChPluginActivityChatBinding)this.binding).chChatRoot.getPaddingBottom());
        });
        return true;
    }

    public void onSessionStateChange(boolean exist) {
        this.getNavigation().setButtonState(GlobalNavigation.Button.MORE, exist ? GlobalNavigation.ButtonState.VISIBLE : GlobalNavigation.ButtonState.HIDDEN);
    }

    @Nullable
    public ChatContract.Presenter getPresenter() {
        return this.presenter;
    }

    protected void onResume() {
        super.onResume();
        if (this.keyboardHeightProvider != null) {
            this.keyboardHeightProvider.onResume();
        }

    }

    protected void onPause() {
        super.onPause();
        KeyboardUtils.hideKeyboard(this);
        if (this.keyboardHeightProvider != null) {
            this.keyboardHeightProvider.onPause();
        }

        if (this.isFinishing()) {
            ChatStore.get().reset();
            TranslationStore.get().reset();
            Action.invoke(ActionType.CHAT_CLOSED);
        }

    }

    protected void onDestroy() {
        ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.setAdapter((RecyclerView.Adapter)null);
        if (this.toastSubscription != null && !this.toastSubscription.isUnsubscribed()) {
            this.toastSubscription.unsubscribe();
        }

        this.toastSubscription = null;
        if (this.focusSubscription != null && !this.focusSubscription.isUnsubscribed()) {
            this.focusSubscription.unsubscribe();
        }

        this.focusSubscription = null;
        if (this.keyboardHeightProvider != null) {
            this.keyboardHeightProvider.removeKeyboardListener(this);
        }

        super.onDestroy();
    }

    public void onStateChange(@NonNull LoadState state) {
        ((ChPluginActivityChatBinding)this.binding).chLoaderChat.setLoadState(state);
    }

    public void setNavigation(@Nullable Manager manager, boolean showMeta) {
        if (manager != null) {
            this.getNavigation().setAvatar(showMeta).setProfile(manager).setOperationTimeVisibility(showMeta);
        } else {
            this.getNavigation().setAvatar(showMeta).bindChannel().setOperationTimeVisibility(showMeta);
        }

    }

    public void onChatStateChange(UserChat userChat) {
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.initUserChat(userChat.getId());
    }

    public void onChatInteractionStateChange(ChatInteractionState inputType) {
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.setState(inputType);
    }

    public boolean isScrollOnBottom() {
        return !((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.canScrollVertically(1);
    }

    public void scrollToTop() {
        this.layoutManager.scrollToPosition(0);
    }

    public void scrollToBottom() {
        this.layoutManager.scrollToPosition(this.adapter.getItemCount() - 1);
    }

    public boolean isScrollable() {
        return ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.canScrollVertically(1) || ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.canScrollVertically(-1);
    }

    public void showNewMessageAlert(ProfileEntity profileEntity) {
        ((ChPluginActivityChatBinding)this.binding).chViewNewMessageAlert.show(profileEntity);
    }

    public void onAttachmentButtonClick() {
        IntentUtils.setNextActivity(this, PhotoPickerActivity.class).startActivityForResult(902);
    }

    public void onSendClick(String message) {
        if (this.presenter != null) {
            this.presenter.sendText(message);
        }

    }

    public void startNewChat(int resultCode, Transition transition) {
        this.finish(resultCode, transition);
    }

    public void startMarketingSupportBot() {
        if (this.presenter != null) {
            this.presenter.createMarketingSupportBotUserChat();
        }

    }

    public void onChatInputFocused() {
    }

    @Nullable
    public Dialog onMessageLongClick(Message message) {
        IconButtonBottomSheetDialog dialog = new IconButtonBottomSheetDialog(this);
        if (message != null && !message.isDeleted()) {
            TranslationInfo translationInfo = (TranslationInfo)TranslationStore.get().translation.get(TranslationInfo.createKey(message.getChatId(), message.getId(), ((Language) SettingsStore.get().language.get()).toString()));
            if (TranslationSelector.canTranslate(message, (Language)SettingsStore.get().language.get()) && (translationInfo == null || translationInfo.getState() == TranslationState.ORIGIN)) {
                dialog.addButton(R.drawable.ch_icon_translate, PResUtils.getString("ch.show_translate"), () -> {
                    this.handleTranslation(message);
                });
            }

            if (translationInfo != null && translationInfo.getState() == TranslationState.TRANSLATED) {
                dialog.addButton(R.drawable.ch_icon_arrow_hook_left_up, PResUtils.getString("ch.undo_translate"), () -> {
                    this.handleTranslation(message);
                });
            }

            if (!message.getPlainText().isEmpty()) {
                dialog.addButton(R.drawable.ch_copy, PResUtils.getString("ch.chat.message.actions.copy_message"), () -> {
                    this.copyText(message.getPlainText());
                });
            }

            if (message.getPersonType() != null && message.getPersonType().equals("user") && message.getChatId() != null) {
                dialog.addButton(R.drawable.ch_trash, PResUtils.getString("ch.chat.message.actions.delete_message"), R.color.ch_red400, R.color.ch_red400, () -> {
                    this.deleteMessage(message.getId());
                });
            }

            if (dialog.getButtons().size() > 0) {
                return dialog;
            }
        }

        return null;
    }

    public void onMessageClick(MessageItem item) {
        if (item instanceof ChatMessageItem) {
            ChatMessageItem chatMessageItem = (ChatMessageItem)item;
            String messageId = chatMessageItem.getMessage().getId();
            boolean currentVisibility = this.adapter.isMessageTimeVisible(messageId);
            this.adapter.setMessageTimeVisibility(item, !currentVisibility);
        }
    }

    public void onAvatarClick(PersonEntity person) {
        if ("manager".equals(person.getPersonType())) {
            Manager manager = (Manager) ManagerStore.get().managers.get(person.getPersonId());
            if (manager != null && !manager.isDisplayAsChannel()) {
                (new ManagerProfileBottomSheet(this, manager)).show();
            }
        }

    }

    public void onReactionsLongClicked(List<Reaction> reactions) {
        (new ReactionsDialog(this, reactions)).show();
    }

    public void onMarketingAction(Marketing marketing, @Nullable String url) {
        MarketingAction.sendClickEvent(marketing, url);
    }

    public void onOpenVideoClick(File attachment, long startAt) {
        Executor.startFullScreenVideo(this, attachment, startAt);
    }

    public void onAttachmentClick(File attachment, Message message) {
        if (attachment.isImage()) {
            IntentUtils.setNextActivity(this, PhotoAlbumActivity.class).putExtra("attachementId", attachment.getId()).putExtra("storageId", PhotoAlbumStorage.save(message.getFiles())).startActivity();
        } else {
            IntentUtils.setNextActivity(this, DownloadActivity.class).putExtra("url", attachment.getUrl()).putExtra("filename", attachment.getName()).putExtra("EXTRA_TYPE", attachment.getType()).setTransition(Transition.NONE).startActivity();
        }

    }

    public void onActionButtonClick(@Nullable ChatMessageItem item, int index) {
        if (this.presenter != null && item != null) {
            this.presenter.onActionButtonClick(item, index);
        }

    }

    public void onSubmitForm(ChatMessageItem item, Form form, @Nullable Map<Integer, Object> values) {
        this.presenter.submitForm(item, form, values);
        Keyboard.close(this, ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat);
    }

    public void onRetryForm(String messageId) {
        if (!this.presenter.isTyping()) {
            this.adapter.setShouldFocusFormMessageId(messageId);
        }

    }

    public void onResendButtonClick(SendItem sendItem) {
        AlertDialog dialog = (new AlertDialog.Builder(this)).setMessage(PResUtils.getString("ch.chat.resend.description")).setPositiveButton(PResUtils.getString("ch.chat.retry_sending_message"), (dialog12, which) -> {
            if (this.presenter != null) {
                this.presenter.resend(sendItem);
            }

        }).setNegativeButton(PResUtils.getString("ch.chat.resend.cancel"), (DialogInterface.OnClickListener)null).setNeutralButton(PResUtils.getString("ch.chat.delete"), (dialog1, which) -> {
            if (this.presenter != null) {
                this.presenter.removeFailedItem(sendItem);
            }

        }).setCancelable(true).create();
        dialog.setOnShowListener((args) -> {
            int dark = ContextCompat.getColor(this, R.color.ch_grey900);
            int cobalt = ContextCompat.getColor(this, R.color.ch_cobalt400);
            dialog.getButton(-1).setTextColor(cobalt);
            dialog.getButton(-2).setTextColor(dark);
            dialog.getButton(-3).setTextColor(dark);
        });
        dialog.show();
    }

    public void onCancelClick(@Nullable SendFileItem sendFileItem) {
        if (sendFileItem != null) {
            this.presenter.cancelSendingFile(sendFileItem);
        }

    }

    public void onUrlClick(String url) {
        Executor.executeLinkAction(this, url, LinkType.URL);
    }

    public void onTypingStateChange(boolean isTyping) {
        if (this.presenter != null) {
            this.presenter.setTyping(isTyping);
        }

    }

    public void handleTranslation(Message message) {
        String chatId = message.getChatId();
        String messageId = message.getId();
        String language = ((Language)SettingsStore.get().language.get()).toString();
        String translationKey = TranslationInfo.createKey(chatId, messageId, language);
        TranslationInfo translationInfo = (TranslationInfo)TranslationStore.get().translation.get(translationKey);
        if (translationInfo != null) {
            switch(translationInfo.getState()) {
                case ORIGIN:
                    translationInfo.setState(TranslationState.TRANSLATED);
                    break;
                case TRANSLATED:
                    translationInfo.setState(TranslationState.ORIGIN);
            }

            TranslationStore.get().translation.upsert(translationInfo);
        } else {
            TranslateAction.translate(chatId, messageId, language);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
            case 902:
                if (resultCode == 12) {
                    this.presenter.uploadFiles(data.getParcelableArrayListExtra("PHOTO_INTENT_KEY"));
                }
            default:
        }
    }

    public void onButtonClick(@Nullable GlobalNavigation.Button button) {
        if (button == GlobalNavigation.Button.MORE) {
            (new IconButtonBottomSheetDialog(this)).addButton(R.drawable.ch_icon_out, PResUtils.getString("ch.chat.menu.leave_chat"), R.color.ch_bgtxt_red_normal, R.color.ch_bgtxt_red_normal, this::showLeaveChatDialog).show();
        } else {
            super.onButtonClick(button);
        }

    }

    private void setChatInputDim(boolean dim) {
        ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.setInputDim(dim);
    }

    private void showLeaveChatDialog() {
        (new ChannelDialog(this)).setTitle(PResUtils.getString("ch.chat.menu.leave_chat")).setDescription(PResUtils.getString("ch.chat.menu.leave_chat.content")).addButton(ButtonType.CANCEL).addButton(PResUtils.getString("ch.chat.menu.leave"), PResUtils.getColor(R.color.ch_bgtxt_red_normal), PResUtils.getColor(R.color.ch_bgtxt_absolute_white_dark), (v) -> {
            if (this.presenter != null) {
                this.presenter.leaveChat();
            }

        }).show();
    }

    private void copyText(String text) {
        if (ClipboardUtils.copyToClipBoard(text)) {
            Toast.makeText(this, PResUtils.getString("ch.copy_message.success"), Toast.LENGTH_SHORT).show();
        }

    }

    private void showFirstMessageDate(@Nullable MessageItem item) {
        if (item != null) {
            switch(item.getType()) {
                case HOST:
                case USER:
                    ChatMessageItem chatMessageItem = (ChatMessageItem)item;
                    if (chatMessageItem.getCreatedAt() != 0L) {
                        ((ChPluginActivityChatBinding)this.binding).chViewChatDateToast.show(chatMessageItem.getCreatedAt());
                    }
            }
        }

    }

    private void deleteMessage(String messageId) {
        ((ChannelDialog)((ChannelDialog)((ChannelDialog)(new ChannelDialog(this)).setTitle(PResUtils.getString("ch.chat.message.delete_confirm.title"))).setDescription(PResUtils.getString("ch.chat.message.delete_confirm.message")).addButton(ButtonType.CANCEL)).addButton(PResUtils.getString("ch.chat.delete"), PResUtils.getColor(R.color.ch_bgtxt_red_normal), PResUtils.getColor(R.color.ch_bgtxt_absolute_white_dark), (v) -> {
            if (this.presenter != null) {
                this.presenter.deleteMessage(messageId);
            }

        })).show();
    }

    public void onKeyboardHeightChanged(int height) {
        boolean isOnBottom = this.isScrollOnBottom();
        ((ChPluginActivityChatBinding)this.binding).chChatRoot.setPadding(0, 0, 0, height);
        if (isOnBottom && ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.getState() == ChatInteractionState.NORMAL && ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.hasFocus()) {
            this.scrollToBottom();
        }

    }

    private void scrollToFocusedForm() {
        android.view.View focusedView = this.getCurrentFocus();
        android.view.View focusedViewHolder = ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.getFocusedChild();
        android.view.View focusedFormGroup = ViewExtensionsKt.findParentView(focusedView, FocusableFormGroup.class);
        int focusedPosition = ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.getChildAdapterPosition(focusedViewHolder);
        if (focusedFormGroup != null && focusedPosition != -1) {
            if (ScreenUtilsKt.getBottomOffset(focusedFormGroup, ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat) < this.bottomFocusMargin + ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.getHeight()) {
                this.layoutManager.scrollToPositionWithOffset(focusedPosition, ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat.getHeight() - this.getNavigation().getHeight() - focusedViewHolder.getHeight() + ScreenUtilsKt.getBottomOffset(focusedFormGroup, focusedViewHolder) - ((ChPluginActivityChatBinding)this.binding).chViewChatInteraction.getHeight() - this.bottomFocusMargin);
            } else if (ScreenUtilsKt.getTopOffset(focusedFormGroup, ((ChPluginActivityChatBinding)this.binding).chRecyclerViewChat) < this.topFocusMargin + this.getNavigation().getHeight()) {
                this.layoutManager.scrollToPositionWithOffset(focusedPosition, this.topFocusMargin - ScreenUtilsKt.getTopOffset(focusedFormGroup, focusedViewHolder));
            }

        }
    }
}
