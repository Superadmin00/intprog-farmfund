package com.intprog.farmfund.activities

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.intprog.farmfund.adapters.ProjectImagesPagerAdapter
import com.intprog.farmfund.databinding.ActivityProjectDetailsBinding
import com.intprog.farmfund.dataclasses.Project

class ProjectDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProjectDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProjectDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val project = intent.getSerializableExtra("project") as? Project

        project?.let {
            // Use the project object to populate your views

            binding.projectTitle.text = it.projTitle
            binding.projectDescription.text = it.projDescription
            binding.projectMilestone.text = it.projMilestone
            binding.donorNumber.text = "${it.projDonorsCount} donated"
            // Calculate days left
            val totalDays = ((it.projDueDate?.time?.minus(System.currentTimeMillis()))?.div((1000 * 60 * 60 * 24)))?.toInt()
            binding.daysLeft.text = if (totalDays!! > 0) "$totalDays Days Left" else "0 Days Left"

            val totalProgress = it.projFundsReceived / it.projFundGoal
            binding.progressNumber.text = "${(totalProgress * 100).toInt()}%"
            binding.progressBar.progress = (totalProgress * 100).toInt()
            binding.projectDonations.text = String.format("₱%.2f", it.projFundsReceived)

            // Setup ViewPager with images
            binding.projectImagesPager.adapter = ProjectImagesPagerAdapter(this, it.imageUrls)
        }

        val userOwns = false // User owns the project

        if (userOwns) {
            binding.updateProjBTN.visibility = View.VISIBLE
            binding.spaceBetween.visibility = View.VISIBLE
            binding.projdetailsDynamicBTN.text = "Withdraw"

            binding.projdetailsDynamicBTN.setOnClickListener {
                val intent = Intent(this, WithdrawFundsActivity::class.java)
                startActivity(intent)
            }
        } else {
            // User does not own the project
            binding.projdetailsDynamicBTN.setOnClickListener {
                val intent = Intent(this, DonateToProjectActivity::class.java).apply {
                    putExtra("projId", project?.projId)
                    putExtra("project", project)
                    putExtra("projTitle", project?.projTitle)
                    putExtra("projStatus", project?.projStatus)
                    putExtra("imageUrl", project?.imageUrls?.firstOrNull()) // Assuming the first image is the main image
                }
                startActivity(intent)
            }
        }
    }
}