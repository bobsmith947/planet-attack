package edu.mines.csci448.planetattack.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import edu.mines.csci448.planetattack.data.Highscore
import java.util.*

@Dao
interface HighScoreDao {
	@Query("SELECT * FROM highscore ORDER BY score DESC LIMIT 25")
	fun getHighScores(): LiveData<List<Highscore>>

	@Query("SELECT * FROM highscore WHERE id=(:id)")
	fun getHighScore(id: UUID): LiveData<Highscore?>

	@Query("DELETE FROM highscore")
	fun deleteHighscores()

	@Insert
	fun addHighScore(highScore: Highscore)
}