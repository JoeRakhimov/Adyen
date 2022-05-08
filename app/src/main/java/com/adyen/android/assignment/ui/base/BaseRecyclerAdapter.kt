package com.adyen.android.assignment.ui.base

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseRecyclerAdapter(var items: List<Any>):
    RecyclerView.Adapter<BaseRecyclerAdapter.ViewHolder>() {

    var mClickListener: RecyclerClickListener? = null

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view)

    abstract fun getLayoutResource(viewType: Int): Int

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(getLayoutResource(viewType), parent, false) as View
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.view.setOnClickListener {
            if (position >= 0 && position < items.size) {
                mClickListener?.onClick(items[position])
            }
        }
    }

    override fun getItemCount() = items.size

    fun setClickListener(clickListener: RecyclerClickListener) {
        mClickListener = clickListener
    }

}