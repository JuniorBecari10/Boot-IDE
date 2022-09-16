package ide.explorercomponents;

import java.awt.Graphics;
import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
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
		return Main.explorer.hovered() && MouseInput.getMouseY() >= y - 5;
	}
	
	public void tick() {
		if (!GitCore.actions.isEmpty())
			action = GitCore.actions.peek();
		
		if (action != null) {
			if (action.state == ActionState.ERROR) sprite = Main.gitError;
			//else if (action.state == ActionState.PROGRESS) sprite = Main.gitProgress;
			else if (action.state == ActionState.WARNING) sprite = Main.gitWarning;
			else if (action.state == ActionState.DONE) sprite = Main.gitDone;
		}
		
		text = Texts.noActionsDone;
		
		if (action != null) {
			if (action.state == ActionState.ERROR) text = Texts.gitError;
			//else if (action.state == ActionState.PROGRESS) text = Texts.gitProgress;
			else if (action.state == ActionState.WARNING) text = Texts.gitWarning;
			else if (action.state == ActionState.DONE) text = Texts.gitDone;
			
			text = action.name + " | " + text + " ";
		}
	}
	
	public void render(Graphics g) {
		g.drawImage(sprite, (Main.explorer.getWidth() / 2 - (text.length() * 12) / 2) - 15, y, 15, 15, null);
	
		Fonts.drawString(text, (Main.explorer.getWidth() / 2 - (text.length() * 12) / 2) + (action == null ? 0 : 15), y, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (action != null) {
			/*List<String> output = new ArrayList<>();
			
			if (action.output.length == 0) {
				action.output = new String[1];
				action.output[0] = Texts.noOutput;
			}
			else if (!action.output[0].contains(Texts.noOutput)) {
				output.add(Texts.lastCommandOutput);
				output.add("");
			}
			
			output.addAll(Arrays.asList(action.output));
			
			if (hovered())
				Explorer.renderCardText(output.toArray(new String[0]), MouseInput.getMouseX() + 30, MouseInput.getMouseY(), g);*/
			
			if (action.output.length == 0) {
				action.output = new String[1];
				action.output[0] = Texts.noOutput;
			}
			
			if (hovered())
				Explorer.renderCardText(action.output, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), g);
		}
	}
}
