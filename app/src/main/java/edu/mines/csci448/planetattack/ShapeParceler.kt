package edu.mines.csci448.planetattack

import android.os.Parcel
import edu.mines.csci448.planetattack.graphics.PieceShape
import kotlinx.parcelize.Parceler

object ShapeParceler : Parceler<PieceShape> {
	override fun create(parcel: Parcel): PieceShape {
		val shape = (parcel.readSerializable() as Class<*>).newInstance() as PieceShape
		shape.createLayout(parcel.readInt())
		return shape
	}

	override fun PieceShape.write(parcel: Parcel, flags: Int) {
		parcel.writeSerializable(this::class.java)
		parcel.writeInt(currentRotation)
	}
}
