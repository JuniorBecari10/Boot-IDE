package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RenameFile;
import ide.components.SetBranchName;
import ide.components.SetCommitName;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.input.MouseInput;
import ide.screen.Screen;
import ide.util.Colors;

public class ExplorerTab extends IDEComponent {
	
	public static final int Y = Screen.DECORATION_HEIGHT + 3;
	public static final int SIZE = 35;
	
	public ExplorerMode regent;
	public String name;
	//public int nameBgWidth;

	public ExplorerTab(int x, BufferedImage sprite, ExplorerMode regent, String name) {
		super(x, Y, SIZE, SIZE, sprite);
		
		this.regent = regent;
		this.name = name;
		//this.nameBgWidth = nameBgWidth;
	}
	
	public void select() {
		// it does nothing, you have to implement
		//Explorer.explorerMode = regent;
	}
	
	public void tick() {
		if (leftClicked())
			select();
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		Color c = Explorer.explorerMode == regent ? Colors.textLight : Colors.explorerLight;
		Color bg = hovered() && !(SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added) ? Colors.explorerLight : Colors.explorer;

		g.setColor(bg);
		g2.setStroke(new BasicStroke(3f));
		g2.fillRect(x - 1, Y - 1, SIZE + 2, SIZE + 2);

		g.setColor(c);
		
		if (Explorer.explorerMode == regent)
			g.drawRect(x, Y, SIZE, SIZE);
		else
			g.drawLine(x + SIZE, Y, x + SIZE, Screen.DECORATION_HEIGHT + SIZE);
		
		final int imageSize = 32;
		g.drawImage(sprite, x + ((SIZE / 2) - (imageSize / 2)), Y + ((SIZE / 2) - (imageSize / 2)), imageSize, imageSize, null);
		
		if (hovered() && !(SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added)) {
			Explorer.renderDescriptionText(name, MouseInput.getMouseX() - 27, MouseInput.getMouseY() + 27, g);
		}
	}
}
