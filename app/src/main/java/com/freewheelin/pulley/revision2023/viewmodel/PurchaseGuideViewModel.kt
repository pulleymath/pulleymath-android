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
    lateinit var replaceStep: (Int) -> Unit
    lateinit var removeStep: (Fragment) -> Unit

    private val _guideOffers = MutableLiveData<List<PurchaseGuideOffer>>()
    val guideOffers: LiveData<List<PurchaseGuideOffer>> = _guideOffers
    val selectedOfferId = MutableLiveData<Int>()

    val step = MutableLiveData(0)

    fun fetchGuides(cb: (User) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val newGuides = anonymousRepository.getPurchaseGuide()
            _guideOffers.postValue(newGuides.offers.sortedByDescending { it.offerId })
        }
    }
    fun getTempToken(cb: (String) -> Unit = {}) {
        contentJob = viewModelScope.launch(Dispatchers.IO + contentExceptionHandler) {
            val tempToken = authRepository.getTempToken()
            cb(tempToken.token)
        }
    }

    fun updateGuides(selected: PurchaseGuideOffer) {
        guideOffers.value?.map {
            it.copy(isSelected = it.offerId == selected.offerId)
        }?.let {
            selectedOfferId.postValue(selected.offerId)
            _guideOffers.postValue(it)
        }
    }

    lateinit var onExitClickCallback: (() -> Unit)

    fun exitBtn() {
        onExitClickCallback()
    }
}