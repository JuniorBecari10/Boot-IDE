package ide.git;

import java.util.ArrayList;
import java.util.List;

import ide.main.Main;

public class GitStatus {
	public String[] branches;
	public int currentBranch; // index
	
	public String[] changedFiles;
	public String[] stagedFiles;
	
	public String[] remoteRepos;
	
	private GitStatus(String[] branches, int currentBranch, String[] changedFiles, String[] stagedFiles, String[] remoteRepos) {
		this.branches = branches;
		this.currentBranch = currentBranch;
		this.changedFiles = changedFiles;
		this.stagedFiles = stagedFiles;
		this.remoteRepos = remoteRepos;
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
		
		bl.append("| Changed Files:");
		
		for (String f : changedFiles) {
			bl.append(" " + f + ",");
		}
		
		bl.deleteCharAt(bl.length() - 1);
		
		return bl.toString();
	}
	
	public static GitStatus fetch() {
		String[] files = Main.runCommand(Main.baseFolder, "git status --porcelain");
		String[] branchesFetch = Main.runCommand(Main.baseFolder, "git branch");
		String[] reposFetch = Main.runCommand(Main.baseFolder, "git remote");
		
		/*String[] commitsNames = Main.runCommand(Main.baseFolder, "git log --pretty=oneline");
		String[] commitsDates = Main.runCommand(Main.baseFolder, "git log --pretty=format:\"%h %an %ad\"");*/
		
		/*Commit[] comm = new Commit[commitsNames.length];
		
		for (int i = 0; i < comm.length; i++) {
			comm[i] = new Commit();
		}*/
		
		List<String> stagedFilesList = new ArrayList<>();
		
		for (String s : files) {
			if (s.startsWith("A")) {
				String[] split = s.split(" ");
				
				if (split.length > 0)
					stagedFilesList.add(split[split.length - 1]);
			}
		}
		
		String[] branches = new String[branchesFetch.length];
		int currentBranch = -1;
		
		if (files.length > 0) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].length() > 0)
					files[i] = files[i].substring(1);
			}
		}
		
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
		
		return new GitStatus(branches, currentBranch, files, stagedFilesList.toArray(new String[0]), reposFetch);
	}
}
