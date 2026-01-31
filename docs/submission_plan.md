# 📦 KẾ HOẠCH NỘP BÀI - TEMPUS
> **App Name:** Tempus - Ứng dụng quản lý thời gian và tập trung  
> **Team Size:** 5 người (Bảo An, Thế Vinh, Quang Vinh, Nhật Đạt, Huy)  
> **Tech Stack:** Kotlin + XML, MVVM, Room + Supabase, Navigation Component  
> **Deadline:** [Cần điền - giả định 2026-02-05]  
> **Created:** 2026-01-31

---

## I. FOLDER STRUCTURE ĐỀ XUẤT

```
TEMPUS_SUBMISSION/
│
├── 📁 Mobile/                              # Source code Android
│   ├── app/
│   │   ├── src/
│   │   ├── build.gradle.kts
│   │   └── ...
│   ├── gradle/
│   ├── build.gradle.kts
│   ├── settings.gradle.kts
│   ├── gradlew, gradlew.bat
│   ├── google-services.json              # [CRITICAL] Firebase config
│   └── local.properties                   # SDK path (gitignored)
│
├── 📁 Docs/                                # Tài liệu báo cáo
│   ├── 01_BaoCaoDoAn.pdf                   # Báo cáo chính (QUAN TRỌNG)
│   ├── 02_DanhGiaDuAn_Template.docx        # File đánh giá theo template
│   ├── 03_UseCases.pdf                     # Tài liệu use cases
│   ├── 04_DatabaseSchema.pdf               # Thiết kế database
│   ├── 05_TestPlan.pdf                     # Test plan & demo scripts
│   ├── 06_TeamTaskDivision.pdf             # Phân công công việc
│   └── README_REPORT.txt                   # Ghi chú về docs
│
├── 📁 Data/                                # Dữ liệu demo & backup
│   ├── supabase_export/
│   │   ├── schedules.json                  # Export bảng schedules
│   │   ├── schedule_items.json             # Export bảng schedule_items
│   │   ├── user_points.json                # Export điểm user
│   │   ├── trees.json                      # Export cây trong garden
│   │   ├── notes.json                      # Export ghi chú
│   │   └── users_sample.json               # Sample user data (anonymized)
│   ├── room_export/
│   │   └── tempus_database.db              # Room database backup
│   └── README_DATA.txt                     # Hướng dẫn import data
│
├── 📁 Assets/                              # Tài nguyên demo
│   ├── screenshots/                        # Ảnh chụp màn hình từng chức năng
│   │   ├── 01_onboarding.png
│   │   ├── 02_login.png
│   │   ├── 03_timeline.png
│   │   ├── ... (mỗi feature 1-2 ảnh)
│   │   └── _feature_index.md
│   ├── videos/                             # Video demo ngắn (nếu có)
│   │   └── demo_full_flow.mp4
│   └── icons/                              # Icons tự thiết kế
│       └── app_icon.svg
│
├── 📁 Releases/                            # APK & build artifacts
│   ├── tempus-release-v1.0.apk             # APK release (signed)
│   ├── tempus-debug.apk                    # APK debug (backup)
│   ├── mapping.txt                         # ProGuard mapping
│   └── README_INSTALL.txt                  # Hướng dẫn cài APK
│
└── 📄 README.md (hoặc README.pdf)          # FILE QUAN TRỌNG NHẤT
    # Nội dung:
    # 1. Giới thiệu app
    # 2. Yêu cầu hệ thống
    # 3. Hướng dẫn Developer (build từ source)
    # 4. Hướng dẫn User (cài APK)
    # 5. Tài khoản test
    # 6. Lưu ý permission
    # 7. Nguồn tham khảo
```

---

## II. CHIA VIỆC CHO 5 THÀNH VIÊN

### 📋 Bảng Phân Công Tổng Hợp

