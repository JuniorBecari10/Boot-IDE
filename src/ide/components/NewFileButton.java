package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.input.MouseInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;
import ide.util.Texts;

public class NewFileButton extends IDEComponent {
	
	public NewFileButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (x < (Main.openBase.getX() + Main.openBase.getWidth()) + 2) x = (Main.openBase.getX() + Main.openBase.getWidth()) + 2;
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !MessageBox.active && !RenameFile.added && Explorer.selected == null)) {
			MouseInput.updateMouse();
			
			int y = 200 + Screen.DECORATION_HEIGHT;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).y + 30;
			
			Explorer.setFileName = new SetFileName(0, y, Main.explorer.width - 3, 30, true);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(Explorer.setFileName);
		}
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			Explorer.renderDescriptionText(Texts.createFile, MouseInput.getMouseX() - 50, MouseInput.getMouseY() + 30, g);
		}
	}
}
