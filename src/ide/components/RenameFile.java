package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;

import ide.codeeditor.Tab;
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
import ide.util.Texts;

public class RenameFile extends IDEComponent {

	public static boolean added = false;
	
	private StringBuilder text;
	private int cursorIndex = 0;
	
	public File old;
	
	private boolean showCursor;
	private Animation cursor;
	
	private boolean canShow = false;
	
	public RenameFile(int x, int y, int width, int height, File old) {
		super(x, y, width, height, null);
		
		this.old = old;
		
		canShow = false;
		
		cursor = new Animation(2, true) { // 20
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

		text = new StringBuilder(old.getName());
		
		cursorIndex = text.length();
	}
	
	private boolean hasIllegalChars(String s) {
		return s.contains("\\") || s.contains("/") || s.contains(":") || s.contains("*") || s.contains("?") || s.contains("<") || s.contains(">") || s.contains("|");
	}
	
	public void tick() {
		//if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).y + 30;
		
		if (text.length() > Main.explorer.maxFileCreateWidth) width = Main.screen.getWidth();
		else width = Main.explorer.width - 3;
		
		if (MouseInput.isLeftPressed() && !leftClicked()) {
			IDEComponent.toRemove.add(this);
			added = false;
		}
		
		if (KeyInput.isKeyPressed() && Character.isLetter(KeyInput.getCharPressed()) || KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) canShow = true;
		
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
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				if (cursorIndex > text.length() - 1) return;
				
				text.deleteCharAt(cursorIndex);
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				if (text.length() == 0 || text.toString().endsWith(".")) return;
				if (hasIllegalChars(text.toString())) return;
				if (ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath()))) return;
				//if (text.toString().trim().equals("")) return;
				
				File newf = new File(Explorer.getScopePath() + "/" + text.toString());
				old.renameTo(newf);
				
				IDEComponent.toRemove.add(this);
				added = false;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.files = ListableFile.loadFolder(Explorer.scope);
				
				for (Tab t : Main.editor.tabs) {
					if (t.regent.getRegent().equals(old)) {
						t.regent = ListableFile.newListableFile(newf);
					}
				}
				
				//if (!ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath())))
				
				//ListableFile searched = ListableFile.search(newf, newf.getParentFile());
				/*Tab equivalent = Tab.searchTab(searched);
				equivalent.close();*/
				
				/*if (!ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath())))
					ListableFile.addTab(searched, true);
				
				Main.editor.tabs.forEach((t) -> t.refreshRegent());*/
				
				return;
			}
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = Main.editor.addAccents(keyCode, c);
			
			if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256) return;
			
			cursorIndex++;
			
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
		
		Fonts.drawString(text.toString(), x, y + 5, new IDEFont(Fonts.otherNormal, 18), g); // depois colocar drawchars e o sistema de fontes
		
		g.setColor(Color.white);
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor)
			g.fillRect(cursorIndex * (16 - 2), y, 2, height);
		
		Fonts.drawString(Texts.renameFile + "...", MouseInput.getMouseX() + 30, MouseInput.getMouseY() - 40, new IDEFont(Fonts.lightGrayNormal, 20), g);
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.enter_Rename, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		
		if (ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath())) && canShow)
			Fonts.drawString(Texts.fileExists, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, 20), g);
		
		if (text.toString().equals("") && canShow)
			Fonts.drawString(Texts.cannotBeEmpty, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, 20), g);
		
		if (text.toString().endsWith("."))
			Fonts.drawString(Texts.cannotEndDot, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, 20), g);
		
		if (hasIllegalChars(text.toString()))
			Fonts.drawString(Texts.fileNameIllegal, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, 20), g);
		
		//if (text.toString().trim().equals(""))
		//	Fonts.drawString(Texts.cannotBeOnlySpaces, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, 20), g);
	}
}
