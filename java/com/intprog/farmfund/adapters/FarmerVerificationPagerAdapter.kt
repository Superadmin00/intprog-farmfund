package com.intprog.farmfund.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intprog.farmfund.fragments.FarmerVerifFirstFragment
import com.intprog.farmfund.fragments.FarmerVerifSecondFragment
import com.intprog.farmfund.fragments.FarmerVerifThirdFragment

class FarmerVerificationPagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> FarmerVerifFirstFragment()
            1 -> FarmerVerifSecondFragment()
            else -> FarmerVerifThirdFragment()
        }
    }
}