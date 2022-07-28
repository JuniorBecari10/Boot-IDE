package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.screen.Screen;
import ide.util.Colors;

public class ExplorerTab extends IDEComponent {
	
	public static final int Y = Screen.DECORATION_HEIGHT + 3;
	public static final int SIZE = 35;
	
	public ExplorerMode regent;

	public ExplorerTab(int x, BufferedImage sprite, final ExplorerMode regent) {
		super(x, Y, SIZE, SIZE, sprite);
		
		this.regent = regent;
	}
	
	public void select() {
		// it does nothing, you have to implement
		//Explorer.explorerMode = regent;
	}
	
	public void tick() {
		if (leftClicked())
			select();
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Color c = Explorer.explorerMode == regent ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() ? Colors.explorerLight : Colors.codeEditor;

		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x, Y, SIZE, SIZE);

		g.setColor(c);
		g.drawRect(x, Y, SIZE, SIZE);
		
		final int imageSize = 32;
		g.drawImage(sprite, x + ((SIZE / 2) - (imageSize / 2)), Y + ((SIZE / 2) - (imageSize / 2)), imageSize, imageSize, null);
	}
}
