package edu.mines.csci448.planetattack.ui.preferences

enum class SwipeSensitivity(val multiplier: Double) {
	ULTRA_LOW(0.125),
	VERY_LOW(0.25),
	LOW(0.5),
	MEDIUM(1.0),
	HIGH(2.0),
	VERY_HIGH(4.0),
	ULTRA_HIGH(8.0)
}