| Owner | Role | % Đóng góp | Task List Chi Tiết | Output Files | Definition of Done | Review/QA | Deadline |
|-------|------|------------|-------------------|--------------|-------------------|-----------|----------|
| **Bảo An** | AI Developer / Report Lead | 20% | - Viết mục 4.x: AI Chat (UC28-32)<br>- Viết mục "Điểm đặc biệt" về AI integration<br>- Export AI history JSON<br>- Chụp screenshots AI features<br>- Viết mục Tham khảo (APIs) | - Phần 4.1-4.5 trong báo cáo<br>- `/Assets/screenshots/ai_*.png`<br>- `/Data/supabase_export/ai_history.json` | Có screenshots + luồng + kỹ thuật cho mỗi chức năng AI | Thế Vinh | D-2 |
| **Thế Vinh** | Backend Lead / Report Editor | 25% | - **Tổng hợp & Edit báo cáo cuối**<br>- Viết mục 3.x: Timeline, Notes (UC13-21, UC33-38)<br>- Viết mục 5: Database Schema<br>- Viết mục Sync (UC66-69)<br>- Export ALL Supabase tables<br>- Room database backup | - `01_BaoCaoDoAn.pdf` (final)<br>- `04_DatabaseSchema.pdf`<br>- `/Data/supabase_export/*.json`<br>- `/Data/room_export/` | Có tất cả JSON exports + DB schema + báo cáo final | Nhật Đạt | D-1 |
| **Quang Vinh** | Game Developer | 15% | - Viết mục 4.x: Gamification (UC22-27)<br>- Viết mục Garden UI flow<br>- Export trees & points data<br>- Chụp screenshots Garden | - Phần Gamification trong báo cáo<br>- `/Data/supabase_export/trees.json`<br>- `/Data/supabase_export/user_points.json`<br>- `/Assets/screenshots/garden_*.png` | Screenshots Garden + Tree detail + Points | Bảo An | D-2 |
| **Nhật Đạt** | Core Features / QA Lead | 22% | - Viết mục 2.x: Timer, Focus Lock (UC06-12)<br>- Viết mục Statistics, Heatmap (UC57, UC64-65)<br>- **Build APK release & debug**<br>- Test toàn bộ app trước nộp<br>- Viết `README_INSTALL.txt` | - Phần Timer/Stats trong báo cáo<br>- `/Releases/tempus-*.apk`<br>- `/Releases/README_INSTALL.txt`<br>- Test report | APK chạy được + test pass | Huy | D-1 |
| **Huy** | Auth/Settings / README Lead | 18% | - Viết mục 1: Mô tả dự án (1.1)<br>- Viết mục 1.2: Đóng góp thành viên<br>- Viết mục 1.3: Hướng dẫn thực thi<br>- Viết mục Auth/Settings (UC01-05, UC48-55)<br>- **Viết README.md chính**<br>- Điền template đánh giá .docx<br>- Viết Self-evaluation | - Phần 1.1-1.3 trong báo cáo<br>- `README.md`<br>- `02_DanhGiaDuAn_Template.docx`<br>- `/Assets/screenshots/auth_*.png` | README có đủ 7 mục + Template điền đủ | Thế Vinh | D-2 |

---

### 📝 Chi Tiết Task Từng Người

#### 👤 BẢO AN (AI Developer)
```markdown
TASK LIST:
□ Viết phần 4.1: UC28 - Chat với AI (Ask Mode)
  - Luồng sự kiện chính (5-6 dòng)
  - Kỹ thuật: OpenAI API, Retrofit, Coroutines, ChatAdapter
□ Viết phần 4.2: UC29 - Tạo lịch với AI (Agent Mode) 
□ Viết phần 4.3: UC30 - Life Planner Mode
□ Viết phần 4.4: UC31 - Chấp nhận gợi ý AI
□ Viết phần 4.5: UC32 - Xóa lịch sử chat
□ Viết phần "Điểm đặc biệt": OpenAI integration
□ Export chat history từ Supabase
□ Chụp 5 screenshots AI features

OUTPUT: ai_features.docx → gửi Thế Vinh merge
```

#### 👤 THẾ VINH (Backend Lead / Editor)
```markdown
TASK LIST:
□ Tổng hợp tất cả phần của thành viên vào 1 file
□ Viết phần Timeline: UC13-21 (9 use cases)
□ Viết phần Notes: UC33-38 (6 use cases)
□ Viết phần Sync: UC66-69 (background sync, WorkManager)
□ Viết phần Database Schema (Room + Supabase)
□ Export ALL JSON từ Supabase:
  - schedules.json
  - schedule_items.json
  - edited_version.json
  - notes.json
  - users (anonymized)
□ Backup Room database (.db file)
□ Format và xuất PDF báo cáo final

OUTPUT: 01_BaoCaoDoAn.pdf, 04_DatabaseSchema.pdf, /Data/*
```

