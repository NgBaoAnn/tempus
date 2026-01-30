# 📋 TEMPUS - Phân Chia Công Việc

> **Ngày tạo:** 2026-01-31  
> **Tổng thành viên:** 5 người  
> **Tổng Use Cases:** 73

---

## 👥 Bảng Phân Công

| Thành viên | Vai trò chính | Modules | Use Cases | Số UC |
|------------|---------------|---------|-----------|-------|
| **Bảo An** | AI Developer | AI Chat, Social | UC28-32, UC39-47, UC63 | 15 |
| **Thế Vinh** | Backend/Data | Timeline, Notes, Sync | UC13-21, UC33-38, UC66-69 | 19 |
| **Quang Vinh** | Game Developer | Gamification, Garden, System | UC22-27, UC58-62 | 11 |
| **Nhật Đạt** | Core Features | Timer, Statistics, Heatmap, Voice, Notification | UC06-12, UC49, UC56-57, UC64-65, UC70-71 | 16 |
| **Huy** | Auth/Settings | Authentication, Settings, Onboarding | UC01-05, UC48, UC50-55, UC72-73 | 12 |

---

# 👤 BẢO AN - AI & Social

## Phần chính: AI Chat
## Phần phụ: Social (Friends, Messages)

### Modules phụ trách:
| Module | Files chính |
|--------|-------------|
| AI Chat | `ui/ai/`, `data/ai/` |
| Social | `ui/social/`, `data/social/` |

### Use Cases (15 UC):
| UC | Tên | Mô tả |
|----|-----|-------|
| UC28 | Chat với AI (Ask Mode) | Hỏi đáp tự do với AI |
| UC29 | Tạo lịch với AI (Agent Mode) | AI gợi ý schedule |
| UC30 | Life Planner Mode | AI lên kế hoạch dài hạn |
| UC31 | Chấp nhận gợi ý AI | Accept AI suggestions |
| UC32 | Xóa lịch sử chat | Clear chat history |
| UC39 | Xem danh sách bạn bè | Friends list |
| UC40 | Tìm kiếm người dùng | Search users |
| UC41 | Gửi lời mời kết bạn | Send friend request |
| UC42 | Chấp nhận lời mời | Accept request |
| UC43 | Từ chối lời mời | Reject request |
| UC44 | Hủy kết bạn | Unfriend |
| UC45 | Chặn người dùng | Block user |
| UC46 | Mở cuộc hội thoại | Open chat |
| UC47 | Gửi tin nhắn | Send message |
| UC63 | [System] Lưu AI history | Auto-save AI history |

---

# 👤 THẾ VINH - Timeline & Data Layer

## Phần chính: Room Database, Timeline
## Phần phụ: Notes, Sync

### Modules phụ trách:
| Module | Files chính |
|--------|-------------|
| Timeline | `TimeLineFragment.kt`, `ui/timeline/`, `data/schedule/` |
| Notes | `ui/notes/`, `data/notes/` |
| Room DB | `data/local/`, `*Dao.kt`, `*Database.kt` |
| Sync | `data/sync/`, `service/SyncScheduleWorker.kt` |

### Use Cases (19 UC):
| UC | Tên | Mô tả |
|----|-----|-------|
| UC13 | Xem lịch trình theo tuần | Week view |
| UC14 | Xem lịch trình theo tháng | Month calendar |
| UC15 | Tạo task mới | Create schedule |
| UC16 | Chỉnh sửa task | Edit schedule |
| UC17 | Xóa task | Delete schedule |
| UC18 | Đánh dấu hoàn thành | Mark as done |
| UC19 | Bỏ qua task | Skip task |
| UC20 | Tạo sub-task | Create sub-task |
| UC21 | Đánh dấu sub-task hoàn thành | Complete sub-task |
| UC33 | Xem danh sách ghi chú | Notes list |
| UC34 | Tạo ghi chú mới | Create note |
| UC35 | Chỉnh sửa ghi chú | Edit note |
| UC36 | Xóa ghi chú | Delete note |
| UC37 | Tìm kiếm ghi chú | Search notes |
| UC38 | Ghim ghi chú | Pin note |
| UC66 | Đồng bộ khi đăng nhập | Pull data on login |
| UC67 | Đồng bộ khi đăng xuất | Push data on logout |
| UC68 | Đồng bộ thủ công | Manual sync |
| UC69 | Đồng bộ nền tự động | Background sync |

---

# 👤 QUANG VINH - Gamification & Garden

## Phần chính: Game System (Points, Trees, Streak)
## Phần phụ: System Automation

### Modules phụ trách:
| Module | Files chính |
|--------|-------------|
| Gamification | `data/gamification/`, `domain/model/Tree*`, `domain/usecase/PointsManager.kt` |
| Garden | `ui/garden/` |

### Use Cases (11 UC):
| UC | Tên | Mô tả |
|----|-----|-------|
| UC22 | Xem điểm số | View points, level |
| UC23 | Xem lịch sử điểm | Point history |
| UC24 | Trồng cây mới | Plant tree |
| UC25 | Tưới cây | Water tree |
| UC26 | Xem chi tiết cây | Tree detail |
| UC27 | Xem Garden | Garden grid view |
| UC58 | [System] Cập nhật streak | Auto update streak |
| UC59 | [System] Kiểm tra cây chết | Check dead trees |
| UC60 | [System] Lưu timer session | Log timer sessions |
| UC61 | [System] Tính điểm Pomodoro | Calculate points |
| UC62 | [System] Chuyển chế độ Break | Switch to break mode |

