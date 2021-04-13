package edu.mines.csci448.planetattack.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.mines.csci448.planetattack.data.Highscore

@Database(entities = [ Highscore::class ], version = 1)
@TypeConverters(HighscoreTypeConverters::class)
abstract class HighscoreDatabase : RoomDatabase() {
	companion object {
		private const val DATABASE_NAME = "highscore-database"
		private var INSTANCE: HighscoreDatabase? = null

		fun getInstance(context: Context): HighscoreDatabase {
			synchronized(this) {
				var instance = INSTANCE
				if (instance == null) {
					instance = Room.databaseBuilder(context.applicationContext, HighscoreDatabase::class.java, DATABASE_NAME)
						.build()

					INSTANCE = INSTANCE
				}
				return instance
			}
		}
	}

	abstract val highscoreDao: HighScoreDao
}