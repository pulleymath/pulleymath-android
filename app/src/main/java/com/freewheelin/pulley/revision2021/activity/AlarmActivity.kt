package com.freewheelin.pulley.revision2021.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.ActivityAlarmBinding
import com.freewheelin.pulley.databinding.ItemAlarmListBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.activity.fragments.AlarmDetailFragment
import com.freewheelin.pulley.revision2021.model.response.Alarm
import com.freewheelin.pulley.revision2021.viewmodel.AlarmViewModel
import com.freewheelin.pulley.utils.DisplayUtils
import com.freewheelin.pulley.utils.hide
import com.freewheelin.pulley.utils.visibleIf


class AlarmActivity : AppCompatActivity() {
    private val binding: ActivityAlarmBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_alarm, null, false)
    }
    companion object {
        fun getIntent(context: Context) = Intent(context, AlarmActivity::class.java)
    }

    var alarmDetailFragment :AlarmDetailFragment? = null
    val viewModel: AlarmViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        init()
    }

    override fun onResume() {
        super.onResume()
//        viewModel.fetchAlarmList()
    }

    private fun init() {
        binding.apply {
            lifecycleOwner = this@AlarmActivity
            vm = viewModel

            setMessageRv()
            setScreen()


            readAllBtn.setOnClickListener {
                viewModel.readAllMessages()
            }
        }

        viewModel.apply {
            wantClose.observe(this@AlarmActivity) { beClose ->
                if (beClose) {
                    finish()
                }
            }
            wantGoAlarmList.observe(this@AlarmActivity) {
                viewModel.fetchAlarmList()
            }
            showAlarmProgress.observe(this@AlarmActivity) { isShow ->
                binding.apply {
                    if (isShow) {
                        loadingLottie.visibleIf(true)
                        loadingLottie.playAnimation()
                    } else {
                        loadingLottie.hide(300)
                    }
                }
            }
        }
    }
    private fun setMessageRv() {
        binding.apply {
            val adapter = AlarmAdapter(viewModel)
            messageRv.adapter = adapter
            messageRv.layoutManager = LinearLayoutManager(this@AlarmActivity)
        }
    }

    private fun setScreen() {
        val topBottomMargin: Int = (resources.getDimension(R.dimen.dp32) * 2).toInt()
        val lp = binding.rootView.layoutParams
        val screenHeight: Int = DisplayUtils.getScreenHeight(this)
        lp.height = screenHeight - topBottomMargin
        binding.rootView.layoutParams = lp
    }

    fun moveTo(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.add(R.id.childContainer, frag)
        tran.commit()
    }

    override fun onBackPressed() {
        if (alarmDetailFragment != null) {
            back(alarmDetailFragment!!)
            alarmDetailFragment = null
        } else {
            super.onBackPressed()
        }
    }

    fun back(frag: Fragment, withAnim: Boolean = true) {
        val tran = supportFragmentManager.beginTransaction()
        if (withAnim)
            tran.setCustomAnimations(R.anim.enter_to_left, R.anim.exit_to_right)
        tran.remove(frag)
        tran.commit()
    }

    inner class AlarmAdapter(private val viewModel: AlarmViewModel): ListAdapter<Alarm, RecyclerView.ViewHolder>(
        DiffCallback<Alarm>()
    ) {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return AlarmItemHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_alarm_list, parent, false))
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            (holder as AlarmItemHolder).bind(getItem(position))
        }
    }
    inner class AlarmItemHolder(private val binding: ItemAlarmListBinding): RecyclerView.ViewHolder(binding.root), AlarmItemClickListener {

        fun bind(item: Alarm) {
            binding.vm = viewModel
            binding.item = item
            binding.listener = this
        }

        override fun onItemClick(alarm: Alarm) {
            alarm.isReadObservable.set(true)
            viewModel.readMessage(alarm.messageID) {
                AlarmDetailFragment.getInstance(alarm).let {
                    alarmDetailFragment = it
                    moveTo(it, true)
                }
            }
        }
    }
    interface AlarmItemClickListener {
        fun onItemClick(alarm: Alarm)
    }

    override fun onStop() {
        super.onStop()
        viewModel.run {
            clearCompositeDisposable()
        }
    }
}

@BindingAdapter("bind_alarm")
fun bindAlarmRecyclerView(recyclerView: RecyclerView, item: List<Alarm>?){
    Log.d("bind_alarm", "list=$item")
    item?.let { alarmList ->
        val adapter = recyclerView.adapter as AlarmActivity.AlarmAdapter
        adapter.submitList(alarmList)
    }
}