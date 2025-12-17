package com.projectapp.tempus

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var btnLoginEmail: MaterialButton
    private lateinit var btnLoginGoogle: MaterialButton
    private lateinit var btnForgotPassword: Button
    private lateinit var btnRegister: Button
    private lateinit var btnHidePassword: ImageButton

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
    }

    private fun addEvents() {

        btnLoginEmail.setOnClickListener {
            handleLogin()
        }

        btnForgotPassword.setOnClickListener {
            // TODO: mở màn hình quên mật khẩu
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
    }

    private fun togglePassword() {
        // xử lý ẩn / hiện mật khẩu
    }
}