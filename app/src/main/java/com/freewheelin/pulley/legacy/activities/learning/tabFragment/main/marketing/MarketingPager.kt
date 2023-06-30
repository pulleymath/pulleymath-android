package com.freewheelin.pulley.legacy.activities.learning.tabFragment.main.marketing

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.freewheelin.pulley.R
import com.squareup.picasso.Picasso

class MarketingPager(val banners: List<Banner>, val bannerInterface:BannerInterface): RecyclerView.Adapter<MarketingPager.Holder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_marketing_banner, parent, false)
        return Holder(view)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.setItem(banners.get(position))
    }

    override fun getItemCount() = banners.size

    inner class Holder(val view: View) : RecyclerView.ViewHolder(view) {
        val imageView = view.findViewById<ImageView>(R.id.imageView)
        lateinit var mBanner:Banner

        init {
            imageView.setOnClickListener {
                bannerInterface.openBanner(mBanner.link)
            }
        }

        fun setItem(banner: Banner) {
            mBanner = banner
            Picasso.get()
                .load(banner.imageURL)
                .fit()
                .centerCrop()
                .into(imageView)
        }
    }

    interface BannerInterface {
        fun openBanner(urlString:String)
    }
}