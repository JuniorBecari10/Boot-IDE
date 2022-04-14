package ide.searchreplace;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;

public class ToggleButton extends IDEComponent {

	private boolean state;
	private String caption;
	
	private int engLength, portLength;
	
	public ToggleButton(int x, int y, int width, int height, BufferedImage sprite, boolean state, String caption, int engLength, int portLength) {
		super(x, y, width, height, sprite);
		
		this.state = state;
		this.caption = caption;
		
		this.engLength = engLength;
		this.portLength = portLength;
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
			g.setColor(new Color(0, 0, 0, 0.5f));
			g.fillRect(MouseInput.getMouseX() - 7, MouseInput.getMouseY() + 27, Main.lang == Language.PORT ? portLength : engLength, 28);
			
			Fonts.drawString(caption, MouseInput.getMouseX(), MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		}
	}
}
