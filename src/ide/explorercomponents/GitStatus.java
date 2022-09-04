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
	
	public static GitStatus fetch() {
		String[] files = Main.runCommand(Main.baseFolder, "git status --porcelain");
		String[] branchesFetch = Main.runCommand(Main.baseFolder, "git branch");
		
		String[] branches = new String[branchesFetch.length];
		int currentBranch = -1;
		
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
			
			branches = new String[0];
			branches[0] = split[split.length - 1];
		}
		
		return new GitStatus(branches, currentBranch, files);
	}
}
