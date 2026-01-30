package com.projectapp.tempus.data.ai

import com.projectapp.tempus.BuildConfig
import com.projectapp.tempus.core.gemini.GeminiApiKeyManager
import com.projectapp.tempus.core.gemini.GeminiClientProvider
import com.projectapp.tempus.data.ai.dto.Content
import com.projectapp.tempus.data.ai.dto.GeminiRequest
import com.projectapp.tempus.data.ai.dto.GenerationConfig
import com.projectapp.tempus.data.ai.dto.Part
import com.projectapp.tempus.data.schedule.ScheduleRepository
import com.projectapp.tempus.domain.model.ActionType
import com.projectapp.tempus.domain.model.AgentProposal
import com.projectapp.tempus.domain.model.EnergyContext
import com.projectapp.tempus.domain.model.ExecutionResult
import com.projectapp.tempus.domain.model.LifePlan
import com.projectapp.tempus.domain.model.LifePlanProposal
import com.projectapp.tempus.domain.model.Milestone
import com.projectapp.tempus.domain.model.MilestoneStatus
import com.projectapp.tempus.domain.model.PlanStatus
import com.projectapp.tempus.domain.model.ProposedAction
import com.projectapp.tempus.domain.model.ScheduledTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import com.projectapp.tempus.data.user.UserProfileCache

/**
 * Repository for AI operations using Gemini API
 * 
 * Supports two modes:
 * - ASK MODE: Q&A only, no database writes
 * - AGENT MODE: Proposals with Accept/Cancel flow
 */
