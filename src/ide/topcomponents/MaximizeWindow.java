package ide.topcomponents;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ide.input.WindowInput;
import ide.main.Main;
import ide.main.OS;
import ide.screen.Screen;

public class MaximizeWindow extends TopComponent {

	public MaximizeWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public static void maximize() {
		if (Main.screen.frame.getExtendedState() != (Main.screen.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH)) {
			GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
			Main.screen.frame.setMaximizedBounds(env.getMaximumWindowBounds());
			Main.screen.frame.setExtendedState(Main.screen.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
		}
		else {
			Main.screen.frame.setExtendedState(JFrame.NORMAL);
			Main.screen.frame.setBounds(Main.screen.frame.getBounds().x, Main.screen.frame.getBounds().y, Screen.WIDTH, Screen.HEIGHT);
		}
	}
	
	public void tick() {
		if (!Main.forceMacButtons && Main.os != OS.MAC)
			x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT * 2;
		else
			x = Screen.DECORATION_HEIGHT * 2;
		
		// click logic in MouseInput
		
		if ((Main.forceMacButtons || Main.os == OS.MAC) && anyTopComponentHovered())
			sprite = Main.maximizeWindowHoverSpr;
		else if ((Main.forceMacButtons || Main.os == OS.MAC) && !anyTopComponentHovered())
			sprite = Main.maximizeWindowMacSpr;
		else
			sprite = Main.maximizeWindowSpr;
		
		if (WindowInput.isDeactivated() && (Main.forceMacButtons || Main.os == OS.MAC))
			sprite = Main.deactivatedMacButtons;
		
		if (leftClicked()) {
			maximize();
		}
	}

}
