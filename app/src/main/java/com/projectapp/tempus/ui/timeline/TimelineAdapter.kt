package com.projectapp.tempus.ui.timeline

import android.graphics.Color
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
import com.projectapp.tempus.domain.model.TimelineBlock
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class TimelineAdapter(private val items: List<TimelineBlock>) :
    RecyclerView.Adapter<TimelineAdapter.TimelineViewHolder>() {

    class TimelineViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Cột 1: Thời gian
        val tvStartTime: TextView = itemView.findViewById(R.id.tvStartTime)
        val tvEndTime: TextView = itemView.findViewById(R.id.tvEndTime)

        // Cột 2: Timeline (Icon & Line)
        val cardIcon: CardView = itemView.findViewById(R.id.cardIcon)
        val imgIcon: ImageView = itemView.findViewById(R.id.imgIcon)
        val viewLine: View = itemView.findViewById(R.id.viewLine)

        // Cột 3: Nội dung
        val tvTimeLabel: TextView = itemView.findViewById(R.id.tvTimeLabel)
        val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)
        val tvDuration: TextView = itemView.findViewById(R.id.tvDuration)
        val tvQuote: TextView = itemView.findViewById(R.id.tvQuote) // Dòng quote nhỏ

        // Cột 4: Checkbox
        val imgCheck: ImageView = itemView.findViewById(R.id.imgCheck)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimelineViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_timeline_block, parent, false)
        return TimelineViewHolder(view)
    }

    @RequiresApi(Build.VERSION_CODES.O) // Yêu cầu Android 8.0 trở lên để xử lý giờ
    override fun onBindViewHolder(holder: TimelineViewHolder, position: Int) {
        val item = items[position]

        // --- 1. XỬ LÝ DỮ LIỆU CHỮ (TEXT) ---
        holder.tvTitle.text = item.title

        // Lấy giờ bắt đầu (Cắt chuỗi ISO: 2025-10-03T07:00:00 -> 07:00)
        val startTimeStr = if (item.startIso.length >= 16) item.startIso.substring(11, 16) else "00:00"
        holder.tvStartTime.text = startTimeStr
        holder.tvTimeLabel.text = startTimeStr // Giờ nhỏ cạnh icon

        // Tính giờ kết thúc (Start + Duration)
        val endTimeStr = calculateEndTime(startTimeStr, item.durationInterval)
        holder.tvEndTime.text = endTimeStr

        // Hiển thị thời lượng
        val durationHour = item.durationInterval.substring(0, 2).toIntOrNull() ?: 0
        val durationMin = item.durationInterval.substring(3, 5).toIntOrNull() ?: 0
        val durationText = if(durationHour > 0) "${durationHour}g ${durationMin}p" else "${durationMin} phút"
        holder.tvDuration.text = "$durationText thời gian rảnh!"


        // --- 2. XỬ LÝ ICON ---
        holder.imgIcon.setImageResource(item.iconId)


        // --- 3. XỬ LÝ MÀU SẮC (THEO DESIGN CỦA BẠN) ---
        try {
            val colorInt = Color.parseColor(item.color)

            // A. Icon: Màu gốc đậm
            holder.imgIcon.setColorFilter(colorInt)

            // B. Nền tròn Icon: Màu gốc mờ (15%)
            holder.cardIcon.setCardBackgroundColor(adjustAlpha(colorInt, 0.15f))

            // C. Đường kẻ dọc: Màu gốc mờ (30%)
            holder.viewLine.setBackgroundColor(adjustAlpha(colorInt, 0.3f))

            // D. Viền Checkbox (Vòng tròn bên phải)
            val checkDrawable = holder.imgCheck.background as GradientDrawable
            checkDrawable.setStroke(4, colorInt) // Độ dày viền 4px, màu theo task

            // E. Chữ Quote (mô phỏng theo màu)
            holder.tvQuote.setTextColor(Color.GRAY)

        } catch (e: Exception) {
            // Fallback nếu lỗi màu
            holder.imgIcon.setColorFilter(Color.BLACK)
        }
    }

    override fun getItemCount(): Int = items.size

    // --- HÀM PHỤ TRỢ ---

    // 1. Hàm làm mờ màu
    private fun adjustAlpha(color: Int, factor: Float): Int {
        val alpha = (Color.alpha(color) * factor).toInt()
        return Color.argb(alpha, Color.red(color), Color.green(color), Color.blue(color))
    }

    // 2. Hàm tính giờ kết thúc (Cần Android 8.0+)
    @RequiresApi(Build.VERSION_CODES.O)
    private fun calculateEndTime(start: String, duration: String): String {
        return try {
            val startTime = LocalTime.parse(start) // "07:00"
            // Duration format "HH:MM:SS" -> lấy giờ và phút
            val durHours = duration.substring(0, 2).toLong()
            val durMinutes = duration.substring(3, 5).toLong()

            val endTime = startTime.plusHours(durHours).plusMinutes(durMinutes)
            endTime.format(DateTimeFormatter.ofPattern("HH:mm"))
        } catch (e: Exception) {
            "00:00"
        }
    }
}