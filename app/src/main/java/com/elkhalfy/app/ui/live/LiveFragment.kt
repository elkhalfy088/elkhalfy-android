package com.elkhalfy.app.ui.live

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.elkhalfy.app.R
import com.elkhalfy.app.data.AppRepository
import com.elkhalfy.app.data.Channel
import com.elkhalfy.app.databinding.FragmentLiveBinding
import com.elkhalfy.app.ui.player.PlayerActivity
import kotlinx.coroutines.launch

class LiveFragment : Fragment() {
    private var _binding: FragmentLiveBinding? = null
    private val binding get() = _binding!!
    private val adapter = ChannelAdapter { ch -> openPlayer(ch) }
    private var selectedCat: String? = null
    private var searchQuery = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentLiveBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvChannels.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChannels.adapter = adapter

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
                AppRepository.loadChannels()
                buildCategoryBar()
                applyFilter()
                binding.swipeRefresh.isRefreshing = false
            }
        }

        loadData()
    }

    private fun loadData() {
        lifecycleScope.launch {
            if (AppRepository.allChannels.isNotEmpty() || AppRepository.loadChannelsFromCache()) {
                buildCategoryBar()
                applyFilter()
            } else {
                val ok = AppRepository.loadChannels()
                if (ok) {
                    buildCategoryBar()
                    applyFilter()
                } else {
                    adapter.submitList(emptyList())
                }
            }
        }
    }

    private fun buildCategoryBar() {
        binding.catBar.removeAllViews()
        val allBtn = createCatButton(getString(R.string.all), selectedCat == null)
        allBtn.setOnClickListener {
            selectedCat = null
            updateCatButtons(allBtn)
            applyFilter()
        }
        binding.catBar.addView(allBtn)

        AppRepository.catList.filter { c -> AppRepository.allowedCats.contains(c.categoryId) }.forEach { cat ->
            val btn = createCatButton(cat.categoryName, selectedCat == cat.categoryId)
            btn.setOnClickListener {
                selectedCat = cat.categoryId
                updateCatButtons(btn)
                applyFilter()
            }
            binding.catBar.addView(btn)
        }
    }

    private fun createCatButton(label: String, active: Boolean): Button {
        return Button(requireContext(), null, 0, if (active) R.style.CategoryButton_Active else R.style.CategoryButton).apply {
            text = label
            isAllCaps = false
            minHeight = 0
            minimumHeight = 0
        }
    }

    private fun updateCatButtons(active: Button) {
        for (i in 0 until binding.catBar.childCount) {
            val btn = binding.catBar.getChildAt(i) as? Button ?: continue
            val isActive = btn == active
            btn.setTextAppearance(if (isActive) R.style.CategoryButton_Active else R.style.CategoryButton)
        }
    }

    private fun applyFilter() {
        var list = AppRepository.allChannels
        if (!selectedCat.isNullOrEmpty()) list = list.filter { it.categoryId == selectedCat }
        if (searchQuery.isNotEmpty()) list = list.filter { it.name.contains(searchQuery, ignoreCase = true) }
        adapter.submitList(list)
    }

    private fun openPlayer(ch: Channel) {
        val intent = Intent(requireContext(), PlayerActivity::class.java).apply {
            putExtra(PlayerActivity.EXTRA_NAME, ch.name)
            putExtra(PlayerActivity.EXTRA_ICON, ch.streamIcon)
            putExtra(PlayerActivity.EXTRA_URL, AppRepository.getChannelStreamUrl(ch))
            putExtra(PlayerActivity.EXTRA_IS_LIVE, true)
            putExtra(PlayerActivity.EXTRA_SUB, ch.catName.ifEmpty { getString(R.string.live_tv) })
        }
        startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
