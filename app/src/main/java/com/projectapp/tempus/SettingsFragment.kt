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
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.export.DataExportRepository
import com.projectapp.tempus.databinding.FragmentSettingsBinding
import com.projectapp.tempus.ui.auth.LoginActivity
import com.projectapp.tempus.ui.setting.PersonalizationActivity
import com.projectapp.tempus.ui.setting.SettingsViewModel
import io.github.jan.supabase.gotrue.auth
import kotlinx.coroutines.launch
import java.io.File


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels()
    private var isLoggedIn = false
    
    private lateinit var exportRepository: DataExportRepository

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        isLoggedIn = checkLogin()
        exportRepository = DataExportRepository(requireContext())
        return binding.root
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
                binding.tvUserName.text = user.username
                binding.tvUserEmail.text = user.email
            }
        }

        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.cardProfile.setOnClickListener {
            Toast.makeText(requireContext(), "Mở Profile", Toast.LENGTH_SHORT).show()
        }

        binding.cardPersonalization.setOnClickListener {
            val intent = Intent(requireContext(), PersonalizationActivity::class.java)
            startActivity(intent)
        }

        binding.cardNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Cài đặt thông báo", Toast.LENGTH_SHORT).show()
        }

        binding.cardTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Cài đặt giao diện", Toast.LENGTH_SHORT).show()
        }

        binding.cardPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Xem chính sách bảo mật", Toast.LENGTH_SHORT).show()
        }

        // ===== EXPORT JSON =====
        binding.cardExportJson.setOnClickListener {
            exportToJson()
        }

        // ===== EXPORT CSV =====
        binding.cardExportCsv.setOnClickListener {
            exportToCsv()
        }

        // ===== DELETE ALL DATA =====
        binding.cardDeleteData.setOnClickListener {
            showDeleteConfirmationStep1()
        }

        binding.btnLogout.setOnClickListener {
            logout()
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
            Toast.makeText(requireContext(), "Đã lưu vào ${file.absolutePath}", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Lỗi chia sẻ file: ${e.message}", Toast.LENGTH_SHORT).show()
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
        
        val biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    showDeleteConfirmationStep2()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    if (errorCode != BiometricPrompt.ERROR_USER_CANCELED && 
                        errorCode != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        Toast.makeText(requireContext(), "Lỗi xác thực: $errString", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(requireContext(), "Xác thực thất bại", Toast.LENGTH_SHORT).show()
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
            hint = "Nhập XÓA để xác nhận"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(50, 30, 50, 30)
        }

        AlertDialog.Builder(requireContext())
            .setTitle("🗑️ Xác nhận lần cuối")
            .setMessage("Nhập \"XÓA\" để xác nhận xóa tất cả dữ liệu:")
            .setView(editText)
            .setPositiveButton("Xóa vĩnh viễn") { _, _ ->
                val input = editText.text.toString().trim()
                if (input.equals("XÓA", ignoreCase = true) || input.equals("XOA", ignoreCase = true)) {
                    performDelete()
                } else {
                    Toast.makeText(requireContext(), "Nhập không đúng. Hủy xóa.", Toast.LENGTH_SHORT).show()
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
                Toast.makeText(requireContext(), "✅ Đã xóa tất cả dữ liệu", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(requireContext(), "❌ Lỗi khi xóa dữ liệu", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun logout() {
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
