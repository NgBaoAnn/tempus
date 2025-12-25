package com.projectapp.tempus.domain.usecase

import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.ScheduleItemRow
import com.projectapp.tempus.data.schedule.dto.ScheduleRow
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.data.schedule.dto.EditedVersionRow
import com.projectapp.tempus.domain.model.TimelineBlock
import java.time.*
import java.time.format.DateTimeFormatter

class BuildTimelineUseCase {

    /**
     * 1. Hàm parse chuỗi thời gian từ Database (timestamptz).
     * Xử lý linh hoạt các trường hợp có hoặc không có 'T', có hoặc không có offset.
     */
    private fun parseToZonedDateTime(s: String): ZonedDateTime {
        // Chuẩn hóa chuỗi: Postgres có thể trả về "2025-12-21 12:00:00+00" (dấu cách)
        // Java LocalTime thích "2025-12-21T12:00:00+00" (chữ T)
        val isoString = s.replace(" ", "T")

        return try {
            // Trường hợp chuẩn ISO có Offset (VD: ...+00 hoặc ...+07:00)
            OffsetDateTime.parse(isoString).toZonedDateTime()
        } catch (_: Exception) {
            try {
                // Trường hợp thiếu Offset -> Mặc định gán là UTC
                LocalDateTime.parse(isoString).atZone(ZoneId.of("UTC"))
            } catch (_: Exception) {
                // Fallback: Lấy ngày đầu ngày hệ thống
                LocalDate.parse(s.split(" ")[0]).atStartOfDay(ZoneId.systemDefault())
            }
        }
    }

    /**
     * 2. Hàm parse Duration từ chuỗi Postgres "HH:mm:ss"
     */
    private fun parseDuration(timeStr: String?): Duration {
        if (timeStr.isNullOrEmpty()) return Duration.ZERO
        return try {
            val parts = timeStr.split(":")
            val h = parts[0].toLong()
            val m = parts[1].toLong()
            val s = if (parts.size > 2) parts[2].toLong() else 0L
            Duration.ofHours(h).plusMinutes(m).plusSeconds(s)
        } catch (e: Exception) {
            Duration.ofMinutes(30) // Fallback 30p nếu lỗi
        }
    }

    fun build(
        targetDate: LocalDate,
        schedules: List<ScheduleRow>,
        items: List<ScheduleItemRow>,
        editedVersions: Map<String, EditedVersionRow>
    ): List<TimelineBlock> {

        val itemsByTask = items.associateBy { it.taskId }

        // Lấy múi giờ hiện tại của điện thoại (VD: Asia/Ho_Chi_Minh)
        val systemZone = ZoneId.systemDefault()

        // Hàm kiểm tra xem Task có diễn ra trong ngày targetDate hay không
        fun occursOnDate(s: ScheduleRow): Boolean {
            // Quan trọng: Phải chuyển giờ DB sang giờ Local trước khi so sánh ngày
            val startZdt = parseToZonedDateTime(s.startTimeDate).withZoneSameInstant(systemZone)
            val startDate = startZdt.toLocalDate()

            return when (s.repeat) {
                RepeatType.once -> targetDate == startDate
                RepeatType.daily -> !targetDate.isBefore(startDate)
                RepeatType.weekly -> !targetDate.isBefore(startDate) && targetDate.dayOfWeek == startDate.dayOfWeek
                RepeatType.monthly -> !targetDate.isBefore(startDate) && targetDate.dayOfMonth == startDate.dayOfMonth
            }
        }

        return schedules.asSequence()
            .filter { occursOnDate(it) }
            .mapNotNull { s ->
                val it = itemsByTask[s.id]
                val status = it?.status ?: StatusType.planned

                // Nếu status là 'delete' -> Bỏ qua, không hiển thị
                if (status == StatusType.delete) return@mapNotNull null

                // Lấy bản sửa đổi (nếu có)
                val ev = it?.editedVersion?.let { id -> editedVersions[id] }

                // --- XỬ LÝ TIMEZONE ---
                val sourceIso = ev?.startTimeDate ?: s.startTimeDate

                // B1: Parse chuỗi UTC từ DB
                val utcTime = parseToZonedDateTime(sourceIso)

                // B2: Chuyển sang giờ điện thoại (VD: +00 -> +07)
                val localZonedTime = utcTime.withZoneSameInstant(systemZone)

                // B3: Ghép giờ đó vào ngày đang chọn (targetDate) để hiển thị trên Timeline
                val uiStartTime = LocalDateTime.of(targetDate, localZonedTime.toLocalTime())

                // --- XỬ LÝ DURATION ---
                val durationStr = ev?.implementationTime ?: s.implementationTime
                val uiDuration = parseDuration(durationStr)

                TimelineBlock(
                    taskId = s.id,
                    scheduleItemId = it?.id,
                    title = s.name,

                    // Logic Label: Bản sửa -> Bản gốc -> Mặc định
                    label = (ev?.label ?: s.label)?.name ?: "book",

                    // Logic Color: Bản sửa -> Bản gốc -> Mặc định xám
                    color = (ev?.color ?: s.color) ?: "#808080",

                    // Dữ liệu đã chuẩn hóa cho UI
                    startTime = uiStartTime,
                    duration = uiDuration,
                    status = status
                )
            }
            .sortedBy { it.startTime }
            .toList()
    }
}