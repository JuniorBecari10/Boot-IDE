package ide.components;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.explorer.Explorer;
import ide.explorercomponents.Execute;
import ide.explorercomponents.ExecuteButton;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;

public class MessageBox extends IDEComponent {

	public String title;
	public String[] text;
	public Execute[] actions;
	
	public ExecuteButton clicked;
	private List<ExecuteButton> buttonsList = new ArrayList<>();
	
	public static boolean active = false;
	
	private boolean closing = false;
	
	public static final int HEIGHT = 200;
	
	private MessageBox(String title, String[] text, String[] buttons, Execute[] actions) {
		super(Main.screen.getWidth() / 4, Screen.DECORATION_HEIGHT - Main.screen.getHeight() / 4, Main.screen.getWidth() / 2, HEIGHT, null);
		
		if (buttons.length != actions.length) throw new RuntimeException("Array lengths don't match!");
		
		active = true;
		
		this.title = title;
		this.text = text;
		this.actions = actions;
		
		int i = 0;
		for (String s : buttons) {
			buttonsList.add(new ExecuteButton(x + 10, ((HEIGHT - 5 - (buttons.length * 30)) + (30 * i)) + 30, (Main.screen.getWidth() / 2) - 20, 20, s, actions[i], true, true) {
				public void tick() {
					if (leftClicked() && enabled) {
						this.execute.execute();
						clicked = this;
						closing = true;
					}
				}
			});
			
			i++;
		}
		
		if (Explorer.allowAnimations) {
			new Thread() {
				public void run() {
					while (y < Screen.DECORATION_HEIGHT) {
						y++;
						Main.canRunLoop = true;
						
						int i = 0;
						for (ExecuteButton b : buttonsList) {
							b.y = (((HEIGHT - 5 - (buttonsList.size() * 30)) + (30 * i++))) + y;
						}
						
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		}
		else {
			y = Screen.DECORATION_HEIGHT;
		}
	}
	
	public void close() {
		if (Explorer.allowAnimations) {
			new Thread() {
				public void run() {
					while (y > Screen.DECORATION_HEIGHT - Main.screen.getHeight() / 4 - 1) {
						y--;
						Main.canRunLoop = true;
						
						int i = 0;
						for (ExecuteButton b : buttonsList) {
							b.y = (((HEIGHT - 5 - (buttonsList.size() * 30)) + (30 * i++))) + y;
						}
						
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
				}
			}.start();
		} else {
			y = Screen.DECORATION_HEIGHT - Main.screen.getHeight() / 4 - 1;
		}
	
		if (y <= Screen.DECORATION_HEIGHT - Main.screen.getHeight() / 4 - 1) {
			IDEComponent.toRemove.add(this);
			active = false;
		}
	}
	
	// Shows up a dialog and returns the name of the clicked button
	public static void showDialog(String title, String[] text, String[] buttons, Execute[] actions) {
		if (active) return;
		
		MessageBox box = new MessageBox(title, text, buttons, actions);
		
		IDEComponent.toAdd.add(box);
	}
	
	public void tick() {
		for (ExecuteButton b : buttonsList) {
			b.tick();
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
		
		Fonts.drawString(title, (Main.screen.frame.getWidth() / 2) - (title.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) / 2, y + 10, new IDEFont(Fonts.lightGrayNormal, 16), g);
		
		g.drawLine(x + 15, y + 40, x + width - 15, y + 40);
		
		int i = 0;
		for (String s : text) {
			Fonts.drawString(s, x + 10, y + 50 + (i++ * 20), new IDEFont(Fonts.lightGrayNormal, 16), g);
		}
		
		for (ExecuteButton b : buttonsList) {
			b.render(g);
		}
	}

}
