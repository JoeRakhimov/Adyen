package com.adyen.android.assignment.ui.places

import com.adyen.android.assignment.R
import com.adyen.android.assignment.api.model.Place
import com.adyen.android.assignment.ui.base.BaseRecyclerAdapter
import kotlinx.android.synthetic.main.listitem_place.view.*

class PlacesAdapter(private val data: List<Place>) : BaseRecyclerAdapter(data) {

    override fun getLayoutResource(viewType: Int) = R.layout.listitem_place

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        super.onBindViewHolder(holder, position)

        val place = data[position]
        holder.view.name.text = place.name
        holder.view.distance.text = "${place.distance}m away"

    }

}