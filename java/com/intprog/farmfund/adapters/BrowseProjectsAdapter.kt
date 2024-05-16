package com.intprog.farmfund.adapters

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.FarmerVerificationActivity
import com.intprog.farmfund.activities.ProjectDetailsActivity
import com.intprog.farmfund.activities.ProposeProjectActivity
import com.intprog.farmfund.dataclasses.Project

class BrowseProjectsAdapter(
    private val context: Context
) : RecyclerView.Adapter<BrowseProjectsAdapter.ProjectViewHolder>() {

    // Your data list
    var projects: List<Project> = ArrayList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_projects, parent, false)
        return ProjectViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProjectViewHolder, position: Int) {
        holder.bind(projects[position])
    }

    override fun getItemCount(): Int {
        return projects.size
    }

    inner class ProjectViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val projectTitle: TextView = itemView.findViewById(R.id.projectTitle)
        private val projectImage: ImageView = itemView.findViewById(R.id.projectImage1)
        private val seeMoreText : TextView = itemView.findViewById(R.id.seeMoreText)
        private val projectItemCard : RelativeLayout = itemView.findViewById(R.id.projectItemCard)

        fun bind(project: Project) {
            projectTitle.text = project.projTitle

            if (project.imageUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(project.imageUrls[0])
                    .into(projectImage)
            }

            projectItemCard.setOnClickListener {
                val intent = Intent(itemView.context, ProjectDetailsActivity::class.java)
                intent.putExtra("project", project)
                itemView.context.startActivity(intent)
            }

            seeMoreText.setOnClickListener {
                //Code to add it to favorites
            }
        }
    }
}