package edu.mines.csci448.planetattack.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import edu.mines.csci448.planetattack.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: MainActivityBinding

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		binding = MainActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)
		NavigationUI.setupActionBarWithNavController(this, (supportFragmentManager
			.findFragmentById(binding.navHostFragment.id) as NavHostFragment).navController)
	}

	override fun onSupportNavigateUp() = findNavController(binding.navHostFragment.id).navigateUp()
						|| super.onSupportNavigateUp()

	// https://stackoverflow.com/a/64828028
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (hasFocus) {
			//supportActionBar?.hide()
			WindowCompat.setDecorFitsSystemWindows(window, false)
			WindowInsetsControllerCompat(window, binding.root).let { controller ->
				controller.hide(WindowInsetsCompat.Type.systemBars())
				controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			}
		}
	}
}
