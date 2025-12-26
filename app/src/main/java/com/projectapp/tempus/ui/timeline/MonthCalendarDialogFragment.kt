package com.projectapp.tempus.ui.timeline

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.GridLayoutManager
import com.projectapp.tempus.R
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

class MonthCalendarDialogFragment(
    private val initialDate: LocalDate,
    private val onPick: (LocalDate) -> Unit,
    private val onMonthChange: (YearMonth) -> Unit
) : DialogFragment() {

    private var ym: YearMonth = YearMonth.from(initialDate)
    private var data: Map<LocalDate, List<String>> = emptyMap()
    private lateinit var adapter: MonthDayAdapter
    private lateinit var tvTitle: TextView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val v = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_month_calendar, null, false)

        tvTitle = v.findViewById(R.id.tvMonthTitle)
        val btnPrev = v.findViewById<TextView>(R.id.btnPrev)
        val btnNext = v.findViewById<TextView>(R.id.btnNext)
        val rv = v.findViewById<androidx.recyclerview.widget.RecyclerView>(R.id.rvMonth)

        adapter = MonthDayAdapter { d ->
            onPick(d)
            dismiss()
        }

        rv.layoutManager = GridLayoutManager(requireContext(), 7)
        rv.adapter = adapter

        btnPrev.setOnClickListener {
            ym = ym.minusMonths(1)
            data = emptyMap()          // clear icons để UI không giữ data cũ
            render()
            onMonthChange(ym)          // ✅ nhờ Fragment load icons tháng này
        }

        btnNext.setOnClickListener {
            ym = ym.plusMonths(1)
            data = emptyMap()
            render()
            onMonthChange(ym)
        }

        render()
        onMonthChange(ym) // ✅ mở dialog xong -> load luôn tháng hiện tại

        return AlertDialog.Builder(requireContext())
            .setView(v)
            .create()
    }

    fun setMonthData(ymNew: YearMonth, map: Map<LocalDate, List<String>>) {
        if (ymNew != ym) return
        data = map
        render()
    }

    private fun render() {
        val formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale("vi"))
        tvTitle.text = ym.atDay(1).format(formatter)
        adapter.submit(buildMonthCells(ym, data))
    }

    private fun buildMonthCells(ym: YearMonth, icons: Map<LocalDate, List<String>>): List<MonthCell> {
        val first = ym.atDay(1)
        val lastDay = ym.lengthOfMonth()
        val startOffset = ((first.dayOfWeek.value - DayOfWeek.MONDAY.value) + 7) % 7

        val res = ArrayList<MonthCell>()
        repeat(startOffset) { res.add(MonthCell(null, emptyList())) }

        for (d in 1..lastDay) {
            val date = ym.atDay(d)
            res.add(MonthCell(date, icons[date] ?: emptyList()))
        }

        while (res.size % 7 != 0) res.add(MonthCell(null, emptyList()))
        return res
    }
}
