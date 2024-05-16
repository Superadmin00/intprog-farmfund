package com.intprog.farmfund.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.intprog.farmfund.databinding.ActivityWelcomeBinding

class WelcomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWelcomeBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        // Check if a user is authenticated
        if (auth.currentUser != null) {
            // User is authenticated, navigate to the NavigatorActivity
            val intent = Intent(this, NavigatorActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.getStartedButton.setOnClickListener {
            val intent = Intent(this, NavigatorActivity::class.java)
            intent.putExtra("dialogToShow", "register")
            startActivity(intent)
            finish()
        }

        binding.loginHyperlinkText.setOnClickListener {
            val intent = Intent(this, HolderLoginRegisterActivity::class.java)
            intent.putExtra("dialogToShow", "login")
            startActivity(intent)
            finish()
        }
    }
}