package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Canvas
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

class GamePiece(
	var x: Int, var y: Int, val shape: PieceShape,
	var direction: PieceDirection, resources: Resources
) {
	val blocks: MutableList<BlockDrawable?>

	init {
		val color = BlockColor.values().random()
		blocks = MutableList(4) { BlockDrawable(color, resources, this) }
		shape.make(this)
	}

	companion object {
		const val blockSize = 50
		val occupiedSpaces: BiMap<BlockDrawable, Pair<Int, Int>> = HashBiMap.create()
	}

	fun drawBlocks(canvas: Canvas) {
		blocks.forEach {
			if (it != null) {
				it.draw(canvas)
				occupiedSpaces.forcePut(it, it.x to it.y)
			}
		}
	}

	/**
	 * Check if the block at the given coordinates corresponds to this piece (or no piece).
	 * @return whether the coordinates are available to move to.
 	 */
	fun contains(x: Int, y: Int): Boolean {
		val block = occupiedSpaces.inverse()[x to y]
		if (block != null) {
			// this is true in the case of a piece that overlaps itself when translated
			// otherwise, the coordinates belong to another piece
			return block.piece === this
		}
		// the coordinates are empty
		return true
	}
}
