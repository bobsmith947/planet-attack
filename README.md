# Planet Attack

This is a Tetris-like puzzle game where the blocks fall towards the center of the screen, and you need to form a complete ring in order to clear blocks. Select "How to Play" on the main menu for more information on how the game works.

The app uses immersive fullscreen mode. You can pull down from the top of the screen, or pull up from the bottom of the screen in order to reveal the system bars.

We would like feedback on the gameplay, and how easy the controls are to use. If possible, we would like to know how the game plays on a physical device, as only the emulator has been used for testing so far.

Addressed feedback:
* The default behavior of the back button was overriden inside the game fragment, to prevent the player from accidentally quitting and losing progress.
* The gameplay area was kept as a 1:1 display ratio, as extending it vertically would not only make the gameplay logic more complex, but it would also negatively impact the gameplay by making pieces that spawn on the left and right harder to manage than pieces that spawn on the top and bottom.
* The areas above and below the gameplay area were kept as a white background in order to provide contrast from where the gameplay takes place.
