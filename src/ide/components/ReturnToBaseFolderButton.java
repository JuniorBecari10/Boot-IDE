package ide.components;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class ReturnToBaseFolderButton extends IDEComponent {

	public ReturnToBaseFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode == ExplorerMode.SEARCHREPLACE) return;
		
		x = Main.explorer.getWidth() - 80;
		
		if (x < (Main.oneFolder.getX() + Main.oneFolder.getWidth()) + 2) x = (Main.oneFolder.getX() + Main.oneFolder.getWidth()) + 2;
		
		if (Main.baseFolder == null) toRemove.add(this);
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !RenameFile.added && Explorer.selected == null)) {
			MouseInput.updateMouse();
			
			returnToBaseFolder();
		}
	}
	
	public static void returnToBaseFolder() {
		Explorer.folderPath = "";
		
		Explorer.files.clear();
		ListableFile.files.clear();
		
		Explorer.scope = null;
		Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(Main.baseFolder));
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff || Explorer.explorerMode == ExplorerMode.SEARCHREPLACE) return;
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 47, MouseInput.getMouseY() + 27, 330, 28);
			
			Fonts.drawString(Texts.returnBaseFolder, MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
