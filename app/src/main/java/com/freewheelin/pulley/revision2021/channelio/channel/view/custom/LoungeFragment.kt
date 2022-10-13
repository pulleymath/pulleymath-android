package com.freewheelin.pulley.revision2021.channelio.channel.view.custom

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.widget.NestedScrollView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.revision2021.activity.LearningCourseActivity
import com.freewheelin.pulley.revision2021.channelio.channel.PResUtils
import com.freewheelin.pulley.views.DaebakToast
import com.zoyi.channel.plugin.android.activity.base.ActivityFrameView
import com.zoyi.channel.plugin.android.activity.base.navigation.GlobalNavigation
import com.zoyi.channel.plugin.android.activity.chat.utils.ChatUtils
import com.zoyi.channel.plugin.android.activity.chats.ChatsActivity
import com.zoyi.channel.plugin.android.activity.chats.enumerate.ChatsReadState
import com.zoyi.channel.plugin.android.activity.common.chat.ChatContentType
import com.zoyi.channel.plugin.android.activity.common.userchat.listener.OnChatClickListener
import com.zoyi.channel.plugin.android.activity.common.userchat.model.ChatItem
import com.zoyi.channel.plugin.android.activity.lounge.LoungePresenter
import com.zoyi.channel.plugin.android.activity.lounge.contract.LoungeContract
import com.zoyi.channel.plugin.android.activity.lounge.enumerate.PreviewState
import com.zoyi.channel.plugin.android.activity.lounge.view.app_messenger.LoungeAppButtonView
import com.zoyi.channel.plugin.android.activity.lounge.view.app_messenger.OnIntegrationClickListener
import com.zoyi.channel.plugin.android.activity.lounge.view.lounge_media.LoungeMediaErrorCardView
import com.zoyi.channel.plugin.android.bind.Binder
import com.zoyi.channel.plugin.android.bind.SubscriptionBinder
import com.zoyi.channel.plugin.android.component.bezier.BezierButton
import com.zoyi.channel.plugin.android.enumerate.*
import com.zoyi.channel.plugin.android.extension.Views
import com.zoyi.channel.plugin.android.global.Action
import com.zoyi.channel.plugin.android.model.entity.Contact
import com.zoyi.channel.plugin.android.model.entity.LoungeMedia
import com.zoyi.channel.plugin.android.model.rest.media.instagram.Instagram
import com.zoyi.channel.plugin.android.open.option.Language
import com.zoyi.channel.plugin.android.selector.AppMessengerSelector
import com.zoyi.channel.plugin.android.selector.ChannelSelector
import com.zoyi.channel.plugin.android.selector.PageSelector
import com.zoyi.channel.plugin.android.store.PopupStore
import com.zoyi.channel.plugin.android.util.*
import com.zoyi.channel.plugin.android.view.dialog.ChannelDialog
import com.zoyi.channel.plugin.android.view.integrations.instagram.InstagramView
import com.zoyi.channel.plugin.android.view.layout.ChBorderLayout
import com.zoyi.com.annimon.stream.Stream
import com.zoyi.rx.Observable
import com.zoyi.rx.android.schedulers.AndroidSchedulers
import com.zoyi.rx.functions.Action1
import io.channel.plugin.android.feature.lounge.view.LoungeChatsView
import io.channel.plugin.android.feature.lounge.view.LoungeWelcomeView
import io.channel.plugin.android.feature.lounge.view.listener.OnLoungeChatsLabelClickListener
import io.channel.plugin.android.feature.settings.SettingsActivity
import io.channel.plugin.android.lounge.app_messenger.AppMessengerBottomSheetDialog
import io.channel.plugin.android.view.ChLoadWrapperLayout

