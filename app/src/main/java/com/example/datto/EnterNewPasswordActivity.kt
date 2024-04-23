package com.example.datto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.NewAccountRequest
import com.example.datto.DataClass.NewAccountResponse
import com.example.datto.DataClass.NewPasswordRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class EnterNewPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_enter_new_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val materialToolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        val email = intent.getStringExtra("email")

        val password = findViewById<TextInputEditText>(R.id.input_password)
        val confirm_password = findViewById<TextInputEditText>(R.id.confirm_password)

        findViewById<Button>(R.id.save_button).setOnClickListener {
            if (password.text.toString() == "") {
                password.error = "This field cannot be empty"
            } else if (confirm_password.text.toString() == "") {
                confirm_password.error = "This field cannot be empty"
            } else if (password.text.toString() != confirm_password.text.toString()) {
                password.error = getString(R.string.not_match_password_helper_text)
                confirm_password.error = getString(R.string.not_match_password_helper_text)
            } else {
                val newPassword = NewPasswordRequest(
                    email.toString(),
                    password.text.toString()
                )
                APIService(this).doPost<NewPasswordRequest>(
                    "accounts/reset-password",
                    newPassword,
                    object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            Log.d("API_SERVICE", "Data: $data")
                            val i: Intent = Intent(applicationContext, SignInActivity::class.java)
                            startActivity(i)
                        }

                        override fun onError(error: Throwable) {
                            Log.e("API_SERVICE", "Error: ${error.message}")
                            Toast.makeText(
                                this@EnterNewPasswordActivity,
                                error.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }
}