package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
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

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
		val view = inflater.inflate(R.layout.fragment_score_list, container, false)

		// Set the adapter
		if (view is RecyclerView) {
			with(view) {
				layoutManager = LinearLayoutManager(context)
				adapter = ScoreAdapter(DummyContent.ITEMS)
			}
		}
		return view
	}
}
