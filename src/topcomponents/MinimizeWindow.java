package topcomponents;

import java.awt.Frame;
import java.awt.image.BufferedImage;

import ide.main.Main;
import ide.main.OS;
import screen.Screen;

public class MinimizeWindow extends TopComponent {

	public MinimizeWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (!Main.forceMacButtons && Main.os != OS.MAC)
			x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT * 3;
		else
			x = Screen.DECORATION_HEIGHT;
		
		if ((Main.forceMacButtons || Main.os == OS.MAC) && hovered())
			sprite = Main.minimizeWindowHoverSpr;
		else if ((Main.forceMacButtons || Main.os == OS.MAC) && !hovered())
			sprite = Main.minimizeWindowSpr;
		
		if (leftClicked())
			Main.screen.frame.setState(Frame.ICONIFIED);
	}

}
