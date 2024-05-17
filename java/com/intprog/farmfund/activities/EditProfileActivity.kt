package com.intprog.farmfund.activities

import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R

class EditProfileActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: TextView
    private lateinit var phoneNumEditText: TextView
    private lateinit var editFullNameIcon: ImageButton
    private lateinit var editEmailIcon: ImageButton
    private lateinit var editNumberIcon: ImageButton
    private lateinit var saveUpdatesButton: Button
    private var isFullNameEditable = false
    private var isEmailEditable = false
    private var isPhoneNumEditable = false
    private var originalFullName: String? = null
    private var originalEmail: String? = null
    private var originalPhoneNum: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize views
        fullNameEditText = findViewById(R.id.fullnamePlaceholder)
        emailEditText = findViewById(R.id.emailPlaceholder)
        phoneNumEditText = findViewById(R.id.numberPlaceholder)
        editFullNameIcon = findViewById(R.id.editNameIcon)
        editEmailIcon = findViewById(R.id.editEmailIcon)
        editNumberIcon = findViewById(R.id.editNumberIcon)
        saveUpdatesButton = findViewById(R.id.saveUpdatesButton)

        // Initially disable edit texts and set input type to none
        fullNameEditText.isEnabled = false
        fullNameEditText.inputType = InputType.TYPE_NULL
        emailEditText.isEnabled = false
        phoneNumEditText.isEnabled = false

        // Set click listener for edit icons
        editFullNameIcon.setOnClickListener {
            if (!isFullNameEditable) {
                it.isSelected = !it.isSelected
                fullNameEditText.isEnabled = true
                fullNameEditText.inputType = InputType.TYPE_CLASS_TEXT
                isFullNameEditable = true
                Toast.makeText(this, "Full Name is now editable", Toast.LENGTH_SHORT).show()
            } else {
                fullNameEditText.isEnabled = false
                fullNameEditText.inputType = InputType.TYPE_NULL
                isFullNameEditable = false
                Toast.makeText(this, "Done editing full name", Toast.LENGTH_SHORT).show()
            }
        }

        editEmailIcon.setOnClickListener {
            if (!isEmailEditable) {
                it.isSelected = !it.isSelected
                emailEditText.isEnabled = true
                emailEditText.inputType = InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS  // Set input type to email
                isEmailEditable = true
                Toast.makeText(this, "Email is now editable", Toast.LENGTH_SHORT).show()
            } else {
                emailEditText.isEnabled = false
                emailEditText.inputType = InputType.TYPE_NULL
                isEmailEditable = false
                Toast.makeText(this, "Done editing email", Toast.LENGTH_SHORT).show()
            }
        }

        editNumberIcon.setOnClickListener {
            if (!isPhoneNumEditable) {
                it.isSelected = !it.isSelected
                phoneNumEditText.isEnabled = true
                phoneNumEditText.inputType = InputType.TYPE_CLASS_NUMBER
                isPhoneNumEditable = true
                Toast.makeText(this, "Phone Number is now editable", Toast.LENGTH_SHORT).show()
            } else {
                phoneNumEditText.isEnabled = false
                phoneNumEditText.inputType = InputType.TYPE_NULL
                isPhoneNumEditable = false
                Toast.makeText(this, "Done editing phone number", Toast.LENGTH_SHORT).show()
            }
        }

        // Get Firebase references
        val auth = FirebaseAuth.getInstance()
        val db = FirebaseFirestore.getInstance()

        val user = auth.currentUser

        user?.let { currentUser ->
            val userId = currentUser.uid
            val usersCollection = db.collection("users")

            usersCollection.whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val userData = document.data
                        val userName = userData?.get("name") as? String  // Use as? for null safety
                        val userEmail = userData?.get("email") as? String
                        val userPhoneNumber = userData?.get("phoneNum") as? String

                        fullNameEditText.text = userName?.let { Editable.Factory.getInstance().newEditable(it) } ?: Editable.Factory.getInstance().newEditable("")
                        emailEditText.text = userEmail
                        phoneNumEditText.text = userPhoneNumber ?: "Not Set"
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                }
        }

        saveUpdatesButton.setOnClickListener {
            val updatedFullName = fullNameEditText.text.toString().trim()
            val updatedEmail = emailEditText.text.toString().trim()
            val updatedPhoneNum = phoneNumEditText.text.toString().trim()

            // Check if any edits were made
            if (updatedFullName == originalFullName && updatedEmail == originalEmail && updatedPhoneNum == originalPhoneNum) {
                Toast.makeText(this, "No changes made to update", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Verify user is signed in before update
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                val userId = user.uid

                // Update Firebase data with query based on userId stored in document
                val db = FirebaseFirestore.getInstance()
                val query = db.collection("users").whereEqualTo("userId", userId)

                query.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val documents = task.result?.documents
                        if (documents != null && documents.isNotEmpty()) {
                            val document = documents[0] // Assuming there's only one document
                            val documentId = document.id

                            val updates = hashMapOf<String, Any>(
                                "name" to updatedFullName,
                                "email" to updatedEmail
                            )

                            if (updatedPhoneNum.isNotEmpty()) {
                                updates["phoneNum"] = updatedPhoneNum
                            }

                            document.reference.update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                                    // ... (Update original values and reset edit text states)
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(this, "Error updating profile: $exception", Toast.LENGTH_SHORT).show()
                                }
                        } else {
                            Toast.makeText(this, "Could not find user document", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Error fetching user data: ${task.exception}", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please sign in to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }
    fun toggleEditMode(view: View) {
        val editText = findViewById<EditText>(R.id.fullnamePlaceholder) // Replace with your EditText ID
        val editIcon = view as ImageButton

        if (editText.isEnabled) {
            // Editing mode enabled, switch to non-editable and change icon
            editText.isEnabled = false
            editText.inputType = InputType.TYPE_NULL
            editIcon.setImageResource(R.drawable.ic_edit)
        } else {
            // Editing mode disabled, switch to editable and change icon
            editText.isEnabled = true
            editText.inputType = InputType.TYPE_TEXT_VARIATION_PERSON_NAME
            editIcon.setImageResource(R.drawable.ic_check)
        }
    }
}