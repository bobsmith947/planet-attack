package edu.mines.csci448.planetattack.view

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.view.SurfaceView

class GameView(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs) {
	private val pieces = ArrayDeque<BlockDrawable>()

	init {
		pieces.add(BlockDrawable(BlockColor.BLUE, BlockShape.I, context.resources))
	}

	override fun draw(canvas: Canvas?) {
		super.draw(canvas)
		pieces.forEach {
			if (canvas != null) {
				it.draw(canvas)
			}
		}
	}
}
