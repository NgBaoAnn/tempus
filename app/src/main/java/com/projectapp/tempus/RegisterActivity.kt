package com.projectapp.tempus

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
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class RegisterActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText

    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnLogin: Button

    private lateinit var authService: AuthService
    private lateinit var sessionStore: SecureSessionStore


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addControls()
        initAuthService()
        addEvents()
    }

    private fun initAuthService() {

        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(
                SupabaseAnonInterceptor(BuildConfig.SUPABASE_KEY)
            )
            .addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
            )
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BuildConfig.SUPABASE_URL + "/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val authApi = retrofit.create(SupabaseAuthApi::class.java)

        authService = AuthService(
            api = authApi
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

        Log.d("RegisterActivity", "FullName: $fullName | Email: $email")

        if (!validateRegisterInput(fullName, email, password, confirmPassword)) {
            return
        }

        lifecycleScope.launch {
            try {
                val res = authService.register(
                    email = email,
                    password = password,
                    fullName = fullName
                )

                Log.d(
                    "RegisterActivity",
                    "REGISTER OK: id=${res.id}, email=${res.email}"
                )

                Toast.makeText(
                    this@RegisterActivity,
                    "Đăng ký thành công",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: retrofit2.HttpException) {

                val errorBody = e.response()?.errorBody()?.string()

                Log.e(
                    "RegisterActivity",
                    "HTTP ${e.code()} | BODY: $errorBody",
                    e
                )

                Toast.makeText(
                    this@RegisterActivity,
                    "Đăng ký thất bại: ${e.code()}",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: java.io.IOException) {

                Log.e("RegisterActivity", "NETWORK ERROR", e)

                Toast.makeText(
                    this@RegisterActivity,
                    "Lỗi mạng",
                    Toast.LENGTH_SHORT
                ).show()

            } catch (e: Exception) {

                Log.e("RegisterActivity", "UNKNOWN ERROR TYPE = ${e::class.java.name}")
                Log.e("RegisterActivity", "MESSAGE = ${e.message}")
                Log.e("RegisterActivity", "STACKTRACE", e)

                Toast.makeText(
                    this@RegisterActivity,
                    "Lỗi không xác định",
                    Toast.LENGTH_SHORT
                ).show()
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