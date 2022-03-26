package ide.searchreplace;

import java.awt.Graphics;

import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;

public class ExecuteButton extends IDEComponent {
	
	private String text;
	private Execute execute;
	
	public ExecuteButton(int x, int y, int width, int height, String text, Execute execute) {
		super(x, y, width, height, null);
		
		this.text = text;
		this.execute = execute;
	}
	
	public void tick() {
		width = Main.explorer.getWidth() - 40;
		
		if (leftClicked())
			execute.execute();
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLighter);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(hovered() ? Colors.explorerLighter : Colors.explorerLight);
		g.fillRect(x, y, width, height);
		
		Fonts.drawString(text, x + ((width / 2) - (text.length() * 6) - 5), y + 2, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
	}
}
