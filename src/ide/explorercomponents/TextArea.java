package ide.explorercomponents;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.terminal.TerminalCore;
import ide.util.Colors;

public class TextArea extends IDEComponent {
	
	public static final int MARGIN = 5;
	
	public String[] lines;
	public boolean acceptInput = false;
	
	private int fontSize = 16;
	
	public int cursorX = 0;
	
	private int scrollX;

	public TextArea(int x, int y, int width, int height, String[] lines) {
		super(x, y, width, height, null);
		
		this.lines = lines;
	}
	
	public void type() {
		if (KeyInput.isKeyPressed() && Explorer.selected == this) {
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 61) {
				fontSize++;
			}
			else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_MINUS) {
				fontSize--;
				
				if (fontSize < 8) fontSize = 8;
			}
		}
		
		if (!acceptInput) return;
		
		StringBuilder text = new StringBuilder(lines[lines.length - 1]);
		
		KeyInput.updateKeys();
		
		// Shortcuts Area
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) {
			cursorX = 2;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END) {
			cursorX = text.length();
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C - Copiar (Tudo)
			CodeEditor.copyText(text.toString().substring(2));
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V - Colar
			if (cursorX >= text.length()) {
				text.append(CodeEditor.clipboard);
				cursorX += CodeEditor.clipboard.length();
			}
			else {
				text.insert(cursorX, CodeEditor.clipboard);
				cursorX += CodeEditor.clipboard.length();
			}
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X - Recortar (Tudo)
			CodeEditor.copyText(text.toString().substring(2));
			
			text = new StringBuilder();
			cursorX = 0;
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) { // Ctrl + Del (Deletar Tudo)
			text = new StringBuilder();
			cursorX = 0;
		}
		
		// Por causa do prompt
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && cursorX > 2) cursorX--;
		else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT && cursorX < text.length()) cursorX++;
		
		// Para não apagar o prompt
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE && cursorX > 2) {
			text.deleteCharAt(cursorX - 1);
			cursorX--;
			
			lines[lines.length - 1] = new String(text.toString());
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE && cursorX > 0) {
			if (text.length() == 0) text.append(" ");
			else text.insert(cursorX, " ");
			
			cursorX++;
			
			lines[lines.length - 1] = new String(text.toString());
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
			if (cursorX > text.length() - 1) return;
			
			text.deleteCharAt(cursorX);
			
			lines[lines.length - 1] = new String(text.toString());
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
			TerminalCore.selected.setLines(lines);
			TerminalCore.selected.write();
			
			TerminalCore.selected.commandRunning = true;
			
			new Thread() {
				public void run() {
					String[] split = lines[lines.length - 1].split(" ");
					String[] c = new String[split.length - 1];
					
					for (int i = 0; i < c.length; i++) {
						c[i] = split[i + 1];
					}
					
					String command = String.join(" ", c);
					
					/*String[] o = */Main.runCommand(new File(Explorer.getScopePath()), "python3 " + Main.script.getAbsolutePath() + " " + command + " >> " + TerminalCore.selected.getLog().getAbsolutePath());
					
					Main.runCommand(new File(Main.userDir), "echo " + TerminalCore.prompt + " >> " + TerminalCore.selected.getLog().getAbsolutePath());
					
					TerminalCore.selected.commandRunning = false;
					TerminalCore.selected.read();
				}
			}.start();
			
			return;
		}
		
		int keyCode = KeyInput.getKeyCodePressed();
		char c = KeyInput.getCharPressed();
		
		c = Main.editor.addAccents(keyCode, c);
		
		if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256) return;
		
		cursorX++;
		
		if (text.length() == 0) text.append(c);
		else text.insert(cursorX - 1, c);
		
		lines[lines.length - 1] = new String(text.toString());
	}
	
	public void tick() {
		if (leftClicked())
			Explorer.selected = this;
		
		if (cursorX < 2) cursorX = 2;
		
		acceptInput = !TerminalCore.selected.commandRunning;

		// mover pra frente (o texto vai pra trás)
		while (x + 1 + (cursorX * (fontSize - 4)) - scrollX > width)
			scrollX += fontSize - 4;
		// mover pra trás (o texto vai pra frente)
		while (x + 1 + (cursorX * (fontSize - 4)) - scrollX < x || (x + 1 + (cursorX * (fontSize - 4)) - scrollX == x + 1 && lines[lines.length - 1].length() > 0))
			scrollX -= fontSize - 4;

		if (lines[lines.length - 1].length() == 0 || scrollX < 0 || cursorX <= 2)
			scrollX = 0;
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLight);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		int i = 0;
		for (String s : lines) {
			Fonts.drawString(s, x + 5 - scrollX, y + 5 + (i++ * (fontSize + MARGIN)), new IDEFont(Fonts.otherNormal, fontSize), x + width, g);
		}
		
		if (Main.editor.showCursor && Explorer.selected == this && !TerminalCore.selected.commandRunning) {
			g.setColor(Colors.other);
			g.fillRect(x + 5 + ((fontSize - 4) * cursorX) - scrollX, y + 5 + (fontSize + MARGIN) * (lines.length - 1), fontSize < 13 ? 1 : 2, fontSize + MARGIN);
		}
	}
}
