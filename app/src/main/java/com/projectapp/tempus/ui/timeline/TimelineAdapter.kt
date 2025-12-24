package com.projectapp.tempus.ui.timeline

import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.GradientDrawable
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.R
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.ui.timeline.getIconResId // Nhớ import hàm này
import java.time.format.DateTimeFormatter

class TimelineAdapter(
    private var items: List<TimelineBlock>,
    private val onBlockClick: (TimelineBlock) -> Unit,
    private val onStatusClick: (TimelineBlock) -> Unit
) : RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    fun submitList(newItems: List<TimelineBlock>) {
        items = newItems
        notifyDataSetChanged()
    }

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)
        val cardIcon: CardView = itemView.findViewById(R.id.cardIcon)
        val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        val viewLine: View = itemView.findViewById(R.id.viewLine)
        val tvTimeLabel: TextView = itemView.findViewById(R.id.tvTimeLabel)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvQuote: TextView = itemView.findViewById(R.id.tvQuote)
        val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_block, parent, false)
        return TimelineViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = items[position]

        // --- 1. SET TEXT & CLICK ---
        holder.tvTitle.text = item.title
        holder.itemView.setOnClickListener { onBlockClick(item) }
        holder.imgCheck.setOnClickListener { onStatusClick(item) }

        // --- 2. XỬ LÝ THỜI GIAN ---
        val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

        // Start Time
        val startStr = item.startTime.format(timeFormatter)
        holder.tvStartTime.text = startStr
        holder.tvTimeLabel.text = startStr

        // End Time
        val endTime = item.startTime.plus(item.duration)
        holder.tvEndTime.text = endTime.format(timeFormatter)

        // Duration Text (VD: 1g 30p eat)
        val hours = item.duration.toHours()
        val minutes = item.duration.toMinutes() % 60
        val durationText = if (hours > 0) "${hours}g ${minutes}p" else "${minutes}p"
        holder.tvDuration.text = "$durationText ${item.label}"


        // --- 3. XỬ LÝ ICON (Dùng hàm extension) ---
        val iconResId = holder.itemView.context.getIconResId(item.label)
        holder.imgIcon.setImageResource(iconResId)


        // --- 4. XỬ LÝ MÀU SẮC & TRẠNG THÁI ---
        try {
            val colorInt = Color.parseColor(item.color)

            // A. XỬ LÝ CARD ICON (Theo yêu cầu mới)
            // 1. Icon: Tô màu theo DB (Xanh, Đỏ, Tím...)
            holder.imgIcon.setColorFilter(colorInt)

            // 2. Nền Card: Màu xám nhạt cố định (#F5F5F5)
            holder.cardIcon.setCardBackgroundColor(Color.parseColor("#F5F5F5"))

            // 3. Đường kẻ dọc: Vẫn nên giữ màu theo Task (nhưng mờ) để đẹp, hoặc bạn muốn xám luôn thì báo tôi
            holder.viewLine.setBackgroundColor(adjustAlpha(colorInt, 0.5f))


            // B. CHECKBOX LOGIC (Giữ nguyên logic cũ)
            if (item.status == StatusType.done) {
                // === DONE ===
                holder.imgCheck.setBackgroundResource(R.drawable.shape_circle_solid)

                // Tô màu vòng tròn đặc bằng màu của Task
                holder.imgCheck.background.setColorFilter(colorInt, PorterDuff.Mode.SRC_IN)
                holder.imgCheck.setImageDrawable(null)

                holder.tvTitle.paint.isStrikeThruText = true
                holder.tvTitle.alpha = 0.6f

            } else {
                // === PLANNED ===
                holder.imgCheck.setBackgroundResource(R.drawable.shape_circle_outline)
                holder.imgCheck.background.clearColorFilter()
                holder.imgCheck.setImageDrawable(null)

                // Viền vòng tròn theo màu của Task
                val bgShape = holder.imgCheck.background as? GradientDrawable
                bgShape?.mutate()
                bgShape?.setStroke(4, colorInt)

                holder.tvTitle.paint.isStrikeThruText = false
                holder.tvTitle.alpha = 1.0f
            }

        } catch (e: Exception) {
            // Fallback
            holder.imgIcon.setColorFilter(Color.GRAY)
            holder.cardIcon.setCardBackgroundColor(Color.parseColor("#F5F5F5"))
        }
    }

    override fun getItemCount(): Int = items.size

    // Hàm làm mờ màu nền
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }
}