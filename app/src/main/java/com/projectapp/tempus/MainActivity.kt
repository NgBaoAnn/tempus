package com.projectapp.tempus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.platform.ComposeView
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.NavOptions
import com.projectapp.tempus.databinding.ActivityMainComposeBinding
import com.projectapp.tempus.ui.navigation.BottomNavBar
import com.projectapp.tempus.ui.navigation.NavItem
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import com.projectapp.tempus.ui.navigation.navItems

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainComposeBinding
    private lateinit var navController: NavController
    private var currentRoute: String = NavItem.Timer.route

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainComposeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Setup Navigation
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        
        // Sync route with destination changes
        navController.addOnDestinationChangedListener { _, destination, _ ->
            val newRoute = navItems.find { it.fragmentId == destination.id }?.route
            if (newRoute != null && newRoute != currentRoute) {
                currentRoute = newRoute
                updateBottomNav()
            }
        }

        // Setup Compose Bottom Nav
        updateBottomNav()
        
        handleNavigationIntent(intent)
        
        // Sync Activity Theme with App Theme (affects Window background and XML layouts)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                com.projectapp.tempus.ui.theme.ThemeManager.themeMode.collect { mode ->
                    val nightMode = when (mode) {
                        com.projectapp.tempus.ui.theme.ThemeMode.LIGHT -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO
                        com.projectapp.tempus.ui.theme.ThemeMode.DARK -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
                        com.projectapp.tempus.ui.theme.ThemeMode.SYSTEM -> androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    }
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(nightMode)
                }
            }
        }
    }
    
    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        handleNavigationIntent(intent)
    }
    
    private fun handleNavigationIntent(intent: android.content.Intent?) {
        intent?.getStringExtra("NAVIGATE_TO")?.let { destination ->
            when (destination) {
                "timer" -> {
                    val timerItem = navItems.find { it.route == NavItem.Timer.route }
                    if (timerItem != null && currentRoute != NavItem.Timer.route) {
                        currentRoute = NavItem.Timer.route
                        navigate(timerItem.fragmentId)
                    }
                }
                "timeline" -> {
                    val timelineItem = navItems.find { it.route == NavItem.Timeline.route }
                    if (timelineItem != null) {
                        // Switch to Timeline tab if not already there
                        if (currentRoute != NavItem.Timeline.route) {
                            currentRoute = NavItem.Timeline.route
                            navigate(timelineItem.fragmentId)
                        }

                        // Handle additional actions (Add Task or Open Specific Task)
                        val openAddTask = intent.getBooleanExtra("OPEN_ADD_TASK", false)
                        val taskId = intent.getStringExtra("TASK_ID")

                        if (openAddTask) {
                            // Delay slightly to ensure fragment is attached
                            binding.root.post {
                                val bundle = Bundle().apply {
                                    putString("selectedDate", java.time.LocalDate.now().toString())
                                }
                                navController.navigate(R.id.action_timelineFragment_to_editScheduleFragment, bundle)
                            }
                        } else if (!taskId.isNullOrEmpty()) {
                             // Delay slightly to ensure fragment is attached
                            binding.root.post {
                                val bundle = Bundle().apply {
                                    putString("taskId", taskId)
                                    putString("selectedDate", java.time.LocalDate.now().toString())
                                }
                                navController.navigate(R.id.action_timelineFragment_to_editScheduleFragment, bundle)
                            }
                        }
                    }
                }
                "garden" -> {
                    // Navigate to Garden fragment
                    binding.root.post {
                        navController.navigate(R.id.gardenFragment)
                    }
                }
            }
        }
    }

    private fun navigate(fragmentId: Int) {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.timerFragment, false, true)
            .setLaunchSingleTop(true)
            .setRestoreState(true)
            .build()
            
        navController.navigate(fragmentId, null, navOptions)
        updateBottomNav()
    }
    
    private fun updateBottomNav() {
        binding.composeBottomNav.setContent {
            com.projectapp.tempus.ui.theme.TempusTheme {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onItemClick = { item ->
                        if (currentRoute != item.route) {
                            currentRoute = item.route
                            navigate(item.fragmentId)
                        }
                    }
                )
            }
        }
    }
}
