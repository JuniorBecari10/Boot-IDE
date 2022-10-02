package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.git.GitAction;
import ide.git.GitCore;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class SetCommitName extends IDEComponent {
	
	public static boolean added = false;
	
	private StringBuilder text = new StringBuilder();
	private int cursorIndex = 0;

	public SetCommitName(int x, int y, int width, int height) {
		super(x, y, width, height, null);
	}
	
	/*private boolean hasIllegalChars(String s) {
		return s.contains("\\") || s.contains("@{") || (s.length() == 1 && s.contains("@"));
	}*/
	
	public void tick() {
		if (text.length() > Main.explorer.maxFileCreateWidth) width = Main.screen.getWidth();
		else width = Main.explorer.getWidth() - 2;
		
		if ((MouseInput.isLeftPressed() && !leftClicked()) || KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
			KeyInput.updateKeys();
			
			IDEComponent.toRemove.add(this);
			added = false;
		}
	}
	
	public synchronized void type() {
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
			
			//if (KeyInput.isKeyPressed() && Character.isLetter(KeyInput.getCharPressed()) || KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) canShow = true;
			
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
				if (text.length() == 0) return;
				
				String[] output = Main.runCommand(Main.baseFolder, "git commit -m" + (Explorer.allowEmpty.getState() ? " --allow-empty" : "") + " " + "\"" + text.toString().replace("\"", "\\\"") + "\"");
				
				boolean error = Main.isError(output);
				boolean warn = Main.isWarning(output);
				
				GitCore.actions.add(new GitAction("git commit", GitCore.getState(error, warn), output));
				
				Explorer.fetchStatus();
				
				IDEComponent.toRemove.add(this);
				added = false;
				
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
		
		if (Main.editor.showCursor)
			g.fillRect(cursorIndex * (CodeEditor.DEFAULT_FONT_SIZE - 4), y, 2, height);
		
		Fonts.drawString(Texts.createNewCommit + "...", MouseInput.getMouseX() + 30, MouseInput.getMouseY() - 35, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString(Texts.enter_Create, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 25, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		// Fonts.drawString(Texts.fileExists, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		/*if (hasIllegalChars(text.toString()))
			Fonts.drawString(Texts.commitNameIllegal, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 50, new IDEFont(Fonts.errorNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		*/
		Fonts.drawString("[Ctrl + C] " + Texts.copy, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 75, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + V] " + Texts.paste, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 100, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + X] " + Texts.cut, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 125, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		Fonts.drawString("[Ctrl + Del] " + Texts.delete, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 150, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
		
		Fonts.drawString("Branch: " + Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch], MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 195, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
	}
}