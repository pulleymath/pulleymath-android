package com.freewheelin.pulley.activities.mypage

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.freewheelin.pulley.activities.learning.LearningTabActivity
import com.freewheelin.pulley.assets.URL
import com.freewheelin.pulley.core.API.ResponseModel.mypage.SummaryBooksItem
import com.freewheelin.pulley.core.API_APP
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

class MyPulleyBooksFragment : MyPageBaseFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_my_pulley_books, container, false)
    }

    lateinit var backBtn: ImageButton
    lateinit var btnOpenPulleyBooks: LinearLayout
    lateinit var btnShowPaidList: LinearLayout

    lateinit var freeContainer: LinearLayout
    lateinit var paidContainer: LinearLayout
    lateinit var recyclerView: RecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews(view)
        load()
    }

    private fun setViews(view: View) {

        backBtn = view.findViewById(R.id.backBtn)
        btnOpenPulleyBooks = view.findViewById(R.id.btnOpenPulleyBooks)
        btnShowPaidList = view.findViewById(R.id.btnShowPaidList)

        freeContainer = view.findViewById(R.id.freeContainer)
        paidContainer = view.findViewById(R.id.paidContainer)
        recyclerView = view.findViewById(R.id.recyclerView)

        backBtn.setOnClickListener {
            onBackBtnClicked()
        }
        btnOpenPulleyBooks.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.풀리북스구매)
            startActivity(intent)
        }
        btnShowPaidList.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(URL.구매내역)
            startActivity(intent)
        }
    }

    private fun load() {
        API_APP.summaryBooks()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                Log.d(javaClass.simpleName, "$result")
                if(result.data.isEmpty()) { //
                    freeContainer.visibility = View.VISIBLE
                    paidContainer.visibility = View.GONE
                } else {
                    freeContainer.visibility = View.GONE
                    paidContainer.visibility = View.VISIBLE
                    val sortedList = result.data!!.sortedByDescending { it.createdAt }

                    setList(sortedList)
                }
            }, {

            })
    }

    private fun setList(list: List<SummaryBooksItem>) {
        val adapter = BooksAdapter(list)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun moveTo(frag: Fragment) {
        (activity as LearningTabActivity).moveTo(frag)
    }

    class BooksAdapter(val list: List<SummaryBooksItem>): RecyclerView.Adapter<BooksAdapter.Holder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            return Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_my_pulley_books, parent, false))
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            holder.set(list.get(position))
        }

        override fun getItemCount() = list.size

        class Holder(val view:View): RecyclerView.ViewHolder(view) {

            var prodNameTv: TextView
            var publisherTv: TextView

            init {
                prodNameTv = view.findViewById(R.id.prodNameTv)
                publisherTv = view.findViewById(R.id.publisherTv)
            }

            fun set(item: SummaryBooksItem) {
                prodNameTv.text = item.title
                publisherTv.text = item.publisher
            }
        }
    }

}