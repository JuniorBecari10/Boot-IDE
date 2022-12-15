package ide.components;

import java.io.File;
import java.util.ArrayList;

import ide.main.Main;
import ide.screen.Screen;

public class FileChooser extends CustomMessageBox {
	
	public File folder;
	public boolean onlyDirs;
	public String title;
	
	private FileChooser(File folder, boolean onlyDirs, String title) {
		super(Main.screen.getWidth() / 4, Screen.DECORATION_HEIGHT - Main.screen.getHeight() / 4, new ArrayList<>());
		
		this.folder = folder;
		this.onlyDirs = onlyDirs;
		this.title = title;
	}
	
	
	public static void showDialog(File folder, boolean onlyDirs, String title) {
		if (MessageBox.active) return;
		
		FileChooser f = new FileChooser(folder, onlyDirs, title);
		
		IDEComponent.toAdd.add(f);
	}
}
