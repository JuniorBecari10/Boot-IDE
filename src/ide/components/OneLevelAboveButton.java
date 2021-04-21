package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class OneLevelAboveButton extends IDEComponent {

	public OneLevelAboveButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (Explorer.scope == null) return;
		
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			Explorer.folderPath = "";
			
			Explorer.files.clear();
			ListableFile.files.clear();
			
			if (Explorer.scope.getParent() == null) { // se for null é porque é a base folder
				Explorer.scope = null; // coloca depois da verificação pra n dar exception
				
				int index = 0;
				
				for (File f : Main.baseFolder.listFiles()) {
					Explorer.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
					
					index++;
				}
			}
			else // se não for é porque tem pasta antes
				Explorer.files = ListableFile.loadFolder(Explorer.scope.getParent());
		}
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered())
			Fonts.drawString("Um Nível Acima", MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
