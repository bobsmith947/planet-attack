package edu.mines.csci448.planetattack

/**
 * A simple interface to override the default behavior of the back button.
 * This is necessary because [android.app.Activity.onBackPressed] is not available in fragments.
 * Fragments which implement this interface will be notified by the parent activity when the back button is pressed.
 * [See this Stack Overflow post for more information.](https://stackoverflow.com/a/46425415)
 */
interface BackPressListener {
	fun onBackPressed()
}
