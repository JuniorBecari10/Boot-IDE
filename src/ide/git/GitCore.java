package ide.git;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RightClickOption;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorercomponents.Execute;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.ExecuteButtonIcon;
import ide.explorercomponents.InputBox;
import ide.explorercomponents.LastAction;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.explorercomponents.ToggleButton;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Language;
import ide.util.Texts;

public class GitCore {
	
	public static Stack<GitAction> actions = new Stack<>();
	
	public static boolean checkoutToCreatedBranch = true;
	
	public static boolean allowEmptyCommits;
	
	public static ActionState getState(boolean error, boolean warn) {
		if (warn)
			return ActionState.WARNING;
		else if (error)
			return ActionState.ERROR;
		
		return ActionState.DONE;
	}
	
	public static ActionState getState(boolean error, boolean warn, boolean conflict) {
		if (conflict)
			return ActionState.CONFLICT;
		else if (warn)
			return ActionState.WARNING;
		else if (error)
			return ActionState.ERROR;
		
		return ActionState.DONE;
	}
	
	public static void merge(String branch1, String branch2) {
		actions.add(new GitAction("git merge", ActionState.PROGRESS, null));
		String[] output = Main.runCommand(Main.baseFolder, "git merge " + branch1 + " " + branch2);
		
		Explorer.fetchStatus();
		
		boolean error = Main.isError(output);
		boolean warn = Main.isWarning(output);
		boolean conflict = Main.isConflict(output);
		
		actions.set(actions.size() - 1, new GitAction("git merge", getState(error, warn, conflict), output));
	}
	
	public static void checkout(String branch) {
		actions.add(new GitAction("git checkout", ActionState.PROGRESS, null));
		String[] output = Main.runCommand(Main.baseFolder, "git checkout " + branch);
		
		Explorer.fetchStatus();
		
		boolean error = Main.isError(output);
		boolean warn = Main.isWarning(output);
		
		actions.set(actions.size() - 1, new GitAction("git checkout", getState(error, warn), output));
	}
	
	public static void delete(String branch) {
		actions.add(new GitAction("git branch", ActionState.PROGRESS, null));
		String[] output = Main.runCommand(Main.baseFolder, "git branch -d " + branch);
		
		Explorer.fetchStatus();
		
		boolean error = Main.isError(output);
		boolean warn = Main.isWarning(output);
		
		actions.set(actions.size() - 1, new GitAction("git branch", getState(error, warn), output));
	}
	
	public static void push(String repo, String branch, boolean force) {
		actions.add(new GitAction("git push", ActionState.PROGRESS, null));
		String[] output = Main.runCommand(Main.baseFolder, "git push " + (force ? "-f " : "-u ") + repo + " " + branch);
		
		Explorer.fetchStatus();
		
		boolean error = Main.isError(output);
		boolean warn = Main.isWarning(output);
		
		actions.set(actions.size() - 1, new GitAction("git push", getState(error, warn), output));
	}
	
	public static void init() {
		Explorer.explorerMode = ExplorerMode.GIT;
		
		if (Explorer.initRepo == null) {
			Explorer.initRepo = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 235, Main.explorer.getWidth() - 40, 20, Texts.initRepository, () -> Main.newThread(() -> {
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> {
					actions.add(new GitAction("git init", ActionState.PROGRESS, null));
					String[] output = Main.runCommand(Main.baseFolder, "git init");
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.set(actions.size() - 1, new GitAction("git init", getState(error, warn), output));
					
					Explorer.fetchStatus();
					}, "", true));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> {
					actions.add(new GitAction("git init", ActionState.PROGRESS, null));
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git init");
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.set(actions.size() - 1, new GitAction("git init", getState(error, warn), output));
					
					Explorer.fetchStatus();
					}, ""));
				
