package edu.mines.csci448.planetattack.data.db

import androidx.room.TypeConverter
import java.util.*

class HighscoreTypeConverters {
	@TypeConverter
	fun fromDate(date: Date?): Long? {
		return date?.time
	}

	@TypeConverter
	fun toDate(millisSinceEpoch: Long?): Date? {
		return millisSinceEpoch?.let { Date(it) }
	}

	@TypeConverter
	fun toUUID(uuid: String?): UUID? {
		return UUID.fromString(uuid)
	}

	@TypeConverter
	fun fromUUID(uuid: UUID?): String? {
		return uuid?.toString()
	}
}