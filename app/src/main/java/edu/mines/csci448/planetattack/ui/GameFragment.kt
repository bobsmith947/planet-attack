package edu.mines.csci448.planetattack.ui

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import edu.mines.csci448.planetattack.*
import edu.mines.csci448.planetattack.data.Highscore
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.*
import edu.mines.csci448.planetattack.graphics.GamePiece.Companion.blockSize
import edu.mines.csci448.planetattack.graphics.GamePiece.Companion.occupiedSpaces
import edu.mines.csci448.planetattack.ui.preferences.GameSize
import edu.mines.csci448.planetattack.ui.preferences.GameSpeed
import edu.mines.csci448.planetattack.ui.preferences.SwipeSensitivity
import edu.mines.csci448.planetattack.ui.viewmodel.GameViewModel
import edu.mines.csci448.planetattack.ui.viewmodel.GameViewModelFactory
import kotlin.jvm.Throws
import kotlin.reflect.full.createInstance

class GameFragment : Fragment(),
	BackPressListener, SurfaceHolder.Callback, GestureDetector.OnGestureListener {
	private lateinit var gameViewModel: GameViewModel

	// region UI Properties
	private var _binding: FragmentGameBinding? = null
	private val binding get() = _binding!!

	private var _holder: SurfaceHolder? = null
	private val holder get() = _holder!!

	private var canvasWidth = 0
	private var canvasHeight = 0
	private var canvasMargin = 0

	private lateinit var boundsRect: Rect
	private val boundsPaint = Paint().apply {
		color = Color.WHITE
		alpha = 32
	}

	private var showNext = true
	private var showHold = false
	// endregion

	private val pieceMover = object : Runnable {
		override fun run() {
			binding.gameView.postDelayed(this, speed.dropDelayMillis)
			movePiece()
			drawPieces()
		}
	}

	// region Gameplay Properties
	private var isPaused = false
	private lateinit var speed: GameSpeed
	private lateinit var size: GameSize
	private lateinit var rings: List<MutableSet<Pair<Int, Int>>>
	private var currentScore = 0
		set(value) {
			field = value
			binding.scoreLabel.text = value.toString()
			if (value > topScore) {
				binding.highScoreLabel.text = getString(R.string.new_record)
			}
		}
	private var topScore = 0

	private val pieces = ArrayDeque<GamePiece>()
	private val nextQueue = ArrayDeque<GamePiece>()
	private lateinit var planetBlock: BlockDrawable
	private var holdPiece: GamePiece? = null

	private var xOffset = 0.0
	private var yOffset = 0.0
	private var minimumOffset = 0.0
	private lateinit var swipeSensitivity: SwipeSensitivity
	// endregion

	companion object {
		private const val PIECE_PLACED_SCORE = 1_000
		private const val RING_CLEARED_SCORE = 10_000
		private const val PIECES_KEY = "pieces"
		private const val NEXT_KEY = "nextQueue"
		private const val HOLD_KEY = "holdPiece"
		private const val SCORE_KEY = "currentScore"
	}

	// region Lifecycle Callbacks
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		GamePiece.resources = resources

		val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
		speed = SettingsFragment.getSpeedPreference(prefs.getString(getString(R.string.speed_key), ""))
		size = SettingsFragment.getSizePreference(prefs.getString(getString(R.string.size_key), ""))
		swipeSensitivity = SettingsFragment.getSensitivityPreference(prefs.getInt(getString(R.string.sensitivity_key), 0))
		showNext = prefs.getBoolean(getString(R.string.next_key), showNext)
		showHold = prefs.getBoolean(getString(R.string.hold_key), showHold)

		if (savedInstanceState != null) {
			savedInstanceState.getParcelableArrayList<GamePiece>(PIECES_KEY)?.let { pieces.addAll(it) }
			savedInstanceState.getParcelableArrayList<GamePiece>(NEXT_KEY)?.let { nextQueue.addAll(it) }
			holdPiece = savedInstanceState.getParcelable(HOLD_KEY)
		}

		val factory = GameViewModelFactory(requireContext())
		gameViewModel = ViewModelProvider(this, factory)
			.get(GameViewModel::class.java)
	}

	@SuppressLint("ClickableViewAccessibility")
	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		(requireActivity() as AppCompatActivity).supportActionBar?.hide()
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()

		if (savedInstanceState == null) {
			resume()
		} else {
			pause()
			currentScore = savedInstanceState.getInt(SCORE_KEY)
		}

		binding.gameView.holder.addCallback(this)
		val detector = GestureDetectorCompat(requireContext(), this)
		binding.gameView.setOnTouchListener { _, event ->
			if (!isPaused) detector.onTouchEvent(event)
			else true
		}

		binding.holdImageView.setImageDrawable(null)
		if (!showNext) {
			binding.nextLabel.visibility = View.INVISIBLE
			binding.firstNextImageView.visibility = View.INVISIBLE
			binding.secondNextImageView.visibility = View.INVISIBLE
			binding.thirdNextImageView.visibility = View.INVISIBLE
		}
		if (!showHold) {
			binding.holdButton.visibility = View.INVISIBLE
		}

		return binding.root
	}

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		gameViewModel.topScoreLiveData.observe(
			viewLifecycleOwner, { score ->
				binding.highScoreLabel.text = score?.toString() ?: "None"
				topScore = score ?: 0
			}
		)
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.gameView.holder.removeCallback(this)
		_binding = null
		(requireActivity() as AppCompatActivity).supportActionBar?.show()
	}

	override fun onDestroy() {
		super.onDestroy()
		occupiedSpaces.clear()
	}

	override fun onBackPressed() {
		pause()
	}

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList(PIECES_KEY, ArrayList(pieces))
		outState.putParcelableArrayList(NEXT_KEY, ArrayList(nextQueue))
		outState.putParcelable(HOLD_KEY, holdPiece)
		outState.putInt(SCORE_KEY, currentScore)
	}
	// endregion

	// region UI methods
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

	private fun gameOver() {
		pause()
		binding.resumeButton.visibility = View.GONE
		binding.gameOverTextView.visibility = View.VISIBLE
		gameViewModel.addHighscore(Highscore(score = currentScore))
	}

	private fun setButtonOnClickListeners() {
		binding.resumeButton.setOnClickListener { resume() }
		binding.menuButton.setOnClickListener { pause() }
		binding.quitButton.setOnClickListener { findNavController().navigateUp() }
		binding.holdButton.setOnClickListener { swapHoldPiece() }
	}
	// endregion

	// region SurfaceHolder Callbacks
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

			blockSize = canvasWidth / (size.blocksPerSide * 2 + 1)
			val boundsStart = blockSize * 4
			val boundsEnd = blockSize * (size.blocksPerSide * 2 - 3)
			boundsRect = Rect(
				boundsStart,
				boundsStart,
				boundsEnd,
				boundsEnd
			)

			// make sure canvas is a multiple of block size so pieces are aligned
			canvasMargin = canvasWidth % blockSize
			canvasWidth -= canvasMargin
			canvasHeight -= canvasMargin

			minimumOffset = canvasWidth.toDouble() / size.blocksPerSide.toDouble() / 2.0 / swipeSensitivity.multiplier

			// add "planet" to center
			planetBlock = BlockDrawable(BlockColor.GRAY, resources, null)
			with (planetBlock) {
				setBounds((canvasWidth / 2) - (blockSize / 2),
					(canvasHeight / 2) - (blockSize / 2))
				occupiedSpaces.updateBlock()
			}

			calculateRings()
			// when not restoring from saved instance state
			if (!isPaused) {
				nextQueue.addAll(generateSequence(this::generatePiece).take(3))
				addNextPiece()
			}
		}
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		drawPieces()
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		_holder = null
		pause()
	}
	// endregion

	//region Gameplay Methods
	@Throws(Exception::class)
	private fun drawPieces() {
		val canvas = holder.lockCanvas()
		// reset the canvas
		canvas.drawColor(Color.BLACK)
		canvas.drawRect(boundsRect, boundsPaint)
		planetBlock.draw(canvas)
		pieces.forEach { it.drawBlocks(canvas) }
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
					(canvasWidth / 2) - (blockSize / 2),
					canvasHeight - (blockSize * shape.height),
					shape,
					direction
				)
			}
			PieceDirection.DOWN -> {
				shape.createLayout(PieceShape.ROTATION_0)
				GamePiece(
					(canvasWidth / 2) - (blockSize / 2),
					0,
					shape,
					direction
				)
			}
			PieceDirection.LEFT -> {
				shape.createLayout(PieceShape.ROTATION_90)
				GamePiece(
					canvasWidth - (blockSize * shape.width),
					(canvasHeight / 2) - (blockSize / 2),
					shape,
					direction
				)
			}
			PieceDirection.RIGHT -> {
				shape.createLayout(PieceShape.ROTATION_270)
				GamePiece(
					0,
					(canvasHeight / 2) - (blockSize / 2),
					shape,
					direction
				)
			}
		}
	}

	private fun resetPiecePosition(piece: GamePiece) {
		val (x, y) = when (piece.direction) {
			PieceDirection.UP -> arrayOf((canvasWidth / 2) - (blockSize / 2),
				canvasHeight - (blockSize * piece.shape.height))
			PieceDirection.DOWN -> arrayOf((canvasWidth / 2) - (blockSize / 2), 0)
			PieceDirection.LEFT -> arrayOf(canvasWidth - (blockSize * piece.shape.width),
				(canvasHeight / 2) - (blockSize / 2))
			PieceDirection.RIGHT -> arrayOf(0, (canvasHeight / 2) - (blockSize / 2))
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
				if (piece.y >= canvasHeight - (blockSize * piece.shape.height)) PieceDirection.UP
				else PieceDirection.DOWN
			}
			PieceDirection.LEFT -> {
				if (piece.x <= 0) PieceDirection.RIGHT
				else PieceDirection.LEFT
			}
			PieceDirection.RIGHT -> {
				if (piece.x >= canvasWidth - (blockSize * piece.shape.width)) PieceDirection.LEFT
				else PieceDirection.RIGHT
			}
		}
	}

	private fun movePiece() {
		val piece = pieces.last()
		if (!piece.direction.drop(piece)) {
			// check if the piece was placed out of bounds
			if (pieces.last().blocks.filterNotNull().any { !boundsRect.contains(it.x, it.y) }) {
				gameOver()
				return
			}
			currentScore += PIECE_PLACED_SCORE * (speed.ordinal + 1)
			// check for completed rings
			clearRings()
			addNextPiece()
		}
		// adjust direction if reached other side
		resetPieceDirection(piece)
	}

	private fun swapHoldPiece() {
		val hold = holdPiece
		holdPiece = pieces.removeLast()
		holdPiece!!.blocks.forEach { occupiedSpaces.remove(it) }
		resetPiecePosition(holdPiece!!)
		if (hold != null) pieces.addLast(hold)
		else addNextPiece()
		binding.holdImageView.setImageResource(holdPiece!!.shape.iconId)
	}

	private fun calculateRings() {
		val center = (canvasHeight / 2) - (blockSize / 2)
		val numRings = center / blockSize
		rings = List(numRings) { LinkedHashSet() }
		// add center coordinates to use for calculating first ring
		rings[0].add(center to center)
		for (i in 1 until numRings) {
			// add diagonal spaces
			val offset = blockSize * i
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
				if (j.first <= center) rings[i].add((j.first - blockSize) to j.second)
				if (j.first >= center) rings[i].add((j.first + blockSize) to j.second)
				if (j.second <= center) rings[i].add(j.first to (j.second - blockSize))
				if (j.second >= center) rings[i].add(j.first to (j.second + blockSize))
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
		var index = 0
		while (index < rings.size) {
			val ring = rings[index]
			// check if ring is completed
			if (occupiedSpaces.values.containsAll(ring)) {
				// determine ring bounds
				val (xmin, xmax, ymin, ymax) = with(ring) { arrayOf(
					minOf { it.first }, maxOf { it.first },
					minOf { it.second }, maxOf { it.second }
				) }
				// remove blocks in ring
				ring.forEach {
					val block = occupiedSpaces.inverse()[it]
					occupiedSpaces.remove(block)
					val blocks = block!!.piece!!.blocks
					blocks[blocks.indexOf(block)] = null
				}
				currentScore += RING_CLEARED_SCORE * (index + 1) * (speed.ordinal + 1)
				// remove empty pieces
				pieces.removeIf { it.blocks.filterNotNull().isEmpty() }
				// fill in cleared spaces
				pieces.forEach { piece ->
					piece.blocks.forEach {
						it?.run {
							if (x < xmin) moveRight()
							else if (x > xmax) moveLeft()
							if (y < ymin) moveDown()
							else if (y > ymax) moveUp()
							occupiedSpaces.updateBlock()
						}
					}
					// update piece coordinates
					val (x, y) = piece.blocks.filterNotNull()[0]
					piece.x = x
					piece.y = y
				}
			} else index++ // clear the same ring multiple times if necessary
		}
	}
	// endregion

	// region GestureDetector Callbacks
	override fun onDown(e: MotionEvent?): Boolean {
		return true
	}

	override fun onShowPress(e: MotionEvent?) {
		return
	}

	override fun onSingleTapUp(e: MotionEvent?): Boolean {
		// TODO handling rotating on edge
		val piece = pieces.last()
		piece.shape.createLayout((piece.shape.currentRotation + PieceShape.ROTATION_90) % PieceShape.ROTATION_360)
		piece.shape.make(piece)
		drawPieces()
		return true
	}

	override fun onScroll(
		e1: MotionEvent?,
		e2: MotionEvent?,
		distanceX: Float,
		distanceY: Float
	): Boolean {
		val piece = pieces.last()
		xOffset += distanceX
		yOffset += distanceY

		when (piece.direction) {
			PieceDirection.UP, PieceDirection.DOWN -> {
				if (xOffset > minimumOffset) {
					xOffset = 0.0
					if (piece.blocks.filterNotNull().minOf { it.x } > 0)
						piece.direction.moveLeft(piece)
				} else if (-xOffset > minimumOffset) {
					xOffset = 0.0
					if (piece.blocks.filterNotNull().maxOf { it.x } < canvasWidth - blockSize)
						piece.direction.moveRight(piece)
				}
			}
			PieceDirection.LEFT, PieceDirection.RIGHT -> {
				if (yOffset > minimumOffset) {
					yOffset = 0.0
					if (piece.blocks.filterNotNull().minOf { it.y } > 0)
						piece.direction.moveUp(piece)
				} else if (-yOffset > minimumOffset) {
					yOffset = 0.0
					if (piece.blocks.filterNotNull().maxOf { it.y } < canvasHeight - blockSize)
						piece.direction.moveDown(piece)
				}
			}
		}

		when (piece.direction) {
			PieceDirection.UP -> {
				if (yOffset > minimumOffset) {
					yOffset = 0.0
					movePiece()
				}
			}
			PieceDirection.DOWN -> {
				if (-yOffset > minimumOffset) {
					yOffset = 0.0
					movePiece()
				}
			}
			PieceDirection.LEFT -> {
				if (xOffset > minimumOffset) {
					xOffset = 0.0
					movePiece()
				}
			}
			PieceDirection.RIGHT -> {
				if (-xOffset > minimumOffset) {
					xOffset = 0.0
					movePiece()
				}
			}
		}

		drawPieces()
		return true
	}

	override fun onLongPress(e: MotionEvent?) {
		val piece = pieces.last()
		while (piece.x in blockSize until canvasWidth - (blockSize * piece.shape.width) &&
			piece.y in blockSize until canvasHeight - (blockSize * piece.shape.height) &&
			piece.direction.drop(piece)) {
			resetPieceDirection(piece)
		}
		drawPieces()
		movePiece()
	}

	override fun onFling(
		e1: MotionEvent?,
		e2: MotionEvent?,
		velocityX: Float,
		velocityY: Float
	): Boolean {
		return true
	}
	// endregion
}
