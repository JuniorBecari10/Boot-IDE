package topcomponents;

import java.awt.image.BufferedImage;

import ide.main.Main;
import ide.main.OS;
import screen.Screen;

public class CloseWindow extends TopComponent {

	public CloseWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT;
		
		if ((Main.forceMacButtons || Main.os == OS.MAC) && hovered())
			sprite = Main.closeWindowHoverSpr;
		else if ((Main.forceMacButtons || Main.os == OS.MAC) && !hovered())
			sprite = Main.closeWindowSpr;
		
		if (leftClicked())
			Main.closeForced(0);
	}

}
