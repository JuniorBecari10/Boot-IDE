package ide.components;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;

import ide.codeeditor.CodeEditor;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;

public class FileChooser extends CustomMessageBox {
	
	public File folder;
	public boolean onlyDirs;
	public String title;
	
	public static int HEIGHT = Main.screen.getHeight() - (Main.screen.getHeight() / 4);
	
	private FileChooser(File folder, boolean onlyDirs, String title) {
		super(Main.screen.getWidth() - (Main.screen.getWidth() / 4), HEIGHT, new ArrayList<>());
		
		this.folder = folder;
		this.onlyDirs = onlyDirs;
		this.title = title;
	}
	
	
	public static void showDialog(File folder, boolean onlyDirs, String title) {
		if (MessageBox.active) return;
		
		FileChooser f = new FileChooser(folder, onlyDirs, title);
		
		IDEComponent.toAdd.add(f);
	}
	
	public void render(Graphics g) {
		super.render(g);
		
		Fonts.drawString(title, (Main.screen.frame.getWidth() / 2) - (title.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) / 2, y + 10, new IDEFont(Fonts.lightGrayNormal, 16), g);
		g.drawLine(x + 15, y + 40, x + width - 15, y + 40);
	}
}
