package edu.mines.csci448.planetattack.ui.game

import android.content.res.Resources
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import androidx.lifecycle.ViewModel
import edu.mines.csci448.planetattack.GameSpeed
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.*
import kotlin.reflect.full.createInstance

class GameViewModel(
	private val highscoreRepository: HighscoreRepository,
	private val callbacks: GameFragmentCallbacks,
	private var speed: GameSpeed,
	private var holdPiece: GamePiece?,
	private val pieces: ArrayDeque<GamePiece>,
	private val nextQueue: ArrayDeque<GamePiece>,
	private val score: Int,
	private val blocksPerAxis: Int
	): ViewModel() {

	val topScoreLiveData = highscoreRepository.getTopScore()

	// region UI Properties
	var canvasWidth = 0
	var canvasHeight = 0
	// endregion

	// region Game Play Properties
	val pieceMover = object : Runnable {
		override fun run() {
			movePiece()
			try {
				onPiecesChanged()
			} catch (e: Exception) {
				onGameEnded()
				return
			}
			callbacks.gameViewPostDelayed(this, speed.dropDelayMillis)
		}
	}

	private lateinit var rings: List<MutableSet<Pair<Int, Int>>>

	// endregion

	// region Game Play State Properties
	private var isPaused = false
	private var gameHasEnded = false

	private var currentScore = score
		set(value) {
			field = value
			callbacks.drawScore(value)
		}

	private lateinit var planetBlock: BlockDrawable


	companion object {
		private const val PIECE_PLACED_SCORE = 1_000
		private const val RING_CLEARED_SCORE = 10_000
		private const val PIECES_KEY = "pieces"
		private const val NEXT_KEY = "nextQueue"
		private const val HOLD_KEY = "holdPiece"
		private const val SCORE_KEY = "currentScore"
	}

	// endregion
	fun makePlanetBlock(resources: Resources) {
		planetBlock = BlockDrawable(BlockColor.GRAY, resources, null)
		val x = (canvasWidth / 2) - (GamePiece.blockSize / 2)
		val y = (canvasHeight / 2) - (GamePiece.blockSize / 2)
		planetBlock.setBounds(x, y)
		GamePiece.occupiedSpaces[planetBlock] = x to y

		calculateRings()
	}

	fun makeNextQueue() {
		nextQueue.addAll(generateSequence(this::generatePiece).take(3))
	}

	fun addNextPiece() {
		pieces.addLast(nextQueue.removeFirst())
		nextQueue.addLast(generatePiece())
		callbacks.drawNextQueue(nextQueue)
	}

	fun generatePiece(): GamePiece {
		val direction = PieceDirection.values().random()
		val shape = PieceShape::class.sealedSubclasses.random().createInstance()
		return when (direction) {
			PieceDirection.UP -> {
				shape.createLayout(PieceShape.ROTATION_180)
				GamePiece(
					(canvasWidth / 2) - (GamePiece.blockSize / 2),
					canvasHeight - (GamePiece.blockSize * shape.height),
					shape,
					direction
				)
			}
			PieceDirection.DOWN -> {
				shape.createLayout(PieceShape.ROTATION_0)
				GamePiece(
					(canvasWidth / 2) - (GamePiece.blockSize / 2),
					0,
					shape,
					direction
				)
			}
			PieceDirection.LEFT -> {
				shape.createLayout(PieceShape.ROTATION_90)
				GamePiece(
					canvasWidth - (GamePiece.blockSize * shape.width),
					(canvasHeight / 2) - (GamePiece.blockSize / 2),
					shape,
					direction
				)
			}
			PieceDirection.RIGHT -> {
				shape.createLayout(PieceShape.ROTATION_270)
				GamePiece(
					0,
					(canvasHeight / 2) - (GamePiece.blockSize / 2),
					shape,
					direction
				)
			}
		}
	}

	private fun resetPiecePosition(piece: GamePiece) {
		val (x, y) = when (piece.direction) {
			PieceDirection.UP -> arrayOf((canvasWidth / 2) - (GamePiece.blockSize / 2),
				canvasHeight - (GamePiece.blockSize * piece.shape.height))
			PieceDirection.DOWN -> arrayOf((canvasWidth / 2) - (GamePiece.blockSize / 2), 0)
			PieceDirection.LEFT -> arrayOf(canvasWidth - (GamePiece.blockSize * piece.shape.width),
				(canvasHeight / 2) - (GamePiece.blockSize / 2))
			PieceDirection.RIGHT -> arrayOf(0, (canvasHeight / 2) - (GamePiece.blockSize / 2))
		}
		piece.x = x
		piece.y = y
	}

	fun resetPieceDirection(piece: GamePiece) {
		piece.direction = when (piece.direction) {
			PieceDirection.UP -> {
				if (piece.y <= 0) PieceDirection.DOWN
				else PieceDirection.UP
			}
			PieceDirection.DOWN -> {
				if (piece.y >= canvasHeight - (GamePiece.blockSize * piece.shape.height)) PieceDirection.UP
				else PieceDirection.DOWN
			}
			PieceDirection.LEFT -> {
				if (piece.x <= 0) PieceDirection.RIGHT
				else PieceDirection.LEFT
			}
			PieceDirection.RIGHT -> {
				if (piece.x >= canvasWidth - (GamePiece.blockSize * piece.shape.width)) PieceDirection.LEFT
				else PieceDirection.RIGHT
			}
		}
	}

	fun movePiece() {
		val piece = pieces.last()
		if (!piece.direction.drop(piece)) {
			currentScore += PIECE_PLACED_SCORE * (speed.ordinal + 1)
			// check for completed rings
			clearRings()
			addNextPiece()
		}
		// adjust direction if reached other side
		resetPieceDirection(piece)
	}

	fun rotatePiece() {
		val piece = pieces.last()
		piece.shape.createLayout((piece.shape.currentRotation + PieceShape.ROTATION_90) % PieceShape.ROTATION_360)
		piece.shape.make(piece)
	}

	fun swapHoldPiece() {
		val hold = holdPiece
		holdPiece = pieces.removeLast()
		holdPiece!!.blocks.forEach { GamePiece.occupiedSpaces.remove(it) }
		resetPiecePosition(holdPiece!!)
		if (hold != null) {
			pieces.addLast(hold)
		} else {
			addNextPiece()
		}

		callbacks.drawHoldPiece(holdPiece)
	}

	fun calculateRings() {
		val center = (canvasHeight / 2) - (GamePiece.blockSize / 2)
		val numRings = center / GamePiece.blockSize
		rings = List(numRings) { LinkedHashSet() }
		// add center coordinates to use for calculating first ring
		rings[0].add(center to center)
		for (i in 1 until numRings) {
			// add diagonal spaces
			val offset = GamePiece.blockSize * i
			val diagonals = arrayOf(
				// top left
				(center - offset) to (center - offset),
				// top right
				(center + offset) to (center - offset),
				// bottom right
				(center + offset) to (center + offset),
				// bottom left
				(center - offset) to (center + offset)
			)
			rings[i].addAll(diagonals)

			// add spaces bordering previous ring
			for (j in rings[i - 1]) {
				if (j.first <= center) rings[i].add((j.first - GamePiece.blockSize) to j.second)
				if (j.first >= center) rings[i].add((j.first + GamePiece.blockSize) to j.second)
				if (j.second <= center) rings[i].add(j.first to (j.second - GamePiece.blockSize))
				if (j.second >= center) rings[i].add(j.first to (j.second + GamePiece.blockSize))
			}

			// remove interior spaces
			rings[i].removeIf {
				(it.first in (diagonals[0].first + 1)..center && it.second in (diagonals[0].second + 1)..center) ||
						(it.first in center until diagonals[1].first && it.second in (diagonals[1].second + 1)..center) ||
						(it.first in center until diagonals[2].first && it.second in center until diagonals[2].second) ||
						(it.first in (diagonals[3].first + 1)..center && it.second in center until diagonals[3].second)
			}
		}
		// remove center coordinates
		rings = rings.drop(1)
	}

	private fun clearRings() {
		rings.forEachIndexed { index, ring ->
			if (GamePiece.occupiedSpaces.values.containsAll(ring)) {
				// determine ring bounds
				val (xmin, xmax, ymin, ymax) = with(ring) { arrayOf(
					minOf { it.first }, maxOf { it.first },
					minOf { it.second }, maxOf { it.second }
				) }
				ring.forEach {
					val block = GamePiece.occupiedSpaces.inverse()[it]
					GamePiece.occupiedSpaces.remove(block)
					val blocks = block!!.piece!!.blocks
					blocks[blocks.indexOf(block)] = null
				}
				currentScore += RING_CLEARED_SCORE * (index + 1) * (speed.ordinal + 1)
				// remove empty pieces
				pieces.removeIf { it.blocks.filterNotNull().isEmpty() }
				// fill in cleared spaces
				pieces.forEach { piece ->
					piece.blocks.filterNotNull().forEach {
						if (it.x < xmin) it.moveRight()
						else if (it.x > xmax) it.moveLeft()
						if (it.y < ymin) it.moveDown()
						else if (it.y > ymax) it.moveUp()
						// update piece coordinates
						val (x, y) = piece.blocks.filterNotNull()[0]; piece.x = x; piece.y = y
					}
				}
			}
		}
	}

	fun addHighscore(highscore: Highscore) {
		highscoreRepository.addHighscore(highscore)
	}

	fun onPiecesChanged() {
		val placedPieces = pieces.dropLast(1)

		val minAllowed = 4 * GamePiece.blockSize
		val maxAllowed = GamePiece.blockSize * (blocksPerAxis - 4)

		if (!placedPieces.isEmpty()) {
			// Check if pieces out of bounds
			val minX = placedPieces.minOf { piece ->
				piece.blocks.minOf { block ->
					block?.x ?: 1_000_000
				}
			}

			val maxX = placedPieces.maxOf { piece ->
				piece.blocks.maxOf { block ->
					block?.x ?: 0
				}
			}

			val minY = placedPieces.minOf { piece ->
				piece.blocks.minOf { block ->
					block?.y ?: 1_000_000
				}
			}

			val maxY = placedPieces.maxOf { piece ->
				piece.blocks.maxOf { block ->
					block?.y ?: 0
				}
			}

			if (minX < minAllowed || minY < minAllowed || maxX >= maxAllowed || maxY >= maxAllowed) {
				onGameEnded()
				return
			}
		}

		callbacks.drawPieces(planetBlock, pieces)
	}

	fun onGameEnded() {
		if (!gameHasEnded) {
			val highscore = Highscore(score=currentScore)
			addHighscore(highscore)
			callbacks.onGameEnded()
		}
		gameHasEnded = true
	}

	fun saveToBundle(bundle: Bundle) {
		bundle.putParcelableArrayList(PIECES_KEY, ArrayList(pieces))
		bundle.putParcelableArrayList(NEXT_KEY, ArrayList(nextQueue))
		bundle.putParcelable(HOLD_KEY, holdPiece)
		bundle.putInt(SCORE_KEY, currentScore)
	}

	fun resume() {
		callbacks.gameViewPostDelayed(pieceMover, speed.dropDelayMillis)
	}

	fun movePieceInDirection(direction: PieceDirection) {
		val piece = pieces.last()
		when (direction) {
			PieceDirection.UP -> {
				val minY = piece.blocks.minOf { it?.y ?: 1_000_000 }

				if (minY > 0) {
					piece.direction.moveUp(piece)
				}
			}
			PieceDirection.DOWN -> {
				val maxY = piece.blocks.maxOf { it?.y ?: 0 }

				if (maxY < (blocksPerAxis - 1) * GamePiece.blockSize) {
					piece.direction.moveDown(piece)
				}
			}
			PieceDirection.LEFT -> {
				val minX = piece.blocks.minOf { it?.x ?: 1_000_000 }

				if (minX > 0) {
					piece.direction.moveLeft(piece)
				}
			}
			PieceDirection.RIGHT -> {
				val maxX = piece.blocks.maxOf { it?.x ?: 0 }

				if (maxX < (blocksPerAxis - 1) * GamePiece.blockSize) {
					piece.direction.moveRight(piece)
				}
			}
		}
	}

	fun fastMove() {
		val piece = pieces.last()
		while (piece.x in GamePiece.blockSize until canvasWidth - (GamePiece.blockSize * piece.shape.width) &&
			piece.y in GamePiece.blockSize until canvasHeight - (GamePiece.blockSize * piece.shape.height) &&
			piece.direction.drop(piece)) {
			resetPieceDirection(piece)
		}
		movePiece()
		onPiecesChanged()
	}

	val pieceDirection: PieceDirection get() = pieces.last().direction
}
