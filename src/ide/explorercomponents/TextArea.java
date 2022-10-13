package ide.explorercomponents;

import java.awt.Graphics;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.util.Colors;

public class TextArea extends IDEComponent {
	
	public String[] lines;

	public TextArea(int x, int y, int width, int height, String[] lines) {
		super(x, y, width, height, null);
		
		this.lines = lines;
	}
	
	public void type() {
		
	}
	
	public void tick() {
		if (leftClicked())
			Explorer.selected = this;
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLight);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
	}
}
