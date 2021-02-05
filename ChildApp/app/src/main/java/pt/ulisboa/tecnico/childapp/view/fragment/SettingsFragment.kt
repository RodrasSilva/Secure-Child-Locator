package pt.ulisboa.tecnico.childapp.view.fragment

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import pt.ulisboa.tecnico.childapp.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}