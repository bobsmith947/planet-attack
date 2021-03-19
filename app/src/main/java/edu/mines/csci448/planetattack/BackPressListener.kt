package edu.mines.csci448.planetattack

/**
 * A simple interface to override the default behavior of the back button.
 * This is necessary because the Android <code>onBackPressed()</code> method is not available in fragments.
 * Fragments which implement this interface will be notified by the parent activity when the back button is pressed.
 * <a href="https://stackoverflow.com/a/46425415">See this Stack Overflow post for more information.</a>
 */
interface BackPressListener {
	fun onBackPressed()
}
