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

public class ReloadButton extends IDEComponent {

	public ReloadButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (x < (Main.returnBase.getX() + Main.returnBase.getWidth()) + 2) x = (Main.returnBase.getX() + Main.returnBase.getWidth()) + 2;
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !MessageBox.active && !SetBranchName.added && !SetCommitName.added && !RenameFile.added && Explorer.selected == null)) {
			MouseInput.updateMouse();
			
			reloadExplorer();
		}
	}
	
	public static void reloadExplorer() {
		Explorer.files.clear();
		ListableFile.files.clear();
		
		Explorer.files = ListableFile.loadFolder(Explorer.scope);
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (Main.baseFolder == null) toRemove.add(this);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			Explorer.renderDescriptionText(Texts.reload, MouseInput.getMouseX() - 50, MouseInput.getMouseY() + 30, g);
		}
	}
}
