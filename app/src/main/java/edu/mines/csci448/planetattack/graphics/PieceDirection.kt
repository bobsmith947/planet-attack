package edu.mines.csci448.planetattack.graphics

import com.google.common.collect.BiMap

enum class PieceDirection {
	UP {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.all { pieceContains(piece, it.x, it.y - GamePiece.blockSize) }) false
			else {
				piece.y -= GamePiece.blockSize
				piece.makeShape()
				true
			}
		}
	},
	DOWN {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.all { pieceContains(piece, it.x, it.y + GamePiece.blockSize) }) false
			else {
				piece.y += GamePiece.blockSize
				piece.makeShape()
				true
			}
		}
	},
	LEFT {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.all { pieceContains(piece, it.x + GamePiece.blockSize, it.y) }) false
			else {
				piece.x += GamePiece.blockSize
				piece.makeShape()
				true
			}
		}
	},
	RIGHT {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.all { pieceContains(piece, it.x - GamePiece.blockSize, it.y) }) false
			else {
				piece.x -= GamePiece.blockSize
				piece.makeShape()
				true
			}
		}
	};

	abstract fun move(piece: GamePiece): Boolean
	protected val usedBlocks: BiMap<Pair<Int, Int>, BlockDrawable> = GamePiece.occupiedSpaces.inverse()
	protected fun pieceContains(piece: GamePiece, x: Int, y: Int) = usedBlocks[x to y]?.piece ?: piece === piece
}
