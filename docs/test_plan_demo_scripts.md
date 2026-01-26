# 📋 TEMPUS - Test Plan & Demo Scripts

> **Ngày tạo:** 2026-01-26  
> **Tổng Use Cases:** 62  
> **Số Tester:** 4 người

---

## 📊 Phân Chia Công Việc

| Tester | Module phụ trách | Số UC | Use Cases |
|--------|------------------|-------|-----------|
| **Tester 1** | Authentication + Settings + Onboarding | 15 | UC01-UC05, UC48-UC57 |
| **Tester 2** | Timer/Focus + Gamification/Garden | 18 | UC06-UC12, UC22-UC27, UC58-UC62 |
| **Tester 3** | Timeline/Schedule + Notes + Voice | 16 | UC13-UC21, UC33-UC38, UC56 |
| **Tester 4** | AI Chat + Social | 15 | UC28-UC32, UC39-UC47, UC63 |

---

# 👤 TESTER 1: Authentication + Settings + Onboarding + Statistics

## Modules phụ trách:
- Onboarding
- Authentication (Đăng ký/Đăng nhập/Đăng xuất)
- Settings (Profile, Export, Privacy)
- Statistics

## Use Cases: UC01-UC05, UC48-UC57 (15 Use Cases)

---

### 📝 Demo Script #1: Onboarding Flow
**UC01 - Xem Onboarding**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Cài đặt app mới (hoặc xóa data) | App khởi động |
| 2 | Mở app lần đầu | Màn hình Onboarding hiển thị |
| 3 | Swipe qua các slides | Slides chuyển tuần tự |
| 4 | Nhấn "Bắt đầu" | Chuyển đến màn hình Login |

**⏱️ Thời gian:** 2 phút

---

### 📝 Demo Script #2: Đăng Ký & Đăng Nhập
**UC02, UC03, UC04 - Register, Login, Forgot Password**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Nhấn "Đăng ký" | Mở RegisterActivity |
| 2 | Để trống email, nhấn Đăng ký | Lỗi validation |
| 3 | Nhập email không hợp lệ | Lỗi "Email không hợp lệ" |
| 4 | Nhập thông tin hợp lệ, nhấn Đăng ký | Tài khoản được tạo |
| 5 | Đăng xuất | Về Login screen |
| 6 | Nhập sai password | Lỗi "Sai email hoặc mật khẩu" |
| 7 | Nhập đúng thông tin | Đăng nhập thành công |
| 8 | Nhấn "Quên mật khẩu", nhập email | Toast "Kiểm tra email" |

**⏱️ Thời gian:** 5 phút

---

### 📝 Demo Script #3: Profile & Personalization
**UC48, UC49 - Profile, Personalization**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Settings → Profile | ProfileActivity mở |
| 2 | Sửa username | Username được cập nhật |
| 3 | Nhấn Lưu | Toast xác nhận |
| 4 | Mở Personalization | PersonalizationActivity mở |
| 5 | Đặt wake_time, sleep_time | Thời gian được set |
| 6 | Chọn persona_type | Persona được lưu |

**⏱️ Thời gian:** 4 phút

---

### 📝 Demo Script #4: Theme & Privacy
**UC50, UC54, UC55 - Theme, Privacy, Terms**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Settings → Theme | Options hiển thị |
| 2 | Chọn Dark mode | App đổi theme |
| 3 | Nhấn Privacy Policy | Browser mở URL |
| 4 | Nhấn Terms of Service | Browser mở URL |

**⏱️ Thời gian:** 2 phút

---

### 📝 Demo Script #5: Export Data
**UC51, UC52 - Export JSON, CSV**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Settings | SettingsFragment |
| 2 | Nhấn "Export to JSON" | Share dialog mở |
| 3 | Lưu/Share file | File JSON được tạo |
| 4 | Nhấn "Export to CSV" | Share dialog mở |
| 5 | Verify file content | Data đúng format |

**⏱️ Thời gian:** 3 phút

---

