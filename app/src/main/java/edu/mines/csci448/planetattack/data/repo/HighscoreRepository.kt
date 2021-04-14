package edu.mines.csci448.planetattack.data.repo

import android.content.Context
import androidx.lifecycle.LiveData
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.data.db.HighscoreDao
import edu.mines.csci448.planetattack.data.db.HighscoreDatabase
import java.util.*
import java.util.concurrent.Executors

class HighscoreRepository private constructor(private val highscoreDao: HighscoreDao) {
	companion object {
		private var INSTANCE: HighscoreRepository? = null

		fun getInstance(context: Context): HighscoreRepository {
			synchronized(this) {
				var instance = INSTANCE
				if (instance == null) {
					val database = HighscoreDatabase.getInstance(context)
					instance = HighscoreRepository(database.highscoreDao)
					INSTANCE = instance
				}
				return instance
			}
		}
	}

	private val executor = Executors.newSingleThreadExecutor()

	fun getHighscores(): LiveData<List<Highscore>> = highscoreDao.getHighScores()
	fun getHighscore(id: UUID): LiveData<Highscore?> = highscoreDao.getHighScore(id)

	fun deleteHighScores() {
		executor.execute {
			highscoreDao.deleteHighscores()
		}
	}

	fun addHighscore(highscore: Highscore) {
		executor.execute {
			highscoreDao.addHighScore(highscore)
		}
	}
}