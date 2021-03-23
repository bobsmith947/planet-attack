package edu.mines.csci448.planetattack.graphics

import androidx.annotation.DrawableRes
import edu.mines.csci448.planetattack.R

enum class PieceShape(@DrawableRes val iconId: Int) {
	I(R.drawable.block_icon_i) {
		override val width: Int
			get() = 1
		override val height: Int
			get() = 4
	},
	J(R.drawable.block_icon_j) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 3
	},
	L(R.drawable.block_icon_l) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 3
	},
	O(R.drawable.block_icon_o) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 2
	},
	S(R.drawable.block_icon_s) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 3
	},
	T(R.drawable.block_icon_t) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 3
	},
	Z(R.drawable.block_icon_z) {
		override val width: Int
			get() = 2
		override val height: Int
			get() = 3
	};

	// the width and height of the shape in terms of block units
	abstract val width: Int
	abstract val height: Int
}
