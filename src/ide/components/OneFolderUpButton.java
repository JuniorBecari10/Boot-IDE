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

public class OneFolderUpButton extends IDEComponent {

	public OneFolderUpButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		if (Main.baseFolder == null) toRemove.add(this);
		
		x = Main.explorer.getWidth() - 120;
		
		if (x < (Main.newFolder.getX() + Main.newFolder.getWidth()) + 2) x = (Main.newFolder.getX() + Main.newFolder.getWidth()) + 2;
		
		if (Explorer.scope == null) return;
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !RenameFile.added && Explorer.selected == null)) {
			MouseInput.updateMouse();
			
			oneFolderUp();
		}
	}
	
	public static void oneFolderUp() {
		Explorer.folderPath = "";
		
		Explorer.files.clear();
		ListableFile.files.clear();
		
		if (Explorer.scope == null ? true : Explorer.scope.getParent() == null) { // se for null e porque é a base folder
			Explorer.scope = null; // coloca depois da verificação pra n dar exception
			
			Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(Main.baseFolder));
		}
		else // se não for é porque tem pasta antes
			Explorer.files = ListableFile.loadFolder(Explorer.scope == null ? ListableFile.newListableFile(Main.baseFolder) : Explorer.scope.getParent());
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			Explorer.renderDescriptionText(Texts.oneFolderUp, MouseInput.getMouseX() - 50, MouseInput.getMouseY() + 30, g);
		}
	}
}
