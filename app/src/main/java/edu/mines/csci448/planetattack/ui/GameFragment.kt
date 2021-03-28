package edu.mines.csci448.planetattack.ui

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.BuildConfig
import edu.mines.csci448.planetattack.GameSpeed
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.*
import kotlin.reflect.full.createInstance

class GameFragment : Fragment(), BackPressListener, SurfaceHolder.Callback {
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
			drawPieces()
			binding.gameView.postDelayed(this, speed.dropDelayMillis)
		}
	}

	private var isPaused = false
	private val pieces = ArrayDeque<GamePiece>()
	private val nextQueue = ArrayDeque<GamePiece>()
	private lateinit var speed: GameSpeed
	private lateinit var planetBlock: BlockDrawable

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

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		(requireActivity() as AppCompatActivity).supportActionBar?.hide()
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()
		resume()
		binding.gameView.holder.addCallback(this)
		// TODO make sure click doesn't register when game is paused
		binding.gameView.setOnClickListener { rotatePiece() }
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

	private fun drawPieces() {
		val canvas = holder.lockCanvas()
		// reset the canvas
		canvas.drawColor(Color.BLACK)
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

	private fun movePiece() {
		val piece = pieces.last()
		if (!piece.direction.move(piece)) addNextPiece()
	}

	private fun rotatePiece() {
		val piece = pieces.last()
		piece.shape.createLayout((piece.shape.rotation + PieceShape.ROTATION_90) % PieceShape.ROTATION_360)
	}
}
