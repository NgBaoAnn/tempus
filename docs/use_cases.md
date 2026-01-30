# 📋 TEMPUS - Tài Liệu Use Cases

> **Ngày tạo:** 2026-01-26  
> **Dự án:** TEMPUS - Ứng dụng quản lý thời gian và tập trung  
> **Tổng số Use Cases:** 48

---

## 📌 Tổng Quan Actors

| Actor | Mô tả |
|-------|-------|
| **Guest** | Người dùng chưa đăng nhập (chỉ xem Onboarding) |
| **User** | Người dùng đã đăng ký và đăng nhập |
| **System** | Hệ thống tự động xử lý (background jobs) |

---

## 👤 Actor: Guest

### UC01: Xem Onboarding
- **Mô tả:** Người dùng mới xem hướng dẫn giới thiệu app lần đầu
- **Pre-condition:** App được cài đặt, chưa hoàn thành onboarding
- **Main flow:**
  1. Guest mở app lần đầu
  2. Hệ thống hiển thị màn hình Onboarding
  3. Guest swipe qua các slide giới thiệu
  4. Guest nhấn "Bắt đầu" hoặc "Skip"
- **Post-condition:** Chuyển đến màn hình Login

### UC02: Đăng ký tài khoản
- **Mô tả:** Tạo tài khoản mới bằng email/password
- **Pre-condition:** Chưa có tài khoản
- **Main flow:**
  1. Guest chọn "Đăng ký"
  2. Nhập email, password, họ tên
  3. Hệ thống validate dữ liệu
  4. Hệ thống tạo tài khoản trên Supabase
  5. Chuyển đến MainActivity
- **Alternative flow:**
  - 3a. Email không hợp lệ → Hiển thị lỗi
  - 3b. Password yếu → Hiển thị lỗi
  - 4a. Email đã tồn tại → Hiển thị lỗi
- **Post-condition:** Tài khoản được tạo, user được đăng nhập

### UC03: Đăng nhập
- **Mô tả:** Đăng nhập vào hệ thống bằng email/password
- **Pre-condition:** Có tài khoản đã đăng ký
- **Main flow:**
  1. Guest nhập email và password
  2. Hệ thống xác thực với Supabase
  3. Lưu session vào máy
  4. Chuyển đến MainActivity
- **Alternative flow:**
  - 2a. Sai email/password → Hiển thị lỗi
  - 2b. Lỗi mạng → Hiển thị thông báo
- **Post-condition:** User đã đăng nhập, session được lưu

### UC04: Quên mật khẩu
- **Mô tả:** Khôi phục mật khẩu qua email
- **Pre-condition:** Có tài khoản với email hợp lệ
- **Main flow:**
  1. Guest nhấn "Quên mật khẩu"
  2. Nhập email
  3. Hệ thống gửi email reset password
  4. Hiển thị thông báo kiểm tra email
- **Alternative flow:**
  - 2a. Email không tồn tại → Vẫn thông báo thành công (bảo mật)
- **Post-condition:** Email reset được gửi

---

## 👤 Actor: User

### 🔐 AUTHENTICATION

### UC05: Đăng xuất
- **Mô tả:** Đăng xuất khỏi hệ thống
- **Pre-condition:** Đã đăng nhập
- **Main flow:**
  1. User chọn Đăng xuất trong Settings
  2. Hệ thống xóa session
  3. Chuyển về LoginActivity
- **Post-condition:** Session bị xóa, chuyển về Login

---

### ⏱️ TIMER & FOCUS

### UC06: Bắt đầu phiên tập trung (Pomodoro)
- **Mô tả:** Bắt đầu đếm ngược thời gian tập trung
- **Pre-condition:** Đăng nhập, đang ở TimerFragment
- **Main flow:**
  1. User chọn thời gian tập trung (mặc định 25 phút)
  2. Nhấn nút Start
  3. Timer bắt đầu đếm ngược
  4. Hiển thị thông báo notification
- **Post-condition:** Timer đang chạy, notification hiển thị

