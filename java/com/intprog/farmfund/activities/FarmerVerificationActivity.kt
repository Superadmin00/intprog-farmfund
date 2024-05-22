package com.intprog.farmfund.activities

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.firestore
import com.intprog.farmfund.R
import com.intprog.farmfund.databinding.ActivityFarmerVerificationBinding
import com.intprog.farmfund.fragments.FarmerVerifFirstFragment
import com.intprog.farmfund.fragments.FarmerVerifSecondFragment
import com.intprog.farmfund.fragments.FarmerVerifThirdFragment

class FarmerVerificationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFarmerVerificationBinding
    private lateinit var auth: FirebaseAuth
    private var currentFragmentIndex = 0

    private val fragments = listOf(
        FarmerVerifFirstFragment(),
        FarmerVerifSecondFragment(),
        FarmerVerifThirdFragment()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFarmerVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initially add the first fragment
        supportFragmentManager.beginTransaction()
            .add(R.id.farmerVerificationContainer, fragments[currentFragmentIndex])
            .commit()

        // Set up the button click listeners
        binding.previousBTN.setOnClickListener {
            if (currentFragmentIndex > 0) {
                currentFragmentIndex--
                replaceFragment()
            }
        }

        binding.nextBTN.setOnClickListener {
            if (currentFragmentIndex < fragments.size) {
                val currentFragment = fragments[currentFragmentIndex]
                if (currentFragment.validateInput()) {
                    if (currentFragmentIndex < fragments.size - 1) { // Not the last fragment
                        currentFragmentIndex++
                        replaceFragment()
                    }
                }
            }
        }

        binding.submitBTN.setOnClickListener {
            val currentFragment = fragments[currentFragmentIndex]
            if (currentFragment.validateInput()) {
                val db = Firebase.firestore
                auth = FirebaseAuth.getInstance()
                val userId = auth.currentUser?.uid
                val usersRef = userId?.let { it1 -> db.collection("users").document(it1) }

                usersRef?.update("verified", true)?.addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully updated!")

                    // Create and show the dialog if changing user's verified field in firestore databse is successful
                    val dialogView = LayoutInflater.from(this)
                        .inflate(R.layout.dialog_verification_submitted, null)
                    val builder = AlertDialog.Builder(this)
                    builder.setView(dialogView)
                    val alertDialog = builder.create()
                    alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    alertDialog.show()

                    val gotoHome: Button = alertDialog.findViewById(R.id.gotoHome)
                    gotoHome.setOnClickListener {
                        alertDialog.dismiss()
                    }

                    // Dismiss the dialog and navigate to NavigatorActivity when the dialog is clicked
                    alertDialog.setOnDismissListener {
                        val intent = Intent(this, NavigatorActivity::class.java)
                        startActivity(intent)
                        finish()
                    }
                }?.addOnFailureListener { e ->
                    Log.w("UpdateData", "Error updating document", e)
                    Toast.makeText(
                        this,
                        "Error updating document: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }


        // Set up the global layout listener
        val rootView = window.decorView.rootView
        rootView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                val r = Rect()
                rootView.getWindowVisibleDisplayFrame(r)
                val screenHeight = rootView.height

                // r.bottom is the position above soft keypad or device button.
                // If keypad is shown, the r.bottom is smaller than that before.
                val keypadHeight = screenHeight - r.bottom

                // 0.15 ratio is perhaps enough to determine keypad height.
                if (keypadHeight > screenHeight * 0.15) {
                    // Keyboard is visible, hide the farmerVerificationButtonsLayout
                    binding.farmerVerificationButtonsLayout.visibility = View.GONE
                } else {
                    // Keyboard is hidden, show the farmerVerificationButtonsLayout
                    binding.farmerVerificationButtonsLayout.visibility = View.VISIBLE
                }
            }
        })
    }

    private fun replaceFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.farmerVerificationContainer, fragments[currentFragmentIndex])
            .commit()

        // Update the UI based on the current page
        when (currentFragmentIndex) {
            0 -> {
                binding.previousBTN.visibility = View.GONE
                binding.spaceBetween.visibility = View.GONE
                binding.submitBTN.visibility = View.GONE
            }

            fragments.size - 1 -> {
                binding.submitBTN.visibility = View.VISIBLE
                binding.previousBTN.visibility = View.VISIBLE
                binding.spaceBetween.visibility = View.VISIBLE
                binding.nextBTN.visibility = View.GONE
            }

            else -> {
                binding.submitBTN.visibility = View.GONE
                binding.previousBTN.visibility = View.VISIBLE
                binding.spaceBetween.visibility = View.VISIBLE
            }
        }
    }
}