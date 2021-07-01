package com.freewheelin.pulley.activities.mypage


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.Theme
import com.freewheelin.pulley.core.manage.NoticeManager
import com.freewheelin.pulley.model.Notice
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setImageURL
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import kotlinx.android.synthetic.main.fragment_my_notice.*
import kotlinx.android.synthetic.main.item_mypage_expandable_list.view.*


class MyNoticeFragment : MyPageBaseFragment() {
    var notices: List<Notice> = NoticeManager.notices
    var selectedIndex: Int? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_notice, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        rv.adapter = NoticeAdapter()
        rv.layoutManager = LinearLayoutManager(context)
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
                val view = LayoutInflater.from(context).inflate(R.layout.item_mypage_expandable_list, parent, false)
                return NoticeItemHolder(view)
            }

        }

    }
}

class NoticeItemHolder(val view: View): RecyclerView.ViewHolder(view) {
    val headerCl = view.headerCl
    val collapseContainerCl = view.collapseContainerCl

    val titleTv = view.titleTv
    val updateTag = view.updateTag
    val dateTv = view.dateTv
    val collapseIndicator = view.collapseIndicateIv
    val headlineTv = view.headlineTv
    val contentsTv = view.contentsTv
    val imageView = view.iv

    fun set(notice: Notice) {
        titleTv.text = notice.subject
        headlineTv.text = notice.headline
        contentsTv.text = notice.contents
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(notice.dateTime)

        if(notice.isNeedUpdateTag()) {
            titleTv.typeface = Theme.bold(view.context)
            updateTag.visibility = View.VISIBLE
        } else {
            titleTv.typeface = Theme.regular(view.context)
            updateTag.visibility = View.GONE
        }

        if(notice.imageUrl?.isNotEmpty() == true)
            imageView.setImageURL(notice.imageUrl!!)
        else
            imageView.visibility = View.GONE

    }
}

