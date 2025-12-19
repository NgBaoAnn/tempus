package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.R // <--- Nhớ Import R để lấy icon mặc định
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.data.schedule.dto.EditedVersionRow
import com.projectapp.tempus.domain.model.TimelineBlock
import java.time.*

class BuildTimelineUseCase {

    private fun parseToZonedDateTime(s: String): ZonedDateTime {
        return try {
            OffsetDateTime.parse(s).toZonedDateTime()
        } catch (_: Exception) {
            try {
                LocalDateTime.parse(s).atZone(ZoneId.systemDefault())
            } catch (_: Exception) {
                LocalDate.parse(s).atStartOfDay(ZoneId.systemDefault())
            }
        }
    }

    fun build(
        targetDate: LocalDate,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>,
        editedVersions: Map<String, EditedVersionRow>
    ): List<TimelineBlock> {

        val itemsByTask = items.associateBy { it.taskId }

        fun occursOnDate(s: ScheduleRow): Boolean {
            val start = parseToZonedDateTime(s.startTimeDate).toLocalDate()
            return when (s.repeat) {
                RepeatType.once -> targetDate == start
                RepeatType.daily -> !targetDate.isBefore(start)
                RepeatType.weekly -> !targetDate.isBefore(start) && targetDate.dayOfWeek == start.dayOfWeek
                RepeatType.monthly -> !targetDate.isBefore(start) && targetDate.dayOfMonth == start.dayOfMonth
            }
        }

        return schedules.asSequence()
            .filter { occursOnDate(it) }
            .mapNotNull { s ->
                val it = itemsByTask[s.id]
                val status = it?.status ?: StatusType.planned
                if (status == StatusType.delete) return@mapNotNull null

                val ev = it?.editedVersion?.let { id -> editedVersions[id] }

                TimelineBlock(
                    taskId = s.id,
                    scheduleItemId = it?.id,
                    title = s.name,

                    // --- SỬA Ở ĐÂY ---
                    // Logic: Lấy icon bản sửa -> nếu null thì lấy bản gốc -> nếu vẫn null thì lấy icon mặc định
                    iconId = (ev?.iconId ?: s.iconId) ?: R.drawable.ic_launcher_foreground,

                    // Logic: Lấy màu bản sửa -> nếu null thì lấy bản gốc -> nếu vẫn null thì lấy màu xám
                    color = (ev?.color ?: s.color) ?: "#808080",

                    startIso = ev?.startTimeDate ?: s.startTimeDate,
                    durationInterval = ev?.implementationTime ?: s.implementationTime,
                    status = status
                )
            }
            .sortedBy { parseToZonedDateTime(it.startIso).toInstant() }
            .toList()
    }
}