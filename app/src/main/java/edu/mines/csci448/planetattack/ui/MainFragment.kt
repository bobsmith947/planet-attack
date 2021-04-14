package edu.mines.csci448.planetattack.ui

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.MainFragmentBinding

class MainFragment : Fragment() {
	private var _binding: MainFragmentBinding? = null
	private val binding get() = _binding!!

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = MainFragmentBinding.inflate(inflater, container, false)

		setButtonOnClickListeners()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun setButtonOnClickListeners() {
		binding.playButton.setOnClickListener {
			findNavController().navigate(MainFragmentDirections.actionMainFragmentToGameFragment())
		}

		binding.helpButton.setOnClickListener {
			AlertDialog.Builder(context)
				.setTitle(R.string.help_label)
				.setMessage(R.string.help_text)
				.setNeutralButton(R.string.help_okay, null)
				.show()
		}

		binding.scoresButton.setOnClickListener {
			findNavController().navigate(MainFragmentDirections.actionMainFragmentToScoreFragment())
		}

		binding.settingsButton.setOnClickListener {
			findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
		}
	}
}