### UC07: Tạm dừng phiên tập trung
- **Mô tả:** Tạm dừng timer đang chạy
- **Pre-condition:** Timer đang chạy
- **Main flow:**
  1. User nhấn Pause
  2. Timer dừng lại, lưu thời gian còn lại
- **Post-condition:** Timer tạm dừng

### UC08: Tiếp tục phiên tập trung
- **Mô tả:** Tiếp tục timer đã tạm dừng
- **Pre-condition:** Timer đang tạm dừng
- **Main flow:**
  1. User nhấn Resume
  2. Timer tiếp tục đếm từ vị trí tạm dừng
- **Post-condition:** Timer tiếp tục chạy

### UC09: Hủy phiên tập trung  
- **Mô tả:** Hủy bỏ phiên timer hiện tại
- **Pre-condition:** Timer đang chạy hoặc tạm dừng
- **Main flow:**
  1. User nhấn Cancel
  2. Timer reset về trạng thái ban đầu
- **Post-condition:** Timer reset, không ghi nhận phiên

### UC10: Hoàn thành phiên tập trung
- **Mô tả:** Timer kết thúc tự động
- **Pre-condition:** Timer đang chạy
- **Main flow:**
  1. Timer đếm về 0
  2. Phát âm thanh/rung thông báo
  3. Cộng điểm cho user (1 điểm/phút)
  4. Lưu timer_session vào DB
  5. Chuyển sang chế độ nghỉ (break)
- **Post-condition:** Điểm được cộng, session được lưu

### UC11: Bật chế độ khóa màn hình Focus Lock
- **Mô tả:** Mở màn hình toàn màn hình khóa điện thoại
- **Pre-condition:** Timer đang chạy
- **Main flow:**
  1. User bật Focus Lock trong settings hoặc tự động
  2. Mở FocusLockActivity toàn màn hình
  3. Không thể thoát bằng nút Back
  4. Hiển thị timer và thông điệp động viên
- **Alternative flow:**
  - 3a. User xác nhận muốn thoát → Hiện dialog xác nhận
- **Post-condition:** Màn hình Focus Lock hiển thị

### UC12: Mở khóa màn hình Focus Lock
- **Mô tả:** Thoát khỏi màn hình Focus Lock
- **Pre-condition:** Focus Lock đang hiển thị
- **Main flow:**
  1. User nhấn nút unlock
  2. Hiển thị dialog xác nhận
  3. User xác nhận muốn thoát
  4. Đóng FocusLockActivity
- **Alternative flow:**
  - 3a. User hủy → Tiếp tục Focus Lock
- **Post-condition:** Focus Lock đóng, timer vẫn chạy

---

### 📅 TIMELINE & SCHEDULE

### UC13: Xem lịch trình theo tuần
- **Mô tả:** Xem các task được lên lịch trong tuần
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở TimelineFragment
  2. Hệ thống load tasks từ Supabase
  3. Hiển thị tasks theo từng ngày trong tuần
  4. User có thể swipe để chuyển tuần
- **Post-condition:** Timeline hiển thị tasks

### UC14: Xem lịch trình theo tháng
- **Mô tả:** Mở dialog chọn tháng để nhảy đến
- **Pre-condition:** Đang xem Timeline
- **Main flow:**
  1. User nhấn vào header tháng/năm
  2. Hiển thị MonthCalendarDialogFragment
  3. User chọn tháng cần xem
  4. Timeline nhảy đến tháng được chọn
- **Post-condition:** Timeline hiển thị tháng mới

### UC15: Tạo task mới
- **Mô tả:** Tạo lịch trình/công việc mới
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User nhấn nút + hoặc FAB
  2. Mở EditScheduleFragment
  3. Nhập tên task, thời gian, lặp lại, màu sắc, label
  4. Thêm sub-tasks nếu cần
  5. Nhấn Lưu
  6. Task được insert vào Supabase
- **Alternative flow:**
  - 5a. Thiếu thông tin bắt buộc → Hiển thị lỗi
