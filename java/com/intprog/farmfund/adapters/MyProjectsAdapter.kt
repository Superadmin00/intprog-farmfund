package com.intprog.farmfund.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R
import com.intprog.farmfund.dataclasses.Project

class MyProjectsAdapter(private val projects: List<Project>) : RecyclerView.Adapter<MyProjectsAdapter.ProjectViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item_my_projects, parent, false)
        return ProjectViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        val project = projects[position]
        holder.projectName.text = project.projTitle
        holder.projectStatus.text = project.projStatus

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
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val projectName: TextView = itemView.findViewById(R.id.projectName)
        val projectStatus: TextView = itemView.findViewById(R.id.projectStatus)
        val projectImage: ImageView = itemView.findViewById(R.id.projectImage)
    }
}