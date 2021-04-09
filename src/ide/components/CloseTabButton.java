package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.codeeditor.Tab;

public class CloseTabButton extends IDEComponent {
	
	private Tab regent;

	public CloseTabButton(int x, int y, int width, int height, BufferedImage sprite, Tab regent) {
		super(x, y, width, height, sprite);
		
		this.regent = regent;
	}
	
	public void tick() {
		if (leftClicked())
			regent.close();
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Color.decode("#354d69"));
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
	}
}
