# 📋 Tiramisu - Developer Task Tracking

> **Last Updated:** 2026-01-07  
> **Timeline:** 4 weeks  
> **Team Size:** 5 developers

---

## 👤 Developer 1 - AI & Smart Features

**Focus:** AI Integration, Chat UI, Notes  
**Kỹ thuật showcase:** Retrofit, Coroutines, RecyclerView Multi-ViewType, Room

### Sprint Backlog

| # | Task | Priority | Points | Status | Due |
|---|------|----------|--------|--------|-----|
| 1.1 | [Setup OpenAI API service](#11-openai-api-service) | 🔴 HIGH | 5 | [ ] TODO | Week 1 |
| 1.2 | [Chat UI với RecyclerView](#12-chat-ui) | 🔴 HIGH | 5 | [ ] TODO | Week 1 |
| 1.3 | [Parse AI → Schedule suggestions](#13-ai-schedule-parsing) | 🔴 HIGH | 5 | [ ] TODO | Week 2 |
| 1.4 | [Quick Notes - Room + UI](#14-quick-notes) | 🟡 MED | 3 | [ ] TODO | Week 3 |
| 1.5 | [Daily Motivation Quote](#15-daily-quote) | 🟢 LOW | 2 | [ ] TODO | Week 4 |

### Task Details

#### 1.1 OpenAI API Service
```kotlin
// File: data/ai/OpenAIService.kt
interface OpenAIService {
    @POST("v1/chat/completions")
    suspend fun chat(@Body request: ChatRequest): ChatResponse
}

// Files to create:
// - data/ai/dto/ChatRequest.kt
// - data/ai/dto/ChatResponse.kt  
// - data/ai/AIRepository.kt
```
**Acceptance Criteria:**
- [ ] API key stored securely (BuildConfig)
- [ ] Request/Response DTOs defined
- [ ] Error handling implemented
- [ ] Unit tests written

---

#### 1.2 Chat UI
```kotlin
// File: ui/ai/ChatAdapter.kt
// ViewTypes: USER_MESSAGE, AI_MESSAGE, LOADING

// Files to create:
// - ui/ai/AIViewModel.kt
// - ui/ai/ChatAdapter.kt
// - res/layout/item_chat_user.xml
// - res/layout/item_chat_ai.xml
```
**Acceptance Criteria:**
- [ ] User can type and send messages
- [ ] AI responses display with typing indicator
- [ ] Scroll to bottom on new message
- [ ] Handle loading states

---

#### 1.3 AI Schedule Parsing
```kotlin
// File: domain/usecase/SuggestScheduleUseCase.kt
// Parse AI response → List<ScheduleSuggestion>
// User can accept/reject suggestions
```
**Acceptance Criteria:**
- [ ] AI returns structured schedule suggestions
- [ ] User can preview suggestions before applying
- [ ] Suggestions create real tasks in database

---

#### 1.4 Quick Notes
```kotlin
// Room Entity + DAO
// Files:
// - data/notes/NoteEntity.kt
// - data/notes/NoteDao.kt
// - ui/notes/NotesFragment.kt
// - ui/notes/NoteAdapter.kt
```
**Acceptance Criteria:**
- [ ] Add/Edit/Delete notes
- [ ] Notes saved to local Room DB
- [ ] Search notes by content

---

#### 1.5 Daily Quote
```kotlin
// Simple random quote from local list or API
// Display in home/timeline screen header
```
**Acceptance Criteria:**
- [ ] Quote changes daily
- [ ] Fallback to local quotes if API fails

---

## 👤 Developer 2 - Notifications & Background Services

**Focus:** Reminders, Pomodoro, Widget  
**Kỹ thuật showcase:** AlarmManager, ForegroundService, WorkManager, AppWidget

### Sprint Backlog

| # | Task | Priority | Points | Status | Due |
|---|------|----------|--------|--------|-----|
| 2.1 | [Notification System](#21-notification-system) | 🔴 HIGH | 8 | [ ] TODO | Week 1 |
| 2.2 | [Pomodoro ForegroundService](#22-pomodoro-service) | 🔴 HIGH | 8 | [ ] TODO | Week 2 |
| 2.3 | [Timer Session Logging](#23-timer-sessions) | 🟡 MED | 3 | [ ] TODO | Week 3 |
| 2.4 | [Home Screen Widget](#24-widget) | 🟡 MED | 5 | [ ] TODO | Week 4 |
| 2.5 | [Auto-start next session](#25-auto-start) | 🟢 LOW | 2 | [ ] TODO | Week 4 |

### Task Details

#### 2.1 Notification System
```kotlin
// Files to create:
// - service/NotificationHelper.kt
// - service/ReminderReceiver.kt (BroadcastReceiver)
// - service/ReminderScheduler.kt

// AndroidManifest additions:
// - POST_NOTIFICATIONS permission
// - SCHEDULE_EXACT_ALARM permission
// - <receiver> declaration
```
**Acceptance Criteria:**
- [ ] Notification channel created for Android 8+
- [ ] Alarm scheduled when task created
- [ ] Notification shows task name and time
- [ ] Tap notification opens app at task
- [ ] Handle alarm cancellation on task delete

---

#### 2.2 Pomodoro ForegroundService
```kotlin
// File: service/TimerForegroundService.kt
// Ongoing notification with countdown
// Actions: Pause, Resume, Cancel
```
**Acceptance Criteria:**
- [ ] Service runs in foreground with notification
- [ ] Timer continues when app backgrounded
- [ ] Notification shows remaining time
- [ ] Sound/vibration when session ends
- [ ] Transitions between focus/break modes

---

#### 2.3 Timer Sessions
```kotlin
// Room entities for session logging
// - data/timer/TimerSessionEntity.kt
// - data/timer/TimerSessionDao.kt
// Link to statistics for Dev 3
```
**Acceptance Criteria:**
- [ ] Sessions saved to database
- [ ] Track focus minutes per day
- [ ] Query for weekly/monthly stats

---

#### 2.4 Widget
```kotlin
// Files:
// - widget/TasksWidgetProvider.kt
// - widget/TasksWidgetService.kt
// - res/layout/widget_tasks.xml
// - res/xml/tasks_widget_info.xml
```
**Acceptance Criteria:**
- [ ] Widget shows today's tasks (max 5)
- [ ] Tap task opens app at that task
- [ ] Tap "+" opens add task screen
- [ ] Auto-refresh when tasks change

---

#### 2.5 Auto-start Next Session
```kotlin
// Setting to auto-start break after focus
// Setting to auto-start focus after break
```
**Acceptance Criteria:**
- [ ] Toggle in settings
- [ ] Countdown before auto-start (5s)
- [ ] Cancel auto-start with tap

---

## 👤 Developer 3 - Data & Analytics

**Focus:** Statistics, Persona, Export  
**Kỹ thuật showcase:** Room Aggregations, DataStore Proto, MPAndroidChart, BiometricPrompt

### Sprint Backlog

| # | Task | Priority | Points | Status | Due |
|---|------|----------|--------|--------|-----|
| 3.1 | [Statistics với real data](#31-real-statistics) | 🔴 HIGH | 5 | [ ] TODO | Week 1 |
| 3.2 | [Productivity Insights](#32-insights) | 🟡 MED | 5 | [ ] TODO | Week 2 |
| 3.3 | [Persona & Constraints](#33-persona) | 🟡 MED | 5 | [ ] TODO | Week 2 |
| 3.4 | [Search/Sort/Filter](#34-search-filter) | 🟡 MED | 3 | [ ] TODO | Week 3 |
| 3.5 | [Data Export & Privacy](#35-export-privacy) | 🟡 MED | 5 | [ ] TODO | Week 4 |

### Task Details

#### 3.1 Real Statistics
```kotlin
// Update StatisticsFragment to use real data
// Create StatisticsViewModel with queries:
// - Tasks completed per day
// - Tasks skipped per day  
// - Completion rate %
```
**Acceptance Criteria:**
- [ ] Bar chart shows real completion data
- [ ] Week view shows 7 days
- [ ] Month view shows 30 days
- [ ] Loading state while fetching

---

#### 3.2 Productivity Insights
```kotlin
// Weekly/Monthly summary:
// - Total tasks completed
// - Average completion rate
// - Best day/worst day
// - Trend analysis
```
**Acceptance Criteria:**
- [ ] Insights card in Statistics screen
- [ ] Suggestions based on data ("Try fewer tasks on Mondays")
- [ ] Compare to previous week/month

---

#### 3.3 Persona & Constraints
```kotlin
// Proto DataStore for:
// - wake_time, sleep_time
// - fixed_start_time
// - persona_type (student, worker, etc.)
// - allowed_activity_slots
```
**Acceptance Criteria:**
- [ ] Set constraints in PersonalizationActivity
- [ ] Constraints saved to DataStore
- [ ] Timeline respects constraints
- [ ] AI scheduling uses constraints

---

#### 3.4 Search/Sort/Filter
```kotlin
// UI additions to Timeline:
// - Search bar (by name)
// - Sort dropdown (deadline, priority, created)
// - Filter chips (category, status)
```
**Acceptance Criteria:**
- [ ] Search filters list in real-time
- [ ] Multiple sorts available
- [ ] Filter by category/priority
- [ ] Clear all filters button

---

#### 3.5 Data Export & Privacy
```kotlin
// Features:
// - Export all data as JSON
// - Export as CSV
// - Delete all data (with confirmation)
// - Biometric confirmation for delete
```
**Acceptance Criteria:**
- [ ] Export creates file in Downloads
- [ ] Share intent for exported file
- [ ] BiometricPrompt before delete
- [ ] 2-step confirmation for delete
- [ ] Deletion log saved locally

---

## 👤 Developer 4 - Gamification Core

**Focus:** Points System, Tree Growth Logic, Animations  
**Kỹ thuật showcase:** Custom Views, Lottie, State Machine, Room Relations

### Sprint Backlog

| # | Task | Priority | Points | Status | Due |
|---|------|----------|--------|--------|-----|
| 4.1 | [Points System](#41-points-system) | 🔴 HIGH | 5 | [ ] TODO | Week 1 |
| 4.2 | [Tree Growth Logic](#42-tree-growth) | 🔴 HIGH | 5 | [ ] TODO | Week 1 |
| 4.3 | [Tree Animations](#43-animations) | 🔴 HIGH | 5 | [ ] TODO | Week 2 |

### Task Details

#### 4.1 Points System
```kotlin
// Room entities:
// - data/gamification/UserPointsEntity.kt
// - data/gamification/GamificationDao.kt

// Points logic:
// - Complete task: +10 points
// - Complete pomodoro: +5 points
// - Streak bonus: x1.5 multiplier
// - Miss task: -5 points
```
**Acceptance Criteria:**
- [ ] Points update on task complete
- [ ] Streak tracking (consecutive days)
- [ ] Points visible in UI
- [ ] History of points earned

---

#### 4.2 Tree Growth Logic
```kotlin
// State machine:
enum class TreeState {
    SEED,      // 0-100 points
    SPROUT,    // 100-250 points
    SAPLING,   // 250-500 points
    TREE,      // 500+ points
    DEAD       // 0 points for 3 days
}

// data/gamification/TreeEntity.kt
// domain/model/TreeGrowthCalculator.kt
```
**Acceptance Criteria:**
- [ ] Plant new tree costs 50 points
- [ ] Tree grows based on invested points
- [ ] Tree dies if neglected
- [ ] Multiple trees possible

---

#### 4.3 Tree Animations
```kotlin
// Options:
// 1. Lottie animations (tree_grow.json)
// 2. Custom View with drawable stages
// 3. Animated Vector Drawable

// Files:
// - ui/garden/TreeView.kt (Custom View)
// - res/raw/tree_*.json (Lottie)
```
**Acceptance Criteria:**
- [ ] Smooth growth animation
- [ ] Different tree types
- [ ] Death/wilt animation
- [ ] Success celebration

---

## 👤 Developer 5 - UI/UX & Profile

**Focus:** Garden View, Profile Page, User Experience  
**Kỹ thuật showcase:** RecyclerView Grid, Navigation Component, DataBinding, Supabase Auth

### Sprint Backlog

| # | Task | Priority | Points | Status | Due |
|---|------|----------|--------|--------|-----|
| 5.1 | [Garden View](#51-garden-view) | 🟡 MED | 5 | [ ] TODO | Week 3 |
| 5.2 | [Profile Page](#52-profile-page) | 🟢 LOW | 3 | [ ] TODO | Week 4 |

### Task Details

#### 5.1 Garden View
```kotlin
// Grid of all user's trees
// - ui/garden/GardenFragment.kt
// - ui/garden/GardenViewModel.kt
// - ui/garden/TreeAdapter.kt
```
**Acceptance Criteria:**
- [ ] Grid layout of trees
- [ ] Show tree state/progress
- [ ] Tap tree for details
- [ ] Empty state for new users

---

#### 5.2 Profile Page
```kotlin
// Update ProfileActivity:
// - User info from Supabase
// - Total points display
// - Achievement badges
// - Stats summary
```
**Acceptance Criteria:**
- [ ] Display username/email
- [ ] Edit profile functionality
- [ ] Points and level display
- [ ] Link to Garden

---

## 📊 Progress Overview

### By Developer

| Developer | Completed | In Progress | TODO | Total |
|-----------|-----------|-------------|------|-------|
| Dev 1 | 0 | 0 | 5 | 5 |
| Dev 2 | 0 | 0 | 5 | 5 |
| Dev 3 | 0 | 0 | 5 | 5 |
| Dev 4 | 0 | 0 | 3 | 3 |
| Dev 5 | 0 | 0 | 2 | 2 |
| **Total** | **0** | **0** | **20** | **20** |

### By Week

| Week | Tasks Due | Completed |
|------|-----------|-----------|
| Week 1 | 1.1, 1.2, 2.1, 3.1, 4.1, 4.2 | 0 |
| Week 2 | 1.3, 2.2, 3.2, 3.3, 4.3 | 0 |
| Week 3 | 1.4, 2.3, 3.4, 5.1 | 0 |
| Week 4 | 1.5, 2.4, 2.5, 3.5, 5.2 | 0 |

---

## 🤝 Integration Points

| Task | Depends On | Blocks |
|------|------------|--------|
| 1.3 AI Schedule | - | 3.3 Persona constraints |
| 2.3 Timer Sessions | - | 3.1 Statistics data |
| 3.2 Insights | 3.1 Statistics | - |
| 4.1 Points | - | 4.2 Tree Growth |
| 4.2 Tree Growth | 4.1 Points | 4.3 Animations |
| 5.1 Garden View | 4.2 Tree Growth, 4.3 Animations | - |
| 5.2 Profile | 4.1 Points | - |

---

## 📝 Daily Standup Template

```markdown
### [Date] - [Dev Name]

**Yesterday:**
- 

**Today:**
- 

**Blockers:**
- 
```
