package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding

/**
 * A simple [Fragment] subclass.
 * Use the [GameFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class GameFragment : Fragment(), BackPressListener {
	private var _binding: FragmentGameBinding? = null
	val binding get() = _binding!!

	private var isPaused = false

	override fun onCreateView(
		inflater: LayoutInflater, container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()
		resume()

		(requireActivity() as AppCompatActivity).supportActionBar?.hide()

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
		(requireActivity() as AppCompatActivity).supportActionBar?.show()
	}

	override fun onBackPressed() {
		pause()
	}

	private fun pause() {
		binding.menuOverlay.visibility = View.VISIBLE
		binding.resumeButton.visibility = View.VISIBLE
		binding.quitButton.visibility = View.VISIBLE
	}

	private fun resume() {
		binding.menuOverlay.visibility = View.INVISIBLE
		binding.resumeButton.visibility = View.INVISIBLE
		binding.quitButton.visibility = View.INVISIBLE
	}

	private fun setButtonOnClickListeners() {
		binding.resumeButton.setOnClickListener {
			resume()
		}

		binding.menuButton.setOnClickListener {
			pause()
		}

		binding.quitButton.setOnClickListener {
			findNavController().navigateUp()
		}
	}
}
