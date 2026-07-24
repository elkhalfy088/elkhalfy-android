package com.elkhalfy.app.ui.movies

import android.content.Intent
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
import com.elkhalfy.app.data.Movie
import com.elkhalfy.app.databinding.FragmentVodBinding
import com.elkhalfy.app.ui.player.PlayerActivity
import com.elkhalfy.app.ui.series.VodAdapter
import kotlinx.coroutines.launch

class MoviesFragment : Fragment() {
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
        binding.ivHeaderIcon.setImageResource(R.drawable.ic_film)
        binding.tvHeaderTitle.setText(R.string.movies)
        binding.etSearch.hint = getString(R.string.search_movie)

        adapter = VodAdapter(isMovie = true,
            onItemClick = { idx -> playMovie(idx) },
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
                AppRepository.loadMovies()
                buildCategoryBar(); applyFilter()
                binding.swipeRefresh.isRefreshing = false
            }
        }
        loadData()
    }

    private fun loadData() {
        if (loaded) return
        lifecycleScope.launch {
            if (AppRepository.allMovies.isNotEmpty() || AppRepository.loadMoviesFromCache()) {
                buildCategoryBar(); applyFilter(); loaded = true
            } else {
                AppRepository.loadMovies()
                buildCategoryBar(); applyFilter(); loaded = true
            }
        }
    }

    private fun buildCategoryBar() {
        adapter.setCategories(AppRepository.allMovieCategories)
        adapter.onCategorySelected = { catId -> selectedCat = catId; applyFilter() }
    }

    private fun filtered(): List<Movie> {
        var list = AppRepository.allMovies
        if (!selectedCat.isNullOrEmpty()) list = list.filter { it.categoryId == selectedCat }
        if (searchQuery.isNotEmpty()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        return list
    }

    private fun applyFilter() { adapter.refresh() }

    private fun playMovie(idx: Int) {
        val movie = filtered().getOrNull(idx) ?: return
        startActivity(Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_NAME, movie.name)
            putExtra(PlayerActivity.EXTRA_ICON, movie.streamIcon)
            putExtra(PlayerActivity.EXTRA_URL, AppRepository.getMovieStreamUrl(movie))
            putExtra(PlayerActivity.EXTRA_IS_LIVE, false)
            putExtra(PlayerActivity.EXTRA_SUB, movie.categoryName.ifEmpty { getString(R.string.movies) })
        })
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}
