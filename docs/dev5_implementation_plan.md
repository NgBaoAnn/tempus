# 🎯 Dev 5 Implementation Plan - UI/UX & Profile
## Final Version - Jetpack Compose + Lottie

> **Tech Stack:** Jetpack Compose, Lottie Animations, Supabase Auth  
> **Timeline:** Week 3-4 | **Points:** 8 (5 + 3)  
> **Last Updated:** 2026-01-12

---

## 📋 Scope Summary

| Feature | Approach | Status |
|---------|----------|--------|
| **Garden View** | Enhance existing + Lottie | 🟡 In Progress |
| **Profile Page** | New - Jetpack Compose | ❌ Not Started |
| **Lottie Animations** | ✅ YES | Dependency có sẵn |
| **Avatar Upload** | ❌ SKIP | Không cần |

---

## ⚙️ Setup: Thêm Jetpack Compose

### Step 1: Update `build.gradle.kts` (app level)

```kotlin
android {
    // Thêm trong block android
    buildFeatures {
        viewBinding = true
        buildConfig = true
        compose = true  // ← THÊM MỚI
    }
    
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.8"  // ← THÊM MỚI
    }
}

dependencies {
    // === JETPACK COMPOSE ===
    val composeBom = platform("androidx.compose:compose-bom:2024.02.00")
    implementation(composeBom)
    
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    
    // Lottie for Compose
    implementation("com.airbnb.android:lottie-compose:6.3.0")
    
    // Debug tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
    
    // Lottie đã có sẵn (v6.3.0) ✓
}
```

### Step 2: Download Lottie Assets

