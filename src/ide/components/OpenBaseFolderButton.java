package ide.components;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorer.ListableFile;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Texts;

public class OpenBaseFolderButton extends IDEComponent {
	
	public static JFileChooser chooser;

	public OpenBaseFolderButton(int x, int y, int width, int height, BufferedImage sprite) {
		super(x, y, width, height, sprite);
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			
			chooser = new JFileChooser();
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}
	
	public void tick() {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		chooser.setDialogTitle(Texts.selectBaseFolder + "...");
		
		super.tick();
		
		if (leftClicked() && (!SetFileName.added && !CommandTerminal.active && !MessageBox.active && !SetBranchName.added && !SetCommitName.added && !RenameFile.added && Explorer.selected == null)) {
			MouseInput.updateMouse();
			
			openBaseFolder();
		}
	}
	
	public static void openBaseFolder() {
		int option = chooser.showOpenDialog(Main.screen.frame);

		if (option == JFileChooser.APPROVE_OPTION) {
			if (chooser.getSelectedFile() == null || chooser.getSelectedFile().listFiles() == null) return;
			
			File sel = chooser.getSelectedFile();
			
			boolean alreadyHasBaseFolder = Main.baseFolder != null;
			
			Main.baseFolder = sel;
			
			Explorer.files.clear();
			ListableFile.files.clear();
			
			Explorer.scope = null;
			Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(Main.baseFolder));
			Explorer.gitStatus = null;
			
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " - " + Main.PROGRAM_NAME);
			
			if (!alreadyHasBaseFolder) {
				IDEComponent.toAdd.add(Main.oneFolder);
				IDEComponent.toAdd.add(Main.returnBase);
				IDEComponent.toAdd.add(Main.newFile);
				IDEComponent.toAdd.add(Main.newFolder);
				IDEComponent.toAdd.add(Main.reload);
			}
			
			//Main.writeFile(Main.settingsFile);
			
			Explorer.fetchStatus();
			MouseInput.updateMouse();
		}
	}
	
	public void render(Graphics g) {
		if (CommandTerminal.expOff || Explorer.explorerMode != ExplorerMode.EXPLORER) return;
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x - 1, y - 1, width + 4, height + 4);
		}
		
		super.render(g);
		
		if (hovered() && !(SetFileName.added || RenameFile.added || CommandTerminal.active || MessageBox.active)) {
			Explorer.renderDescriptionText(Texts.selectBaseFolder, MouseInput.getMouseX() - 50, MouseInput.getMouseY() + 30, g);
		}
	}
}
