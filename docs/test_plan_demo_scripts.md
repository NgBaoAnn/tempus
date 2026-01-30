# 📋 TEMPUS - Test Plan & Demo Scripts

> **Ngày cập nhật:** 2026-01-31  
> **Tổng Use Cases:** 73  
> **Người Demo:** 1 người

---

## 📊 Tổng Quan Demo

| Module | Use Cases | Thời gian |
|--------|-----------|-----------|
| Onboarding & Authentication | UC01-UC05, UC72-UC73 | 8 phút |
| Timer & Focus | UC06-UC12 | 7 phút |
| Timeline & Schedule | UC13-UC21 | 10 phút |
| Gamification & Garden | UC22-UC27 | 6 phút |
| AI Chat | UC28-UC32 | 6 phút |
| Notes | UC33-UC38 | 5 phút |
| Social | UC39-UC47 | 8 phút |
| Settings | UC48-UC55 | 5 phút |
| Statistics & Heatmap | UC57, UC64-UC65 | 4 phút |
| Sync & Notification | UC66-UC71 | 5 phút |
| Voice | UC56 | 2 phút |
| **Tổng** | **73 UC** | **~66 phút** |

---

# 🎬 DEMO SCRIPTS

## 📝 Script #1: Onboarding & Authentication (8 phút)
**UC01-UC05, UC72-UC73**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Xóa data app, mở app | Màn hình Onboarding hiển thị |
| 2 | Swipe qua các slides | Slides chuyển tuần tự |
| 3 | Nhấn "Bắt đầu" | Chuyển đến màn hình Login |
| 4 | Nhấn "Đăng ký" | Mở RegisterActivity |
| 5 | Để trống email, nhấn Đăng ký | Lỗi validation |
| 6 | Nhập email không hợp lệ | Lỗi "Email không hợp lệ" |
| 7 | Nhập thông tin hợp lệ, nhấn Đăng ký | Tài khoản được tạo, sync data |
| 8 | Đăng xuất | Về Login screen |
| 9 | Nhập sai password | Lỗi "Sai email hoặc mật khẩu" |
| 10 | Nhập đúng thông tin | Đăng nhập thành công |
| 11 | Đăng xuất, nhấn "Quên mật khẩu" | Mở ResetPasswordActivity |
| 12 | Nhập email, gửi | Toast "Kiểm tra email", mở OTP screen |
| 13 | Nhập OTP (nếu có) | Xác thực thành công |
| 14 | Nhấn "Đăng nhập Google" | Google OAuth flow mở |
| 15 | Chọn tài khoản Google | Đăng nhập thành công |

---

## 📝 Script #2: Timer & Focus (7 phút)
**UC06-UC12**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở TimerFragment | Timer UI hiển thị |
| 2 | Chọn 5 phút | Thời gian được set |
| 3 | Nhấn Start | Timer đếm ngược, notification hiển thị |
| 4 | Nhấn Pause | Timer dừng |
| 5 | Nhấn Resume | Timer tiếp tục |
| 6 | Nhấn Cancel | Timer reset |
| 7 | Start lại với 2 phút | Timer bắt đầu |
| 8 | Bật Focus Lock | FocusLockActivity mở toàn màn hình |
| 9 | Nhấn Back | Không thoát được |
| 10 | Nhấn Unlock → xác nhận | Thoát Focus Lock |
| 11 | Để timer chạy hết | Âm thanh/rung, +2 điểm được cộng |
| 12 | Xem notification | Thông báo "Hoàn thành Pomodoro" |

---

## 📝 Script #3: Timeline & Schedule (10 phút)
**UC13-UC21**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở TimelineFragment | Tuần hiện tại hiển thị |
| 2 | Swipe trái/phải | Chuyển tuần |
| 3 | Tap header tháng | MonthCalendarDialog mở |
| 4 | Chọn tháng khác | Timeline nhảy đến tháng đó |
| 5 | Nhấn FAB (+) | EditScheduleFragment mở |
| 6 | Để trống tên, nhấn Lưu | Lỗi validation |
| 7 | Nhập: "Họp team", 14:00-15:00 | - |
| 8 | Thêm sub-task: "Chuẩn bị slides" | Sub-task được add |
| 9 | Thêm sub-task: "Review báo cáo" | Sub-task được add |
| 10 | Bật Reminder | Reminder được set |
| 11 | Nhấn Lưu | Task được tạo, xuất hiện trong timeline |
| 12 | Tap vào task → Sửa tên | Task được update |
| 13 | Tap checkbox sub-task | Sub-task done |
| 14 | Tap checkbox task chính | Task done, +10 điểm, streak tăng |
| 15 | Tạo task mới, chọn Skip | Status → skipped |
| 16 | Tạo task, nhấn Xóa | Dialog xác nhận, task bị xóa |

