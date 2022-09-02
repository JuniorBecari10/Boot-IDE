package ide.explorercomponents;

import java.util.Date;

public class GitAction {
	public String name;
	public ActionState state;
	public Date date;
	
	public GitAction(String name, ActionState state, Date date) {
		this.name = name;
		this.state = state;
		this.date = date;
	}
	
	public GitAction(String name, ActionState state) {
		this.name = name;
		this.state = state;
		this.date = new Date();
	}
}
