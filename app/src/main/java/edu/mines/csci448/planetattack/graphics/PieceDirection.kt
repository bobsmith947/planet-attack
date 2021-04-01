package edu.mines.csci448.planetattack.graphics

import edu.mines.csci448.planetattack.graphics.GamePiece.Companion.pieceContains
import edu.mines.csci448.planetattack.graphics.GamePiece.Companion.blockSize

enum class PieceDirection {
	UP {
		override fun drop(piece: GamePiece) = moveUp(piece)
	},
	DOWN {
		override fun drop(piece: GamePiece) = moveDown(piece)
	},
	LEFT {
		override fun drop(piece: GamePiece) = moveLeft(piece)
	},
	RIGHT {
		override fun drop(piece: GamePiece) = moveRight(piece)
	};

	abstract fun drop(piece: GamePiece): Boolean

	fun moveUp(piece: GamePiece): Boolean {
		return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x, it.y - blockSize) }) false
		else {
			piece.y -= blockSize
			piece.shape.make(piece)
			true
		}
	}

	fun moveDown(piece: GamePiece): Boolean {
		return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x, it.y + blockSize) }) false
		else {
			piece.y += blockSize
			piece.shape.make(piece)
			true
		}
	}

	fun moveLeft(piece: GamePiece): Boolean {
		return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x - blockSize, it.y) }) false
		else {
			piece.x -= blockSize
			piece.shape.make(piece)
			true
		}
	}

	fun moveRight(piece: GamePiece): Boolean {
		return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x + blockSize, it.y) }) false
		else {
			piece.x += blockSize
			piece.shape.make(piece)
			true
		}
	}
}
