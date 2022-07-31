package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class SettingsButton extends IDEComponent {

	public SettingsButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (leftClicked())
			CommandTerminal.runCommand("settings");
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, y, width, height);
		}
		
		super.render(g);
		
		// continuar
		if (hovered()) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 47, MouseInput.getMouseY() + 27, Main.lang == Language.PORT ? 285 : 240, 28);
			
			Fonts.drawString(Texts.createFile, MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
