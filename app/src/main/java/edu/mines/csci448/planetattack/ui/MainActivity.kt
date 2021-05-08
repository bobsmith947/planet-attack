package edu.mines.csci448.planetattack.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import edu.mines.csci448.planetattack.BackPressListener
import edu.mines.csci448.planetattack.SoundManager
import edu.mines.csci448.planetattack.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {
	private lateinit var binding: MainActivityBinding
	private val navHost get() = supportFragmentManager.findFragmentById(binding.navHostFragment.id) as NavHostFragment

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		SoundManager.makeShared(assets)
		binding = MainActivityBinding.inflate(layoutInflater)
		setContentView(binding.root)
		NavigationUI.setupActionBarWithNavController(this, navHost.navController)
	}

	override fun onSupportNavigateUp() = findNavController(binding.navHostFragment.id).navigateUp()
						|| super.onSupportNavigateUp()

	// https://stackoverflow.com/a/64828028
	override fun onWindowFocusChanged(hasFocus: Boolean) {
		super.onWindowFocusChanged(hasFocus)
		if (hasFocus) {
			WindowCompat.setDecorFitsSystemWindows(window, false)
			WindowInsetsControllerCompat(window, binding.root).let { controller ->
				controller.hide(WindowInsetsCompat.Type.systemBars())
				controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
			}
		}
	}

	override fun onBackPressed() {
		val fragment = navHost.childFragmentManager.primaryNavigationFragment
		if (fragment is BackPressListener) fragment.onBackPressed()
		else super.onBackPressed()
	}
}
