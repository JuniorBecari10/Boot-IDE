package ide.searchreplace;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.util.Colors;

public class InputBox extends IDEComponent {

	private StringBuilder text;
	private boolean canDigit = false;
	private int cursorIndex = 0;
	
	public InputBox(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		text = new StringBuilder();
		
		new Thread() {
			public void run() {
				while (true) {
					if (KeyInput.isKeyPressed() && canDigit) {
						KeyInput.updateKeys();
						
						if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
							KeyInput.updateKeys();
							if (cursorIndex == 0) continue;
							
							text.deleteCharAt(cursorIndex - 1);
							cursorIndex--;
						}
						
						if (cursorIndex == text.length()) text.append(KeyInput.getCharPressed());
						else text.insert(cursorIndex, KeyInput.getCharPressed());
						
						cursorIndex++;
					}
				}
			}
		}.start();
	}
	
	public void tick() {
		if (leftClicked())
			Explorer.selected = this;
		
		canDigit = Explorer.selected == this;
	}
	
	public String getText() {
		return text.toString();
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLighter);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(hovered() ? Colors.explorerLighter : Colors.explorerLight);
		g.fillRect(x, y, width, height);
		
		Fonts.drawString(getText(), x + 2, y + 2, new IDEFont(Fonts.otherNormal, 16), x + width, g);
	}
}
