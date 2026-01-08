package com.projectapp.tempus

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.databinding.FragmentSettingsBinding
import com.projectapp.tempus.ui.auth.LoginActivity
import com.projectapp.tempus.ui.setting.PersonalizationActivity
import com.projectapp.tempus.ui.setting.SettingsViewModel
import io.github.jan.supabase.gotrue.auth


class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val viewModel: SettingsViewModel by viewModels()
    private var isLoggedIn = false

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        isLoggedIn = checkLogin()
        return binding.root
    }

    private fun checkLogin(): Boolean {
        // Sử dụng client từ Provider bạn đã tạo
        val session = SupabaseClientProvider.client.auth.currentSessionOrNull()

        if (session == null) {
            // Nếu chưa đăng nhập, đẩy về LoginActivity
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

        // Chỉ load user nếu đã đăng nhập
        if (isLoggedIn) {
            // 1. Load và hiển thị dữ liệu User
            viewModel.loadUser()
            viewModel.user.observe(viewLifecycleOwner) { user ->
                binding.tvUserName.text = user.username
                binding.tvUserEmail.text = user.email
            }
        }

        // 2. Thiết lập các sự kiện Click
        setupClickListeners()
    }

    private fun setupClickListeners() {
        // Thẻ thông tin cá nhân (Profile)
        binding.cardProfile.setOnClickListener {
            // TODO: Mở màn hình chỉnh sửa Profile (ProfileActivity)
            Toast.makeText(requireContext(), "Mở Profile", Toast.LENGTH_SHORT).show()
        }

        // THẺ CÁ NHÂN HÓA (Mở PersonalizationActivity)
        binding.cardPersonalization.setOnClickListener {
            val intent = Intent(requireContext(), PersonalizationActivity::class.java)
            startActivity(intent)
        }

        // Thẻ Thông báo
        binding.cardNotifications.setOnClickListener {
            Toast.makeText(requireContext(), "Cài đặt thông báo", Toast.LENGTH_SHORT).show()
        }

        // Thẻ Giao diện
        binding.cardTheme.setOnClickListener {
            Toast.makeText(requireContext(), "Cài đặt giao diện", Toast.LENGTH_SHORT).show()
        }

        // Thẻ Chính sách bảo mật
        binding.cardPrivacy.setOnClickListener {
            Toast.makeText(requireContext(), "Xem chính sách bảo mật", Toast.LENGTH_SHORT).show()
        }

        // Nút Đăng xuất
        binding.btnLogout.setOnClickListener {
            logout()
        }
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }



    private fun logout() {
        // 1. XÓA TOKEN
        // 2. MỞ LOGIN ACTIVITY
        val intent = Intent(requireContext(), LoginActivity::class.java)
        startActivity(intent)

        // 3. ĐÓNG MAIN ACTIVITY
        requireActivity().finish()
    }

}
