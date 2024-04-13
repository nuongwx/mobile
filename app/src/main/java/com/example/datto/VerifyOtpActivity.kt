package com.example.datto

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Display
import android.view.KeyEvent
import android.view.View
import android.view.ViewTreeObserver
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.datto.API.APICallback
import com.example.datto.API.APIService
import com.example.datto.DataClass.NewOtpRequest
import com.example.datto.DataClass.OtpResponse
import com.example.datto.DataClass.VerifyOtpRequest
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.textfield.TextInputEditText

class VerifyOtpActivity : AppCompatActivity() {

    private val otpFields: MutableList<TextInputEditText> = mutableListOf()
    private lateinit var scrollView: ScrollView
    private lateinit var bottomLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_verify_otp)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val materialToolbar = findViewById<MaterialToolbar>(R.id.materialToolbar)
        materialToolbar.setNavigationOnClickListener {
            finish()
        }

        findViewById<Button>(R.id.submit_button).setOnClickListener {
            startActivity(Intent(this, EnterNewPasswordActivity::class.java))
        }

        // Add all OTP fields to the list
        otpFields.add(findViewById(R.id.number_1))
        otpFields.add(findViewById(R.id.number_2))
        otpFields.add(findViewById(R.id.number_3))
        otpFields.add(findViewById(R.id.number_4))
        otpFields.add(findViewById(R.id.number_5))
        otpFields.add(findViewById(R.id.number_6))

        // Set TextWatcher for each OTP field
        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    // Move focus to the next OTP field when the current field is filled
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus()
                    }
                }

                override fun afterTextChanged(s: Editable?) {}
            })

            // Handle backspace press
            editText.setOnKeyListener { _, keyCode, event ->
                if (keyCode == KeyEvent.KEYCODE_DEL && event.action == KeyEvent.ACTION_DOWN) {
                    // Move focus to the previous OTP field if backspace is pressed on an empty field
                    if (index > 0 && (editText.text?.isEmpty() == true)) {
                        otpFields[index - 1].text = Editable.Factory.getInstance().newEditable("")
                        otpFields[index - 1].requestFocus()
                        return@setOnKeyListener true // Consume the event
                    }
                }
                false // Return false to indicate that the event is not consumed
            }
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

        scrollView = findViewById(R.id.scrollView)
        bottomLayout = findViewById(R.id.bottomLayout)

        // Add global layout listener to detect keyboard visibility changes
        scrollView.viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            scrollView.getWindowVisibleDisplayFrame(r)
            val screenHeight = scrollView.rootView.height

            // Calculate the height difference between the visible display frame and the screen height
            val keypadHeight = screenHeight - r.bottom
            if (keypadHeight > screenHeight * 0.15) { // If the height difference is significant, keyboard is shown
                adjustLayoutOnKeyboardShown(keypadHeight)
            } else { // If the height difference is not significant, keyboard is hidden
                adjustLayoutOnKeyboardHidden()
            }
        }

        val email = intent.getStringExtra("email").toString()
        var id = intent.getStringExtra("id").toString()
        val key = intent.getStringExtra("key").toString()
        val text_top = findViewById<TextView>(R.id.text_top)
        "Please enter the verification code sent to $email".also { text_top.text = it }

        findViewById<TextView>(R.id.resend_otp).setOnClickListener {
            val otp = NewOtpRequest(key, email.toString())
            APIService().doPost<OtpResponse>(
                "otp",
                otp,
                object :
                    APICallback<Any> {
                    override fun onSuccess(data: Any) {
                        Log.d("API_SERVICE", "Data: $data")
                        data as OtpResponse
                        id = data.id
                    }

                    override fun onError(error: Throwable) {
                        Log.e("API_SERVICE", "Error: ${error.message}")
                    }
                })
        }

        findViewById<Button>(R.id.submit_button).setOnClickListener {
            var code: String = ""

            otpFields.forEachIndexed { index, editText ->
                code += editText.text.toString()
            }
            if (code.length < 6) {
                Toast.makeText(this@VerifyOtpActivity, "Invalid code. Please check your code and try again.", Toast.LENGTH_SHORT).show()
            } else {
                val verifyOtpRq = VerifyOtpRequest(id, code.toInt())
                APIService().doPost<Any>(
                    "otp/verification",
                    verifyOtpRq,
                    object :
                        APICallback<Any> {
                        override fun onSuccess(data: Any) {
                            Log.d("API_SERVICE", "Verify OTP success")
                            if (key == "Sign up") {
                                val i: Intent =
                                    Intent(applicationContext, SignUpInfoActivity::class.java)
                                i.putExtra("email", email.toString())
                                startActivity(i)
                            } else {
                                val i: Intent =
                                    Intent(applicationContext, EnterNewPasswordActivity::class.java)
                                i.putExtra("email", email.toString())
                                startActivity(i)
                            }
                        }

                        override fun onError(error: Throwable) {
                            Log.e("API_SERVICE", "Error: ${error.message}")
                            Toast.makeText(
                                this@VerifyOtpActivity,
                                error.message,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
        }
    }

    private fun adjustLayoutOnKeyboardShown(keypadHeight: Int) {
        // Adjust the layout when the soft keyboard is shown
        val layoutParams = bottomLayout.layoutParams as RelativeLayout.LayoutParams
        layoutParams.bottomMargin = keypadHeight
        bottomLayout.layoutParams = layoutParams
    }

    private fun adjustLayoutOnKeyboardHidden() {
        // Adjust the layout when the soft keyboard is hidden
        val layoutParams = bottomLayout.layoutParams as RelativeLayout.LayoutParams
        layoutParams.bottomMargin = 0
        bottomLayout.layoutParams = layoutParams
    }
}

