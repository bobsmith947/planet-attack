package edu.mines.csci448.planetattack.graphics

import androidx.annotation.DrawableRes
import edu.mines.csci448.planetattack.R
import kotlin.jvm.Throws

sealed class PieceShape(@DrawableRes val iconId: Int) {
	// the width and height of the shape in terms of block units
	val width get() = layout[0].size
	val height get() = layout.size

	// the layout of blocks in the piece
	// true represents the presence of the block
	// false represents no block
	protected lateinit var layout: Array<BooleanArray>
	var rotation = ROTATION_360

	@Throws(IllegalArgumentException::class)
	abstract fun createLayout(rotation: Int)

	// TODO check for piece collisions on rotation
	fun make(piece: GamePiece): Boolean {
		var blockNum = 0
		for (i in 0 until height) {
			for (j in 0 until width) {
				if (layout[i][j])
					piece.blocks[blockNum++].setBounds(piece.x + GamePiece.blockSize * j,
						piece.y + GamePiece.blockSize * i)
			}
		}
		return true
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
		this.rotation = rotation
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
		this.rotation = rotation
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
		this.rotation = rotation
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
		this.rotation = rotation
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
		this.rotation = rotation
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
		this.rotation = rotation
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
		this.rotation = rotation
		layout = when (rotation) {
			ROTATION_0 -> r0
			ROTATION_90 -> r90
			ROTATION_180 -> r180
			ROTATION_270 -> r270
			else -> throw IllegalArgumentException()
		}
	}
}
