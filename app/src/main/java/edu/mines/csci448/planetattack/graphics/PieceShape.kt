package edu.mines.csci448.planetattack.graphics

import androidx.annotation.DrawableRes
import edu.mines.csci448.planetattack.R
import kotlin.jvm.Throws

sealed class PieceShape(@DrawableRes val iconId: Int) {
	/**
	 * The width in terms of block units.
	 */
	val width get() = layout[0].size

	/**
	 * The height in terms of block units.
	 */
	val height get() = layout.size

	/**
	 * The layout of the blocks in the piece.
	 * True represents the presence of a block.
	 * False represents an empty space.
 	 */
	protected lateinit var layout: Array<BooleanArray>

	/**
	 * The current rotation of the block in degrees.
	 */
	var currentRotation = ROTATION_360
		protected set(value) {
			previousRotation = field
			field = value
		}

	/**
	 * The previous rotation of the block in degrees.
	 */
	private var previousRotation = ROTATION_360

	@Throws(IllegalArgumentException::class)
	abstract fun createLayout(rotation: Int)

	fun make(piece: GamePiece) {
		var blockNum = 0
		for (i in 0 until height) {
			for (j in 0 until width) {
				if (layout[i][j]) {
					piece.blocks[blockNum++]?.setBounds(
						piece.x + GamePiece.blockSize * j,
						piece.y + GamePiece.blockSize * i
					)
				}
			}
		}
		// check if there is space for the rotated piece
		if (previousRotation != ROTATION_360 && piece.blocks.filterNotNull().any { !piece.contains(it.x, it.y) }) {
			// attempt to perform wall kick
			when (piece.direction) {
				PieceDirection.UP, PieceDirection.DOWN -> {
					if (piece.direction.moveRight(piece)) return
					else if (piece.direction.moveLeft(piece)) return
				}
				PieceDirection.LEFT, PieceDirection.RIGHT -> {
					if (piece.direction.moveDown(piece)) return
					else if (piece.direction.moveUp(piece)) return
				}
			}
			// revert to previous rotation if wall kick fails
			createLayout(previousRotation)
			make(piece)
		}
	}

	companion object {
		const val ROTATION_0 = 0
		const val ROTATION_90 = 90
		const val ROTATION_180 = 180
		const val ROTATION_270 = 270
		const val ROTATION_360 = 360
	}
}

class ShapeI : PieceShape(R.drawable.block_icon_i) {
	private val vertical = arrayOf(
		booleanArrayOf(true),
		booleanArrayOf(true),
		booleanArrayOf(true),
		booleanArrayOf(true)
	)
	private val horizontal = arrayOf(booleanArrayOf(true, true, true, true))

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> vertical
			ROTATION_90 -> horizontal
			ROTATION_180 -> vertical
			ROTATION_270 -> horizontal
			else -> throw IllegalArgumentException()
		}
	}
}

class ShapeJ : PieceShape(R.drawable.block_icon_j) {
	private val r0 = arrayOf(
		booleanArrayOf(false, true),
		booleanArrayOf(false, true),
		booleanArrayOf(true, true)
	)
	private val r90 = arrayOf(
		booleanArrayOf(true, false, false),
		booleanArrayOf(true, true, true)
	)
	private val r180 = arrayOf(
		booleanArrayOf(true, true),
		booleanArrayOf(true, false),
		booleanArrayOf(true, false)
	)
	private val r270 = arrayOf(
		booleanArrayOf(true, true, true),
		booleanArrayOf(false, false, true)
	)

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}

class ShapeL : PieceShape(R.drawable.block_icon_l) {
	private val r0 = arrayOf(
		booleanArrayOf(true, false),
		booleanArrayOf(true, false),
		booleanArrayOf(true, true)
	)
	private val r90 = arrayOf(
		booleanArrayOf(true, true, true),
		booleanArrayOf(true, false, false)
	)
	private val r180 = arrayOf(
		booleanArrayOf(true, true),
		booleanArrayOf(false, true),
		booleanArrayOf(false, true)
	)
	private val r270 = arrayOf(
		booleanArrayOf(false, false, true),
		booleanArrayOf(true, true, true)
	)

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}

class ShapeO : PieceShape(R.drawable.block_icon_o) {
	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = arrayOf(
			booleanArrayOf(true, true),
			booleanArrayOf(true, true)
		)
	}
}

class ShapeS : PieceShape(R.drawable.block_icon_s) {
	private val r0 = arrayOf(
		booleanArrayOf(true, false),
		booleanArrayOf(true, true),
		booleanArrayOf(false, true)
	)
	private val r90 = arrayOf(
		booleanArrayOf(false, true, true),
		booleanArrayOf(true, true, false)
	)
	private val r180 = arrayOf(
		booleanArrayOf(true, false),
		booleanArrayOf(true, true),
		booleanArrayOf(false, true)
	)
	private val r270 = arrayOf(
		booleanArrayOf(false, true, true),
		booleanArrayOf(true, true, false)
	)

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}

class ShapeT : PieceShape(R.drawable.block_icon_t) {
	private val r0 = arrayOf(
		booleanArrayOf(true, false),
		booleanArrayOf(true, true),
		booleanArrayOf(true, false)
	)
	private val r90 = arrayOf(
		booleanArrayOf(true, true, true),
		booleanArrayOf(false, true, false)
	)
	private val r180 = arrayOf(
		booleanArrayOf(false, true),
		booleanArrayOf(true, true),
		booleanArrayOf(false, true)
	)
	private val r270 = arrayOf(
		booleanArrayOf(false, true, false),
		booleanArrayOf(true, true, true)
	)

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}

class ShapeZ : PieceShape(R.drawable.block_icon_z) {
	private val r0 = arrayOf(
		booleanArrayOf(false, true),
		booleanArrayOf(true, true),
		booleanArrayOf(true, false)
	)
	private val r90 = arrayOf(
		booleanArrayOf(true, true, false),
		booleanArrayOf(false, true, true)
	)
	private val r180 = arrayOf(
		booleanArrayOf(false, true),
		booleanArrayOf(true, true),
		booleanArrayOf(true, false)
	)
	private val r270 = arrayOf(
		booleanArrayOf(true, true, false),
		booleanArrayOf(false, true, true)
	)

	override fun createLayout(rotation: Int) {
		this.currentRotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}
