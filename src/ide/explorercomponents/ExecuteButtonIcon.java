package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.util.Colors;

public class ExecuteButtonIcon extends IDEComponent {

	protected Execute execute;
	protected String caption;
	
	public ExecuteButtonIcon(int x, int y, int width, int height, BufferedImage sprite, Execute execute, String caption) {
		super(x, y, width, height, sprite);
		
		this.execute = execute;
		this.caption = caption;
	}
	
	public boolean hovered() {
		if (SetBranchName.added || SetFileName.added || CommandTerminal.active || MessageBox.active || SetBranchName.added || SetCommitName.added) return false;
		
		return super.hovered();
	}
	
	public void tick() {
		if (leftClicked()) {
			KeyInput.updateKeys();
			
			execute.execute();
		}
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.explorerLight);
			g.fillRect(x, y, width, height);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			Explorer.renderDescriptionText(caption, MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, g);
		}
	}
}
