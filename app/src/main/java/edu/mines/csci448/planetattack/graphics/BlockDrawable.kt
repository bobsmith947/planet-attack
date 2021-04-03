package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable
import edu.mines.csci448.planetattack.graphics.GamePiece.Companion.blockSize

/**
 * This class is the component which makes up a piece.
 * @constructor Creates a block with the specified color.
 * @property piece the piece that this block belongs to
 */
class BlockDrawable(color: BlockColor, resources: Resources, val piece: GamePiece?) :
	BitmapDrawable(resources, color.getBitmap(resources)) {
	/**
	 * The x coordinate of the block.
	 */
	var x = 0
		private set

	/**
	 * The y coordinate of the block.
	 */
	var y = 0
		private set

	/**
	 * Sets the bounds of the block to a square starting in the top-left corner.
	 * The dimension of the square is [GamePiece.blockSize].
	 * This also updates [x] and [y] to match the bounds.
	 * @param x the top-left x position
	 * @param y the top-left y position
	 */
	fun setBounds(x: Int, y: Int) {
		super.setBounds(x, y, x + blockSize, y + blockSize)
		this.x = x
		this.y = y
	}

	operator fun component1(): Int {
		return x
	}

	operator fun component2(): Int {
		return y
	}

	fun moveUp() {
		setBounds(x, y - blockSize)
	}

	fun moveDown() {
		setBounds(x, y + blockSize)
	}

	fun moveLeft() {
		setBounds(x - blockSize, y)
	}

	fun moveRight() {
		setBounds(x + blockSize, y)
	}
}
