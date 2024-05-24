package com.intprog.farmfund.activities

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.intprog.farmfund.R
import kotlin.reflect.KMutableProperty0

class EditProfileActivity : AppCompatActivity() {

    private lateinit var fullNameTextView: TextView
    private lateinit var emailTextView: TextView
    private lateinit var phoneNumEditText: EditText
    private lateinit var editNumberIcon: ImageButton
    private lateinit var saveUpdatesButton: Button
    private lateinit var imagePlaceholder: ImageView

    private lateinit var firstNameEditText: EditText
    private lateinit var middleNameEditText: EditText
    private lateinit var lastNameEditText: EditText
    private lateinit var municipalityEditText: EditText
    private lateinit var provinceEditText: EditText
    private lateinit var barangayEditText: EditText
    private lateinit var zipEditText: EditText

    private lateinit var editFirstNameIcon: ImageButton
    private lateinit var editMiddleNameIcon: ImageButton
    private lateinit var editLastNameIcon: ImageButton
    private lateinit var editMunicipalityIcon: ImageButton
    private lateinit var editProvinceIcon: ImageButton
    private lateinit var editBarangayIcon: ImageButton
    private lateinit var editZipIcon: ImageButton

    private var isPhoneNumEditable = false
    private var isFirstNameEditable = false
    private var isMiddleNameEditable = false
    private var isLastNameEditable = false
    private var isMunicipalityEditable = false
    private var isProvinceEditable = false
    private var isBarangayEditable = false
    private var isZipEditable = false

    private var originalEmail: String? = null
    private var originalPhoneNum: String? = null
    private var originalFirstName: String? = null
    private var originalMiddleName: String? = null
    private var originalLastName: String? = null
    private var originalMunicipality: String? = null
    private var originalProvince: String? = null
    private var originalBarangay: String? = null
    private var originalZip: String? = null

    private var photoURI: Uri? = null
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        // Initialize views
        fullNameTextView = findViewById(R.id.fullnamePlaceholder)
        emailTextView = findViewById(R.id.emailPlaceholder)
        phoneNumEditText = findViewById(R.id.numberPlaceholder)
        editNumberIcon = findViewById(R.id.editNumberIcon)
        saveUpdatesButton = findViewById(R.id.saveUpdatesButton)
        imagePlaceholder = findViewById(R.id.imagePlaceholder)

        firstNameEditText = findViewById(R.id.firstNamePlaceholder)
        middleNameEditText = findViewById(R.id.editMiddleInitialPlaceholder)
        lastNameEditText = findViewById(R.id.editLastNamePlaceholder)
        municipalityEditText = findViewById(R.id.editMunicipalityPlaceholder)
        provinceEditText = findViewById(R.id.editProvincePlaceholder)
        barangayEditText = findViewById(R.id.editBarangayPlaceholder)
        zipEditText = findViewById(R.id.editZipPlaceholder)

        editFirstNameIcon = findViewById(R.id.editFirstNameIcon)
        editMiddleNameIcon = findViewById(R.id.editMiddleNameIcon)
        editLastNameIcon = findViewById(R.id.editLastNameIcon)
        editMunicipalityIcon = findViewById(R.id.editMunicipalityIcon)
        editProvinceIcon = findViewById(R.id.editProvinceIcon)
        editBarangayIcon = findViewById(R.id.editBarangayIcon)
        editZipIcon = findViewById(R.id.editZipIcon)

        // Initially disable edit texts and set input type to none
        disableEditText(phoneNumEditText)
        disableEditText(firstNameEditText)
        disableEditText(middleNameEditText)
        disableEditText(lastNameEditText)
        disableEditText(municipalityEditText)
        disableEditText(provinceEditText)
        disableEditText(barangayEditText)
        disableEditText(zipEditText)

        // Set click listener for edit icons

        editNumberIcon.setOnClickListener {
            toggleEditText(
                phoneNumEditText,
                it as ImageButton,
                ::isPhoneNumEditable,
                InputType.TYPE_CLASS_NUMBER
            )
        }

        editFirstNameIcon.setOnClickListener {
            toggleEditText(firstNameEditText, it as ImageButton, ::isFirstNameEditable)
        }

        editMiddleNameIcon.setOnClickListener {
            toggleEditText(middleNameEditText, it as ImageButton, ::isMiddleNameEditable)
        }

        editLastNameIcon.setOnClickListener {
            toggleEditText(lastNameEditText, it as ImageButton, ::isLastNameEditable)
        }

        editMunicipalityIcon.setOnClickListener {
            toggleEditText(municipalityEditText, it as ImageButton, ::isMunicipalityEditable)
        }

        editProvinceIcon.setOnClickListener {
            toggleEditText(provinceEditText, it as ImageButton, ::isProvinceEditable)
        }

        editBarangayIcon.setOnClickListener {
            toggleEditText(barangayEditText, it as ImageButton, ::isBarangayEditable)
        }

