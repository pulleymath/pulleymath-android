package com.freewheelin.pulley.bases

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.freewheelin.pulley.model.User
import io.reactivex.disposables.CompositeDisposable
val Fragment.user: User?
    get() = MyApplication.user

abstract class BaseFragment(val layoutId: Int = 0) : Fragment() {



    protected val disposables by lazy { CompositeDisposable() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        if(layoutId != 0)
            return inflater.inflate(layoutId, container, false)

        throw NotImplementedError()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        //
    }

    override fun onDestroyView() {
        disposables.clear()
        super.onDestroyView()
    }
}
