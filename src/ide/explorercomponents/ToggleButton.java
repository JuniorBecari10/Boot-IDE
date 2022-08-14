package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.util.Colors;

public class ToggleButton extends IDEComponent {

	private boolean state;
	protected String caption;
	
	public ToggleButton(int x, int y, int width, int height, BufferedImage sprite, boolean state, String caption) {
		super(x, y, width, height, sprite);
		
		this.state = state;
		this.caption = caption;
	}
	
	public boolean getState() {
		return state;
	}
	
	public void invertState() {
		this.state = !this.state;
	}
	
	public void setState(boolean state) {
		this.state = state;
	}
	
	public void tick() {
		if (leftClicked()) {
			KeyInput.updateKeys();
			
			state = !state;
		}
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.explorerLight);
			g.fillRect(x, y, width, height);
		}
		
		super.render(g);
		
		if (state) {
			g.setColor(Colors.explorerLighter);
			g.drawLine(x, y + height, x + width, y + height);
		}
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active)) {
			Explorer.renderDescriptionText(caption, MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, g);
		}
	}
}
