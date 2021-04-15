package edu.mines.csci448.planetattack.ui.viewmodel

import androidx.lifecycle.ViewModel
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository

class GameViewModel(private val highscoreRepository: HighscoreRepository): ViewModel() {
	val highscoreListLiveData = highscoreRepository.getHighscores()
	val topScoreLiveData = highscoreRepository.getTopScore()

	fun addHighscore(highscore: Highscore) {
		highscoreRepository.addHighscore(highscore)
	}
}
