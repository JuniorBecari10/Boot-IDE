package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class SettingsButton extends IDEComponent {

	public SettingsButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		x = Main.explorer.getWidth() - 34;
		
		if (leftClicked())
			CommandTerminal.runCommand("settings");
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff) return;
		
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, y, width, height);
		}
		
		super.render(g);
		
		if (hovered()) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, Texts.settings.length() * 16, 28);
			
			Fonts.drawString(Texts.settings, MouseInput.getMouseX() - 20, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
