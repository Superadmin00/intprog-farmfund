package com.intprog.farmfund.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.intprog.farmfund.R

class ForgotPasswordActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        val emailLayout: TextInputLayout = findViewById(R.id.emailTextInputLayout)
        val emailInput: TextInputEditText = findViewById(R.id.emailEditText)

        val sendButton: Button = findViewById(R.id.otpButton)
        val backButton : ImageButton = findViewById(R.id.backButton)

        val otpWindow: RelativeLayout = findViewById(R.id.otpWindow)
        otpWindow.isFocusable = false
        otpWindow.isFocusableInTouchMode = false

        val emailDisplay: TextView = findViewById(R.id.emailTextView)
        val forgotRL: RelativeLayout = findViewById(R.id.forgotRL)
        sendButton.setOnClickListener {
            val  email = emailInput.text.toString()
            val  validEmailSyntax = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

            if (email.isNotEmpty()) {
                if (!email.matches(validEmailSyntax.toRegex())){
                    emailLayout.error = "This email is invalid!"
                    return@setOnClickListener
                }else{
                    emailLayout.error = null
                    otpWindow.visibility = View.VISIBLE
                    emailDisplay.text = email
                    sendButton.visibility = View.INVISIBLE
                    emailLayout.isEnabled = false
                    backButton.isEnabled = false
                }
            }else{
                emailLayout.error = "This field is required!"
                return@setOnClickListener
            }
        }

        backButton.setOnClickListener{
            finish()
        }

        val verifyButton : Button = findViewById(R.id.verifyButton)
        verifyButton.setOnClickListener {
            val otp1 = findViewById<EditText>(R.id.otp1)
            val otp2 = findViewById<EditText>(R.id.otp2)
            val otp3 = findViewById<EditText>(R.id.otp3)
            val otp4 = findViewById<EditText>(R.id.otp4)
            val otp5 = findViewById<EditText>(R.id.otp5)
            val otp6 = findViewById<EditText>(R.id.otp6)

            val otp = otp1.text.toString() +
                    otp2.text.toString() +
                    otp3.text.toString() +
                    otp4.text.toString() +
                    otp5.text.toString() +
                    otp6.text.toString()

            val validOTP = "123456"

            if (otp == validOTP){
                val passValue = "Change Password"
                val intent = Intent(this, ProjectDetailsActivity::class.java)
                intent.putExtra("pass", passValue)
                startActivity(intent)
            }else{
                otp1.text.clear()
                otp2.text.clear()
                otp3.text.clear()
                otp4.text.clear()
                otp5.text.clear()
                otp6.text.clear()
                Toast.makeText(this, "Invalid OTP", Toast.LENGTH_SHORT).show()
            }

        }
        val cancelButton : Button = findViewById(R.id.cancelButton)
        cancelButton.setOnClickListener {
            otpWindow.visibility = View.INVISIBLE
            sendButton.visibility = View.VISIBLE
            emailLayout.isEnabled = true
            backButton.isEnabled = true
        }

        val layout: RelativeLayout = findViewById(R.id.layout2)
        layout.setOnClickListener {
            val inputMethodManager = it.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
        }
    }
}