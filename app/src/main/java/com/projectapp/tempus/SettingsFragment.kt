package com.projectapp.tempus

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.export.DataExportRepository
import com.projectapp.tempus.ui.auth.LoginActivity
import com.projectapp.tempus.ui.setting.LegalDocumentActivity
import com.projectapp.tempus.ui.setting.PersonalizationActivity
import com.projectapp.tempus.ui.setting.ProfileActivity
import com.projectapp.tempus.ui.setting.SettingsViewModel
import com.projectapp.tempus.ui.setting.ThemeSettingsActivity
import com.projectapp.tempus.ui.setting.compose.SettingsScreen
import com.projectapp.tempus.ui.setting.compose.UserInfo
import com.projectapp.tempus.ui.theme.TempusTheme
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.io.File


class SettingsFragment : Fragment() {

    private val viewModel: SettingsViewModel by viewModels()
    private var isLoggedIn = false

    private lateinit var exportRepository: DataExportRepository

    // Compose state - using mutableStateOf properly
    private val userInfoState = mutableStateOf(UserInfo())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        isLoggedIn = checkLogin()
        exportRepository = DataExportRepository(requireContext())
        
        // Initialize user profile cache
        com.projectapp.tempus.data.user.UserProfileCache.init(requireContext())

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                TempusTheme {
                    SettingsScreen(
                        userInfo = userInfoState.value,
                        onProfileClick = ::navigateToProfile,
                        onNotificationsClick = ::onNotificationsClick,
                        onPersonalizationClick = ::navigateToPersonalization,
                        onThemeClick = ::onThemeClick,
                        onPrivacyClick = ::navigateToPrivacyPolicy,
                        onTermsClick = ::navigateToTermsOfService,
                        onExportJsonClick = ::performSync,
                        onExportCsvClick = ::exportToCsv,
                        onDeleteDataClick = ::showDeleteConfirmationStep1,
                        onLogoutClick = ::logout
                    )
                }
            }
        }
    }

    private fun checkLogin(): Boolean {
        val session = SupabaseClientProvider.client.auth.currentSessionOrNull()

        if (session == null) {
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
            return false
        }
        return true
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (isLoggedIn) {
            viewModel.loadUser()
            viewModel.user.observe(viewLifecycleOwner) { user ->
                userInfoState.value = UserInfo(
                    name = user.username,
                    email = user.email,
                    avatar = user.avatar
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (isLoggedIn) {
            viewModel.loadUser()
        }
    }

    // ===== NAVIGATION =====

    private fun navigateToProfile() {
        val intent = Intent(requireContext(), ProfileActivity::class.java)
        startActivity(intent)
    }

    private fun navigateToPersonalization() {
        val intent = Intent(requireContext(), PersonalizationActivity::class.java)
        startActivity(intent)
    }

    private fun onNotificationsClick() {
        Toast.makeText(requireContext(), "Cài đặt thông báo", Toast.LENGTH_SHORT).show()
    }

    private fun onThemeClick() {
        val intent = Intent(requireContext(), ThemeSettingsActivity::class.java)
        startActivity(intent)
    }
    
    private fun navigateToPrivacyPolicy() {
        val intent = Intent(requireContext(), LegalDocumentActivity::class.java)
        intent.putExtra(LegalDocumentActivity.EXTRA_DOCUMENT_TYPE, LegalDocumentActivity.TYPE_PRIVACY_POLICY)
        startActivity(intent)
    }
    
    private fun navigateToTermsOfService() {
        val intent = Intent(requireContext(), LegalDocumentActivity::class.java)
        intent.putExtra(LegalDocumentActivity.EXTRA_DOCUMENT_TYPE, LegalDocumentActivity.TYPE_TERMS_OF_SERVICE)
        startActivity(intent)
    }

    // ===== SYNC FUNCTION =====

    private fun performSync() {
        val userId = SupabaseClientProvider.client.auth.currentUserOrNull()?.id
        if (userId == null) {
            Toast.makeText(requireContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show()
            
            try {
                // 1. Sync Schedule data (push then pull)
                val syncManager = com.projectapp.tempus.data.RepositoryProvider.getSyncManager(requireContext())
                syncManager.pushToServer()
                syncManager.pullFromServer(userId)
                
                // 2. Sync Gamification data
                val gamificationSyncManager = com.projectapp.tempus.data.RepositoryProvider.getGamificationSyncManager(requireContext())
                gamificationSyncManager.pushToServer()
                gamificationSyncManager.pullFromServer()
                
                // 3. Sync Notes data
                val notesSyncManager = com.projectapp.tempus.data.RepositoryProvider.getNotesSyncManager(requireContext())
                notesSyncManager.pushToServer()
                notesSyncManager.pullFromServer(userId)
                
                Toast.makeText(requireContext(), "✅ Đồng bộ thành công!", Toast.LENGTH_SHORT).show()
                
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Sync failed", e)
                Toast.makeText(requireContext(), "❌ Đồng bộ thất bại: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ===== EXPORT FUNCTIONS =====

    private fun exportToJson() {
        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Đang xuất dữ liệu...", Toast.LENGTH_SHORT).show()
            val file = exportRepository.exportToJson()
            if (file != null) {
                shareFile(file, "application/json")
            } else {
                Toast.makeText(requireContext(), "Lỗi xuất dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun exportToCsv() {
        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Đang xuất dữ liệu...", Toast.LENGTH_SHORT).show()
            val file = exportRepository.exportToCsv()
            if (file != null) {
                shareFile(file, "text/csv")
            } else {
                Toast.makeText(requireContext(), "Lỗi xuất dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun shareFile(file: File, mimeType: String) {
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Chia sẻ dữ liệu"))
            Toast.makeText(
                requireContext(),
                "Đã lưu vào ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Lỗi chia sẻ file: ${e.message}",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // ===== DELETE FUNCTIONS (2-STEP + BIOMETRIC) =====

    private fun showDeleteConfirmationStep1() {
        AlertDialog.Builder(requireContext())
            .setTitle("⚠️ Xóa tất cả dữ liệu")
            .setMessage("Bạn có chắc chắn muốn xóa TẤT CẢ dữ liệu của mình? Hành động này KHÔNG THỂ hoàn tác!")
            .setPositiveButton("Tiếp tục") { _, _ ->
                authenticateWithBiometric()
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun authenticateWithBiometric() {
        val biometricManager = BiometricManager.from(requireContext())

        when (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                showBiometricPrompt()
            }

            else -> {
                // Nếu không có biometric, skip đến step 2
                showDeleteConfirmationStep2()
            }
        }
    }

    private fun showBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(requireContext())

        val biometricPrompt = BiometricPrompt(
            this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showDeleteConfirmationStep2()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED &&
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON
                    ) {
                        Toast.makeText(
                            requireContext(),
                            "Lỗi xác thực: $errString",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Xác thực thất bại", Toast.LENGTH_SHORT)
                        .show()
                }
            })

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Xác thực để xóa dữ liệu")
            .setSubtitle("Sử dụng vân tay hoặc khuôn mặt")
            .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL)
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun showDeleteConfirmationStep2() {
        val editText = EditText(requireContext()).apply {
            hint = "Nhập XÁC NHẬN để xác nhận"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Xác nhận lần cuối")
            .setMessage("Nhập \"XÁC NHẬN\" để xác nhận xóa tất cả dữ liệu:")
            .setView(editText)
            .setPositiveButton("Xóa vĩnh viễn") { _, _ ->
                val input = editText.text.toString().trim()
                if (input.equals("XÁC NHẬN", ignoreCase = true) || 
                    input.equals("XAC NHAN", ignoreCase = true) ||
                    input.equals("XACNHAN", ignoreCase = true)) {
                    performDelete()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Nhập không đúng. Hủy xóa.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun performDelete() {
        lifecycleScope.launch {
            Toast.makeText(requireContext(), "Đang xóa dữ liệu...", Toast.LENGTH_SHORT).show()
            val success = exportRepository.deleteAllData()
            if (success) {
                Toast.makeText(requireContext(), "✅ Đã xóa tất cả dữ liệu", Toast.LENGTH_LONG)
                    .show()
            } else {
                Toast.makeText(requireContext(), "❌ Lỗi khi xóa dữ liệu", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    private fun logout() {
        // Check network connectivity first
        val connectivityManager = requireContext().getSystemService(android.content.Context.CONNECTIVITY_SERVICE) as android.net.ConnectivityManager
        val networkCapabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        val hasInternet = networkCapabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        
        if (!hasInternet) {
            // Show warning dialog if no internet
            androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("⚠️ Không có kết nối mạng")
                .setMessage("Nếu bạn đăng xuất khi không có mạng, dữ liệu chưa đồng bộ sẽ bị mất.\n\nBạn có chắc muốn đăng xuất?")
                .setPositiveButton("Đăng xuất") { _, _ ->
                    performLogout(syncBeforeLogout = false)
                }
                .setNegativeButton("Hủy", null)
                .show()
        } else {
            performLogout(syncBeforeLogout = true)
        }
    }
    
    private fun performLogout(syncBeforeLogout: Boolean) {
        lifecycleScope.launch {
            try {
                if (syncBeforeLogout) {
                    Toast.makeText(requireContext(), "Đang đồng bộ dữ liệu...", Toast.LENGTH_SHORT).show()
                }
                
                // Call AuthService logout with context for auto-sync push
                val authService = com.projectapp.tempus.data.auth.AuthService(SupabaseClientProvider.client)
                authService.logout(
                    syncBeforeLogout = syncBeforeLogout,
                    context = requireContext()
                )
                
                // Clear user profile cache on logout (from master)
                com.projectapp.tempus.data.user.UserProfileCache.clearCache()
                
                Toast.makeText(requireContext(), "Đã đăng xuất", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                android.util.Log.e("Settings", "Logout error", e)
                // Continue to login screen even if logout fails
            }
            
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            requireActivity().finish()
        }
    }
}
