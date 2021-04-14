package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository

class SettingsFragment : PreferenceFragmentCompat() {

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)

		val button = preferenceManager.findPreference<Preference>(getString(R.string.clear_history_key))
		button?.setOnPreferenceClickListener {
			HighscoreRepository.getInstance(requireContext())
				.deleteHighScores()
			true
		}
	}
}
