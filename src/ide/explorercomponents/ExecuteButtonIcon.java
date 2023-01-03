package ide.explorercomponents;

import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.components.CommandTerminal;
import ide.components.FileChooser;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class ExecuteButtonIcon extends IDEComponent {

	protected Execute execute;
	protected String caption;
	protected boolean enabled = true;
	protected boolean isMessageBox = false;
	
	public ExecuteButtonIcon(int x, int y, int width, int height, BufferedImage sprite, Execute execute, String caption) {
		super(x, y, width, height, sprite);
		
		this.execute = execute;
		this.caption = caption;
	}
	
	public ExecuteButtonIcon(int x, int y, int width, int height, BufferedImage sprite, Execute execute, boolean isMessageBox, String caption) {
		super(x, y, width, height, sprite);
		
		this.execute = execute;
		this.caption = caption;
		this.isMessageBox = isMessageBox;
	}
	
	public ExecuteButtonIcon(int x, int y, int width, int height, BufferedImage sprite, Execute execute, String caption, boolean enabled) {
		super(x, y, width, height, sprite);
		
		this.execute = execute;
		this.caption = caption;
		this.enabled = enabled;
	}
	
	public boolean hovered() {
		if (SetBranchName.added || SetFileName.added || CommandTerminal.active || SetBranchName.added || SetCommitName.added) return false;
		
		if (MessageBox.active && !isMessageBox) return false;
		if (isMessageBox && IDEComponent.components.contains(FileChooser.fileChooser.setFileName))
			return false;
		
		return super.hovered();
	}
	
	public void tick() {
		if (hovered())
			Main.screen.setCursor(Cursor.getDefaultCursor());
		
		if (leftClicked() && enabled) {
			KeyInput.updateKeys();
			RightClickOption.removeAllRightClickOptions();
			
			execute.execute();
		}
	}
	
	public void render(Graphics g) {
		if (hovered() && enabled) {
			g.setColor(Colors.explorerLight);
			g.fillRect(x, y, width, height);
		}
		
		super.render(g);
		
		if (hovered()) {
			Explorer.renderDescriptionText(caption, MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, g);
		}
	}
}
