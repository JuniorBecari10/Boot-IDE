package ide.terminal;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.ToggleButton;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Texts;

public class TerminalCore {
	
	public static boolean breakLine = true;
	public static TerminalTab selected;
	
	public static void init() {
		Explorer.explorerMode = ExplorerMode.TERMINAL;
		
		if (Explorer.breakLine == null) {
			Explorer.breakLine = new ToggleButton(20, Screen.DECORATION_HEIGHT + 80, 32, 32, Main.breakLineSpr, breakLine, Texts.breakLine) {
				public void tick() {
					super.tick();
					
					caption = Texts.breakLine;
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
		
		IDEComponent.toAdd.add(Explorer.showOverlay);
		IDEComponent.toAdd.add(Explorer.breakLine); // por causa do texto
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.breakLine);
		IDEComponent.toRemove.add(Explorer.showOverlay);
	}
}
