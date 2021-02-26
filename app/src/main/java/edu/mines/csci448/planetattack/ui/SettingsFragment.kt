package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import edu.mines.csci448.planetattack.R

class SettingsFragment : PreferenceFragmentCompat() {

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
	}
}
