package com.freewheelin.pulley.revision2023.viewmodel

import android.app.Application
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.freewheelin.pulley.model.User
import com.freewheelin.pulley.revision2023.model.PurchaseGuide
import com.freewheelin.pulley.revision2023.model.PurchaseGuideOffer
import com.freewheelin.pulley.revision2023.repository.AnonymousRepository
import com.freewheelin.pulley.revision2023.repository.AuthRepository
import com.freewheelin.pulley.revision2023.repository.ChallengeRepository
import com.freewheelin.pulley.revision2023.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PurchaseGuideViewModel(application: Application) : BaseAndroidViewModel(application), LifecycleObserver {
    private val challengeRepository by lazy { ChallengeRepository.instance }
    private val userRepository by lazy { UserRepository.instance }
    private val anonymousRepository by lazy { AnonymousRepository.instance }
    private val authRepository by lazy { AuthRepository.instance }
//    val user = userRepository.user

    lateinit var goLoginActCallback: () -> Unit
    lateinit var setStep: (Int) -> Unit
//    lateinit var replaceStep: (Int) -> Unit
    lateinit var removeStep: (Fragment) -> Unit

    private val _originalGuides = MutableLiveData<PurchaseGuide>()
    val originalGuides: LiveData<PurchaseGuide> = _originalGuides
//    val backgroundColor = MutableLiveData<String>()
    val selectedOfferId = MutableLiveData<Int>()

    val step = MutableLiveData(0)
    val step2TabIndex = MutableLiveData<Int>(0)
    val purchaseEnabled = MutableLiveData(false)

    fun fetchGuides(cb: (User) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val newGuides = anonymousRepository.getPurchaseGuide()
            _originalGuides.postValue(newGuides)
        }
    }
    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            cb(tempToken.token)
        }
    }


    fun updateGuides(selected: PurchaseGuideOffer) {
        _originalGuides.value?.let {
            if (step2TabIndex.value == 0) {
                it.single.forEach {
                    it.isSelected.set(false)
                }
            } else {
                it.regular.forEach {
                    it.isSelected.set(false)
                }
            }
        }
        selected.isSelected.set(true)
        purchaseEnabled.postValue(true)
        setSelectedItem(selected)
    }
    var selectedOffer: PurchaseGuideOffer? = null
    fun setSelectedItem(list: List<PurchaseGuideOffer>) {
        val item = list.find { it.isSelected.get() }
        selectedOffer = item
    }
    fun setSelectedItem(item: PurchaseGuideOffer) {
        selectedOffer = item
    }

    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }
}