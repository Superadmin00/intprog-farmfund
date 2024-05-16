package com.intprog.farmfund.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.NavigatorActivity
import com.intprog.farmfund.adapters.BrowseProjectsAdapter
import com.intprog.farmfund.dataclasses.Project

class BrowseProjectsFragment : Fragment() {
    private lateinit var adapter: BrowseProjectsAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse_projects, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        // This will show the refresh indicator while fetchingProjects()
        swipeRefreshLayout.isRefreshing = true
        fetchProjects()

        val projectsRecyclerView = view.findViewById<RecyclerView>(R.id.projectsRecyclerView)
        projectsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = BrowseProjectsAdapter(requireContext())
        projectsRecyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            fetchProjects()
        }

        return view
    }


    private fun fetchProjects() {
        db.collection("projects")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val projects = mutableListOf<Project>()
                for (document in documents) {
                    val project = document.toObject(Project::class.java)
                    projects.add(project)
                }
                setProjects(projects)
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                swipeRefreshLayout.isRefreshing = false
            }
    }

    // Call this method when you have the projects data
    fun setProjects(projects: List<Project>) {
        adapter.projects = projects
        adapter.notifyDataSetChanged()
        Log.d("BrowseProjectsFragment", "Projects set: $projects")
    }
}
