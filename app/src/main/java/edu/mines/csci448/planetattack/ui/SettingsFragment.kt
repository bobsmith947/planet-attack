package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository
import edu.mines.csci448.planetattack.ui.preferences.GameSize
import edu.mines.csci448.planetattack.ui.preferences.GameSpeed
import edu.mines.csci448.planetattack.ui.preferences.SwipeSensitivity

class SettingsFragment : PreferenceFragmentCompat() {

	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
		setPreferencesFromResource(R.xml.root_preferences, rootKey)
		preferenceManager.findPreference<Preference>(getString(R.string.reset_key))
			?.setOnPreferenceClickListener {
				HighscoreRepository.getInstance(requireContext())
					.deleteHighScores()
				Toast.makeText(requireContext(), R.string.reset_data_toast, Toast.LENGTH_SHORT)
					.show()
				true
			}
	}

	companion object {
		fun getSpeedPreference(speed: String?) = when (speed) {
			"1" -> GameSpeed.SLOW
			"2" -> GameSpeed.MEDIUM
			"3" -> GameSpeed.FAST
			else -> throw IllegalStateException()
		}

		fun getSizePreference(size: String?) = when (size) {
			"1" -> GameSize.SMALL
			"2" -> GameSize.MEDIUM
			"3" -> GameSize.LARGE
			"4" -> GameSize.HUGE
			else -> throw IllegalStateException()
		}

		fun getSensitivityPreference(sensitivity: Int) = when (sensitivity) {
			1 -> SwipeSensitivity.ULTRA_LOW
			2 -> SwipeSensitivity.VERY_LOW
			3 -> SwipeSensitivity.LOW
			4 -> SwipeSensitivity.MEDIUM
			5 -> SwipeSensitivity.HIGH
			6 -> SwipeSensitivity.VERY_HIGH
			7 -> SwipeSensitivity.ULTRA_HIGH
			else -> throw IllegalStateException()
		}
	}
}