- **Post-condition:** Task mới được tạo

### UC16: Chỉnh sửa task
- **Mô tả:** Sửa thông tin task đã tạo
- **Pre-condition:** Task đã tồn tại
- **Main flow:**
  1. User tap vào task trong timeline
  2. Mở EditScheduleFragment với dữ liệu task
  3. Sửa thông tin cần thiết
  4. Nhấn Lưu
  5. Task được update
- **Post-condition:** Task được cập nhật

### UC17: Xóa task
- **Mô tả:** Xóa task khỏi hệ thống
- **Pre-condition:** Task đã tồn tại
- **Main flow:**
  1. User mở task cần xóa
  2. Nhấn nút Xóa
  3. Hiển thị dialog xác nhận
  4. User xác nhận
  5. Task bị xóa khỏi DB
- **Post-condition:** Task bị xóa

### UC18: Đánh dấu task hoàn thành
- **Mô tả:** Đánh dấu task là đã hoàn thành
- **Pre-condition:** Task có status "planned"
- **Main flow:**
  1. User tap checkbox hoặc swipe task
  2. Status chuyển thành "done"
  3. Cộng điểm cho user (+10 điểm)
  4. Cập nhật streak
- **Post-condition:** Task done, điểm được cộng

### UC19: Bỏ qua task (Skip)
- **Mô tả:** Đánh dấu bỏ qua task cho ngày hôm nay
- **Pre-condition:** Task có status "planned"
- **Main flow:**
  1. User chọn Skip task
  2. Status chuyển thành "skipped" cho ngày đó
  3. Trừ điểm (-5 điểm) nếu có penalty
- **Post-condition:** Task skipped cho ngày đó

### UC20: Tạo sub-task
- **Mô tả:** Thêm các task con vào task chính
- **Pre-condition:** Đang edit task
- **Main flow:**
  1. User nhấn "Thêm sub-task"
  2. Nhập tiêu đề sub-task
  3. Sub-task được thêm vào danh sách
- **Post-condition:** Sub-task được tạo

### UC21: Đánh dấu sub-task hoàn thành
- **Mô tả:** Tick hoàn thành sub-task
- **Pre-condition:** Sub-task tồn tại
- **Main flow:**
  1. User tap checkbox sub-task
  2. is_done chuyển thành true
- **Post-condition:** Sub-task được đánh dấu done

---

### 🌳 GAMIFICATION & GARDEN

### UC22: Xem điểm số
- **Mô tả:** Xem tổng điểm, streak, level hiện tại
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User xem điểm trên header hoặc Settings
  2. Hiển thị total_points, current_streak, level
- **Post-condition:** Điểm được hiển thị

### UC23: Xem lịch sử điểm
- **Mô tả:** Xem lịch sử kiếm/tiêu điểm
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở Point History
  2. Hiển thị 50 records gần nhất với reason và timestamp
- **Post-condition:** History hiển thị

### UC24: Trồng cây mới
- **Mô tả:** Dùng điểm để trồng cây trong Garden
- **Pre-condition:** Có đủ 50 điểm
- **Main flow:**
  1. User mở Garden
  2. Nhấn "Trồng cây mới"
  3. Chọn loại cây (Oak, Cherry, Pine...)
  4. Đặt tên cho cây
  5. Trừ 50 điểm
  6. Cây mới được tạo với state = SEED
- **Alternative flow:**
  - 3a. Không đủ điểm → Thông báo lỗi
- **Post-condition:** Cây mới được trồng

### UC25: Tưới cây
- **Mô tả:** Đầu tư điểm vào cây để phát triển
- **Pre-condition:** Cây đang sống, có điểm
- **Main flow:**
  1. User chọn cây trong Garden
  2. Nhấn "Tưới cây"
  3. Chọn số điểm đầu tư (mặc định 10)
  4. Cây tăng invested_points
  5. Cây có thể chuyển trạng thái (SEED → SPROUT → SAPLING → TREE)
- **Post-condition:** Cây được tưới, có thể lên cấp

