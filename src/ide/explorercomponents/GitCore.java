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

	public static ActionState getState(boolean error, boolean warn) {
		if (warn)
			return ActionState.WARNING;
		else if (error)
			return ActionState.ERROR;
		
		return ActionState.DONE;
	}
	
	public static void init() {
		Explorer.explorerMode = ExplorerMode.GIT;
		
		if (Explorer.initRepo == null) {
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 230, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> {
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> {
					String[] output = Main.runCommand(Main.baseFolder, "git init");
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.add(new GitAction("git init", getState(error, warn), output));
					}, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> {
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git init");
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.add(new GitAction("git init", getState(error, warn), output));
					}, ""));
				
				IDEComponent.addRightClickOptions(20, Explorer.isBaseFolderRepository() ? Screen.DECORATION_HEIGHT + 252 : Screen.DECORATION_HEIGHT + 112, list.toArray(new RightClickOption[list.size()]));
			}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.initRepository;
					
					if (Explorer.isBaseFolderRepository())
						y = Screen.DECORATION_HEIGHT + 220;
					else
						y = Screen.DECORATION_HEIGHT + 90;
				}
			};
		}
		
		if (Explorer.cloneURL == null) {
			Explorer.cloneURL = new InputBox(20, Screen.DECORATION_HEIGHT + 300, Main.explorer.getWidth() - 40, 20) {
				public void tick() {
					super.tick();
					
					if (Explorer.isBaseFolderRepository())
						y = Screen.DECORATION_HEIGHT + 300;
					else
						y = Screen.DECORATION_HEIGHT + 160;
				}
			};
		}
		
		Explorer.selected = Explorer.cloneURL;
		
		if (Explorer.clone == null) {
			Explorer.clone = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 330, Main.explorer.getWidth() - 40, 20, Texts.clone, () -> {
				if (Explorer.cloneURL.getText().length() == 0) return;
				
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> {
					String[] output = Main.runCommand(Main.baseFolder, "git clone " + Explorer.cloneURL.getText());
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.add(new GitAction("git clone", getState(error, warn), output));
					
					Explorer.fetchStatus();
					}, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> {
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git clone " + Explorer.cloneURL.getText());
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.add(new GitAction("git clone", getState(error, warn), output));
					}, ""));
				
				IDEComponent.addRightClickOptions(20, Explorer.isBaseFolderRepository() ? Screen.DECORATION_HEIGHT + 352 : Screen.DECORATION_HEIGHT + 212, list.toArray(new RightClickOption[list.size()]));
			}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.clone;
					
					if (Explorer.isBaseFolderRepository())
						y = Screen.DECORATION_HEIGHT + 330;
					else
						y = Screen.DECORATION_HEIGHT + 190;
				}
			};
		}
		
		if (Explorer.lastAction == null) {
			Explorer.lastAction = new LastAction(20, Main.screen.getHeight() - 30, Main.explorer.getWidth() - 40, 20, null);
		}
		
		IDEComponent.toAdd.add(Explorer.initRepo);
		IDEComponent.toAdd.add(Explorer.cloneURL);
		IDEComponent.toAdd.add(Explorer.clone);
		
		if (Explorer.isBaseFolderRepository())
			initRepoComponents();
		
		IDEComponent.toAdd.add(Explorer.lastAction);
	}
	
	public static synchronized void initRepoComponents() {
		if (Explorer.stageAll == null) {
			Explorer.stageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 400, Main.explorer.getWidth() - 40, 20, Texts.stageAll, () -> {
				String[] output = Main.runCommand(Main.baseFolder, "git add .");
				
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				
				actions.add(new GitAction("git add", getState(error, warn), output)); }, true) {
				public void tick() {
					super.tick();
					
					text = Texts.stageAll;
				}
			};
		}
		
		if (Explorer.unstageAll == null) {
			Explorer.unstageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 430, Main.explorer.getWidth() - 40, 20, Texts.unstageAll, () -> {
				String[] output = Main.runCommand(Main.baseFolder, "git reset");
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				actions.add(new GitAction("git reset", getState(error, warn), output)); }, true) {
				public void tick() {
					super.tick();
					
					text = Texts.unstageAll;
				}
			};
		}
		
		IDEComponent.toAdd.add(Explorer.stageAll);
		IDEComponent.toAdd.add(Explorer.unstageAll);
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.initRepo);
		IDEComponent.toRemove.add(Explorer.cloneURL);
		IDEComponent.toRemove.add(Explorer.clone);
		IDEComponent.toRemove.add(Explorer.lastAction);
		
		IDEComponent.toRemove.add(Explorer.stageAll);
		IDEComponent.toRemove.add(Explorer.unstageAll);
	}
}
