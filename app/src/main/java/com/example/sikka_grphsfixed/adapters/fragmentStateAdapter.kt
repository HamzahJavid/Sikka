package com.example.sikka_grphsfixed.adapters
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.sikka_grphsfixed.fragments.AnalysisFragment
import com.example.sikka_grphsfixed.fragments.ExportRecordFragment
import com.example.sikka_grphsfixed.fragments.HomeFragment
import com.example.sikka_grphsfixed.fragments.Records

class MyFragmentAdapter(
    fragmentActivity: FragmentActivity,
    val stringExtra: String?,
) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount() = 3

    override fun createFragment(position: Int): Fragment {
        val fragment: Fragment = when (position) {
            0 -> HomeFragment()
            1 -> AnalysisFragment()
            2 -> ExportRecordFragment()
            else -> Records()
        }

        // Pass UID using bundle



        val bundle = Bundle()
        bundle.putString("UserID",stringExtra )
        fragment.arguments = bundle

        return fragment
    }
}
