package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.util.Colors;

public class FileView extends IDEComponent {

	@SuppressWarnings("unused")
	private File folder;
	private List<FileViewFile> files;
	private int scroll = 0;
	
	public static final int FILE_HEIGHT = 30;
	
	public FileView(int x, int y, int width, int height, File folder) {
		super(x, y, width, height, null);
		
		files = new ArrayList<>();
		setFolder(folder);
	}
	
	public void setFolder(File folder) {
		this.folder = folder;
		
		int i = 0;
		for (File f : folder.listFiles()) {
			files.add(new FileViewFile(x, y + (i * FILE_HEIGHT) - scroll, width, FILE_HEIGHT, f));
			
			i++;
		}
	}
	
	public void tick() {
		int i = 0;
		for (FileViewFile f : files) {
			f.setY(y + (i * FILE_HEIGHT) - scroll);
			
			f.tick();
			i++;
		}
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLight);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		for (FileViewFile f : files)
			f.render(g);
	}
}
