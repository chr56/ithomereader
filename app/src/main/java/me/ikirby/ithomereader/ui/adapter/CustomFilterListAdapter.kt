package me.ikirby.ithomereader.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.ikirby.ithomereader.R

class CustomFilterListAdapter(
    private val list: MutableList<String>,
    private val layoutInflater: LayoutInflater,
    private val onItemClickListener: View.OnClickListener
) : RecyclerView.Adapter<CustomFilterListAdapter.CustomFilterViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomFilterViewHolder {
        val view = layoutInflater.inflate(R.layout.custom_filter_item, parent, false)
        view.setOnClickListener(onItemClickListener)
        return CustomFilterViewHolder(view)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CustomFilterViewHolder, position: Int) {
        holder.textView.text = list[position]
    }

    inner class CustomFilterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView = itemView as TextView
    }
}