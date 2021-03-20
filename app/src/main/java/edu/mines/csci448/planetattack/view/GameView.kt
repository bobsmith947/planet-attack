package edu.mines.csci448.planetattack.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs) {
	private val pieces = ArrayDeque<GamePiece>()

	init {
		val res = context.resources
		var pos = 0
		pieces.apply {
			add(GamePiece(0, 0, PieceShape.I, res))
			add(GamePiece(let { pos += GamePiece.blockSize; pos }, 0, PieceShape.J, res))
			add(GamePiece(let { pos += GamePiece.blockSize * 2; pos }, 0, PieceShape.L, res))
			add(GamePiece(let { pos += GamePiece.blockSize * 2; pos }, 0, PieceShape.O, res))
			add(GamePiece(let { pos += GamePiece.blockSize * 2; pos }, 0, PieceShape.S, res))
			add(GamePiece(let { pos += GamePiece.blockSize * 2; pos }, 0, PieceShape.T, res))
			add(GamePiece(let { pos += GamePiece.blockSize * 2; pos }, 0, PieceShape.Z, res))
		}
	}

	override fun draw(canvas: Canvas?) {
		super.draw(canvas)
		pieces.forEach {
			if (canvas != null) {
				it.drawBlocks(canvas)
			}
		}
	}
}
