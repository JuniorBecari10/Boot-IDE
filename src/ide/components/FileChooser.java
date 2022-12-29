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
	
	public static FileChooser fileChooser;
	
	public File folder;
	public boolean onlyDirs;
	public String title;
	
	public ComboBox folderScope;
	public FileView fileView;
	
	public InputBox fileName;
	
	public ExecuteButton cancelBtn;
	public ExecuteButton okBtn;
	
	public FileViewSetFileName setFileName;
	
	public ExecuteCommand ok;
	public ExecuteCommand cancel;
	
	public static int HEIGHT = Main.screen.getHeight() - (Main.screen.getHeight() / 4);
	
	private FileChooser(File folder, boolean onlyDirs, String title, ExecuteCommand ok, ExecuteCommand cancel) {
		super(Main.screen.getWidth() - (Main.screen.getWidth() / 4), HEIGHT, new ArrayList<>(), false);
		
		this.folder = folder;
		this.onlyDirs = onlyDirs;
		this.title = title;
		this.ok = ok;
		this.cancel = cancel;
		
		fileView = new FileView(x + 15, y + 180, width - 30, FileView.FILE_HEIGHT * 8, folder, onlyDirs, folder, this);
		
		folderScope = new ComboBox(
				x + 50,
				y + 60,
				width - 100,
				30,
				new String[] { }) {
			public void tick() {
				super.tick();
				
				text = new StringBuilder(fileView.getFolder().getPath());
			}
		};
		
		fileName = new InputBox(
				x + ((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - (Main.screen.getWidth() / 4),
				y + 430,
				Main.screen.getWidth() / 2,
				20,
				true,
				true);
		
		cancelBtn = new ExecuteButton(
				x + 15,
				y + height - 15 - 20,
				((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - 15,
				20,
				Texts.cancel,
				() -> {},
				true,
				true);
		
		okBtn = new ExecuteButton(
				x + (((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2)) + 15,
				y + height - 15 - 20,
				((Main.screen.getWidth() - (Main.screen.getWidth() / 4)) / 2) - 30,
				20,
				"Ok",
				() -> {},
				true,
				true);
		
		fileName.setText(fileView.getFolder().getName());
		
		setFileName = new FileViewSetFileName(x + 15, y + 180, width - 30, 30, false, fileView);
		
		innerComponents.add(folderScope);
		innerComponents.add(fileView);
		innerComponents.add(fileName);
		
		innerComponents.add(cancelBtn);
		innerComponents.add(okBtn);
		
		innerComponents.add(new ExecuteButtonIcon(x + 20, y + 120, 32, 32, Main.newFolderSpr, () -> { IDEComponent.toAdd.add(setFileName); }, true, Texts.createFolder));
		innerComponents.add(new ExecuteButtonIcon(x + 60, y + 120, 32, 32, Main.folderUp, () -> {
			new Thread() {
				public void run() {
					fileView.setFolder(fileView.getFolder().getParentFile());
					folderScope.setText(fileView.getFolder().getPath());
					
					fileName.setText(fileView.getFolder().getName());
				}
			}.start();
		}, true, Texts.oneFolderUp));
		innerComponents.add(new ExecuteButtonIcon(x + 100, y + 120, 32, 32, Main.reloadSpr, () -> {
			new Thread() {
				public void run() {
					fileView.setFolder(fileView.getFolder());
				}
			}.start();
		}, true, Texts.reload));
	}
	
	public static void showDialog(File folder, boolean onlyDirs, String title, ExecuteCommand ok, ExecuteCommand cancel) {
		if (MessageBox.active) return;
		
		fileChooser = new FileChooser(folder, onlyDirs, title, ok, cancel);
		
		IDEComponent.toAdd.add(fileChooser);
	}
	
	public void tick() {
		super.tick();
		
		String path = fileView.getFolder() + File.separator + (fileName.getText().equals(fileView.getFolder().getName()) ? "" : fileName.getText());
		
		if (KeyInput.isKeyPressed()) {
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				doClose();
				ok.execute(path);
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
				KeyInput.updateKeys();
				
				if (FileViewSetFileName.added) {
					FileViewSetFileName.added = false;
					IDEComponent.toRemove.add(setFileName);
				}
				else {
					doClose();
				}
				
				cancel.execute(path);
			}
		}
		
		if (okBtn.leftClicked()) {
			doClose();
			ok.execute(path);
		}
		else if (cancelBtn.leftClicked()) {
			doClose();
			cancel.execute(path);
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
