package edu.mines.csci448.planetattack

import android.content.res.AssetManager
import android.media.SoundPool
import android.util.Log
import java.io.IOException

class SoundManager(private val assets: AssetManager) {
	companion object {
		private const val LOG_TAG = "csci448.planetattack.SoundManager"
		private const val SOUNDS_FOLDER = "sounds"
		private const val MAX_SOUNDS = 5

		private var _shared: SoundManager? = null
		val shared get() = _shared!!

		fun makeShared(assets: AssetManager) {
			_shared = SoundManager(assets)
		}
	}

	private val sounds: List<Sound>
	private val soundPool = SoundPool.Builder()
		.setMaxStreams(MAX_SOUNDS)
		.build()

	val blockPlaceSound get() = sounds.firstOrNull { it.name == "block_place" }
	val ringDestroySound get() = sounds.firstOrNull { it.name == "ring_destroy" }
	val gameOverSound get() = sounds.firstOrNull { it.name == "game_over" }
	val startGameSound get() = sounds.firstOrNull { it.name == "start_game" }

	init {
		sounds = loadSounds()
	}

	private fun loadSounds(): List<Sound> {
		val soundNames: Array<String>

		try {
			soundNames = assets.list(SOUNDS_FOLDER)!!
		} catch (e: Exception) {
			Log.e(LOG_TAG, "Could not list assets", e)
			return emptyList()
		}

		val sounds = mutableListOf<Sound>()
		soundNames.forEach { filename ->
			val assetPath = "$SOUNDS_FOLDER/$filename"
			val sound = Sound(assetPath)
			try {
				load(sound)
				sounds.add(sound)
			} catch (ioe: IOException) {
				Log.e(LOG_TAG, "Could not load sound $filename", ioe)
			}
		}
		return sounds
	}

	fun play(sound: Sound?) {
		sound?.soundID?.let {
			Log.d(LOG_TAG, "Playing sound ${sound.name}")
			soundPool.play(it, 1.0f, 1.0f, 1, 0, 1.0f)
		}
	}

	fun release() {
		soundPool.release()
	}

	private fun load(sound: Sound) {
		val afd = assets.openFd(sound.assetPath)
		val soundID = soundPool.load(afd, 1)
		sound.soundID = soundID
	}
}