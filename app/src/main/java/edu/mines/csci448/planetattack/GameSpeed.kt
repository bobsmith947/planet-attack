package edu.mines.csci448.planetattack

enum class GameSpeed(val dropDelayMillis: Long) {
	SLOW(5000),
	MEDIUM(3000),
	FAST(1000);
}
