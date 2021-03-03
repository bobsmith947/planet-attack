package edu.mines.csci448.planetattack.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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
}
