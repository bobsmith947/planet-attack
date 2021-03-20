package edu.mines.csci448.planetattack.view

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

class BlockDrawable(var x: Int, var y: Int, color: BlockColor, resources: Resources) :
	BitmapDrawable(resources, BitmapFactory.decodeResource(resources, color.blockId)) {

		init {
			setBounds(x, y, x + GamePiece.blockSize, y + GamePiece.blockSize)
		}
}
