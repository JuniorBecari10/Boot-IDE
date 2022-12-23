package ide.components;

import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;

import javax.swing.filechooser.FileSystemView;

import ide.codeeditor.CodeEditor;
import ide.explorercomponents.ComboBox;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.ExecuteButtonIcon;
import ide.explorercomponents.FileView;
import ide.explorercomponents.InputBox;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.ExecuteCommand;
import ide.util.Texts;

public class FileChooser extends CustomMessageBox {
	
	public static final String DEFAULT_FOLDER = FileSystemView.getFileSystemView().getDefaultDirectory().getPath();
	public static final String HOME_FOLDER = FileSystemView.getFileSystemView().getHomeDirectory().getPath();
	
	public File folder;
	public boolean onlyDirs;
	public String title;
	
	public ComboBox folderScope;
	public FileView fileView;
	
	public InputBox fileName;
	
	public ExecuteButton cancel;
	public ExecuteButton ok;
	
	public ExecuteCommand func;
	
	public static int HEIGHT = Main.screen.getHeight() - (Main.screen.getHeight() / 4);
	
	private FileChooser(File folder, boolean onlyDirs, String title, ExecuteCommand func) {
		super(Main.screen.getWidth() - (Main.screen.getWidth() / 4), HEIGHT, new ArrayList<>());
		
		this.folder = folder;
		this.onlyDirs = onlyDirs;
		this.title = title;
		this.func = func;
		
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
		
		fileView = new FileView(x + 15, y + 180, width - 30, FileView.FILE_HEIGHT * 8, folder, onlyDirs, folder);
		
		fileName = new InputBox(
				x + ((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - (Main.screen.getWidth() / 4),
				y + 430,
				Main.screen.getWidth() / 2,
				20,
				true,
				false) {
			public void tick() {
				super.tick();
				
				if (fileView.selectedFile != null)
					text = new StringBuilder(fileView.selectedFile.getPath());
			}
		};
		
		cancel = new ExecuteButton(
				x + 15,
				y + height - 15 - 20,
				((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - 15,
				20,
				Texts.cancel,
				() -> {
					doClose();
				},
				true,
				true);
		
		ok = new ExecuteButton(
				x + (((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2)) + 15,
				y + height - 15 - 20,
				((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - 30,
				20,
				"Ok",
				() -> {
					OpenBaseFolderButton.setBaseFolder(fileView.selectedFile);
					doClose();
				},
				true,
				true);
		
		innerComponents.add(folderScope);
		innerComponents.add(fileView);
		innerComponents.add(fileName);
		
		innerComponents.add(cancel);
		innerComponents.add(ok);
		
		innerComponents.add(new ExecuteButtonIcon(x + 20, y + 120, 32, 32, Main.newFolderSpr, () -> {  }, true, Texts.createFolder));
		innerComponents.add(new ExecuteButtonIcon(x + 60, y + 120, 32, 32, Main.folderUp, () -> { fileView.setFolder(fileView.getFolder().getParentFile()); folderScope.setText(fileView.getFolder().getPath()); }, true, Texts.oneFolderUp));
		innerComponents.add(new ExecuteButtonIcon(x + 100, y + 120, 32, 32, Main.reloadSpr, () -> { fileView.setFolder(fileView.getFolder()); }, true, Texts.reload));
	}
	
	public static void showDialog(File folder, boolean onlyDirs, String title, ExecuteCommand func) {
		if (MessageBox.active) return;
		
		FileChooser f = new FileChooser(folder, onlyDirs, title, func);
		
		IDEComponent.toAdd.add(f);
	}
	
	public void tick() {
		super.tick();
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
			OpenBaseFolderButton.setBaseFolder(fileView.selectedFile);
			doClose();
		}
		
		if (ok.leftClicked() && ok.enabled) {
			func.execute(fileView.selectedFile.getAbsolutePath());
		}
	}
	
	public void render(Graphics g) {
		super.render(g);
		
		Fonts.drawString(title, (Main.screen.frame.getWidth() / 2) - (title.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) / 2, y + 10, new IDEFont(Fonts.lightGrayNormal, 16), g);
		Fonts.drawString(Texts.file + ":", x + 30, y + 435, new IDEFont(Fonts.lightGrayNormal, 16), g);
		
		// linha abaixo do titulo
		g.setColor(Colors.textLight);
		g.drawLine(x + 15, y + 40, x + width - 15, y + 40);
	}
}
