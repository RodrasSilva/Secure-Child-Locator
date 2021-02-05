package pt.ulisboa.tecnico.guardianapp.view.fragment

import android.os.Bundle
import android.widget.Toast
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import pt.ulisboa.tecnico.guardianapp.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val radiusPreference: EditTextPreference? = findPreference("radius")
        radiusPreference?.setOnPreferenceChangeListener { preference, newValue ->
                val v = (newValue as String).toUIntOrNull()
                if (v == null) {
                    Toast.makeText(context, "Only positive numbers are allowed!", Toast.LENGTH_SHORT).show()
                    false
                } else true
        }
    }


}