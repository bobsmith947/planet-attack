package edu.mines.csci448.planetattack.graphics

import com.google.common.collect.BiMap

enum class PieceDirection {
	UP {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x, it.y - GamePiece.blockSize) }) false
			else {
				piece.y -= GamePiece.blockSize
				piece.shape.make(piece)
				true
			}
		}
	},
	DOWN {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x, it.y + GamePiece.blockSize) }) false
			else {
				piece.y += GamePiece.blockSize
				piece.shape.make(piece)
				true
			}
		}
	},
	LEFT {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x - GamePiece.blockSize, it.y) }) false
			else {
				piece.x -= GamePiece.blockSize
				piece.shape.make(piece)
				true
			}
		}
	},
	RIGHT {
		override fun move(piece: GamePiece): Boolean {
			return if (!piece.blocks.filterNotNull().all { pieceContains(piece, it.x + GamePiece.blockSize, it.y) }) false
			else {
				piece.x += GamePiece.blockSize
				piece.shape.make(piece)
				true
			}
		}
	};

	abstract fun move(piece: GamePiece): Boolean

	// determines whether the coordinates are available to move to
	protected fun pieceContains(piece: GamePiece, x: Int, y: Int): Boolean {
		val block = GamePiece.occupiedSpaces.inverse()[x to y]
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
