package com.laupdev.spotifyclone.adapters

import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.laupdev.spotifyclone.data.entities.Song
import com.laupdev.spotifyclone.databinding.ListItemBinding

class SongViewHolderIMPLEMENT(
    private val itemBinding: ListItemBinding,
    private val glide: RequestManager,
    private val onItemClickListener: ((Song) -> Unit)?
) :
    RecyclerView.ViewHolder(itemBinding.root) {

    fun bind(song: Song) {
        itemBinding.tvPrimary.text = song.title
        itemBinding.tvSecondary.text = song.subTitle
        glide.load(song.imageUrl).into(itemBinding.ivItemImage)
        itemBinding.root.setOnClickListener {
            onItemClickListener?.let { click ->
                click(song)
            }
        }
    }
}