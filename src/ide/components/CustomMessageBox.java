package ide.components;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.List;

import ide.input.KeyInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;

public class CustomMessageBox extends IDEComponent {
	
	protected List<IDEComponent> innerComponents;
	private boolean closing = false;
	
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
	
	public void close() {
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
			}
		}.start();
	
		if (y <= Screen.DECORATION_HEIGHT - height - 1) {
			IDEComponent.toRemove.add(this);
			MessageBox.active = false;
		}
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
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
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