---

## 📝 Script #4: Gamification & Garden (6 phút)
**UC22-UC27, UC58-UC59**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Xem điểm trên header | Điểm hiện tại, streak hiển thị |
| 2 | Mở Timeline → Garden icon | GardenFragment mở |
| 3 | Xem vườn cây | Grid các cây hiển thị |
| 4 | Nhấn "Trồng cây mới" | Dialog chọn loại cây |
| 5 | Chọn Oak, đặt tên "My Tree" | Cây được tạo, -50 điểm |
| 6 | Xem cây state = SEED | Cây mới hiển thị trong garden |
| 7 | Tap vào cây | TreeDetailActivity mở |
| 8 | Nhấn "Tưới cây" 10 điểm | invested_points tăng |
| 9 | Tưới thêm vài lần | Cây phát triển, state có thể đổi |
| 10 | Mở Point History | Lịch sử cộng/trừ điểm hiển thị |

---

## 📝 Script #5: AI Chat (6 phút)
**UC28-UC32, UC63**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở AIFragment | Welcome message hiển thị |
| 2 | Mode = "Ask" | Indicator hiển thị |
| 3 | Gõ "Cho tôi mẹo quản lý thời gian" | Message gửi đi |
| 4 | Xem typing indicator | Loading animation |
| 5 | Nhận AI response | Bubble hiển thị với câu trả lời |
| 6 | Chuyển mode "Agent" | Mode changed |
| 7 | Gõ "Lên lịch học bài cho tuần này" | AI phân tích và đề xuất |
| 8 | Xem proposals | Schedule suggestions hiển thị |
| 9 | Accept 1 suggestion | Task được tạo tự động |
| 10 | Nhấn Clear Chat | Chat reset, welcome message mới |

---

## 📝 Script #6: Notes (5 phút)
**UC33-UC38**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Timer → Notes icon | NotesFragment mở |
| 2 | Nhấn + | Editor mở |
| 3 | Nhập title "Ghi chú quan trọng", content | - |
| 4 | Nhấn Save | Note được lưu |
| 5 | Tạo thêm 2 notes | 3 notes trong list |
| 6 | Search "quan trọng" | List được filter |
| 7 | Toggle pin note đầu | Note lên đầu danh sách |
| 8 | Edit note | Content updated |
| 9 | Delete 1 note | Note bị xóa |

---

## 📝 Script #7: Social (8 phút)
**UC39-UC47**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở SocialFragment | Tab Friends hiển thị |
| 2 | Xem friends list | Danh sách bạn bè |
| 3 | Tab Discover | Search users UI |
| 4 | Tìm username "testuser" | Kết quả hiển thị |
| 5 | Nhấn Add Friend | Request gửi đi, status "Pending" |
| 6 | Tab Requests | Xem pending requests |
| 7 | Accept 1 request | Thành bạn bè |
| 8 | Reject 1 request | Request bị xóa |
| 9 | Tap friend → View Profile | Profile hiển thị |
| 10 | Tap conversation | ChatScreen mở |
| 11 | Xem message history | Tin nhắn cũ hiển thị |
| 12 | Nhập và gửi message | Message gửi thành công |
| 13 | Nhấn Unfriend | Friendship bị hủy |

---

## 📝 Script #8: Settings (5 phút)
**UC48-UC55**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở SettingsFragment | Settings UI hiển thị |
| 2 | Mở Profile | ProfileActivity mở |
| 3 | Sửa username, nhấn Lưu | Username updated |
| 4 | Đổi avatar | Avatar được upload |
| 5 | Mở Personalization | PersonalizationActivity mở |
| 6 | Đặt wake_time, sleep_time | Thời gian được set |
| 7 | Mở Theme → chọn Dark | App đổi theme tối |
| 8 | Nhấn Privacy Policy | Browser mở URL |
| 9 | Nhấn "Export to JSON" | Share dialog mở, file được tạo |
| 10 | Nhấn "Delete All Data" | Dialog cảnh báo → Biometric → nhập "DELETE" |

