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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.FavoriteProjectsAdapter
import com.intprog.farmfund.dataclasses.Project
import com.intprog.farmfund.adapters.FavoriteUpdateListener

class FavoriteProjectsFragment : Fragment(), FavoriteUpdateListener {
    companion object {
        private var instance: FavoriteProjectsFragment? = null
        fun getInstance(): FavoriteProjectsFragment {
            if (instance == null) {
                instance = FavoriteProjectsFragment()
            }
            return instance!!
        }
    }
    private lateinit var adapter: FavoriteProjectsAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_favorite_projects, container, false)
        auth = FirebaseAuth.getInstance()
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)

        swipeRefreshLayout.isRefreshing = true
        fetchProjects()

        val projectsRecyclerView = view.findViewById<RecyclerView>(R.id.projectsRecyclerView)
        projectsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = FavoriteProjectsAdapter(requireContext(), this)
        projectsRecyclerView.adapter = adapter

        swipeRefreshLayout.setOnRefreshListener {
            fetchProjects()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchProjects()
    }
    override fun onFavoriteUpdated() {
        swipeRefreshLayout.isRefreshing = true
        fetchProjects()
    }
    fun fetchProjects() {
        val user = auth.currentUser

        if (user != null) {
            val userId = user.uid
            val userRef = db.collection("users").whereEqualTo("userId", userId)

            userRef.get().addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val documentSnapshot = querySnapshot.documents[0]
                    val userData = documentSnapshot.data
                    val favorites = userData?.get("favorites") as? List<Long> ?: emptyList()
                    db.collection("projects")
                        .whereIn("projId", favorites)
                        .orderBy("timestamp", Query.Direction.DESCENDING)
                        .get()
                        .addOnSuccessListener { documents ->
                            val projects = mutableListOf<Project>()
                            for (document in documents) {
                                val project = document.toObject(Project::class.java)
                                projects.add(project)
                            }
                            setProjects(projects)
                            Log.d("FavoriteProjectsFragment", "Project favorites: $projects")
                            swipeRefreshLayout.isRefreshing = false
                        }
                        .addOnFailureListener { exception ->
                            swipeRefreshLayout.isRefreshing = false
                        }
                } else {
                    swipeRefreshLayout.isRefreshing = false
                }
            }.addOnFailureListener { exception ->
                swipeRefreshLayout.isRefreshing = false
            }
        } else {
            swipeRefreshLayout.isRefreshing = false
        }
    }
    fun setProjects(projects: List<Project>) {
        adapter.projects = projects
        adapter.notifyDataSetChanged()
    }
}
