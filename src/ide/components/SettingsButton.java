package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import ide.explorer.Explorer;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class SettingsButton extends IDEComponent {

	public SettingsButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
	}
	
	public boolean hovered() {
		if (SetBranchName.added || SetFileName.added || CommandTerminal.active || MessageBox.active || SetBranchName.added || SetCommitName.added) return false;
		
		return super.hovered();
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		x = Main.explorer.getWidth() - 34;
		
		if (leftClicked())
			CommandTerminal.runCommand("settings");
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff) return;
		
		if (hovered()) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, y, width, height + 3);
		}
		
		super.render(g);
		
		if (hovered()) {
			Explorer.renderDescriptionText(Texts.settings, MouseInput.getMouseX() - 20, MouseInput.getMouseY() + 30, g);
		}
	}
}
