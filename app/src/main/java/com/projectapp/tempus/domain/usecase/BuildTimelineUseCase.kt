package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.schedule.dto.ScheduleLabel
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.data.schedule.dto.PriorityType
import com.projectapp.tempus.data.schedule.dto.EditedVersionRow
import com.projectapp.tempus.data.schedule.dto.SubTaskRow
import com.projectapp.tempus.domain.model.TimelineBlock
import com.projectapp.tempus.domain.model.SubtaskInfo
import java.time.*
import java.time.format.DateTimeFormatter

class BuildTimelineUseCase {

    
    private fun parseToZonedDateTime(s: String): ZonedDateTime {
        
        
        val isoString = s.replace(" ", "T")

        return try {
            
            OffsetDateTime.parse(isoString).toZonedDateTime()
        } catch (_: Exception) {
            try {
                
                LocalDateTime.parse(isoString).atZone(ZoneId.of("UTC"))
            } catch (_: Exception) {
                
                LocalDate.parse(s.split(" ")[0]).atStartOfDay(ZoneId.systemDefault())
            }
        }
    }

    
    private fun parseDuration(timeStr: String?): Duration {
        if (timeStr.isNullOrEmpty()) return Duration.ZERO
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val s = if (parts.size > 2) parts[2].toLong() else 0L
            Duration.ofHours(h).plusMinutes(m).plusSeconds(s)
        } catch (e: Exception) {
            Duration.ofMinutes(30) 
        }
    }

    fun build(
        targetDate: LocalDate,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>,
        editedVersions: Map<String, EditedVersionRow>,
        subtasksMap: Map<String, List<SubTaskRow>> = emptyMap()
    ): List<TimelineBlock> {

        val itemsByTask = items.associateBy { it.taskId }

        
        val systemZone = ZoneId.systemDefault()

        
        fun occursOnDate(s: ScheduleRow): Boolean {
            
            val startZdt = parseToZonedDateTime(s.startTimeDate).withZoneSameInstant(systemZone)
            val startDate = startZdt.toLocalDate()

            
            val endDate = s.endDate?.let { 
                try { LocalDate.parse(it.split("T")[0].split(" ")[0]) } 
                catch (_: Exception) { null } 
            }
            if (endDate != null && targetDate.isAfter(endDate)) {
                return false 
            }

            
            if (targetDate.isBefore(startDate)) return false

            return when (s.repeat) {
                RepeatType.once -> targetDate == startDate
                RepeatType.daily -> true 
                RepeatType.weekly -> targetDate.dayOfWeek == startDate.dayOfWeek
                RepeatType.monthly -> targetDate.dayOfMonth == startDate.dayOfMonth
                RepeatType.custom -> {
                    
                    val repeatDays = s.repeatDays?.split(",")?.mapNotNull { it.trim().toIntOrNull() } ?: emptyList()
                    if (repeatDays.isEmpty()) return false
                    
                    repeatDays.contains(targetDate.dayOfWeek.value)
                }
            }
        }

        return schedules.asSequence()
            .filter { occursOnDate(it) }
            .mapNotNull { s ->
                val item = itemsByTask[s.id]
                val status = item?.status ?: StatusType.planned
                if (status == StatusType.delete) return@mapNotNull null

                val ev = item?.editedVersion?.let { editedVersions[it] }

                
                val lbEnum = ev?.label ?: s.label ?: ScheduleLabel.book
                val labelStr = if (lbEnum == ScheduleLabel.UNKNOWN) "book" else lbEnum.name

                
                val colorStr = (ev?.color ?: s.color) ?: "#808080"

                
                val sourceIso = ev?.startTimeDate ?: s.startTimeDate
                val utcTime = parseToZonedDateTime(sourceIso)
                val localZonedTime = utcTime.withZoneSameInstant(systemZone)
                val uiStartTime = LocalDateTime.of(targetDate, localZonedTime.toLocalTime())

                
                val durationStr = ev?.implementationTime ?: s.implementationTime
                val uiDuration = parseDuration(durationStr)

                
                val createdAtLdt = s.createdAt?.let { 
                    try { parseToZonedDateTime(it).withZoneSameInstant(systemZone).toLocalDateTime() } 
                    catch (_: Exception) { null }
                }

                
                val subtaskInfos = subtasksMap[s.id]?.map { st ->
                    SubtaskInfo(
                        id = st.id ?: "",
                        title = st.title,
                        isDone = st.isDone
                    )
                } ?: emptyList()

                
                val titleStr = ev?.name ?: s.name

                
                val priorityVal = ev?.priority ?: s.priority ?: PriorityType.medium

                TimelineBlock(
                    taskId = s.id,
                    scheduleItemId = item?.id,
                    title = titleStr,
                    label = labelStr,
                    labelEnum = lbEnum,
                    color = colorStr,
                    startTime = uiStartTime,
                    duration = uiDuration,
                    priority = priorityVal,
                    status = status,
                    createdAt = createdAtLdt,
                    subtasks = subtaskInfos
                )
            }
            .sortedBy { it.startTime }
            .toList()
    }
}