### 📝 Demo Script #6: Delete Data & Logout
**UC53, UC05 - Delete All Data, Logout**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Nhấn "Xóa toàn bộ dữ liệu" | Dialog cảnh báo |
| 2 | Nhấn Tiếp tục | Yêu cầu Biometric |
| 3 | Xác thực thành công | Dialog nhập "DELETE" |
| 4 | Nhập sai | Nút disable |
| 5 | Nhập đúng "DELETE" | Data bị xóa |
| 6 | (Hoặc) Nhấn Đăng xuất | Về LoginActivity |

**⏱️ Thời gian:** 3 phút

---

### 📝 Demo Script #7: Statistics
**UC57 - Xem thống kê**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở StatisticsFragment | Chart hiển thị |
| 2 | Xem biểu đồ tuần | 7 ngày data |
| 3 | Xem completion rate | % hiển thị |

**⏱️ Thời gian:** 2 phút

---

## ✅ Tổng kết Tester 1: ~21 phút, 15 UC, 7 scripts

---

# 👤 TESTER 2: Timer/Focus + Gamification/Garden + System

## Modules phụ trách:
- Timer/Pomodoro
- Focus Lock
- Gamification (Points, Trees)
- System automation

## Use Cases: UC06-UC12, UC22-UC27, UC58-UC62 (18 Use Cases)

---

### 📝 Demo Script #1: Pomodoro Full Flow
**UC06-UC10 - Start, Pause, Resume, Cancel, Complete Timer**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở TimerFragment | Timer UI hiển thị |
| 2 | Chọn 5 phút, nhấn Start | Timer đếm ngược |
| 3 | Notification bar | Notification hiển thị |
| 4 | Nhấn Pause | Timer dừng |
| 5 | Nhấn Resume | Timer tiếp tục |
| 6 | Nhấn Cancel | Timer reset |
| 7 | Start lại, để hết | Âm thanh/rung, +5 điểm |

**⏱️ Thời gian:** 7 phút

---

### 📝 Demo Script #2: Focus Lock
**UC11, UC12 - Focus Lock Mode**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Start Timer, bật Focus Lock | FocusLockActivity mở |
| 2 | Xem UI | Animation, motivational text |
| 3 | Nhấn Back | Không thoát được |
| 4 | Nhấn Unlock | Dialog xác nhận |
| 5 | Confirm unlock | Thoát Focus Lock |

**⏱️ Thời gian:** 5 phút

---

### 📝 Demo Script #3: Points System
**UC22, UC23 - Xem điểm, Lịch sử điểm**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Xem header/Settings | Điểm hiện tại hiển thị |
| 2 | Hoàn thành task | +10 điểm |
| 3 | Hoàn thành Pomodoro | +5 điểm (1đ/phút) |
| 4 | Xem Point History | Lịch sử hiển thị |

**⏱️ Thời gian:** 4 phút

---

### 📝 Demo Script #4: Garden & Trees
**UC24, UC25, UC26, UC27 - Plant, Water, View Trees**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở GardenFragment | Grid vườn cây |
| 2 | Nhấn "Trồng cây mới" | Dialog chọn loại |
| 3 | Chọn Oak, đặt tên | Cây được tạo, -50 điểm |
| 4 | Xem cây state = SEED | Cây mới hiển thị |
| 5 | Tap vào cây | TreeDetailActivity |
| 6 | Nhấn "Tưới cây" 10 điểm | invested_points tăng |
| 7 | Tưới đến 100 điểm | State → SPROUT |

**⏱️ Thời gian:** 6 phút

---

### 📝 Demo Script #5: System Automation
**UC58-UC62 - Streak, Dead Trees, Session Logging**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Xem streak | current_streak hiển thị |
| 2 | Complete task | Streak tăng (nếu liên tiếp) |
| 3 | (Simulate) Cây không tưới 3 ngày | is_alive = false |
| 4 | Mở app | Dead trees được update |
| 5 | Complete Pomodoro | Session được log vào DB |

**⏱️ Thời gian:** 4 phút

