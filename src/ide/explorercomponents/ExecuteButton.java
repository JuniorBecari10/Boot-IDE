package ide.explorercomponents;

import java.awt.Color;
import java.awt.Graphics;

import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;

public class ExecuteButton extends IDEComponent {
	
	protected String text;
	private Execute execute;
	protected boolean enabled;
	
	public ExecuteButton(int x, int y, int width, int height, String text, Execute execute, boolean enabled) {
		super(x, y, width, height, null);
		
		this.text = text;
		this.execute = execute;
		this.enabled = enabled;
	}
	
	public void tick() {
		width = Main.explorer.getWidth() - 40;
		
		if (leftClicked() && enabled)
			execute.execute();
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLighter);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		if (enabled) {
			g.setColor(hovered() ? Colors.explorerLighter : Colors.explorerLight);
			g.fillRect(x, y, width, height);
		}
		
		Fonts.drawString(text, x + ((width / 2) - (text.length() * 6) - 5), y + 2, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
		
		if (!enabled) {
			g.setColor(Colors.setAlpha(Color.black, 64));
			g.fillRect(x, y, width, height);
		}
	}
}
