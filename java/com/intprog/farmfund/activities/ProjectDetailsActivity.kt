package com.intprog.farmfund.activities

import android.app.AlertDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.Toast
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.ProjectImagesPagerAdapter
import com.intprog.farmfund.databinding.ActivityProjectDetailsBinding
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.dataclasses.User
import java.util.Date

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var projId: String
    private lateinit var project: Project

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        binding.backButton.setOnClickListener { finish() }

        projId = intent.getStringExtra("projId") ?: ""

        if (projId.isEmpty()) {
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        binding.swipeRefreshLayout.isRefreshing = true
        fetchProjectDetails(projId)

        binding.swipeRefreshLayout.setOnRefreshListener { fetchProjectDetails(projId) }
    }

    private fun fetchProjectDetails(projId: String) {
        db.collection("projects").document(projId)
            .get()
            .addOnSuccessListener { document ->
                project = document.toObject(Project::class.java) ?: return@addOnSuccessListener
                fetchUserDetails(project.userId)
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun fetchUserDetails(userId: String) {
        db.collection("users").document(userId)
            .get()
            .addOnSuccessListener { document ->
                val user = document.toObject(User::class.java) ?: return@addOnSuccessListener
                val userImageURL = document.getString("userImageURL")
                updateUI(user, userImageURL)
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }


    private fun updateUI(user: User, userImageURL: String?) {
        binding.apply {
            projectTitle.text = project.projTitle
            projectDescription.text = project.projDescription
            projectMilestone.text = project.projMilestone
            donorNumber.text = "${project.projDonorsCount} donated"
            daysLeft.text = calculateDaysLeft(project.projDueDate)
            progressNumber.text = "${calculateProgress(project.projFundsReceived, project.projFundGoal)}%"
            progressBar.progress = calculateProgress(project.projFundsReceived, project.projFundGoal)
            projectDonations.text = String.format("₱%.2f", project.projFundsReceived)
            projectImagesPager.adapter = ProjectImagesPagerAdapter(this@ProjectDetailsActivity, project.imageUrls)

            val midInitial = if (user.midName!!.isNotEmpty()) "${user.midName?.first()}." else ""
            binding.projectAuthorName.text = "${user.firstName} $midInitial ${user.lastName}"

            if (!userImageURL.isNullOrEmpty()) {
                Glide.with(this@ProjectDetailsActivity)
                    .load(userImageURL)
                    .placeholder(R.drawable.img_default_pfp) // Placeholder image while loading
                    .error(R.drawable.img_default_pfp) // Error image if loading fails
                    .into(binding.projectAuthorPfp)
            }
        }
        handleDynamicButton()
    }

    private fun calculateDaysLeft(dueDate: Date?): String {
        val totalDays = ((dueDate?.time?.minus(System.currentTimeMillis()))?.div(1000 * 60 * 60 * 24))?.toInt()
        return if ((totalDays ?: 0) > 0) "$totalDays Days Left" else "0 Days Left"
    }

    private fun calculateProgress(fundsReceived: Double, fundGoal: Double): Int {
        return ((fundsReceived / fundGoal) * 100).toInt()
    }

    private fun handleDynamicButton() {
        val currentDate = Date()
        val user = auth.currentUser
        val isProjectOverdue = currentDate.after(project.projDueDate)
        val isFundGoalReached = project.projFundsReceived >= project.projFundGoal
        val userOwnsProject = project.userId == user?.uid

        binding.apply {
            if (isProjectOverdue || isFundGoalReached) {
                if (isProjectOverdue) {
                    projdetailsDynamicBTN.text = "Overdue"
                    updateProjectStatusToOverdue()
                } else projdetailsDynamicBTN.text = "Completed"
                projdetailsDynamicBTN.setOnClickListener { showProjectEndedDialog() }
            } else {
                if (user == null) {
                    projdetailsDynamicBTN.setOnClickListener { showLoginNoticeDialog() }
                } else {
                    if (userOwnsProject) {
                        updateProjBTN.visibility = View.GONE
                        spaceBetween.visibility = View.GONE
                        projdetailsDynamicBTN.text = "Withdraw"
                        projdetailsDynamicBTN.setOnClickListener { handleWithdrawFunds() }
                    } else {
                        projdetailsDynamicBTN.setOnClickListener { handleDonation() }
                    }
                }
            }
        }
    }

    private fun updateProjectStatusToOverdue() {
        db.collection("projects").document(project.projId)
            .update("projStatus", "Cancelled")
            .addOnSuccessListener {
                Log.d(TAG, "Project status updated to Overdue.")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error updating project status: ", e)
            }
    }

    private fun showProjectEndedDialog() {
        val projectEndedDialog = LayoutInflater.from(this).inflate(R.layout.dialog_project_ended, null)
        val builder = AlertDialog.Builder(this).setView(projectEndedDialog)
        val alertDialog = builder.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val closeDialogBTN = projectEndedDialog.findViewById<Button>(R.id.closeDialogBTN)
        closeDialogBTN.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun showLoginNoticeDialog() {
        val loginNoticeDialog = LayoutInflater.from(this).inflate(R.layout.dialog_login_notice, null)
        val builder = AlertDialog.Builder(this).setView(loginNoticeDialog)
        val alertDialog = builder.show()
        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        loginNoticeDialog.findViewById<Button>(R.id.gotoLoginButton).setOnClickListener {
            val intent = Intent(this, HolderLoginRegisterActivity::class.java).apply {
                putExtra("dialogToShow", "login")
            }
            startActivity(intent)
            finish()
        }
    }

    private fun handleWithdrawFunds() {
        if (project.projStatus == "Withdrawn") {
            Toast.makeText(this, "Donation Funds has already been withdrawn.", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, WithdrawFundsActivity::class.java).apply {
                putExtra("projId", project.projId)
                putExtra("imageUrl", project.imageUrls.firstOrNull())
            }
            startActivity(intent)
        }
    }

    private fun handleDonation() {
        val intent = Intent(this, DonateToProjectActivity::class.java).apply {
            putExtra("projId", project.projId)
            putExtra("imageUrl", project.imageUrls.firstOrNull())
        }
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        fetchProjectDetails(projId)
    }
}