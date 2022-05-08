package com.adyen.android.assignment.ui.places

import android.util.Log
import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.Place
import com.adyen.android.assignment.ui.base.BaseRecyclerAdapter
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.listitem_place.view.*

class PlacesAdapter(private val data: List<Place>) : BaseRecyclerAdapter(data) {

    override fun getLayoutResource(viewType: Int) = R.layout.listitem_place

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val place = data[position]

        val prefix = place.categories.firstOrNull()?.icon?.prefix
        val imageSize = 120
        val suffix = place.categories.firstOrNull()?.icon?.suffix
        val imageUrl = "${prefix}bg_$imageSize$suffix"
        Glide.with(holder.view.context).load(imageUrl).into(holder.view.image_icon)

        holder.view.name.text = place.name
        holder.view.distance.text = "${place.distance}m away"

    }

}