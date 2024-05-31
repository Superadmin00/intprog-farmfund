package com.intprog.farmfund.adapters

import android.content.Context
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.ProjectDetailsActivity
import com.intprog.farmfund.dataclasses.Project
import java.util.Date
import java.util.Locale

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
        private val projectDate: TextView = itemView.findViewById(R.id.projectDate)
        private val projectImage: ImageView = itemView.findViewById(R.id.projectImage1)
        private val addToFavoritesBTN : ImageView = itemView.findViewById(R.id.addToFavoritesBTN)
        private val daysLeft : TextView = itemView.findViewById(R.id.daysLeft)
        private val projectDonations : TextView = itemView.findViewById(R.id.projectDonations)
        private val projectProgress: ProgressBar = itemView.findViewById(R.id.progressBar)
        private val progressNumber: TextView = itemView.findViewById(R.id.progressNumber)
        private val projectItemCard : RelativeLayout = itemView.findViewById(R.id.projectItemCard)

        fun bind(project: Project) {
            projectTitle.text = project.projTitle

            if (project.imageUrls.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(project.imageUrls[0])
                    .into(projectImage)
            }

            project.timestamp?.let {
                val date = it.toDate()
                val sdf = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())
                val formattedDate = sdf.format(date)
                projectDate.text = formattedDate
            }

            daysLeft.text = calculateDaysLeft(project.projDueDate)
            progressNumber.text = "${calculateProgress(project.projFundsReceived, project.projFundGoal)}%"
            projectProgress.progress = calculateProgress(project.projFundsReceived, project.projFundGoal)
            projectDonations.text = String.format("₱%.2f", project.projFundsReceived)

            projectItemCard.setOnClickListener {
                val intent = Intent(itemView.context, ProjectDetailsActivity::class.java)
                intent.putExtra("projId", project.projId)
                itemView.context.startActivity(intent)
            }

            val auth = FirebaseAuth.getInstance()
            val user = auth.currentUser
            var isFavorite = false

            if (user != null) {
                val userId = user.uid
                val db = FirebaseFirestore.getInstance()

                val favoriteRef = db.collection("users").whereEqualTo("userId", userId)
                favoriteRef.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val userData = documentSnapshot.data
                        val favorites = userData?.get("favorites") as? List<String> ?: emptyList()
                        if (favorites.contains(project.projId)) {
                            isFavorite = true
                            addToFavoritesBTN.setImageResource(R.drawable.ic_heart_filled)
                        } else {
                            isFavorite = false
                            addToFavoritesBTN.setImageResource(R.drawable.ic_heart_unfilled)
                        }
                    }
                }
                    .addOnFailureListener { e ->
                        Log.e("BrowseProjectsAdapter", "Error getting user document: ${e.message}")
                    }

                addToFavoritesBTN.setOnClickListener {
                    isFavorite = !isFavorite
                    if (isFavorite) {
                        addToFavoritesBTN.setImageResource(R.drawable.ic_heart_filled)
                        updateFavorites(userId, project.projId, add = true)
                    } else {
                        addToFavoritesBTN.setImageResource(R.drawable.ic_heart_unfilled)
                        updateFavorites(userId, project.projId, add = false)
                    }
                }
            } else {
                // If user is not logged in, hide the favorite button
                addToFavoritesBTN.visibility = View.GONE
            }
        }
    }
    private fun calculateDaysLeft(dueDate: Date?): String {
        val totalDays = ((dueDate?.time?.minus(System.currentTimeMillis()))?.div(1000 * 60 * 60 * 24))?.toInt()
        return if ((totalDays ?: 0) > 0) "$totalDays Days Left" else "0 Days Left"
    }

    private fun calculateProgress(fundsReceived: Double, fundGoal: Double): Int {
        return ((fundsReceived / fundGoal) * 100).toInt()
    }

    private fun updateFavorites(userId: String, projectId: String, add: Boolean) {
        val db = FirebaseFirestore.getInstance()
        val userRef = db.collection("users").whereEqualTo("userId", userId)
        userRef.get().addOnSuccessListener { querySnapshot ->
            for (document in querySnapshot.documents) {
                val userData = document.data
                val favorites = userData?.get("favorites") as? MutableList<String> ?: mutableListOf()
                if (add) {
                    Log.d("BrowseProjectsAdapter", "UserId: $userId, ProjectId: $projectId")
                    if (!favorites.contains(projectId)) {
                        favorites.add(projectId)
                        document.reference.update("favorites", favorites)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Project Added to Favorites!", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(context, "Failed to add to favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        // Project already in favorites
                        Toast.makeText(context, "Project already in favorites!", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    favorites.remove(projectId)
                    document.reference.update("favorites", favorites)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Project Removed from Favorites!", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Failed to remove from favorites: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        }
    }
}
