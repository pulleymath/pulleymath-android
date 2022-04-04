package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.NoticeManager
import com.freewheelin.pulley.databinding.FragmentMyNoticeBinding
import com.freewheelin.pulley.databinding.ItemMypageExpandableListBinding
import com.freewheelin.pulley.model.Notice
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setImageURL
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type


class MyNoticeFragment : MyPageBaseFragment() {
    var notices: List<Notice> = NoticeManager.notices
    var selectedIndex: Int? = null

    lateinit var binding: FragmentMyNoticeBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_notice, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = NoticeAdapter()
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
    }


    inner class NoticeAdapter: SectionAdapter<RecyclerView.ViewHolder>() {
        init {
            sectionType = SectionType.header
        }

        override fun getItemViewType(indexPath: IndexPath): Int {
            when(indexPath.type) {
                Type.header -> {
                    return 0
                }
                else -> {
                    return 1
                }
            }
        }

        override fun numberOfRows(section: Int): Int {
            return if(notices == null) 0 else notices!!.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? NoticeItemHolder)?.apply {
                val notice = notices!![indexPath.row]
                set(notice)

                if(selectedIndex == indexPath.row) {
                    collapseContainerCl.visibility = View.VISIBLE
                    collapseIndicator.rotation = 180f
                } else {
                    collapseContainerCl.visibility = View.GONE
                    collapseIndicator.rotation = 0f
                }
                headerCl.setOnClickListener {
                    val beforeIndex = selectedIndex
                    if(beforeIndex != null)
                        notifyItemChanged(getRawPosition(IndexPath(beforeIndex,0, Type.row)), "")

                    if(selectedIndex == indexPath.row)
                        selectedIndex = null
                    else
                        selectedIndex = indexPath.row

                    notifyItemChanged(getRawPosition(indexPath), "")
                }
            }

            (holder as? HeaderHolder)?.apply {
                set("공지 목록")
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            if(viewType == 0)
                return HeaderHolder.create(parent)
            else {
                val itemBinding: ItemMypageExpandableListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(requireContext()), R.layout.item_mypage_expandable_list, parent, false)
//                val view = LayoutInflater.from(context).inflate(R.layout.item_mypage_expandable_list, parent, false)
                return NoticeItemHolder(itemBinding)
            }

        }

    }
}

class NoticeItemHolder(val itemBinding: ItemMypageExpandableListBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val headerCl = itemBinding.headerCl
    val collapseContainerCl = itemBinding.collapseContainerCl

    val titleTv = itemBinding.titleTv
    val updateTag = itemBinding.updateTag
    val dateTv = itemBinding.dateTv
    val collapseIndicator = itemBinding.collapseIndicateIv
    val headlineTv = itemBinding.headlineTv
    val contentsTv = itemBinding.contentsTv
    val imageView = itemBinding.iv

    fun set(notice: Notice) {
        titleTv.text = notice.subject
        headlineTv.text = notice.headline
        contentsTv.text = notice.contents
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(notice.dateTime)

        if(notice.isNeedUpdateTag()) {
            titleTv.typeface = Theme.bold(itemBinding.root.context)
            updateTag.visibility = View.VISIBLE
        } else {
            titleTv.typeface = Theme.regular(itemBinding.root.context)
            updateTag.visibility = View.GONE
        }

        if(notice.imageUrl?.isNotEmpty() == true)
            imageView.setImageURL(notice.imageUrl!!)
        else
            imageView.visibility = View.GONE

    }
}

