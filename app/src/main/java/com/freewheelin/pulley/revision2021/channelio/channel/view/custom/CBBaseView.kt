package com.freewheelin.pulley.revision2021.channelio.channel.view.custom

import android.app.Dialog
import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.doOnAttach
import com.zoyi.channel.plugin.android.activity.base.ActivityFrameView
import com.zoyi.channel.plugin.android.activity.base.navigation.OnGlobalNavigationButtonClickListener
import com.zoyi.channel.plugin.android.bind.BinderCollection
import com.zoyi.channel.plugin.android.bind.BinderController
import com.zoyi.channel.plugin.android.contract.BasePresenter
import com.zoyi.channel.plugin.android.enumerate.Transition
import com.zoyi.rx.Subscription
import com.zoyi.channel.plugin.android.contract.BaseView;

abstract class CBBaseView: View, BaseView, BinderController, OnGlobalNavigationButtonClickListener {


    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    private var actionSubscription: Subscription? = null
    private var presenter: BasePresenter? = null
    private var outTransition: Transition? = null
    private var dialog: Dialog? = null
    private var binderCollection = BinderCollection()

    override fun getBinderCollection(): BinderCollection {
        return BinderCollection()
    }

    private var activityFrameView: ActivityFrameView? = null

    init {
        outTransition = Transition.SLIDE_FROM_RIGHT
//        binderCollection.bind()

        doOnAttach {
//            onCreate()

        }
    }
}