package com.elkhalfy.app.ui.matches

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.data.StandingEntry

class StandingsAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    companion object { const val TYPE_HEADER = 0; const val TYPE_ROW = 1 }
    private val items = mutableListOf<Any>()

    fun submitGroups(groups: List<List<StandingEntry>>) {
        items.clear()
        groups.forEach { g ->
            g.firstOrNull()?.group?.let { if (it.isNotEmpty()) items.add(it) }
            items.addAll(g)
        }
        notifyDataSetChanged()
    }

    override fun getItemCount() = items.size
    override fun getItemViewType(pos: Int) = if (items[pos] is String) TYPE_HEADER else TYPE_ROW

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_header, parent, false)
            object : RecyclerView.ViewHolder(v) {}
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_standing_row, parent, false)
            StandVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        val item = items[pos]
        if (item is String) {
            holder.itemView.findViewById<TextView>(R.id.tv_group)?.text = item
        } else if (item is StandingEntry) {
            (holder as StandVH).bind(item)
        }
    }

    inner class StandVH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(e: StandingEntry) {
            itemView.findViewById<TextView>(R.id.tv_rank).text = e.rank.toString()
            itemView.findViewById<TextView>(R.id.tv_team_name).text = e.team.name
            itemView.findViewById<TextView>(R.id.tv_played).text = e.all.played.toString()
            itemView.findViewById<TextView>(R.id.tv_wins).text = e.all.win.toString()
            itemView.findViewById<TextView>(R.id.tv_losses).text = e.all.lose.toString()
            itemView.findViewById<TextView>(R.id.tv_draws).text = e.all.draw.toString()
            itemView.findViewById<TextView>(R.id.tv_points).text = e.points.toString()
            val iv = itemView.findViewById<ImageView>(R.id.iv_team_logo)
            if (e.team.logo.isNotEmpty()) Glide.with(iv).load(e.team.logo).into(iv)
        }
    }
}
