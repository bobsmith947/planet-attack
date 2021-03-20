package edu.mines.csci448.planetattack.view

import android.content.res.Resources
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable

class BlockDrawable(val color: BlockColor, val shape: BlockShape, resources: Resources) :
	BitmapDrawable(resources, BitmapFactory.decodeResource(resources, color.blockId)) {

		init {
			setBounds(0, 0, 100, 100)
		}
}
