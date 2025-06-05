package com.freewheelin.pulley.revision2021.channelio.channel.view.custom

//import com.zoyi.channel.plugin.android.databinding.ChPluginActivityChatBinding
import android.app.Dialog
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.AnimRes
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ItemAnimator
import com.freewheelin.pulley.R
import com.zoyi.channel.plugin.android.action.MarketingAction
import com.zoyi.channel.plugin.android.action.TranslateAction
import com.zoyi.channel.plugin.android.activity.chat.ChatAdapter
import com.zoyi.channel.plugin.android.activity.chat.dialog.ReactionsDialog
import com.zoyi.channel.plugin.android.activity.chat.listener.TypingListener
import com.zoyi.channel.plugin.android.activity.chat.listener.view.ChatInteractionActionListener
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnAttachmentUploadContentActionListener
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnMessageActionListener
import com.zoyi.channel.plugin.android.activity.chat.listener.viewholder.OnSendingActionListener
import com.zoyi.channel.plugin.android.activity.chat.model.ChatMessageItem
import com.zoyi.channel.plugin.android.activity.chat.model.MessageItem
import com.zoyi.channel.plugin.android.activity.chat.model.SendFileItem
import com.zoyi.channel.plugin.android.activity.chat.model.SendItem
import com.zoyi.channel.plugin.android.activity.chat.type.MessageType
import com.zoyi.channel.plugin.android.activity.chat.utils.KeyboardUtils
import com.zoyi.channel.plugin.android.activity.download.DownloadActivity
import com.zoyi.channel.plugin.android.activity.photo_picker.PhotoPickerActivity
import com.zoyi.channel.plugin.android.enumerate.*
import com.zoyi.channel.plugin.android.global.Action
import com.zoyi.channel.plugin.android.model.etc.TranslationInfo
import com.zoyi.channel.plugin.android.model.rest.*
import com.zoyi.channel.plugin.android.open.option.Language
import com.zoyi.channel.plugin.android.selector.PageSelector
import com.zoyi.channel.plugin.android.selector.TranslationSelector
import com.zoyi.channel.plugin.android.store.ChatStore
import com.zoyi.channel.plugin.android.store.ManagerStore
import com.zoyi.channel.plugin.android.store.SettingsStore
import com.zoyi.channel.plugin.android.store.TranslationStore
import com.zoyi.channel.plugin.android.util.ClipboardUtils
import com.zoyi.channel.plugin.android.util.Executor
import com.zoyi.channel.plugin.android.util.IntentUtils
import com.zoyi.channel.plugin.android.util.io.Keyboard
import com.zoyi.channel.plugin.android.view.dialog.ChannelDialog
import com.zoyi.channel.plugin.android.view.dialog.bottom_sheet.IconButtonBottomSheetDialog
import com.zoyi.channel.plugin.android.view.handler.InfiniteScrollListener
import com.zoyi.rx.Subscription
import com.zoyi.rx.android.schedulers.AndroidSchedulers
import com.zoyi.rx.subjects.PublishSubject
import io.channel.plugin.android.activity.PhotoAlbumActivity
import io.channel.plugin.android.activity.PhotoAlbumStorage.save
import io.channel.plugin.android.feature.chat.ChatPresenter
import io.channel.plugin.android.feature.chat.contract.ChatContract
import io.channel.plugin.android.feature.chat.provider.keyboard.KeyboardHeightProvider
import io.channel.plugin.android.feature.chat.view.ManagerProfileBottomSheet
import io.channel.plugin.android.model.api.Manager
import io.channel.plugin.android.model.entity.Form
import io.channel.plugin.android.model.entity.PersonEntity
import io.channel.plugin.android.model.entity.ProfileEntity
import java.util.concurrent.TimeUnit
import com.freewheelin.pulley.databinding.*
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.channelio.channel.PResUtils

