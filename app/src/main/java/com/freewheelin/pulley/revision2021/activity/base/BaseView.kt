package com.freewheelin.pulley.revision2021.activity.base

import android.content.Context
import android.util.AttributeSet
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import android.view.ViewGroup

import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnAttach
import androidx.core.view.doOnDetach


abstract class BaseView: ConstraintLayout, LifecycleOwner, LifecycleEventObserver {

    constructor(context: Context) : super(context) {}
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {}
    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {}

    init {
//        val view = createView(LayoutInflater.from(context), this)
//        layoutParams = view.layoutParams
//        addView(view)
        onViewCreated();
        doOnAttach {
            doOnAttached()
        }
        doOnDetach {
            doOnDetached()
        }
    }
    var lifecycleRegistry: LifecycleRegistry = LifecycleRegistry(this)

    override fun getLifecycle(): Lifecycle = lifecycleRegistry

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        lifecycleRegistry.handleLifecycleEvent(event)
    }

//    protected abstract fun createView(
//        inflater: LayoutInflater?,
//        container: ViewGroup
//    ): View

    protected abstract fun onViewCreated()
    protected abstract fun doOnAttached()
    protected abstract fun doOnDetached()
}