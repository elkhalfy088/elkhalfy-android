package com.elkhalfy.app.ui.matches

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.data.FixtureItem
import java.text.SimpleDateFormat
import java.util.*

class MatchAdapter(
    private val onClick: (Int, FixtureItem) -> Unit
) : ListAdapter<Any, RecyclerView.ViewHolder>(object : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(a: Any, b: Any) = a === b
    override fun areContentsTheSame(a: Any, b: Any) = a == b
}) {
    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_MATCH = 1
    }

    private val flatList = mutableListOf<Any>()

    fun submitList(fixtures: List<FixtureItem>) {
        flatList.clear()
        val grouped = fixtures.groupBy { it.league.id }
        grouped.values.forEach { items ->
            items.firstOrNull()?.let { flatList.add(it.league) }
            flatList.addAll(items)
        }
        submitList(flatList.toList())
    }

    override fun getItemViewType(pos: Int): Int {
        return if (currentList[pos] is com.elkhalfy.app.data.LeagueData) TYPE_HEADER else TYPE_MATCH
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_league_header, parent, false)
            LeagueVH(v)
        } else {
            val v = LayoutInflater.from(parent.context).inflate(R.layout.item_match, parent, false)
            MatchVH(v)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, pos: Int) {
        when (val item = currentList[pos]) {
            is com.elkhalfy.app.data.LeagueData -> (holder as LeagueVH).bind(item)
            is FixtureItem -> (holder as MatchVH).bind(item, onClick)
        }
    }

    inner class LeagueVH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(league: com.elkhalfy.app.data.LeagueData) {
            itemView.findViewById<TextView>(R.id.tv_league_name).text = "${league.name} · ${league.country}"
            val iv = itemView.findViewById<ImageView>(R.id.iv_league_logo)
            if (league.logo.isNotEmpty()) Glide.with(iv).load(league.logo).into(iv)
        }
    }

    inner class MatchVH(v: View) : RecyclerView.ViewHolder(v) {
        fun bind(item: FixtureItem, onClick: (Int, FixtureItem) -> Unit) {
            val hName = itemView.findViewById<TextView>(R.id.tv_home_name)
            val aName = itemView.findViewById<TextView>(R.id.tv_away_name)
            val hLogo = itemView.findViewById<ImageView>(R.id.iv_home_logo)
            val aLogo = itemView.findViewById<ImageView>(R.id.iv_away_logo)
            val tvScore = itemView.findViewById<TextView>(R.id.tv_score)
            val tvStatus = itemView.findViewById<TextView>(R.id.tv_status)

            hName.text = item.teams.home.name
            aName.text = item.teams.away.name

            if (item.teams.home.logo.isNotEmpty()) Glide.with(hLogo).load(item.teams.home.logo).into(hLogo)
            if (item.teams.away.logo.isNotEmpty()) Glide.with(aLogo).load(item.teams.away.logo).into(aLogo)

            val st = item.fixture.status.short
            val gh = item.goals.home
            val ga = item.goals.away
            val isLive = st in listOf("1H","HT","2H","ET","P","BT")

            if (gh != null && ga != null) {
                tvScore.text = "$gh - $ga"
                tvScore.setTextColor(if (isLive) Color.parseColor("#F5CC45") else Color.parseColor("#E2E8F0"))
            } else {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
                    val d = sdf.parse(item.fixture.date)
                    val fmt = SimpleDateFormat("HH:mm", Locale.US)
                    tvScore.text = d?.let { fmt.format(it) } ?: "--:--"
                } catch (e: Exception) { tvScore.text = "--:--" }
                tvScore.setTextColor(Color.parseColor("#E2E8F0"))
            }

            val elapsed = item.fixture.status.elapsed
            tvStatus.text = when {
                isLive && elapsed != null -> "$elapsed'"
                isLive -> "مباشر"
                st == "FT" -> "نهاية"
                st == "HT" -> "استراحة"
                else -> ""
            }
            tvStatus.setTextColor(if (isLive) Color.parseColor("#EF4444") else Color.parseColor("#8892A4"))

            itemView.setOnClickListener { onClick(item.fixture.id, item) }
        }
    }
}
