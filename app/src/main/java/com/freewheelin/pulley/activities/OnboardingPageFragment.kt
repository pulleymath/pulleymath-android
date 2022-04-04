package com.freewheelin.pulley.activities


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.airbnb.lottie.LottieAnimationView
import com.freewheelin.pulley.R


class OnboardingPageFragment : Fragment() {

    var isFirst: Boolean = false
    var isLast: Boolean = false
    var lottieSrc: String = ""
    companion object {
        const val ARG_LOTTIE_SRC = "ARG_LOTTIE_SRC"
        const val ARG_LOTTIE_FIRST = "ARG_LOTTIE_FIRST"
        const val ARG_LOTTIE_LAST = "ARG_LOTTIE_LAST"
        fun newInstance(lottieSrc: String, isFirst: Boolean, isLast: Boolean): OnboardingPageFragment {
            val frag = OnboardingPageFragment()
            val args = Bundle()
            args.putSerializable(ARG_LOTTIE_SRC, lottieSrc)
            args.putBoolean(ARG_LOTTIE_FIRST, isFirst)
            args.putBoolean(ARG_LOTTIE_LAST, isLast)
            frag.arguments = args
            return frag
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        this.isFirst = arguments?.getBoolean(ARG_LOTTIE_FIRST) ?: false
        this.isLast = arguments?.getBoolean(ARG_LOTTIE_LAST) ?: false
        this.lottieSrc = arguments?.getString(ARG_LOTTIE_SRC) ?: ""
        if(savedInstanceState != null) {
            this.isFirst = savedInstanceState.getBoolean(ARG_LOTTIE_FIRST)
            this.isLast = savedInstanceState.getBoolean(ARG_LOTTIE_LAST)
            this.lottieSrc = savedInstanceState.getString(ARG_LOTTIE_SRC)?:""
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(ARG_LOTTIE_SRC, lottieSrc)
        outState.putSerializable(ARG_LOTTIE_LAST, isLast)
        outState.putSerializable(ARG_LOTTIE_FIRST, isFirst)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_onboarding_page, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUI(view)
    }

    fun runAnim() {

        onboardLottie.setAnimation(lottieSrc)
        onboardLottie.playAnimation()
    }

    lateinit var onboardLottie: LottieAnimationView
    fun initUI(view: View) {
        onboardLottie = view.findViewById(R.id.onboardLottie)

//        if(isLast) {
//            startBtn.visibility = View.VISIBLE
//            startBtn.setOnClickListener {
//                isNeedOnboarding = false
//                val intent = StartActivity.getIntent(context!!)
//                activity?.startActivity(intent)
//                activity?.finish()
//            }
//        } else {
//            startBtn.visibility = View.GONE
//        }

        if(isFirst) {
            runAnim()
        }

//        if(context?.isMobileUI == true)
//            startBtn.text = "다음"
    }

}
