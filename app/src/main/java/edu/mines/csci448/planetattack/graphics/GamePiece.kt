package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Canvas
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

/**
 * This class contains the necessary information for the game to draw a piece.
 * @constructor Creates a piece at its starting position and orientation.
 * @property x the x coordinate of the first block in the piece
 * @property y the y coordinate of the first block in the piece
 * @property shape an instance of a subclass of [PieceShape]
 * @property direction the direction in which the piece initially falls
 */
class GamePiece(
	var x: Int, var y: Int, val shape: PieceShape,
	var direction: PieceDirection, resources: Resources
) {
	/**
	 * A length 4 list of the blocks contained by the piece.
	 * A null entry indicates that the block in that position has been cleared.
	 */
	val blocks: MutableList<BlockDrawable?>

	init {
		val color = BlockColor.values().random()
		blocks = MutableList(4) { BlockDrawable(color, resources, this) }
		shape.make(this)
	}

	companion object {
		/**
		 * The size of each block in terms of coordinate units.
		 */
		const val blockSize = 50

		/**
		 * A bi-directional map between blocks and coordinate pairs.
		 * This requires that no two blocks occupy the same space.
		 */
		val occupiedSpaces: BiMap<BlockDrawable, Pair<Int, Int>> = HashBiMap.create()
	}

	/**
	 * Draws the blocks contained by the piece on the screen.
	 * @param canvas the canvas on which to draw
	 */
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
	 * @param x the x coordinate to check
	 * @param y the y coordinate to check
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
