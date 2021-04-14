package edu.mines.csci448.planetattack

enum class GameSpeed(val dropDelayMillis: Long) {
	SLOW(1500),
	MEDIUM(750),
	FAST(200);
}