class AIRepository(
    private val scheduleRepository: ScheduleRepository? = null,
    private val userId: String? = null
) {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    // Conversation history for multi-turn chat
    private val conversationHistory = mutableListOf<Content>()
    
    // Maximum retry attempts when hitting rate limits
    private val maxRetries = 8  // Try all keys once
    
    // ============================================
    // SYSTEM INSTRUCTIONS
    // ============================================
    
    // Ask Mode: Q&A only, no actions
    private fun getAskModeInstruction(): Content {
        val lang = UserProfileCache.getLanguage() ?: "vi"
        val text = if (lang == "en") {
            """You are Tiramisu AI, a smart planning assistant.
            |Mode: ASK MODE (Q&A only)
            |
            |Rules:
            |1. Provide information, explain how things work
            |2. DO NOT perform any actions (create/edit schedule)
            |3. If user requests schedule changes → Guide them to switch to Agent Mode
            |4. Keep answers concise, friendly, and helpful
            |
            |Example response if user asks for action:
            |"To help you manage your schedule, please switch to Agent mode by tapping the 🤖 button above."
            """.trimMargin()
        } else {
            """Bạn là Tiramisu AI, trợ lý lập kế hoạch thông minh.
            |Chế độ: CHỈ HỎI-ĐÁP (Ask Mode)
            |
            |Quy tắc:
            |1. Trả lời thông tin, giải thích cách làm
            |2. KHÔNG thực hiện bất kỳ hành động nào
            |3. Nếu người dùng yêu cầu tạo/sửa/xóa lịch → Hướng dẫn họ chuyển sang Agent Mode
            |4. Trả lời ngắn gọn, thân thiện, hữu ích
            |
            |Ví dụ khi người dùng yêu cầu hành động:
            |"Để tôi giúp bạn tạo lịch, vui lòng chuyển sang chế độ Agent bằng cách nhấn nút 🤖 Agent ở trên."
            """.trimMargin()
        }
        
        return Content(
            role = "user",
            parts = listOf(Part(text = text))
        )
    }

    private val askModeInstruction_Legacy = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lập kế hoạch thông minh.
                |Chế độ: CHỈ HỎI-ĐÁP (Ask Mode)
                |
                |Quy tắc:
                |1. Trả lời thông tin, giải thích cách làm
                |2. KHÔNG thực hiện bất kỳ hành động nào
                |3. Nếu người dùng yêu cầu tạo/sửa/xóa lịch → Hướng dẫn họ chuyển sang Agent Mode
                |4. Trả lời ngắn gọn, thân thiện, hữu ích
                |
                |Ví dụ khi người dùng yêu cầu hành động:
                |"Để tôi giúp bạn tạo lịch, vui lòng chuyển sang chế độ Agent bằng cách nhấn nút 🤖 Agent ở trên."
                """.trimMargin()
            )
        )
    )
    
    // Agent Mode: Proposes actions in JSON format
    private fun getAgentModeInstruction(): Content {
        val lang = UserProfileCache.getLanguage() ?: "vi"
        val text = if (lang == "en") {
            """You are Tiramisu AI, a planning assistant.
            |Mode: AGENT (Action Proposal)
            |
            |When user requests action, return JSON with EXACT format:
            |{
            |  "intent": "Short description of user intent",
            |  "actions": [
            |    {"type": "...", ...}
            |  ],
            |  "impact": "Summary of impact (e.g., Added 3 tasks)"
            |}
            |
            |Valid types: CREATE_SCHEDULE, UPDATE_SCHEDULE, DELETE_SCHEDULE, SKIP_INSTANCE
            |
            |=== CREATE_SCHEDULE ===
            |Format: {"type": "CREATE_SCHEDULE", "name": "Name", "startTime": "HH:MM", "duration": 60, "date": "YYYY-MM-DD", "label": "...", "color": "..."}
            |
            |LABEL (icon) - CHOOSE CORRECTLY:
            |  - wakeup: Alarm, morning routine
            |  - eat: Breakfast, lunch, dinner, meal
            |  - exercise: Gym, running, yoga, workout
            |  - rest: Relax, break, meditation
            |  - water: Drink water, hydration
            |  - book: Study, reading, work, meeting, coding
            |  - sleep: Go to sleep, nap
            |  - clean: Cleaning, laundry, chores
            |  - cook: Cooking, kitchen
            |  - garden: Gardening, planting
            |
            |COLOR (hex) - CHOOSE MATCHING COLOR:
            |  - #FF3B30 (red): exercise
            |  - #FF9500 (orange): wakeup
            |  - #FFCC00 (yellow): eat
            |  - #34C759 (green): clean, garden
            |  - #00C7BE (teal): water
            |  - #007AFF (blue): book
            |  - #5856D6 (dark purple): sleep
            |  - #AF52DE (purple): rest
            |  - #FF2D55 (pink): cook
            |  - #A2845E (brown): garden
            |
            |EXAMPLE CREATE_SCHEDULE:
            |Request: "add IELTS study at 9am tomorrow for 2 hours"
            |→ {"actions": [{"type": "CREATE_SCHEDULE", "name": "Study IELTS", "startTime": "09:00", "duration": 120, "date": "2026-01-26", "label": "book", "color": "#007AFF"}]}
            |
            |=== UPDATE_SCHEDULE ===
            |Format: {"type": "UPDATE_SCHEDULE", "id": "uuid-from-context", "name": "New Name", "startTime": "HH:MM", "duration": 60}
            |- MUST start "id" from provided CONTEXT
            |- Include only changed fields
            |
            |=== DELETE/SKIP ===
            |SKIP_INSTANCE: Remove specific date occurrence for recurring tasks.
            |DELETE_SCHEDULE: Permanently delete task.
            |
            |IMPORTANT:
            |- Return ONLY JSON
            |- ALWAYS use "id" from CONTEXT for UPDATE/DELETE
            """.trimMargin()
        } else {
            """Bạn là Tiramisu AI, trợ lý lập kế hoạch.
            |Chế độ: AGENT (đề xuất hành động)
            |
            |Khi người dùng yêu cầu hành động, trả về JSON với format CHÍNH XÁC:
            |{
            |  "intent": "Mô tả ngắn gọn ý định của người dùng",
            |  "actions": [
            |    {"type": "...", ...}
            |  ],
            |  "impact": "Tóm tắt ảnh hưởng (VD: Thêm 3 công việc mới)"
            |}
            |... (Keep original Vietnamese prompt logic here implicitly via fallback) ...
            |
            |Các type hợp lệ: CREATE_SCHEDULE, UPDATE_SCHEDULE, DELETE_SCHEDULE, SKIP_INSTANCE
            |
            |=== CREATE_SCHEDULE ===
            |Format: {"type": "CREATE_SCHEDULE", "name": "Tên", "startTime": "HH:MM", "duration": 60, "date": "YYYY-MM-DD", "label": "...", "color": "..."}
            |
            |LABEL (icon) - CHỌN ĐÚNG loại phù hợp với hoạt động:
            |  - wakeup: Thức dậy, alarm, báo thức
            |  - eat: Ăn uống, bữa sáng/trưa/tối, meal
            |  - exercise: Tập luyện, gym, thể dục, chạy bộ, yoga
            |  - rest: Nghỉ ngơi, thư giãn, break, meditation
            |  - water: Uống nước, trà, cà phê
            |  - book: Học tập, đọc sách, làm việc, meeting, coding
            |  - sleep: Đi ngủ, ngủ
            |  - clean: Dọn dẹp, vệ sinh, giặt giũ
            |  - cook: Nấu ăn, làm bếp
            |  - garden: Làm vườn, trồng cây
            |
            |COLOR (hex) - CHỌN màu phù hợp với loại hoạt động:
            |  - #FF3B30 (đỏ): exercise, thể thao
            |  - #FF9500 (cam): wakeup, báo thức
            |  - #FFCC00 (vàng): eat, ăn uống
            |  - #34C759 (xanh lá): clean, garden
            |  - #00C7BE (teal): water, uống nước
            |  - #007AFF (xanh dương): book, học tập, làm việc
            |  - #5856D6 (tím đậm): sleep
            |  - #AF52DE (tím): rest, nghỉ ngơi
            |  - #FF2D55 (hồng): cook, nấu ăn
            |  - #A2845E (nâu): garden
            |
            |VÍ DỤ CREATE_SCHEDULE:
            |Request: "thêm lịch học IELTS lúc 9h sáng mai, 2 tiếng"
            |→ {"actions": [{"type": "CREATE_SCHEDULE", "name": "Học IELTS", "startTime": "09:00", "duration": 120, "date": "2026-01-26", "label": "book", "color": "#007AFF"}]}
            |
            |Request: "tạo lịch tập gym 7h sáng"
            |→ {"actions": [{"type": "CREATE_SCHEDULE", "name": "Tập gym", "startTime": "07:00", "duration": 60, "label": "exercise", "color": "#FF3B30"}]}
            |
            |=== UPDATE_SCHEDULE ===
            |Format: {"type": "UPDATE_SCHEDULE", "id": "uuid-từ-context", "name": "Tên mới", "startTime": "HH:MM", "duration": 60}
            |- PHẢI có "id" từ CONTEXT đã cung cấp
            |- Chỉ bao gồm các field cần thay đổi (không cần tất cả)
            |- Fields có thể cập nhật: name, startTime, duration, date, label, color
            |
            |VÍ DỤ UPDATE_SCHEDULE:
            |Request: "đổi lịch học IELTS sang 10h"
            |Context: [HÔM NAY] - ID: abc-123, Tên: Học IELTS, Giờ: 09:00
            |→ {"intent": "Đổi giờ học IELTS", "actions": [{"type": "UPDATE_SCHEDULE", "id": "abc-123", "name": "Học IELTS", "startTime": "10:00"}], "impact": "Cập nhật 1 lịch trình"}
            |
            |Request: "đổi tên Tập gym thành Tập Yoga"
            |Context: - ID: def-456, Tên: Tập gym, Giờ: 07:00
            |→ {"intent": "Đổi tên hoạt động", "actions": [{"type": "UPDATE_SCHEDULE", "id": "def-456", "name": "Tập Yoga"}], "impact": "Cập nhật 1 lịch trình"}
            |
            |Request: "sửa thời gian học thành 2 tiếng"
            |Context: - ID: ghi-789, Tên: Học bài, Giờ: 14:00, (lặp: daily)
            |→ {"intent": "Tăng thời gian học", "actions": [{"type": "UPDATE_SCHEDULE", "id": "ghi-789", "name": "Học bài", "duration": 120}], "impact": "Cập nhật 1 lịch trình"}
            |
            |=== XÓA LỊCH RECURRING (daily/weekly) ===
            |
            |1. SKIP_INSTANCE: Dùng khi XÓA/BỎ QUA cho MỘT NGÀY CỤ THỂ
            |   - Khi lịch có (lặp: daily) hoặc (lặp: weekly)
            |   - Và người dùng muốn xóa "hôm nay" hoặc ngày cụ thể
            |   - Format: {"type": "SKIP_INSTANCE", "id": "uuid", "name": "Tên", "date": "YYYY-MM-DD"}
            |
            |2. DELETE_SCHEDULE: Dùng khi XÓA HOÀN TOÀN chuỗi lịch
            |   - Khi lịch KHÔNG lặp (repeat: once)
            |   - HOẶC khi người dùng nói rõ "xóa hoàn toàn", "xóa vĩnh viễn"
            |   - Format: {"type": "DELETE_SCHEDULE", "id": "uuid", "name": "Tên"}
            |
            |QUAN TRỌNG:
            |- CHỈ trả về JSON, không thêm text giải thích
            |- LUÔN bao gồm "id" từ CONTEXT khi UPDATE/DELETE/SKIP_INSTANCE
            |- LUÔN gán label và color phù hợp khi CREATE_SCHEDULE
            """.trimMargin()
        }

        return Content(
            role = "user",
            parts = listOf(Part(text = text))
        )
    }

    private val agentModeInstruction_Legacy = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lập kế hoạch.
                |Chế độ: AGENT (đề xuất hành động)
                |
                |Khi người dùng yêu cầu hành động, trả về JSON với format CHÍNH XÁC:
                |{
                |  "intent": "Mô tả ngắn gọn ý định của người dùng",
                |  "actions": [
                |    {"type": "...", ...}
                |  ],
                |  "impact": "Tóm tắt ảnh hưởng (VD: Thêm 3 công việc mới)"
                |}
                |
                |Các type hợp lệ: CREATE_SCHEDULE, UPDATE_SCHEDULE, DELETE_SCHEDULE, SKIP_INSTANCE
                |
                |=== CREATE_SCHEDULE ===
                |Format: {"type": "CREATE_SCHEDULE", "name": "Tên", "startTime": "HH:MM", "duration": 60, "date": "YYYY-MM-DD", "label": "...", "color": "..."}
                |
                |LABEL (icon) - CHỌN ĐÚNG loại phù hợp với hoạt động:
                |  - wakeup: Thức dậy, alarm, báo thức
                |  - eat: Ăn uống, bữa sáng/trưa/tối, meal
                |  - exercise: Tập luyện, gym, thể dục, chạy bộ, yoga
                |  - rest: Nghỉ ngơi, thư giãn, break, meditation
                |  - water: Uống nước, trà, cà phê
                |  - book: Học tập, đọc sách, làm việc, meeting, coding
                |  - sleep: Đi ngủ, ngủ
                |  - clean: Dọn dẹp, vệ sinh, giặt giũ
                |  - cook: Nấu ăn, làm bếp
                |  - garden: Làm vườn, trồng cây
                |
                |COLOR (hex) - CHỌN màu phù hợp với loại hoạt động:
                |  - #FF3B30 (đỏ): exercise, thể thao
                |  - #FF9500 (cam): wakeup, báo thức
                |  - #FFCC00 (vàng): eat, ăn uống
                |  - #34C759 (xanh lá): clean, garden
                |  - #00C7BE (teal): water, uống nước
                |  - #007AFF (xanh dương): book, học tập, làm việc
                |  - #5856D6 (tím đậm): sleep
                |  - #AF52DE (tím): rest, nghỉ ngơi
                |  - #FF2D55 (hồng): cook, nấu ăn
                |  - #A2845E (nâu): garden
                |
                |VÍ DỤ CREATE_SCHEDULE:
                |Request: "thêm lịch học IELTS lúc 9h sáng mai, 2 tiếng"
                |→ {"actions": [{"type": "CREATE_SCHEDULE", "name": "Học IELTS", "startTime": "09:00", "duration": 120, "date": "2026-01-26", "label": "book", "color": "#007AFF"}]}
                |
                |Request: "tạo lịch tập gym 7h sáng"
                |→ {"actions": [{"type": "CREATE_SCHEDULE", "name": "Tập gym", "startTime": "07:00", "duration": 60, "label": "exercise", "color": "#FF3B30"}]}
                |
                |=== UPDATE_SCHEDULE ===
                |Format: {"type": "UPDATE_SCHEDULE", "id": "uuid-từ-context", "name": "Tên mới", "startTime": "HH:MM", "duration": 60}
                |- PHẢI có "id" từ CONTEXT đã cung cấp
                |- Chỉ bao gồm các field cần thay đổi (không cần tất cả)
                |- Fields có thể cập nhật: name, startTime, duration, date, label, color
                |
                |VÍ DỤ UPDATE_SCHEDULE:
                |Request: "đổi lịch học IELTS sang 10h"
                |Context: [HÔM NAY] - ID: abc-123, Tên: Học IELTS, Giờ: 09:00
                |→ {"intent": "Đổi giờ học IELTS", "actions": [{"type": "UPDATE_SCHEDULE", "id": "abc-123", "name": "Học IELTS", "startTime": "10:00"}], "impact": "Cập nhật 1 lịch trình"}
                |
                |Request: "đổi tên Tập gym thành Tập Yoga"
                |Context: - ID: def-456, Tên: Tập gym, Giờ: 07:00
                |→ {"intent": "Đổi tên hoạt động", "actions": [{"type": "UPDATE_SCHEDULE", "id": "def-456", "name": "Tập Yoga"}], "impact": "Cập nhật 1 lịch trình"}
                |
                |Request: "sửa thời gian học thành 2 tiếng"
                |Context: - ID: ghi-789, Tên: Học bài, Giờ: 14:00, (lặp: daily)
                |→ {"intent": "Tăng thời gian học", "actions": [{"type": "UPDATE_SCHEDULE", "id": "ghi-789", "name": "Học bài", "duration": 120}], "impact": "Cập nhật 1 lịch trình"}
                |
                |=== XÓA LỊCH RECURRING (daily/weekly) ===
                |
                |1. SKIP_INSTANCE: Dùng khi XÓA/BỎ QUA cho MỘT NGÀY CỤ THỂ
                |   - Khi lịch có (lặp: daily) hoặc (lặp: weekly)
                |   - Và người dùng muốn xóa "hôm nay" hoặc ngày cụ thể
                |   - Format: {"type": "SKIP_INSTANCE", "id": "uuid", "name": "Tên", "date": "YYYY-MM-DD"}
                |
                |2. DELETE_SCHEDULE: Dùng khi XÓA HOÀN TOÀN chuỗi lịch
                |   - Khi lịch KHÔNG lặp (repeat: once)
                |   - HOẶC khi người dùng nói rõ "xóa hoàn toàn", "xóa vĩnh viễn"
                |   - Format: {"type": "DELETE_SCHEDULE", "id": "uuid", "name": "Tên"}
                |
                |QUAN TRỌNG:
                |- CHỈ trả về JSON, không thêm text giải thích
                |- LUÔN bao gồm "id" từ CONTEXT khi UPDATE/DELETE/SKIP_INSTANCE
                |- LUÔN gán label và color phù hợp khi CREATE_SCHEDULE
                """.trimMargin()
            )
        )
    )
    
    // Life Planner Mode: Creates long-term plans with milestones
    private fun getLifePlannerInstruction(): Content {
        val lang = UserProfileCache.getLanguage() ?: "vi"
        val text = if (lang == "en") {
             """You are Tiramisu AI, a life planning assistant.
            |Mode: LIFE PLANNER (Long-term planning)
            |
            |When user shares a goal, analyze it and return JSON:
            |{
            |  "planTitle": "Short plan title",
            |  "description": "Detailed description",
            |  "durationWeeks": number_of_weeks,
            |  "hoursPerWeek": hours_per_week,
            |  "milestones": [
            |    {
            |      "title": "Milestone title",
            |      "week": week_number,
            |      "tasks": [
            |        {"title": "Task name", "dayOfWeek": "monday", "time": "09:00", "duration": 60, "label": "book"}
            |      ]
            |    }
            |  ],
            |  "tips": ["Tip 1", "Tip 2"],
            |  "warnings": ["Warning if plan is too intense"]
            |}
            |
            |LABELS: wakeup, eat, exercise, rest, water, book, sleep, clean, cook, garden
            |
            |RULES:
            |1. Divide goal into 3-5 clear, measurable milestones
            |2. Distribute tasks evenly (max 3 tasks/day)
            |3. Use CORRECT label
            |4. Suggest appropriate times (Morning for focus, Afternoon for practice)
            |5. Include rest days
            |6. Warning if target is unrealistic
            |7. dayOfWeek must be: monday, tuesday, wednesday, thursday, friday, saturday, sunday
            |8. Start from TODAY if date not specified
            |9. RETURN ONLY JSON
            """.trimMargin()
        } else {
            """Bạn là Tiramisu AI, trợ lý lên kế hoạch cuộc sống.
            |Chế độ: LIFE PLANNER (lên kế hoạch dài hạn)
            |
            |Khi người dùng chia sẻ mục tiêu, phân tích và trả về JSON:
            |{
            |  "planTitle": "Tên kế hoạch ngắn gọn",
            |  "description": "Mô tả chi tiết mục tiêu",
            |  "durationWeeks": số_tuần,
            |  "hoursPerWeek": số_giờ_mỗi_tuần,
            |  "milestones": [
            |    {
            |      "title": "Tên milestone",
            |      "week": số_tuần,
            |      "tasks": [
            |        {"title": "Tên task", "dayOfWeek": "monday", "time": "09:00", "duration": 60, "label": "book"}
            |      ]
            |    }
            |  ],
            |  "tips": ["Lời khuyên 1", "Lời khuyên 2"],
            |  "warnings": ["Cảnh báo nếu lịch quá nặng"]
            |}
            |
            |LABELS HỢP LỆ - CHỈ ĐƯỢC DÙNG CÁC LABEL SAU:
            |  - "wakeup": Thức dậy, báo thức, morning routine
            |  - "eat": Ăn uống, bữa ăn, ăn sáng, ăn trưa, ăn tối
            |  - "exercise": Tập thể dục, gym, chạy bộ, yoga, workout
            |  - "rest": Nghỉ ngơi, thư giãn, relaxation
            |  - "water": Uống nước, hydration
            |  - "book": Học tập, đọc sách, nghiên cứu, ôn bài, lập trình, coding, học ngôn ngữ
            |  - "sleep": Ngủ, đi ngủ, nghỉ trưa
            |  - "clean": Dọn dẹp, vệ sinh, làm sạch
            |  - "cook": Nấu ăn, chuẩn bị bữa ăn
            |  - "garden": Làm vườn, chăm sóc cây
            |
            |QUY TẮC QUAN TRỌNG:
            |1. Chia goal thành 3-5 milestones rõ ràng, có thể đo lường
            |2. Phân bổ tasks đều trong tuần, KHÔNG quá 3 tasks/ngày
            |3. PHẢI chọn đúng label phù hợp với nội dung task từ danh sách trên
            |4. Đề xuất thời điểm phù hợp:
            |   - Sáng (8-11h): Việc khó, học bài mới
            |   - Chiều (14-17h): Luyện tập, làm bài
            |   - Tối (19-21h): Review, ôn lại
            |5. Luôn có ít nhất 1 ngày nghỉ/tuần
            |6. Cảnh báo nếu target không realistic
            |7. dayOfWeek phải là: monday, tuesday, wednesday, thursday, friday, saturday, sunday
            |8. NẾU người dùng KHÔNG nói ngày bắt đầu, mặc định bắt đầu từ HÔM NAY
            |9. CHỈ trả về JSON, không thêm text
            |
            |VÍ DỤ:
            |Input: "Học React trong 4 tuần"
            |Output:
            |{
            |  "planTitle": "Học React",
            |  "description": "Kế hoạch học React từ cơ bản đến nâng cao trong 4 tuần",
            |  "durationWeeks": 4,
            |  "hoursPerWeek": 10,
            |  "milestones": [
            |    {
            |      "title": "React Fundamentals",
            |      "week": 1,
            |      "tasks": [
            |        {"title": "Học Components & JSX", "dayOfWeek": "monday", "time": "09:00", "duration": 60, "label": "book"},
            |        {"title": "Học State & Props", "dayOfWeek": "wednesday", "time": "09:00", "duration": 60, "label": "book"}
            |      ]
            |    },
            |    {
            |      "title": "React Hooks",
            |      "week": 2,
            |      "tasks": [
            |        {"title": "Học useState & useEffect", "dayOfWeek": "tuesday", "time": "14:00", "duration": 90, "label": "book"}
            |      ]
            |    }
            |  ],
            |  "tips": ["Code nhiều hơn đọc", "Làm dự án thực hành"],
            |  "warnings": []
            |}
            """.trimMargin()
        }
        
        return Content(
            role = "user",
            parts = listOf(Part(text = text))
        )
    }

    private val lifePlannerInstruction_Legacy = Content(
        role = "user",
        parts = listOf(
            Part(
                text = """Bạn là Tiramisu AI, trợ lý lên kế hoạch cuộc sống.
                |Chế độ: LIFE PLANNER (lên kế hoạch dài hạn)
                |
                |Khi người dùng chia sẻ mục tiêu, phân tích và trả về JSON:
                |{
                |  "planTitle": "Tên kế hoạch ngắn gọn",
                |  "description": "Mô tả chi tiết mục tiêu",
                |  "durationWeeks": số_tuần,
                |  "hoursPerWeek": số_giờ_mỗi_tuần,
                |  "milestones": [
                |    {
                |      "title": "Tên milestone",
                |      "week": số_tuần,
                |      "tasks": [
                |        {"title": "Tên task", "dayOfWeek": "monday", "time": "09:00", "duration": 60, "label": "book"}
                |      ]
                |    }
                |  ],
                |  "tips": ["Lời khuyên 1", "Lời khuyên 2"],
                |  "warnings": ["Cảnh báo nếu lịch quá nặng"]
                |}
                |
                |LABELS HỢP LỆ - CHỈ ĐƯỢC DÙNG CÁC LABEL SAU:
                |  - "wakeup": Thức dậy, báo thức, morning routine
                |  - "eat": Ăn uống, bữa ăn, ăn sáng, ăn trưa, ăn tối
                |  - "exercise": Tập thể dục, gym, chạy bộ, yoga, workout
                |  - "rest": Nghỉ ngơi, thư giãn, relaxation
                |  - "water": Uống nước, hydration
                |  - "book": Học tập, đọc sách, nghiên cứu, ôn bài, lập trình, coding, học ngôn ngữ
                |  - "sleep": Ngủ, đi ngủ, nghỉ trưa
                |  - "clean": Dọn dẹp, vệ sinh, làm sạch
                |  - "cook": Nấu ăn, chuẩn bị bữa ăn
                |  - "garden": Làm vườn, chăm sóc cây
                |
                |QUY TẮC QUAN TRỌNG:
                |1. Chia goal thành 3-5 milestones rõ ràng, có thể đo lường
                |2. Phân bổ tasks đều trong tuần, KHÔNG quá 3 tasks/ngày
                |3. PHẢI chọn đúng label phù hợp với nội dung task từ danh sách trên
                |4. Đề xuất thời điểm phù hợp:
                |   - Sáng (8-11h): Việc khó, học bài mới
                |   - Chiều (14-17h): Luyện tập, làm bài
                |   - Tối (19-21h): Review, ôn lại
                |5. Luôn có ít nhất 1 ngày nghỉ/tuần
                |6. Cảnh báo nếu target không realistic
                |7. dayOfWeek phải là: monday, tuesday, wednesday, thursday, friday, saturday, sunday
                |8. NẾU người dùng KHÔNG nói ngày bắt đầu, mặc định bắt đầu từ HÔM NAY
                |9. CHỈ trả về JSON, không thêm text
                |
                |VÍ DỤ:
                |Input: "Học React trong 4 tuần"
                |Output:
                |{
                |  "planTitle": "Học React",
                |  "description": "Kế hoạch học React từ cơ bản đến nâng cao trong 4 tuần",
                |  "durationWeeks": 4,
                |  "hoursPerWeek": 10,
                |  "milestones": [
                |    {
                |      "title": "React Fundamentals",
                |      "week": 1,
                |      "tasks": [
                |        {"title": "Học Components & JSX", "dayOfWeek": "monday", "time": "09:00", "duration": 60, "label": "book"},
                |        {"title": "Học State & Props", "dayOfWeek": "wednesday", "time": "09:00", "duration": 60, "label": "book"}
                |      ]
                |    },
                |    {
                |      "title": "React Hooks",
                |      "week": 2,
                |      "tasks": [
                |        {"title": "Học useState & useEffect", "dayOfWeek": "tuesday", "time": "14:00", "duration": 90, "label": "book"}
                |      ]
                |    }
                |  ],
                |  "tips": ["Code nhiều hơn đọc", "Làm dự án thực hành"],
                |  "warnings": []
                |}
                """.trimMargin()
            )
        )
    )
    
    // ============================================
    // VOICE COMMAND PARSING (STATELESS)
    // ============================================
    
    /**
     * Helper function to execute API calls with automatic retry on rate limit
     * 
     * @param apiCall Lambda that takes an API key and returns the response
     * @return Result with the response or error
     */
    private suspend fun <T> executeWithRetry(
        apiCall: suspend (apiKey: String) -> T
    ): Result<T> {
        var lastException: Exception? = null
        
        repeat(maxRetries) { attempt ->
            try {
                val apiKey = if (attempt == 0) {
                    apiKeyManager.getCurrentKey()
                } else {
                    apiKeyManager.rotateToNextKey()
                }
                
                val result = apiCall(apiKey)
                return Result.success(result)
                
            } catch (e: Exception) {
                lastException = e
                val errorMessage = e.message?.lowercase() ?: ""
                
                // Check if it's a rate limit error
                val isRateLimitError = errorMessage.contains("429") || 
                                      errorMessage.contains("rate limit") ||
                                      errorMessage.contains("quota exceeded") ||
                                      errorMessage.contains("resource_exhausted")
                
                if (isRateLimitError) {
                    android.util.Log.w("AIRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
                    // Continue to next iteration to try with next key
                } else {
                    // Not a rate limit error, fail immediately
                    android.util.Log.e("AIRepository", "Non-rate-limit error: ${e.message}")
                    return Result.failure(e)
                }
            }
        }
        
        // All retries exhausted
        return Result.failure(
            lastException ?: Exception("All API keys exhausted due to rate limits")
        )
    }
    
    /**
     * Parse voice command to JSON - stateless, no conversation history
     * Lower temperature for more consistent JSON output
     */
    suspend fun parseVoiceCommand(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        executeWithRetry { apiKey ->
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                systemInstruction = Content(
                    role = "user",
                    parts = listOf(
                        Part(text = """
                            |Bạn là JSON parser. CHỈ trả về JSON, KHÔNG có text khác.
                            |Output phải là valid JSON object bắt đầu bằng { và kết thúc bằng }
                        """.trimMargin())
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,  // Lower temperature for consistent output
                    maxOutputTokens = 512
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")
        }
    }
    
    // ============================================
    // ASK MODE METHODS
    // ============================================
    
    /**
     * Send message in Ask Mode (Q&A only)
     */
    suspend fun sendAskModeMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        val userContent = Content(
            role = "user",
            parts = listOf(Part(text = message))
        )
        conversationHistory.add(userContent)
        
        val result = executeWithRetry { apiKey ->
            val request = GeminiRequest(
                contents = conversationHistory.toList(),
                systemInstruction = getAskModeInstruction(),
                generationConfig = GenerationConfig(
                    temperature = 0.7f,
                    maxOutputTokens = 2048
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")
        }
        
        result.onSuccess { responseText ->
            val aiContent = Content(
                role = "model",
                parts = listOf(Part(text = responseText))
            )
            conversationHistory.add(aiContent)
        }.onFailure {
            // Remove user message on failure
            if (conversationHistory.isNotEmpty()) {
                conversationHistory.removeAt(conversationHistory.size - 1)
            }
        }
        
        result
    }
    
    // ============================================
    // AGENT MODE METHODS
    // ============================================
    
    /**
     * Sealed class for Agent mode responses
     * Can be either a structured proposal or just a text response
     */
    sealed class AgentResponse {
        data class Proposal(val proposal: AgentProposal) : AgentResponse()
        data class TextOnly(val text: String) : AgentResponse()
    }
    
    /**
     * Request a proposal from AI (dry-run, no DB writes)
     * Returns either a structured proposal or plain text response
     */
    suspend fun requestProposal(message: String): Result<AgentResponse> = withContext(Dispatchers.IO) {
        try {
            // Build context with user's current schedules for today
            val scheduleContext = buildScheduleContext()
            
            // DEBUG: Log the context being sent
            android.util.Log.d("AIRepository", "Schedule context: $scheduleContext")
            android.util.Log.d("AIRepository", "userId: $userId, repo: ${scheduleRepository != null}")
            
            val contextMessage = if (scheduleContext.isNotBlank()) {
                """$message

[CONTEXT - Lịch trình hiện tại của người dùng]
$scheduleContext
[END CONTEXT]"""
            } else {
                // If no context, still inform AI
                """$message

[CONTEXT - Lịch trình hiện tại của người dùng]
Không thể tải lịch trình. Vui lòng thử lại.
[END CONTEXT]"""
            }
            
            android.util.Log.d("AIRepository", "Final message to AI: $contextMessage")
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = contextMessage))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = getAgentModeInstruction(),
                    generationConfig = GenerationConfig(
                        temperature = 0.5f,  // Lower for structured output
                        maxOutputTokens = 4096
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("AIRepository", "Error in requestProposal", e)
                return@withContext Result.failure(e)
            }
            
            android.util.Log.d("AIRepository", "AI response: $responseText")
            
            // Try to parse JSON response into AgentProposal
            val proposal = parseProposal(responseText)
            
            if (proposal != null && proposal.actions.isNotEmpty()) {
                // Successfully parsed a proposal with actions
                Result.success(AgentResponse.Proposal(proposal))
            } else {
                // AI responded with text (not an action request)
                Result.success(AgentResponse.TextOnly(responseText))
            }
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error in requestProposal", e)
            Result.failure(e)
        }
    }
    
    /**
     * Build context string with user's current schedules
     * This allows AI to reference actual schedule IDs for delete/update
     */
    private suspend fun buildScheduleContext(): String {
        if (scheduleRepository == null || userId == null) return ""
        
        return try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val allSchedules = scheduleRepository.getAllSchedules(userId)
            
            if (allSchedules.isEmpty()) return "Không có lịch trình nào."
            
            val scheduleLines = allSchedules.map { schedule ->
                // Parse date/time from startTimeDate
                // Format can be: "2025-12-21T07:00:00+07:00" or "2025-12-21 07:00:00+07"
                val dateTimeStr = schedule.startTimeDate
                val originalDate = try {
                    if (dateTimeStr.contains("T")) {
                        dateTimeStr.substringBefore("T")
                    } else {
                        dateTimeStr.split(" ").firstOrNull() ?: "không xác định"
                    }
                } catch (e: Exception) {
                    "không xác định"
                }
                
                val timeInfo = try {
                    if (dateTimeStr.contains("T")) {
                        dateTimeStr.substringAfter("T").take(5)
                    } else {
                        dateTimeStr.split(" ").getOrNull(1)?.take(5) ?: "?"
                    }
                } catch (e: Exception) {
                    "?"
                }
                
                // Check repeat type to determine if applies to today
                val repeatType = schedule.repeat.name
                val appliesToday = when (repeatType) {
                    "daily" -> true
                    "weekly" -> true  // Simplified - could check day of week
                    "once" -> originalDate == today
                    else -> true
                }
                
                val todayMarker = if (appliesToday) "[HÔM NAY] " else ""
                val repeatInfo = if (repeatType != "once") "(lặp: $repeatType)" else ""
                
                "$todayMarker- ID: ${schedule.id}, Tên: ${schedule.name}, Giờ: $timeInfo, $repeatInfo"
            }
            
            """Hôm nay: $today
LƯU Ý: Schedule có lặp (daily/weekly) SẼ xuất hiện hôm nay dù ngày tạo gốc khác.
Danh sách lịch trình:
${scheduleLines.joinToString("\n")}"""
        } catch (e: Exception) {
            ""
        }
    }
    
    /**
     * Parse AI response into AgentProposal
     */
    private fun parseProposal(responseText: String): AgentProposal? {
        return try {
            android.util.Log.d("AIRepository", "parseProposal input: $responseText")
            
            // Find JSON object in response
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            android.util.Log.d("AIRepository", "JSON range: $jsonStart to $jsonEnd")
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                android.util.Log.w("AIRepository", "No JSON found in response")
                return null
            }
            
            val jsonString = responseText.substring(jsonStart, jsonEnd)
            android.util.Log.d("AIRepository", "JSON extracted: $jsonString")
            
            val json = JSONObject(jsonString)
            
            val intent = json.optString("intent", "Thực hiện yêu cầu của bạn")
            val impact = json.optString("impact", "Thay đổi lịch trình")
            
            val actionsArray = json.optJSONArray("actions") ?: JSONArray()
            android.util.Log.d("AIRepository", "Actions count: ${actionsArray.length()}")
            
            val actions = mutableListOf<ProposedAction>()
            
            for (i in 0 until actionsArray.length()) {
                val actionJson = actionsArray.getJSONObject(i)
                val typeStr = actionJson.optString("type", "CREATE_SCHEDULE")
                val type = try {
                    ActionType.valueOf(typeStr)
                } catch (e: Exception) {
                    android.util.Log.w("AIRepository", "Unknown action type: $typeStr")
                    ActionType.CREATE_SCHEDULE
                }
                
                val data = mutableMapOf<String, Any?>()
                actionJson.keys().forEach { key ->
                    if (key != "type") {
                        data[key] = actionJson.opt(key)
                    }
                }
                
                val name = actionJson.optString("name", "Công việc")
                android.util.Log.d("AIRepository", "Parsed action: type=$type, name=$name, data=$data")
                actions.add(ProposedAction(
                    type = type,
                    description = name,
                    data = data
                ))
            }
            
            android.util.Log.d("AIRepository", "Proposal parsed successfully with ${actions.size} actions")
            
            AgentProposal(
                intent = intent,
                actions = actions,
                impact = impact,
                rawResponse = responseText
            )
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error parsing proposal", e)
            null
        }
    }
    
    /**
     * Execute a proposal (Step 2 of Agent Mode flow)
     * This is where database writes happen
     */
    suspend fun executeProposal(proposal: AgentProposal): Result<ExecutionResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val appliedChanges = mutableListOf<String>()
        
        // Validate that we have the required dependencies
        if (scheduleRepository == null || userId == null) {
            return@withContext Result.failure(
                IllegalStateException("ScheduleRepository or userId not configured for Agent Mode")
            )
        }
        
        try {
            for (action in proposal.actions) {
                when (action.type) {
                    ActionType.CREATE_SCHEDULE -> {
                        val scheduleData = action.getScheduleData()
                        if (scheduleData != null) {
                            // Parse date or use today
                            val dateStr = scheduleData.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            
                            // Build start_time_date as ISO timestamp
                            // Format: "2025-12-21T07:00:00+07:00"
                            val startTimeDate = "${dateStr}T${scheduleData.startTime}:00+07:00"
                            
                            // Convert duration to interval format (HH:MM:SS)
                            val hours = scheduleData.durationMinutes / 60
                            val minutes = scheduleData.durationMinutes % 60
                            val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                            
                            // Ưu tiên label/color từ AI JSON, fallback về ActivityClassifier
                            val aiLabel = action.data["label"] as? String
                            val aiColor = action.data["color"] as? String
                            
                            val (fallbackLabel, fallbackColor) = ActivityClassifier.classifyWithColor(scheduleData.name)
                            
                            val finalLabel = if (!aiLabel.isNullOrBlank()) aiLabel else fallbackLabel.name
                            val finalColor = if (!aiColor.isNullOrBlank() && aiColor.startsWith("#")) aiColor else fallbackColor
                            
                            // Build schedule row matching database schema
                            val row = mapOf(
                                "user_id" to userId,
                                "name_schedule" to scheduleData.name,
                                "start_time_date" to startTimeDate,
                                "implementation_time" to implementationTime,
                                "repeat" to "once",  // Default repeat type
                                "label" to finalLabel,  // AI-assigned or auto-classified icon
                                "color" to finalColor,  // AI-assigned or auto-classified color
                                "source" to "ai"        // Mark as AI-generated
                            )
                            
                            // Actually insert to database
                            val inserted = scheduleRepository.insertSchedule(row)
                            appliedChanges.add("✅ Tạo: ${scheduleData.name} (${scheduleData.startTime}) - ID: ${inserted.id}")
                        }
                    }
                    ActionType.UPDATE_SCHEDULE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        if (taskId != null) {
                            val dbFields = mutableMapOf<String, Any?>()
                            
                            // Map AI field names to database column names
                            action.data.forEach { (key, value) ->
                                when (key) {
                                    "name" -> dbFields["name_schedule"] = value
                                    "startTime" -> {
                                        // Need to build full datetime - get existing date or use today
                                        val time = value as? String ?: return@forEach
                                        val date = action.data["date"] as? String 
                                            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        dbFields["start_time_date"] = "${date}T${time}:00+07:00"
                                    }
                                    "duration" -> {
                                        // Convert minutes to HH:MM:SS format
                                        val minutes = (value as? Number)?.toInt() ?: return@forEach
                                        val hours = minutes / 60
                                        val mins = minutes % 60
                                        dbFields["implementation_time"] = String.format("%02d:%02d:00", hours, mins)
                                    }
                                    "label" -> dbFields["label"] = value
                                    "color" -> dbFields["color"] = value
                                    "date" -> {
                                        // Date change - need to update start_time_date
                                        // Only process if startTime wasn't already handled
                                        if (!dbFields.containsKey("start_time_date")) {
                                            val newDate = value as? String ?: return@forEach
                                            // Use a default time if only date is changing
                                            // The AI should provide startTime when changing date
                                            dbFields["start_time_date"] = "${newDate}T00:00:00+07:00"
                                        }
                                    }
                                    // Skip id/taskId and unknown fields
                                    "id", "taskId" -> { }
                                    else -> { 
                                        android.util.Log.d("AIRepository", "Unknown update field: $key")
                                    }
                                }
                            }
                            
                            if (dbFields.isNotEmpty()) {
                                android.util.Log.d("AIRepository", "Updating schedule $taskId with: $dbFields")
                                scheduleRepository.updateSchedule(taskId, dbFields)
                                appliedChanges.add("✅ Cập nhật: ${action.description}")
                            } else {
                                appliedChanges.add("⚠️ Không có field hợp lệ để cập nhật: ${action.description}")
                            }
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua cập nhật (thiếu ID): ${action.description}")
                        }
                    }
                    ActionType.DELETE_SCHEDULE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        if (taskId != null) {
                            scheduleRepository.deleteSchedule(taskId)
                            appliedChanges.add("✅ Xóa hoàn toàn: ${action.description}")
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua xóa (thiếu ID): ${action.description}")
                        }
                    }
                    ActionType.SKIP_INSTANCE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        val date = action.data["date"] as? String 
                            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                        
                        if (taskId != null) {
                            // Mark this instance as "delete" status for specific date
                            scheduleRepository.upsertScheduleItem(
                                taskId = taskId,
                                date = date,
                                status = com.projectapp.tempus.data.schedule.dto.StatusType.delete
                            )
                            appliedChanges.add("✅ Bỏ qua ngày $date: ${action.description}")
                        } else {
                            appliedChanges.add("⚠️ Bỏ qua (thiếu ID): ${action.description}")
                        }
                    }
                    else -> {
                        appliedChanges.add("ℹ️ ${action.description}")
                    }
                }
            }
            
            Result.success(ExecutionResult(
                success = true,
                changesApplied = appliedChanges,
                executionTimeMs = System.currentTimeMillis() - startTime
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    // ============================================
    // LEGACY METHODS (for backward compatibility)
    // ============================================
    
    /**
     * Send a single message (legacy - uses Ask Mode)
     */
    suspend fun sendMessage(message: String): Result<String> = sendAskModeMessage(message)
    
    /**
     * Request schedule suggestions (legacy)
     */
    suspend fun requestScheduleSuggestions(userRequest: String): Result<String> = withContext(Dispatchers.IO) {
        requestProposal(userRequest).map { response ->
            when (response) {
                is AgentResponse.Proposal -> response.proposal.rawResponse
                is AgentResponse.TextOnly -> response.text
            }
        }
    }
    
    /**
     * Clear conversation history
     */
    fun clearHistory() {
        conversationHistory.clear()
    }
    
    /**
     * Get current conversation history
     */
    fun getHistory(): List<ChatMessage> {
        return conversationHistory.map { content ->
            ChatMessage(
                text = content.parts.firstOrNull()?.text ?: "",
                isFromUser = content.role == "user"
            )
        }
    }
    
    // ============================================
    // LIFE PLANNER MODE METHODS
    // ============================================
    
    /**
     * Request a life plan from AI based on user's goal
     * Returns a structured LifePlanProposal for user approval
     */
    suspend fun requestLifePlan(
        goal: String,
        energyContext: EnergyContext? = null
    ): Result<LifePlanProposal> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            
            // Build context message with energy preferences if available
            val contextPart = energyContext?.let {
                """
                |
                |[THÔNG TIN NGƯỜI DÙNG]
                |${it.toContextString()}
                """.trimMargin()
            } ?: ""
            
            val fullMessage = """
                |Ngày hôm nay: $today
                |
                |Mục tiêu: $goal
                |$contextPart
                |
                |Hãy tạo kế hoạch chi tiết cho mục tiêu này.
            """.trimMargin()
            
            android.util.Log.d("AIRepository", "Requesting life plan for: $goal")
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = fullMessage))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    systemInstruction = getLifePlannerInstruction(),
                    generationConfig = GenerationConfig(
                        temperature = 0.6f,
                        maxOutputTokens = 4096  // Longer for detailed plans
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("AIRepository", "Error in requestLifePlan", e)
                return@withContext Result.failure(e)
            }
            
            android.util.Log.d("AIRepository", "Life plan response: $responseText")
            
            val proposal = parseLifePlanResponse(responseText, today)
                ?: return@withContext Result.failure(Exception("Could not parse life plan response"))
            
            Result.success(proposal)
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error in requestLifePlan", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse AI response into LifePlanProposal
     */
    private fun parseLifePlanResponse(responseText: String, startDate: LocalDate): LifePlanProposal? {
        return try {
            // Find JSON in response
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            
            if (jsonStart == -1 || jsonEnd <= jsonStart) return null
            
            val jsonString = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonString)
            
            val planTitle = json.optString("planTitle", "Kế hoạch mới")
            val description = json.optString("description", "")
            val durationWeeks = json.optInt("durationWeeks", 4)
            val hoursPerWeek = json.optInt("hoursPerWeek", 10)
            
            val milestonesArray = json.optJSONArray("milestones") ?: JSONArray()
            val milestones = mutableListOf<Milestone>()
            var totalTasks = 0
            
            for (i in 0 until milestonesArray.length()) {
                val milestoneJson = milestonesArray.getJSONObject(i)
                val milestoneTitle = milestoneJson.optString("title", "Milestone ${i + 1}")
                val weekNumber = milestoneJson.optInt("week", i + 1)
                val targetDate = startDate.plusWeeks(weekNumber.toLong())
                
                val tasksArray = milestoneJson.optJSONArray("tasks") ?: JSONArray()
                val scheduledTasks = mutableListOf<ScheduledTask>()
                
                for (j in 0 until tasksArray.length()) {
                    val taskJson = tasksArray.getJSONObject(j)
                    val taskTitle = taskJson.optString("title", "Task")
                    val dayOfWeekStr = taskJson.optString("dayOfWeek", "monday").uppercase()
                    val time = taskJson.optString("time", "09:00")
                    val duration = taskJson.optInt("duration", 60)
                    val taskLabel = taskJson.optString("label", "star") // Get label from AI
                    
                    val dayOfWeek = try {
                        DayOfWeek.valueOf(dayOfWeekStr)
                    } catch (e: Exception) {
                        DayOfWeek.MONDAY
                    }
                    
                    scheduledTasks.add(ScheduledTask(
                        title = taskTitle,
                        dayOfWeek = dayOfWeek,
                        preferredTime = time,
                        durationMinutes = duration,
                        label = taskLabel
                    ))
                    totalTasks++
                }
                
                milestones.add(Milestone(
                    title = milestoneTitle,
                    weekNumber = weekNumber,
                    targetDate = targetDate,
                    scheduledTasks = scheduledTasks,
                    status = MilestoneStatus.PENDING
                ))
            }
            
            val tipsArray = json.optJSONArray("tips") ?: JSONArray()
            val tips = (0 until tipsArray.length()).map { tipsArray.optString(it, "") }
            
            val warningsArray = json.optJSONArray("warnings") ?: JSONArray()
            val warnings = (0 until warningsArray.length()).map { warningsArray.optString(it, "") }
            
            val endDate = startDate.plusWeeks(durationWeeks.toLong())
            
            val lifePlan = LifePlan(
                title = planTitle,
                description = description,
                startDate = startDate,
                endDate = endDate,
                milestones = milestones,
                estimatedHoursPerWeek = hoursPerWeek,
                status = PlanStatus.DRAFT,
                tips = tips.filter { it.isNotBlank() },
                warnings = warnings.filter { it.isNotBlank() }
            )
            
            LifePlanProposal(
                plan = lifePlan,
                totalTasksToCreate = totalTasks * durationWeeks, // Rough estimate
                rawResponse = responseText
            )
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error parsing life plan", e)
            null
        }
    }
    
    /**
     * Convert a life plan to actual schedule entries
     * Call this after user approves the plan
     */
    suspend fun executeLifePlan(
        plan: LifePlan
    ): Result<Int> = withContext(Dispatchers.IO) {
        if (scheduleRepository == null || userId == null) {
            return@withContext Result.failure(
                IllegalStateException("ScheduleRepository or userId not configured")
            )
        }
        
        try {
            var schedulesCreated = 0
            val today = LocalDate.now()
            
            for (milestone in plan.milestones) {
                // Track which day in the first week we're scheduling to distribute tasks
                var firstWeekDayOffset = 0
                
                for (task in milestone.scheduledTasks) {
                    // Calculate weeks until milestone
                    val weeksUntilMilestone = milestone.weekNumber - 1
                    
                    // Create task for each week until milestone
                    for (weekOffset in 0..weeksUntilMilestone) {
                        var taskDate: LocalDate
                        
                        if (weekOffset == 0) {
                            // FIRST WEEK: Start from TODAY, distribute tasks across days starting from today
                            // Don't wait for specific dayOfWeek - user wants plan to start TODAY
                            taskDate = today.plusDays(firstWeekDayOffset.toLong())
                            
                            // Increment offset for next task in first week (distribute across days)
                            // Skip to next day for variety, max 7 days
                            firstWeekDayOffset = (firstWeekDayOffset + 1) % 7
                        } else {
                            // SUBSEQUENT WEEKS: Use the AI-specified dayOfWeek
                            // Calculate the base date for this week (relative to today's week)
                            val weekStartDate = today.plusWeeks(weekOffset.toLong())
                            
                            // Find the specified day of week in this week
                            taskDate = weekStartDate
                            // Go back to the start of the week (Monday) first
                            while (taskDate.dayOfWeek != java.time.DayOfWeek.MONDAY) {
                                taskDate = taskDate.minusDays(1)
                            }
                            // Then find the task's day of week
                            while (taskDate.dayOfWeek != task.dayOfWeek) {
                                taskDate = taskDate.plusDays(1)
                            }
                        }
                        
                        // Skip if date is in the past (safety check)
                        if (taskDate.isBefore(today)) continue
                        
                        val startTimeDate = "${taskDate}T${task.preferredTime}:00+07:00"
                        val hours = task.durationMinutes / 60
                        val minutes = task.durationMinutes % 60
                        val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                        
                        // Use label from AI, fallback to infer if empty
                        val label = if (task.label.isNotBlank() && task.label != "star") 
                            task.label 
                        else 
                            inferLabelFromTitle(task.title)
                        
                        val row = mapOf(
                            "user_id" to userId,
                            "name_schedule" to "${task.title} [${plan.title}]",
                            "start_time_date" to startTimeDate,
                            "implementation_time" to implementationTime,
                            "repeat" to "once",
                            "source" to "ai",
                            "label" to label,
                            "color" to inferColorFromLabel(label)
                        )
                        
                        scheduleRepository.insertSchedule(row)
                        schedulesCreated++
                    }
                }
            }
            
            Result.success(schedulesCreated)
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error executing life plan", e)
            Result.failure(e)
        }
    }
    
    /**
     * Infer schedule label from task title using keyword matching
     * Uses project's ScheduleLabel enum values: wakeup, eat, exercise, rest, water, book, sleep, clean, cook, garden
     */
    private fun inferLabelFromTitle(title: String): String {
        val lowerTitle = title.lowercase()
        return when {
            // Wakeup - morning routine
            lowerTitle.contains("thức dậy") || lowerTitle.contains("wake") ||
            lowerTitle.contains("dậy") || lowerTitle.contains("morning") ||
            lowerTitle.contains("báo thức") -> "wakeup"
            
            // Eat - meals
            lowerTitle.contains("ăn") || lowerTitle.contains("eat") ||
            lowerTitle.contains("bữa") || lowerTitle.contains("meal") ||
            lowerTitle.contains("sáng") || lowerTitle.contains("trưa") ||
            lowerTitle.contains("tối") || lowerTitle.contains("breakfast") ||
            lowerTitle.contains("lunch") || lowerTitle.contains("dinner") -> "eat"
            
            // Exercise - physical activity
            lowerTitle.contains("tập") || lowerTitle.contains("gym") ||
            lowerTitle.contains("chạy") || lowerTitle.contains("run") ||
            lowerTitle.contains("yoga") || lowerTitle.contains("thể dục") ||
            lowerTitle.contains("exercise") || lowerTitle.contains("workout") -> "exercise"
            
            // Rest - relaxation
            lowerTitle.contains("nghỉ") || lowerTitle.contains("rest") ||
            lowerTitle.contains("thư giãn") || lowerTitle.contains("relax") -> "rest"
            
            // Water - hydration
            lowerTitle.contains("uống nước") || lowerTitle.contains("water") ||
            lowerTitle.contains("hydrat") -> "water"
            
            // Sleep - sleeping
            lowerTitle.contains("ngủ") || lowerTitle.contains("sleep") ||
            lowerTitle.contains("đi ngủ") -> "sleep"
            
            // Clean - cleaning
            lowerTitle.contains("dọn") || lowerTitle.contains("clean") ||
            lowerTitle.contains("vệ sinh") || lowerTitle.contains("lau") -> "clean"
            
            // Cook - cooking
            lowerTitle.contains("nấu") || lowerTitle.contains("cook") ||
            lowerTitle.contains("chuẩn bị") -> "cook"
            
            // Garden - gardening
            lowerTitle.contains("vườn") || lowerTitle.contains("garden") ||
            lowerTitle.contains("cây") || lowerTitle.contains("plant") -> "garden"
            
            // Book - study/reading/work/coding (default for learning activities)
            lowerTitle.contains("học") || lowerTitle.contains("study") || 
            lowerTitle.contains("ôn") || lowerTitle.contains("đọc") ||
            lowerTitle.contains("nghiên cứu") || lowerTitle.contains("research") ||
            lowerTitle.contains("luyện") || lowerTitle.contains("practice") ||
            lowerTitle.contains("code") || lowerTitle.contains("lập trình") ||
            lowerTitle.contains("làm") || lowerTitle.contains("work") ||
            lowerTitle.contains("tiếng") || lowerTitle.contains("english") -> "book"
            
            // Default - use book for any learning/study task
            else -> "book"
        }
    }
    
    /**
     * Get color for a label based on ScheduleLabel enum
     */
    private fun inferColorFromLabel(label: String): String {
        return when (label) {
            "wakeup" -> "#FF9800"    // Orange
            "eat" -> "#FFC107"       // Amber
            "exercise" -> "#4CAF50"  // Green
            "rest" -> "#9C27B0"      // Purple
            "water" -> "#2196F3"     // Blue
            "book" -> "#3F51B5"      // Indigo
            "sleep" -> "#607D8B"     // Blue Grey
            "clean" -> "#00BCD4"     // Cyan
            "cook" -> "#E91E63"      // Pink
            "garden" -> "#8BC34A"    // Light Green
            else -> "#3F51B5"        // Indigo fallback
        }
    }
}

/**
 * Simple chat message model for UI layer
 */
data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
