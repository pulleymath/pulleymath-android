package com.freewheelin.pulley.revision2021.activity.fragments

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.FragmentAlarmDetailBinding
import com.freewheelin.pulley.revision2021.model.response.Alarm
import com.freewheelin.pulley.revision2021.viewmodel.AlarmViewModel
import com.freewheelin.pulley.legacy.utils.IntentUtils
import com.freewheelin.pulley.legacy.views.DaebakToast

class AlarmDetailFragment : Fragment() {
    lateinit var binding: FragmentAlarmDetailBinding
    lateinit var alarm: Alarm
    private val ARG_ALARM = "alarm"
    val viewModel: AlarmViewModel by viewModels()

    companion object {
        fun getInstance(alarm: Alarm) =
            AlarmDetailFragment().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_ALARM, alarm)
                }
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            alarm = it.getSerializable(ARG_ALARM) as Alarm
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_alarm_detail, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = viewModel
            item = alarm

            bodyTv.movementMethod = ScrollingMovementMethod()
        }
        viewModel.wantClose.observe(viewLifecycleOwner) { beClose ->
            if (beClose) {
                activity?.finish()
            }
        }
        viewModel.wantGoAlarmList.observe(viewLifecycleOwner) { wantToList ->
            if (wantToList) {
                activity?.onBackPressed()
            }
        }
        viewModel.sendLinkUrl.observe(viewLifecycleOwner) {
            if (it.isEmpty()) return@observe
            IntentUtils.openWebLink(requireContext(), it, requireContext().packageManager)
        }
    }

}