Tải từ [LottieFiles.com](https://lottiefiles.com) và đặt vào `res/raw/`:
- `tree_seed.json` - Animation hạt giống
- `tree_sprout.json` - Animation mầm
- `tree_grow.json` - Animation lớn lên  
- `tree_full.json` - Animation cây trưởng thành
- `tree_dead.json` - Animation cây chết

---

## 📁 File Structure

```
ui/
├── setting/
│   ├── ProfileActivity.kt          [MODIFY] Host Compose
│   └── profile/                     [NEW FOLDER]
│       ├── ProfileScreen.kt         [NEW] Main Compose Screen
│       ├── ProfileViewModel.kt      [NEW] ViewModel
│       └── components/              [NEW FOLDER]
│           ├── StatCard.kt          [NEW] Reusable stat card
│           └── ProfileHeader.kt     [NEW] Avatar + Name section
├── garden/
│   ├── GardenFragment.kt            [MODIFY] Add stats summary
│   ├── TreeAdapter.kt               [MODIFY] Better UX
│   └── lottie/                      [NEW FOLDER]
│       └── LottieTreeView.kt        [NEW] Lottie-powered tree
```

---

## 🚀 Implementation Steps

### Phase 1: Setup (30 min)

| # | Task | File |
|---|------|------|
| 1.1 | Thêm Compose dependencies | `build.gradle.kts` |
| 1.2 | Sync Gradle & Verify build | Terminal |
| 1.3 | Download Lottie JSON files | `res/raw/` |

---

### Phase 2: Profile Page - Compose (3-4 hours)

#### 2.1 [NEW] `ProfileViewModel.kt`

```kotlin
class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    
    data class ProfileUiState(
        val email: String = "",
        val fullName: String = "",
        val totalPoints: Int = 0,
        val currentStreak: Int = 0,
        val treeCount: Int = 0,
        val memberSince: String = "",
        val isLoading: Boolean = true,
        val isSaving: Boolean = false
    )
    
    fun loadProfile() { ... }
    fun updateName(newName: String) { ... }
}
```

#### 2.2 [NEW] `ProfileScreen.kt`

```kotlin
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    onNavigateToGarden: () -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    
    Scaffold(
        topBar = { ProfileTopBar(onBack) }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ProfileHeader(name = uiState.fullName, email = uiState.email, onEditName = { ... })
            StatsRow(points = uiState.totalPoints, streak = uiState.currentStreak, trees = uiState.treeCount)
            GardenLinkCard(onClick = onNavigateToGarden)
            MemberSinceText(date = uiState.memberSince)
        }
    }
}
```

#### 2.3 [MODIFY] `ProfileActivity.kt`

```kotlin
class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TempusTheme {
                ProfileScreen(
                    onNavigateToGarden = { 
                        // Navigate to MainActivity -> Garden tab
                    },
                    onBack = { finish() }
                )
            }
        }
    }
}
```

#### 2.4 [NEW] UI Components

**ProfileHeader.kt:**
```kotlin
@Composable
fun ProfileHeader(name: String, email: String, onEditName: () -> Unit) {
    Column(horizontalAlignment = CenterHorizontally) {
        // Avatar placeholder (không cần upload)
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
        ) {
            Icon(Icons.Default.Person, "Avatar", Modifier.size(60.dp).align(Center))
        }
        
        // Name + Edit button
        Row(verticalAlignment = CenterVertically) {
            Text(name.ifEmpty { "Chưa đặt tên" }, style = MaterialTheme.typography.headlineSmall)
            IconButton(onClick = onEditName) {
                Icon(Icons.Default.Edit, "Edit")
            }
        }
        
        Text(email, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
    }
}
```

**StatCard.kt:**
```kotlin
@Composable
fun StatCard(icon: String, value: String, label: String) {
    Card(modifier = Modifier.width(100.dp)) {
        Column(horizontalAlignment = CenterHorizontally, modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 24.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}
```

---

### Phase 3: Garden Enhancement + Lottie (2-3 hours)

#### 3.1 [NEW] `LottieTreeView.kt`

```kotlin
@Composable
fun LottieTreeView(
    state: TreeState,
    type: TreeType,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        LottieCompositionSpec.RawRes(getLottieRes(state))
    )
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = if (state == TreeState.TREE) LottieConstants.IterateForever else 1
    )
    
    LottieAnimation(
        composition = composition,
        progress = { progress },
        modifier = modifier
    )
}

private fun getLottieRes(state: TreeState): Int = when (state) {
    TreeState.SEED -> R.raw.tree_seed
    TreeState.SPROUT -> R.raw.tree_sprout
    TreeState.SAPLING -> R.raw.tree_grow
    TreeState.TREE -> R.raw.tree_full
    TreeState.DEAD -> R.raw.tree_dead
}
```

#### 3.2 [MODIFY] `TreeAdapter.kt` - Thêm Long Press

```kotlin
override fun onBindViewHolder(holder: TreeViewHolder, position: Int) {
    val tree = getItem(position)
    holder.itemView.setOnClickListener { onClick(tree) }
    
    // Long press để xóa
    holder.itemView.setOnLongClickListener {
        onLongClick(tree)
        true
    }
}
```

#### 3.3 [MODIFY] `fragment_garden.xml` - Thêm Stats + SwipeRefresh

```xml
<androidx.swiperefreshlayout.widget.SwipeRefreshLayout
    android:id="@+id/swipeRefresh"
    android:layout_width="match_parent"
    android:layout_height="0dp">
    
    <!-- Stats Summary -->
    <com.google.android.material.card.MaterialCardView
        android:id="@+id/cardStats"
        android:layout_width="match_parent"
        android:layout_height="wrap_content">
        
        <LinearLayout android:orientation="horizontal" android:gravity="center">
            <!-- 3 stat items: Trees, Points, Invested -->
        </LinearLayout>
    </com.google.android.material.card.MaterialCardView>
    
    <androidx.recyclerview.widget.RecyclerView ... />
</androidx.swiperefreshlayout.widget.SwipeRefreshLayout>
```

#### 3.4 [MODIFY] `GardenFragment.kt`

```kotlin
// Setup SwipeRefresh
binding.swipeRefresh.setOnRefreshListener {
    viewLifecycleOwner.lifecycleScope.launch {
        pointsManager.checkAndUpdateDeadTrees()
        binding.swipeRefresh.isRefreshing = false
    }
}

// Long press delete
private fun showDeleteTreeDialog(tree: TreeEntity) {
    MaterialAlertDialogBuilder(requireContext())
        .setTitle("Xóa cây?")
        .setMessage("Bạn có chắc muốn xóa ${tree.name}?")
        .setPositiveButton("Xóa") { _, _ -> deleteTree(tree.id) }
        .setNegativeButton("Hủy", null)
        .show()
}
```

---

### Phase 4: Connect & Polish (1 hour)

| # | Task | File |
|---|------|------|
| 4.1 | Settings → Profile navigation | `SettingsFragment.kt` |
| 4.2 | Profile → Garden navigation | `ProfileActivity.kt` |
| 4.3 | Theme setup | `ui/theme/Theme.kt` |
| 4.4 | Test all flows | Manual testing |

---

## 🧪 Testing Checklist

### Profile Page (Compose)
- [ ] Mở Profile từ Settings → card_profile
- [ ] Hiển thị đúng email từ Supabase Auth
- [ ] Hiển thị đúng full_name từ user metadata
- [ ] Nhấn Edit → Dialog nhập tên mới
- [ ] Save tên → Persist to Supabase
- [ ] Stats cards hiển thị đúng (points, streak, trees)
- [ ] Nhấn "Vườn cây" → Navigate đến Garden tab
- [ ] Nhấn Back → Quay lại Settings

### Garden View (Lottie)
- [ ] Mở Garden tab → Grid hiển thị cây
- [ ] Mỗi cây có Lottie animation đúng state
- [ ] Pull-to-refresh hoạt động
- [ ] Stats summary cập nhật đúng
- [ ] Plant tree → Seed animation xuất hiện
- [ ] Water tree → Animation grow chạy
- [ ] Long press tree → Dialog xóa

---

## 📅 Timeline (Estimated 11 hours)

| Day | Tasks | Hours |
|-----|-------|-------|
| **Day 1** | Setup Compose dependencies + Download Lottie | 1h |
| **Day 1** | ProfileViewModel + ProfileScreen layout | 3h |
| **Day 2** | Profile components + Connect to Supabase | 2h |
| **Day 2** | Garden Stats + SwipeRefresh | 1.5h |
| **Day 3** | LottieTreeView + Integration | 2h |
| **Day 3** | Testing + Bug fixes | 1.5h |
| **Total** | | **~11h** |

---

## ✅ Final Acceptance Criteria

### Task 5.1 - Garden View (5 points)
- [ ] Grid hiển thị với Lottie animations
- [ ] Stats summary ở header (tổng cây, điểm, đã đầu tư)
- [ ] Pull-to-refresh
- [ ] Long-press → Delete tree dialog

### Task 5.2 - Profile Page (3 points)
- [ ] Material 3 Compose Design
- [ ] Hiển thị user info (email, name)
- [ ] Editable name (dialog)
- [ ] Stats cards (points, streak, trees)
- [ ] Link to Garden
- [ ] Smooth navigation & animations

---

## 📝 Fallback Plan

> Nếu không tìm được Lottie JSON phù hợp → fallback về drawable animation hiện có trong `TreeView.kt`

> Nếu Compose gặp lỗi build → có thể chuyển Profile sang XML layout như các screen khác
