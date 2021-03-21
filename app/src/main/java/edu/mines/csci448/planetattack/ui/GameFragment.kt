package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.FragmentGameBinding
import edu.mines.csci448.planetattack.view.GamePiece
import edu.mines.csci448.planetattack.view.PieceShape

class GameFragment : Fragment(), BackPressListener, SurfaceHolder.Callback {
	private var _binding: FragmentGameBinding? = null
	val binding get() = _binding!!

	private var isPaused = false
	private val pieces = ArrayDeque<GamePiece>()

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = FragmentGameBinding.inflate(inflater, container, false)
		setButtonOnClickListeners()
		resume()

		(requireActivity() as AppCompatActivity).supportActionBar?.hide()

		addPieces()
		binding.gameView.holder.addCallback(this)

		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
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
	}

	private fun resume() {
		binding.menuOverlay.visibility = View.INVISIBLE
		binding.resumeButton.visibility = View.INVISIBLE
		binding.quitButton.visibility = View.INVISIBLE
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
		drawPieces(holder)
	}

	override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
		drawPieces(holder)
	}

	override fun surfaceDestroyed(holder: SurfaceHolder) {
		drawPieces(holder)
	}

	private fun drawPieces(holder: SurfaceHolder) {
		val canvas = holder.lockCanvas()
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
}
