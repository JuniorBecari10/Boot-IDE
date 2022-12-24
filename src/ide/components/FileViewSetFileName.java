package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.explorer.Explorer;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.explorercomponents.FileView;
import ide.explorercomponents.FileViewFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Animation;
import ide.util.Colors;
import ide.util.Texts;

public class FileViewSetFileName extends IDEComponent {
	
	public static boolean added = false;
	
	private StringBuilder text = new StringBuilder();
	private int cursorIndex = 0;
	
	private boolean isFile;
	
	private boolean showCursor;
	private Animation cursor;
	
	private boolean canShow = false;
	private FileView view;

	public FileViewSetFileName(int x, int y, int width, int height, boolean isFile, FileView view) {
		super(x, y, width, height, null);
		
		this.isFile = isFile;
		this.view = view;
		
		cursor = new Animation() { // 20
			private boolean flip = false;
			
			public void play() {
				while (true) {
					showCursor = !flip;
	
					flip = !flip;
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		new Thread() {
			public void run() {
				cursor.play();
			}
		}.start();
		
		while (this.y > Main.screen.getHeight() - 30) {
			for (FileViewFile f : view.files)
				f.setY(f.getY() - FileView.FILE_HEIGHT);
			
			this.y -= 30;
		}
	}
	
	private boolean hasIllegalChars(String s) {
		return s.contains("\\") || s.contains("/") || s.contains(":") || s.contains("*") || s.contains("?") || s.contains("<") || s.contains(">") || s.contains("|");
	}
	
	public void tick() {
		if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).y + 30;
		
		if (text.length() > Main.explorer.maxFileCreateWidth) width = Main.screen.getWidth();
		else width = view.width - 2;
		
		if (MouseInput.isLeftPressed() && !leftClicked()) {
			IDEComponent.toRemove.add(this);
			added = false;
		}
	}
	
	public synchronized void type() {
		if (!SetFileName.added || CommandTerminal.active || RenameFile.added || Explorer.selected != null) return;
		
		if (KeyInput.isKeyPressed()) {
			// Shortcuts Area
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C - Copiar (Tudo)
				CodeEditor.copyText(text.toString());
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V - Colar
				if (cursorIndex >= text.length()) {
					text.append(CodeEditor.clipboard);
					cursorIndex += CodeEditor.clipboard.length();
				}
				else {
					text.insert(cursorIndex, CodeEditor.clipboard);
					cursorIndex += CodeEditor.clipboard.length();
				}
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X - Recortar (Tudo)
				CodeEditor.copyText(text.toString());
				
				text = new StringBuilder();
				cursorIndex = 0;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) { // Ctrl + Del (Deletar Tudo)
				text = new StringBuilder();
				cursorIndex = 0;
			}
			
			if (KeyInput.isKeyPressed() && Character.isLetter(KeyInput.getCharPressed()) || KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) canShow = true;
			
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
				
				File f = new File(view.getFolder() + File.separator + text.toString());
				
				if (ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath()))) {
					if (!CodeEditor.isBinary(ListableFile.getFileExtension(f)))
						ListableFile.addTab(ListableFile.search(f, f.getParentFile()), true);
					
					IDEComponent.toRemove.add(this);
					added = false;
					
					return;
				}
				
				//if (text.toString().trim().equals("")) return;
				
				if (isFile)
					try {
						f.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				else
					f.mkdir();
				
				if (!f.exists()) {
					CodeEditor.setSystemLook();
					
					JOptionPane.showMessageDialog(null, Texts.anErrorOccurred, Texts.errorCreatingFile, JOptionPane.OK_OPTION);
					
					IDEComponent.toRemove.add(this);
					added = false;
					
					return;
				}
				
				IDEComponent.toRemove.add(this);
				added = false;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.files = ListableFile.loadFolder(Explorer.scope);
				
				if (!CodeEditor.isBinary(ListableFile.getFileExtension(f)))
					ListableFile.addTab(ListableFile.search(f, f.getParentFile()), true);
				
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
		g.fillRect(0, 0, Main.screen.getWidth(), Main.screen.getHeight());
		
		g.setColor(Colors.explorerLight);
		g.fillRect(x, y, width, height);
		
		Fonts.drawString(text.toString(), x, y + 5, new IDEFont(Fonts.otherNormal, CodeEditor.DEFAULT_FONT_SIZE), g); // depois colocar drawchars e o sistema de fontes
		
		g.setColor(Colors.other);
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor)
			g.fillRect(x + (cursorIndex * (CodeEditor.DEFAULT_FONT_SIZE - 4)), y, 2, height);
		
		if (isFile)
			Fonts.drawString(Texts.createFile + "...", MouseInput.getMouseX() + 30, MouseInput.getMouseY() - 35, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		else
			Fonts.drawString(Texts.createFolder + "...", MouseInput.getMouseX() + 30, MouseInput.getMouseY() - 35, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString(Texts.enter_Create, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 25, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (ListableFile.hasDuplicateFileNames(text.toString(), new File(Explorer.getScopePath())))
			Fonts.drawString(Texts.fileExists, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (text.toString().equals("") && canShow)
			Fonts.drawString(Texts.cannotBeEmpty, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (text.toString().endsWith("."))
			Fonts.drawString(Texts.cannotEndDot, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (hasIllegalChars(text.toString()))
			Fonts.drawString(Texts.fileNameIllegal, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		Fonts.drawString("[Ctrl + C] " + Texts.copy, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 75, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + V] " + Texts.paste, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 100, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + X] " + Texts.cut, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 125, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + Del] " + Texts.delete, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 150, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		if (!isFile) return;
		
		for (FileType f : ListableFile.types) {
			if (f.getExtension().equalsIgnoreCase(ListableFile.getFileExtension(text.toString()))) {
				g.drawImage(f.getIcon(), MouseInput.getMouseX() - 45, MouseInput.getMouseY() - 16, 32, 32, null);
				
				return;
			}
			
			else if (f.getExtension().equalsIgnoreCase(text.toString())) {
				g.drawImage(f.getIcon(), MouseInput.getMouseX() - 45, MouseInput.getMouseY() - 16, 32, 32, null);
				
				return;
			}
		}
		
		if (!text.toString().isEmpty())
			g.drawImage(Main.UNKNOWN_FILE_ICON, MouseInput.getMouseX() - 45, MouseInput.getMouseY() - 16, 32, 32, null);
		
		//if (text.toString().trim().equals(""))
		//	Fonts.drawString(Texts.cannotBeOnlySpaces, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
	}
}