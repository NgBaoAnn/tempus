package com.projectapp.tempus

import android.graphics.Color
import android.graphics.PorterDuff
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.databinding.ItemTopTaskBinding

data class TopTask(
    val name: String,
    val completedCount: Int,
    val totalCount: Int,
    val percentage: Int,
    val iconResId: Int,
    val colorHex: String
)

class TopTaskAdapter(private val tasks: List<TopTask>) :
    RecyclerView.Adapter<TopTaskAdapter.ViewHolder>() {

    class ViewHolder(val binding: ItemTopTaskBinding) : RecyclerView.ViewHolder(binding.root)

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
            tvTaskName.text = task.name
            tvTaskProgress.text = "Hoàn thành ${task.completedCount}/${task.totalCount} nhiệm vụ"
            tvPercentage.text = "${task.percentage}%"
            ivTaskIcon.setImageResource(task.iconResId)
            pbTask.progress = task.percentage

            // Đổ màu icon giống timeline (tô màu icon, nền xám nhạt)
            try {
                val colorInt = Color.parseColor(task.colorHex)
                ivTaskIcon.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN)
                pbTask.setIndicatorColor(colorInt) // Đổi màu thanh tiến độ theo task luôn cho đồng bộ
            } catch (e: Exception) {
                ivTaskIcon.clearColorFilter()
            }
        }
    }

    override fun getItemCount() = tasks.size
}