---

## ✅ Tổng kết Tester 2: ~26 phút, 18 UC, 5 scripts

---

# 👤 TESTER 3: Timeline/Schedule + Notes + Voice

## Modules phụ trách:
- Timeline view
- Schedule CRUD
- Sub-tasks
- Notes
- Voice input

## Use Cases: UC13-UC21, UC33-UC38, UC56 (16 Use Cases)

---

### 📝 Demo Script #1: Timeline Navigation
**UC13, UC14 - Week/Month View**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở TimelineFragment | Tuần hiện tại |
| 2 | Swipe trái/phải | Chuyển tuần |
| 3 | Tap header tháng | MonthCalendarDialog |
| 4 | Chọn tháng khác | Timeline nhảy đến |

**⏱️ Thời gian:** 3 phút

---

### 📝 Demo Script #2: Task CRUD
**UC15, UC16, UC17 - Create, Update, Delete Task**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Nhấn FAB (+) | EditScheduleFragment |
| 2 | Để trống tên, Lưu | Lỗi validation |
| 3 | Nhập đầy đủ thông tin | Task được tạo |
| 4 | Tap task → Sửa | Data được update |
| 5 | Nhấn Xóa | Task bị xóa |

**⏱️ Thời gian:** 5 phút

---

### 📝 Demo Script #3: Task Status
**UC18, UC19 - Complete, Skip Task**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Tạo task mới | Status = planned |
| 2 | Tap checkbox | Status → done, +10 điểm |
| 3 | Tạo task khác | Task mới |
| 4 | Chọn Skip | Status → skipped |

**⏱️ Thời gian:** 3 phút

---

### 📝 Demo Script #4: Sub-tasks
**UC20, UC21 - Create, Complete Sub-task**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Tạo task "Dự án" | EditScheduleFragment |
| 2 | Thêm 3 sub-tasks | Sub-tasks được add |
| 3 | Lưu task | Task với sub-tasks |
| 4 | Tick sub-task 1 | is_done = true |

**⏱️ Thời gian:** 4 phút

---

### 📝 Demo Script #5: Notes Full Flow
**UC33-UC38 - CRUD Notes, Search, Pin**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở NotesFragment | List notes |
| 2 | Nhấn + | Editor mở |
| 3 | Nhập title, content, Save | Note được lưu |
| 4 | Tạo thêm 2 notes | 3 notes trong list |
| 5 | Search text | List được filter |
| 6 | Toggle pin | Note lên đầu |
| 7 | Edit note | Content updated |
| 8 | Delete note | Note bị xóa |

**⏱️ Thời gian:** 5 phút

---

### 📝 Demo Script #6: Voice Input
**UC56 - Tạo task bằng giọng nói**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Nhấn nút microphone | Request permission |
| 2 | Cấp quyền | Mic bắt đầu nghe |
| 3 | Nói "Họp lúc 2 giờ chiều" | Speech-to-text |
| 4 | Xác nhận | Task được tạo lúc 14:00 |

**⏱️ Thời gian:** 3 phút

---

## ✅ Tổng kết Tester 3: ~23 phút, 16 UC, 6 scripts

---

# 👤 TESTER 4: AI Chat + Social

## Modules phụ trách:
- AI Chat (Ask, Agent, Life Planner)
- Social (Friends, Messages)
- AI History (System)

## Use Cases: UC28-UC32, UC39-UC47, UC63 (15 Use Cases)

---

### 📝 Demo Script #1: AI Ask Mode
**UC28, UC32 - Chat Ask Mode, Clear Chat**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở AIFragment | Welcome message |
| 2 | Mode = "Ask" | Indicator hiển thị |
| 3 | Gõ câu hỏi | Message gửi đi |
| 4 | Xem typing indicator | Loading animation |
| 5 | Nhận AI response | Bubble hiển thị |
| 6 | Nhấn Clear Chat | Chat reset |

**⏱️ Thời gian:** 4 phút

---

