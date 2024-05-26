package com.intprog.farmfund.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.WithdrawFundsActivity
import com.intprog.farmfund.dataclasses.Project

class MyProjectsAdapter(private val projects: List<Project>) : RecyclerView.Adapter<MyProjectsAdapter.ProjectViewHolder>() {

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectName)
        val projectStatus: TextView = itemView.findViewById(R.id.projectStatus)
        val projectImage: ImageView = itemView.findViewById(R.id.projectImage)
        val projectItemLayout: RelativeLayout = itemView.findViewById(R.id.projectItemLayout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_my_projects, parent, false)
        return ProjectViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.projTitle
        holder.projectStatus.text = project.projStatus

        // Set the text color based on project status
        val context = holder.itemView.context
        when (project.projStatus) {
            "Ongoing" -> holder.projectStatus.setTextColor(ContextCompat.getColor(context, R.color.activeStatus))
            "Withdrawn" -> holder.projectStatus.setTextColor(ContextCompat.getColor(context, R.color.withdrawnStatus))
            "Finished" -> holder.projectStatus.setTextColor(ContextCompat.getColor(context, R.color.finishedStatus))
            "Cancelled" -> holder.projectStatus.setTextColor(ContextCompat.getColor(context, R.color.red))
        }

        // Check if there are any image URLs and load the first one
        if (project.imageUrls.isNotEmpty()) {
            val imageUrl = project.imageUrls[0]
            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.ic_launcher_background)
                .into(holder.projectImage)
        } else {
            holder.projectImage.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.projectItemLayout.setOnClickListener {
            val intent = Intent(context, WithdrawFundsActivity::class.java)
            intent.putExtra("project", project)
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int {
        return projects.size
    }
}
