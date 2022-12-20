package ide.explorercomponents;

import java.awt.Graphics;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.input.MouseInput;
import ide.input.MouseWheelRoll;
import ide.util.Colors;

public class FileView extends IDEComponent {

	private File folder;
	private File folderScheduled;
	public boolean onlyDirs;
	
	private List<FileViewFile> files;
	private int scroll = 0;
	
	public static final int FILE_HEIGHT = 30;
	
	public FileView(int x, int y, int width, int height, File folder, boolean onlyDirs) {
		super(x, y, width, height, null);
		
		files = new ArrayList<>();
		this.onlyDirs = onlyDirs;
		
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
		this.folder = folder;
		
		files.clear();
		
		File[] filesList = folder.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				File f = new File(dir, name);
				
				return onlyDirs ? f.isDirectory() : true;
			}
		});
		
		int i = 0;
		for (File f : filesList) {
			files.add(new FileViewFile(x, y + (i * FILE_HEIGHT) - scroll, width, FILE_HEIGHT, f) {
				public void onClick() {
					if (f.isDirectory())
						scheduleSetFolder(f);
				}
			});
			
			i++;
		}
	}
	
	public void scroll() {
		System.out.println(folder);
		System.out.println(scroll);
		System.out.println("---");
		
		if (MouseInput.getWheelRoll() == MouseWheelRoll.DOWN)
			scroll += FILE_HEIGHT;
		else if (MouseInput.getWheelRoll() == MouseWheelRoll.UP)
			if (scroll > 0)
				scroll -= FILE_HEIGHT;
	}
	
	public void tick() {
		int i = 0;
		for (FileViewFile f : files) {
			if (y + (i * FILE_HEIGHT) - scroll < y) continue;
			if (y + (i * FILE_HEIGHT) + FILE_HEIGHT - scroll > y + height) break;
			
			f.setY(y + (i * FILE_HEIGHT) - scroll);
			
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
		
		int i = 0;
		for (FileViewFile f : files) {
			if (y + (i * FILE_HEIGHT) - scroll < y) continue;
			if (y + (i * FILE_HEIGHT) + FILE_HEIGHT - scroll > y + height) break;
			
			f.render(g);
			i++;
		}
	}
}
