package edu.mines.csci448.planetattack.ui.game

import android.annotation.SuppressLint
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.GestureDetectorCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.BuildConfig
import edu.mines.csci448.planetattack.GameSpeed
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.data.repo.HighscoreRepository
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.*
import kotlin.collections.ArrayDeque

class GameFragment : Fragment(),
	BackPressListener, SurfaceHolder.Callback, GestureDetector.OnGestureListener, GameFragmentCallbacks {

	private lateinit var viewModel: GameViewModel

	// region Gesture Properties
	private var xOffset = 0.0
	private var yOffset = 0.0
	private val minimumOffset = 60.0
	// endregion

	// region UI Properties
	private var _binding: FragmentGameBinding? = null
	private val binding get() = _binding!!

	private var _holder: SurfaceHolder? = null
	private val holder get() = _holder!!

	private var canvasWidth = 0
	private var canvasHeight = 0
	private var canvasMargin = 0

	private var showNext = true
	private var showHold = false
	// endregion

	// region Game Play Properties

	// endregion

	// region Game Play State Properties
	private var isPaused = false

	// endregion

	companion object {
		private const val PIECES_KEY = "pieces"
		private const val NEXT_KEY = "nextQueue"
		private const val HOLD_KEY = "holdPiece"
		private const val SCORE_KEY = "currentScore"

		private const val PIECES_PER_SIDE = 10
	}

	// region Lifecycle Callbacks
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		GamePiece.resources = resources

		val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
		val speed = when (prefs.getString("speed", "")) {
			"1" -> GameSpeed.SLOW
			"2" -> GameSpeed.MEDIUM
			"3" -> GameSpeed.FAST
			else -> {
				// Invalid preference state, fix state and default to SLOW
				prefs.edit()
					.putString("string", "1")
					.apply()
				GameSpeed.SLOW
			}
		}

		showNext = prefs.getBoolean("next", showNext)
		showHold = prefs.getBoolean("hold", showHold)

		val pieces = ArrayDeque<GamePiece>()
		val nextQueue = ArrayDeque<GamePiece>()
		var holdPiece: GamePiece? = null

		if (savedInstanceState != null) {
			savedInstanceState.getParcelableArrayList<GamePiece>(PIECES_KEY)?.let { pieces.addAll(it) }
			savedInstanceState.getParcelableArrayList<GamePiece>(NEXT_KEY)?.let { nextQueue.addAll(it) }
			holdPiece = savedInstanceState.getParcelable(HOLD_KEY)
		}

		val highscoreRepository = HighscoreRepository.getInstance(requireContext())

		val score = savedInstanceState?.getInt(SCORE_KEY, 0) ?: 0

		viewModel = GameViewModel(
			highscoreRepository,
			this,
			speed,
			holdPiece,
			pieces,
			nextQueue,
			score,
			PIECES_PER_SIDE * 2 + 1
		)
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
		}

		binding.gameView.holder.addCallback(this)
		val detector = GestureDetectorCompat(requireContext(), this)
		binding.gameView.setOnTouchListener { _, event ->
			if (!isPaused) {
				detector.onTouchEvent(event)
			} else true
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

	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		viewModel.saveToBundle(outState)
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
		binding.gameView.removeCallbacks(viewModel.pieceMover)
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

		viewModel.resume()
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
			viewModel.swapHoldPiece()
		}
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

			GamePiece.blockSize = canvasWidth / (PIECES_PER_SIDE * 2 + 1)
			// make sure canvas is a multiple of block size so pieces are aligned
			canvasMargin = canvasWidth % GamePiece.blockSize
			canvasWidth -= canvasMargin
			canvasHeight -= canvasMargin

			viewModel.canvasWidth = canvasWidth
			viewModel.canvasHeight = canvasHeight

			// add "planet" to center
			viewModel.makePlanetBlock(resources)

			viewModel.calculateRings()
			// when not restoring from saved instance state
			if (!isPaused) {
				viewModel.makeNextQueue()
				viewModel.addNextPiece()
			}
		}
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		viewModel.onPiecesChanged()
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		_holder = null
		pause()
	}
	// endregion

	//region Gameplay Methods
	override fun drawPieces(planetBlock: BlockDrawable, pieces: ArrayDeque<GamePiece>) {
		val canvas = holder.lockCanvas()
		// reset the canvas
		canvas.drawColor(Color.BLACK)

		// Valid

		val placeableRect = Rect(
			GamePiece.blockSize * 4,
			GamePiece.blockSize * 4,
			(PIECES_PER_SIDE * 2 - 3) * GamePiece.blockSize,
			(PIECES_PER_SIDE * 2 - 3) * GamePiece.blockSize
		)

		val placeablePaint = Paint()
			.apply {
				color = Color.WHITE
				alpha = 32
			}

		canvas.drawRect(placeableRect, placeablePaint)

		planetBlock.draw(canvas)
		// draw previously placed pieces
		pieces.dropLast(1).forEach { it.drawBlocks(canvas) }
		// ensure there is space for the current piece
		val piece = pieces.last()
//		if (piece.blocks.filterNotNull().any { !piece.contains(it.x, it.y) }) {
//			// if there is no space for the piece, throw an exception to indicate the game is over
//			holder.unlockCanvasAndPost(canvas)
//			throw Exception()
//		}
		piece.drawBlocks(canvas)
		holder.unlockCanvasAndPost(canvas)
	}

