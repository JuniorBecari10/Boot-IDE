package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorer.ListableFile;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class ReturnToBaseFolderButton extends IDEComponent {

	public ReturnToBaseFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (x < (Main.oneFolder.getX() + Main.oneFolder.getWidth()) + 2) x = (Main.oneFolder.getX() + Main.oneFolder.getWidth()) + 2;
		
		if (Main.baseFolder == null) toRemove.add(this);
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !MessageBox.active && !SetBranchName.added && !SetCommitName.added && !RenameFile.added && Explorer.selected == null)) {
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
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			Explorer.renderDescriptionText(Texts.returnBaseFolder, MouseInput.getMouseX() - 50, MouseInput.getMouseY() + 30, g);
		}
	}
}
