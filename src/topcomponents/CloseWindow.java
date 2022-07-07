package topcomponents;

import java.awt.image.BufferedImage;

import ide.main.Main;

public class CloseWindow extends TopComponent {

	public CloseWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (leftClicked())
			Main.main.closeWindow();
	}

}
