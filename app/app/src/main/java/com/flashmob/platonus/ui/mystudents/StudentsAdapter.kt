package com.flashmob.platonus.ui.mystudents

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashmob.platonus.data.model.User
import com.flashmob.platonus.databinding.ItemStudentGradeBinding

private val DIFF = object : DiffUtil.ItemCallback<User>() {
    override fun areItemsTheSame(oldItem: User, newItem: User) = oldItem.id == newItem.id
    override fun areContentsTheSame(oldItem: User, newItem: User) = oldItem == newItem
}

class StudentsAdapter(
    private val listener: OnGradeClickListener
) : ListAdapter<User, StudentsAdapter.VH>(DIFF) {

    interface OnGradeClickListener {
        fun onGradeClick(user: User)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(
        ItemStudentGradeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: VH, position: Int) =
        holder.bind(getItem(position), listener)

    class VH(private val b: ItemStudentGradeBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(u: User, l: OnGradeClickListener) = with(b) {
            textStudentName.text = u.name
            root.setOnClickListener { l.onGradeClick(u) }
        }
    }
}