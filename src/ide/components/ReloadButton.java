package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class ReloadButton extends IDEComponent {

	public ReloadButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		x = Main.explorer.getWidth() - 40;
		
		if (x < (Main.returnBase.getX() + Main.returnBase.getWidth()) + 2) x = (Main.returnBase.getX() + Main.returnBase.getWidth()) + 2;
		
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			Explorer.files.clear();
			ListableFile.files.clear();
			
			Explorer.files = ListableFile.loadFolder(Explorer.scope);
		}
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff) return;
		
		if (Main.baseFolder == null) toRemove.add(this);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 47, MouseInput.getMouseY() + 27, Main.lang == Language.PORT ? 160 : 110, 28);
			
			Fonts.drawString(Texts.reload, MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
