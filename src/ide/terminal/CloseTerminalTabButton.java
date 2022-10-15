package ide.terminal;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import ide.components.IDEComponent;
import ide.util.Colors;

public class CloseTerminalTabButton extends IDEComponent implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private TerminalTab regent;

	public CloseTerminalTabButton(int x, int y, int width, int height, BufferedImage sprite, TerminalTab regent) {
		super(x, y, width, height, sprite);
		
		this.regent = regent;
	}
	
	public void tick() {
		if (leftClicked())
			regent.close();
	}
	
	public void render(Graphics g) {
		if (hovered()) {
			g.setColor(Colors.explorerLighter);
			g.fillRect(x - 2, y - 2, width + 4, height + 4);
		}
		
		super.render(g);
	}
}
