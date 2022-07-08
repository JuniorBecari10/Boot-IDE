package topcomponents;

import java.awt.DisplayMode;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ide.main.Main;
import screen.Screen;

public class MaximizeWindow extends TopComponent {

	public MaximizeWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		x = Main.screen.getWidth() - Screen.DECORATION_HEIGHT * 2;
		
		if (Main.screen.frame.getBounds().equals(new Rectangle(0, 0, Main.toolkit.getScreenSize().width, Main.toolkit.getScreenSize().height)))
			sprite = Main.maximizedWindowSpr;
		else
			sprite = Main.maximizeWindowSpr;
		
		if (leftClicked()) {
			Main.screen.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
			
			Rectangle bounds = Main.screen.frame.getBounds();
			DisplayMode mode = Main.screen.frame.getGraphicsConfiguration().getDevice().getDisplayMode();
			
			Main.screen.frame.setBounds(bounds.x, bounds.y, mode.getWidth(), mode.getHeight());
			
			/*boolean isMaximized = Main.screen.frame.getState() == JFrame.MAXIMIZED_BOTH | Main.screen.maximized;
			
			if (!isMaximized) {
				Main.screen.frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
				
				DisplayMode mode = Main.screen.frame.getGraphicsConfiguration().getDevice().getDisplayMode();
				
				Main.screen.frame.setBounds(new Rectangle(1, 1, mode.getWidth(), mode.getHeight()));
				Main.screen.maximized = true;
			}
			
			else {
				Main.screen.frame.setState(JFrame.NORMAL);
				
				Main.screen.frame.setBounds(new Rectangle(0, 0, Screen.WIDTH, Screen.HEIGHT));
				Main.screen.maximized = false;
			}*/
		}
	}

}
