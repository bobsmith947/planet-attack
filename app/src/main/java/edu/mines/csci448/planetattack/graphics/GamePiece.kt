package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Canvas
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

class GamePiece(
	var x: Int, var y: Int, val shape: PieceShape,
	var direction: PieceDirection, resources: Resources
) {
	internal val blocks: List<BlockDrawable>

	init {
		val color = BlockColor.values().random()
		blocks = List(4) { BlockDrawable(color, resources, this) }
	}

	companion object {
		const val blockSize = 50
		val occupiedSpaces: BiMap<BlockDrawable, Pair<Int, Int>> = HashBiMap.create()
	}

	fun drawBlocks(canvas: Canvas) {
		blocks.forEach {
			it.draw(canvas)
			occupiedSpaces.forcePut(it, it.x to it.y)
		}
	}
}
