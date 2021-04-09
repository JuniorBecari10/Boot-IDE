package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;

public class NewFolderButton extends IDEComponent {

	public NewFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public void tick() {
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			int y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).y + 30;
			
			SetFileName set = new SetFileName(0, y, Main.explorer.width, 30, false);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(set);
		}
	}
	
	public void render(Graphics g) {
		super.render(g);
		
		if (hovered())
			Fonts.drawString("Criar Nova Pasta", MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
