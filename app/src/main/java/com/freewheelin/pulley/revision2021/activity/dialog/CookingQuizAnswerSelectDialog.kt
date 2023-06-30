package com.freewheelin.pulley.revision2021.activity.dialog

import android.content.Context
import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import androidx.databinding.BindingAdapter
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import android.util.TypedValue
import android.util.DisplayMetrics
import android.view.*
import com.freewheelin.pulley.R
import com.freewheelin.pulley.databinding.DialogLcCookingQuizSelectBinding
import com.freewheelin.pulley.databinding.ItemLcCookingSelectionBinding
import com.freewheelin.pulley.revision2021.activity.base.DiffCallback
import com.freewheelin.pulley.revision2021.model.CookingQuizSelection
import com.freewheelin.pulley.revision2021.viewmodel.learningcourse.cooking.CookingQuizAnswerSelectViewModel
import com.freewheelin.pulley.legacy.utils.dpToPx


class CookingQuizAnswerSelectDialog(
    context: Context,
    private val sourceView: View,
    private val selectionImages: List<String>?,
    private val answerBtnCallback: (content: CookingQuizSelection) -> Unit
): DialogFragment() {

    private val viewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(
            CookingQuizAnswerSelectViewModel::class.java)
    }

    private val binding: DialogLcCookingQuizSelectBinding by lazy {
        DataBindingUtil.inflate(LayoutInflater.from(context), R.layout.dialog_lc_cooking_quiz_select, null, false)
    }

    var dialogDismissed = false

    override fun onStart() {
        super.onStart()
        dialog?.window?.let {
            val params = it.attributes
            params.dimAmount = 0.2f
            it.attributes = params
        }
    }

    override fun onResume() {
        super.onResume()
        if (dialogDismissed && dialog != null) {
            this.dismiss()
        }
    }

    override fun onPause() {
        dialogDismissed = true
        super.onPause()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        setDialogPosition()
        return binding.root
    }
    fun setDialogPosition() {
        val location = IntArray(2)
        sourceView.getLocationInWindow(location)
        val sourceX = location[0]
        val sourceY = location[1]
        dialog?.window?.let {
            it.setGravity(Gravity.LEFT or Gravity.TOP)
            val p = it.attributes
            p.width = ViewGroup.LayoutParams.MATCH_PARENT
            p.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE
            p.x = sourceX
            p.y = sourceY - 116.dpToPx()
            it.attributes = p
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            lifecycleOwner = this@CookingQuizAnswerSelectDialog
            vm = viewModel
            viewModel.initImageUrlList(selectionImages)

//            selectionImageRv.adapter = SelectionListAdapter()
//            selectionImages?.let {
//                if (it.size < 5) {
                    selectionImageRv.layoutParams.width = ViewGroup.LayoutParams.WRAP_CONTENT
//                }
//            }
        }
    }

//    inner class SelectionListAdapter(): ListAdapter<CookingQuizSelection, RecyclerView.ViewHolder>(
//        DiffCallback<CookingQuizSelection>()
//    ) {
//        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
//            return SelectionViewHolder(DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.item_lc_cooking_selection, parent, false))
//        }
//
//        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
//            (holder as SelectionViewHolder).bind(getItem(position))
//        }
//    }

//    inner class SelectionViewHolder(private val binding: ItemLcCookingSelectionBinding): RecyclerView.ViewHolder(binding.root), CookingSelectionItemClickListener {
//
//        fun bind(item: CookingQuizSelection) {
//            binding.apply {
//                listener = this@SelectionViewHolder
//                vm = viewModel
//                this.item = item
//            }
//        }
//
//        override fun onItemClick(content: CookingQuizSelection) {
//            answerBtnCallback(content)
//            dismiss()
//        }
//    }
//    interface CookingSelectionItemClickListener {
//        fun onItemClick(content: CookingQuizSelection)
//    }
}


