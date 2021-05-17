package ide.components;

import java.awt.Color;
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

public class ReturnToBaseFolderButton extends IDEComponent {

	public ReturnToBaseFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			Explorer.folderPath = "";
			
			Explorer.files.clear();
			ListableFile.files.clear();
			
			Explorer.scope = null;
			
			int index = 0;
			
			for (File f : Main.baseFolder.listFiles()) {
				Explorer.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
				
				index++;
			}
		}
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff) return;
		
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
			
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 47, MouseInput.getMouseY() + 27, 330, 28);
		}
		
		super.render(g);
		
		if (hovered())
			Fonts.drawString("Retornar à Pasta Base", MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
