package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.util.Colors;

public class TextArea extends IDEComponent {
	
	public static final int MARGIN = 4;
	
	public String[] lines;
	public boolean acceptInput = false;
	
	private int fontSize = 16;

	public TextArea(int x, int y, int width, int height, String[] lines) {
		super(x, y, width, height, null);
		
		this.lines = lines;
	}
	
	public void type() {
		if (KeyInput.isKeyPressed() && Explorer.selected == this) {
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 61) {
				fontSize++;
			}
			else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_MINUS) {
				fontSize--;
				
				if (fontSize < 8) fontSize = 8;
			}
		}
		
		if (!acceptInput) return;
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
		
		int i = 0;
		for (String s : lines) {
			Fonts.drawString(s, x + 5, y + 5 + (i++ * fontSize + MARGIN), new IDEFont(Fonts.otherNormal, fontSize), x + width, g);
		}
	}
}
