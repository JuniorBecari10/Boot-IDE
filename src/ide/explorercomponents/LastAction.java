package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.Rectangle;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Texts;

public class LastAction extends IDEComponent {

	public GitAction action;
	private String text = "";
	
	public LastAction(int x, int y, int width, int height, GitAction action) {
		super(x, y, width, height, null);
		
		this.action = action;
	}
	
	public boolean hovered() {
		Rectangle bounds = new Rectangle((Main.explorer.getWidth() / 2 - (text.length() * 12) / 2) - (action == null ? 0 : 15), y, text.length() * 12, height);
		Rectangle mouse = MouseInput.getMouseBounds();
		
		return bounds.intersects(mouse);
	}
	
	public void tick() {
		if (!GitCore.actions.isEmpty())
			action = GitCore.actions.peek();
		
		if (action != null) {
			if (action.state == ActionState.ERROR) sprite = Main.gitError;
			else if (action.state == ActionState.PROGRESS) sprite = Main.gitProgress;
			else if (action.state == ActionState.DONE) sprite = Main.gitDone;
		}
		
		text = Texts.noActionsDone;
		
		if (action != null) {
			if (action.state == ActionState.ERROR) text = Texts.gitError;
			else if (action.state == ActionState.PROGRESS) text = Texts.gitProgress;
			else if (action.state == ActionState.DONE) text = Texts.gitDone;
			
			text = action.name + " | " + text + ".";
		}
	}
	
	public void render(Graphics g) {
		g.drawImage(sprite, (Main.explorer.getWidth() / 2 - (text.length() * 12) / 2) - 15, y, 15, 15, null);
	
		Fonts.drawString(text, (Main.explorer.getWidth() / 2 - (text.length() * 12) / 2) + (action == null ? 0 : 15), y, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
	}
}
