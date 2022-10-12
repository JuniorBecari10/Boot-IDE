package ide.terminal;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RenameFile;
import ide.components.SetFileName;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;

public class TerminalTab extends IDEComponent {

	public static final int HEIGHT = 30;
	public String name;
	
	public TerminalTab(int x, int y, int width, String name) {
		super(x, y, width, HEIGHT, Main.terminalTab);
		
		this.name = name;
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Color c = TerminalCore.selected == this ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() && !(SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added) ? Colors.explorerLight : Colors.explorer;

		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x - 1, y - 1, width + 2, HEIGHT + 2);

		g.setColor(c);
		
		if (TerminalCore.selected == this)
			g.drawRect(x, y, width, HEIGHT);
		else
			g.drawLine(x + width, y, x + width, Screen.DECORATION_HEIGHT + HEIGHT);
		
		final int imageSize = 24;
		g.drawImage(sprite, x + ((width / 2) - (imageSize / 2)), y + ((HEIGHT / 2) - (imageSize / 2)), imageSize, imageSize, null);
	}
}
