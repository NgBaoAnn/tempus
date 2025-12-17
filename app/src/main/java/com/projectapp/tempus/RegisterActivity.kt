package com.projectapp.tempus

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class RegisterActivity : AppCompatActivity() {

    private lateinit var edtFullName: EditText
    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var edtConfirmPassword: EditText

    private lateinit var btnConfirm: MaterialButton
    private lateinit var btnLogin: Button

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
        addEvents()
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

        Log.d(
            "RegisterActivity",
            "FullName: $fullName | Email: $email | Password: $password | Confirm: $confirmPassword"
        )

        // TODO: validate + gọi API
    }


}