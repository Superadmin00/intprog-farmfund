package com.intprog.farmfund.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.VoucherAdapter
import com.intprog.farmfund.databinding.FragmentVouchersCenterBinding
import com.intprog.farmfund.dataclasses.Voucher

class VouchersCenterFragment : Fragment() {

    private var _binding: FragmentVouchersCenterBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVouchersCenterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Initialize SwipeRefreshLayout
        swipeRefreshLayout = binding.swipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener {
            // Refresh action
            loadUserData()
        }

        // Load user data from Firestore
        loadUserData()

        val vouchers = listOf(
            Voucher(1, 100, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(2, 250, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(3, 500, R.drawable.ic_globe_logo, "Globe Prepaid Load Credits"),
            Voucher(4, 100, R.drawable.ic_smart, "SMART Prepaid Load Credits"),
            Voucher(5, 250, R.drawable.ic_smart, "SMART Prepaid Load Credits"),
            Voucher(6, 500, R.drawable.ic_smart, "SMART Prepaid Load Credits"),
            Voucher(7, 1000, R.drawable.img_valo, "Valorant Points | Game Credits"),
            Voucher(8, 2500, R.drawable.img_valo, "Valorant Points | Game Credits")
        )
        binding.vouchersRecyclerView.layoutManager = LinearLayoutManager(context)
        binding.vouchersRecyclerView.adapter = VoucherAdapter(vouchers)
    }

    private fun loadUserData() {
        val user = auth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.getString("name") ?: "N/A"
                        val userFundPoints = document.getDouble("fundPoints") ?: 0.0
                        val profileImageUrl = document.getString("userImageURL") ?: ""

                        binding.username.text = userName
                        binding.fundpoints.text = userFundPoints.toString()

                        if (profileImageUrl.isNotEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.ic_default_pfp) // Optional placeholder image
                                .into(binding.imagePlaceholder)
                        }
                    } else {
                        // Handle the case where the document does not exist
                    }
                    // Stop the refreshing animation
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener { exception ->
                    // Handle any errors
                    // Stop the refreshing animation
                    swipeRefreshLayout.isRefreshing = false
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