        editZipIcon.setOnClickListener {
            toggleEditText(zipEditText, it as ImageButton, ::isZipEditable)
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
                        val userEmail = userData?.get("email") as? String
                        val userPhoneNumber = userData?.get("phoneNum") as? String
                        val userImageURL = userData?.get("userImageURL") as? String

                        val address = userData?.get("address") as? Map<String, Any> // Access the address map
                        val firstName = userData?.get("firstName") as? String
                        val middleName = userData?.get("midName") as? String
                        val lastName = userData?.get("lastName") as? String
                        // Retrieve values from the address map
                        val barangay = address?.get("barangay") as? String
                        val municipality = address?.get("municipality") as? String
                        val province = address?.get("province") as? String
                        val zip = address?.get("zip code") as? String

                        emailTextView.text = userEmail
                        phoneNumEditText.text = userPhoneNumber?.let { Editable.Factory.getInstance().newEditable(it) }
                            ?: Editable.Factory.getInstance().newEditable("Not Set")
                        firstNameEditText.text =
                            firstName?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        middleNameEditText.text =
                            middleName?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        lastNameEditText.text =
                            lastName?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        municipalityEditText.text =
                            municipality?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        provinceEditText.text =
                            province?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        barangayEditText.text =
                            barangay?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")
                        zipEditText.text =
                            zip?.let { Editable.Factory.getInstance().newEditable(it) }
                                ?: Editable.Factory.getInstance().newEditable("Not Set")

                        userImageURL?.let { url ->
                            Glide.with(this).load(url).placeholder(R.drawable.img_default_pfp)
                                .into(imagePlaceholder)
                        } ?: imagePlaceholder.setImageResource(R.drawable.img_default_pfp)

                        // Save original values
                        originalEmail = userEmail
                        originalPhoneNum = userPhoneNumber
                        originalFirstName = firstName
                        originalMiddleName = middleName
                        originalLastName = lastName
                        originalMunicipality = municipality
                        originalProvince = province
                        originalBarangay = barangay
                        originalZip = zip

                        // Update full name
                        updateFullName()
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                }
        }
        saveUpdatesButton.setOnClickListener {
            val updatedPhoneNum = phoneNumEditText.text.toString().trim()
            val updatedFirstName = firstNameEditText.text.toString().trim()
            val updatedMiddleName = middleNameEditText.text.toString().trim()
            val updatedLastName = lastNameEditText.text.toString().trim()
            val updatedMunicipality = municipalityEditText.text.toString().trim()
            val updatedProvince = provinceEditText.text.toString().trim()
            val updatedBarangay = barangayEditText.text.toString().trim()
            val updatedZip = zipEditText.text.toString().trim()

            // Check if any edits were made
            if (updatedPhoneNum == originalPhoneNum &&
                updatedFirstName == originalFirstName &&
                updatedMiddleName == originalMiddleName &&
                updatedLastName == originalLastName &&
                updatedMunicipality == originalMunicipality &&
                updatedProvince == originalProvince &&
                updatedBarangay == originalBarangay &&
                updatedZip == originalZip
            ) {
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
                                "firstName" to updatedFirstName,
                                "midName" to updatedMiddleName,
                                "lastName" to updatedLastName
                            )

                            // Create a map for the updated address
                            val updatedAddress = hashMapOf<String, Any>(
                                "municipality" to updatedMunicipality,
                                "province" to updatedProvince,
                                "barangay" to updatedBarangay,
                                "zip code" to updatedZip
                            )

                            updates["address"] = updatedAddress // Add address map to updates

                            if (updatedPhoneNum.isNotEmpty()) {
                                updates["phoneNum"] = updatedPhoneNum
                            }

                            document.reference.update(updates)
                                .addOnSuccessListener {
                                    Toast.makeText(
                                        this,
                                        "Profile updated successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // Update original values and reset edit text states
                                    originalPhoneNum = updatedPhoneNum
                                    originalFirstName = updatedFirstName
                                    originalMiddleName = updatedMiddleName
                                    originalLastName = updatedLastName
                                    originalMunicipality = updatedMunicipality
                                    originalProvince = updatedProvince
                                    originalBarangay = updatedBarangay
                                    originalZip = updatedZip
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(
                                        this,
                                        "Error updating profile: $exception",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                        } else {
                            Toast.makeText(this, "Could not find user document", Toast.LENGTH_SHORT)
                                .show()
                        }
                    } else {
                        Toast.makeText(
                            this,
                            "Error fetching user data: ${task.exception}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(this, "Please sign in to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun toggleEditText(
        editText: TextView,
        icon: ImageButton,
        flag: KMutableProperty0<Boolean>,
        inputType: Int = InputType.TYPE_CLASS_TEXT
    ) {
        if (!flag.get()) {
            icon.isSelected = !icon.isSelected
            editText.isEnabled = true
            editText.inputType = inputType
            flag.set(true)
            Toast.makeText(this, "${editText.hint} is now editable", Toast.LENGTH_SHORT).show()
        } else {
            disableEditText(editText)
            flag.set(false)
            Toast.makeText(this, "Done editing ${editText.hint}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun disableEditText(editText: TextView) {
        editText.isEnabled = false
        editText.inputType = InputType.TYPE_NULL
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
                                Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT)
                                    .show()
                                Glide.with(this).load(imageUrl).into(imagePlaceholder)
                            }
                            .addOnFailureListener {
                                Toast.makeText(
                                    this,
                                    "Failed to update profile picture URL",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to find user document", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateFullName() {
        val firstName = firstNameEditText.text.toString()
        val middleName = middleNameEditText.text.toString()
        val lastName = lastNameEditText.text.toString()

        val fullName = if (middleName.isNotEmpty()) {
            "$firstName $middleName $lastName"
        } else {
            "$firstName $lastName"
        }

        fullNameTextView.text = fullName
    }
}