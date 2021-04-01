package edu.mines.csci448.planetattack.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.BuildConfig
import edu.mines.csci448.planetattack.GameSpeed
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.*
import kotlin.jvm.Throws
import kotlin.reflect.full.createInstance

class GameFragment : Fragment(),
	BackPressListener, SurfaceHolder.Callback, GestureDetector.OnGestureListener {
	private var _binding: FragmentGameBinding? = null
	private val binding get() = _binding!!

	private var _holder: SurfaceHolder? = null
	private val holder get() = _holder!!
	private var canvasWidth = 0
	private var canvasHeight = 0
	private var canvasMargin = 0

	private val pieceMover = object : Runnable {
		override fun run() {
			movePiece()
			try {
				drawPieces()
			} catch (e: Exception) {
				pause()
				binding.resumeButton.visibility = View.GONE
				binding.gameOverTextView.visibility = View.VISIBLE
				return
			}
			binding.gameView.postDelayed(this, speed.dropDelayMillis)
		}
	}

	private var isPaused = false
	private lateinit var speed: GameSpeed
	private lateinit var rings: List<MutableSet<Pair<Int, Int>>>
	private var currentScore = 0
		set(value) {
			field = value
			binding.scoreLabel.text = value.toString()
		}

	private val pieces = ArrayDeque<GamePiece>()
	private val nextQueue = ArrayDeque<GamePiece>()
	private lateinit var planetBlock: BlockDrawable
	private var holdPiece: GamePiece? = null

	companion object {
		private const val PIECE_PLACED_SCORE = 1_000
		private const val RING_CLEARED_SCORE = 10_000
	}

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
		speed = when (prefs.getString("speed", "")) {
			"1" -> GameSpeed.SLOW
			"2" -> GameSpeed.MEDIUM
			"3" -> GameSpeed.FAST
			else -> throw IllegalStateException()
		}
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		(requireActivity() as AppCompatActivity).supportActionBar?.hide()
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()
		resume()
		binding.gameView.holder.addCallback(this)
		val detector = GestureDetectorCompat(requireContext(), this)
		binding.gameView.setOnTouchListener { _, event ->
			if (!isPaused) {
				detector.onTouchEvent(event)
			} else true
		}
		binding.holdImageView.setImageDrawable(null)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.gameView.holder.removeCallback(this)
		_binding = null
		(requireActivity() as AppCompatActivity).supportActionBar?.show()
	}

	override fun onDestroy() {
		super.onDestroy()
		GamePiece.occupiedSpaces.clear()
	}

	override fun onBackPressed() {
		pause()
	}

	private fun pause() {
		isPaused = true
		binding.menuOverlay.visibility = View.VISIBLE
		binding.menuButton.apply {
			alpha = ResourcesCompat.getFloat(resources, R.dimen.alpha_background)
			isEnabled = false
		}
		binding.holdButton.apply {
			alpha = ResourcesCompat.getFloat(resources, R.dimen.alpha_background)
			isEnabled = false
		}
		binding.gameView.removeCallbacks(pieceMover)
	}

	private fun resume() {
		isPaused = false
		binding.menuOverlay.visibility = View.INVISIBLE
		binding.menuButton.apply {
			alpha = ResourcesCompat.getFloat(resources, R.dimen.alpha_foreground)
			isEnabled = true
		}
		binding.holdButton.apply {
			alpha = ResourcesCompat.getFloat(resources, R.dimen.alpha_foreground)
			isEnabled = true
		}
		binding.gameView.postDelayed(pieceMover, speed.dropDelayMillis)
	}

	private fun setButtonOnClickListeners() {
		binding.resumeButton.setOnClickListener {
			resume()
		}

		binding.menuButton.setOnClickListener {
			pause()
		}

		binding.quitButton.setOnClickListener {
			findNavController().navigateUp()
		}

		binding.holdButton.setOnClickListener {
			swapHoldPiece()
		}
	}

	override fun surfaceCreated(holder: SurfaceHolder) {
		_holder = holder
		// when first initializing
		if (canvasWidth == 0 && canvasHeight == 0) {
			val canvas = holder.lockCanvas()
			canvasWidth = canvas.width
			canvasHeight = canvas.height
			holder.unlockCanvasAndPost(canvas)
			if (BuildConfig.DEBUG && canvasWidth != canvasHeight) {
				error("Assertion failed")
			}
			// make sure canvas is a multiple of block size so pieces are aligned
			canvasMargin = canvasWidth % GamePiece.blockSize
			canvasWidth -= canvasMargin
			canvasHeight -= canvasMargin

			// add "planet" to center
			planetBlock = BlockDrawable(BlockColor.GRAY, resources, null)
			val x = (canvasWidth / 2) - (GamePiece.blockSize / 2)
			val y = (canvasHeight / 2) - (GamePiece.blockSize / 2)
			planetBlock.setBounds(x, y)
			GamePiece.occupiedSpaces[planetBlock] = x to y

			calculateRings()
			nextQueue.addAll(generateSequence(this::generatePiece).take(3))
			addNextPiece()
		}
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		drawPieces()
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		_holder = null
		pause()
	}

	@Throws(Exception::class)
	private fun drawPieces() {
		val canvas = holder.lockCanvas()
		// reset the canvas
		canvas.drawColor(Color.BLACK)
		planetBlock.draw(canvas)
		// draw previously placed pieces
		pieces.dropLast(1).forEach { it.drawBlocks(canvas) }
		// ensure there is space for the current piece
		val piece = pieces.last()
		if (piece.blocks.filterNotNull().any { !piece.contains(it.x, it.y) }) {
			// if there is no space for the piece, throw an exception to indicate the game is over
			holder.unlockCanvasAndPost(canvas)
			throw Exception()
		}
		piece.drawBlocks(canvas)
		holder.unlockCanvasAndPost(canvas)
	}

	private fun addNextPiece() {
		pieces.addLast(nextQueue.removeFirst())
		nextQueue.addLast(generatePiece())
		binding.firstNextImageView.setImageResource(nextQueue[0].shape.iconId)
		binding.secondNextImageView.setImageResource(nextQueue[1].shape.iconId)
		binding.thirdNextImageView.setImageResource(nextQueue[2].shape.iconId)
	}

	private fun generatePiece(): GamePiece {
		val direction = PieceDirection.values().random()
		val shape = PieceShape::class.sealedSubclasses.random().createInstance()
		return when (direction) {
			PieceDirection.UP -> {
				shape.createLayout(PieceShape.ROTATION_180)
				GamePiece(
					(canvasWidth / 2) - (GamePiece.blockSize / 2),
					canvasHeight - (GamePiece.blockSize * shape.height),
					shape,
					direction,
					resources
				)
			}
			PieceDirection.DOWN -> {
				shape.createLayout(PieceShape.ROTATION_0)
				GamePiece(
					(canvasWidth / 2) - (GamePiece.blockSize / 2),
					0,
					shape,
					direction,
					resources
				)
			}
			PieceDirection.LEFT -> {
				shape.createLayout(PieceShape.ROTATION_90)
				GamePiece(
					canvasWidth - (GamePiece.blockSize * shape.width),
					(canvasHeight / 2) - (GamePiece.blockSize / 2),
					shape,
					direction,
					resources
				)
			}
			PieceDirection.RIGHT -> {
				shape.createLayout(PieceShape.ROTATION_270)
				GamePiece(
					0,
					(canvasHeight / 2) - (GamePiece.blockSize / 2),
					shape,
					direction,
					resources
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

	private fun resetPieceDirection(piece: GamePiece) {
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

	private fun movePiece() {
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

	private fun rotatePiece() {
		val piece = pieces.last()
		piece.shape.createLayout((piece.shape.rotation + PieceShape.ROTATION_90) % PieceShape.ROTATION_360)
	}

	private fun swapHoldPiece() {
		val hold = holdPiece
		holdPiece = pieces.removeLast()
		holdPiece!!.blocks.forEach { GamePiece.occupiedSpaces.remove(it) }
		resetPiecePosition(holdPiece!!)
		if (hold != null) {
			pieces.addLast(hold)
		} else {
			addNextPiece()
		}
		binding.holdImageView.setImageResource(holdPiece!!.shape.iconId)
	}

	private fun calculateRings() {
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
				ring.forEach {
					val block = GamePiece.occupiedSpaces.inverse()[it]
					if (block != null) {
						GamePiece.occupiedSpaces.remove(block)
						val blocks = block.piece!!.blocks
						blocks[blocks.indexOf(block)] = null
					}
				}
				currentScore += RING_CLEARED_SCORE * (index + 1) * (speed.ordinal + 1)
				// move pieces to fill in cleared spaces
				// TODO this doesn't work very well, need to find a better solution
				pieces.forEach {
					if (it.blocks.filterNotNull().isEmpty()) pieces.remove(it)
					else it.direction.drop(it)
				}
			}
		}
	}

	override fun onDown(e: MotionEvent?): Boolean {
		return true
	}

	override fun onShowPress(e: MotionEvent?) {
		return
	}

	override fun onSingleTapUp(e: MotionEvent?): Boolean {
		rotatePiece()
		return true
	}

	override fun onScroll(
		e1: MotionEvent?,
		e2: MotionEvent?,
		distanceX: Float,
		distanceY: Float
	): Boolean {
		return true
	}

	override fun onLongPress(e: MotionEvent?) {
		val piece = pieces.last()
		while (piece.x in GamePiece.blockSize until canvasWidth - (GamePiece.blockSize * piece.shape.width) &&
			piece.y in GamePiece.blockSize until canvasHeight - (GamePiece.blockSize * piece.shape.height) &&
			piece.direction.drop(piece)) {
			resetPieceDirection(piece)
		}
		try {
			drawPieces()
		} catch (e: Exception) {
			pause()
			binding.resumeButton.visibility = View.GONE
			binding.gameOverTextView.visibility = View.VISIBLE
		}
		movePiece()
	}

	override fun onFling(
		e1: MotionEvent?,
		e2: MotionEvent?,
		velocityX: Float,
		velocityY: Float
	): Boolean {
		val piece = pieces.last()
		when (piece.direction) {
			PieceDirection.UP, PieceDirection.DOWN -> {
				if (velocityX < 0) piece.direction.moveLeft(piece)
				else if (velocityX > 0) piece.direction.moveRight(piece)
			}
			PieceDirection.LEFT, PieceDirection.RIGHT -> {
				if (velocityY < 0) piece.direction.moveUp(piece)
				else if (velocityY > 0) piece.direction.moveDown(piece)
			}
		}
		return true
	}
}
