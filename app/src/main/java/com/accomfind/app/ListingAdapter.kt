package com.accomfind.app

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.accomfind.app.data.Accommodation
import com.bumptech.glide.Glide

class ListingAdapter(
    private val context: Context,
    private val items: MutableList<Accommodation>
) : RecyclerView.Adapter<ListingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivListing: ImageView = view.findViewById(R.id.ivListing)
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
        val tvLocation: TextView = view.findViewById(R.id.tvLocation)
        val tvDistance: TextView = view.findViewById(R.id.tvDistance)
        val tvPrice: TextView = view.findViewById(R.id.tvPrice)
        val tvAvailDate: TextView = view.findViewById(R.id.tvAvailDate)
        val tvStatus: TextView = view.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_listing, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]

        holder.tvTitle.text = item.title
        holder.tvLocation.text = "📍 ${item.location}"
        holder.tvDistance.text = item.distance
        holder.tvPrice.text = "BWP ${String.format("%,.0f", item.price)}/mo"
        holder.tvAvailDate.text = "Avail. ${item.availabilityDate}"
        holder.tvStatus.text = item.status

        // Status badge
        when (item.status) {
            "Reserved" -> holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
            "Rented" -> {
                holder.tvStatus.text = "Rented"
                holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_reserved)
            }
            else -> holder.tvStatus.setBackgroundResource(R.drawable.bg_badge_available)
        }

        // Load image with Glide (async, avoids OOM)
        val resId = context.resources.getIdentifier(item.imageResName, "drawable", context.packageName)
        if (resId != 0) {
            Glide.with(context).load(resId).centerCrop().into(holder.ivListing)
        } else {
            Glide.with(context).load(R.drawable.house_img_1).centerCrop().into(holder.ivListing)
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra("ACCOM_ID", item.id)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = items.size

    fun updateData(newItems: List<Accommodation>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
