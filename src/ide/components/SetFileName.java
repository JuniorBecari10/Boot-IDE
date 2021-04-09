package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import ide.codeeditor.CodeEditor;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;

public class SetFileName extends IDEComponent {
	
	public static boolean added = false;
	
	private StringBuilder text = new StringBuilder();
	private int cursorIndex = 0;
	
	private boolean isFile;
	
	private boolean showCursor;
	private Animation cursor;

	public SetFileName(int x, int y, int width, int height, boolean isFile) {
		super(x, y, width, height, null);
		
		this.isFile = isFile;
		
		cursor = new Animation(2, true) {
			private boolean flip = false;
			
			public void play() {
				showCursor = !flip;
				
				flip = !flip;
				
				super.play();
			}
		};
		
		new Thread() {
			public void run() {
				cursor.play();
			}
		}.start();
	}
	
	public void tick() {
		if (MouseInput.isLeftPressed() && !leftClicked()) {
			IDEComponent.toRemove.add(this);
			added = false;
		}
		
		if (KeyInput.isKeyPressed()) {
			KeyInput.updateKeys();
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
				IDEComponent.toRemove.add(this);
				added = false;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && cursorIndex > 0) cursorIndex--;
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT && cursorIndex < text.length()) cursorIndex++;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE && cursorIndex > 0) {
				text.deleteCharAt(cursorIndex - 1);
				cursorIndex--;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE && cursorIndex > 0) {
				if (text.length() == 0) text.append(" ");
				else text.insert(cursorIndex, " ");
				
				cursorIndex++;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE && cursorIndex < text.length()) {
				text.deleteCharAt(cursorIndex);
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				if (text.length() == 0) return;
				
				File f = new File(Explorer.getScopePath() + "/" + text.toString());
				
				if (isFile)
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				else
					f.mkdir();
				
				IDEComponent.toRemove.add(this);
				added = false;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.files = ListableFile.loadFolder(Explorer.scope);
				
				return;
			}
			
			if (KeyInput.getCharPressed() < 33 || KeyInput.getCharPressed() > 256) return;
			
			cursorIndex++;
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = Main.editor.addAccents(keyCode, c);
			
			if (text.length() == 0) text.append(c);
			else text.insert(cursorIndex - 1, c);
		}
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(new Color(0, 0, 0, 0.3f));
		g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);
		
		g.setColor(Colors.explorerLight);
		g.fillRect(x, y, width, height);
		
		Fonts.drawString(text.toString(), x, y + 5, new IDEFont(Fonts.normal, 18), g); // depois colocar drawchars e o sistema de fontes
		
		g.setColor(Color.white);
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor)
			g.fillRect(cursorIndex * (CodeEditor.FONT_SIZE - 2), y, 2, height);
		
		Fonts.drawString("[Esc] Cancelar", MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Enter] Criar", MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
