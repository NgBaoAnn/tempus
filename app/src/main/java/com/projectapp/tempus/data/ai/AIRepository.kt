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
import java.util.UUID
import com.projectapp.tempus.data.schedule.dto.RepeatType
import com.projectapp.tempus.data.schedule.dto.SourceType
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import io.github.jan.supabase.gotrue.auth

import com.projectapp.tempus.data.user.UserProfileCache
import com.projectapp.tempus.data.ai.context.AIContextManager
import com.projectapp.tempus.data.ai.vector.VectorMemoryRepository
import android.content.Context


class AIRepository(
    private val scheduleRepository: ScheduleRepository? = null,
    private val userId: String? = null,
    private val appContext: Context? = null,
    private val useVectorMemory: Boolean = false  // Feature flag for vector memory
) {
    
    private val geminiService = GeminiClientProvider.service
    private val apiKeyManager = GeminiApiKeyManager
    
    // Advanced context management with sliding window, summarization, and persistence
    private val contextManager: AIContextManager? = appContext?.let { 
        AIContextManager(
            appContext = it,
            maxRecentMessages = 10,
            maxTokens = 8000,
            summarizeThreshold = 8
        )
    }
    
    // Vector memory repository for long-term semantic context
    private val vectorMemoryRepo: VectorMemoryRepository? by lazy {
        if (useVectorMemory && userId != null) {
            VectorMemoryRepository(userId, scheduleRepository)
        } else null
    }
    
    // Fallback for cases where appContext is not provided
    private val conversationHistory = mutableListOf<Content>()
    
    
    private val maxRetries = 8  
    
    
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
                
                
                val isRateLimitError = errorMessage.contains("429") || 
                                      errorMessage.contains("rate limit") ||
                                      errorMessage.contains("quota exceeded") ||
                                      errorMessage.contains("resource_exhausted")
                
                if (isRateLimitError) {
                    android.util.Log.w("AIRepository", "Rate limit hit on attempt ${attempt + 1}, rotating key...")
                    
                } else {
                    
                    android.util.Log.e("AIRepository", "Non-rate-limit error: ${e.message}")
                    return Result.failure(e)
                }
            }
        }
        
        
        return Result.failure(
            lastException ?: Exception("All API keys exhausted due to rate limits")
        )
    }
    
    
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
                    temperature = 0.3f,  
                    maxOutputTokens = 512
                )
            )
            
            val response = geminiService.generateContent(apiKey, request)
            
            response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: throw Exception("Empty response from AI")
        }
    }
    
    
    suspend fun sendAskModeMessage(message: String): Result<String> = withContext(Dispatchers.IO) {
        val userContent = Content(
            role = "user",
            parts = listOf(Part(text = message))
        )
        
        // Use context manager if available, otherwise fallback to simple list
        if (contextManager != null) {
            contextManager.addMessage(userContent)
        } else {
            conversationHistory.add(userContent)
        }
        
        val result = executeWithRetry { apiKey ->
            // Get context from manager (includes summary + profile) or fallback to simple history
            val contents = contextManager?.getContextForRequest() ?: conversationHistory.toList()
            
            val request = GeminiRequest(
                contents = contents,
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
            if (contextManager != null) {
                contextManager.addMessage(aiContent)
            } else {
                conversationHistory.add(aiContent)
            }
        }.onFailure {
            // Rollback on failure - context manager handles this internally
            if (contextManager == null && conversationHistory.isNotEmpty()) {
                conversationHistory.removeAt(conversationHistory.size - 1)
            }
        }
        
        result
    }
    
    
    sealed class AgentResponse {
        data class Proposal(val proposal: AgentProposal) : AgentResponse()
        data class TextOnly(val text: String) : AgentResponse()
    }
    
    
    suspend fun requestProposal(message: String): Result<AgentResponse> = withContext(Dispatchers.IO) {
        try {
            
            val scheduleContext = buildScheduleContext()
            
            
            android.util.Log.d("AIRepository", "Schedule context: $scheduleContext")
            android.util.Log.d("AIRepository", "userId: $userId, repo: ${scheduleRepository != null}")
            
            val contextMessage = if (scheduleContext.isNotBlank()) {
                """$message

[CONTEXT - Lịch trình hiện tại của người dùng]
$scheduleContext
[END CONTEXT]"""
            } else {
                
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
                        temperature = 0.5f,  
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
            
            
            val proposal = parseProposal(responseText)
            
            if (proposal != null && proposal.actions.isNotEmpty()) {
                
                Result.success(AgentResponse.Proposal(proposal))
            } else {
                
                Result.success(AgentResponse.TextOnly(responseText))
            }
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error in requestProposal", e)
            Result.failure(e)
        }
    }
    
    
    private suspend fun buildScheduleContext(): String {
        if (scheduleRepository == null || userId == null) return ""
        
        return try {
            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val allSchedules = scheduleRepository.getAllSchedules(userId)
            
            if (allSchedules.isEmpty()) return "Không có lịch trình nào."
            
            val scheduleLines = allSchedules.map { schedule ->
                
                
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
                
                
                val repeatType = schedule.repeat.name
                val appliesToday = when (repeatType) {
                    "daily" -> true
                    "weekly" -> true  
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
    
    
    private fun parseProposal(responseText: String): AgentProposal? {
        return try {
            android.util.Log.d("AIRepository", "parseProposal input: $responseText")
            
            
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
    
    
    suspend fun executeProposal(proposal: AgentProposal): Result<ExecutionResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        val appliedChanges = mutableListOf<String>()
        
        
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
                            
                            val dateStr = scheduleData.date ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                            
                            
                            val startTimeDate = "${dateStr}T${scheduleData.startTime}:00+07:00"
                            
                            
                            val hours = scheduleData.durationMinutes / 60
                            val minutes = scheduleData.durationMinutes % 60
                            val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                            
                            
                            val aiLabel = action.data["label"] as? String
                            val aiColor = action.data["color"] as? String
                            
                            val (fallbackLabel, fallbackColor) = ActivityClassifier.classifyWithColor(scheduleData.name)
                            
                            val finalLabel = if (!aiLabel.isNullOrBlank()) aiLabel else fallbackLabel.name
                            val finalColor = if (!aiColor.isNullOrBlank() && aiColor.startsWith("#")) aiColor else fallbackColor
                            
                            
                            val row = mapOf(
                                "user_id" to userId,
                                "name_schedule" to scheduleData.name,
                                "start_time_date" to startTimeDate,
                                "implementation_time" to implementationTime,
                                "repeat" to "once",  
                                "label" to finalLabel,  
                                "color" to finalColor,  
                                "source" to "ai"        
                            )
                            
                            
                            val inserted = scheduleRepository.insertSchedule(row)
                            appliedChanges.add("✅ Tạo: ${scheduleData.name} (${scheduleData.startTime}) - ID: ${inserted.id}")
                        }
                    }
                    ActionType.UPDATE_SCHEDULE -> {
                        val taskId = action.data["id"] as? String ?: action.data["taskId"] as? String
                        if (taskId != null) {
                            val dbFields = mutableMapOf<String, Any?>()
                            
                            
                            action.data.forEach { (key, value) ->
                                when (key) {
                                    "name" -> dbFields["name_schedule"] = value
                                    "startTime" -> {
                                        
                                        val time = value as? String ?: return@forEach
                                        val date = action.data["date"] as? String 
                                            ?: LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                                        dbFields["start_time_date"] = "${date}T${time}:00+07:00"
                                    }
                                    "duration" -> {
                                        
                                        val minutes = (value as? Number)?.toInt() ?: return@forEach
                                        val hours = minutes / 60
                                        val mins = minutes % 60
                                        dbFields["implementation_time"] = String.format("%02d:%02d:00", hours, mins)
                                    }
                                    "label" -> dbFields["label"] = value
                                    "color" -> dbFields["color"] = value
                                    "date" -> {
                                        
                                        
                                        if (!dbFields.containsKey("start_time_date")) {
                                            val newDate = value as? String ?: return@forEach
                                            
                                            
                                            dbFields["start_time_date"] = "${newDate}T00:00:00+07:00"
                                        }
                                    }
                                    
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
    
    
    suspend fun sendMessage(message: String): Result<String> = sendAskModeMessage(message)
    
    
    suspend fun requestScheduleSuggestions(userRequest: String): Result<String> = withContext(Dispatchers.IO) {
        requestProposal(userRequest).map { response ->
            when (response) {
                is AgentResponse.Proposal -> response.proposal.rawResponse
                is AgentResponse.TextOnly -> response.text
            }
        }
    }
    
    
    suspend fun generateChatTitle(firstMessage: String): Result<String> = withContext(Dispatchers.IO) {
        val lang = UserProfileCache.getLanguage() ?: "vi"
        val fallbackTitle = if (lang == "en") "New Chat" else "Cuộc trò chuyện mới"
        
        try {
            android.util.Log.d("AIRepository", "Generating chat title for: ${firstMessage.take(50)}...")
            
            val prompt = if (lang == "en") {
                """Generate a very short title (max 5 words, under 30 characters) for a conversation that starts with:
                |"$firstMessage"
                |
                |Rules:
                |- Just return the title, no quotes, no explanation
                |- Be concise and descriptive
                |- Use the same language as the message
                """.trimMargin()
            } else {
                """Tạo một tiêu đề rất ngắn gọn (tối đa 5 từ, dưới 30 ký tự) cho cuộc trò chuyện bắt đầu với:
                |"$firstMessage"
                |
                |Quy tắc:
                |- Chỉ trả về tiêu đề, không dấu ngoặc kép, không giải thích
                |- Ngắn gọn và mô tả được nội dung
                |- Dùng cùng ngôn ngữ với tin nhắn
                """.trimMargin()
            }
            
            val request = GeminiRequest(
                contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.5f,
                    maxOutputTokens = 50
                )
            )
            
            val response = geminiService.generateContent(
                apiKey = apiKeyManager.getCurrentKey(),
                request = request
            )
            
            android.util.Log.d("AIRepository", "Title generation response: candidates=${response.candidates?.size}")
            
            val title = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()
                ?.take(30)
            
            if (title.isNullOrBlank()) {
                android.util.Log.w("AIRepository", "Title generation returned empty, using fallback")
                Result.success(fallbackTitle)
            } else {
                android.util.Log.d("AIRepository", "Generated title: $title")
                Result.success(title)
            }
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Title generation failed: ${e.message}", e)
            Result.success(fallbackTitle)
        }
    }
    
    
    fun clearHistory() {
        contextManager?.clearHistory()
        conversationHistory.clear()
    }
    
    fun getHistory(): List<ChatMessage> {
        val messages = contextManager?.getRecentMessages() ?: conversationHistory
        return messages.map { content ->
            ChatMessage(
                text = content.parts.firstOrNull()?.text ?: "",
                isFromUser = content.role == "user"
            )
        }
    }
    
    // New session management methods
    suspend fun saveSession() {
        contextManager?.saveSession()
    }
    
    suspend fun restoreSession(): Boolean {
        return contextManager?.restoreSession() ?: false
    }
    
    fun startNewSession() {
        contextManager?.startNewSession()
        conversationHistory.clear()
    }
    
    fun getContextStats(): com.projectapp.tempus.data.ai.context.ContextStats? {
        return contextManager?.getStats()
    }
    
    // ============ VECTOR MEMORY METHODS ============
    
    /**
     * Send message with vector context retrieval (if enabled)
     * Falls back to local context management if vector memory fails
     */
    suspend fun sendMessageWithVectorContext(
        message: String,
        mode: String = "ask"
    ): Result<String> {
        // Try vector memory first if enabled
        if (vectorMemoryRepo != null) {
            val result = vectorMemoryRepo!!.sendMessage(message, mode)
            if (result.isSuccess) {
                android.util.Log.d("AIRepository", "Vector memory response received")
                return Result.success(result.getOrThrow().response)
            }
            // Fallback to local on error
            android.util.Log.w("AIRepository", "Vector memory failed, falling back to local")
        }
        
        // Fallback to local context management
        return sendAskModeMessage(message)
    }
    
    /**
     * Sync tasks to vector memory for semantic search
     */
    suspend fun syncToVectorMemory(): Result<Int> {
        return vectorMemoryRepo?.syncTasks() 
            ?: Result.failure(Exception("Vector memory not enabled"))
    }
    
    /**
     * Add a user preference to vector memory
     */
    suspend fun addToVectorMemory(text: String, category: String = "general"): Result<Unit> {
        return vectorMemoryRepo?.addMemory(text, category)
            ?: Result.failure(Exception("Vector memory not enabled"))
    }
    
    /**
     * Clear all vector memory for the user
     */
    suspend fun clearVectorMemory(): Result<Unit> {
        return vectorMemoryRepo?.clearMemory()
            ?: Result.failure(Exception("Vector memory not enabled"))
    }
    
    /**
     * Check if vector memory backend is available
     */
    suspend fun isVectorMemoryAvailable(): Boolean {
        return vectorMemoryRepo?.isAvailable() ?: false
    }
    
    /**
     * Get vector memory statistics
     */
    suspend fun getVectorMemoryStats(): Result<com.projectapp.tempus.data.ai.vector.MemoryStats>? {
        return vectorMemoryRepo?.getStats()
    }
    
    
    suspend fun requestLifePlan(
        goal: String,
        energyContext: EnergyContext? = null
    ): Result<LifePlanProposal> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            
            
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
                        maxOutputTokens = 4096  
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
    
    
    private fun parseLifePlanResponse(responseText: String, startDate: LocalDate): LifePlanProposal? {
        return try {
            
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
                    val taskLabel = taskJson.optString("label", "star") 
                    
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
                totalTasksToCreate = totalTasks * durationWeeks, 
                rawResponse = responseText
            )
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error parsing life plan", e)
            null
        }
    }
    
    
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
                
                var firstWeekDayOffset = 0
                
                for (task in milestone.scheduledTasks) {
                    
                    val weeksUntilMilestone = milestone.weekNumber - 1
                    
                    
                    for (weekOffset in 0..weeksUntilMilestone) {
                        var taskDate: LocalDate
                        
                        if (weekOffset == 0) {
                            
                            
                            taskDate = today.plusDays(firstWeekDayOffset.toLong())
                            
                            
                            firstWeekDayOffset = (firstWeekDayOffset + 1) % 7
                        } else {
                            
                            
                            val weekStartDate = today.plusWeeks(weekOffset.toLong())
                            
                            
                            taskDate = weekStartDate
                            
                            while (taskDate.dayOfWeek != java.time.DayOfWeek.MONDAY) {
                                taskDate = taskDate.minusDays(1)
                            }
                            
                            while (taskDate.dayOfWeek != task.dayOfWeek) {
                                taskDate = taskDate.plusDays(1)
                            }
                        }
                        
                        
                        if (taskDate.isBefore(today)) continue
                        
                        val startTimeDate = "${taskDate}T${task.preferredTime}:00+07:00"
                        val hours = task.durationMinutes / 60
                        val minutes = task.durationMinutes % 60
                        val implementationTime = String.format("%02d:%02d:00", hours, minutes)
                        
                        
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
    
    // ============================================
    // PERSONALIZATION SCHEDULE GENERATION
    // ============================================
    
    /**
     * Data class for personalization task input
     */
    data class PersonalizationTaskInput(
        val name: String,
        val description: String,
        val estimatedMinutes: Int,
        val priority: String // high, medium, low
    )
    
    /**
     * Generate schedule from personalization settings
     * AI will analyze tasks and create optimal time slots based on:
     * - Wake up / sleep time
     * - Task priorities
     * - Estimated durations
     * 
     * @param wakeUpTime User's wake up time (HH:mm)
     * @param sleepTime User's sleep time (HH:mm)
     * @param tasks List of tasks to schedule
     * @param activeDays Days to apply (1=Mon, 7=Sun)
     * @return Result with list of schedule slots for preview (NOT saved yet)
     */
    suspend fun generateSchedulePreview(
        wakeUpTime: String,
        sleepTime: String,
        tasks: List<PersonalizationTaskInput>,
        activeDays: List<Int>
    ): Result<List<ScheduleSlot>> = withContext(Dispatchers.IO) {
        try {
            if (tasks.isEmpty()) {
                return@withContext Result.failure(Exception("Không có công việc nào để lên lịch"))
            }
            
            // Build task list for AI
            val taskListStr = tasks.mapIndexed { index, task ->
                """${index + 1}. "${task.name}" - ${task.estimatedMinutes} phút (${task.priority}) ${if (task.description.isNotEmpty()) "- ${task.description}" else ""} """
            }.joinToString("\n")
            
            val prompt = """
                |THÔNG TIN NGƯỜI DÙNG:
                |- Thức dậy: $wakeUpTime
                |- Đi ngủ: $sleepTime
                |- Ngày áp dụng: ${activeDays.joinToString(", ")}
                |
                |CÁC CÔNG VIỆC CẦN SẮP XẾP:
                |$taskListStr
                |
                |YÊU CẦU:
                |Hãy tạo lịch trình tối ưu cho các công việc trên. Sắp xếp sao cho:
                |1. Công việc ưu tiên cao nên làm vào buổi sáng khi năng lượng cao
                |2. Có thời gian nghỉ ngơi giữa các công việc (15-30 phút)
                |3. Không xếp lịch ngoài giờ thức - ngủ
                |4. Sử dụng đúng thời gian ước tính cho từng công việc
                |5. Thêm thời gian cho bữa ăn (sáng, trưa, tối) nếu cần
                |
                |Trả lời theo JSON format sau (KHÔNG có markdown):
                |{
                |  "schedule": [
                |    {
                |      "name": "Tên công việc",
                |      "start_time": "HH:mm",
                |      "end_time": "HH:mm",
                |      "priority": "high/medium/low",
                |      "label": "book/exercise/rest/cook/clean/garden/eat/water/wakeup/sleep"
                |    }
                |  ]
                |}
            """.trimMargin()
            
            android.util.Log.d("AIRepository", "Generating personalization schedule preview...")
            
            val responseText = executeWithRetry { apiKey ->
                val contents = listOf(
                    Content(
                        role = "user",
                        parts = listOf(Part(text = prompt))
                    )
                )
                
                val request = GeminiRequest(
                    contents = contents,
                    generationConfig = GenerationConfig(
                        temperature = 0.5f,
                        maxOutputTokens = 2048
                    )
                )
                
                val response = geminiService.generateContent(apiKey, request)
                
                response.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()?.text
                    ?: throw Exception("Empty response from AI")
            }.getOrElse { e ->
                android.util.Log.e("AIRepository", "Error in generateSchedulePreview", e)
                return@withContext Result.failure(e)
            }
            
            android.util.Log.d("AIRepository", "Personalization response: $responseText")
            
            // Parse JSON response
            val slots = parseScheduleSlots(responseText)
            if (slots.isEmpty()) {
                return@withContext Result.failure(Exception("AI không thể tạo lịch. Vui lòng thử lại."))
            }
            
            // Return slots for preview - don't save yet
            Result.success(slots)
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error generating schedule preview", e)
            Result.failure(e)
        }
    }
    
    /**
     * Save generated schedule slots to database after user confirmation
     * @param slots List of schedule slots from preview
     * @param activeDays Days to apply (1=Mon, 7=Sun)
     * @return Result with number of schedules created
     */
    suspend fun saveGeneratedSchedulesToDatabase(
        slots: List<ScheduleSlot>,
        activeDays: List<Int>
    ): Result<Int> = withContext(Dispatchers.IO) {
        try {
            val today = LocalDate.now()
            val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id 
                ?: return@withContext Result.failure(Exception("User not logged in"))
            
            var schedulesCreated = 0
            
            for (slot in slots) {
                val label = slot.label.ifEmpty { inferLabelFromTitle(slot.name) }
                val color = inferColorFromLabel(label)
                
                android.util.Log.d("AIRepository", "Processing slot: ${slot.name}, startTime='${slot.startTime}', endTime='${slot.endTime}'")
                
                // Calculate duration
                val startParts = slot.startTime.split(":")
                val endParts = slot.endTime.split(":")
                val startMinutes = startParts.getOrNull(0)?.toIntOrNull() ?: 0
                val startMins = startParts.getOrNull(1)?.toIntOrNull() ?: 0
                val endMinutes = endParts.getOrNull(0)?.toIntOrNull() ?: 0
                val endMins = endParts.getOrNull(1)?.toIntOrNull() ?: 0
                val totalStartMinutes = startMinutes * 60 + startMins
                val totalEndMinutes = endMinutes * 60 + endMins
                val durationMinutes = if (totalEndMinutes > totalStartMinutes) totalEndMinutes - totalStartMinutes else 60
                val durationStr = String.format("%02d:%02d:00", durationMinutes / 60, durationMinutes % 60)
                
                // Format start_time properly as HH:mm:ss
                val startTimeFormatted = if (slot.startTime.contains(":")) {
                    if (slot.startTime.count { it == ':' } == 1) {
                        slot.startTime + ":00"  // HH:mm -> HH:mm:ss
                    } else {
                        slot.startTime  // Already HH:mm:ss
                    }
                } else {
                    "08:00:00"  // Default fallback
                }
                
                android.util.Log.d("AIRepository", "Formatted startTime: $startTimeFormatted, duration: $durationStr")
                
                val priority = when (slot.priority) {
                    "high" -> "high"
                    "low" -> "low"
                    else -> "medium"
                }
                
                // Determine repeat type based on active days
                val repeatType = if (activeDays.size == 7) {
                    RepeatType.daily.name
                } else {
                    RepeatType.custom.name
                }
                
                // Convert active days to repeat_days format: "1,3,5" = Mon, Wed, Fri
                val repeatDays = if (activeDays.size < 7) {
                    activeDays.joinToString(",")
                } else null
                
                // Format start_time_date as full timestamp: "2026-01-31 08:00:00+07"
                // startTimeFormatted is already "HH:mm:ss"
                val systemOffset = java.time.ZoneId.systemDefault().rules.getOffset(java.time.Instant.now())
                val offsetStr = systemOffset.toString().replace(":", "") // "+07:00" -> "+0700"
                val startTimeDateFull = "${today}T${startTimeFormatted}${systemOffset}"
                
                android.util.Log.d("AIRepository", "Full start_time_date: $startTimeDateFull")
                
                val scheduleRow = mapOf(
                    "id" to UUID.randomUUID().toString(),
                    "user_id" to userId,
                    "name_schedule" to slot.name,
                    "description" to "",
                    "priority" to priority,
                    "label" to label,
                    "start_time_date" to startTimeDateFull,  // Full timestamp with time
                    "implementation_time" to durationStr,    // Was: "duration"
                    "repeat" to repeatType,                  // Was: "repeat_type"
                    "repeat_days" to repeatDays,             // Was: "repeat_pattern"
                    "color" to color,
                    "source" to SourceType.PERSONALIZATION.name,
                    "created_at" to java.time.OffsetDateTime.now().toString()
                )
                
                android.util.Log.d("AIRepository", "Inserting schedule: $scheduleRow")
                
                scheduleRepository?.insertSchedule(scheduleRow)
                schedulesCreated++
                
                android.util.Log.d("AIRepository", "Created schedule: ${slot.name} at $startTimeFormatted")
            }
            
            Result.success(schedulesCreated)
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error saving generated schedules", e)
            Result.failure(e)
        }
    }
    
    /**
     * Parse JSON response to schedule slots
     */
    private fun parseScheduleSlots(responseText: String): List<ScheduleSlot> {
        try {
            // Extract JSON from response
            val jsonStart = responseText.indexOf("{")
            val jsonEnd = responseText.lastIndexOf("}") + 1
            if (jsonStart == -1 || jsonEnd <= jsonStart) {
                return emptyList()
            }
            
            val jsonStr = responseText.substring(jsonStart, jsonEnd)
            val json = JSONObject(jsonStr)
            val scheduleArray = json.optJSONArray("schedule") ?: return emptyList()
            
            val slots = mutableListOf<ScheduleSlot>()
            for (i in 0 until scheduleArray.length()) {
                val item = scheduleArray.getJSONObject(i)
                slots.add(ScheduleSlot(
                    name = item.optString("name", ""),
                    startTime = item.optString("start_time", "08:00"),
                    endTime = item.optString("end_time", "09:00"),
                    priority = item.optString("priority", "medium"),
                    label = item.optString("label", "book")
                ))
            }
            
            return slots
        } catch (e: Exception) {
            android.util.Log.e("AIRepository", "Error parsing schedule slots: ${e.message}")
            return emptyList()
        }
    }

    /**
     * Infer schedule label from task title using keyword matching
     * Uses project's ScheduleLabel enum values: wakeup, eat, exercise, rest, water, book, sleep, clean, cook, garden
     */
    private fun inferLabelFromTitle(title: String): String {
        val lowerTitle = title.lowercase()
        return when {
            
            lowerTitle.contains("thức dậy") || lowerTitle.contains("wake") ||
            lowerTitle.contains("dậy") || lowerTitle.contains("morning") ||
            lowerTitle.contains("báo thức") -> "wakeup"
            
            
            lowerTitle.contains("ăn") || lowerTitle.contains("eat") ||
            lowerTitle.contains("bữa") || lowerTitle.contains("meal") ||
            lowerTitle.contains("sáng") || lowerTitle.contains("trưa") ||
            lowerTitle.contains("tối") || lowerTitle.contains("breakfast") ||
            lowerTitle.contains("lunch") || lowerTitle.contains("dinner") -> "eat"
            
            
            lowerTitle.contains("tập") || lowerTitle.contains("gym") ||
            lowerTitle.contains("chạy") || lowerTitle.contains("run") ||
            lowerTitle.contains("yoga") || lowerTitle.contains("thể dục") ||
            lowerTitle.contains("exercise") || lowerTitle.contains("workout") -> "exercise"
            
            
            lowerTitle.contains("nghỉ") || lowerTitle.contains("rest") ||
            lowerTitle.contains("thư giãn") || lowerTitle.contains("relax") -> "rest"
            
            
            lowerTitle.contains("uống nước") || lowerTitle.contains("water") ||
            lowerTitle.contains("hydrat") -> "water"
            
            
            lowerTitle.contains("ngủ") || lowerTitle.contains("sleep") ||
            lowerTitle.contains("đi ngủ") -> "sleep"
            
            
            lowerTitle.contains("dọn") || lowerTitle.contains("clean") ||
            lowerTitle.contains("vệ sinh") || lowerTitle.contains("lau") -> "clean"
            
            
            lowerTitle.contains("nấu") || lowerTitle.contains("cook") ||
            lowerTitle.contains("chuẩn bị") -> "cook"
            
            
            lowerTitle.contains("vườn") || lowerTitle.contains("garden") ||
            lowerTitle.contains("cây") || lowerTitle.contains("plant") -> "garden"
            
            
            lowerTitle.contains("học") || lowerTitle.contains("study") || 
            lowerTitle.contains("ôn") || lowerTitle.contains("đọc") ||
            lowerTitle.contains("nghiên cứu") || lowerTitle.contains("research") ||
            lowerTitle.contains("luyện") || lowerTitle.contains("practice") ||
            lowerTitle.contains("code") || lowerTitle.contains("lập trình") ||
            lowerTitle.contains("làm") || lowerTitle.contains("work") ||
            lowerTitle.contains("tiếng") || lowerTitle.contains("english") -> "book"
            
            
            else -> "book"
        }
    }
    
    
    private fun inferColorFromLabel(label: String): String {
        return when (label) {
            "wakeup" -> "#FF9800"    
            "eat" -> "#FFC107"       
            "exercise" -> "#4CAF50"  
            "rest" -> "#9C27B0"      
            "water" -> "#2196F3"     
            "book" -> "#3F51B5"      
            "sleep" -> "#607D8B"     
            "clean" -> "#00BCD4"     
            "cook" -> "#E91E63"      
            "garden" -> "#8BC34A"    
            else -> "#3F51B5"        
        }
    }
}


data class ChatMessage(
    val text: String,
    val isFromUser: Boolean,
    val timestamp: Long = System.currentTimeMillis(),
    val id: String? = null
)

/**
 * Schedule slot returned from AI for preview
 */
data class ScheduleSlot(
    val name: String,
    val startTime: String,
    val endTime: String,
    val priority: String,
    val label: String
)
