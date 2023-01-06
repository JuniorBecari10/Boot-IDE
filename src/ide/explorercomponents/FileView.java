package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ide.components.FileChooser;
import ide.components.FileViewSetFileName;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.util.Colors;

public class FileView extends IDEComponent {

	private File folder;
	private File folderScheduled;
	public boolean onlyDirs;
	
	public File selectedFile;
	
	public FileChooser parent;
	
	public List<FileViewFile> files;
	public int scroll = 0;
	
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
	
	public FileView(int x, int y, int width, int height, File folder, boolean onlyDirs, File selectedFile, FileChooser parent) {
		super(x, y, width, height, null);
		
		files = new ArrayList<>();
		this.onlyDirs = onlyDirs;
		this.selectedFile = selectedFile;
		this.parent = parent;
		
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
	
	public void reload() {
		setFolder(folder);
	}
	
	public void setFolder(File folder) {
		if (folder == null) return;
		
		File[] filesList = ListableFile.listFilesOrderedArray(folder);
		
		if (filesList == null) return;
		
		files.clear();
		
		int i = 0;
		for (File f : filesList) {
			files.add(new FileViewFile(x, (y + (i * FILE_HEIGHT)) - scroll, width, FILE_HEIGHT, f) {
				public void onClick(int y) {
					if (y < this.y || y + FILE_HEIGHT > this.y + height)
						return;

					scroll = 0;
					if (f.isDirectory())
						scheduleSetFolder(f);
					
					selectedFile = this.regent;
					parent.fileName.text = new StringBuilder(selectedFile.getName());
				}
			});
			
			i++;
		}
		
		this.folder = folder;
	}
	
	public void scroll() {
		if (files.size() == 0) {
			scroll = 0;
			return;
		}
		
		if (FileViewSetFileName.added || components.contains(FileChooser.fileChooser.setFileName)) return;
		
		int amount = FILE_HEIGHT;
		if (KeyInput.isControlDown()) amount *= 3;
		
		if (MouseInput.wheelDown()) {
			if (files.get(files.size() - 1 < 0 ? 0 : files.size() - 1).getY() - FILE_HEIGHT >= y)
				scroll += amount;
		}
		else if (MouseInput.wheelUp()) {
			if (scroll > 0)
				scroll -= amount;
		}
	}
	
	public void tick() {
		/*while (y + (scroll * FILE_HEIGHT) <= y + FILE_HEIGHT)
			scroll -= FILE_HEIGHT;*/
		
		int i = 0;
		for (FileViewFile f : files) {
			f.setY((y + (i * FILE_HEIGHT)) - scroll);
			i++;
			
			if (f.getY() < y) continue;
			if (f.getY() + f.getHeight() > y + height) break;
			
			f.tick();
		}
		
		performScheduled();
		reload();
		
		if (hovered()) Explorer.selected = this;
	}
	
	public void render(Graphics g) {
		g.setColor(Colors.explorerLight);
		g.fillRect(x - 2, y - 2, width + 4, height + 4);
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		for (FileViewFile f : files) { // concurrentmodification
			if (f.getY() < y) continue;
			if (f.getY() + f.getHeight() > y + height) break;
			
			f.render(g);
		}
	}
}