//	private fun tryDrawPieces() {
//		try {
//			drawPieces()
//		} catch (e: Exception) {
//			onGameEnded()
//		}
//	}
	// endregion

	// region GestureDetector Callbacks
	override fun onDown(e: MotionEvent?): Boolean {
		return true
	}

	override fun onShowPress(e: MotionEvent?) {
		return
	}

	override fun onSingleTapUp(e: MotionEvent?): Boolean {
		viewModel.rotatePiece()
		viewModel.onPiecesChanged()
		return true
	}

	override fun onScroll(
		e1: MotionEvent?,
		e2: MotionEvent?,
		distanceX: Float,
		distanceY: Float
	): Boolean {
		xOffset += distanceX
		yOffset += distanceY

		val direction = viewModel.pieceDirection

		when (direction) {
			PieceDirection.UP, PieceDirection.DOWN -> {
				if (xOffset > minimumOffset) {
					xOffset = 0.0
					viewModel.movePieceInDirection(PieceDirection.LEFT)
				} else if (-xOffset > minimumOffset) {
					xOffset = 0.0
					viewModel.movePieceInDirection(PieceDirection.RIGHT)
				}
			}
			PieceDirection.LEFT, PieceDirection.RIGHT -> {
				if (yOffset > minimumOffset) {
					yOffset = 0.0
					viewModel.movePieceInDirection(PieceDirection.UP)
				} else if (-yOffset > minimumOffset) {
					yOffset = 0.0
					viewModel.movePieceInDirection(PieceDirection.DOWN)
				}
			}
		}

		when (direction) {
			PieceDirection.UP -> {
				if (yOffset > minimumOffset) {
					yOffset = 0.0
					viewModel.movePiece()
				}
			}
			PieceDirection.DOWN -> {
				if (-yOffset > minimumOffset) {
					yOffset = 0.0
					viewModel.movePiece()
				}
			}
			PieceDirection.LEFT -> {
				if (xOffset > minimumOffset) {
					xOffset = 0.0
					viewModel.movePiece()
				}
			}
			PieceDirection.RIGHT -> {
				if (-xOffset > minimumOffset) {
					xOffset = 0.0
					viewModel.movePiece()
				}
			}
		}

		viewModel.onPiecesChanged()

		return true
	}

	override fun onLongPress(e: MotionEvent?) {
		viewModel.fastMove()
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

	override fun drawNextQueue(queue: ArrayDeque<GamePiece>) {
		binding.firstNextImageView.setImageResource(queue[0].shape.iconId)
		binding.secondNextImageView.setImageResource(queue[1].shape.iconId)
		binding.thirdNextImageView.setImageResource(queue[2].shape.iconId)
	}

	override fun drawScore(score: Int) {
		binding.scoreLabel.text = score.toString()
	}

	override fun drawHoldPiece(piece: GamePiece?) {
		binding.holdImageView.visibility = when (piece) {
			null -> View.INVISIBLE
			else -> {
				binding.holdImageView.setImageResource(piece.shape.iconId)
				View.VISIBLE
			}
		}
	}

	override fun onGameEnded() {
		pause()
		binding.resumeButton.visibility = View.GONE
		binding.gameOverTextView.visibility = View.VISIBLE
	}

	override fun gameViewPostDelayed(action: Runnable, delayMillis: Long) {
		binding.gameView.postDelayed(action, delayMillis)
	}
}
