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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.ProjectImagesPagerAdapter
import com.intprog.farmfund.databinding.ActivityProjectDetailsBinding
import com.intprog.farmfund.dataclasses.Project

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        binding.backButton.setOnClickListener {
            finish()
        }

        val project = intent.getSerializableExtra("project") as? Project
        val projId = project?.projId

        if (projId == null) {
            // Handle the error gracefully if the projId is null
            Toast.makeText(this, "Project ID not found", Toast.LENGTH_SHORT).show()
            finish() // Close the activity
            return
        }

        binding.swipeRefreshLayout.isRefreshing = true
        fetchProjectDetails(projId)

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchProjectDetails(projId)
        }

        //Code to check if a user is currently logged in
        val user = auth.currentUser

        if (user == null) {
            binding.projdetailsDynamicBTN.setOnClickListener {
                val loginNoticeDialog = LayoutInflater.from(this).inflate(R.layout.dialog_login_notice, null)
                val builder = AlertDialog.Builder(this).setView(loginNoticeDialog)
                val alertDialog = builder.show()

                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val gotoLoginButton = loginNoticeDialog.findViewById<Button>(R.id.gotoLoginButton)
                gotoLoginButton.setOnClickListener {
                    val intent = Intent(this, HolderLoginRegisterActivity::class.java)
                    intent.putExtra("dialogToShow", "login")
                    startActivity(intent)
                    finish()
                }
            }
        } else {
            val userId = user.uid
            //Check whether the logged in user owns the project
            val userOwns = project?.userId == userId
            //If the user owns the project, adjust the code if necessary
            if (userOwns) {
                binding.updateProjBTN.visibility = View.VISIBLE
                binding.spaceBetween.visibility = View.VISIBLE
                binding.projdetailsDynamicBTN.text = "Withdraw"

                binding.projdetailsDynamicBTN.setOnClickListener {
                    val intent = Intent(this, WithdrawFundsActivity::class.java)
                    intent.putExtra("project", project)
                    startActivity(intent)
                }
            } else {
                //User does not own the pr oject
                binding.projdetailsDynamicBTN.setOnClickListener {
                    val intent = Intent(this, DonateToProjectActivity::class.java)
                    intent.putExtra("project", project)
                    intent.putExtra("imageUrl", project?.imageUrls?.firstOrNull())
                    startActivity(intent)
                }
            }
        }
    }
    fun fetchProjectDetails(projId:String) {
        db.collection("projects").document(projId)
            .get()
            .addOnSuccessListener { document ->
                val projectNew = document.toObject(Project::class.java)
                projectNew?.let {
                    // Use the project object to populate your views

                    binding.projectTitle.text = it.projTitle
                    binding.projectDescription.text = it.projDescription
                    binding.projectMilestone.text = it.projMilestone
                    binding.donorNumber.text = "${it.projDonorsCount} donated"
                    // Calculate days left
                    val totalDays = ((it.projDueDate?.time?.minus(System.currentTimeMillis()))?.div((1000 * 60 * 60 * 24)))?.toInt()
                    binding.daysLeft.text = if ((totalDays ?: 0) > 0) "$totalDays Days Left" else "0 Days Left"

                    val totalProgress = it.projFundsReceived / it.projFundGoal
                    binding.progressNumber.text = "${(totalProgress * 100).toInt()}%"
                    binding.progressBar.progress = (totalProgress * 100).toInt()
                    binding.projectDonations.text = String.format("₱%.2f", it.projFundsReceived)

                    // Setup ViewPager with images
                    binding.projectImagesPager.adapter = ProjectImagesPagerAdapter(this, it.imageUrls)
                }
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                Log.w(TAG, "Error getting documents: ", exception)
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }
}