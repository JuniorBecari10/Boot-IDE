package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class ExplorerTab extends IDEComponent {
	
	public static final int Y = Screen.DECORATION_HEIGHT + 3;
	public static final int SIZE = 35;
	
	public ExplorerMode regent;
	public String name;
	//public int nameBgWidth;

	public ExplorerTab(int x, BufferedImage sprite, ExplorerMode regent, String name) {
		super(x, Y, SIZE, SIZE, sprite);
		
		this.regent = regent;
		this.name = name;
		//this.nameBgWidth = nameBgWidth;
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
		
		if (hovered()) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, name.length() * 16, 28);
			
			Fonts.drawString(name, MouseInput.getMouseX() - 20, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