### UC26: Xem chi tiết cây
- **Mô tả:** Xem thông tin chi tiết của một cây
- **Pre-condition:** Cây tồn tại
- **Main flow:**
  1. User tap vào cây trong Garden
  2. Mở TreeDetailActivity
  3. Hiển thị: tên, loại, trạng thái, điểm đã đầu tư, thời gian trồng
- **Post-condition:** Chi tiết cây hiển thị

### UC27: Xem Garden (vườn cây)
- **Mô tả:** Xem grid danh sách tất cả cây
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở GardenFragment
  2. Hiển thị grid các cây còn sống
  3. Mỗi cây hiển thị trạng thái và animation
- **Post-condition:** Garden hiển thị

---

### 🤖 AI CHAT

### UC28: Chat với AI (Ask Mode)
- **Mô tả:** Hỏi đáp tự do với AI
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở AIFragment
  2. Chọn mode "Ask"
  3. Gõ tin nhắn
  4. AI trả lời qua OpenAI API
  5. Hiển thị response trong chat bubble
- **Alternative flow:**
  - 4a. API lỗi → Hiển thị error message
- **Post-condition:** Cuộc trò chuyện được hiển thị

### UC29: Tạo lịch với AI (Agent Mode)
- **Mô tả:** AI gợi ý lịch trình dựa trên yêu cầu
- **Pre-condition:** Đăng nhập, mode Agent
- **Main flow:**
  1. User mô tả công việc cần làm
  2. AI phân tích và đề xuất schedule
  3. User xem preview các suggestions
  4. User Accept hoặc Reject từng suggestion
  5. Accepted suggestions được tạo thành tasks
- **Alternative flow:**
  - 4a. User Cancel proposal → Reset agent state
- **Post-condition:** Tasks được tạo từ AI suggestions

### UC30: Lên kế hoạch sống (Life Planner Mode)
- **Mô tả:** AI lên kế hoạch dài hạn cho user
- **Pre-condition:** Đăng nhập, mode Life Planner
- **Main flow:**
  1. User mô tả mục tiêu dài hạn
  2. AI đề xuất kế hoạch chi tiết
  3. User xem và chấp nhận life plan
  4. Các schedules được tạo tự động
- **Post-condition:** Kế hoạch dài hạn được tạo

### UC31: Chấp nhận gợi ý AI
- **Mô tả:** Accept một schedule suggestion từ AI
- **Pre-condition:** Có suggestions đang hiển thị
- **Main flow:**
  1. User nhấn Accept trên suggestion
  2. Task được tạo trong database
  3. Suggestion bị remove khỏi list
- **Post-condition:** Task được tạo

### UC32: Xóa lịch sử chat
- **Mô tả:** Clear toàn bộ chat history
- **Pre-condition:** Có chat history
- **Main flow:**
  1. User nhấn Clear Chat
  2. Messages được xóa
  3. Hiển thị welcome message mới
- **Post-condition:** Chat được reset

---

### 📝 NOTES

### UC33: Xem danh sách ghi chú
- **Mô tả:** Xem tất cả notes đã tạo
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở NotesFragment
  2. Hiển thị danh sách notes (pinned lên đầu)
- **Post-condition:** Notes hiển thị

### UC34: Tạo ghi chú mới
- **Mô tả:** Tạo quick note mới
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User nhấn nút +
  2. Nhập title và content
  3. Nhấn Save
  4. Note được lưu vào Room DB
- **Post-condition:** Note mới được tạo

### UC35: Chỉnh sửa ghi chú
- **Mô tả:** Sửa nội dung note
- **Pre-condition:** Note tồn tại
- **Main flow:**
  1. User tap vào note
  2. Sửa title/content
  3. Nhấn Save
- **Post-condition:** Note được cập nhật

### UC36: Xóa ghi chú
- **Mô tả:** Xóa note
- **Pre-condition:** Note tồn tại
- **Main flow:**
  1. User chọn Delete trên note
  2. Note bị xóa khỏi DB
- **Post-condition:** Note bị xóa