class ChatFragment: Fragment(), ChatContract.View, ChatInteractionActionListener,
    OnAttachmentUploadContentActionListener, OnMessageActionListener, OnSendingActionListener,
    TypingListener, KeyboardHeightProvider.KeyboardListener {

    companion object {
        fun newInstance(chatId: String?, chatPresetMessage: String?) : ChatFragment {
            return ChatFragment().apply {
                val page = PageSelector.getPage()
                arguments = Bundle().apply {
                    putString("chatId", chatId)
                    putString("chatPresetMessage", chatPresetMessage)
                    putString("page", page)
//                    putBoolean("handleOpenChat", false)
//                    putString("page", PageSelector.getPage())
                }
            }
        }
    }

    val binding: ChPluginActivityChatBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.ch_plugin_activity_chat, null, false)
    }

    private var adapter: ChatAdapter? = null
    private var layoutManager: LinearLayoutManager? = null
    private var presenter: ChatContract.Presenter? = null
    private var toastSubscription: Subscription? = null
    private var focusSubscription: Subscription? = null
    private var toastPublishSubject = PublishSubject.create<Int>()
    private var bottomPaddingPublishSubject = PublishSubject.create<Int>()
    private var focusedViewPublishSubject = PublishSubject.create<Int>()
    private var keyboardHeightProvider: KeyboardHeightProvider? = null
    private var topFocusMargin = 0
    private var bottomFocusMargin = 0
    override val isScrollOnBottom: Boolean
        get() {
            return !binding.chRecyclerViewChat.canScrollVertically(1)
        }
    override val isScrollable: Boolean
        get() {
            return binding.chRecyclerViewChat.canScrollVertically(1) || binding.chRecyclerViewChat.canScrollVertically(
                -1
            )
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // Chat Activity의 onCreateActivity
        var presetMessage = ""
        var chatId: String? = null
        var page = ""
        arguments?.apply {
            presetMessage = getString("chatPresetMessage") ?: ""
            chatId = getString("chatId")
            page = getString("page") ?: ""
        }
//        val presetMessage = Optional.ofNullable<Intent>(this.getIntent()).map { intent: Intent ->
//            intent.getStringExtra(
//                "chatPresetMessage"
//            )
//        }.orElse(null as String?) as String

//        this.initNavigation(true).activateBackpress().activateBackground().bindChannel()
//            .bindUserAlertCount()
//            .addButton(GlobalNavigation.Button.MORE, GlobalNavigation.ButtonState.HIDDEN)
//            .addButton(GlobalNavigation.Button.EXIT)

        binding.chLoaderChat.onErrorRefreshClickListener = {
            presenter!!.fetchMessages()
            Unit
        }
        (binding as ChPluginActivityChatBinding).chViewNewMessageAlert.setListener {
            scrollToBottom()
            (binding as ChPluginActivityChatBinding).chViewNewMessageAlert.hide()
        }
        (binding as ChPluginActivityChatBinding).chViewChatInteraction.setChatInteractionActionListener(
            this
        )
        (binding as ChPluginActivityChatBinding).chViewChatInteraction.setTypingListener(this)
        adapter = ChatAdapter().apply {
            setOnMessageActionListener(this@ChatFragment)
            setOnSendingActionListener(this@ChatFragment)
            setOnAttachmentUploadContentActionListener(this@ChatFragment)
        }

        presenter = ChatPresenter(this, adapter!!, chatId, page)
        (activity as LearningCourseActivity).presenter = presenter

        (presenter as ChatPresenter).init()
        layoutManager = object : LinearLayoutManager(requireContext()) {
            override fun requestChildRectangleOnScreen(
                parent: RecyclerView,
                child: View,
                rect: Rect,
                immediate: Boolean,
                focusedChildVisible: Boolean
            ): Boolean {
                return false
            }
        }
        binding.chRecyclerViewChat.layoutManager = layoutManager
        binding.chRecyclerViewChat.adapter = adapter
        binding.chRecyclerViewChat.preserveFocusAfterLayout =
            false
        binding.chRecyclerViewChat.itemAnimator =
            null as ItemAnimator?
        binding.chRecyclerViewChat.addOnScrollListener(object :
            InfiniteScrollListener() {
            override fun scrollAttachedToBottom() {
                this@ChatFragment.binding.chViewNewMessageAlert.hide()
            }

            override fun scrollAttachedToTop() {
                (this@ChatFragment.presenter as ChatPresenter).fetchPrevMessages()
            }
        })
        binding.chRecyclerViewChat.viewTreeObserver.addOnGlobalFocusChangeListener { oldView: View?, newView: View? ->
            if (newView != null) {
                focusedViewPublishSubject.onNext(newView.hashCode())
            }
        }
        binding.chBottomLayoutChat.setRecyclerView(binding.chRecyclerViewChat)
        binding.chBottomLayoutChat.setStackFromEnd(true)
        binding.chBottomLayoutChat.setOnSizeChangeListener { height: Int ->
            val params = binding.chViewNewMessageAlert.layoutParams
            if (params is ViewGroup.MarginLayoutParams) {
                params.setMargins(0, 0, 0, height)
            }
        }
        keyboardHeightProvider = KeyboardHeightProvider.newInstance(requireActivity())
        keyboardHeightProvider!!.addKeyboardListener(this)
        toastSubscription =
            toastPublishSubject.throttleLast(250L, TimeUnit.MILLISECONDS).onBackpressureLatest()
                .observeOn(
                    AndroidSchedulers.mainThread()
                ).subscribe ({ integer: Int? ->
                    this.showFirstMessageDate(adapter!!.getItem(integer!!))
                }, { /* error */ })
//        focusSubscription = Observable.combineLatest(
//            bottomPaddingPublishSubject, focusedViewPublishSubject
//        ) { first: Int?, second: Int? ->
//            Pair(first, second)
//        }.distinctUntilChanged().observeOn(AndroidSchedulers.mainThread())
//            .subscribe { pair: android.util.Pair<Int, Int>? -> this.scrollToFocusedForm() }

        binding.chRecyclerViewChat.addOnScrollListener(object :
            RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (dy != 0 && this@ChatFragment.toastPublishSubject != null && recyclerView.layoutManager != null) {
                    this@ChatFragment.toastPublishSubject.onNext((recyclerView.layoutManager as LinearLayoutManager?)!!.findFirstVisibleItemPosition())
                }
            }
        })
        binding.chViewChatInteraction.setText(presetMessage)
        topFocusMargin = PResUtils.getDimen(requireContext(), R.dimen.ch_chat_message_top_focus_margin)
        bottomFocusMargin = PResUtils.getDimen(requireContext(), R.dimen.ch_chat_message_bottom_focus_margin)
        binding.chChatRoot.viewTreeObserver.addOnGlobalLayoutListener {
            bottomPaddingPublishSubject.onNext(
                binding.chChatRoot.paddingBottom
            )
        }
        binding.backBtn.setOnClickListener {
            (activity as LearningCourseActivity).beginLoungeFragment()
        }
        binding.exitBtn.setOnClickListener {
            (activity as LearningCourseActivity).beginBlackChannelIoFrame()
        }
    }

    private fun showFirstMessageDate(item: MessageItem?) {
        if (item != null) {
            when (item.type) {
                MessageType.HOST, MessageType.USER -> {
                    val chatMessageItem = item as ChatMessageItem
                    if (chatMessageItem.createdAt != 0L) {
                        binding.chViewChatDateToast.show(chatMessageItem.createdAt)
                    }
                }
                else -> {}
            }
        }
    }

    private fun scrollToFocusedForm() {
//        val focusedView: View = this.getCurrentFocus()
//        val focusedViewHolder = binding.chRecyclerViewChat.focusedChild
//        val focusedFormGroup = focusedView.findParentView(FocusableFormGroup::class.java)
//        val focusedPosition = binding.chRecyclerViewChat.getChildAdapterPosition(focusedViewHolder)
//        if (focusedFormGroup != null && focusedPosition != -1) {
//            if (focusedFormGroup.getBottomOffset(binding.chRecyclerViewChat) < bottomFocusMargin + binding.chViewChatInteraction.height) {
//                layoutManager!!.scrollToPositionWithOffset(
//                    focusedPosition,
//                    binding.chRecyclerViewChat.height - this.getNavigation()
//                        .getHeight() - focusedViewHolder.height + focusedFormGroup.getBottomOffset(
//                        focusedViewHolder
//                    ) - binding.chViewChatInteraction.height - bottomFocusMargin
//                )
//            } else if (focusedFormGroup.getTopOffset(binding.chRecyclerViewChat) < topFocusMargin + this.getNavigation()
//                    .getHeight()
//            ) {
//                layoutManager!!.scrollToPositionWithOffset(
//                    focusedPosition,
//                    topFocusMargin - focusedFormGroup.getTopOffset(focusedViewHolder)
//                )
//            }
//        }
    }

    fun getPresenter(): ChatContract.Presenter? {
        return presenter
    }
    override fun onResume() {
        super.onResume()
        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.onResume()
        }
    }
    override fun onPause() {
        super.onPause()
        KeyboardUtils.hideKeyboard(requireActivity())
        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.onPause()
        }
        if (this.activity?.isFinishing == true) {
            ChatStore.get().reset()
            TranslationStore.get().reset()
            Action.invoke(ActionType.CHAT_CLOSED)
        }
    }

    override fun onDestroy() {
        binding.chRecyclerViewChat.adapter = null as RecyclerView.Adapter<*>?
        if (toastSubscription != null && !toastSubscription!!.isUnsubscribed) {
            toastSubscription!!.unsubscribe()
        }
        toastSubscription = null
        if (focusSubscription != null && !focusSubscription!!.isUnsubscribed) {
            focusSubscription!!.unsubscribe()
        }
        focusSubscription = null
        if (keyboardHeightProvider != null) {
            keyboardHeightProvider!!.removeKeyboardListener(this)
        }
        super.onDestroy()
    }
    var outTransition: Transition? = null

    @AnimRes
    open fun getEnterAnimOfFinish(): Int {
        return R.anim.ch_plugin_idle
    }
    @AnimRes
    open fun getExistAnimOfFinish(): Int {
        return when (outTransition) {
            Transition.NONE -> R.anim.ch_plugin_idle
            Transition.SLIDE_FROM_BOTTOM -> R.anim.ch_plugin_slide_out_bottom
            else -> R.anim.ch_plugin_slide_out_right
        }
    }
    override fun finish() {
        onDestroy()
        activity?.overridePendingTransition(getEnterAnimOfFinish(), getExistAnimOfFinish())
    }

    override fun finish(transition: Transition?) {
        outTransition = transition
        this.finish()
    }

    override fun finish(resultCode: Int) {
        activity?.setResult(resultCode)
        this.finish()
    }

    override fun finish(resultCode: Int, transition: Transition?) {
        activity?.setResult(resultCode)
        outTransition = transition
        this.finish()
    }

    override fun hideProgress() {
//        if (dialog != null && dialog!!.isShowing) {
//            dialog!!.dismiss()
//            dialog = null
//        }
    }

    override fun onChatInteractionStateChange(inputType: ChatInteractionState) {
        binding.chViewChatInteraction.state = inputType
    }

    override fun onChatStateChange(userChat: UserChat) {
        binding.chViewChatInteraction.initUserChat(userChat.id)
    }

    override fun onSessionStateChange(exist: Boolean) {
//        this.getNavigation().setButtonState(
//            GlobalNavigation.Button.MORE,
//            if (exist) GlobalNavigation.ButtonState.VISIBLE else GlobalNavigation.ButtonState.HIDDEN
//        )

    }

    override fun onStateChange(state: LoadState) {
        binding.chLoaderChat.loadState = state
    }

    override fun scrollToBottom() {
        layoutManager!!.scrollToPosition(adapter!!.itemCount - 1)
    }

    override fun scrollToTop() {
        layoutManager!!.scrollToPosition(0)
    }

    override fun setNavigation(manager: Manager?, showMeta: Boolean) {
//        if (manager != null) {
//            this.getNavigation().setAvatar(showMeta).setProfile(manager)
//                .setOperationTimeVisibility(showMeta)
//        } else {
//            this.getNavigation().setAvatar(showMeta).bindChannel()
//                .setOperationTimeVisibility(showMeta)
//        }
    }

    override fun showNewMessageAlert(profileEntity: ProfileEntity) {
        binding.chViewNewMessageAlert.show(profileEntity)
    }

    override fun showProgress() {

    }

    override fun showProgress(message: String?) {

    }

    override fun onAttachmentButtonClick() {
        IntentUtils.setNextActivity(requireContext(), PhotoPickerActivity::class.java)
            .startActivityForResult(902)
    }

    override fun onSendClick(message: String?) {
        if (presenter != null) {
            presenter!!.sendText(message!!)
        }
    }

    override fun startNewChat(resultCode: Int, transition: Transition?) {
        this.finish(resultCode, transition)
    }

    override fun startMarketingSupportBot() {
        if (presenter != null) {
            presenter!!.createMarketingSupportBotUserChat()
        }
    }

    override fun onChatInputFocused() {
    }

    override fun onActionButtonClick(item: ChatMessageItem?, index: Int) {
        if (presenter != null && item != null) {
            presenter!!.onActionButtonClick(item, index)
        }
    }

    override fun onUrlClick(url: String?) {
        Executor.executeLinkAction(requireContext(), url, LinkType.URL)
    }

    override fun onOpenVideoClick(attachment: File?, startAt: Long) {
        Executor.startFullScreenVideo(requireContext(), attachment, startAt)
    }

    override fun onAttachmentClick(attachment: File?, message: Message?) {
        if (attachment!!.isImage) {
            IntentUtils.setNextActivity(requireContext(), PhotoAlbumActivity::class.java)
                .putExtra("attachementId", attachment.id).putExtra(
                    "storageId", save(
                        message!!.files!!
                    )
                ).startActivity()
        } else {
            IntentUtils.setNextActivity(requireContext(), DownloadActivity::class.java)
                .putExtra("url", attachment.url).putExtra("filename", attachment.name)
                .putExtra("EXTRA_TYPE", attachment.type).setTransition(
                    Transition.NONE
                ).startActivity()
        }
    }

    override fun onMarketingAction(marketing: Marketing?, url: String?) {
        MarketingAction.sendClickEvent(marketing, url)
    }

    override fun onMessageLongClick(message: Message?): Dialog? {
        val dialog = IconButtonBottomSheetDialog(requireContext())
        if (message != null && !message.isDeleted) {
            val translationInfo = TranslationStore.get().translation[TranslationInfo.createKey(
                message.chatId,
                message.id,
                (SettingsStore.get().language.get() as Language).toString()
            )]
            if (TranslationSelector.canTranslate(
                    message,
                    SettingsStore.get().language.get() as Language
                ) && (translationInfo == null || translationInfo.state == TranslationState.ORIGIN)
            ) {
                dialog.addButton(
                    R.drawable.ch_icon_translate, PResUtils.getString("ch.show_translate")
                ) { handleTranslation(message) }
            }
            if (translationInfo != null && translationInfo.state == TranslationState.TRANSLATED) {
                dialog.addButton(
                    R.drawable.ch_icon_arrow_hook_left_up, PResUtils.getString("ch.undo_translate")
                ) { handleTranslation(message) }
            }
            if (!message.plainText.isEmpty()) {
                dialog.addButton(
                    R.drawable.ch_copy, PResUtils.getString("ch.chat.message.actions.copy_message")
                ) { this.copyText(message.plainText) }
            }
            if (message.personType != null && message.personType == "user" && message.chatId != null) {
                dialog.addButton(
                    R.drawable.ch_trash,
                    PResUtils.getString("ch.chat.message.actions.delete_message"),
                    R.color.ch_red400,
                    R.color.ch_red400
                ) { this.deleteMessage(message.id) }
            }
            if (dialog.buttons.size > 0) {
                return dialog
            }
        }

        return null
    }
    private fun copyText(text: String) {
        if (ClipboardUtils.copyToClipBoard(text)) {
            Toast.makeText(requireContext(), PResUtils.getString("ch.copy_message.success"), Toast.LENGTH_SHORT)
                .show()
        }
    }
    private fun deleteMessage(messageId: String) {
        (((ChannelDialog(requireContext()).setTitle(PResUtils.getString("ch.chat.message.delete_confirm.title")) as ChannelDialog).setDescription(
            PResUtils.getString("ch.chat.message.delete_confirm.message")
        ).addButton(ButtonType.CANCEL) as ChannelDialog).addButton(
            PResUtils.getString("ch.chat.delete"),
            PResUtils.getColor(R.color.ch_bgtxt_red_normal),
            PResUtils.getColor(R.color.ch_bgtxt_absolute_white_dark)
        ) { v: View? ->
            if (presenter != null) {
                presenter!!.deleteMessage(messageId)
            }
        } as ChannelDialog).show()
    }
    override fun onMessageClick(item: MessageItem?) {
        if (item is ChatMessageItem) {
            val messageId: String = item.message.id
            val currentVisibility = adapter!!.isMessageTimeVisible(messageId)
            adapter!!.setMessageTimeVisibility(item, !currentVisibility)
        }
    }

    override fun onReactionsLongClicked(reactions: MutableList<Reaction>?) {
        ReactionsDialog(requireContext(), reactions).show()
    }

    override fun onAvatarClick(person: PersonEntity?) {
        if ("manager" == person!!.personType) {
            val manager = ManagerStore.get().managers[person!!.personId]
            if (manager != null && !manager.isDisplayAsChannel) {
                ManagerProfileBottomSheet(requireContext(), manager).show()
            }
        }
    }

    override fun handleTranslation(message: Message?) {
        val chatId = message!!.chatId
        val messageId: String = message.id
        val language = SettingsStore.get().language.get().toString()
        val translationKey = TranslationInfo.createKey(chatId, messageId, language)
        val translationInfo = TranslationStore.get().translation[translationKey]
        if (translationInfo != null) {
            when (translationInfo.state) {
                TranslationState.ORIGIN -> translationInfo.state = TranslationState.TRANSLATED
                TranslationState.TRANSLATED -> translationInfo.state = TranslationState.ORIGIN
                else -> {}
            }
            TranslationStore.get().translation.upsert(translationInfo)
        } else {
            TranslateAction.translate(chatId, messageId, language)
        }

    }

    override fun onSubmitForm(item: ChatMessageItem?, form: Form?, values: MutableMap<Int, Any>?) {
        presenter!!.submitForm(item!!, form!!, values)
        Keyboard.close(requireContext(), binding.chRecyclerViewChat)
    }

    override fun onRetryForm(messageId: String?) {
        if (!presenter!!.isTyping) {
            adapter!!.setShouldFocusFormMessageId(messageId)
        }
    }

    override fun onResendButtonClick(sendItem: SendItem?) {
        val dialog =
            AlertDialog.Builder(requireContext()).setMessage(PResUtils.getString("ch.chat.resend.description"))
                .setPositiveButton(
                    PResUtils.getString("ch.chat.retry_sending_message")
                ) { dialog12: DialogInterface?, which: Int ->
                    if (presenter != null) {
                        presenter!!.resend(sendItem!!)
                    }
                }.setNegativeButton(
                    PResUtils.getString("ch.chat.resend.cancel"),
                    null as DialogInterface.OnClickListener?
                ).setNeutralButton(
                    PResUtils.getString("ch.chat.delete")
                ) { dialog1: DialogInterface?, which: Int ->
                    if (presenter != null) {
                        presenter!!.removeFailedItem(sendItem!!)
                    }
                }.setCancelable(true).create()
        dialog.setOnShowListener { args: DialogInterface? ->
            val dark = ContextCompat.getColor(requireContext(), R.color.ch_grey900)
            val cobalt = ContextCompat.getColor(requireContext(), R.color.ch_cobalt400)
            dialog.getButton(-1).setTextColor(cobalt)
            dialog.getButton(-2).setTextColor(dark)
            dialog.getButton(-3).setTextColor(dark)
        }
        dialog.show()
    }

    override fun onCancelClick(sendFileItem: SendFileItem?) {
        if (sendFileItem != null) {
            presenter!!.cancelSendingFile(sendFileItem)
        }
    }

    override fun onTypingStateChange(isTyping: Boolean) {
        if (presenter != null) {
            presenter!!.setTyping(isTyping)
        }
    }

    override fun onKeyboardHeightChanged(height: Int) {
        val isOnBottom: Boolean = this.isScrollOnBottom
        binding.chChatRoot.setPadding(0, 0, 0, height)
        if (isOnBottom && binding.chViewChatInteraction.state == ChatInteractionState.NORMAL && binding.chViewChatInteraction.hasFocus()) {
            scrollToBottom()
        }
    }
}