---

# 👤 NHẬT ĐẠT - Timer & Statistics

## Phần chính: Pomodoro Timer, Statistics, Personalization
## Phần phụ: Voice, Notification, Heatmap

### Modules phụ trách:
| Module | Files chính |
|--------|-------------|
| Timer | `TimerFragment.kt`, `ui/timer/`, `data/timer/`, `service/focus/` |
| Focus Lock | `ui/focus/` |
| Statistics | `StatisticsFragment.kt`, `StatisticsViewModel.kt` |
| Heatmap | `ui/heatmap/` |
| Voice | `ui/voice/`, `data/voice/` |
| Notification | `service/ReminderReceiver.kt`, `service/TimelineAlarmManager.kt` |
| Personalization | `ui/setting/PersonalizationActivity.kt` |

### Use Cases (16 UC):
| UC | Tên | Mô tả |
|----|-----|-------|
| UC06 | Bắt đầu phiên tập trung | Start Pomodoro |
| UC07 | Tạm dừng phiên | Pause timer |
| UC08 | Tiếp tục phiên | Resume timer |
| UC09 | Hủy phiên | Cancel timer |
| UC10 | Hoàn thành phiên | Complete Pomodoro |
| UC11 | Bật Focus Lock | Enable Focus Lock |
| UC12 | Mở khóa Focus Lock | Unlock Focus Lock |
| UC49 | Cài đặt Personalization | Wake/sleep time, persona |
| UC56 | Tạo task bằng giọng nói | Voice input |
| UC57 | Xem thống kê hoàn thành | Statistics charts |
| UC64 | Xem Heatmap năng suất | Productivity heatmap |
| UC65 | Xem chi tiết ngày từ Heatmap | Heatmap day detail |
| UC70 | Thông báo hoàn thành Pomodoro | Pomodoro notification |
| UC71 | Thông báo nhắc nhở công việc | Task reminder notification |

---

# 👤 HUY - Authentication & Settings

## Phần chính: Login/Register, Settings
## Phần phụ: Onboarding

### Modules phụ trách:
| Module | Files chính |
|--------|-------------|
| Auth | `ui/auth/`, `data/auth/` |
| Onboarding | `ui/onboarding/` |
| Settings | `SettingsFragment.kt`, `ui/setting/` |
| User | `data/user/` |
| Export | `data/export/` |

### Use Cases (12 UC):
| UC | Tên | Mô tả |
|----|-----|-------|
| UC01 | Xem Onboarding | First-time onboarding |
| UC02 | Đăng ký tài khoản | Register |
| UC03 | Đăng nhập | Login |
| UC04 | Quên mật khẩu | Forgot password |
| UC05 | Đăng xuất | Logout |
| UC48 | Xem/Sửa Profile | Edit profile |
| UC50 | Đổi Theme | Change theme |
| UC51 | Export JSON | Export to JSON |
| UC52 | Export CSV | Export to CSV |
| UC53 | Xóa toàn bộ dữ liệu | Delete all data |
| UC54 | Xem Privacy Policy | Privacy policy |
| UC55 | Xem Terms of Service | Terms |
| UC72 | Đăng nhập Google | Google OAuth |
| UC73 | Xác thực OTP | OTP verification |

---

# 📊 TỔNG KẾT

## Phân bố Use Cases

```
Bảo An     ████████████████ 15 UC (20%)
Thế Vinh   ████████████████████████ 19 UC (26%)
Quang Vinh ████████████ 11 UC (15%)
Nhật Đạt   █████████████████ 16 UC (22%)
Huy        █████████████ 12 UC (16%)
           ─────────────────────────────
           Tổng: 73 UC (100%)
```

## Folder Structure theo phân công

```
app/src/main/java/com/projectapp/tempus/
├── ui/
│   ├── ai/              → Bảo An
│   ├── social/          → Bảo An
│   ├── timeline/        → Thế Vinh
│   ├── notes/           → Thế Vinh
│   ├── garden/          → Quang Vinh
│   ├── focus/           → Nhật Đạt
│   ├── timer/           → Nhật Đạt
│   ├── heatmap/         → Nhật Đạt
│   ├── statistics/      → Nhật Đạt
│   ├── voice/           → Nhật Đạt
│   ├── auth/            → Huy
│   ├── setting/         → Huy (Personalization → Nhật Đạt)
│   └── onboarding/      → Huy
├── data/
│   ├── ai/              → Bảo An
│   ├── social/          → Bảo An
│   ├── schedule/        → Thế Vinh
│   ├── notes/           → Thế Vinh
│   ├── sync/            → Thế Vinh
│   ├── local/           → Thế Vinh
│   ├── gamification/    → Quang Vinh
│   ├── timer/           → Nhật Đạt
│   ├── voice/           → Nhật Đạt
│   ├── auth/            → Huy
│   ├── user/            → Huy
│   └── export/          → Huy
├── domain/
│   ├── model/Tree*      → Quang Vinh
│   └── usecase/         → Shared
└── service/
    ├── focus/           → Nhật Đạt
    ├── Reminder*        → Nhật Đạt
    └── Sync*            → Thế Vinh
```

---

##  Lưu ý khi làm việc

1. **Code review**: Khi sửa file chung, thông báo cho người phụ trách
2. **Conflict resolution**: Ưu tiên người phụ trách chính giải quyết
3. **Testing**: Mỗi người tự test module của mình trước khi merge
4. **Documentation**: Cập nhật comment/docs khi thay đổi logic
