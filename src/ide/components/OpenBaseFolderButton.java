package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;

import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class OpenBaseFolderButton extends IDEComponent {
	
	private JFileChooser chooser;

	public OpenBaseFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		super.tick();
		
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			int option = chooser.showOpenDialog(Main.screen.frame);

			if (option == JFileChooser.APPROVE_OPTION) {
				if (chooser.getSelectedFile() == null || chooser.getSelectedFile().listFiles() == null) return;
				
				File sel = chooser.getSelectedFile();
				
				Main.baseFolder = sel;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.scope = null;
				
				int index = 0;
				
				for (File f : Main.baseFolder.listFiles()) {
					Explorer.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
					
					index++;
				}
				
				Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
				
				IDEComponent.toAdd.add(Main.oneLevel);
				IDEComponent.toAdd.add(Main.returnBase);
				IDEComponent.toAdd.add(Main.newFile);
				IDEComponent.toAdd.add(Main.newFolder);
				IDEComponent.toAdd.add(Main.reload);
				
				MouseInput.updateMouse();
			}
		}
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff) return;
		
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 1, y - 1, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered())
			Fonts.drawString("Selecionar Pasta Base", MouseInput.getMouseX() - 40, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
