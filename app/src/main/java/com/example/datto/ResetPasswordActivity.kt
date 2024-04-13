package com.example.datto

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.NewOtpRequest
import com.example.datto.DataClass.OtpResponse
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class ResetPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_reset_password)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val materialToolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        val email = findViewById<TextInputEditText>(R.id.input_email)

        findViewById<Button>(R.id.continue_button).setOnClickListener {
            if (email.text.toString() == "") {
                email.error = getString(R.string.required_helper_text)
            } else {
                val otp = NewOtpRequest("Reset password", email.text.toString())
                APIService().doPost<OtpResponse>(
                    "otp",
                    otp,
                    object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            Log.d("API_SERVICE", "Data: $data")
                            data as OtpResponse
                            val i: Intent =
                                Intent(applicationContext, VerifyOtpActivity::class.java)
                            i.putExtra("email", email.text.toString()) // Assuming email is a String
                            i.putExtra(
                                "id",
                                data.id
                            ) // Accessing the id property of the data object
                            i.putExtra(
                                "key",
                                "Reset password"
                            )
                            startActivity(i)
                        }

                        override fun onError(error: Throwable) {
                            email.error = error.message
                            Log.e("API_SERVICE", "Error: ${error.message}")
                            Toast.makeText(this@ResetPasswordActivity, error.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        }
    }
}