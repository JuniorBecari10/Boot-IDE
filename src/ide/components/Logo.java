package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class Logo extends IDEComponent {
	
	private boolean showMessage1 = true;
	private boolean show = true;

	public Logo(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		super.tick();
		
		if (Main.baseFolder != null)
			showMessage1 = false;
		else
			showMessage1 = true;
		
		show = Main.editor.editing == null;
		
		if (!CommandTerminal.expOff) {
			x = (int) (Main.screen.getWidth() / 2 + 80);
			y = (int) (Main.screen.getHeight() / 2 - 120);
		}
		else {
			x = (int) ((Main.screen.getWidth() / 2) - width / 2);
			y = (int) (Main.screen.getHeight() / 2 - 120);
		}
	}
	
	public void render(Graphics g) {
		if (Main.screen.getWidth() < 690) return;
		if (!show) return;
		
		super.render(g);
		
		if (showMessage1) {
			g.setColor(Colors.explorer);
			
			g.fillRect(x + 47, y + 218, Main.lang == Language.PORT ? 17 * 20 + 10 : 15 * 20 + 5, 27);
			
			Fonts.drawString(Texts.noFolderLoadedLogoText, Main.lang == Language.PORT ? x - 140 : x - 100, y + 170, new IDEFont(Fonts.lighterGrayNormal, 20), g);
			Fonts.drawString(Texts.clickOnButton, x - 190, y + 220, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString(Texts.loadOne, Main.lang == Language.PORT ? x - 50 : x - 10, y + 250, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
		else {
			g.setColor(Colors.explorer);
			
			g.fillRect(x - 145, y + 189, 14 * 20 - 2, 25);
			g.fillRect(x - 145, y + 219, 8 * 20 - 5, 25);
			g.fillRect(x - 145, y + 249, Main.lang == Language.PORT ? 12 * 20 + 5 : 10 * 20, 25);
			
			Fonts.drawString(Texts.ctrl_Win_Prompt, x - 140, y + 190, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString(Texts.ctrl_T_terminal, x - 140, y + 220, new IDEFont(Fonts.lightGrayNormal, 20), g);
			Fonts.drawString(Texts.rightClick_Options, x - 140, y + 250, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
