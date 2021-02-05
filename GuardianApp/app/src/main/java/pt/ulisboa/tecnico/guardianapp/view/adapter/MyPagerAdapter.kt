package pt.ulisboa.tecnico.guardianapp.view.adapter

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import pt.ulisboa.tecnico.guardianapp.R
import pt.ulisboa.tecnico.guardianapp.view.fragment.CurrentFragment
import pt.ulisboa.tecnico.guardianapp.view.fragment.HistoryFragment

class MyPagerAdapter(private val context: Context, fragmentManager: FragmentManager) : FragmentStatePagerAdapter(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    private val TAB_TITLES = arrayOf(
        R.string.tab_text_1,
        R.string.tab_text_2
    )

    override fun getItem(position: Int): Fragment {
        return when(position) {
            0 -> CurrentFragment()
            1 -> HistoryFragment()
            else -> Fragment()
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(TAB_TITLES[position])
    }

}