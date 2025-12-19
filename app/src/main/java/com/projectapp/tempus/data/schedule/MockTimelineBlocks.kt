package com.projectapp.tempus.data.schedule

import com.projectapp.tempus.R
import com.projectapp.tempus.data.schedule.dto.StatusType
import com.projectapp.tempus.domain.model.TimelineBlock

object MockTimelineBlocks {

    fun getList(): List<TimelineBlock> {
        return listOf(
            // 1. Sáng sớm: Đã xong
            TimelineBlock(
                taskId = "task_01",
                scheduleItemId = "sch_01",
                title = "Thức dậy & Vệ sinh",
                iconId = R.drawable.bed,       // Bắt buộc có
                color = "#FFC107",             // Màu Vàng Hổ Phách
                startIso = "2025-12-20T06:00:00",
                durationInterval = "00:30:00",
                status = StatusType.done
            ),

            // 2. Tập thể dục: Đã xong
            TimelineBlock(
                taskId = "task_02",
                scheduleItemId = "sch_02",
                title = "Chạy bộ",
                iconId = R.drawable.exercise,  // Bắt buộc có
                color = "#4CAF50",             // Màu Xanh Lá
                startIso = "2025-12-20T06:30:00",
                durationInterval = "00:45:00",
                status = StatusType.skip
            ),

            // 3. Ăn sáng: Bị bỏ qua
            TimelineBlock(
                taskId = "task_03",
                scheduleItemId = "sch_03",
                title = "Ăn sáng",
                iconId = R.drawable.eat,       // Bắt buộc có
                color = "#FF5722",             // Màu Cam Đậm
                startIso = "2025-12-20T07:15:00",
                durationInterval = "00:30:00",
                status = StatusType.skip
            ),

            // 4. Làm việc: Đang lên kế hoạch (Chưa có ID schedule)
            TimelineBlock(
                taskId = "task_04",
                scheduleItemId = null,         // Null vì chưa sync/chưa đến giờ
                title = "Code chức năng mới",
                iconId = R.drawable.water,     // Dùng tạm icon nước (hoặc laptop nếu có)
                color = "#2196F3",             // Màu Xanh Dương
                startIso = "2025-12-20T08:00:00",
                durationInterval = "04:00:00",
                status = StatusType.planned
            ),

            // 5. Nghỉ trưa
            TimelineBlock(
                taskId = "task_05",
                scheduleItemId = null,
                title = "Nghỉ trưa & Ăn uống",
                iconId = R.drawable.eat,
                color = "#9C27B0",             // Màu Tím
                startIso = "2025-12-20T12:00:00",
                durationInterval = "01:30:00",
                status = StatusType.planned
            )
        )
    }
}