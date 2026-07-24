package com.elkhalfy.app.ui.series

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.elkhalfy.app.R
import com.elkhalfy.app.data.Category
import com.elkhalfy.app.data.Movie
import com.elkhalfy.app.data.Series
import com.elkhalfy.app.databinding.ItemVodCardBinding

class VodAdapter(
    private val isMovie: Boolean,
    private val onItemClick: (Int) -> Unit,
    private val getItems: () -> List<Any>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var categories: List<Category> = emptyList()
    private var selectedCat: String? = null
    var onCategorySelected: ((String?) -> Unit)? = null

    companion object {
        const val TYPE_HEADER = 0
        const val TYPE_ITEM = 1
        const val COLS = 3
    }

    fun setCategories(cats: List<Category>) {
        categories = cats
        notifyItemChanged(0)
    }

    fun refresh() { notifyDataSetChanged() }

    private fun items() = getItems()

    override fun getItemCount() = 1 + items().size // header + items
    override fun getItemViewType(pos: Int) = if (pos == 0) TYPE_HEADER else TYPE_ITEM

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val scroll = HorizontalScrollView(parent.context).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
                isHorizontalScrollBarEnabled = false
                setBackgroundColor(Color.parseColor("#111520"))
            }
            val ll = LinearLayout(parent.context).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(24, 0, 24, 0)
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, 126)
            }
            scroll.addView(ll)
            object : RecyclerView.ViewHolder(scroll) {}
        } else {
            val binding = ItemVodCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            // Force 1/3 width for grid effect
            binding.root.layoutParams = RecyclerView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            ItemVH(binding)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (position == 0) {
            val scroll = holder.itemView as HorizontalScrollView
            val ll = scroll.getChildAt(0) as LinearLayout
            ll.removeAllViews()
            val ctx = holder.itemView.context

            fun mkBtn(label: String, active: Boolean, catId: String?): Button {
                return Button(ctx, null, 0,
                    if (active) R.style.CategoryButton_Active else R.style.CategoryButton
                ).apply {
                    text = label
                    isAllCaps = false
                    minHeight = 0
                    minimumHeight = 0
                    setOnClickListener {
                        selectedCat = catId
                        onCategorySelected?.invoke(catId)
                        refresh()
                    }
                }
            }

            ll.addView(mkBtn("الكل", selectedCat == null, null))
            categories.forEach { cat ->
                ll.addView(mkBtn(cat.categoryName, selectedCat == cat.categoryId, cat.categoryId))
            }
            return
        }

        val idx = position - 1
        val item = items().getOrNull(idx) ?: return
        (holder as? ItemVH)?.bind(item, idx, onItemClick)
    }

    inner class ItemVH(private val binding: ItemVodCardBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Any, idx: Int, onClick: (Int) -> Unit) {
            val (name, img, rating) = when (item) {
                is Movie -> Triple(item.name, item.streamIcon, item.rating)
                is Series -> Triple(item.name, item.cover.ifEmpty { item.streamIcon }, item.rating)
                else -> Triple("", "", "")
            }
            binding.tvName.text = name
            if (img.isNotEmpty()) {
                binding.ivPlaceholder.visibility = View.GONE
                Glide.with(binding.ivThumb).load(img).centerCrop().placeholder(R.color.card)
                    .error(R.drawable.ic_film).into(binding.ivThumb)
            } else {
                binding.ivPlaceholder.visibility = View.VISIBLE
                binding.ivThumb.setImageDrawable(null)
            }
            if (rating.isNotEmpty() && rating != "0") {
                binding.tvRating.visibility = View.VISIBLE
                binding.tvRating.text = "★ ${String.format("%.1f", rating.toFloatOrNull() ?: 0f)}"
            } else {
                binding.tvRating.visibility = View.GONE
            }
            binding.root.setOnClickListener { onClick(idx) }
        }
    }
}
