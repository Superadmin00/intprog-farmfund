package com.intprog.farmfund.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.intprog.farmfund.R
import com.intprog.farmfund.objects.LoadingDialog

class ForgotPasswordActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)

        auth = FirebaseAuth.getInstance()

        val emailLayout: TextInputLayout = findViewById(R.id.emailTextInputLayout)
        val emailInput: TextInputEditText = findViewById(R.id.emailEditText)
        val sendButton: Button = findViewById(R.id.otpButton)
        val backButton: ImageButton = findViewById(R.id.backButton)

        sendButton.setOnClickListener {
            val email = emailInput.text.toString()
            val validEmailSyntax = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"

            if (email.isNotEmpty()) {
                if (!email.matches(validEmailSyntax.toRegex())) {
                    emailLayout.error = "This email is invalid!"
                } else {
                    emailLayout.error = null
                    auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(this, "Password Reset Link Sent", Toast.LENGTH_SHORT).show()
                                LoadingDialog.show(this, false)
                                showPasswordResetDialog()
                            } else {
                                Toast.makeText(this, "Error sending reset link", Toast.LENGTH_SHORT).show()
                            }
                        }
                }
            } else {
                emailLayout.error = "This field is required!"
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }

    private fun showPasswordResetDialog() {
        val dialogBuilder = AlertDialog.Builder(this)
        dialogBuilder.setMessage("We have sent a password reset link to your email. Please check your email and reset your password. Once done, please log in again.")
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, id ->
                dialog.dismiss()
                LoadingDialog.dismiss()
                redirectToLogin()
            }
        val alert = dialogBuilder.create()
        alert.setTitle("Password Reset")
        alert.show()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, HolderLoginRegisterActivity::class.java)
        intent.putExtra("dialogToShow", "login")
        startActivity(intent)
        finish()
    }
}