---

## 📝 Script #9: Statistics & Heatmap (4 phút)
**UC57, UC64-UC65**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở StatisticsFragment | Charts hiển thị |
| 2 | Xem biểu đồ tuần | 7 ngày data, tasks done/skipped |
| 3 | Xem completion rate | % hoàn thành hiển thị |
| 4 | Nhấn Heatmap | HeatmapFragment mở |
| 5 | Xem lưới ngày theo tháng | Màu sắc thể hiện năng suất |
| 6 | Vuốt xem tháng khác | Tháng trước/sau hiển thị |
| 7 | Tap vào một ngày | Chuyển đến EditSchedule với ngày đó |

---

## 📝 Script #10: Sync & Notification (5 phút)
**UC66-UC71**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Đăng xuất | Data được push lên Supabase |
| 2 | Đăng nhập lại | Data được pull từ Supabase (schedules, points, trees, history) |
| 3 | Xem Statistics | Pomodoro history hiển thị đúng |
| 4 | Mở Settings → Sync Now | Progress indicator, sync thành công |
| 5 | Tạo task với Reminder | Alarm được đặt |
| 6 | Chờ đến giờ reminder | Notification nhắc nhở hiển thị |
| 7 | Start Pomodoro, để hết | Notification "Hoàn thành" hiển thị |

---

## 📝 Script #11: Voice Input (2 phút)
**UC56**

| Bước | Hành động | Kết quả mong đợi |
|------|-----------|------------------|
| 1 | Mở Timeline, nhấn microphone | Request permission (nếu chưa có) |
| 2 | Cấp quyền | Mic bắt đầu nghe |
| 3 | Nói "Họp nhóm lúc 3 giờ chiều" | Speech-to-text chuyển đổi |
| 4 | Xác nhận | Task được tạo với thời gian 15:00 |

---

# 📊 TỔNG KẾT

## Thời gian Demo

| Script | Module | Thời gian |
|--------|--------|-----------|
| #1 | Onboarding & Auth | 8 phút |
| #2 | Timer & Focus | 7 phút |
| #3 | Timeline & Schedule | 10 phút |
| #4 | Gamification & Garden | 6 phút |
| #5 | AI Chat | 6 phút |
| #6 | Notes | 5 phút |
| #7 | Social | 8 phút |
| #8 | Settings | 5 phút |
| #9 | Statistics & Heatmap | 4 phút |
| #10 | Sync & Notification | 5 phút |
| #11 | Voice | 2 phút |
| **Tổng** | **11 Scripts, 73 UC** | **~66 phút** |

---

## ✅ Checklist Chuẩn Bị

### Trước khi demo:
- [ ] Cài đặt app trên thiết bị
- [ ] Tạo ít nhất 2 tài khoản test (cho Social demo)
- [ ] Đảm bảo kết nối internet ổn định
- [ ] Tool quay màn hình
- [ ] Reset data app nếu demo fresh
- [ ] Có sẵn 1 tài khoản Google

### Thiết bị cần:
- [ ] Android phone (API 26+)
- [ ] Microphone hoạt động (voice test)
- [ ] Fingerprint/PIN setup (biometric test)

---

## 🐛 Bug Report Template

```markdown
## Bug ID: BUG-XXX
### Use Case: UC##
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

## 📅 Lịch Trình Demo Đề Xuất

| Phần | Nội dung | Thời gian |
|------|----------|-----------|
| Phần 1 | Scripts #1-#4 (Auth, Timer, Timeline, Gamification) | ~31 phút |
| Nghỉ giữa giờ | - | 5 phút |
| Phần 2 | Scripts #5-#8 (AI, Notes, Social, Settings) | ~24 phút |
| Nghỉ | - | 5 phút |
| Phần 3 | Scripts #9-#11 (Stats, Sync, Voice) | ~11 phút |
| Q&A | Hỏi đáp | 10 phút |
| **Tổng** | | **~86 phút** |