class LoungeFragment: CBBaseFragment(), LoungeContract.View, OnChatClickListener, OnIntegrationClickListener {
    private var buttonCloseLounge: View? = null
    private var textDescription: AppCompatTextView? = null
    private var navigation: GlobalNavigation? = null
    private var loadWrapperLayout: ChLoadWrapperLayout? = null
    private var viewError: View? = null
    private var buttonRefresh: BezierButton? = null
    private var cardChats: CardView? = null
    private var welcomeView: LoungeWelcomeView? = null
    private var loungeChats: LoungeChatsView? = null
    private var cardAppMessengers: CardView? = null
    private var layoutAppMessengers: LinearLayout? = null
    private var layoutMoreMessengers: ChBorderLayout? = null
    private var bottomSheetAppMessenger: AppMessengerBottomSheetDialog? = null
    private var loadWrapperInstagram: ChLoadWrapperLayout? = null
    private var viewInstagram: InstagramView? = null
    private var viewInstagramError: LoungeMediaErrorCardView? = null
    private var scrollViewLounge: NestedScrollView? = null
    private var presenter: LoungeContract.Presenter? = null
    private var appMessengersBinder: Binder? = null
    private var channelBinder: Binder? = null
    private var chatId: String? = null
    private var presetMessage: String? = null
    private var handleOpenChat = false
    private var page: String? = null
    private var contacts: List<Contact>? = null

