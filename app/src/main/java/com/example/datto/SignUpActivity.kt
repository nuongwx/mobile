package com.example.datto

import android.content.Intent
import com.example.datto.DataClass.AccountResponse
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.NewAccountRequest
import com.example.datto.DataClass.NewGroupResponse
import com.example.datto.DataClass.NewOtpRequest
import com.example.datto.DataClass.OtpResponse
import com.example.datto.utils.GoogleAuth
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class SignUpActivity : AppCompatActivity() {

    private lateinit var googleAuth: GoogleAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        val email = findViewById<TextInputEditText>(R.id.input_email)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val materialToolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            finish()
        }
        findViewById<TextView>(R.id.sign_in).setOnClickListener {
            startActivity(Intent(this, SignInActivity::class.java))
        }

        findViewById<Button>(R.id.sign_up_button).setOnClickListener {
            if (email.text.toString() == "") {
                email.error = getString(R.string.required_helper_text)
            } else if (!isValidEmail(email.text.toString())) {
                email.error = getString(R.string.invalid_email_helper_text)
            } else {
                val otp = NewOtpRequest("Sign up", email.text.toString())
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
                                "Sign up"
                            )
                            startActivity(i)
                        }

                        override fun onError(error: Throwable) {
                            email.error = error.message
                            Log.e("API_SERVICE", "Error: ${error.message}")
                            Toast.makeText(this@SignUpActivity, error.message, Toast.LENGTH_SHORT)
                                .show()
                        }
                    })
            }
        }

        googleAuth = GoogleAuth(this)
        findViewById<Button>(R.id.google_button).setOnClickListener {
            googleAuth.signInWithGoogle()
        }
    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = Regex("^\\S+@\\S+\\.\\S+\$")
        return emailRegex.matches(email)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        googleAuth.onActivityResult(requestCode, data)
    }
}