### UC37: Tìm kiếm ghi chú
- **Mô tả:** Tìm note theo nội dung
- **Pre-condition:** Có notes
- **Main flow:**
  1. User nhập text vào search bar
  2. Danh sách filter theo query
- **Post-condition:** Notes được filter

### UC38: Ghim ghi chú
- **Mô tả:** Pin note lên đầu danh sách
- **Pre-condition:** Note tồn tại
- **Main flow:**
  1. User toggle pin trên note
  2. Note được đánh dấu pinned
  3. Note hiển thị lên đầu list
- **Post-condition:** Note được pin

---

### 👥 SOCIAL

### UC39: Xem danh sách bạn bè
- **Mô tả:** Xem list bạn bè đã kết nối
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở SocialFragment
  2. Tab Friends hiển thị
  3. Hiển thị danh sách bạn bè
- **Post-condition:** Friends list hiển thị

### UC40: Tìm kiếm người dùng
- **Mô tả:** Tìm user khác theo username
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User chuyển tab Discover
  2. Nhập username vào search
  3. Hiển thị kết quả tìm kiếm
- **Post-condition:** Kết quả search hiển thị

### UC41: Gửi lời mời kết bạn
- **Mô tả:** Gửi friend request
- **Pre-condition:** Tìm thấy user, chưa là bạn
- **Main flow:**
  1. User nhấn "Add Friend"
  2. Friend request được gửi
  3. Hiển thị trạng thái "Pending"
- **Post-condition:** Request được gửi

### UC42: Chấp nhận lời mời kết bạn
- **Mô tả:** Accept friend request
- **Pre-condition:** Có pending request
- **Main flow:**
  1. User xem tab Requests
  2. Nhấn Accept
  3. Trở thành bạn bè
- **Post-condition:** Hai user thành bạn

### UC43: Từ chối lời mời kết bạn
- **Mô tả:** Reject friend request
- **Pre-condition:** Có pending request
- **Main flow:**
  1. User nhấn Reject
  2. Request bị xóa
- **Post-condition:** Request bị reject

### UC44: Hủy kết bạn
- **Mô tả:** Unfriend một người
- **Pre-condition:** Đã là bạn
- **Main flow:**
  1. User chọn Unfriend
  2. Friendship bị xóa
- **Post-condition:** Không còn là bạn

### UC45: Chặn người dùng
- **Mô tả:** Block user
- **Pre-condition:** User tồn tại
- **Main flow:**
  1. User chọn Block
  2. User bị thêm vào blocked list
  3. Không thể nhắn tin hoặc kết bạn
- **Post-condition:** User bị block

### UC46: Mở cuộc hội thoại
- **Mô tả:** Mở chat với bạn bè
- **Pre-condition:** Đã là bạn
- **Main flow:**
  1. User nhấn vào conversation
  2. Mở ChatScreen
  3. Hiển thị lịch sử tin nhắn
- **Post-condition:** Chat screen mở

### UC47: Gửi tin nhắn
- **Mô tả:** Gửi message trong chat
- **Pre-condition:** Đang trong ChatScreen
- **Main flow:**
  1. User nhập tin nhắn
  2. Nhấn Send
  3. Message được lưu và hiển thị
- **Post-condition:** Message được gửi

---

### ⚙️ SETTINGS

### UC48: Xem/Sửa Profile
- **Mô tả:** Xem và cập nhật thông tin cá nhân
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở Settings → Profile
  2. Sửa username, avatar
  3. Lưu thay đổi
- **Post-condition:** Profile được cập nhật

### UC49: Cài đặt Personalization
- **Mô tả:** Thiết lập thời gian cá nhân
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở Personalization
  2. Đặt wake_time, sleep_time, working hours
  3. Chọn persona_type
  4. Lưu vào user_constraints
- **Post-condition:** Constraints được lưu

### UC50: Đổi Theme ứng dụng
- **Mô tả:** Chuyển đổi giao diện sáng/tối
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User chọn Theme
  2. Chọn Light/Dark/System
  3. App apply theme mới
