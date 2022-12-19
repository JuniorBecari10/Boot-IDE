package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.components.RightClickOption;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.util.Colors;

public class ComboBox extends IDEComponent {
	
	public String[] options;
	public boolean editable;
	
	private StringBuilder text;
	private int cursorIndex = 0;
	
	private int scroll = 0;
	
	public ComboBox(int x, int y, int width, int height, String[] options, boolean editable) {
		super(x, y, width, height, null);
		
		this.options = options;
		this.editable = editable;
		
		if (options == null || options.length == 0)
			text = new StringBuilder();
		else
			text = new StringBuilder(options[0]);
		
		cursorIndex = text.length();
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
	
	public void tick() {
		// mover pra frente (o texto vai pra trás)
		while (x + 1 + (cursorIndex * (16 - 4)) - scroll > width)
			scroll += 12;
		// mover pra trás (o texto vai pra frente)
		while (x + 1 + (cursorIndex * (16 - 4)) - scroll < x || (x + 1 + (cursorIndex * (16 - 4)) - scroll == x + 1 && text.length() > 0))
			scroll -= 12;

		if (text.length() == 0 || scroll < 0)
			scroll = 0;

		if (leftClicked()) {
			Explorer.selected = this;	
			
			List<RightClickOption> list = new ArrayList<>();
			
			boolean isTop = true;
			
			for (String s : options) {
				list.add(new RightClickOption(0, 0, width, s, (a) -> {
					if (Explorer.selected instanceof ComboBox) {
						((ComboBox) Explorer.selected).text = new StringBuilder(s);
					}
				}, "", isTop));
				isTop = false;
			}
			
			IDEComponent.addRightClickOptions(x, y + height, list.toArray(new RightClickOption[0]));
		}
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(hovered() ? Colors.explorerLight : Colors.explorer);
		g.fillRect(x, y, width, height);
		
		g.setColor(Colors.textLight);
		g2.setStroke(new BasicStroke(2f));
		g2.drawLine(x, y + height, x + width, y + height);
		
		Fonts.drawString(text.toString(), x + 2, (y + (height / 2)) - (CodeEditor.DEFAULT_FONT_SIZE / 2), new IDEFont(Fonts.lighterGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), x + width, g2);
		
		g.setColor(Colors.textLight);
		
		if (Explorer.selected == this && Main.editor.showCursor && x + 1 + (cursorIndex * (16 - 4)) - scroll < width)
			g.fillRect(x + 1 + (cursorIndex * (16 - 4)) - scroll, y, 2, height);
	}
}
