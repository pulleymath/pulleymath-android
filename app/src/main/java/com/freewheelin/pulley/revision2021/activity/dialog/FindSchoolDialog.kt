package com.freewheelin.pulley.revision2021.activity.dialog

import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogFindSchoolBinding
import com.freewheelin.pulley.databinding.ItemFindSchoolBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.response.School
import com.freewheelin.pulley.revision2021.model.response.SchoolResponse
import com.freewheelin.pulley.revision2021.viewmodel.FindSchoolViewModel

class FindSchoolDialog(): DialogFragment() {

    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(FindSchoolViewModel::class.java)
    }

    private val binding: DialogFindSchoolBinding by lazy {
        DataBindingUtil.inflate(layoutInflater.cloneInContext(requireContext()), R.layout.dialog_find_school, null, false)
    }
    var callback: (school: School) -> Unit = {}
    companion object {
        const val DIALOG_TEST = "DIALOG_TEST"
        fun newInstance(): FindSchoolDialog {
            val args = Bundle().apply {
//                putSerializable(DIALOG_TEST, test)
            }
            val instance = FindSchoolDialog()
            instance.arguments = args
            return instance
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        arguments?.apply {

        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.onItemClickCallback = {
            this.dismiss()
            this.callback(it)
        }

        binding.apply {
            lifecycleOwner = this@FindSchoolDialog
            vm = viewModel
            recyclerSchool.adapter = FindSchoolAdapter(viewModel)
            btnClose.setOnClickListener {
                dismiss()
            }
        }

        dialog?.window?.decorView?.setOnTouchListener { v, event ->
            val focusView: View? = binding.textSchoolName
            Log.d(javaClass.simpleName, "event=${event.action} view=$focusView")
            if (focusView != null) {
                val rect = Rect()
                focusView.getGlobalVisibleRect(rect)
                val x = event.x.toInt()
                val y = event.y.toInt()
                if (!rect.contains(x, y)) {
                    val imm: InputMethodManager = requireContext().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
                    if (imm != null) imm.hideSoftInputFromWindow(focusView.windowToken, 0)
                    focusView.clearFocus()
                }
            }
            true
        }
    }
}

class FindSchoolAdapter(private val viewModel:FindSchoolViewModel): ListAdapter<School, FindSchoolAdapter.Holder>(DiffCallback<School>()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {

        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_find_school, parent, false)
        val binding:ItemFindSchoolBinding = DataBindingUtil.bind(view)!!
        return Holder(binding)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class Holder(private val binding: ItemFindSchoolBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: School) {
            binding.item = item
            binding.vm = viewModel
        }
    }
}

@BindingAdapter("bind_school_response")
fun bindRecyclerView(recyclerView: RecyclerView, item: SchoolResponse?){
    item?.let { response ->
        val adapter = recyclerView.adapter as FindSchoolAdapter
        adapter.submitList(response.data.content)
    }
}

