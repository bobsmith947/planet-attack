package edu.mines.csci448.planetattack.ui

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.ViewGroup
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.databinding.FragmentScoreBinding

import edu.mines.csci448.planetattack.ui.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class ScoreAdapter(private val highscores: List<Highscore>) : RecyclerView.Adapter<ScoreAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = FragmentScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val highscore = highscores[position]
		holder.bind(highscore, position)
	}

	override fun getItemCount(): Int = highscores.size

	inner class ViewHolder(val binding: FragmentScoreBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(highscore: Highscore, position: Int) {
			binding.placeTextView.text = "#${position + 1}"
			binding.scoreTextView.text = "${highscore.score}"
		}

		override fun toString() = "${super.toString()} '${binding.scoreTextView.text}'"
	}
}
