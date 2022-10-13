package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogLcPatternDetailBinding
import kotlinx.coroutines.*

class LCPatternDetailDialog(context: Context,
                            private val imageUrl: String,
                            private val callback: () -> Unit): DialogFragment() {

//    private val viewModel by lazy {
//        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(LCPatternDetailDialogViewModel::class.java)
//    }

    private val binding: DialogLcPatternDetailBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_lc_pattern_detail, null, false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@LCPatternDetailDialog
//            vm = viewModel
            Glide.with(requireContext())
                .load(imageUrl)
                .into(dialogIv)


        }
    }

}