				IDEComponent.addRightClickOptions(20, Explorer.isBaseFolderRepository() ? Screen.DECORATION_HEIGHT + 257 : Screen.DECORATION_HEIGHT + 112, list.toArray(new RightClickOption[list.size()]));
			}), true) {
				public void tick() {
					super.tick();
					
					text = Texts.initRepository;
					
					if (Explorer.isBaseFolderRepository())
						y = Screen.DECORATION_HEIGHT + 235;
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
			Explorer.clone = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 330, Main.explorer.getWidth() - 40, 20, Texts.clone, () -> Main.newThread(() -> {
				if (Explorer.cloneURL.getText().length() == 0) return;
				
				int widthDraw = Main.explorer.getWidth() - 40;
				
				List<RightClickOption> list = new ArrayList<>();
				
				list.add(new RightClickOption(0, 0, widthDraw, Texts.inBaseFolder, (s) -> Main.newThread(() -> {
					actions.add(new GitAction("git clone", ActionState.PROGRESS, null));
					String[] output = Main.runCommand(Main.baseFolder, "git clone " + Explorer.cloneURL.getText());
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.set(actions.size() - 1, new GitAction("git clone", getState(error, warn), output));
					
					Explorer.fetchStatus();
					}), "", true));
				list.add(new RightClickOption(0, 0, widthDraw, Main.baseFolder != null, Texts.inCurrentFolder, (s) -> Main.newThread(() -> {
					actions.add(new GitAction("git clone", ActionState.PROGRESS, null));
					String[] output = Main.runCommand(Explorer.scope == null ? Main.baseFolder : Explorer.scope.getRegent(), "git clone " + Explorer.cloneURL.getText());
					
					boolean error = Main.isError(output);
					boolean warn = Main.isWarning(output);
					
					actions.set(actions.size() - 1, new GitAction("git clone", getState(error, warn), output));
					}), ""));
				
				IDEComponent.addRightClickOptions(20, Explorer.isBaseFolderRepository() ? Screen.DECORATION_HEIGHT + 352 : Screen.DECORATION_HEIGHT + 212, list.toArray(new RightClickOption[list.size()]));
			}), true) {
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
		if (Explorer.createBranch == null) {
			Explorer.createBranch = new ExecuteButtonIcon(20, Screen.DECORATION_HEIGHT + 130, 32, 32, Main.createBranchSpr, () -> Main.newThread(() -> {
				Explorer.setBranchName = new SetBranchName(0, Screen.DECORATION_HEIGHT + 165, 0, 30, false);
				
				IDEComponent.toAdd.add(Explorer.setBranchName);
				SetBranchName.added = true;
				Explorer.selected = null;
				
				Explorer.fetchStatus();
			}), Texts.createBranch) {
				public void tick() {
					super.tick();
					
					caption = Texts.createBranch;
				}
			};
		}
		
		if (Explorer.checkout == null) {
			Explorer.checkout = new ExecuteButtonIcon(58, Screen.DECORATION_HEIGHT + 130, 32, 32, Main.checkoutSpr, () -> Main.newThread(() -> {
				List<RightClickOption> list = new ArrayList<>();
				int width = Texts.selectABranch.length() * 14;
				
				list.add(new RightClickOption(0, 0, width, 30, false, Texts.selectABranch, (a) -> {  }, ""));
				
				for (String s : Explorer.gitStatus.branches) {
					list.add(new RightClickOption(0, 0, width, 30, s, (a) -> { checkout(s); }, ""));
				}
				
				IDEComponent.addRightClickOptions(Main.explorer.getWidth() + 1, Explorer.checkout.getY(), list.toArray(new RightClickOption[list.size()]));
			}), "Checkout") {
				public void tick() {
					if (Explorer.gitStatus != null)
						enabled = Explorer.gitStatus.branches.length > 1;
					else
						enabled = false;	
					
					super.tick();
				}
			};
		}
		
		if (Explorer.renameBranch == null) {
			Explorer.renameBranch = new ExecuteButtonIcon(96, Screen.DECORATION_HEIGHT + 130, 32, 32, Main.renameBranchSpr, () -> Main.newThread(() -> {
				Explorer.setBranchName = new SetBranchName(0, Screen.DECORATION_HEIGHT + 165, 0, 30, true);

				IDEComponent.toAdd.add(Explorer.setBranchName);
				SetBranchName.added = true;
				Explorer.selected = null;
				
				Explorer.fetchStatus();
			}), Texts.renameBranch) {
				public void tick() {
					super.tick();
					
					caption = Texts.renameBranch;
				}
			};
		}
		
		if (Explorer.mergeBranch == null) {
			Explorer.mergeBranch = new ExecuteButtonIcon(134, Screen.DECORATION_HEIGHT + 130, 32, 32, Main.mergeBranchSpr, () -> Main.newThread(() -> {
				List<RightClickOption> list = new ArrayList<>();
				int width = Texts.selectABranch.length() * 14;
				
				list.add(new RightClickOption(0, 0, width, 30, false, Texts.selectABranch, (a) -> {  }, "", true));
				list.add(new RightClickOption(0, 0, width, 30, false, "Branch: " + Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch], (a) -> {  }, ""));
				
				for (String s : Explorer.gitStatus.branches) {
					if (s.equals(Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch])) continue;
					
					list.add(new RightClickOption(0, 0, width, 30, s, (a) -> {
						MessageBox.showDialog(Texts.confirmMerge, new String[] { Texts.sureMerge, s + " -> " + Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch] }, new String[] { Texts.yes, Texts.no }, new Execute[] { () -> { merge(s, Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch]); }, () -> { } });
					}, ""));
				}
				
				IDEComponent.addRightClickOptions(Main.explorer.getWidth() + 1, Explorer.checkout.getY(), list.toArray(new RightClickOption[list.size()]));
			}), Texts.mergeBranches) {
				public void tick() {
					if (Explorer.gitStatus != null)
						enabled = Explorer.gitStatus.branches.length > 1;
					
					super.tick();
					
					caption = Texts.mergeBranches;
				}
			};
		}
		
		if (Explorer.deleteBranch == null) {
			Explorer.deleteBranch = new ExecuteButtonIcon(172, Screen.DECORATION_HEIGHT + 130, 32, 32, Main.deleteBranchSpr, () -> Main.newThread(() -> {
				List<RightClickOption> list = new ArrayList<>();
				int width = Texts.selectABranch.length() * 14;
				
				list.add(new RightClickOption(0, 0, width, 30, false, Texts.selectABranch, (a) -> {  }, "", true));
				
				for (String s : Explorer.gitStatus.branches) {
					list.add(new RightClickOption(0, 0, width, 30, s, (a) -> { 
						MessageBox.showDialog(Texts.confirmDelete, new String[] { Texts.sureDeleteBranch, s + "?" }, new String[] { Texts.yes, Texts.no }, new Execute[] { () -> { delete(s); }, () -> { } });
					}, ""));
				}
				
				IDEComponent.addRightClickOptions(Main.explorer.getWidth() + 1, Explorer.checkout.getY(), list.toArray(new RightClickOption[list.size()]));
			}), Texts.deleteBranch) {
				public void tick() {
					enabled = Explorer.gitStatus.branches.length > 1;
					
					super.tick();
					
					caption = Texts.deleteBranch;
				}
			};
		}
		
		if (Explorer.stageAll == null) {
			Explorer.stageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 440, Main.explorer.getWidth() - 40, 20, Texts.stageAll, () -> Main.newThread(() -> {
				actions.add(new GitAction("git add", ActionState.PROGRESS, null));
				String[] output = Main.runCommand(Main.baseFolder, "git add .");
				
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				
				actions.set(actions.size() - 1, new GitAction("git add", getState(error, warn), output)); }), true) {
				public void tick() {
					super.tick();
					
					text = Texts.stageAll;
				}
			};
		}
		
		if (Explorer.unstageAll == null) {
			Explorer.unstageAll = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 470, Main.explorer.getWidth() - 40, 20, Texts.unstageAll, () -> Main.newThread(() -> {
				actions.add(new GitAction("git reset", ActionState.PROGRESS, null));
				String[] output = Main.runCommand(Main.baseFolder, "git reset");
				
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				actions.set(actions.size() - 1, new GitAction("git reset", getState(error, warn), output)); }), true) {
				public void tick() {
					super.tick();
					
					text = Texts.unstageAll;
				}
			};
		}
		
		if (Explorer.commit == null) {
			Explorer.commit = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 540, Main.explorer.getWidth() - 82, 20, (Main.lang == Language.PORT ? Texts.create + " " : "") + "Commit", () -> Main.newThread(() -> {
				Explorer.setCommitName = new SetCommitName(0, Screen.DECORATION_HEIGHT + 600, 0, 30);

				IDEComponent.toAdd.add(Explorer.setCommitName);
				SetCommitName.added = true;
				Explorer.selected = null;
				}), true) {
				public void tick() {
					width = Main.explorer.getWidth() - 82;
					text = (Main.lang == Language.PORT ? Texts.create + " " : "") + "Commit";
					
					if (leftClicked() && enabled)
						execute.execute();
				}
			};
		}
		
		if (Explorer.allowEmpty == null) {
			Explorer.allowEmpty = new ToggleButton(Main.explorer.getWidth() - 52, Screen.DECORATION_HEIGHT + 533, 32, 32, Main.allowEmptySpr, false, Texts.allowEmpty) {
				public void tick() {
					super.tick();
					
					x = Main.explorer.getWidth() - 52;
					caption = Texts.allowEmpty;
				}
			};
		}
		
		if (Explorer.push == null) {
			Explorer.push = new ExecuteButton(20, Screen.DECORATION_HEIGHT + 570, Main.explorer.getWidth() - 82, 20, Texts.push, () -> Main.newThread(() -> {
				List<RightClickOption> list = new ArrayList<>();
				int width = Texts.selectARepository.length() * 14;
				
				list.add(new RightClickOption(0, 0, width, 30, false, Texts.selectARepository, (a) -> {  }, "", true));
				list.add(new RightClickOption(0, 0, width, 30, false, "Branch: " + Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch], (a) -> {  }, "", false));
				
				for (String s : Explorer.gitStatus.remoteRepos) {
					list.add(new RightClickOption(0, 0, width, 30, s, (a) -> { push(s, Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch], false); }, ""));
				}
				
				IDEComponent.addRightClickOptions(Main.explorer.getWidth() + 1, Explorer.push.getY(), list.toArray(new RightClickOption[list.size()]));
				}), false) {
				public void tick() {
					width = Main.explorer.getWidth() - 82;
					
					if (leftClicked() && enabled)
						execute.execute();
					
					text = Texts.push;
					
					if (Explorer.gitStatus.remoteRepos.length == 0)
						enabled = false;
					else
						enabled = true;
				}
			};
		}
		
		if (Explorer.forcePush == null) {
			Explorer.forcePush = new ToggleButton(Main.explorer.getWidth() - 52, Screen.DECORATION_HEIGHT + 567, 32, 32, Main.forcePushSpr, false, Texts.forcePush, false) {
				public void tick() {
					super.tick();
					
					x = Main.explorer.getWidth() - 52;
					caption = Texts.forcePush;
					
					if (Explorer.gitStatus.remoteRepos.length == 0)
						enabled = false;
					else
						enabled = true;
				}
			};
		}
		
		IDEComponent.toAdd.add(Explorer.createBranch);
		IDEComponent.toAdd.add(Explorer.checkout);
		IDEComponent.toAdd.add(Explorer.renameBranch);
		IDEComponent.toAdd.add(Explorer.mergeBranch);
		IDEComponent.toAdd.add(Explorer.deleteBranch);
		IDEComponent.toAdd.add(Explorer.stageAll);
		IDEComponent.toAdd.add(Explorer.unstageAll);
		IDEComponent.toAdd.add(Explorer.commit);
		IDEComponent.toAdd.add(Explorer.push);
		IDEComponent.toAdd.add(Explorer.forcePush);
		IDEComponent.toAdd.add(Explorer.allowEmpty); // coloca por cima por causa da caption
	}
	
	public static synchronized void dispose() {
		IDEComponent.toRemove.add(Explorer.initRepo);
		IDEComponent.toRemove.add(Explorer.cloneURL);
		IDEComponent.toRemove.add(Explorer.clone);
		IDEComponent.toRemove.add(Explorer.lastAction);
		
		IDEComponent.toRemove.add(Explorer.createBranch);
		IDEComponent.toRemove.add(Explorer.checkout);
		IDEComponent.toRemove.add(Explorer.renameBranch);
		IDEComponent.toRemove.add(Explorer.mergeBranch);
		IDEComponent.toRemove.add(Explorer.deleteBranch);
		IDEComponent.toRemove.add(Explorer.stageAll);
		IDEComponent.toRemove.add(Explorer.unstageAll);
		IDEComponent.toRemove.add(Explorer.commit);
		IDEComponent.toRemove.add(Explorer.allowEmpty);
		IDEComponent.toRemove.add(Explorer.push);
		IDEComponent.toRemove.add(Explorer.forcePush);
	}
}
