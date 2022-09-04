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
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 180, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> {
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> {
					String[] output = Main.runCommand(Main.baseFolder, "git init");
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					actions.add(new GitAction("git init", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output));
					}, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> {
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git init");
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					actions.add(new GitAction("git init", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output));
					}, ""));
				
				IDEComponent.addRightClickOptions(20, Screen.DECORATION_HEIGHT + 102, list.toArray(new RightClickOption[list.size()]));
			}, true) {
				public void tick() {
					super.tick();
					
					text = Texts.initRepository;
				}
			};
		}
		
		if (Explorer.cloneURL == null) {
			Explorer.cloneURL = new InputBox(20, Screen.DECORATION_HEIGHT + 250, Main.explorer.getWidth() - 40, 20);
		}
		
		if (Explorer.clone == null) {
			Explorer.clone = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 280, Main.explorer.getWidth() - 40, 20, Texts.clone, () -> {
				if (Explorer.cloneURL.getText().length() == 0) return;
				
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> {
					String[] output = Main.runCommand(Main.baseFolder, "git clone " + Explorer.cloneURL.getText());
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					actions.add(new GitAction("git clone", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output));
					}, ""));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> {
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git clone " + Explorer.cloneURL.getText());
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					actions.add(new GitAction("git clone", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output));
					}, ""));
				
				IDEComponent.addRightClickOptions(20, Screen.DECORATION_HEIGHT + 302, list.toArray(new RightClickOption[list.size()]));
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
		
		if (Explorer.isBaseFolderRepository())
			initRepoComponents();
	}
	
	public static synchronized void initRepoComponents() {
		if (Explorer.stageAll == null) {
			Explorer.stageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 350, Main.explorer.getWidth() - 40, 20, Texts.stageAll, () -> {
				String[] output = Main.runCommand(Main.baseFolder, "git add .");
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				actions.add(new GitAction("git add", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output)); }, true) {
				public void tick() {
					super.tick();
					
					text = Texts.stageAll;
				}
			};
		}
		
		if (Explorer.unstageAll == null) {
			Explorer.unstageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 380, Main.explorer.getWidth() - 40, 20, Texts.unstageAll, () -> {
				String[] output = Main.runCommand(Main.baseFolder, "git reset");
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				actions.add(new GitAction("git reset", error ? ActionState.ERROR : warn ? ActionState.WARNING : ActionState.DONE, output)); }, true) {
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