- **Post-condition:** Theme được thay đổi

### UC51: Export dữ liệu JSON
- **Mô tả:** Xuất toàn bộ dữ liệu ra file JSON
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User chọn Export to JSON
  2. Hệ thống export data
  3. Mở share intent
- **Post-condition:** File JSON được tạo và share

### UC52: Export dữ liệu CSV
- **Mô tả:** Xuất dữ liệu ra file CSV
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User chọn Export to CSV
  2. Hệ thống export data
  3. Mở share intent
- **Post-condition:** File CSV được tạo

### UC53: Xóa toàn bộ dữ liệu
- **Mô tả:** Xóa tất cả data của user (với xác thực)
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User chọn Delete All Data
  2. Hiển thị dialog cảnh báo lần 1
  3. Yêu cầu xác thực Biometric
  4. Hiển thị dialog xác nhận lần 2 (nhập "DELETE")
  5. Xóa toàn bộ data
- **Alternative flow:**
  - 3a. Biometric thất bại → Hủy
  - 4a. Nhập sai "DELETE" → Hủy
- **Post-condition:** Data bị xóa

### UC54: Xem Privacy Policy
- **Mô tả:** Mở trang chính sách bảo mật
- **Pre-condition:** N/A
- **Main flow:**
  1. User nhấn Privacy Policy
  2. Mở URL trong browser
- **Post-condition:** Trang web mở

### UC55: Xem Terms of Service
- **Mô tả:** Mở điều khoản dịch vụ
- **Pre-condition:** N/A
- **Main flow:**
  1. User nhấn Terms
  2. Mở URL trong browser
- **Post-condition:** Trang web mở

---

### 🎤 VOICE

### UC56: Tạo task bằng giọng nói
- **Mô tả:** Dùng voice input để tạo task nhanh
- **Pre-condition:** Có quyền microphone
- **Main flow:**
  1. User nhấn nút microphone
  2. Nói mô tả task
  3. Speech-to-text chuyển thành text
  4. TaskParserService parse và tạo task
- **Alternative flow:**
  - 2a. Không nhận dạng được → Thông báo thử lại
- **Post-condition:** Task được tạo từ voice

---

### 📊 STATISTICS (Suy luận từ cấu trúc)

### UC57: Xem thống kê hoàn thành
- **Mô tả:** Xem biểu đồ tasks hoàn thành
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở StatisticsFragment
  2. Hiển thị bar chart tasks completed/skipped
  3. Xem theo tuần hoặc tháng
- **Post-condition:** Statistics hiển thị

---

## ⚙️ Actor: System (Automated)

### UC58: Tự động cập nhật streak
- **Mô tả:** Cập nhật streak khi user hoàn thành task
- **Pre-condition:** User complete task
- **Main flow:**
  1. Task được đánh dấu done
  2. System kiểm tra ngày hiện tại
  3. Nếu liên tiếp → Tăng current_streak
  4. Cập nhật best_streak nếu cần
- **Post-condition:** Streak được cập nhật

### UC59: Tự động kiểm tra cây chết
- **Mô tả:** Đánh dấu cây chết nếu không được tưới
- **Pre-condition:** App được mở
- **Main flow:**
  1. System kiểm tra last_watered_at của từng cây
  2. Nếu > 3 ngày không tưới → is_alive = false
  3. State chuyển thành DEAD
- **Post-condition:** Cây chết được đánh dấu

### UC60: Tự động lưu timer session
- **Mô tả:** Ghi nhận phiên focus khi hoàn thành
- **Pre-condition:** Timer kết thúc
- **Main flow:**
  1. Timer đếm về 0
  2. System tạo timer_session record
  3. Lưu started_at, ended_at, duration
- **Post-condition:** Session được lưu

### UC61: Tự động tính điểm Pomodoro
- **Mô tả:** Cộng điểm theo thời gian tập trung
- **Pre-condition:** Pomodoro session hoàn thành
- **Main flow:**
  1. Session kết thúc
  2. Tính: 1 điểm/phút focus
  3. Áp dụng streak bonus (x1.5 nếu streak >= 3)
  4. Cộng điểm cho user
