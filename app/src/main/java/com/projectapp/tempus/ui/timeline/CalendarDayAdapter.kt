package com.projectapp.tempus.ui.timeline

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.R
import com.projectapp.tempus.databinding.ItemCalendarDayBinding
import java.time.LocalDate

class CalendarDayAdapter(
    private val onClick: (LocalDate) -> Unit
) : RecyclerView.Adapter<CalendarDayAdapter.VH>() {

    private val days = mutableListOf<LocalDate>()
    private var selected: LocalDate = LocalDate.now()

    fun submit(list: List<LocalDate>, selectedDate: LocalDate) {
        days.clear()
        days.addAll(list)
        selected = selectedDate
        notifyDataSetChanged()
    }

    inner class VH(val b: ItemCalendarDayBinding) : RecyclerView.ViewHolder(b.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemCalendarDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun getItemCount() = days.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val d = days[position]
        val b = holder.b

        b.tvDow.text = dowVi(d)
        b.tvDay.text = d.dayOfMonth.toString()

        val isSel = d == selected
        if (isSel) {
            b.tvDay.setBackgroundResource(R.drawable.bg_date_selected_red)
            b.tvDay.setTextColor(0xFFFFFFFF.toInt())
        } else {
            b.tvDay.background = null
            b.tvDay.setTextColor(0xFF000000.toInt())
        }

        b.root.setOnClickListener { onClick(d) }
    }

    private fun dowVi(d: LocalDate): String {
        return when (d.dayOfWeek.value) {
            1 -> "T2"
            2 -> "T3"
            3 -> "T4"
            4 -> "T5"
            5 -> "T6"
            6 -> "T7"
            else -> "CN"
        }
    }
}
