package topcomponents;

import java.awt.image.BufferedImage;

import ide.main.Main;
import screen.Screen;

public class CloseWindow extends TopComponent {

	public CloseWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT;
		
		if (leftClicked())
			Main.closeForced(0);
	}

}
