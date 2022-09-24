package ide.git;

import java.util.Date;

public class GitAction {
	public String name;
	public ActionState state;
	public Date date;
	public String[] output;
	
	public GitAction(String name, ActionState state, Date date, String[] output) {
		this.name = name;
		this.state = state;
		this.date = date;
		this.output = output;
	}
	
	public GitAction(String name, ActionState state, String[] output) {
		this.name = name;
		this.state = state;
		this.date = new Date();
		this.output = output;
	}
}