#### 👤 QUANG VINH (Game Developer)
```markdown
TASK LIST:
□ Viết phần Gamification: UC22-27
  - Xem điểm số
  - Xem lịch sử điểm
  - Trồng cây mới
  - Tưới cây
  - Xem chi tiết cây
  - Xem Garden
□ Viết phần System tự động: UC58-62
□ Export trees.json và user_points.json
□ Chụp screenshots:
  - Garden grid view
  - Tree detail
  - Point history
  - Tree states (seed, sprout, sapling, tree, dead)

OUTPUT: gamification.docx → gửi Thế Vinh merge
```

#### 👤 NHẬT ĐẠT (Core Features / QA)
```markdown
TASK LIST:
□ Viết phần Timer: UC06-12
  - Bắt đầu/Tạm dừng/Tiếp tục Pomodoro
  - Focus Lock mode
□ Viết phần Statistics: UC57
□ Viết phần Heatmap: UC64-65
□ Viết phần Voice: UC56
□ Viết phần Notification: UC70-71
□ Build APK release (signed)
  - Tạo keystore nếu chưa có
  - Build ./gradlew assembleRelease
  - Test APK trên 2-3 thiết bị
□ Build APK debug (backup)
□ Viết README_INSTALL.txt
□ Test toàn bộ app và ghi report

OUTPUT: timer_stats.docx, /Releases/*.apk, test_report.md
```

#### 👤 HUY (Auth/Settings / README Lead)
```markdown
TASK LIST:
□ Viết phần 0: Tự đánh giá đồ án (x/10 + lý do)
□ Viết phần 1.1: Mô tả dự án
  - Tên dự án
  - Môi trường thực thi
  - Mục tiêu
  - Lý do ra đời
  - App tương tự
  - Điểm khác biệt
□ Viết phần 1.2: Đóng góp thành viên (bảng %)
□ Viết phần 1.3: Hướng dẫn thực thi
□ Viết phần Authentication: UC01-05, UC72-73
□ Viết phần Settings: UC48-55
□ Điền template đánh giá .docx
□ Viết README.md chính (7 mục)
□ Chụp screenshots auth flow

OUTPUT: intro_auth.docx, README.md, 02_DanhGiaDuAn.docx
```

---

## III. TIMELINE THEO NGÀY (5 NGÀY)

> **Giả định Deadline:** 2026-02-05 23:59  
> **Ngày bắt đầu:** 2026-01-31

| Ngày | Date | Deliverables | Owner(s) | Checkpoint |
|------|------|--------------|----------|------------|
| **D-5** | 31/01 (Thứ 6) | - Kick-off meeting, phân công<br>- Setup folder structure<br>- Export JSON data từ Supabase<br>- Bắt đầu viết content | Thế Vinh (lead) | ✓ Folder created<br>✓ JSON exported |
| **D-4** | 01/02 (Thứ 7) | - AI features content hoàn thành<br>- Gamification content hoàn thành<br>- Timer/Stats content hoàn thành | Bảo An, Quang Vinh, Nhật Đạt | ✓ 3 file .docx submit |
| **D-3** | 02/02 (CN) | - Auth/Settings content hoàn thành<br>- README.md draft<br>- Template đánh giá draft<br>- Build APK debug test | Huy, Nhật Đạt | ✓ README draft<br>✓ APK debug works |
| **D-2** | 03/02 (Thứ 2) | - Timeline/Notes/Sync content hoàn thành<br>- Database schema doc<br>- **Cross-review tất cả nội dung**<br>- Build APK release | Thế Vinh, Nhật Đạt | ✓ All content submitted<br>✓ APK release tested |
| **D-1** | 04/02 (Thứ 3) | - Merge tất cả vào báo cáo chính<br>- Format & xuất PDF final<br>- Final folder check<br>- Zip và test unzip | Thế Vinh (merge), ALL | ✓ PDF final<br>✓ Folder complete |
| **D-0** | 05/02 (Thứ 4) | - **NỘP BÀI**<br>- Buffer time cho issue | ALL | ✓ SUBMITTED |

