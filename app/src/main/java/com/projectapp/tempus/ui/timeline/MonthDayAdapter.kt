package com.projectapp.tempus.ui.timeline

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projectapp.tempus.R
import java.time.LocalDate

data class MonthCell(
    val date: LocalDate?,            // null = ô trống (padding)
    val labels: List<String> = emptyList() // label string: "eat", "sleep", ...
)

class MonthDayAdapter(
    private val onPick: (LocalDate) -> Unit
) : RecyclerView.Adapter<MonthDayAdapter.VH>() {

    private var items: List<MonthCell> = emptyList()

    fun submit(list: List<MonthCell>) {
        items = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(p: ViewGroup, v: Int): VH {
        val view = LayoutInflater.from(p.context).inflate(R.layout.item_month_day, p, false)
        return VH(view)
    }

    override fun onBindViewHolder(h: VH, pos: Int) = h.bind(items[pos])
    override fun getItemCount(): Int = items.size

    inner class VH(v: View) : RecyclerView.ViewHolder(v) {
        private val tvDay = v.findViewById<TextView>(R.id.tvDay)
        private val iconRow = v.findViewById<LinearLayout>(R.id.iconRow)

        fun bind(cell: MonthCell) {
            val d = cell.date
            if (d == null) {
                tvDay.text = ""
                iconRow.removeAllViews()
                itemView.setOnClickListener(null)
                return
            }

            tvDay.text = d.dayOfMonth.toString()
            iconRow.removeAllViews()

            val ctx = itemView.context
            val show = cell.labels.distinct().take(3)

            for (lb in show) {
                val iv = ImageView(ctx)
                val size = (12 * ctx.resources.displayMetrics.density).toInt()
                val lp = LinearLayout.LayoutParams(size, size)
                lp.marginEnd = (3 * ctx.resources.displayMetrics.density).toInt()
                iv.layoutParams = lp

                val resId = ctx.getIconResId(lb) // ✅ dùng chung mapping y như TimelineAdapter
                iv.setImageResource(resId)
                iconRow.addView(iv)
            }

            if (cell.labels.distinct().size > 3) {
                val dots = TextView(ctx)
                dots.text = "…"
                dots.textSize = 14f
                iconRow.addView(dots)
            }

            itemView.setOnClickListener { onPick(d) }
        }
    }
}
