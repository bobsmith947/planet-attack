package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Canvas
import android.os.Parcel
import android.os.Parcelable
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import edu.mines.csci448.planetattack.ShapeParceler
import edu.mines.csci448.planetattack.ShapeParceler.write
import kotlinx.parcelize.*

/**
 * This class contains the necessary information for the game to draw a piece.
 * @constructor Creates a piece at its starting position and orientation.
 * @property x the x coordinate of the first block in the piece
 * @property y the y coordinate of the first block in the piece
 * @property shape an instance of a subclass of [PieceShape]
 * @property direction the direction in which the piece falls
 */
@Parcelize
class GamePiece(var x: Int, var y: Int, val shape: PieceShape, var direction: PieceDirection) : Parcelable {
	/**
	 * A length 4 list of the blocks contained by the piece.
	 * A null entry indicates that the block in that position has been cleared.
	 */
	@IgnoredOnParcel
	val blocks: MutableList<BlockDrawable?>

	init {
		val color = BlockColor.values().random()
		blocks = MutableList(4) { BlockDrawable(color, resources, this) }
		shape.make(this)
	}

	companion object : Parceler<GamePiece> {
		/**
		 * The size of each block in terms of coordinate units.
		 */
		const val blockSize = 50

		/**
		 * A bi-directional map between blocks and coordinate pairs.
		 * This requires that no two blocks occupy the same space.
		 */
		val occupiedSpaces: BiMap<BlockDrawable, Pair<Int, Int>> = HashBiMap.create()

		override fun create(parcel: Parcel): GamePiece {
			val piece = GamePiece(parcel.readInt(), parcel.readInt(),
				ShapeParceler.create(parcel), parcel.readSerializable() as PieceDirection)
			val blocks = BooleanArray(4)
			parcel.readBooleanArray(blocks)
			for (i in 0 until 4) {
				if (!blocks[i]) {
					piece.blocks[i] = null
				} else {
					piece.blocks[i]!!.setBounds(parcel.readInt(), parcel.readInt())
				}
			}
			return piece
		}

		override fun GamePiece.write(parcel: Parcel, flags: Int) {
			parcel.writeInt(x); parcel.writeInt(y)
			shape.write(parcel, flags); parcel.writeSerializable(direction)
			val blocks = BooleanArray(4) { this.blocks[it] != null }
			parcel.writeBooleanArray(blocks)
			for (i in 0 until 4) {
				if (blocks[i]) {
					parcel.writeInt(this.blocks[i]!!.x)
					parcel.writeInt(this.blocks[i]!!.y)
				}
			}
		}

		lateinit var resources: Resources
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
