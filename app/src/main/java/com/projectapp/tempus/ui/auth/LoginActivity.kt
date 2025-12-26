package com.projectapp.tempus.ui.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.projectapp.tempus.MainActivity
import com.projectapp.tempus.R
import com.projectapp.tempus.core.supabase.SupabaseClientProvider
import com.projectapp.tempus.data.auth.AuthService
import kotlinx.coroutines.launch
import retrofit2.HttpException

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLoginEmail: MaterialButton
    private lateinit var btnLoginGoogle: MaterialButton
    private lateinit var btnForgotPassword: Button
    private lateinit var btnRegister: Button
    private lateinit var btnHidePassword: ImageButton

    private var isPasswordVisible = false

    private lateinit var authService: AuthService



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        addControls()
        initAuthService()
        addEvents()
    }




    private fun addControls() {
        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        btnLoginEmail = findViewById(R.id.btn_confirm)
        btnLoginGoogle = findViewById(R.id.btn_login_google)
        btnForgotPassword = findViewById(R.id.btn_forgot_password)
        btnRegister = findViewById(R.id.btn_register)
        btnHidePassword = findViewById(R.id.btn_hide_password)

        // tạm
        edtPassword.transformationMethod =
            PasswordTransformationMethod.getInstance()

        btnHidePassword.setImageResource(R.drawable.ic_hidden)
    }

    private fun initAuthService() {

        authService = AuthService(
            supabaseClient = SupabaseClientProvider.client,
        )
    }

    private fun addEvents() {

        btnLoginEmail.setOnClickListener {
            handleLogin()
        }

        btnForgotPassword.setOnClickListener {
            showForgotPasswordDialog()
        }

        btnRegister.setOnClickListener {
            // TODO: mở màn hình đăng ký
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        btnHidePassword.setOnClickListener {
            togglePassword()
        }
    }

    private fun handleLogin() {
        val email = edtEmail.text.toString().trim()
        val password = edtPassword.text.toString().trim()

        Log.d("LoginActivity", "Email: $email | Password: $password")
        if (!validateLoginInput(email, password)) {
            return
        }

        lifecycleScope.launch {
            try {
                val res = authService.login(
                    email = email,
                    password = password
                )

//                sessionStore.saveSession(
//                    accessToken = res.access_token,
//                    refreshToken = res.refresh_token,
//                    expiresIn = res.expires_in
//                )

//                Log.d(
//                    "LoginActivity",
//                    "LOGIN OK: userId=${res.user.id}, email=${res.user.email}"
//                )

                Toast.makeText(
                    this@LoginActivity,
                    "Đăng nhập thành công",
                    Toast.LENGTH_SHORT
                ).show()

                // TODO: chuyển sang MainActivity
                startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                finish()

            } catch (e: HttpException) {

                val errorBody = e.response()?.errorBody()?.string()
                Log.e("LoginActivity", "HTTP ${e.code()} | $errorBody", e)

                Toast.makeText(
                    this@LoginActivity,
                    "Sai email hoặc mật khẩu",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Log.e("LoginActivity", "LOGIN ERROR", e)

                Toast.makeText(
                    this@LoginActivity,
                    "Lỗi không xác định",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun validateLoginInput(
        email: String,
        password: String
    ): Boolean {

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email và mật khẩu", Toast.LENGTH_SHORT).show()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
            return false
        }

        return true
    }


    private fun togglePassword() {

        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            // 👁️ Hiện mật khẩu
            edtPassword.transformationMethod =
                HideReturnsTransformationMethod.getInstance()

            btnHidePassword.setImageResource(R.drawable.ic_note_hide)
        } else {
            // 🙈 Ẩn mật khẩu
            edtPassword.transformationMethod =
                PasswordTransformationMethod.getInstance()

            btnHidePassword.setImageResource(R.drawable.ic_hidden)
        }

        // Giữ con trỏ ở cuối text
        edtPassword.setSelection(edtPassword.text.length)
    }

    private fun showForgotPasswordDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_forgot_password, null)
        val builder = AlertDialog.Builder(this)
        builder.setView(view)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val btnSend = view.findViewById<MaterialButton>(R.id.btnSend)
        val btnCancel = view.findViewById<MaterialButton>(R.id.btnCancel)
        val edtEmailReset = view.findViewById<EditText>(R.id.edt_email_reset)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnSend.setOnClickListener {
            val email = edtEmailReset.text.toString().trim()

            if (email.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Email không hợp lệ", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Gọi hàm xử lý và đóng dialog
            handleResetPassword(email)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun handleResetPassword(email: String) {
        // Sử dụng lifecycleScope.launch đồng bộ với cách làm handleLogin
        lifecycleScope.launch {
            try {
                authService.resetPassword(email)

                Log.d("LoginActivity", "Reset email sent to: $email")
                Toast.makeText(
                    this@LoginActivity,
                    "Vui lòng kiểm tra email để đặt lại mật khẩu",
                    Toast.LENGTH_LONG
                ).show()

            } catch (e: Exception) {
                Log.e("LoginActivity", "RESET PASSWORD ERROR", e)
                Toast.makeText(
                    this@LoginActivity,
                    "Lỗi: Không thể gửi email khôi phục",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}