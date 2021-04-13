package edu.mines.csci448.planetattack.data.repo

import android.content.Context
import androidx.lifecycle.LiveData
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.data.db.HighScoreDao
import edu.mines.csci448.planetattack.data.db.HighscoreDatabase
import java.util.*
import java.util.concurrent.Executors

class HighscoreRepository private constructor(private val highScoreDao: HighScoreDao) {
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

	fun getHighscores(): LiveData<List<Highscore>> = highScoreDao.getHighScores()
	fun getHighscore(id: UUID): LiveData<Highscore?> = highScoreDao.getHighScore(id)

	fun addHighscore(highscore: Highscore) {
		executor.execute {
			highScoreDao.addHighScore(highscore)
		}
	}
}