package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.util.Colors;

public class ComboBox extends IDEComponent {
	
	public String[] options;
	public boolean editable;
	
	private StringBuilder text;
	
	public ComboBox(int x, int y, int width, int height, String[] options, boolean editable) {
		super(x, y, width, height, null);
		
		this.options = options;
		this.editable = editable;
		
		text = new StringBuilder("Documents");
	}
	
	public void tick() {
		
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(hovered() ? Colors.explorerLight : Colors.explorer);
		g.fillRect(x, y, width, height);
		
		g.setColor(Colors.textLight);
		g2.setStroke(new BasicStroke(2f));
		g2.drawLine(x, y + height, x + width, y + height);
		
		Fonts.drawString(text.toString(), x + 2, (y + (height / 2)) - (CodeEditor.DEFAULT_FONT_SIZE / 2), new IDEFont(Fonts.lighterGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), x + width, g2);
	}
}
