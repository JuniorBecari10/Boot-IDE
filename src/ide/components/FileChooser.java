package ide.components;

import java.io.File;
import java.util.ArrayList;

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
}
