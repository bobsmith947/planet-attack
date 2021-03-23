package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Canvas
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap

class GamePiece(var x: Int, var y: Int, val shape: PieceShape,
				resources: Resources, var direction: PieceDirection? = null) {
	internal val blocks: List<BlockDrawable>

	init {
		val color = BlockColor.values().random()
		blocks = List(4) { BlockDrawable(color, resources, this) }
		makeShape()
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

	fun makeShape() {
		when (shape) {
			PieceShape.I -> makeI()
			PieceShape.J -> makeJ()
			PieceShape.L -> makeL()
			PieceShape.O -> makeO()
			PieceShape.S -> makeS()
			PieceShape.T -> makeT()
			PieceShape.Z -> makeZ()
		}
	}

	private fun makeI() {
		blocks[0].setBounds(x, y)
		blocks[1].setBounds(x, y + blockSize)
		blocks[2].setBounds(x, y + blockSize * 2)
		blocks[3].setBounds(x, y + blockSize * 3)
	}

	private fun makeJ() {
		blocks[0].setBounds(x, y + blockSize * 2)
		blocks[1].setBounds(x + blockSize, y)
		blocks[2].setBounds(x + blockSize, y + blockSize)
		blocks[3].setBounds(x + blockSize, y + blockSize * 2)
	}

	private fun makeL() {
		blocks[0].setBounds(x, y)
		blocks[1].setBounds(x, y + blockSize)
		blocks[2].setBounds(x, y + blockSize * 2)
		blocks[3].setBounds(x + blockSize, y + blockSize * 2)
	}

	private fun makeO() {
		blocks[0].setBounds(x, y)
		blocks[1].setBounds(x + blockSize, y)
		blocks[2].setBounds(x, y + blockSize)
		blocks[3].setBounds(x + blockSize, y + blockSize)
	}

	private fun makeS() {
		blocks[0].setBounds(x, y)
		blocks[1].setBounds(x, y + blockSize)
		blocks[2].setBounds(x + blockSize, y + blockSize)
		blocks[3].setBounds(x + blockSize, y + blockSize * 2)
	}

	private fun makeT() {
		blocks[0].setBounds(x, y)
		blocks[1].setBounds(x, y + blockSize)
		blocks[2].setBounds(x, y + blockSize * 2)
		blocks[3].setBounds(x + blockSize, y + blockSize)
	}

	private fun makeZ() {
		blocks[0].setBounds(x + blockSize, y)
		blocks[1].setBounds(x, y + blockSize)
		blocks[2].setBounds(x + blockSize, y + blockSize)
		blocks[3].setBounds(x, y + blockSize * 2)
	}
}
