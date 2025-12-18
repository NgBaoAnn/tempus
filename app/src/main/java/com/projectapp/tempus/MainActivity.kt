package com.projectapp.tempus

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.projectapp.tempus.data.schedule.SupabaseScheduleRepository
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.databinding.ActivityMainBinding
import com.projectapp.tempus.ui.timeline.TimelineViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainActivity : AppCompatActivity() {

    private lateinit var vb: ActivityMainBinding
    private val scope = MainScope()

    private var vm: TimelineViewModel? = null
    private var currentDate: LocalDate = LocalDate.now()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vb = ActivityMainBinding.inflate(layoutInflater)
        setContentView(vb.root)
        try {
            vb.btnEditName.setOnClickListener { }
            vb.btnEditTime.setOnClickListener { }
        } catch (e: Exception) {
            android.util.Log.e("BINDING", "Binding views failed: ${e.message}", e)
            finish()
        }


        vb.tvDate.text = "Date: $currentDate"
        vb.btnToday.setOnClickListener {
            currentDate = LocalDate.now()
            vb.tvDate.text = "Date: $currentDate"
            ensureVmAndLoad()
            vm?.onSelectDate(currentDate)
        }

        vb.btnPickDate.setOnClickListener {
            val d = currentDate
            DatePickerDialog(
                this,
                { _, y, m, day ->
                    currentDate = LocalDate.of(y, m + 1, day)
                    vb.tvDate.text = "Date: $currentDate"
                    ensureVmAndLoad()
                    vm?.onSelectDate(currentDate)
                },
                d.year, d.monthValue - 1, d.dayOfMonth
            ).show()
        }

        vb.btnRefresh.setOnClickListener {
            ensureVmAndLoad()
            vm?.onRefresh()
        }

        vb.btnAddDummy.setOnClickListener {
            ensureVmAndLoad()
            vm?.onClickAddDummyTask()
        }

        vb.btnDone.setOnClickListener { upsertFromInput(StatusType.done) }
        vb.btnSkip.setOnClickListener { upsertFromInput(StatusType.skip) }
        vb.btnDelete.setOnClickListener { upsertFromInput(StatusType.delete) }

        vb.btnClickFirst.setOnClickListener {
            val first = vm?.ui?.value?.blocks?.firstOrNull()
            if (first == null) {
                toast("No blocks to click. Refresh first.")
            } else {
                vm?.onClickBlock(first.taskId)
                toast("Logged click for first block")
            }
        }

        vb.btnEditName.setOnClickListener {
            ensureVmAndLoad()
            val v = vm ?: return@setOnClickListener
            val first = v.ui.value.blocks.firstOrNull()
            if (first == null) return@setOnClickListener toast("No blocks. Refresh first.")

            val newName = "Edited ${System.currentTimeMillis()}"
            v.onEditName(first.taskId, newName)
            toast("Editing name...")
        }

        vb.btnEditTime.setOnClickListener {
            ensureVmAndLoad()
            val v = vm ?: return@setOnClickListener
            val first = v.ui.value.blocks.firstOrNull()
            if (first == null) return@setOnClickListener toast("No blocks. Refresh first.")

            // DB bạn đang dùng timestamp (không timezone) nên dùng format này là ổn:
            val newStart = "${v.ui.value.date}T15:00:00"
            val newDuration = "00:20:00"
            v.onEditTime(first.taskId, newStart, newDuration)
            toast("Editing time...")
        }

    }

    private fun ensureVmAndLoad() {
        if (vm != null) return

        val userId = vb.etUserId.text?.toString()?.trim().orEmpty()
        if (userId.isEmpty()) {
            toast("Dán userId (uuid) vào ô userId trước đã")
            return
        }

        val repo = SupabaseScheduleRepository()
        vm = TimelineViewModel(
            userId = userId,
            repo = repo
        )

        scope.launch {
            vm!!.ui.collect { state ->
                vb.tvDate.text = "Date: ${state.date}"

                val sb = StringBuilder()
                sb.append("Loading: ${state.isLoading}\n")
                sb.append("Blocks: ${state.blocks.size}\n\n")

                state.blocks.forEachIndexed { i, b ->
                    sb.append("#${i + 1}  ${b.title}\n")
                    sb.append("taskId=${b.taskId}\n")
                    sb.append("start=${b.startIso}\n")
                    sb.append("duration=${b.durationInterval}\n")
                    sb.append("status=${b.status}\n")
                    sb.append("-----\n")
                }

                vb.tvOutput.text = sb.toString()
                Log.d("TimelineTest", "UI updated blocks=${state.blocks.size}")
            }
        }

        // load lần đầu
        vm!!.onSelectDate(currentDate)
    }

    private fun upsertFromInput(status: StatusType) {
        ensureVmAndLoad()
        val v = vm ?: return

        val inputTaskId = vb.etTaskId.text?.toString()?.trim().orEmpty()
        val taskId = if (inputTaskId.isNotEmpty()) inputTaskId
        else v.ui.value.blocks.firstOrNull()?.taskId

        if (taskId.isNullOrEmpty()) {
            toast("Không có taskId. Refresh để có blocks hoặc dán taskId vào ô taskId.")
            return
        }

        v.onToggleStatus(taskId, status)
        toast("Sent: $status for taskId=$taskId")
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
