package com.intprog.farmfund.activities

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import java.util.Calendar
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.FirebaseStorage
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.UploadProjectImageAdapter
import com.intprog.farmfund.databinding.ActivityProposeProjectBinding
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.objects.HideKeyboardOnClick
import com.intprog.farmfund.objects.LoadingDialog
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class ProposeProjectActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProposeProjectBinding
    private val PICK_IMAGE_REQUEST_CODE = 1

    private lateinit var adapter: UploadProjectImageAdapter
    private var imageList = mutableListOf<Bitmap>() // This is where you'll store the images

    private lateinit var auth: FirebaseAuth
    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference

    private val calendar = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProposeProjectBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        val db = Firebase.firestore

        // Grid Layout of Image Uploads
        val numberOfColumns = 2
        binding.uploadedImagesRecyclerView.layoutManager = GridLayoutManager(this, numberOfColumns)
        adapter = UploadProjectImageAdapter(imageList) // Pass the imageList to the adapter
        binding.uploadedImagesRecyclerView.adapter = adapter

        binding.backButton.setOnClickListener {
            finish()
        }

        binding.imageViewUploadBTN.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST_CODE)
        }

        binding.projectDueDateEditText.setOnClickListener {
            showDatePicker()
        }

        binding.submitButton.setOnClickListener {
            val fields = listOf(
                binding.projectTitleEditText to binding.projectTitleTextLayout,
                binding.projectDescriptionEditText to binding.projectDescriptionTextLayout,
                binding.projectMilestoneEditText to binding.projectMilestoneTextLayout,
                binding.projectFundGoalEditText to binding.projectFundGoalTextLayout
            )

            for ((editText, textLayout) in fields) {
                if (editText.text!!.isEmpty()) {
                    textLayout.error = "This field is required."
                    return@setOnClickListener
                }
            }

            if (imageList.isEmpty()) {
                binding.errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            val dueDateString = binding.projectDueDateEditText.text.toString()
            if (dueDateString.isEmpty()) {
                binding.projectDueDateTextLayout.error = "This field is required."
                return@setOnClickListener
            }
            val dueDate = dateFormat.parse(dueDateString)
            val currentDate = Calendar.getInstance().time

            if (dueDate == null) {
                binding.projectDueDateTextLayout.error = "Invalid date."
                return@setOnClickListener
            } else if (dueDate.before(currentDate)) {
                binding.projectDueDateTextLayout.error = "Due date must be today or later."
                return@setOnClickListener
            }

            LoadingDialog.show(this, false)

            val title = binding.projectTitleEditText.text.toString()
            val descriptions = binding.projectDescriptionEditText.text.toString()
            val milestone = binding.projectMilestoneEditText.text.toString()
            val fundGoal = binding.projectFundGoalEditText.text.toString().toDouble()
            val status = "Active"

            val user = auth.currentUser

            val project = Project(
                projId = "", // Temporary value
                userId = user?.uid ?: "userId",
                projTitle = title,
                projDescription = descriptions,
                projMilestone = milestone,
                projDonorsCount = 0,
                projFundGoal = fundGoal,
                projFundsReceived = 0.0,
                projDueDate = dueDate,
                projStatus = status
            )

            db.collection("projects")
                .add(project)
                .addOnSuccessListener { documentReference ->
                    val projectId = documentReference.id
                    val updatedProject = project.copy(projId = projectId)

                    db.collection("projects").document(projectId)
                        .set(updatedProject)
                        .addOnSuccessListener {
                            uploadImages(projectId)
                            LoadingDialog.dismiss()
                            // If all validations are met and project successfully created
                            val projSubmittedDialog =
                                LayoutInflater.from(this).inflate(R.layout.dialog_project_proposed, null)
                            val builder = AlertDialog.Builder(this).setView(projSubmittedDialog)
                            val alertDialog = builder.show()
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                            val gotoHome: Button = projSubmittedDialog.findViewById(R.id.gotoHome)
                            gotoHome.setOnClickListener {
                                alertDialog.dismiss()
                                finish()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Error updating project ID: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            LoadingDialog.dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error adding project: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    LoadingDialog.dismiss()
                }
        }

        // Code to close keyboard when clicking outside
        binding.mainLayout.setOnClickListener {
            HideKeyboardOnClick.hideKeyboardOnClick(it)
        }
        // Code to close keyboard when clicking outside
        binding.mainContainer.setOnClickListener {
            HideKeyboardOnClick.hideKeyboardOnClick(it)
        }

        addTextWatcher(binding.projectTitleEditText, binding.projectTitleTextLayout)
        addTextWatcher(binding.projectDescriptionEditText, binding.projectDescriptionTextLayout)
        addTextWatcher(binding.projectMilestoneEditText, binding.projectMilestoneTextLayout)
        addTextWatcher(binding.projectFundGoalEditText, binding.projectFundGoalTextLayout)
        addTextWatcher(binding.projectDueDateEditText, binding.projectDueDateTextLayout)
    }

    private fun addTextWatcher(editText: TextInputEditText, layout: TextInputLayout) {
        // Ensure that the space for the error message is always reserved
        layout.isErrorEnabled = true

        editText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                // Clear the error message without affecting the layout spacing
                if (s.isNotEmpty()) {
                    layout.error = null
                }
            }
            override fun afterTextChanged(s: Editable) {}
        })
    }

    private fun showDatePicker() {
        val dateSetListener =
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, monthOfYear)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                updateDueDateEditText()
            }

        DatePickerDialog(
            this@ProposeProjectActivity,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun updateDueDateEditText() {
        val date = dateFormat.format(calendar.time)
        binding.projectDueDateEditText.setText(date)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            val uri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)

            // Add the bitmap to your list of images and notify the adapter
            imageList.add(bitmap)
            adapter.notifyDataSetChanged()
        }
    }

    private fun uploadImages(projectId: String) {
        val db = Firebase.firestore
        val imageUrls = mutableListOf<String>()

        for ((index, bitmap) in imageList.withIndex()) {
            val imageRef = storageRef.child("projects/$projectId/images/image_$index.jpg")

            val baos = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val data = baos.toByteArray()

            val uploadTask = imageRef.putBytes(data)
            uploadTask.addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    imageUrls.add(uri.toString())

                    if (imageUrls.size == imageList.size) {
                        val timestamp = Timestamp.now()
                        db.collection("projects").document(projectId)
                            .update(
                                mapOf(
                                    "imageUrls" to imageUrls,
                                    "timestamp" to timestamp
                                )
                            )
                            .addOnSuccessListener {

                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    this,
                                    "Error updating project: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }.addOnFailureListener { e ->
                Toast.makeText(this, "Error uploading image: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}
