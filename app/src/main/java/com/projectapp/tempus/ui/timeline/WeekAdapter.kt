package com.projectapp.tempus.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.R
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

class WeekAdapter(
    private val onDayClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<WeekAdapter.VH>() {

    private var weeks: List<WeekItem> = emptyList()
    private var selectedDate: LocalDate = LocalDate.now()

    fun submit(weeks: List<WeekItem>, selected: LocalDate) {
        this.weeks = weeks
        this.selectedDate = selected
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val view = LayoutInflater.from(p.context)
            .inflate(R.layout.item_week, p, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) {
        h.bind(weeks[pos])
    }

    override fun getItemCount() = weeks.size

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        private val container: LinearLayout = view.findViewById(R.id.weekContainer)

        fun bind(week: WeekItem) {
            container.removeAllViews()

            week.days.forEach { date ->
                val v = LayoutInflater.from(container.context)
                    .inflate(R.layout.item_calendar_day, container, false)

                v.findViewById<TextView>(R.id.tvDow).text =
                    date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("vi"))

                v.findViewById<TextView>(R.id.tvDay).text =
                    date.dayOfMonth.toString()

                v.isSelected = date == selectedDate
                v.setOnClickListener { onDayClick(date) }

                container.addView(v)
            }
        }
    }
}

