package com.intprog.farmfund.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.intprog.farmfund.R
import kotlin.reflect.KMutableProperty0

class EditProfileActivity : AppCompatActivity() {

    private lateinit var fullNameEditText: EditText
    private lateinit var emailEditText: TextView
    private lateinit var phoneNumEditText: TextView
    private lateinit var editFullNameIcon: ImageButton
    private lateinit var editEmailIcon: ImageButton
    private lateinit var editNumberIcon: ImageButton
    private lateinit var saveUpdatesButton: Button
    private lateinit var imagePlaceholder: ImageView
    private var isFullNameEditable = false
    private var isEmailEditable = false
    private var isPhoneNumEditable = false
    private var originalFullName: String? = null
    private var originalEmail: String? = null
    private var originalPhoneNum: String? = null
    private var photoURI: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

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
        imagePlaceholder = findViewById(R.id.imagePlaceholder)

        // Initially disable edit texts and set input type to none
        fullNameEditText.isEnabled = false
        fullNameEditText.inputType = InputType.TYPE_NULL
        emailEditText.isEnabled = false
        phoneNumEditText.isEnabled = false

        // Set click listener for edit icons
        editFullNameIcon.setOnClickListener {
            toggleEditText(fullNameEditText, it as ImageButton, ::isFullNameEditable)
        }

        editEmailIcon.setOnClickListener {
            toggleEditText(emailEditText, it as ImageButton, ::isEmailEditable, InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS)
        }

        editNumberIcon.setOnClickListener {
            toggleEditText(phoneNumEditText, it as ImageButton, ::isPhoneNumEditable, InputType.TYPE_CLASS_NUMBER)
        }

        imagePlaceholder.setOnClickListener {
            showImagePickerOptions()
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
                        val userName = userData?.get("name") as? String
                        val userEmail = userData?.get("email") as? String
                        val userPhoneNumber = userData?.get("phoneNum") as? String
                        val userImageURL = userData?.get("userImageURL") as? String

                        fullNameEditText.text = userName?.let { Editable.Factory.getInstance().newEditable(it) } ?: Editable.Factory.getInstance().newEditable("")
                        emailEditText.text = userEmail
                        phoneNumEditText.text = userPhoneNumber ?: "Not Set"
                        userImageURL?.let { url ->
                            Glide.with(this).load(url).placeholder(R.drawable.img_default_pfp).into(imagePlaceholder)
                        } ?: imagePlaceholder.setImageResource(R.drawable.img_default_pfp)

                        // Save original values
                        originalFullName = userName
                        originalEmail = userEmail
                        originalPhoneNum = userPhoneNumber
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
                                    // Update original values and reset edit text states
                                    originalFullName = updatedFullName
                                    originalEmail = updatedEmail
                                    originalPhoneNum = updatedPhoneNum
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

    private fun toggleEditText(editText: TextView, icon: ImageButton, flag: KMutableProperty0<Boolean>, inputType: Int = InputType.TYPE_CLASS_TEXT) {
        if (!flag.get()) {
            icon.isSelected = !icon.isSelected
            editText.isEnabled = true
            editText.inputType = inputType
            flag.set(true)
            Toast.makeText(this, "${editText.hint} is now editable", Toast.LENGTH_SHORT).show()
        } else {
            editText.isEnabled = false
            editText.inputType = InputType.TYPE_NULL
            flag.set(false)
            Toast.makeText(this, "Done editing ${editText.hint}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showImagePickerOptions() {
        val options = arrayOf("Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> pickImageFromGallery()
            }
        }
        builder.show()
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, REQUEST_IMAGE_PICK)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    photoURI?.let {
                        uploadImageToFirebase(it)
                    }
                }
                REQUEST_IMAGE_PICK -> {
                    data?.data?.let {
                        photoURI = it
                        uploadImageToFirebase(it)
                    }
                }
            }
        }
    }

    private fun uploadImageToFirebase(fileUri: Uri) {
        val storageReference = FirebaseStorage.getInstance().reference
        val user = FirebaseAuth.getInstance().currentUser
        val userId = user?.uid

        userId?.let {
            val fileReference = storageReference.child("profile_images/$it.jpg")
            fileReference.putFile(fileUri)
                .addOnSuccessListener {
                    fileReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        saveImageUrlToFirestore(imageUrl)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun saveImageUrlToFirestore(imageUrl: String) {
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            val userId = it.uid
            val db = FirebaseFirestore.getInstance()

            db.collection("users").whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        document.reference.update("userImageURL", imageUrl)
                            .addOnSuccessListener {
                                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show()
                                Glide.with(this).load(imageUrl).into(imagePlaceholder)
                            }
                            .addOnFailureListener {
                                Toast.makeText(this, "Failed to update profile picture URL", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to find user document", Toast.LENGTH_SHORT).show()
                }
        }
    }
}