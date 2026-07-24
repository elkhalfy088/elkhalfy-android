package com.elkhalfy.app.ui.matches

import android.view.LayoutInflater
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.elkhalfy.app.R
import com.elkhalfy.app.data.FixtureItem
import com.elkhalfy.app.network.ApiClient
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

object MatchDetailSheet {
    fun show(activity: AppCompatActivity, item: FixtureItem) {
        val dialog = BottomSheetDialog(activity, R.style.SeriesBottomSheet)
        val view = LayoutInflater.from(activity).inflate(R.layout.sheet_match_detail, null)
        dialog.setContentView(view)

        val gh = item.goals.home
        val ga = item.goals.away
        view.findViewById<TextView>(R.id.tv_home_name).text = item.teams.home.name
        view.findViewById<TextView>(R.id.tv_away_name).text = item.teams.away.name
        view.findViewById<TextView>(R.id.tv_score).text = if (gh != null && ga != null) "$gh - $ga" else "vs"
        view.findViewById<TextView>(R.id.tv_league).text = "${item.league.name} · ${item.league.country}"

        val st = item.fixture.status.short
        val elapsed = item.fixture.status.elapsed
        val stMap = mapOf("FT" to "انتهت", "HT" to "استراحة", "1H" to "الشوط الأول",
            "2H" to "الشوط الثاني", "NS" to "لم تبدأ", "AET" to "بعد الوقت")
        view.findViewById<TextView>(R.id.tv_status).text = if (elapsed != null) "$elapsed' · ${stMap[st] ?: st}" else (stMap[st] ?: st)
        view.findViewById<android.widget.Button>(R.id.btn_close_match).setOnClickListener { dialog.dismiss() }

        val eventsContainer = view.findViewById<LinearLayout>(R.id.events_container)
        val loading = view.findViewById<android.widget.ProgressBar>(R.id.pb_loading_match)

        loading.visibility = android.view.View.VISIBLE
        activity.lifecycleScope.launch {
            try {
                val data = ApiClient.getFixtureDetails(item.fixture.id)
                loading.visibility = android.view.View.GONE
                val fix = data.response.firstOrNull() ?: return@launch
                val hid = fix.teams.home.id
                val goals = fix.events.filter { it.type == "Goal" }
                val cards = fix.events.filter { it.type == "Card" }
                eventsContainer.removeAllViews()

                if (goals.isNotEmpty()) {
                    addSection(activity, eventsContainer, "⚽ الأهداف")
                    goals.forEach { ev ->
                        val isHome = ev.team.id == hid
                        val icon = when(ev.detail) { "Own Goal" -> "🙈"; "Penalty" -> "🎯"; else -> "⚽" }
                        addEvent(activity, eventsContainer, "${ev.time.elapsed}${if(ev.time.extra!=null) "+${ev.time.extra}" else ""}'", icon, ev.player.name, if (isHome) "" else "  ←")
                    }
                }
                if (cards.isNotEmpty()) {
                    addSection(activity, eventsContainer, "🟨 البطاقات")
                    cards.forEach { ev ->
                        val icon = if (ev.detail == "Yellow Card") "🟨" else "🟥"
                        addEvent(activity, eventsContainer, "${ev.time.elapsed}'", icon, ev.player.name, "")
                    }
                }
            } catch (e: Exception) {
                loading.visibility = android.view.View.GONE
            }
        }
        dialog.show()
    }

    private fun addSection(activity: AppCompatActivity, container: LinearLayout, title: String) {
        val tv = TextView(activity).apply {
            text = title; textSize = 11f; setTextColor(0xFF4A5568.toInt())
            setPadding(32, 20, 32, 8)
        }
        container.addView(tv)
    }

    private fun addEvent(activity: AppCompatActivity, container: LinearLayout, min: String, icon: String, player: String, side: String) {
        val row = LinearLayout(activity).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(32, 10, 32, 10)
        }
        row.addView(TextView(activity).apply { text = min; textSize = 11f; setTextColor(0xFFF5CC45.toInt()); minWidth = 60 })
        row.addView(TextView(activity).apply { text = icon; textSize = 13f; setPadding(8, 0, 8, 0) })
        row.addView(TextView(activity).apply { text = player + side; textSize = 12f; setTextColor(0xFFE2E8F0.toInt()); layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f) })
        container.addView(row)
    }
}
