package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.input.MouseInput;
import ide.util.Colors;

public class FileView extends IDEComponent {

	private File folder;
	private File folderScheduled;
	public boolean onlyDirs;
	
	public File selectedFile;
	
	private List<FileViewFile> files;
	private int scroll = 0;
	
	public static final int FILE_HEIGHT = 30;
	
	public FileView(int x, int y, int width, int height, File folder, boolean onlyDirs) {
		super(x, y, width, height, null);
		
		files = new ArrayList<>();
		this.onlyDirs = onlyDirs;
		
		setFolder(folder);
	}
	
	public FileView(int x, int y, int width, int height, File folder, boolean onlyDirs, File selectedFile) {
		super(x, y, width, height, null);
		
		files = new ArrayList<>();
		this.onlyDirs = onlyDirs;
		this.selectedFile = selectedFile;
		
		setFolder(folder);
	}
	
	public File getFolder() {
		return folder;
	}
	
	public void scheduleSetFolder(File folder) {
		folderScheduled = folder;
	}
	
	public void performScheduled() {
		if (folderScheduled == null) return;
		
		setFolder(folderScheduled);
		folderScheduled = null;
	}
	
	public void setFolder(File folder) {
		if (folder == null) return;
		
		File[] filesList = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir, name);
				
				return onlyDirs ? f.isDirectory() : true;
			}
		});
		
		if (filesList == null) return;
		
		files.clear();
		
		int i = 0;
		for (File f : filesList) {
			files.add(new FileViewFile(x, (y + (i * FILE_HEIGHT)) - scroll, width, FILE_HEIGHT, f) {
				public void onClick(int y) {
					if (y < this.y || y + FILE_HEIGHT > this.y + height)
						return;

					if (f.isDirectory())
						scheduleSetFolder(f);
					
					selectedFile = this.regent;
				}
			});
			
			i++;
		}
		
		this.folder = folder;
	}
	
	public void scroll() {
		if (MouseInput.wheelDown()) {
			if (files.get(files.size() - 1).getY() - FILE_HEIGHT >= y)
				scroll += FILE_HEIGHT;
		}
		else if (MouseInput.wheelUp()) {
			if (scroll > 0)
				scroll -= FILE_HEIGHT;
		}
	}
	
	public void tick() {
		int i = 0;
		for (FileViewFile f : files) {
			f.setY((y + (i * FILE_HEIGHT)) - scroll);
			
			// continue pq precisa ficar definindo o y
			//if (f.getY() < y) continue;
			//if (f.getY() + f.getHeight() > y + height) continue;
			
			f.tick();
			i++;
		}
		
		performScheduled();
		
		if (hovered()) Explorer.selected = this;
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLight);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		for (FileViewFile f : files) {
			if (f.getY() < y) continue;
			if (f.getY() + f.getHeight() > y + height) break;
			
			f.render(g);
		}
	}
}
