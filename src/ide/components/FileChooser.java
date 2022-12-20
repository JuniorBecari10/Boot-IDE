package ide.components;

import java.awt.Graphics;
import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileSystemView;

import ide.codeeditor.CodeEditor;
import ide.explorercomponents.ComboBox;
import ide.explorercomponents.ExecuteButtonIcon;
import ide.explorercomponents.FileView;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class FileChooser extends CustomMessageBox {
	
	public static final String DEFAULT_FOLDER = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	public static final String HOME_FOLDER = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
	
	public File folder;
	public boolean onlyDirs;
	public String title;
	
	public ComboBox folderScope;
	public FileView fileView;
	
	public static int HEIGHT = Main.screen.getHeight() - (Main.screen.getHeight() / 4);
	
	private FileChooser(File folder, boolean onlyDirs, String title) {
		super(Main.screen.getWidth() - (Main.screen.getWidth() / 4), HEIGHT, new ArrayList<>());
		
		this.folder = folder;
		this.onlyDirs = onlyDirs;
		this.title = title;
		
		folderScope = new ComboBox(
				x + ((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - (Main.screen.getWidth() / 6),
				y + 60,
				Main.screen.getWidth() / 3,
				30,
				new String[] { }) {
			public void tick() {
				super.tick();
				
				text = new StringBuilder(fileView.getFolder().getPath());
			}
		};
		
		fileView = new FileView(x + 15, y + 180, width - 30, FileView.FILE_HEIGHT * 8, folder, onlyDirs);
		
		innerComponents.add(folderScope);
		innerComponents.add(fileView);
		
		innerComponents.add(new ExecuteButtonIcon(x + 20, y + 120, 32, 32, Main.newFolderSpr, () -> {  }, true, Texts.createFolder));
		innerComponents.add(new ExecuteButtonIcon(x + 60, y + 120, 32, 32, Main.folderUp, () -> { fileView.setFolder(fileView.getFolder().getParentFile()); folderScope.setText(fileView.getFolder().getPath()); }, true, Texts.oneFolderUp));
		innerComponents.add(new ExecuteButtonIcon(x + 100, y + 120, 32, 32, Main.reloadSpr, () -> { fileView.setFolder(fileView.getFolder()); }, true, Texts.reload));
	}
	
	
	public static void showDialog(File folder, boolean onlyDirs, String title) {
		if (MessageBox.active) return;
		
		FileChooser f = new FileChooser(folder, onlyDirs, title);
		
		IDEComponent.toAdd.add(f);
	}
	
	public void render(Graphics g) {
		super.render(g);
		
		Fonts.drawString(title, (Main.screen.frame.getWidth() / 2) - (title.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) / 2, y + 10, new IDEFont(Fonts.lightGrayNormal, 16), g);
		
		// linha abaixo do titulo
		g.setColor(Colors.textLight);
		g.drawLine(x + 15, y + 40, x + width - 15, y + 40);
	}
}
