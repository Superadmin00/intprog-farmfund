package com.intprog.farmfund.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.FarmerVerificationActivity
import com.intprog.farmfund.activities.HolderLoginRegisterActivity
import com.intprog.farmfund.activities.ProposeProjectActivity
import com.intprog.farmfund.adapters.BrowseProjectsAdapter
import com.intprog.farmfund.dataclasses.Project

class BrowseProjectsFragment : Fragment() {
    private lateinit var adapter: BrowseProjectsAdapter
    private val db = FirebaseFirestore.getInstance()
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var auth: FirebaseAuth
    private var projects: MutableList<Project> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_browse_projects, container, false)
        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout)
        val search = view.findViewById<EditText>(R.id.searchBar)

        swipeRefreshLayout.isRefreshing = true
        fetchProjects()

        val projectsRecyclerView = view.findViewById<RecyclerView>(R.id.projectsRecyclerView)
        projectsRecyclerView.layoutManager = LinearLayoutManager(context)
        adapter = BrowseProjectsAdapter(requireContext())
        projectsRecyclerView.adapter = adapter

        val addProjectButton = view.findViewById<ImageButton>(R.id.addProjectButton)

        swipeRefreshLayout.setOnRefreshListener {
            fetchProjects()
        }

        addProjectButton.setOnClickListener {
            if (user == null) {
                val loginDialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_login_notice, null)
                val builder = AlertDialog.Builder(requireContext())
                val alertDialog = builder.setView(loginDialog).show()
                alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                val gotoLoginButton: Button = loginDialog.findViewById(R.id.gotoLoginButton)
                gotoLoginButton.setOnClickListener {
                    val intent = Intent(activity, HolderLoginRegisterActivity::class.java)
                    intent.putExtra("dialogToShow", "login")
                    alertDialog.dismiss()
                    startActivity(intent)
                }
            } else {
                val userId = user.uid
                val firestore = FirebaseFirestore.getInstance()
                val userRef = firestore.collection("users").whereEqualTo("userId", userId)
                userRef.get().addOnSuccessListener { querySnapshot ->
                    if (!querySnapshot.isEmpty) {
                        val documentSnapshot = querySnapshot.documents[0]
                        val userData = documentSnapshot.data
                        val isVerified = userData?.get("verified") as? Boolean ?: false

                        if (isVerified) {
                            val intent = Intent(activity, ProposeProjectActivity::class.java)
                            startActivity(intent)
                        } else {
                            val verificationDialog = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_verification_notice, null)
                            val builder = AlertDialog.Builder(requireContext())
                            val alertDialog = builder.setView(verificationDialog).show()
                            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                            val verificationButton: Button = verificationDialog.findViewById(R.id.gotoVerificationPage)
                            verificationButton.setOnClickListener {
                                alertDialog.dismiss()

                                val intent = Intent(requireContext(), FarmerVerificationActivity::class.java)
                                startActivity(intent)
                            }
                        }
                    }
                }
            }
        }

        search.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                filterProjects(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })
        return view
    }

    override fun onResume() {
        super.onResume()
        fetchProjects()
    }

    private fun fetchProjects() {
        db.collection("projects")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                projects.clear()
                for (document in documents) {
                    val project = document.toObject(Project::class.java)
                    if (project.projStatus == "Ongoing") { // Only add active projects
                        projects.add(project)
                    }
                }
                setProjects(projects)
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                swipeRefreshLayout.isRefreshing = false
                Toast.makeText(context, "Failed to fetch projects: ${exception.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun setProjects(projects: List<Project>) {
        this.projects = projects.toMutableList()
        filterProjects("")
    }

    private fun filterProjects(query: String) {
        val filteredProjects = if (query.isEmpty()) {
            projects
        } else {
            projects.filter { project ->
                project.projTitle.contains(query, true)
            }
        }

        if (filteredProjects.isEmpty()) {
            view?.findViewById<TextView>(R.id.noProjectsText)?.visibility = View.VISIBLE
        } else {
            view?.findViewById<TextView>(R.id.noProjectsText)?.visibility = View.GONE
        }

        adapter.projects = filteredProjects
        adapter.notifyDataSetChanged()
    }
}
