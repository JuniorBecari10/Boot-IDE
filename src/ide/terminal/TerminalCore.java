package ide.terminal;

import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.ExecuteButtonIcon;
import ide.explorercomponents.TextArea;
import ide.explorercomponents.ToggleButton;
import ide.input.MouseInput;
import ide.input.MouseWheelRoll;
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
	public static boolean showOverlay = false;
	
	public static char prompt = '$';
	public static int tabScroll = 0;
	
	/*public static final String[] pythonScript = {
			"import sys",
			"import os",
			
			"if len(sys.argv) < 2:",
			"	sys.exit(0)",
			"os.system(\" \".join(sys.argv[1:]))"
	};*/
	
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
		
		if (Explorer.addTerminal == null) {
			Explorer.addTerminal = new ExecuteButtonIcon(58, Screen.DECORATION_HEIGHT + 80, 32, 32, Main.add, () -> {
				addTerminal();
			}, Texts.addTerminal) {
				public void tick() {
					super.tick();
					
					caption = Texts.addTerminal;
				}
			};
		}
		
		if (Explorer.showOverlay == null) {
			Explorer.showOverlay = new ToggleButton(96, Screen.DECORATION_HEIGHT + 80, 32, 32, Main.showOverlaySpr, showOverlay, Texts.showOverlay) {
				public void tick() {
					super.tick();
					
					caption = Texts.showOverlay;
					showOverlay = state;
				}
			};
		}
		
		if (Explorer.textArea == null) {
			Explorer.textArea = new TextArea(10, TerminalTab.Y_EXPLORER + (TerminalTab.HEIGHT * 2) - 10, Main.explorer.getWidth() - 20, (Main.screen.getHeight() - Screen.DECORATION_HEIGHT + 280), selected == null ? new String[0] : selected.getLines()) {
				public void tick() {
					super.tick();
					
					y = TerminalCore.tabs.isEmpty() ? TerminalTab.Y_EXPLORER + 10 : TerminalTab.Y_EXPLORER + (TerminalTab.HEIGHT * 2) - 10;
					
					width = Main.explorer.getWidth() - 20;
					height = (Main.screen.getHeight() - Screen.DECORATION_HEIGHT + 280);
					lines = selected == null ? new String[0] : selected.getLines();
					
				}
			};
		}
		
		IDEComponent.toAdd.add(Explorer.showOverlay);
		IDEComponent.toAdd.add(Explorer.addTerminal);
		IDEComponent.toAdd.add(Explorer.wordWrap); // por causa do texto
		IDEComponent.toAdd.add(Explorer.textArea);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.wordWrap);
		IDEComponent.toRemove.add(Explorer.addTerminal);
		IDEComponent.toRemove.add(Explorer.showOverlay);
		
		if (showOverlay) return;
		
		IDEComponent.toRemove.add(Explorer.textArea);
	}
	
	public static void addTerminal() {
		int x = 1;
		
		if (!tabs.isEmpty())
			x = tabs.get(tabs.size() - 1).getX() + tabs.get(tabs.size() - 1).getWidth() + 3;
		
		TerminalTab term = new TerminalTab(x, TerminalTab.Y_EXPLORER, TerminalTab.WIDTH, "Term-" + TerminalCore.getNextUntitledNumber());
		
		TerminalCore.tabs.add(term);
    	TerminalCore.selected = term;
    	
    	if (TerminalCore.selected != null)
    		while (term.getX() - tabScroll >= Main.explorer.getWidth() - 1)
    			tabScroll += TerminalTab.WIDTH + 3;
    	
    	Explorer.selected = Explorer.textArea;
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
	
	public static void scroll() {
		if (MouseInput.getWheelRoll() == MouseWheelRoll.DOWN && !tabs.isEmpty() && tabs.get(tabs.size() - 1).getX() - tabScroll - (TerminalTab.WIDTH - 3) >= 1)
			tabScroll += TerminalTab.WIDTH + 3;
		else if (MouseInput.getWheelRoll() == MouseWheelRoll.UP && tabScroll > 0)
			tabScroll -= TerminalTab.WIDTH + 3;
	}
}
