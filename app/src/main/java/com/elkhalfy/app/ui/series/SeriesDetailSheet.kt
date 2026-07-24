package com.elkhalfy.app.ui.series

import android.content.Intent
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.elkhalfy.app.R
import com.elkhalfy.app.data.AppRepository
import com.elkhalfy.app.data.Episode
import com.elkhalfy.app.data.Series
import com.elkhalfy.app.network.ApiClient
import com.elkhalfy.app.ui.player.PlayerActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch

object SeriesDetailSheet {
    fun show(activity: AppCompatActivity, series: Series) {
        val dialog = BottomSheetDialog(activity, R.style.SeriesBottomSheet)
        val view = LayoutInflater.from(activity).inflate(R.layout.sheet_series_detail, null)
        dialog.setContentView(view)

        view.findViewById<TextView>(R.id.tv_series_title).text = series.name

        val srv = AppRepository.getFirstServer()
        if (srv == null) { dialog.show(); return }

        val seasonTabs = view.findViewById<LinearLayout>(R.id.season_tabs)
        val episodeList = view.findViewById<LinearLayout>(R.id.episode_list)
        val loading = view.findViewById<android.widget.ProgressBar>(R.id.pb_loading)
        view.findViewById<Button>(R.id.btn_close).setOnClickListener { dialog.dismiss() }

        loading.visibility = android.view.View.VISIBLE
        activity.lifecycleScope.launch {
            try {
                val info = ApiClient.getSeriesInfo(srv, series.seriesId)
                loading.visibility = android.view.View.GONE
                val seasons = info.episodes.keys.sortedBy { it.toIntOrNull() ?: 0 }
                if (seasons.isEmpty()) return@launch
                var currentSeason = seasons[0]

                fun showSeason(sn: String) {
                    currentSeason = sn
                    episodeList.removeAllViews()
                    val eps = info.episodes[sn] ?: return
                    eps.forEach { ep ->
                        val url = AppRepository.getEpisodeStreamUrl(srv, ep.id, ep.containerExtension.ifEmpty { "mp4" })
                        val title = ep.title.ifEmpty { "الحلقة ${ep.episodeNum}" }
                        val epView = LayoutInflater.from(activity).inflate(R.layout.item_episode, episodeList, false)
                        epView.findViewById<TextView>(R.id.tv_ep_num).text = ep.episodeNum.toString()
                        epView.findViewById<TextView>(R.id.tv_ep_title).text = title
                        epView.findViewById<TextView>(R.id.tv_ep_sub).text = "الموسم $sn · الحلقة ${ep.episodeNum}"
                        epView.setOnClickListener {
                            dialog.dismiss()
                            activity.startActivity(Intent(activity, PlayerActivity::class.java).apply {
                                putExtra(PlayerActivity.EXTRA_NAME, title)
                                putExtra(PlayerActivity.EXTRA_URL, url)
                                putExtra(PlayerActivity.EXTRA_IS_LIVE, false)
                                putExtra(PlayerActivity.EXTRA_SUB, series.name)
                            })
                        }
                        episodeList.addView(epView)
                    }
                }

                // Build season tabs
                seasons.forEach { sn ->
                    val btn = Button(activity, null, 0, R.style.CategoryButton).apply {
                        text = "الموسم $sn"
                        isAllCaps = false
                        setOnClickListener {
                            showSeason(sn)
                            for (i in 0 until seasonTabs.childCount) {
                                (seasonTabs.getChildAt(i) as? Button)?.setTextAppearance(R.style.CategoryButton)
                            }
                            setTextAppearance(R.style.CategoryButton_Active)
                        }
                    }
                    seasonTabs.addView(btn)
                }
                (seasonTabs.getChildAt(0) as? Button)?.setTextAppearance(R.style.CategoryButton_Active)
                showSeason(currentSeason)
            } catch (e: Exception) {
                loading.visibility = android.view.View.GONE
            }
        }
        dialog.show()
    }
}
