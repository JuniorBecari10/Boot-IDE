package topcomponents;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ide.main.Main;
import ide.main.Screen;

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
			boolean isMaximized = Main.screen.frame.getState() == JFrame.MAXIMIZED_BOTH;
			
			if (!isMaximized) {
				Main.screen.frame.setExtendedState(Main.screen.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
				
				Main.screen.frame.setBounds(new Rectangle(0, 0, Main.toolkit.getScreenSize().width, Main.toolkit.getScreenSize().height));
			}
			
			else {
				Main.screen.frame.setState(JFrame.NORMAL);
				
				Main.screen.frame.setBounds(new Rectangle(0, 0, Screen.WIDTH, Screen.HEIGHT));
			}
		}
	}

}
