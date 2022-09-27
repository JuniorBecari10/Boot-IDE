package ide.explorercomponents;

import java.awt.Color;
import java.awt.Graphics;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.SetFileName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;

public class ExecuteButton extends IDEComponent {
	
	public String text;
	protected Execute execute;
	protected boolean enabled;
	
	protected boolean inMessageBox;
	
	public ExecuteButton(int x, int y, int width, int height, String text, Execute execute, boolean enabled) {
		super(x, y, width, height, null);
		
		this.text = text;
		this.execute = execute;
		this.enabled = enabled;
		
		this.inMessageBox = false;
	}
	
	public ExecuteButton(int x, int y, int width, int height, String text, Execute execute, boolean enabled, boolean inMessageBox) {
		super(x, y, width, height, null);
		
		this.text = text;
		this.execute = execute;
		this.enabled = enabled;
		
		this.inMessageBox = inMessageBox;
	}
	
	public boolean hovered() {
		if (inMessageBox) {
			if (SetBranchName.added || SetFileName.added || CommandTerminal.active || SetBranchName.added) return false;
		}
		else
			if (SetBranchName.added || SetFileName.added || CommandTerminal.active || MessageBox.active || SetBranchName.added) return false;
		
		return super.hovered();
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
