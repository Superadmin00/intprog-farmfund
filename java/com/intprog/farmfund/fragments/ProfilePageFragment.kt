package com.intprog.farmfund.fragments

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.intprog.farmfund.R
import com.intprog.farmfund.activities.EditProfileActivity
import com.intprog.farmfund.activities.HolderLoginRegisterActivity
import com.intprog.farmfund.activities.SettingsActivity
import com.intprog.farmfund.activities.TransactionHistoryActivity
import com.intprog.farmfund.databinding.FragmentProfilePageBinding
import com.intprog.farmfund.objects.LoadingDialog

class ProfilePageFragment : Fragment() {

    private var _binding: FragmentProfilePageBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var storageReference: StorageReference
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentProfilePageBinding.inflate(inflater, container, false)
        auth = FirebaseAuth.getInstance()
        storageReference = FirebaseStorage.getInstance().reference
        swipeRefreshLayout = binding.swipeRefreshLayout

        loadUserProfile()

        swipeRefreshLayout.setOnRefreshListener {
            loadUserProfile()
        }

        return binding.root
    }

    private fun loadUserProfile() {
        val user = auth.currentUser

        user?.let { currentUser ->
            val usersCollection = FirebaseFirestore.getInstance().collection("users")
            val userId = currentUser.uid
            usersCollection.whereEqualTo("userId", userId).get()
                .addOnSuccessListener { querySnapshot ->
                    for (document in querySnapshot.documents) {
                        val userData = document.data
                        val userName = userData?.get("name") as? String
                        val userEmail = userData?.get("email") as? String
                        val userFundPoints = userData?.get("fundPoints") as? Number
                        val userPhoneNumber = userData?.get("phoneNum") as? String

                        Log.d("FirestoreData", "Name: $userName")
                        Log.d("FirestoreData", "Email: $userEmail")
                        Log.d("FirestoreData", "Fund Points: $userFundPoints")
                        Log.d("FirestoreData", "Phone Number: $userPhoneNumber")

                        binding.profName.text = userName
                        binding.profEmail.text = userEmail
                        binding.profFundPoints.text = userFundPoints?.toDouble()?.toString() ?: "0.00"
                        binding.profNumber.text = userPhoneNumber ?: "(Not set)"
                    }
                    swipeRefreshLayout.isRefreshing = false
                }
                .addOnFailureListener { exception ->
                    swipeRefreshLayout.isRefreshing = false
                    Log.e("FirestoreError", "Error retrieving user data", exception)
                    // Handle any errors
                }
        }
        loadProfileImage()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //If user is not authenticated
        if (FirebaseAuth.getInstance().currentUser == null) {
            binding.editProfileBTN.text = "Login To Edit Profile"

            binding.editProfileBTN.setOnClickListener {
                val intent = Intent(activity, HolderLoginRegisterActivity::class.java)
                intent.putExtra("dialogToShow", "login")
                startActivity(intent)
            }

            binding.gotoMyProjectsBTN.visibility = View.GONE //Hide gotoMyProjectsBTN
            binding.gotoLogoutBTN.visibility = View.GONE //Hide the gotoLogoutBTN
            binding.gotoPayMethodBTN.visibility = View.GONE //Hide the gotoPayMethodBTN

            // Create new LayoutParams with layout_below set
            val layoutParams = RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT
            )

            // Convert 20dp to pixels
            val marginStartEnd = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, 20f, resources.displayMetrics
            ).toInt()

            // Set margins
            layoutParams.setMargins(marginStartEnd, 0, marginStartEnd, 0)

            layoutParams.addRule(RelativeLayout.BELOW, R.id.gotoTransacHistoryBTN)

            // Set the LayoutParams on the button
            binding.gotoSettingsBTN.layoutParams = layoutParams

        } else {
            // If user is authenticated, set the click listener for editProfileBTN to navigate to EditProfileActivity
            binding.editProfileBTN.setOnClickListener {
                val intent = Intent(activity, EditProfileActivity::class.java)
                startActivity(intent)
            }

        }

        binding.gotoFavProjectsBTN.setOnClickListener {
        }

        binding.gotoTransacHistoryBTN.setOnClickListener {
            val intent = Intent(activity, TransactionHistoryActivity::class.java)
            startActivity(intent)
        }

        binding.gotoSettingsBTN.setOnClickListener {
            val intent = Intent(activity, SettingsActivity::class.java)
            startActivity(intent)
        }

        binding.gotoPayMethodBTN.setOnClickListener {
            //Code to go to Payment Methods Activity
        }

        binding.gotoLogoutBTN.setOnClickListener {
            val logoutDialog = LayoutInflater.from(context).inflate(R.layout.dialog_logout, null)
            val builder = AlertDialog.Builder(context).setView(logoutDialog)
            val alertDialog = builder.show()

            alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            val logoutButton = logoutDialog.findViewById<Button>(R.id.logoutButton)
            logoutButton.setOnClickListener {

                //Show Loading dialog
                LoadingDialog.show(requireContext(), false)
                auth.signOut()
                val intent = Intent(activity, HolderLoginRegisterActivity::class.java)
                intent.putExtra("dialogToShow", "login")
                LoadingDialog.dismiss()
                startActivity(intent)
                activity?.finish()
                Toast.makeText(context, "Logout Successful.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadProfileImage() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        userId?.let { uid ->
            val profileImageRef = storageReference.child("profile_images/$uid.jpg")
            profileImageRef.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(requireContext())
                    .load(uri)
                    .placeholder(R.drawable.ic_default_pfp) // Placeholder image while loading
                    .error(R.drawable.ic_default_pfp) // Error image if loading fails
                    .into(binding.imagePlaceholder)
            }.addOnFailureListener {
                // Handle any errors
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
