package ide.terminal;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.ToggleButton;
import ide.main.Main;
import ide.screen.Screen;

public class TerminalCore {
	
	public static boolean breakLine = true;
	
	public static void init() {
		Explorer.explorerMode = ExplorerMode.TERMINAL;
		
		if (Explorer.breakLine == null) {
			Explorer.breakLine = new ToggleButton(20, Screen.DECORATION_HEIGHT + 100, 32, 32, Main.breakLineSpr, breakLine, "Break Line") {
				public void tick() {
					super.tick();
					
					TerminalCore.breakLine = state;
				}
			};
		}
		
		IDEComponent.toAdd.add(Explorer.breakLine);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.breakLine);
	}
}
