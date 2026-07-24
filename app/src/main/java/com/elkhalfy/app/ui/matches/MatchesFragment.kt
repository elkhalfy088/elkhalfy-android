package com.elkhalfy.app.ui.matches

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.elkhalfy.app.R
import com.elkhalfy.app.data.*
import com.elkhalfy.app.databinding.FragmentMatchesBinding
import com.elkhalfy.app.network.ApiClient
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class MatchesFragment : Fragment() {
    private var _binding: FragmentMatchesBinding? = null
    private val binding get() = _binding!!
    private var matchSub = "fixtures"
    private var dayOffset = 0
    private var standLg = 39
    private var scorLg = 39
    private val season = 2024

    private val TOP_LEAGUES = listOf(
        Pair(2, "🏆 دوري أبطال أوروبا"), Pair(39, "🏴󠁧󠁢󠁥󠁮󠁧󠁿 الإنجليزي"),
        Pair(140, "🇪🇸 الإسباني"), Pair(135, "🇮🇹 الإيطالي"),
        Pair(78, "🇩🇪 الألماني"), Pair(61, "🇫🇷 الفرنسي"),
        Pair(307, "🇸🇦 روشن السعودي")
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMatchesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSubTabs()
        buildDateBar()
        loadFixtures()
    }

    private fun setupSubTabs() {
        binding.btnFixtures.setOnClickListener { switchSub("fixtures") }
        binding.btnLiveMatches.setOnClickListener { switchSub("live") }
        binding.btnStandings.setOnClickListener { switchSub("standings") }
        binding.btnScorers.setOnClickListener { switchSub("scorers") }
    }

    private fun switchSub(sub: String) {
        matchSub = sub
        listOf(binding.btnFixtures, binding.btnLiveMatches, binding.btnStandings, binding.btnScorers).forEach {
            it.setBackgroundResource(R.drawable.bg_tab_inactive)
            it.setTextColor(resources.getColor(R.color.text2, null))
        }
        val activeBtn = when(sub) {
            "live" -> binding.btnLiveMatches
            "standings" -> binding.btnStandings
            "scorers" -> binding.btnScorers
            else -> binding.btnFixtures
        }
        activeBtn.setBackgroundResource(R.drawable.bg_tab_active)
        activeBtn.setTextColor(resources.getColor(R.color.accent2, null))

        binding.paneFixtures.visibility = if (sub == "fixtures") View.VISIBLE else View.GONE
        binding.rvLiveMatches.visibility = if (sub == "live") View.VISIBLE else View.GONE
        binding.paneStandings.visibility = if (sub == "standings") View.VISIBLE else View.GONE
        binding.paneScorers.visibility = if (sub == "scorers") View.VISIBLE else View.GONE

        when (sub) {
            "fixtures" -> loadFixtures()
            "live" -> loadLive()
            "standings" -> loadStandings()
            "scorers" -> loadScorers()
        }
    }

    private fun buildDateBar() {
        binding.dateBar.removeAllViews()
        val today = Calendar.getInstance()
        for (i in -3..3) {
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_YEAR, i)
            val label = when(i) { 0 -> "اليوم"; -1 -> "أمس"; 1 -> "غداً"
                else -> SimpleDateFormat("d MMM", Locale("ar")).format(cal.time) }
            val btn = Button(requireContext(), null, 0,
                if (i == dayOffset) R.style.CategoryButton_Active else R.style.CategoryButton).apply {
                text = label; isAllCaps = false
                setOnClickListener {
                    dayOffset = i
                    buildDateBar()
                    loadFixtures()
                }
            }
            binding.dateBar.addView(btn)
        }
    }

    private fun getDateString(offset: Int): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, offset)
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(cal.time)
    }

    private fun loadFixtures() {
        val fixturesAdapter = MatchAdapter { fid, fItem -> MatchDetailSheet.show(requireActivity(), fItem) }
        binding.rvFixtures.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFixtures.adapter = fixturesAdapter
        lifecycleScope.launch {
            try {
                val data = ApiClient.getFixtures(getDateString(dayOffset))
                fixturesAdapter.submitList(data.response)
            } catch (e: Exception) { fixturesAdapter.submitList(emptyList()) }
        }
    }

    private fun loadLive() {
        val liveAdapter = MatchAdapter { fid, fItem -> MatchDetailSheet.show(requireActivity(), fItem) }
        binding.rvLiveMatches.layoutManager = LinearLayoutManager(requireContext())
        binding.rvLiveMatches.adapter = liveAdapter
        lifecycleScope.launch {
            try {
                val data = ApiClient.getLiveFixtures()
                liveAdapter.submitList(data.response)
            } catch (e: Exception) { liveAdapter.submitList(emptyList()) }
        }
    }

    private fun buildLeagueTabs(container: LinearLayout, selected: Int, onClick: (Int) -> Unit) {
        container.removeAllViews()
        TOP_LEAGUES.forEach { (id, name) ->
            val btn = Button(requireContext(), null, 0,
                if (id == selected) R.style.CategoryButton_Active else R.style.CategoryButton).apply {
                text = name; isAllCaps = false
                setOnClickListener {
                    onClick(id)
                    buildLeagueTabs(container, id, onClick)
                }
            }
            container.addView(btn)
        }
    }

    private fun loadStandings() {
        buildLeagueTabs(binding.standTabs, standLg) { id ->
            standLg = id; fetchStandings()
        }
        fetchStandings()
    }

    private fun fetchStandings() {
        val standAdapter = StandingsAdapter()
        binding.rvStandings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvStandings.adapter = standAdapter
        lifecycleScope.launch {
            try {
                val data = ApiClient.getStandings(standLg, season)
                val groups = data.response.firstOrNull()?.league?.standings ?: emptyList()
                standAdapter.submitGroups(groups)
            } catch (e: Exception) { standAdapter.submitGroups(emptyList()) }
        }
    }

    private fun loadScorers() {
        buildLeagueTabs(binding.scorerTabs, scorLg) { id ->
            scorLg = id; fetchScorers()
        }
        fetchScorers()
    }

    private fun fetchScorers() {
        val scorersAdapter = ScorersAdapter()
        binding.rvScorers.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScorers.adapter = scorersAdapter
        lifecycleScope.launch {
            try {
                val data = ApiClient.getTopScorers(scorLg, season)
                scorersAdapter.submitList(data.response)
            } catch (e: Exception) { scorersAdapter.submitList(emptyList()) }
        }
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
