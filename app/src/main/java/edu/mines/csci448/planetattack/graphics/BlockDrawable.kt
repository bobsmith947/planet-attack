package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.drawable.BitmapDrawable

class BlockDrawable(color: BlockColor, resources: Resources, val piece: GamePiece?) :
	BitmapDrawable(resources, color.getBitmap(resources)) {
	var x = 0
		private set
	var y = 0
		private set

	fun setBounds(x: Int, y: Int) {
		super.setBounds(x, y, x + GamePiece.blockSize, y + GamePiece.blockSize)
		this.x = x
		this.y = y
	}
}
