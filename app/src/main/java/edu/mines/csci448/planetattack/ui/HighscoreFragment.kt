package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.databinding.FragmentScoreListBinding
import edu.mines.csci448.planetattack.ui.viewmodel.GameViewModel
import edu.mines.csci448.planetattack.ui.viewmodel.GameViewModelFactory

class HighscoreFragment : Fragment() {
	private lateinit var gameViewModel: GameViewModel
	private lateinit var adapter: HighscoreAdapter

	private var _binding: FragmentScoreListBinding? = null
	val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val factory = GameViewModelFactory(requireContext())
		gameViewModel = ViewModelProvider(this, factory)
			.get(GameViewModel::class.java)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentScoreListBinding.inflate(inflater, container, false)
		updateUI(emptyList())

		binding.list.layoutManager = LinearLayoutManager(context)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		gameViewModel.highscoreListLiveData.observe(
			viewLifecycleOwner, { highscores ->
				highscores?.let {
					updateUI(highscores)
				}
			}
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}

	private fun updateUI(highscores: List<Highscore>) {
		adapter = HighscoreAdapter(highscores)
		binding.list.adapter = adapter
	}
}
