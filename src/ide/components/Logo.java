package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.codeeditor.CodeEditor;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class Logo extends IDEComponent {
	
	private boolean showMessage1 = true;
	private boolean showTexts = true;
	private boolean show = true;
	
	private int initialWidth, initialHeight;

	public Logo(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		initialWidth = width;
		initialHeight = height;
	}
	
	public void tick() {
		super.tick();
		
		if (Main.baseFolder != null)
			showMessage1 = false;
		else
			showMessage1 = true;
		
		show = Main.editor.editing == null;
		showTexts = x + 350 < Main.screen.getWidth();
		
		if (!CommandTerminal.expOff) {
			x = (int) (Main.screen.getWidth() / 2 + 80);
			y = (int) (Main.screen.getHeight() / 2 - 120);
		}
		else {
			x = (int) ((Main.screen.getWidth() / 2) - width / 2);
			y = (int) (Main.screen.getHeight() / 2 - 120);
		}
		
		if (x <= Main.explorer.getWidth() + 230) // 290
			x = Main.explorer.getWidth() + 230;
		
		if (Main.screen.getWidth() < 690) {
			x = ((Main.screen.getWidth() / 2) - (width / 2)) + (Main.explorer.getWidth() / 2) + 5;
			
			int size = CodeEditor.ruleOf3(100, 50, ((Main.screen.getWidth() / 2) - (width / 2)));
			
			width = size;
			height = size;
		}
		else {
			width = initialWidth;
			height = initialHeight;
		}
	}
	
	public void render(Graphics g) {
		if (!show) return;
		
		super.render(g);
		
		if (Main.screen.getWidth() < 690) return;
		if (!showTexts) return;
		
		if (showMessage1) {
			g.setColor(Colors.explorer);
			
			g.fillRect(Main.lang == Language.PORT ? x + 34 : x - 26, y + 216, Main.lang == Language.PORT ? 17 * 16 + 10 : 15 * 16 + 5, 27);
			
			Fonts.drawString(Texts.noFolderLoadedLogoText, (x + width / 2) - ((Texts.noFolderLoadedLogoText.length() * 12) / 2), y + 180, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
			Fonts.drawString(Texts.clickTheButton, (x + width / 2) - ((Texts.clickTheButton.length() * 12) / 2), y + 220, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
			Fonts.drawString(Texts.loadOne, (x + width / 2) - ((Texts.loadOne.length() * 12) / 2), y + 250, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		}
		else {
			g.setColor(Colors.explorer);
			
			g.fillRect(x - 145, y + 189, 15 * 16 + 6, 25);
			//g.fillRect(x - 145, y + 219, 8 * 16 - 5, 25);
			//g.fillRect(x - 145, y + 249, Main.lang == Language.PORT ? 12 * 16 + 5 : 10 * 16, 25);
			
			Fonts.drawString(Texts.ctrl_Win_Prompt, (x + width / 2) - ((Texts.ctrl_Win_Prompt.length() * 12) / 2), y + 190, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
			Fonts.drawString(Texts.ctrl_T_terminal, (x + width / 2) - ((Texts.ctrl_Win_Prompt.length() * 12) / 2), y + 220, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
			Fonts.drawString(Texts.rightClick_Options, (x + width / 2) - ((Texts.ctrl_Win_Prompt.length() * 12) / 2), y + 250, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		}
	}
}
