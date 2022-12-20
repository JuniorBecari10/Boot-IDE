package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.event.KeyEvent;

import ide.codeeditor.CodeEditor;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.util.Colors;

public class InputBox extends IDEComponent {

	protected StringBuilder text;
	protected int cursorIndex = 0;
	
	private int scroll = 0;
	private boolean isMessageBox = false;
	
	public InputBox(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		text = new StringBuilder();
	}
	
	public InputBox(int x, int y, int width, int height, boolean isMessageBox) {
		super(x, y, width, height, null);
		
		text = new StringBuilder();
		this.isMessageBox = true;
	}
	
	public InputBox(int x, int y, int width, int height, boolean isMessageBox, String t) {
		super(x, y, width, height, null);
		
		text = new StringBuilder(t);
		this.isMessageBox = true;
		cursorIndex = t.length();
	}
	
	public boolean hovered() {
		if (SetBranchName.added || SetFileName.added || CommandTerminal.active || SetBranchName.added || SetCommitName.added || RightClickOption.isRightClickActive()) return false;
		
		if (!isMessageBox)
			return MessageBox.active;
		
		return super.hovered();
	}
	
	public void tick() {
		if (!isMessageBox) // arrumar
			width = Main.explorer.getWidth() - 40;
		
		// mover pra frente (o texto vai pra trás)
		while (x + 1 + (cursorIndex * (16 - 4)) - scroll > x + width)
			scroll += 12;
		// mover pra trás (o texto vai pra frente)
		while (x + 1 + (cursorIndex * (16 - 4)) - scroll < x || (x + 1 + (cursorIndex * (16 - 4)) - scroll == x + 1 && text.length() > 0))
			scroll -= 12;
		
		if (text.length() == 0 || scroll < 0)
			scroll = 0;
		
		if (leftClicked())
			Explorer.selected = this;
	}
	
	public synchronized void type() {
		if (KeyInput.isKeyPressed() && Explorer.selected == this) {
			KeyInput.updateKeys();
			
			// Shortcuts Area
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) {
				cursorIndex = 0;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END) {
				cursorIndex = text.length();
			}
			
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
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = Main.editor.addAccents(keyCode, c);
			
			if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256) return;
			
			cursorIndex++;
			
			if (text.length() == 0) text.append(c);
			else text.insert(cursorIndex - 1, c);
		}
	}
	
	public String getText() {
		return text.toString();
	}
	
	public void setText(String t) {
		text = new StringBuilder(t);
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLighter);
		//g.fillRect(x - 2, y - 2, width + 4, height + 4);
		g.fillRect(x, y + height + 2, width, 2);
		
		g.setColor(hovered() ? Colors.explorerLight : Colors.explorer);
		g.fillRect(x, y, width, height + 2);
		
		Fonts.drawString(getText(), (x + 2) - scroll, y + 2, new IDEFont(Fonts.otherNormal, 16), x, x + width, g);
		
		g.setColor(Colors.other);
		
		if (Explorer.selected == this && Main.editor.showCursor && x + 1 + (cursorIndex * (16 - 4)) - scroll < x + width)
			g.fillRect(x + 1 + (cursorIndex * (16 - 4)) - scroll, y, 2, height);
	}
}
