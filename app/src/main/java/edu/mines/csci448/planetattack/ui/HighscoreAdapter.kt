package edu.mines.csci448.planetattack.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.databinding.FragmentScoreBinding

class HighscoreAdapter(private val highscores: List<Highscore>) : RecyclerView.Adapter<HighscoreAdapter.ViewHolder>() {

	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
		val binding = FragmentScoreBinding.inflate(LayoutInflater.from(parent.context), parent, false)
		return ViewHolder(binding)
	}

	override fun onBindViewHolder(holder: ViewHolder, position: Int) {
		val highscore = highscores[position]
		holder.bind(highscore, position)
	}

	override fun getItemCount() = highscores.size

	inner class ViewHolder(val binding: FragmentScoreBinding) : RecyclerView.ViewHolder(binding.root) {

		fun bind(highscore: Highscore, position: Int) {
			val positionString = "#${position + 1}"
			binding.placeTextView.text = positionString
			binding.scoreTextView.text = highscore.toString()
		}

		override fun toString() = "${super.toString()} '${binding.scoreTextView.text}'"
	}
}
