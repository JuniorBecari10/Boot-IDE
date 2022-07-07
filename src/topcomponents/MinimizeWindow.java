package topcomponents;

import java.awt.Frame;
import java.awt.image.BufferedImage;

import ide.main.Main;
import ide.main.Screen;

public class MinimizeWindow extends TopComponent {

	public MinimizeWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT * 3;
		
		if (leftClicked())
			Main.screen.frame.setState(Frame.ICONIFIED);
	}

}
