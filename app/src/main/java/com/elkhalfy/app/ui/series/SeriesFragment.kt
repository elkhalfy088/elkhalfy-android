package com.elkhalfy.app.ui.series

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.elkhalfy.app.R
import com.elkhalfy.app.data.AppRepository
import com.elkhalfy.app.data.Series
import com.elkhalfy.app.databinding.FragmentVodBinding
import kotlinx.coroutines.launch

class SeriesFragment : Fragment() {
    private var _binding: FragmentVodBinding? = null
    private val binding get() = _binding!!
    private lateinit var adapter: VodAdapter
    private var selectedCat: String? = null
    private var searchQuery = ""
    private var loaded = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentVodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.ivHeaderIcon.setImageResource(R.drawable.ic_monitor)
        binding.tvHeaderTitle.setText(R.string.series)
        binding.etSearch.hint = getString(R.string.search_series)

        adapter = VodAdapter(isMovie = false,
            onItemClick = { idx -> openSeries(idx) },
            getItems = { filtered() }
        )
        val glm = GridLayoutManager(requireContext(), 3).apply {
            spanSizeLookup = object : GridLayoutManager.SpanSizeLookup() {
                override fun getSpanSize(pos: Int) = if (pos == 0) 3 else 1
            }
        }
        binding.rvVod.layoutManager = glm
        binding.rvVod.adapter = adapter

        binding.btnSearch.setOnClickListener {
            if (binding.searchBar.visibility == View.VISIBLE) {
                binding.searchBar.visibility = View.GONE
                binding.etSearch.setText("")
                searchQuery = ""
                applyFilter()
            } else {
                binding.searchBar.visibility = View.VISIBLE
                binding.etSearch.requestFocus()
            }
        }
        binding.etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                searchQuery = s?.toString() ?: ""
                applyFilter()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        binding.swipeRefresh.setColorSchemeColors(resources.getColor(R.color.accent, null))
        binding.swipeRefresh.setOnRefreshListener {
            lifecycleScope.launch {
                AppRepository.loadSeries()
                buildCategoryBar(); applyFilter()
                binding.swipeRefresh.isRefreshing = false
            }
        }
        loadData()
    }

    private fun loadData() {
        if (loaded) return
        lifecycleScope.launch {
            if (AppRepository.allSeries.isNotEmpty() || AppRepository.loadSeriesFromCache()) {
                buildCategoryBar(); applyFilter(); loaded = true
            } else {
                val ok = AppRepository.loadSeries()
                buildCategoryBar(); applyFilter(); loaded = ok
            }
        }
    }

    private fun buildCategoryBar() {
        adapter.setCategories(AppRepository.allSeriesCategories)
        adapter.onCategorySelected = { catId -> selectedCat = catId; applyFilter() }
    }

    private fun filtered(): List<Series> {
        var list = AppRepository.allSeries
        if (!selectedCat.isNullOrEmpty()) list = list.filter { it.categoryId == selectedCat }
        if (searchQuery.isNotEmpty()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        return list
    }

    private fun applyFilter() { adapter.refresh() }

    private fun openSeries(idx: Int) {
        val series = filtered().getOrNull(idx) ?: return
        SeriesDetailSheet.show(requireActivity(), series)
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
