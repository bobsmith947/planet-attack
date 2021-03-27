package edu.mines.csci448.planetattack.graphics

import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.DrawableRes
import edu.mines.csci448.planetattack.R

enum class BlockColor(@DrawableRes val blockId: Int) {
	BLUE(R.drawable.block_blue),
	CYAN(R.drawable.block_cyan),
	GRAY(R.drawable.block_gray),
	GREEN(R.drawable.block_green),
	MAGENTA(R.drawable.block_magenta),
	ORANGE(R.drawable.block_orange),
	RED(R.drawable.block_red),
	YELLOW(R.drawable.block_yellow);

	private lateinit var blockBitmap: Bitmap
	fun getBitmap(resources: Resources): Bitmap {
		if (!this::blockBitmap.isInitialized) {
			blockBitmap = BitmapFactory.decodeResource(resources, blockId)
		}
		return blockBitmap
	}
}
