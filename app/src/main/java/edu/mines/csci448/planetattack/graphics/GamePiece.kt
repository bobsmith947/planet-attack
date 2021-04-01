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
	}

	companion object {
		const val blockSize = 50
		val occupiedSpaces: BiMap<BlockDrawable, Pair<Int, Int>> = HashBiMap.create()

		// determines whether the coordinates are available to move to
		fun pieceContains(piece: GamePiece, x: Int, y: Int): Boolean {
			val block = occupiedSpaces.inverse()[x to y]
			if (block != null) {
				// check if the block corresponds to the same piece as the given one
				// this is true in the case of a piece that overlaps itself when translated
				// otherwise, the coordinates belong to another piece
				return block.piece === piece
			}
			// the coordinates are empty
			return true
		}
	}

	fun drawBlocks(canvas: Canvas) {
		blocks.forEach {
			if (it != null) {
				it.draw(canvas)
				occupiedSpaces.forcePut(it, it.x to it.y)
			}
		}
	}
}
