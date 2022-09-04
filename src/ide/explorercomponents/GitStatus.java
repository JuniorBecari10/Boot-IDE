package ide.explorercomponents;

import ide.main.Main;

public class GitStatus {
	public String[] branches;
	public int currentBranch; // index
	
	public String[] changedFiles;
	
	private GitStatus(String[] branches, int currentBranch, String[] changedFiles) {
		this.branches = branches;
		this.currentBranch = currentBranch;
		this.changedFiles = changedFiles;
	}
	
	public String toString() {
		StringBuilder bl = new StringBuilder();
		
		bl.append("GitStatus | Branches:");
		
		for (String b : branches) {
			bl.append(" " + b + ",");
		}
		
		bl.deleteCharAt(bl.length() - 1);
		bl.append(" ");
		
		String currentBranchStr = currentBranch >= 0 ? branches[currentBranch] : "None";
		bl.append("| Current Branch: " + currentBranchStr + " (index: " + currentBranch + ") ");
		
		bl.append("| Changed Files: ");
		
		for (String f : changedFiles) {
			bl.append(f + ", ");
		}
		
		return bl.toString();
	}
	
	public static GitStatus fetch() {
		String[] files = Main.runCommand(Main.baseFolder, "git status --porcelain");
		String[] branchesFetch = Main.runCommand(Main.baseFolder, "git branch");
		
		String[] branches = new String[branchesFetch.length];
		int currentBranch = -1;
		
		/*if (files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				files[i] = files[i].substring(1);
			}
		}*/
		
		if (branchesFetch.length > 0) {
			for (int i = 0; i < branchesFetch.length; i++) {
				String s = branchesFetch[i];
				
				String[] split = s.split(" ");
				branches[i] = split[split.length - 1];
				
				if (s.contains("*")) {
					currentBranch = i;
				}
			}
		}
		else {
			String[] status = Main.runCommand(Main.baseFolder, "git status");
			
			String[] split = status[0].split(" ");
			
			if (split.length > 0) {
				branches = new String[1];
				branches[0] = split[split.length - 1];
				
				currentBranch = 0;
			}
		}
		
		return new GitStatus(branches, currentBranch, files);
	}
}