- **Post-condition:** Điểm được cộng

### UC62: Tự động chuyển chế độ Break
- **Mô tả:** Chuyển từ Focus sang Break khi timer hết
- **Pre-condition:** Focus session kết thúc
- **Main flow:**
  1. Focus timer kết thúc
  2. Thông báo cho user
  3. Đề xuất chuyển sang Short Break (5 phút)
- **Post-condition:** Gợi ý break hiển thị

### UC63: Tự động lưu AI history
- **Mô tả:** Lưu conversation với AI vào database
- **Pre-condition:** User chat với AI
- **Main flow:**
  1. User gửi message
  2. AI respond
  3. Lưu prompt và response vào ai_history
- **Post-condition:** History được lưu

---

## 📊 Tóm Tắt

| Actor | Số Use Cases |
|-------|--------------|
| Guest | 4 |
| User | 52 |
| System | 6 |
| **Tổng** | **62** |

### Phân loại theo Module

| Module | Use Cases |
|--------|-----------|
| Authentication | UC01-UC05 |
| Timer & Focus | UC06-UC12 |
| Timeline & Schedule | UC13-UC21 |
| Gamification | UC22-UC27 |
| AI Chat | UC28-UC32 |
| Notes | UC33-UC38 |
| Social | UC39-UC47 |
| Settings | UC48-UC55 |
| Voice | UC56 |
| Statistics | UC57 |
| Heatmap | UC64-UC65 |
| Sync & Background | UC66-UC69 |
| Notification | UC70-UC71 |
| System (Auto) | UC58-UC63 |

---

## 📈 HEATMAP (Bổ sung mới)

### UC64: Xem Heatmap năng suất
- **Mô tả:** Xem heatmap hiển thị mức độ năng suất theo ngày trong tháng
- **Pre-condition:** Đăng nhập
- **Main flow:**
  1. User mở Statistics → nhấn Heatmap
  2. Hiển thị lưới ngày theo tháng
  3. Màu sắc thể hiện mức độ năng suất (xanh nhạt → xanh đậm)
  4. User có thể vuốt để xem các tháng khác
- **Post-condition:** Heatmap hiển thị

### UC65: Xem chi tiết ngày từ Heatmap
- **Mô tả:** Nhấn vào ngày trên heatmap để xem chi tiết
- **Pre-condition:** Đang xem Heatmap
- **Main flow:**
  1. User tap vào một ngày trên heatmap
  2. Chuyển đến EditScheduleFragment với ngày đó
  3. Hiển thị các task trong ngày
- **Post-condition:** Chi tiết ngày hiển thị

---

## 🔄 SYNC & BACKGROUND (Bổ sung mới)

### UC66: Đồng bộ dữ liệu khi đăng nhập
- **Mô tả:** Pull dữ liệu từ Supabase về Room sau khi login
- **Pre-condition:** Đăng nhập thành công
- **Main flow:**
  1. User đăng nhập thành công
  2. System pull schedules từ Supabase
  3. System pull user_points từ Supabase
  4. System pull trees từ Supabase
  5. System pull point_history từ Supabase
  6. System pull notes từ Supabase
  7. Dữ liệu được lưu vào Room database
- **Post-condition:** Dữ liệu local được đồng bộ với server

### UC67: Đồng bộ dữ liệu khi đăng xuất
- **Mô tả:** Push dữ liệu từ Room lên Supabase trước khi logout
- **Pre-condition:** User thực hiện đăng xuất
- **Main flow:**
  1. User nhấn Đăng xuất
  2. System push schedules lên Supabase
  3. System push gamification data lên Supabase
  4. System xóa session local
  5. Chuyển về LoginActivity
- **Post-condition:** Dữ liệu được backup lên server

