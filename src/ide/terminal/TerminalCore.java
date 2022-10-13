package ide.terminal;

import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.TextArea;
import ide.explorercomponents.ToggleButton;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Texts;

public class TerminalCore {
	
	/*
	 * Shell Redirection
	 * 
	 * command > file  | write output to file
	 * command >> file | append output to file
	 */
	
	public static boolean breakLine = true;
	public static char prompt = '$';
	
	public static List<TerminalTab> tabs = new ArrayList<>();
	
	public static TerminalTab selected;
	
	public static void init() {
		Explorer.explorerMode = ExplorerMode.TERMINAL;
		
		if (Explorer.wordWrap == null) {
			Explorer.wordWrap = new ToggleButton(20, Screen.DECORATION_HEIGHT + 80, 32, 32, Main.wordWrapSpr, breakLine, Texts.wordWrap) {
				public void tick() {
					super.tick();
					
					caption = Texts.wordWrap;
					TerminalCore.breakLine = state;
				}
			};
		}
		
		if (Explorer.showOverlay == null) {
			Explorer.showOverlay = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 130, Main.explorer.getWidth() - 20, 20, Texts.showOverlay, () -> {}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.showOverlay;
				}
			};
		}
		
		if (Explorer.textArea == null) {
			Explorer.textArea = new TextArea(10, Screen.DECORATION_HEIGHT + 260, Main.explorer.getWidth() - 20, (Main.screen.getHeight() - Screen.DECORATION_HEIGHT + 280) - 20, selected.getLines()) {
				public void tick() {
					super.tick();
					
					width = Main.explorer.getWidth() - 20;
					height = (Main.screen.getHeight() - Screen.DECORATION_HEIGHT + 280) - 20;
					lines = selected.getLines();
					
				}
			};
		}
		
		IDEComponent.toAdd.add(Explorer.showOverlay);
		IDEComponent.toAdd.add(Explorer.wordWrap); // por causa do texto
		IDEComponent.toAdd.add(Explorer.textArea);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.wordWrap);
		IDEComponent.toRemove.add(Explorer.showOverlay);
		IDEComponent.toRemove.add(Explorer.textArea);
	}
	
	public static int getNextUntitledNumber() {
		int num = 0;
		
		for (TerminalTab t : tabs) {
			String name;
			if ((name = t.getLog().getName()).contains("Term"))
				num = Integer.parseInt(String.valueOf(name.charAt(name.length() - 1))) + 1;
		}
		
		return num;
	}
}