### 📝 Demo Script #2: AI Agent Mode
**UC29, UC30, UC31 - Agent Mode, Life Planner, Accept Suggestions**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Chuyển mode "Agent" | Mode changed |
| 2 | Mô tả lịch cần lên | AI phân tích |
| 3 | Xem proposals | Suggestions hiển thị |
| 4 | Accept 1 suggestion | Task được tạo |
| 5 | Reject suggestion khác | Removed from list |
| 6 | Chuyển "Life Planner" | Mode changed |
| 7 | Mô tả mục tiêu dài hạn | AI đề xuất plan |
| 8 | Accept plan | Schedules được tạo |

**⏱️ Thời gian:** 6 phút

---

### 📝 Demo Script #3: Friends Management
**UC39-UC45 - Friends List, Search, Request, Accept, Block**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở SocialFragment | Tab Friends |
| 2 | Xem friends list | Danh sách hiển thị |
| 3 | Tab Discover | Search users |
| 4 | Tìm username | Results hiển thị |
| 5 | Add Friend | Request gửi đi |
| 6 | (Acc khác) Accept | Thành bạn |
| 7 | Unfriend | Friendship hủy |
| 8 | Block user | User bị block |
| 9 | Unblock | User được unblock |

**⏱️ Thời gian:** 6 phút

---

### 📝 Demo Script #4: Messaging
**UC46, UC47 - Conversations, Send Message**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Conversations tab | List conversations |
| 2 | Tap conversation | ChatScreen mở |
| 3 | Xem history | Messages cũ hiển thị |
| 4 | Nhập message | Input field |
| 5 | Nhấn Send | Message gửi |
| 6 | Real-time update | Message hiển thị |

**⏱️ Thời gian:** 4 phút

---

### 📝 Demo Script #5: AI History (System)
**UC63 - Auto-save AI History**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Chat với AI | Conversation |
| 2 | (Backend verify) | ai_history table có record |

**⏱️ Thời gian:** 2 phút

---

## ✅ Tổng kết Tester 4: ~22 phút, 15 UC, 5 scripts

---

# 📊 BẢNG TỔNG HỢP

## Thời Gian Demo

| Tester | Module | Thời gian | Scripts | UC |
|--------|--------|-----------|---------|-----|
| Tester 1 | Auth + Settings + Stats | ~21 phút | 7 | 15 |
| Tester 2 | Timer + Garden + System | ~26 phút | 5 | 18 |
| Tester 3 | Timeline + Notes + Voice | ~23 phút | 6 | 16 |
| Tester 4 | AI + Social | ~22 phút | 5 | 15 |
| **Tổng** | | **~92 phút** | **23** | **64** |

---

## ✅ Checklist Chuẩn Bị

### Trước khi test:
- [ ] Cài đặt app trên thiết bị test
- [ ] Tạo ít nhất 2 tài khoản test
- [ ] Đảm bảo kết nối internet ổn định
- [ ] Tool quay màn hình để demo
- [ ] Reset data nếu cần test fresh

### Thiết bị cần:
- [ ] Android phone (API 26+)
- [ ] Microphone hoạt động (voice test)
- [ ] Fingerprint/PIN setup (biometric test)

---

## 🐛 Bug Report Template

```markdown
## Bug ID: BUG-XXX
### Use Case: UC##
### Tester: Tester X
### Ngày: YYYY-MM-DD

**Mô tả:** [Mô tả ngắn gọn]

**Các bước tái hiện:**
1. Step 1
2. Step 2

**Kết quả thực tế:** [Điều xảy ra]
**Kết quả mong đợi:** [Điều nên xảy ra]

**Mức độ:** Critical/High/Medium/Low
**Screenshots:** [Đính kèm nếu có]
```

---

## 📅 Lịch Trình Đề Xuất

| Ngày | Buổi sáng | Buổi chiều |
|------|-----------|------------|
| Ngày 1 | Tester 1, Tester 2 demo | Tester 3, Tester 4 demo |
| Ngày 2 | Tổng hợp bugs | Re-test & báo cáo |
