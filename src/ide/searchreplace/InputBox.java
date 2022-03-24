package ide.searchreplace;

import java.awt.Graphics;

import ide.components.IDEComponent;
import ide.input.KeyInput;
import ide.util.Colors;

public class InputBox extends IDEComponent {

	private String text;
	
	public InputBox(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		new Thread() {
			public void run() {
				while (true) {
					if (KeyInput.isKeyPressed())
						text += KeyInput.getCharPressed();
				}
			}
		}.start();
	}
	
	public String getText() {
		return text;
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLighter);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorerLight);
		g.fillRect(x, y, width, height);
	}
}
