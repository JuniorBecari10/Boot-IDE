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
import ide.util.Colors;

public class ComboBox extends IDEComponent {
	
	public String[] options;
	
	protected StringBuilder text;
	
	private int scroll = 0;
	
	public ComboBox(int x, int y, int width, int height, String[] options) {
		super(x, y, width, height, null);
		
		this.options = options;
		
		if (options == null || options.length == 0)
			text = new StringBuilder();
		else
			text = new StringBuilder(options[0]);
	}
	
	public synchronized void type() {
		if (KeyInput.isKeyPressed() && Explorer.selected == this) {
			KeyInput.updateKeys();
			
			// Shortcuts Area
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C - Copiar (Tudo)
				CodeEditor.copyText(text.toString());
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V - Colar
				text.append(CodeEditor.clipboard);
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X - Recortar (Tudo)
				CodeEditor.copyText(text.toString());
				
				text = new StringBuilder();
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) { // Ctrl + Del (Deletar Tudo)
				text = new StringBuilder();
			}
		}
	}
	
	public void setText(String t) {
		text = new StringBuilder(t);
	}
	
	public void tick() {
		// mover pra frente (o texto vai pra trás)
		while (x + 1 + (text.length() * (16 - 4)) - scroll > width)
			scroll += 12;
		// mover pra trás (o texto vai pra frente)
		while (x + 1 + (text.length() * (16 - 4)) - scroll < x || (x + 1 + (text.length() * (16 - 4)) - scroll == x + 1 && text.length() > 0))
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
	}
}
