package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import edu.mines.csci448.planetattack.R
import edu.mines.csci448.planetattack.databinding.MainFragmentBinding

class MainFragment : Fragment() {
	private var _binding: MainFragmentBinding? = null
	private val binding get() = _binding!!

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setHasOptionsMenu(true)
	}

	override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
		super.onCreateOptionsMenu(menu, inflater)
		inflater.inflate(R.menu.main_menu, menu)
	}

	override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
		R.id.open_settings_menu_item -> {
			findNavController().navigate(MainFragmentDirections.actionMainFragmentToSettingsFragment())
			true
		}
		R.id.exit_app_menu_item -> {
			requireActivity().finishAndRemoveTask()
			exitProcess(0)
		}
		else -> super.onOptionsItemSelected(item)
	}

	override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
		_binding = MainFragmentBinding.inflate(inflater, container, false)
		return binding.root
	}

	override fun onDestroyView() {
		super.onDestroyView()
		_binding = null
	}
}
