package topcomponents;

import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import ide.main.Main;

public class MaximizeWindow extends TopComponent {

	public MaximizeWindow(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (leftClicked()) {
			boolean isMaximized = Main.screen.frame.getState() == JFrame.MAXIMIZED_BOTH;
			
			if (isMaximized)
				Main.screen.frame.setExtendedState(Main.screen.frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
			else
				Main.screen.frame.setState(JFrame.ICONIFIED);
		}
	}

}
