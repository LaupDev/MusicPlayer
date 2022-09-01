package com.laupdev.spotifyclone.adapters

import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.laupdev.spotifyclone.R
import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            findViewById<TextView>(R.id.tvPrimary).text = song.title
            findViewById<TextView>(R.id.tvSecondary).text = song.subTitle
            glide.load(song.imageUrl).into(findViewById(R.id.ivItemImage))

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }
}