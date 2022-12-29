package ide.components;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

import ide.explorer.Explorer;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.input.KeyInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;

public class CustomMessageBox extends IDEComponent {
	
	protected List<IDEComponent> innerComponents;
	protected boolean closing = false;
	
	private boolean doNativeClosing = true;
	
	protected CustomMessageBox(int width, int height, List<IDEComponent> innerComponents) {
		super((Main.screen.getWidth() / 2) - (width / 2), Screen.DECORATION_HEIGHT - height, width, height, null);
		
		MessageBox.active = true;
		
		if (innerComponents == null) {
			throw new IllegalArgumentException("The components list cannot be null!");
		}
		
		this.innerComponents = innerComponents;
		
		new Thread() {
			public void run() {
				while (y < Screen.DECORATION_HEIGHT) {
					y += 2;
					
					for (IDEComponent c : innerComponents)
						c.y += 2;
					
					Main.canRunLoop = true;
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
	}
	
	protected CustomMessageBox(int width, int height, List<IDEComponent> innerComponents, boolean doNativeClosing) {
		this(width, height, innerComponents);
		
		this.doNativeClosing = doNativeClosing;
	}
	
	public void doClose() {
		closing = true;
	}
	
	public void close() {
		CustomMessageBox m = this;
		
		new Thread() {
			public void run() {
				while (y > Screen.DECORATION_HEIGHT - height - 1) {
					y--;
					
					for (IDEComponent c : innerComponents)
						c.y--;
					
					Main.canRunLoop = true;
					
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				
				IDEComponent.toRemove.add(m);
				MessageBox.active = false;
				Explorer.selected = null;
			}
		}.start();
	}
	
	// Shows up a dialog and returns the name of the clicked button
	public static void showDialog(int width, int height, List<IDEComponent> innerComponents) {
		if (MessageBox.active) return;
		
		CustomMessageBox box = new CustomMessageBox(width, height, components);
		
		IDEComponent.toAdd.add(box);
	}
	
	public void tick() {
		for (IDEComponent c : innerComponents) {
			c.tick();
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE && doNativeClosing && !(SetBranchName.added || SetFileName.added || CommandTerminal.active || SetBranchName.added || SetCommitName.added || RightClickOption.isRightClickActive())) {
			KeyInput.updateKeys();
			
			closing = true;
		}
		
		if (closing)
			close();
		}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		g.setColor(Colors.textLight);
		g2.setStroke(new BasicStroke(2f));
		g2.drawRect(x, y, width, height);
		
		for (IDEComponent c : innerComponents) {
			c.render(g);
		}
	}

}
