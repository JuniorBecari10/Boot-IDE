package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;

public class FileViewFile extends IDEComponent {

	public File regent;
	
	public FileViewFile(int x, int y, int width, int height, File regent) {
		super(x, y, width, height, null);
		
		this.regent = regent;
	}
	
	public void onClick() {}
	
	public void tick() {
		if (leftClicked())
			onClick();
	}
	
	public void render(Graphics g) {
		g.setColor(hovered() ? Colors.explorerLight : Colors.explorer);
		g.fillRect(x, y, width, height);
		
		if (regent.isFile())
			g.drawImage(ListableFile.getFileIcon(regent.getName()), x + 5, y, height, height, null);
		else
			g.drawImage(Main.folder, x + 6, y + 2, height - 5, height - 5, null);
		
		Fonts.drawString(regent.getName(), x + 40, y + ((height / 2) - (CodeEditor.DEFAULT_FONT_SIZE / 2)) - 2, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), x + width, g);
	}
}
