package edu.mines.csci448.planetattack.ui.highscores

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository

class HighscoreListViewModelFactory(private val context: Context): ViewModelProvider.Factory {
	override fun <T : ViewModel?> create(modelClass: Class<T>): T {
		return modelClass.getConstructor(HighscoreRepository::class.java)
			.newInstance(HighscoreRepository.getInstance(context))
	}
}