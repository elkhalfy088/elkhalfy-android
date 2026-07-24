package com.elkhalfy.app.ui.matches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.data.ScorerItem

class ScorersAdapter : ListAdapter<ScorerItem, ScorersAdapter.VH>(DIFF) {
    override fun onCreateViewHolder(parent: ViewGroup, vt: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_scorer, parent, false))

    override fun onBindViewHolder(holder: VH, pos: Int) = holder.bind(getItem(pos), pos + 1)

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: ScorerItem, rank: Int) {
            itemView.findViewById<TextView>(R.id.tv_rank).text = rank.toString()
            itemView.findViewById<TextView>(R.id.tv_player_name).text = item.player.name
            itemView.findViewById<TextView>(R.id.tv_goals).text = item.statistics.firstOrNull()?.goals?.total?.toString() ?: "0"
            val teamName = item.statistics.firstOrNull()?.team?.name ?: ""
            itemView.findViewById<TextView>(R.id.tv_team_name).text = teamName
            val photo = itemView.findViewById<ImageView>(R.id.iv_photo)
            if (item.player.photo.isNotEmpty()) Glide.with(photo).load(item.player.photo).circleCrop().into(photo)
            val teamLogo = itemView.findViewById<ImageView>(R.id.iv_team_logo)
            item.statistics.firstOrNull()?.team?.logo?.let { logo ->
                if (logo.isNotEmpty()) Glide.with(teamLogo).load(logo).into(teamLogo)
            }
        }
    }

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<ScorerItem>() {
            override fun areItemsTheSame(a: ScorerItem, b: ScorerItem) = a.player.id == b.player.id
            override fun areContentsTheSame(a: ScorerItem, b: ScorerItem) = a == b
        }
    }
}
