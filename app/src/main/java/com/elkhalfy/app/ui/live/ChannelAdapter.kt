package com.elkhalfy.app.ui.live

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.data.Channel
import com.elkhalfy.app.databinding.ItemChannelBinding

class ChannelAdapter(private val onClick: (Channel) -> Unit) :
    ListAdapter<Channel, ChannelAdapter.ViewHolder>(DIFF) {

    inner class ViewHolder(private val binding: ItemChannelBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ch: Channel) {
            binding.tvName.text = ch.name
            binding.tvCategory.text = ch.catName
            if (ch.streamIcon.isNotEmpty()) {
                Glide.with(binding.ivLogo).load(ch.streamIcon)
                    .placeholder(R.drawable.ic_tv).error(R.drawable.ic_tv)
                    .centerCrop().into(binding.ivLogo)
            } else {
                binding.ivLogo.setImageResource(R.drawable.ic_tv)
            }
            binding.root.setOnClickListener { onClick(ch) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ViewHolder(ItemChannelBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(getItem(position))

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Channel>() {
            override fun areItemsTheSame(a: Channel, b: Channel) = a.streamId == b.streamId
            override fun areContentsTheSame(a: Channel, b: Channel) = a == b
        }
    }
}
