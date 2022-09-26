package ide.components;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import ide.codeeditor.CodeEditor;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.util.Colors;

public class MessageBox extends IDEComponent {

	public String title;
	public String[] text;
	public MessageBoxType type;
	public String[] buttons;
	
	private MessageBox(String title, String[] text, MessageBoxType type, String[] buttons) {
		super(Main.screen.getWidth() / 4, 0 - Main.screen.getHeight() / 4, Main.screen.getWidth() / 2, Main.screen.getHeight() / 4, null);
		
		this.title = title;
		this.text = text;
		this.type = type;
		this.buttons = buttons;
		
		System.out.println(title);
		
		new Thread() {
			public void run() {
				while (y < 0) {
					y++;
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
	
	public static String showDialog(String title, String[] text, MessageBoxType type, String[] buttons) {
		IDEComponent.toAdd.add(new MessageBox(title, text, type, buttons));
		
		return "";
	}
	
	public void tick() {
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
			KeyInput.updateKeys();
			
			new Thread() {
				public void run() {
					while (y > 0 - Main.screen.getHeight() / 4 - 1) {
						y--;
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
		
		if (y <= 0 - Main.screen.getHeight() / 4 - 1)
			IDEComponent.toRemove.add(this);
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		g.setColor(Colors.textLight);
		g2.setStroke(new BasicStroke(2f));
		g2.drawRect(x, y, width, height);
		
		Fonts.drawString(title, (Main.screen.frame.getWidth() / 2) - (title.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) / 2, y + 10, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g2);
	}

}
