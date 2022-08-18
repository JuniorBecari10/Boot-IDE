package ide.explorercomponents;

import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.components.RightClickOption;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Texts;

public class GitCore {

	public static void init() {
		Explorer.explorerMode = ExplorerMode.GIT;
		
		if (Explorer.initRepo == null) {
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 80, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> {
				Main.runCommand(Main.baseFolder, "git init");
			}, true);
		}
		
		if (Explorer.cloneURL == null) {
			Explorer.cloneURL = new InputBox(20, Screen.DECORATION_HEIGHT + 150, Main.explorer.getWidth() - 40, 20);
		}
		
		if (Explorer.clone == null) {
			Explorer.clone = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 180, Main.explorer.getWidth() - 40, 20, Texts.clone, () -> {
				if (Explorer.cloneURL.getText().length() == 0) return;
				
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> { Main.runCommand(Main.baseFolder, "git clone " + Explorer.cloneURL.getText() ); }, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder + (Main.baseFolder != null ? Explorer.scope != null ? (" (" + Explorer.scope.getRegent().getName() + ")") : (" (" + Main.baseFolder.getName() + ")") : ""), (s) -> {}, ""));
				
				IDEComponent.addRightClickOptions(20, Screen.DECORATION_HEIGHT + 205, list.toArray(new RightClickOption[list.size()]));
			}, true);
		}
		
		IDEComponent.toAdd.add(Explorer.initRepo);
		IDEComponent.toAdd.add(Explorer.cloneURL);
		IDEComponent.toAdd.add(Explorer.clone);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.initRepo);
		IDEComponent.toRemove.add(Explorer.cloneURL);
		IDEComponent.toRemove.add(Explorer.clone);
	}
}
