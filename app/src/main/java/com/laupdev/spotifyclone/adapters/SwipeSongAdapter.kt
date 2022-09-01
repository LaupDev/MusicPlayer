package com.laupdev.spotifyclone.adapters

import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.laupdev.spotifyclone.R

class SwipeSongAdapter : BaseSongAdapter(R.layout.swipe_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.tvPrimary).text = "${song.title} - ${song.subTitle}"

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}