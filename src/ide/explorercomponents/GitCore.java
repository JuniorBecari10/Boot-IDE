package ide.explorercomponents;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Texts;

public class GitCore {

	public static void init() {
		Explorer.explorerMode = ExplorerMode.GIT;
		
		if (Explorer.initRepo == null)
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 50, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> { Main.runCommand("cmd", "/c", "git init"); }, true);
		
		IDEComponent.toAdd.add(Explorer.initRepo);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.initRepo);
	}
}
