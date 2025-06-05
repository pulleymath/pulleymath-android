package com.freewheelin.pulley.revision2021.channelio.channel.view.custom

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.AnimRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment
import com.freewheelin.pulley.R
import com.zoyi.channel.plugin.android.activity.base.ActivityFrameView
import com.zoyi.channel.plugin.android.activity.base.navigation.GlobalNavigation
import com.zoyi.channel.plugin.android.activity.base.navigation.OnGlobalNavigationButtonClickListener
import com.zoyi.channel.plugin.android.bind.BinderCollection
import com.zoyi.channel.plugin.android.bind.BinderController
import com.zoyi.channel.plugin.android.contract.BasePresenter
import com.zoyi.channel.plugin.android.contract.BaseView
import com.zoyi.channel.plugin.android.enumerate.ActionType
import com.zoyi.channel.plugin.android.enumerate.Transition
import com.zoyi.channel.plugin.android.extension.Views
import com.zoyi.channel.plugin.android.global.Action
import com.zoyi.channel.plugin.android.selector.SocketSelector
import com.zoyi.channel.plugin.android.util.Initializer
import com.zoyi.channel.plugin.android.util.ProgressHelper
import com.zoyi.channel.plugin.android.util.ResUtils
import com.zoyi.channel.plugin.android.util.draw.Display
import com.zoyi.rx.Subscription
import io.channel.plugin.android.socket.SocketManager

abstract class CBBaseFragment : Fragment(), BaseView, BinderController,
    OnGlobalNavigationButtonClickListener {

    companion object {
//        fun newInstance() =
//            CBBaseFragment().apply {
//                arguments = Bundle().apply {
//
//                }
//                outTransition = Transition.SLIDE_FROM_RIGHT
//                binderCollection = BinderCollection()
//            }
    }
//    init {
//        outTransition = Transition.SLIDE_FROM_RIGHT
//        binderCollection = BinderCollection()
//    }

    private var actionSubscription: Subscription? = null
    private var presenter: BasePresenter? = null
    var outTransition: Transition? = null
    private var dialog: Dialog? = null
    private var binderCollection: BinderCollection? = null
    private var activityFrameView: ActivityFrameView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        if (savedInstanceState != null) {
//            this.finish()
//        } else {
//            if (onCreateCall()) {
//                actionSubscription = Action.observable().subscribe { actionType: ActionType? ->
//                    this.handleBaseActions(
//                        actionType
//                    )
//                }
//            } else {
//                this.finish()
//            }
//        }
    }
    protected abstract fun onCreateCall(): Boolean
    protected abstract fun getCreatedView(): View?

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (savedInstanceState != null) {
            this.finish()
        } else {
            if (onCreateCall()) {
                actionSubscription = Action.observable().subscribe ({ actionType: ActionType? ->
                    this.handleBaseActions(actionType)
                }, { error ->
                    Log.e(javaClass.simpleName, "CBBF error=${error.localizedMessage}")
                })

                return getCreatedView()
            } else {
                this.finish()
            }
        }

        return inflater.inflate(R.layout.ch_plugin_activity_lounge, container, false)
    }

    fun handleBaseActions(actionType: ActionType?) {
        when(actionType) {
            ActionType.EXIT -> {
                finish(Transition.SLIDE_FROM_BOTTOM)
            }
            ActionType.SHUTDOWN -> {
                this.finish(Transition.NONE)
            }
            else -> {}
        }
    }

    override fun onResume() {
        super.onResume()
        if (!Display.isLocked() && SocketSelector.doNothing()) {
            SocketManager.get().connect()
        }
    }

    override fun onDestroy() {
        if (presenter != null) {
            presenter!!.release()
            presenter = null
        }

        if (actionSubscription != null) {
            if (!actionSubscription!!.isUnsubscribed) {
                actionSubscription!!.unsubscribe()
            }
            actionSubscription = null
        }

        try {
            getViewNavigation()!!.setButtonClickListener(null as OnGlobalNavigationButtonClickListener?)
        } catch (var2: Exception) {

        }

        unbindAll()
        super.onDestroy()
    }

    protected open fun bindPresenter(presenter: BasePresenter?) {
        this.presenter = presenter
        this.presenter!!.init()
    }
//    val binding: FragmentLearningCoursePatternBinding by lazy {
//        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_learning_course_pattern, null, false)
//    }
    @Initializer
    protected open fun init(@LayoutRes layoutResId: Int): ActivityFrameView {
        activityFrameView = ActivityFrameView(requireContext())
        activityFrameView!!.setContentView(
            LayoutInflater.from(requireContext()).inflate(layoutResId, null as ViewGroup?, false)
        )
        return activityFrameView as ActivityFrameView
    }

//    open fun setOutTransition2(outTransition: Transition?) {
//        this.outTransition = outTransition
//    }


    fun initNavigation(): GlobalNavigation? {
        return initNavigation(false)
    }
    fun initNavigation(floating: Boolean): GlobalNavigation? {
        val navigation = activityFrameView!!.navigation
        Views.setVisibility(navigation, true)
        navigation.setButtonClickListener(this)
        activityFrameView!!.setFloating(floating)
        return navigation
    }
    fun getViewNavigation(): GlobalNavigation? {
        return activityFrameView!!.navigation
    }

    override fun showProgress() {
        this.showProgress(ResUtils.getString(requireContext(), "ch.settings.changing_message"))
    }

    override fun showProgress(message: String?) {
        hideProgress()
        dialog = ProgressHelper.show(requireContext(), message, false)
    }

    override fun hideProgress() {
        if (dialog != null && dialog!!.isShowing) {
            dialog!!.dismiss()
            dialog = null
        }
    }

    override fun finish() {
        // TODO fragment end
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
    open fun getString(key: String?): String? {
        return activity?.intent?.getStringExtra(key)
    }

    override fun getBinderCollection(): BinderCollection {
        if (binderCollection != null) {
            return binderCollection!!
        }
        binderCollection = BinderCollection()
        return binderCollection!!
    }
    open fun getInteger(key: String?): Int? {
        val intent: Intent? = this.activity?.intent
        if (intent != null) {
            val result = intent.getIntExtra(key, -2147483648)
            if (result != -2147483648) {
                return result
            }
        }
        return null
    }

    override fun onButtonClick(button: GlobalNavigation.Button?) {
        when (button) {
            GlobalNavigation.Button.EXIT -> {
                Action.invoke(ActionType.EXIT)
            }
            GlobalNavigation.Button.BACK -> {
                activity?.onBackPressed()
            }
            else -> {

            }
        }
    }

}