### UC68: Đồng bộ thủ công
- **Mô tả:** User kích hoạt sync thủ công từ Settings
- **Pre-condition:** Đăng nhập, có kết nối mạng
- **Main flow:**
  1. User mở Settings → Data & Sync
  2. Nhấn "Sync Now"
  3. Hiển thị progress indicator
  4. Pull và Push dữ liệu
  5. Hiển thị kết quả sync
- **Post-condition:** Dữ liệu được đồng bộ 2 chiều

### UC69: Đồng bộ nền tự động (WorkManager)
- **Mô tả:** System tự động sync dữ liệu định kỳ
- **Pre-condition:** App được cài đặt, có kết nối mạng
- **Main flow:**
  1. WorkManager trigger SyncScheduleWorker
  2. Kiểm tra kết nối mạng
  3. Sync schedules với Supabase
  4. Ghi log kết quả
- **Post-condition:** Dữ liệu được đồng bộ tự động

---

## 🔔 NOTIFICATION (Bổ sung mới)

### UC70: Thông báo hoàn thành Pomodoro
- **Mô tả:** Gửi notification khi phiên Pomodoro kết thúc
- **Pre-condition:** Timer về 0
- **Main flow:**
  1. Timer đếm ngược về 0
  2. System tạo notification với âm thanh/rung
  3. Hiển thị thông báo "Phiên tập trung hoàn thành!"
  4. User có thể tap để mở app
- **Post-condition:** Notification hiển thị

### UC71: Thông báo nhắc nhở công việc
- **Mô tả:** Gửi notification nhắc nhở trước khi task bắt đầu
- **Pre-condition:** Task có reminder được bật
- **Main flow:**
  1. AlarmManager trigger ReminderReceiver
  2. System tạo notification với thông tin task
  3. Hiển thị thông báo nhắc nhở
  4. User có thể tap để xem chi tiết task
- **Post-condition:** Reminder notification hiển thị

---

## 🔐 AUTHENTICATION (Bổ sung)

### UC72: Đăng nhập Google
- **Mô tả:** Đăng nhập bằng tài khoản Google OAuth
- **Pre-condition:** Có tài khoản Google
- **Main flow:**
  1. Guest nhấn "Đăng nhập với Google"
  2. Mở Google Sign-In flow
  3. User chọn tài khoản Google
  4. System xác thực với Supabase qua OAuth
  5. Đăng nhập thành công, sync dữ liệu
- **Alternative flow:**
  - 3a. User hủy → Quay về màn hình Login
  - 4a. OAuth thất bại → Hiển thị lỗi
- **Post-condition:** User đăng nhập, dữ liệu được sync

### UC73: Xác thực OTP reset password
- **Mô tả:** Nhập mã OTP để xác thực reset password
- **Pre-condition:** Đã gửi email reset password
- **Main flow:**
  1. User nhận email chứa OTP
  2. Mở VerifyOtpActivity
  3. Nhập mã OTP 6 số
  4. System xác thực OTP với Supabase
  5. Chuyển sang màn hình đặt mật khẩu mới
- **Alternative flow:**
  - 4a. OTP sai → Hiển thị lỗi, cho phép nhập lại
  - 4b. OTP hết hạn → Yêu cầu gửi lại
- **Post-condition:** OTP xác thực thành công

---

## 📊 Tóm Tắt

| Actor | Số Use Cases |
|-------|--------------|
| Guest | 6 |
| User | 60 |
| System | 7 |
| **Tổng** | **73** |

### Phân loại theo Module

| Module | Use Cases |
|--------|-----------|
| Authentication | UC01-UC05, UC72-UC73 |
| Timer & Focus | UC06-UC12 |
| Timeline & Schedule | UC13-UC21 |
| Gamification | UC22-UC27 |
| AI Chat | UC28-UC32 |
| Notes | UC33-UC38 |
| Social | UC39-UC47 |
| Settings | UC48-UC55 |
| Voice | UC56 |
| Statistics | UC57 |
| Heatmap | UC64-UC65 |
| Sync & Background | UC66-UC69 |
| Notification | UC70-UC71 |
| System (Auto) | UC58-UC63 |
