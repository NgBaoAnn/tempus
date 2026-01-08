package com.projectapp.tempus

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.projectapp.tempus.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        
        // 1. Kết nối Navigation
        binding.bottomNavView.setupWithNavController(navController)
        
        // 2. ÉP BUỘC hiển thị màu gốc của PNG bằng cách tắt Tint trong code
        // Việc này giúp icon không bị biến thành màu đen/xám mặc định
        binding.bottomNavView.itemIconTintList = null
    }
}
