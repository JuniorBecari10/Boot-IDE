package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.util.Colors;

public class FileViewFile extends IDEComponent {

	public File regent;
	
	public FileViewFile(int x, int y, int width, int height, File regent) {
		super(x, y, width, height, null);
		
		this.regent = regent;
	}
	
	public void render(Graphics g) {
		g.setColor(hovered() ? Colors.explorerLight : Colors.explorer);
		g.fillRect(x, y, width, height);
		
		g.drawImage(ListableFile.getFileIcon(regent.getName()), x + 5, y, height, height, null);
		Fonts.drawString(regent.getName(), x + 5, y + ((height / 2) - (CodeEditor.DEFAULT_FONT_SIZE / 2)), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), x, g);
	}
}
