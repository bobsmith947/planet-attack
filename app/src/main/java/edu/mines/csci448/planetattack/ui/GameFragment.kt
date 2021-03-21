package edu.mines.csci448.planetattack.ui

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.GameSpeed
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.graphics.GamePiece
import edu.mines.csci448.planetattack.graphics.PieceShape

class GameFragment : Fragment(), BackPressListener, SurfaceHolder.Callback {
	private var _binding: FragmentGameBinding? = null
	private val binding get() = _binding!!

	private var _holder: SurfaceHolder? = null
	private val holder get() = _holder!!

	private val handler = Handler(Looper.getMainLooper())
	private val pieceMover = object : Runnable {
		override fun run() {
			movePiece()
			drawPieces()
			handler.postDelayed(this, speed.dropDelayMillis)
		}

	}

	private var isPaused = false
	private val pieces = ArrayDeque<GamePiece>()
	private lateinit var speed: GameSpeed

	override fun onActivityCreated(savedInstanceState: Bundle?) {
		super.onActivityCreated(savedInstanceState)
		(requireActivity() as AppCompatActivity).supportActionBar?.hide()
		addPieces()
		val prefs = PreferenceManager.getDefaultSharedPreferences(requireActivity())
		speed = when (prefs.getString("speed", "")) {
			"1" -> GameSpeed.SLOW
			"2" -> GameSpeed.MEDIUM
			"3" -> GameSpeed.FAST
			else -> throw IllegalStateException()
		}
		resume()
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()
		binding.gameView.holder.addCallback(this)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		binding.gameView.holder.removeCallback(this)
		_binding = null
		(requireActivity() as AppCompatActivity).supportActionBar?.show()
	}

	override fun onBackPressed() {
		pause()
	}

	private fun pause() {
		binding.menuOverlay.visibility = View.VISIBLE
		binding.resumeButton.visibility = View.VISIBLE
		binding.quitButton.visibility = View.VISIBLE
		handler.removeCallbacks(pieceMover)
	}

	private fun resume() {
		binding.menuOverlay.visibility = View.INVISIBLE
		binding.resumeButton.visibility = View.INVISIBLE
		binding.quitButton.visibility = View.INVISIBLE
		handler.postDelayed(pieceMover, speed.dropDelayMillis)
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
		pieces.forEach { it.drawBlocks(canvas) }
		holder.unlockCanvasAndPost(canvas)
	}

	private fun addPieces() {
		val res = resources
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

	private fun movePiece() {
		pieces.last().moveDown()
	}
}
