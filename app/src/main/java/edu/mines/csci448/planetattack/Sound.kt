package edu.mines.csci448.planetattack

class Sound(val assetPath: String, var soundID: Int? = null) {
	companion object {
		private const val MP3 = ".mp3"
	}

	val name = assetPath.split("/").last().removeSuffix(MP3)
}