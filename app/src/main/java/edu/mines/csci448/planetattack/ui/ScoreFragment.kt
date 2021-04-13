package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.databinding.FragmentScoreListBinding
import edu.mines.csci448.planetattack.ui.dummy.DummyContent

/**
 * A fragment representing a list of Items.
 */
class ScoreFragment : Fragment() {

	private lateinit var highscoreListViewModel: HighscoreListViewModel
	private lateinit var adapter: ScoreAdapter

	private var _binding: FragmentScoreListBinding? = null
	val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		val factory = HighscoreListViewModelFactory(requireContext())
		highscoreListViewModel = ViewModelProvider(this@ScoreFragment, factory)
			.get(HighscoreListViewModel::class.java)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		_binding = FragmentScoreListBinding.inflate(inflater, container, false)
		updateUI(emptyList())

		binding.list.layoutManager = LinearLayoutManager(context)

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		highscoreListViewModel.highscoreListLiveData.observe(
			viewLifecycleOwner,
			Observer { highscores ->
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
		adapter = ScoreAdapter(highscores)
		binding.list.adapter = adapter
	}
}
