package com.intprog.farmfund.activities

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import android.widget.TextView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.MyProjectsAdapter
import com.intprog.farmfund.dataclasses.Project

class MyProjectsActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var projectAdapter: MyProjectsAdapter
    private lateinit var myProjectsRecyclerView: RecyclerView
    private lateinit var noProjectsTextView: TextView
    private lateinit var backButton: ImageButton
    private val projects = mutableListOf<Project>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_my_projects)
        backButton = findViewById(R.id.backButton)

        // Initialize Firebase Auth and Firestore
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
        swipeRefreshLayout.setOnRefreshListener {
            loadUserProjects()
        }

        // Initialize RecyclerView and its adapter
        myProjectsRecyclerView = findViewById(R.id.myProjectsRecyclerView)
        myProjectsRecyclerView.layoutManager = LinearLayoutManager(this)
        projectAdapter = MyProjectsAdapter(projects)
        myProjectsRecyclerView.adapter = projectAdapter

        // Initialize No Projects TextView
        noProjectsTextView = findViewById(R.id.noProjectsText)

        // Load user projects
        loadUserProjects()
        backButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserProjects() {
        val user = auth.currentUser
        user?.let {
            val userId = it.uid
            firestore.collection("projects")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener { documents ->
                    projects.clear()
                    for (document in documents) {
                        val project = document.toObject(Project::class.java)
                        projects.add(project)
                    }
                    projectAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = false
                    noProjectsTextView.visibility = if (projects.isEmpty()) View.VISIBLE else View.GONE
                }
                .addOnFailureListener {
                    // Handle any errors
                    swipeRefreshLayout.isRefreshing = false
                }
        }
    }
}