    private var rootView: ActivityFrameView? = null
    companion object {
        fun showMessenger() : LoungeFragment {
            return LoungeFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("handleOpenChat", false)
                    putString("page", PageSelector.getPage())
                }
            }
        }
    }

    override fun getCreatedView(): View? {
        return rootView?.rootView
    }

    override fun onCreateCall(): Boolean {
        rootView = init(R.layout.ch_plugin_activity_lounge)

        rootView?.let { rootView ->


            this.outTransition = Transition.SLIDE_FROM_BOTTOM
            this.navigation = rootView.findViewById<View>(R.id.ch_navigationLounge) as GlobalNavigation
            this.navigation!!.setButtonClickListener(this).activateAvatar().bindChannel().setTitleSize(18)
                .addButton(GlobalNavigation.Button.SETTINGS).addButton(GlobalNavigation.Button.EXIT)
            textDescription =
                rootView.findViewById<View>(R.id.ch_textLoungeChannelDescription) as AppCompatTextView?
            ChannelSelector.bindDescription { description: String? ->
                Views.setVisibility(textDescription, description != null)
                textDescription!!.text = description
            }.bind(this)
            buttonCloseLounge = rootView.findViewById<View>(R.id.ch_buttonLoungeClose)
            buttonCloseLounge!!.setOnClickListener { v: View? ->
                onButtonClick(
                    GlobalNavigation.Button.EXIT
                )
            }
            loadWrapperLayout =
                rootView.findViewById<View>(R.id.ch_loadWrapperLounge) as ChLoadWrapperLayout?
            viewError = rootView.findViewById<View>(R.id.ch_viewLoungeError)
            buttonRefresh = rootView.findViewById<View>(R.id.ch_buttonLoungeRefresh) as BezierButton?
            buttonRefresh!!.setOnClickListener { view: View? -> presenter!!.fetchLoungeData() }
            cardChats = rootView.findViewById<View>(R.id.ch_cardLoungeChats) as CardView?
            loungeChats = rootView.findViewById<View>(R.id.ch_viewLoungeChats) as LoungeChatsView?
            loungeChats!!.onChatClickListener = this
            loungeChats!!.onLoungeChatsLabelClickListener = object : OnLoungeChatsLabelClickListener {
                override fun onChatsOpenClick() {
                    this@LoungeFragment.showChats()
                }

                override fun onReadAllChats() {
                    this@LoungeFragment.presenter?.readAllChats()
                }
            }
            welcomeView = rootView.findViewById<View>(R.id.ch_viewLoungeWelcome) as LoungeWelcomeView?
            welcomeView!!.onStartButtonClickListener =
                View.OnClickListener { v: View? -> startChat(Transition.SLIDE_FROM_RIGHT) }
            cardAppMessengers = rootView.findViewById<View>(R.id.ch_cardLoungeIntegrations) as CardView?
            layoutAppMessengers =
                rootView.findViewById<View>(R.id.ch_layoutLoungeAppMessenger) as LinearLayout?
            layoutMoreMessengers =
                rootView.findViewById<View>(R.id.ch_layoutLoungeMoreMessenger) as ChBorderLayout?
            val closeButtonThreshold = Utils.dpToPx(requireContext(), 50.0f).toInt()
            val closeButtonSize = Utils.dpToPx(requireContext(), 30.0f).toInt()
            scrollViewLounge = rootView.findViewById<View>(R.id.ch_scrollViewLounge) as NestedScrollView?
            scrollViewLounge!!.setOnScrollChangeListener(NestedScrollView.OnScrollChangeListener { v: NestedScrollView?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
                Views.setVisibility(buttonCloseLounge, true)
                if (scrollY < closeButtonThreshold) {
                    buttonCloseLounge!!.alpha = 0.0f
                } else if (scrollY > closeButtonThreshold + closeButtonSize) {
                    buttonCloseLounge!!.alpha = 1.0f
                } else {
                    buttonCloseLounge!!.alpha =
                        (scrollY - closeButtonThreshold).toFloat() / closeButtonSize.toFloat()
                }
            } as NestedScrollView.OnScrollChangeListener)
            this.bind(
                SubscriptionBinder(Observable.combineLatest(HeightProcessor.create(
                    scrollViewLounge!!
                ).observe(), HeightProcessor.create(this.navigation!!).observe()
                ) { first: Int?, second: Int? ->
                    Pair(first, second)
                }.subscribeOn(AndroidSchedulers.mainThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe {
                        val params = viewError!!.layoutParams
                        params.height = it.first as Int - it.second as Int
                        viewError!!.layoutParams = params
                    })
            )
            appMessengersBinder = AppMessengerSelector.bindIntegrations(requireContext(),
                Action1 { contacts: List<Contact> ->
                    this.contacts = contacts
                    layoutAppMessengers!!.removeAllViews()
                    if (contacts.size > 0) {
                        cardAppMessengers!!.visibility = View.VISIBLE
                        layoutAppMessengers!!.visibility = View.VISIBLE
                        Views.setVisibility(layoutMoreMessengers, contacts.size > 3)
                        Stream.of(contacts)
                            .limit(if (contacts.size > 3) 2L else contacts.size.toLong())
                            .forEach { contact: Contact? ->
                                layoutAppMessengers!!.addView(
                                    LoungeAppButtonView(requireContext(), contact, this)
                                )
                            }
                    } else {
                        cardAppMessengers!!.visibility = View.GONE
                        layoutAppMessengers!!.visibility = View.GONE
                        layoutMoreMessengers!!.visibility = View.GONE
                    }
                })
            layoutMoreMessengers!!.setOnClickListener { v: View? ->
                if (bottomSheetAppMessenger != null && bottomSheetAppMessenger!!.isShowing) {
                    bottomSheetAppMessenger!!.dismiss()
                }
                bottomSheetAppMessenger = AppMessengerBottomSheetDialog(requireContext(), this, contacts)
                bottomSheetAppMessenger!!.show()
            }
            loadWrapperInstagram =
                rootView.findViewById<View>(R.id.ch_loadWrapperLoungeMediaInstagram) as ChLoadWrapperLayout?
            viewInstagram = rootView.findViewById<View>(R.id.ch_viewLoungeInstagram) as InstagramView?
            viewInstagramError =
                rootView.findViewById<View>(R.id.ch_viewLoungeInstagramError) as LoungeMediaErrorCardView?
            viewInstagramError!!.setOnErrorRefreshClickListener {
                presenter!!.fetchLoungeMediaData(
                    "instagram"
                )
            }
            this.setLoungeMediaErrorViewDescription(viewInstagramError, "instagram")
            page = this.getString("page")
            presenter = LoungePresenter(this, page)
            bindPresenter(presenter)
            PopupStore.get().popupMessage.set(null)

            arguments?.apply {
                chatId = getString("chatId")
                presetMessage = getString("chatPresetMessage")
                handleOpenChat = getBoolean("handleOpenChat", false)
            }

//            val intent: Intent? = activity?.intent
//            if (intent != null) {
//                chatId = intent.getStringExtra("chatId")
//                presetMessage = intent.getStringExtra("chatPresetMessage")
//                handleOpenChat = intent.getBooleanExtra("handleOpenChat", false)
//            }
        }
        return true
    }


//    protected fun onNewIntent(intent: Intent?) {
//        activity?.onNewIntent(intent)
//        if (intent != null) {
//            chatId = intent.getStringExtra("chatId")
//            presetMessage = intent.getStringExtra("chatPresetMessage")
//            handleOpenChat = intent.getBooleanExtra("handleOpenChat", false)
//        }
//    }

    override fun onResume() {
        super.onResume()
        if (viewInstagram != null) {
            viewInstagram!!.onResume()
        }
    }


    override fun onPause() {
        super.onPause()
        if (viewInstagram != null) {
            viewInstagram!!.onPause()
        }
        if (bottomSheetAppMessenger != null) {
            bottomSheetAppMessenger!!.hide()
        }
        if (welcomeView != null) {
            welcomeView!!.hideDialog()
        }
        if (this.activity?.isFinishing == true) {
            Action.invoke(ActionType.MESSENGER_CLOSED)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (viewInstagram != null) {
            viewInstagram!!.clear()
        }
        if (appMessengersBinder != null) {
            appMessengersBinder!!.unbind()
            appMessengersBinder = null
        }
        if (channelBinder != null) {
            channelBinder!!.unbind()
            channelBinder = null
        }
    }

    override fun onButtonClick(button: GlobalNavigation.Button?) {
        when (button) {
            GlobalNavigation.Button.SETTINGS -> {
                println("zxozxo Lounge ButtonClick SETTINGS")
                IntentUtils.setNextActivity(requireContext(), SettingsActivity::class.java).startActivity()
            }
            GlobalNavigation.Button.EXIT -> {
                println("zxozxo Lounge ButtonClick EXIT")
                (activity as LearningCourseActivity).beginBlackChannelIoFrame()
//                Action.invoke(ActionType.MESSENGER_CLOSED)
//                this.finish()
            }
            else -> {
                println("zxozxo Lounge ButtonClick ELSE")
            }
        }
    }


    override fun onPreviewStateChange(previewState: PreviewState) {
        when (previewState) {
            PreviewState.CHATS, PreviewState.WELCOME -> loadWrapperLayout!!.loadState =
                LoadState.IDLE
            PreviewState.LOADING -> loadWrapperLayout!!.loadState = LoadState.LOADING
            PreviewState.FAILED -> loadWrapperLayout!!.loadState = LoadState.ERROR
        }
        if ((previewState == PreviewState.WELCOME || previewState == PreviewState.CHATS) && handleOpenChat) {
            if (chatId != null) {
                this.startChat(ChatContentType.USER_CHAT, chatId, Transition.NONE)
            } else if (presetMessage != null) {
                this.startChat(presetMessage, Transition.NONE)
            } else {
                this.startChat(Transition.NONE)
            }
            chatId = null
            presetMessage = null
            handleOpenChat = false
        }
        if (welcomeView != null) {
            welcomeView!!.hasChat = previewState == PreviewState.CHATS
        }
    }

    override fun onLoungeMediaStateChange(type: String?, fetchState: FetchState?) {
        when (fetchState) {
            FetchState.COMPLETE, FetchState.FAILED, FetchState.LOADING -> if ("instagram" == type) {
                loadWrapperInstagram!!.visibility = View.VISIBLE
                loadWrapperInstagram!!.loadState = LoadState.fromFetchState(fetchState)
            }
            FetchState.EMPTY -> if ("instagram" == type) {
                loadWrapperInstagram!!.visibility = View.GONE
            }
            else -> {}
        }
    }

    override fun onFetchLoungeMediaInstagram(loungeMedia: MutableList<LoungeMedia>?) {
        val var2: Iterator<LoungeMedia> = loungeMedia!!.iterator()

        while (var2.hasNext()) {
            val media = var2.next()
            if (media is Instagram) {
                if (media.data.isNotEmpty()) {
                    viewInstagram!!.visibility = View.VISIBLE
                    viewInstagram!!.setInstagram(media as Instagram)
                } else {
                    viewInstagram!!.visibility = View.GONE
                }
            }
        }
    }


    override fun onConnectFetch(url: String?) {
        IntentUtils.setApp(requireContext(), url).startActivity()
    }

    override fun onChatsChange(
        items: MutableList<ChatItem>?,
        lessItemCount: Int,
        userAlertCount: Int,
        hiddenAlertCount: Int
    ) {
        if (cardChats != null && loungeChats != null) {
            Views.setVisibility(cardChats, items!!.size > 0)
            loungeChats!!.updateChatItems(items, lessItemCount, userAlertCount, hiddenAlertCount)
        }
    }

    override fun onReadStateChange(state: ChatsReadState?) {
        loungeChats!!.setChatsReadState(state!!)
    }

    override fun onLanguageChange(language: Language?) {
        buttonRefresh!!.text = PResUtils.getString(requireContext(), "ch.error.button")
    }

    override fun onChatItemClick(chatItem: ChatItem) {
        this.startChat(chatItem.type, chatItem.subKey, Transition.SLIDE_FROM_RIGHT)
    }

    override fun onChatItemLongClick(chatItem: ChatItem) {
        ((((ChannelDialog(requireContext()).setTitle(PResUtils.getString("ch.chat.menu.leave_chat")) as ChannelDialog).setDescription(
            PResUtils.getString("ch.chat.menu.leave_chat.content")
        ).addButton(ButtonType.CANCEL) as ChannelDialog).addButton(
            PResUtils.getString("ch.chat.menu.leave"),
            PResUtils.getColor(R.color.ch_bgtxt_red_normal),
            PResUtils.getColor(R.color.ch_bgtxt_absolute_white_dark)
        ) { v: View? ->
            println("zxozxo, leave chat! ")
            presenter!!.leaveChat(
                chatItem
            )
            (activity as LearningCourseActivity).resetChatId(chatItem.id)
        } as ChannelDialog).allowBackpress(true) as ChannelDialog).show()    }

    override fun onLinkClick(link: String?) {
        if (ClipboardUtils.copyToClipBoard(link)) {
            Toast.makeText(
                requireContext(),
                PResUtils.getString("ch.integrations.copy_link.success"),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCallClick(number: String?) {
        if (number != null && Executor.canCall(requireContext())) {
            Executor.openCall(requireContext(), number)
        } else {
            this.showPermissionDeniedToast()
        }

    }

    override fun onMessengerClick(name: String?) {
        presenter!!.fetchConnect(name)
    }
    private fun showPermissionDeniedToast() {
        Toast.makeText(requireContext(), PResUtils.getString("ch.permission.denied"), Toast.LENGTH_LONG).show()
    }

    private fun startChat(transition: Transition) {
        println("zxozxo startChat 1")
        this.startChat(null as String?, transition)
    }

    private fun startChat(presetMessage: String?, transition: Transition) {
        println("zxozxo startChat 2")
//        ChatUtils.createChatActivityIntent(requireActivity(), page).putExtra("chatPresetMessage", presetMessage)
//            .setTransition(transition).startActivityForResult(21)
        DaebakToast.show(requireContext(),"새로운 질문은 질문하기 버튼을 통해 진행해주세요.", overDialog = true)

    }

    private fun startChat(contentType: ChatContentType, chatId: String?, transition: Transition) {
        if (contentType == ChatContentType.USER_CHAT && chatId != null) {
            println("zxozxo startChat 3 : chatId ${chatId}")
//            ChatUtils.createChatActivityIntent(requireActivity(), page).putExtra("chatId", chatId)
//                .setTransition(transition).startActivityForResult(21)
            (activity as LearningCourseActivity).beginChatFragment(chatId, null)
        }
    }

    private fun setLoungeMediaErrorViewDescription(
        errorView: LoungeMediaErrorCardView?,
        type: String
    ) {
        errorView?.setLoungeMediaType(type)
    }

    private fun showChats() {
        Log.d("tpehf", " showChats!")
        println("zxozxo startChat 4")

        IntentUtils.setNextActivity(requireContext(), ChatsActivity::class.java).putExtra("page", page)
            .startActivity()
    }
}