package edu.mines.csci448.planetattack.view

import android.content.res.Resources
import android.graphics.Canvas
import java.util.*

class GamePiece(var x: Int, var y: Int, shape: PieceShape, resources: Resources) {
	private val blocks = LinkedList<BlockDrawable>()

	init {
		// TODO choose color randomly
		val color = BlockColor.BLUE
		when (shape) {
			PieceShape.I -> {
				blocks.apply {
					add(BlockDrawable(x, y, color, resources))
					add(BlockDrawable(x, y + blockSize, color, resources))
					add(BlockDrawable(x, y + blockSize * 2, color, resources))
					add(BlockDrawable(x, y + blockSize * 3, color, resources))
				}
			}
			// TODO implement other shapes
		}
	}

	companion object {
		const val blockSize = 50
	}

	fun drawBlocks(canvas: Canvas) {
		blocks.forEach { it.draw(canvas) }
	}
}
