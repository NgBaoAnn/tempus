package com.projectapp.tempus.ui.auth

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.launch


class RegisterActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText

    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnLogin: Button

    private lateinit var authService: AuthService


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val supabaseClient = SupabaseClientProvider.client
        authService = AuthService(supabaseClient)

        addControls()
        initAuthService()
        addEvents()
    }

    private fun initAuthService() {
        authService = AuthService(
            supabaseClient = SupabaseClientProvider.client,
        )
    }

    private fun addControls() {
        edtFullName = findViewById(R.id.edt_full_name)
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edit_password)
        edtConfirmPassword = findViewById(R.id.edt_confirm_password)

        btnConfirm = findViewById(R.id.btn_confirm)
        btnLogin = findViewById(R.id.btn_login)
    }

    private fun addEvents() {

        btnConfirm.setOnClickListener {
            handleRegister()
        }

        btnLogin.setOnClickListener {
            finish() // quay lại LoginActivity
        }
    }

    private fun handleRegister() {
        val fullName = edtFullName.text.toString().trim()
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()
        val confirmPassword = edtConfirmPassword.text.toString().trim()

        if (!validateRegisterInput(fullName, email, password, confirmPassword)) return

        lifecycleScope.launch {
            try {
                // Gọi hàm register với đầy đủ tham số
                authService.register(email, password, fullName)

                Toast.makeText(
                    applicationContext,
                    "Đăng ký thành công! Vui lòng kiểm tra email.",
                    Toast.LENGTH_LONG
                ).show()
                finish() // Quay lại màn hình Login

            } catch (e: RestException) {
                // Lỗi từ phía Server Supabase (Email đã tồn tại, mật khẩu quá yếu...)
                Log.e("RegisterActivity", "Supabase Error: ${e.message}")
                val errorMsg = when {
                    e.message?.contains("already registered") == true -> "Email này đã được sử dụng"
                    else -> "Lỗi: ${e.message}"
                }
                Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_SHORT).show()

            } catch (e: Exception) {
                // Lỗi mạng hoặc lỗi không xác định
                Log.e("RegisterActivity", "General Error: ${e.message}")
                Toast.makeText(this@RegisterActivity, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun validateRegisterInput(
        fullName: String,
        email: String,
        password: String,
        confirmPassword: String
    ): Boolean {

        if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return false
        }

        if (password != confirmPassword) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }



}