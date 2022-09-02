package ide.explorercomponents;

import java.awt.Graphics;

import ide.components.IDEComponent;

public class LastAction extends IDEComponent {

	public GitAction action;	
	
	public LastAction(int x, int y, int width, int height, GitAction action) {
		super(x, y, width, height, null);
		
		this.action = action;
	}
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		
	}
}
