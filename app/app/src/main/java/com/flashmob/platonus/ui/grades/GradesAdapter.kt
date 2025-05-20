package com.flashmob.platonus.ui.grades

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashmob.platonus.data.model.Grade
import com.flashmob.platonus.databinding.ItemGradeBinding

private val DIFF = object : DiffUtil.ItemCallback<Grade>() {
    override fun areItemsTheSame(oldItem: Grade, newItem: Grade) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: Grade, newItem: Grade) = oldItem == newItem
}

class GradesAdapter(
    private val onClick: (Grade) -> Unit
) : ListAdapter<Grade, GradesAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b, onClick)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    class VH(
        private val binding: ItemGradeBinding,
        private val onClick: (Grade) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Grade) = with(binding) {
            textSubject.text = item.subjectName
            textTeacher.text = item.teacherName
            textScore.text = "${item.score}"
            root.setOnClickListener {
                onClick(item)
            }
        }
    }
}