### 📅 Daily Stand-up (15 phút)
- **Thời gian:** 21:00 hàng ngày (Discord/Zalo)
- **Format:** 
  1. Hôm nay xong gì?
  2. Mai làm gì?
  3. Có blocker không?

---

## IV. READY-TO-SUBMIT CHECKLIST

### ✅ Folder Structure Check
```
□ /Mobile/               - có đủ source code
□ /Mobile/app/           - có thể mở bằng Android Studio
□ /Docs/                 - có ít nhất 4 file PDF
□ /Data/supabase_export/ - có ít nhất 5 file JSON
□ /Assets/screenshots/   - có ít nhất 15 screenshots
□ /Releases/             - có ít nhất 1 APK
□ README.md              - tồn tại và đầy đủ
```

### ✅ Build & Run Check
```
□ Mở project bằng Android Studio → Sync thành công
□ Build > Make Project → BUILD SUCCESSFUL
□ Run app trên emulator → App chạy được
□ Login với test account → Pass
□ APK cài được trên device thật → Pass
```

### ✅ Documentation Check
```
□ Báo cáo có mục "Tự đánh giá" với điểm x/10
□ Báo cáo có bảng đóng góp thành viên với %
□ Mỗi chức năng có "Luồng sự kiện" + "Kỹ thuật"
□ README có hướng dẫn cho Developer
□ README có hướng dẫn cho User
□ README có tài khoản test
□ README có danh sách permission
□ README có nguồn tham khảo
```

### ✅ Data Check
```
□ schedules.json không rỗng
□ Có ít nhất 10 sample tasks trong JSON
□ Có sample user points data
□ Có sample trees/garden data
□ Có sample notes
```

### ✅ APK Check
```
□ APK có thể cài được (không lỗi parse)
□ APK đã signed (release)
□ APK chạy được trên Android 8.0+ (API 26+)
□ APK size hợp lý (< 50MB)
```

### ✅ Final Check (trước khi zip)
```
□ Xóa thư mục .gradle, .idea, build/ trong /Mobile
□ Xóa local.properties (chứa SDK path cá nhân)
□ Kiểm tra không có file nhạy cảm (API key, password)
□ Zip thử và unzip để verify
□ Đặt tên file: TEMPUS_[MãNhóm].zip
```

---

## V. RISK LIST (TOP 8 RỦI RO + CÁCH CHẶN)

| # | Rủi ro | Impact | Xác suất | Cách phòng ngừa | Owner |
|---|--------|--------|----------|-----------------|-------|
| 1 | **Thiếu JSON export từ Supabase** | HIGH | Medium | - Export ngay ngày D-5<br>- Backup lên Drive | Thế Vinh |
| 2 | **Build APK fail do thiếu key/config** | HIGH | Medium | - Check google-services.json<br>- Tạo keystore sớm<br>- Test build ngày D-4 | Nhật Đạt |
| 3 | **Thiếu README hoặc README không đủ** | HIGH | Low | - Có template sẵn<br>- Huy phụ trách chính | Huy |
| 4 | **Thiếu bảng đóng góp thành viên** | HIGH | Low | - Huy điền bảng ngay<br>- Review % hợp lý | Huy |
| 5 | **Thiếu điểm tự đánh giá** | HIGH | Low | - Mục đầu tiên Huy viết<br>- Cả nhóm thống nhất điểm | Huy + ALL |
| 6 | **Content không đủ "Luồng + Kỹ thuật"** | MEDIUM | Medium | - Template mẫu cho mỗi UC<br>- Cross-review ngày D-2 | ALL |
| 7 | **Screenshots thiếu hoặc mờ** | MEDIUM | Low | - Mỗi người tự chụp phần mình<br>- Quy chuẩn: PNG, 1080p | ALL |
| 8 | **Conflict merge báo cáo** | MEDIUM | Medium | - Mỗi người viết file riêng<br>- Thế Vinh merge cuối cùng<br>- Dùng Google Docs để review | Thế Vinh |

### 🔥 Contingency Plan

