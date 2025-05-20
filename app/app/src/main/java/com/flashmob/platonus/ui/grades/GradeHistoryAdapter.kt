package com.flashmob.platonus.ui.grades

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.databinding.ItemGradeHistoryBinding
import java.text.SimpleDateFormat
import java.util.Locale

private val DIFF = object : DiffUtil.ItemCallback<Grade>() {
    override fun areItemsTheSame(oldItem: Grade, newItem: Grade) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Grade, newItem: Grade) = oldItem == newItem
}

class GradeHistoryAdapter : ListAdapter<Grade, GradeHistoryAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemGradeHistoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(private val binding: ItemGradeHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val monthFmt = SimpleDateFormat("LLLL", Locale("ru"))
        private val dayFmt = SimpleDateFormat("d", Locale("ru"))

        fun bind(item: Grade) = with(binding) {
            textMonth.text = monthFmt.format(item.date).replaceFirstChar {
                it.uppercase()
            }
            textDate.text = dayFmt.format(item.date)
            textHistoryScore.text = item.score.toString()
        }
    }
}