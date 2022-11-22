package com.sscl.blelibraryforkotlin.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.sscl.blelibraryforkotlin.ui.fragments.MultipleDeviceConnectFragment

class MultipleDeviceConnectFragmentPager2Adapter(
    fragmentActivity: FragmentActivity,
    private val fragments: ArrayList<MultipleDeviceConnectFragment>
) :
    FragmentStateAdapter(fragmentActivity) {

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    override fun getItemCount(): Int {
        return fragments.size
    }

    override fun createFragment(position: Int): Fragment {
        return fragments[position]
    }

}