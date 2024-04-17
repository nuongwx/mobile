package com.example.datto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.Credential.CredentialService
import com.example.datto.DataClass.AccountRequest
import com.example.datto.DataClass.AccountResponse
import com.example.datto.DataClass.NewAccountResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText


class SignInActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val username = findViewById<TextInputEditText>(R.id.input_username)
        val password = findViewById<TextInputEditText>(R.id.input_password)
        val materialToolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.forgot_password).setOnClickListener {
            startActivity(Intent(this, ResetPasswordActivity::class.java))
        }
        findViewById<TextView>(R.id.sign_up).setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
        findViewById<Button>(R.id.sign_in_button).setOnClickListener {
            if (username.text.toString() == "") {
                username.error = getString(R.string.required_helper_text)
            }
            if (password.text.toString() == "") {
                password.error = getString(R.string.required_helper_text)
            }
            if (username.text.toString() != "" && password.text.toString() != "") {
                val newAccount = AccountRequest(
                    username.text.toString(),
                    password.text.toString()
                )
                APIService(this).doPost<NewAccountResponse>(
                    "accounts/sign-in",
                    newAccount,
                    object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            data as NewAccountResponse
                            Log.d("API_SERVICE", "Data: ${data.id}")
                            CredentialService().set(data.id)
                            val i: Intent = Intent(applicationContext, MainActivity::class.java)
                            startActivity(i)
                        }

                        override fun onError(error: Throwable) {
                            Log.e("API_SERVICE", "Error: ${error.message}")
                            Toast.makeText(
                                this@SignInActivity,
                                error.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }

    }

}