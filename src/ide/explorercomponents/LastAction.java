package ide.explorercomponents;

import java.awt.Graphics;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Texts;

public class LastAction extends IDEComponent {

	public GitAction action;	
	
	public LastAction(int x, int y, int width, int height, GitAction action) {
		super(x, y, width, height, null);
		
		this.action = action;
	}
	
	public void tick() {
		if (!GitCore.actions.isEmpty())
			action = GitCore.actions.peek();
		
		if (action != null) {
			if (action.state == ActionState.ERROR) sprite = Main.gitError;
			else if (action.state == ActionState.PROGRESS) sprite = Main.gitProgress;
			else if (action.state == ActionState.DONE) sprite = Main.gitDone;
		}
	}
	
	public void render(Graphics g) {
		g.drawImage(sprite, x, y, 15, 15, null);
		
		String text = Texts.noActionsDone;
		
		if (action != null) {
			if (action.state == ActionState.ERROR) text = Texts.gitError;
			else if (action.state == ActionState.PROGRESS) text = Texts.gitProgress;
			else if (action.state == ActionState.DONE) text = Texts.gitDone;
			
			text = action.name + " | " + text + ".";
		}
	
		Fonts.drawString(text, text.equals(Texts.noActionsDone) ? x + 20 : x + 30, y, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
	}
}
