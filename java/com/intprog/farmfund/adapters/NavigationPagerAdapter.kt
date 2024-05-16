package com.intprog.farmfund.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.intprog.farmfund.fragments.BrowseProjectsFragment
import com.intprog.farmfund.fragments.FavoriteProjectsFragment
import com.intprog.farmfund.fragments.ProfilePageFragment
import com.intprog.farmfund.fragments.VouchersCenterFragment

class NavigationPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {

    private val fragments = listOf(
        BrowseProjectsFragment(),
        FavoriteProjectsFragment(),
        VouchersCenterFragment(),
        ProfilePageFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}
