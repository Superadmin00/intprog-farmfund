package com.intprog.farmfund.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.Toast
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.FirebaseAuth
import com.intprog.farmfund.R
import com.intprog.farmfund.adapters.NavigationPagerAdapter
import com.intprog.farmfund.databinding.ActivityNavigatorBinding
import com.intprog.farmfund.fragments.BrowseProjectsFragment
import com.intprog.farmfund.fragments.FavoriteProjectsFragment
import com.intprog.farmfund.fragments.ProfilePageFragment
import com.intprog.farmfund.fragments.VouchersCenterFragment

class NavigatorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNavigatorBinding
    private lateinit var navigationButtons: List<NavigationButton>
    private var doubleBackToExitPressedOnce = false
    private lateinit var auth: FirebaseAuth

    data class NavigationButton(
        val button: ImageButton,
        val fragmentClass: Class<*>,
        val defaultIcon: Int,
        val selectedIcon: Int
    )

    // Add a list of titles for each fragment
    private val pageTitles =
        listOf("Browse Projects", "Favorite Projects", "Voucher Center", "Profile Page")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNavigatorBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pagerAdapter = NavigationPagerAdapter(this)
        binding.viewPager.adapter = pagerAdapter

        navigationButtons = listOf(
            NavigationButton(
                binding.gotoBrowseProjects,
                BrowseProjectsFragment::class.java,
                R.drawable.ic_home,
                R.drawable.ic_home_selected
            ),
            NavigationButton(
                binding.gotoFavorites,
                FavoriteProjectsFragment::class.java,
                R.drawable.ic_heart,
                R.drawable.ic_heart_selected
            ),
            NavigationButton(
                binding.gotoVouchers,
                VouchersCenterFragment::class.java,
                R.drawable.ic_ticket,
                R.drawable.ic_ticket_selected
            ),
            NavigationButton(
                binding.gotoProfile,
                ProfilePageFragment::class.java,
                R.drawable.ic_person,
                R.drawable.ic_person_selected
            )
        )

        // Set the default fragment
        binding.viewPager.currentItem = 0

        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // Set the title when the page is selected
                binding.pageTitle.text = pageTitles[position]
                updateImageButtons()

                if (binding.viewPager.currentItem == 3) { // Assuming ProfilePageFragment is at index 3
                    binding.navigatorActivityContainer.setBackgroundResource(R.drawable.bg_gradient)
                }
            }
        })

        auth = FirebaseAuth.getInstance()
        val user = auth.currentUser

        //Get the token ID of logged in user
        //if user? is null, this line won't get executed
        user?.getIdToken(true)?.addOnCompleteListener { task -> // addOnCompleteListener will add a listener that will be called when the token refresh operation is complete.
            if (!task.isSuccessful) { //check if token ID refresh operation was successful, if not refreshed, code below ill be executed
                val intent = Intent(this, HolderLoginRegisterActivity::class.java)
                intent.putExtra("dialogToShow", "login")
                Toast.makeText(applicationContext, "Session expired, you are logged out.", Toast.LENGTH_SHORT).show()
                startActivity(intent)
                finish()
            }
        }

        binding.gotoBrowseProjects.setOnClickListener {
            binding.viewPager.currentItem = 0
            binding.pageTitle.text = "Browse Projects"
        }

        binding.gotoFavorites.setOnClickListener {
            binding.viewPager.currentItem = 1
            binding.pageTitle.text = "Favorite Projects"
        }

        binding.gotoVouchers.setOnClickListener {
            binding.viewPager.currentItem = 2
            binding.pageTitle.text = "Voucher Center"
        }

        binding.gotoProfile.setOnClickListener {
            binding.viewPager.currentItem = 3
            binding.pageTitle.text = "Profile Page"
        }
    }

    private fun updateImageButtons() {
        navigationButtons.forEachIndexed { index, navigationButton ->
            if (binding.viewPager.currentItem == index) {
                navigationButton.button.setImageResource(navigationButton.selectedIcon)
            } else {
                navigationButton.button.setImageResource(navigationButton.defaultIcon)
            }
        }
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem != 0) {
            // If the current page is not BrowseProjectsFragment, navigate to it
            binding.viewPager.currentItem = 0
        } else {
            if (doubleBackToExitPressedOnce) {
                // If the back button was already pressed once, exit the app
                super.onBackPressed()
                return
            }
            // Otherwise, set the flag and show a Toast
            this.doubleBackToExitPressedOnce = true
            Toast.makeText(this, "Press again to exit", Toast.LENGTH_SHORT).show()

            // Reset the flag after 2 seconds
            Handler(Looper.getMainLooper()).postDelayed(
                { doubleBackToExitPressedOnce = false },
                3000
            )
        }
    }
}