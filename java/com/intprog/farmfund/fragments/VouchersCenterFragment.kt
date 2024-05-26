package com.intprog.farmfund.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var voucherAdapter: VoucherAdapter
    private val vouchers = mutableListOf<Voucher>()

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentVouchersCenterBinding.inflate(inflater, container, false)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Show SwipeRefreshLayout and load vouchers/user data
        binding.swipeRefreshLayout.isRefreshing = true
        fetchVouchers()
        loadUserData()

        voucherAdapter = VoucherAdapter(vouchers, this)
        binding.vouchersRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = voucherAdapter
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            fetchVouchers()
            loadUserData()
        }
        return binding.root
    }

    fun fetchVouchers() {
        val db = FirebaseFirestore.getInstance()
        db.collection("vouchers")
            .get()
            .addOnSuccessListener { documents ->
                vouchers.clear() // Clear the list before adding new items
                for (document in documents) {
                    val voucher = document.toObject(Voucher::class.java)
                    vouchers.add(voucher)
                }
                // Sort vouchers by voucherBrand alphabetically and then by voucherPoints
                vouchers.sortWith(compareBy<Voucher> { it.voucherBrand }.thenBy { it.voucherPoints })
                voucherAdapter.notifyDataSetChanged()
                binding.swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { exception ->
                // Show an error message
                Toast.makeText(context, "Failed to fetch vouchers: ${exception.message}", Toast.LENGTH_SHORT).show()
                binding.swipeRefreshLayout.isRefreshing = false
            }
    }


    fun loadUserData() {
        val user = auth.currentUser
        user?.let { currentUser ->
            val userId = currentUser.uid
            val userDocRef = firestore.collection("users").document(userId)
            userDocRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val firstName = document.getString("firstName") ?: "-"
                        val midName = document.getString("midName") ?: "-"
                        val lastName = document.getString("lastName") ?: "-"
                        val userFundPoints = document.getDouble("fundPoints") ?: 0.0
                        val profileImageUrl = document.getString("userImageURL") ?: ""

                        val midInitial = if (midName.isNotEmpty()) "${midName.first()}." else ""

                        binding.username.text = "${firstName} ${midInitial} ${lastName} "
                        binding.fundpoints.text = "%.2f".format(userFundPoints)

                        if (profileImageUrl.isNotEmpty()) {
                            // Load the profile image using Glide
                            Glide.with(this)
                                .load(profileImageUrl)
                                .placeholder(R.drawable.img_default_pfp) // Optional placeholder image
                                .into(binding.imagePlaceholder)
                        }
                    } else {
                        // Show an error message
                        Toast.makeText(context, "User document does not exist", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { exception ->
                    // Show an error message
                    Toast.makeText(context, "Failed to load user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        fetchVouchers()
        loadUserData()
    }
}
