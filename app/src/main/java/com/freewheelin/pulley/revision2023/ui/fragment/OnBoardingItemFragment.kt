package com.freewheelin.pulley.revision2023.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentOnBoardingItemBinding
import com.freewheelin.pulley.revision2023.ui.activity.OnBoardingActivity

class OnBoardingItemFragment : Fragment() {


    companion object {
        const val ARG_IMAGE_SRC = "ARG_IMAGE_SRC"
        const val ARG_IS_LAST = "ARG_IS_LAST"
        fun newInstance(imageUrl: String, isLast: Boolean = false): OnBoardingItemFragment {
            val frag = OnBoardingItemFragment()
            val args = Bundle()
            args.putSerializable(ARG_IMAGE_SRC, imageUrl)
            args.putSerializable(ARG_IS_LAST, isLast)
            frag.arguments = args
            return frag
        }
    }
    val binding: FragmentOnBoardingItemBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.fragment_on_boarding_item, null, false)
    }

    lateinit var imageUrl: String
    var isLastImage: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.apply {
            isLastImage = getBoolean(ARG_IS_LAST)
            imageUrl = getString(ARG_IMAGE_SRC, "")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            Glide.with(this@OnBoardingItemFragment)
                .load(imageUrl)
                .into(onBoardingIv)
            if (isLastImage) {
                onBoardingIv.setOnClickListener {
                    (activity as? OnBoardingActivity)?.moveNextActivity()
                }
            }
        }

    }
}