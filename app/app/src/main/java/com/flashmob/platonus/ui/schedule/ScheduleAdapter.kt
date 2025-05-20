package com.flashmob.platonus.ui.schedule

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.flashmob.platonus.databinding.ItemScheduleDayHeaderBinding
import com.flashmob.platonus.databinding.ItemScheduleLessonBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class ScheduleListItem {
    data class DayHeader(val dayOfWeek: Int) : ScheduleListItem()
    data class Lesson(val subject: com.flashmob.platonus.data.model.Subject) : ScheduleListItem()
}

private val DIFF = object : DiffUtil.ItemCallback<ScheduleListItem>() {
    override fun areItemsTheSame(oldItem: ScheduleListItem, newItem: ScheduleListItem) =
        oldItem == newItem

    override fun areContentsTheSame(oldItem: ScheduleListItem, newItem: ScheduleListItem) =
        oldItem == newItem
}

class ScheduleAdapter :
    ListAdapter<ScheduleListItem, RecyclerView.ViewHolder>(DIFF) {

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is ScheduleListItem.DayHeader -> 0
        is ScheduleListItem.Lesson -> 1
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =
        if (viewType == 0) {
            val b = ItemScheduleDayHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            DayVH(b)
        } else {
            val b = ItemScheduleLessonBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            LessonVH(b)
        }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) =
        when (val item = getItem(position)) {
            is ScheduleListItem.DayHeader -> (holder as DayVH).bind(item)
            is ScheduleListItem.Lesson -> (holder as LessonVH).bind(item)
        }

    private class DayVH(
        private val binding: ItemScheduleDayHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleListItem.DayHeader) {
            val locale = Locale("ru")
            val cal = Calendar.getInstance().apply {
                val calDay = (item.dayOfWeek % 7) + 1
                set(Calendar.DAY_OF_WEEK, calDay)
            }
            val formatter = SimpleDateFormat("EEEE", locale)
            binding.textDayHeader.text =
                formatter.format(cal.time).replaceFirstChar { it.uppercase() }
        }
    }

    private class LessonVH(
        private val binding: ItemScheduleLessonBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ScheduleListItem.Lesson) = with(binding) {
            textTime.text = item.subject.time.replace(" - ", "\n")
            textSubject.text = item.subject.name
            textRoom.text = item.subject.room
            textLessonType.text = item.subject.lessonType
        }
    }
}