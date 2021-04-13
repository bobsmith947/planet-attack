package edu.mines.csci448.planetattack.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity
data class Highscore(@PrimaryKey val id: UUID = UUID.randomUUID(),
					 var score: Int,
					 var date: Date = Date())