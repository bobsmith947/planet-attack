package edu.mines.csci448.planetattack.graphics

enum class PieceDirection {
	UP {
		override fun move(piece: GamePiece) {
			piece.y -= GamePiece.blockSize
			piece.makeShape()
		}
	},
	DOWN {
		override fun move(piece: GamePiece) {
			piece.y += GamePiece.blockSize
			piece.makeShape()
		}
	},
	LEFT {
		override fun move(piece: GamePiece) {
			piece.x += GamePiece.blockSize
			piece.makeShape()
		}
	},
	RIGHT {
		override fun move(piece: GamePiece) {
			piece.x -= GamePiece.blockSize
			piece.makeShape()
		}
	};

	abstract fun move(piece: GamePiece)
}
