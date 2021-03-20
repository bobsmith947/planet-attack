package edu.mines.csci448.planetattack.view

import androidx.annotation.DrawableRes
import edu.mines.csci448.planetattack.R

enum class PieceShape(@DrawableRes val iconId: Int) {
	I(R.drawable.block_icon_i),
	J(R.drawable.block_icon_j),
	L(R.drawable.block_icon_l),
	O(R.drawable.block_icon_o),
	S(R.drawable.block_icon_s),
	T(R.drawable.block_icon_t),
	Z(R.drawable.block_icon_z);
}
