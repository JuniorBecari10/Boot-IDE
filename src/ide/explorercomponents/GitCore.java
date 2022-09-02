package ide.explorercomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ide.components.IDEComponent;
import ide.components.RightClickOption;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Texts;

public class GitCore {
	
	public static Stack<GitAction> actions = new Stack<>();

	public static void init() {
		Explorer.explorerMode = ExplorerMode.GIT;
		
		if (Explorer.initRepo == null) {
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 80, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> {
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> { boolean error = Main.isError(Main.runCommand(Main.baseFolder, "git init")); actions.add(new GitAction("git init", error ? ActionState.ERROR : ActionState.DONE)); }, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> { boolean error = Main.isError(Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git init")); actions.add(new GitAction("git init", error ? ActionState.ERROR : ActionState.DONE)); }, ""));
				
				IDEComponent.addRightClickOptions(20, Screen.DECORATION_HEIGHT + 102, list.toArray(new RightClickOption[list.size()]));
			}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.initRepository;
				}
			};
		}
		
		if (Explorer.cloneURL == null) {
			Explorer.cloneURL = new InputBox(20, Screen.DECORATION_HEIGHT + 150, Main.explorer.getWidth() - 40, 20);
		}
		
		if (Explorer.clone == null) {
			Explorer.clone = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 180, Main.explorer.getWidth() - 40, 20, Texts.clone, () -> {
				if (Explorer.cloneURL.getText().length() == 0) return;
				
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> { boolean error = Main.isError(Main.runCommand(Main.baseFolder, "git clone " + Explorer.cloneURL.getText())); actions.add(new GitAction("git clone", error ? ActionState.ERROR : ActionState.DONE)); }, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> { boolean error = Main.isError(Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git clone " + Explorer.cloneURL.getText())); actions.add(new GitAction("git clone", error ? ActionState.ERROR : ActionState.DONE)); }, ""));
				
				IDEComponent.addRightClickOptions(20, Screen.DECORATION_HEIGHT + 202, list.toArray(new RightClickOption[list.size()]));
			}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.clone;
				}
			};
		}
		
		if (Explorer.lastAction == null) {
			Explorer.lastAction = new LastAction(20, Main.screen.getHeight() - 30, Main.explorer.getWidth() - 40, 20, null);
		}
		
		IDEComponent.toAdd.add(Explorer.initRepo);
		IDEComponent.toAdd.add(Explorer.cloneURL);
		IDEComponent.toAdd.add(Explorer.clone);
		IDEComponent.toAdd.add(Explorer.lastAction);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.initRepo);
		IDEComponent.toRemove.add(Explorer.cloneURL);
		IDEComponent.toRemove.add(Explorer.clone);
		IDEComponent.toRemove.add(Explorer.lastAction);
	}
}
