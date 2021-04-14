package edu.mines.csci448.planetattack.ui.game

import edu.mines.csci448.planetattack.graphics.BlockDrawable
import edu.mines.csci448.planetattack.graphics.GamePiece

interface GameFragmentCallbacks {
	fun drawPieces(planetBlock: BlockDrawable, pieces: ArrayDeque<GamePiece>)

	fun drawNextQueue(queue: ArrayDeque<GamePiece>)

	fun drawHoldPiece(piece: GamePiece?)

	fun drawScore(score: Int)

	fun onGameEnded()

	fun gameViewPostDelayed(action: Runnable, delayMillis: Long)
}