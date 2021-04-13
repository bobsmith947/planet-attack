package edu.mines.csci448.planetattack.ui

import androidx.lifecycle.ViewModel
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository

class HighscoreListViewModel(private val highscoreRepository: HighscoreRepository): ViewModel() {
	val highscoreListLiveData = highscoreRepository.getHighscores()

	fun addHighscore(highscore: Highscore) {
		highscoreRepository.addHighscore(highscore)
	}
}