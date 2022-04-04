package com.freewheelin.pulley.activities.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.core.API_V1
import com.freewheelin.pulley.databinding.FragmentMyFaqBinding
import com.freewheelin.pulley.databinding.ItemMypageExpandableListBinding
import com.freewheelin.pulley.model.FAQ
import com.freewheelin.pulley.model.Template
import com.freewheelin.pulley.utils.DateTimeUtils
import com.freewheelin.pulley.utils.setImageURL
import com.ht.RecyclerAdapters.SectionAdapter.IndexPath
import com.ht.RecyclerAdapters.SectionAdapter.SectionAdapter
import com.ht.RecyclerAdapters.SectionAdapter.SectionType
import com.ht.RecyclerAdapters.SectionAdapter.Type
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyFAQFragment : MyPageBaseFragment() {
    var faqs: List<FAQ>? = null
    var selectedIndex: Int? = null

    lateinit var binding: FragmentMyFaqBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_my_faq, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rv.adapter = FrequentQAdapter()
        binding.rv.layoutManager = LinearLayoutManager(context)
        binding.backBtn.setOnClickListener { onBackBtnClicked() }
        API_V1.getFAQList().enqueue(object: Callback<Template<List<FAQ>>>{
            override fun onFailure(call: Call<Template<List<FAQ>>>, t: Throwable) {

            }

            override fun onResponse(call: Call<Template<List<FAQ>>>, response: Response<Template<List<FAQ>>>) {
                faqs = response.body()?.data
                binding.rv.adapter?.notifyDataSetChanged()
            }
        })
    }

    inner class FrequentQAdapter: SectionAdapter<RecyclerView.ViewHolder>() {
        init {
            sectionType = SectionType.header
        }

        override fun getItemViewType(indexPath: IndexPath): Int {
            return when(indexPath.type) {
                Type.header -> 0
                else -> 1
            }
        }

        override fun numberOfRows(section: Int): Int {
            return if(faqs == null) 0 else faqs!!.size
        }


        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, indexPath: IndexPath) {
            (holder as? QuestionItemHolder)?.apply {
                holder.set(faqs!![indexPath.row])

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
                set("질문 목록")
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return if(viewType == 0)
                HeaderHolder.create(parent)
            else {
                val itemBinding: ItemMypageExpandableListBinding = DataBindingUtil.inflate(LayoutInflater.from(requireContext()), R.layout.item_mypage_expandable_list, parent, false)
                itemBinding.dateTv.visibility = View.GONE
                itemBinding.updateTag.visibility = View.GONE
                QuestionItemHolder(itemBinding)
            }
        }
    }
}

class QuestionItemHolder(val itemBinding: ItemMypageExpandableListBinding): RecyclerView.ViewHolder(itemBinding.root) {
    val headerCl = itemBinding.headerCl
    val collapseContainerCl = itemBinding.collapseContainerCl

    val titleTv = itemBinding.titleTv
    val updateTag = itemBinding.updateTag
    val dateTv = itemBinding.dateTv
    val collapseIndicator = itemBinding.collapseIndicateIv
    val headlineTv = itemBinding.headlineTv
    val contentsTv = itemBinding.contentsTv
    val imageView = itemBinding.iv

    fun set(faq: FAQ) {
        titleTv.text = faq.subject
        headlineTv.text = faq.headline
        contentsTv.text = faq.contents
        dateTv.text = DateTimeUtils.yyyyMMddFormat.format(faq.dateTime)

        if(faq.imageUrl?.isNotEmpty() == true) {
            imageView.visibility = View.VISIBLE
            imageView.setImageURL(faq.imageUrl!!)
        } else
            imageView.visibility = View.GONE
    }
}
