package com.projectapp.tempus

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.databinding.ItemTopTaskBinding
import com.projectapp.tempus.domain.usecase.CategoryStats
import com.projectapp.tempus.ui.timeline.getIconResId

class TopTaskAdapter(private var tasks: List<CategoryStats>) :
    RecyclerView.Adapter<TopTaskAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopTaskBinding) : RecyclerView.ViewHolder(binding.root)

    fun updateData(newTasks: List<CategoryStats>) {
        tasks = newTasks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemTopTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val task = tasks[position]
        holder.binding.apply {
            // Tự động dịch tên sang tiếng Việt nếu cần, hoặc để nguyên Capitalize
            tvTaskName.text = translateLabel(task.label)
            tvTaskProgress.text = "Hoàn thành ${task.completedCount}/${task.totalCount} nhiệm vụ"
            tvPercentage.text = "${task.percentage}%"
            
            val iconResId = holder.itemView.context.getIconResId(task.label)
            ivTaskIcon.setImageResource(iconResId)
            pbTask.progress = task.percentage

            val colorHex = getColorForLabel(task.label)
            try {
                val colorInt = Color.parseColor(colorHex)
                ivTaskIcon.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN)
                pbTask.setIndicatorColor(colorInt)
            } catch (e: Exception) {
                ivTaskIcon.clearColorFilter()
            }
        }
    }

    private fun translateLabel(label: String): String {
        return when (label.lowercase()) {
            "eat" -> "Ăn uống"
            "work" -> "Làm việc"
            "study", "book" -> "Học tập"
            "sleep" -> "Ngủ"
            "exercise" -> "Thể dục"
            "rest" -> "Nghỉ ngơi"
            "wakeup" -> "Thức dậy"
            "clean" -> "Dọn dẹp"
            "cook" -> "Nấu ăn"
            "water" -> "Uống nước"
            else -> label.replaceFirstChar { it.uppercase() }
        }
    }

    private fun getColorForLabel(label: String): String {
        return when (label.lowercase()) {
            "eat" -> "#FF2D55"
            "work" -> "#007AFF"
            "study", "book" -> "#5AC8FA"
            "sleep" -> "#34C759"
            "exercise" -> "#FFCC00"
            "rest" -> "#AF52DE"
            "wakeup" -> "#FF9500"
            "clean" -> "#8E8E93"
            "cook" -> "#FF3B30" // Màu đỏ cam cho nấu ăn
            "water" -> "#5856D6"
            else -> "#3CDAEF"
        }
    }

    override fun getItemCount() = tasks.size
}