| Tình huống | Hành động |
|------------|-----------|
| Build fail vào D-1 | → Nộp APK debug + ghi chú trong README |
| Thiếu 1 người không submit content | → Người review take over + ghi chú trong báo cáo |
| Supabase export fail | → Export từ Room DB + screenshots dashboard |
| Không kịp format PDF | → Nộp DOCX + convert sau |

---

## VI. MERGED OUTLINE (Format Chấm + Template Nhóm)

Cấu trúc báo cáo cuối cùng (merge 2 hệ outline):

```markdown
# TEMPUS - BÁO CÁO ĐỒ ÁN MOBILE ANDROID

## 0. TỰ ĐÁNH GIÁ ĐỒ ÁN ⭐ [BẮT BUỘC]
- Điểm tự đánh giá: [X]/10
- Tiêu chí và lý do (5 bullets)

## 1. MÔ TẢ DỰ ÁN (Template 1.1)
### 1.1. Thông tin chung
- Tên dự án
- Môi trường thực thi
- Mục tiêu chương trình
- Lý do ra đời
- App tương tự + ưu nhược
- Điểm khác biệt

### 1.2. Đóng góp thành viên ⭐ [BẮT BUỘC]
- Bảng: STT / MSSV / Họ tên / Tỉ lệ (%)
- Mô tả chi tiết ai làm gì

### 1.3. Hướng dẫn thực thi ⭐ [BẮT BUỘC]
- Dành cho Developer
- Dành cho User

## 2. CẤU TRÚC TỔNG THỂ ỨNG DỤNG (Template mục 2)
- Kiến trúc MVVM
- Cấu trúc thư mục

## 3. TÍNH NĂNG THEO TAB (Template mục 3.1-3.5)
### 3.1. Authentication (UC01-05, UC72-73)
### 3.2. Timer & Focus (UC06-12)
### 3.3. Timeline & Schedule (UC13-21)
### 3.4. AI Chat (UC28-32)
### 3.5. Notes (UC33-38)
### 3.6. Gamification & Garden (UC22-27)
### 3.7. Settings (UC48-55)
### 3.8. Statistics & Heatmap (UC57, UC64-65)
### 3.9. Voice Input (UC56)
### 3.10. Notifications (UC70-71)

> ⚠️ MỖI MỤC PHẢI CÓ:
> - Luồng sự kiện chính (3-8 dòng)
> - Kỹ thuật sử dụng (components, libraries)

## 4. KIẾN TRÚC & TECH STACK (Template mục 4.1-4.2)
### 4.1. Tech Stack
### 4.2. Libraries & Dependencies

## 5. THIẾT KẾ DATABASE (Template mục 5.1-5.8)
### 5.1. Room Database (Local)
### 5.2. Supabase Database (Remote)
### 5.3. Sync Strategy (UC66-69)

## 6. FLOW ỨNG DỤNG (Template mục 6.1-6.6)
- Kết hợp với use cases

## 7. ĐIỂM ĐẶC BIỆT TRONG ĐỒ ÁN ⭐ [GÂY ẤN TƯỢNG]
- AI Integration
- Offline-first architecture
- Gamification system
- Background sync với WorkManager
- Focus Lock mode

## 8. THAM KHẢO ⭐ [BẮT BUỘC]
### 8.1. APIs
### 8.2. Libraries
### 8.3. Tài liệu tham khảo
```

---

## 📎 APPENDIX: Template Mẫu Cho Mỗi Chức Năng

```markdown
### 3.X. [Tên Chức Năng] (UCxx)

**Luồng sự kiện chính:**
1. User [hành động]
2. App [phản hồi]
3. User [tiếp tục]
4. App [kết quả]
5. [Hoàn thành/Lưu dữ liệu]

**Kỹ thuật sử dụng:**
- **UI:** Fragment, ViewBinding, RecyclerView + DiffUtil
- **Architecture:** ViewModel, LiveData/StateFlow
- **Database:** Room DAO, Entity
- **Networking:** Ktor/Retrofit, Coroutines
- **Libraries:** [Liệt kê cụ thể]
```

---

> **Lưu ý cuối:** Yêu cầu ban đầu nêu 4 người nhưng nhóm có 5 người. Kế hoạch này chia cho 5 người theo đúng phân công hiện tại trong `team_task_division.md`. Nếu cần điều chỉnh cho 4 người, vui lòng cho biết ai sẽ không tham gia.
