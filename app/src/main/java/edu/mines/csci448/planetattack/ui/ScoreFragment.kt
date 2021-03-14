package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.ui.dummy.DummyContent

/**
 * A fragment representing a list of Items.
 */
class ScoreFragment : Fragment() {

	private var columnCount = 1

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)

		arguments?.let {
			columnCount = it.getInt(ARG_COLUMN_COUNT)
		}
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
							  savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_score_list, container, false)

		// Set the adapter
		if (view is RecyclerView) {
			with(view) {
				layoutManager = when {
					columnCount <= 1 -> LinearLayoutManager(context)
					else -> GridLayoutManager(context, columnCount)
				}
				adapter = ScoreAdapter(DummyContent.ITEMS)
			}
		}
		return view
	}

	companion object {

		// TODO: Customize parameter argument names
		const val ARG_COLUMN_COUNT = "column-count"

		// TODO: Customize parameter initialization
		@JvmStatic
		fun newInstance(columnCount: Int) =
				ScoreFragment().apply {
					arguments = Bundle().apply {
						putInt(ARG_COLUMN_COUNT, columnCount)
					}
				}
	}
}
