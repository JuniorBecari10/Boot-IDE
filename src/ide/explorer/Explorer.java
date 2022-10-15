package ide.explorer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.OneFolderUpButton;
import ide.components.OpenBaseFolderButton;
import ide.components.ReloadButton;
import ide.components.RenameFile;
import ide.components.ReturnToBaseFolderButton;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.ExecuteButtonIcon;
import ide.explorercomponents.ExplorerTab;
import ide.explorercomponents.InputBox;
import ide.explorercomponents.LastAction;
import ide.explorercomponents.SearchReplaceCore;
import ide.explorercomponents.SearchReplaceRadioButton;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.explorercomponents.TextArea;
import ide.explorercomponents.ToggleButton;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.git.GitCore;
import ide.git.GitStatus;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.terminal.TerminalCore;
import ide.terminal.TerminalTab;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class Explorer extends IDEComponent {
	
	public static final int DEFAULT_WIDTH = 280;
	
	public static List<ListableFile> files;
	public static List<ListableFile> toRemove;
	
	public static List<ExplorerTab> tabs;
	
	public static ListableFile scope;
	
	public static boolean allowAnimations = true;

	public static String folderPath = "";
	public static String folderPathFull = "";
	
	public static SetFileName setFileName;
	public static RenameFile renameFile;
	
	public static IDEComponent selected;
	
	public static GitStatus gitStatus;
	
	// -- Search / Replace --
	
	public static InputBox search, replace;
	public static ToggleButton caseSensitive, regex;
	public static SearchReplaceRadioButton entireDocument, selectedLines;
	public static ExecuteButton searchNext, replaceAll;
	
	// -- Git --
	
	public static SetBranchName setBranchName;
	public static SetCommitName setCommitName;
	
	public static ExecuteButtonIcon createBranch;
	public static ExecuteButtonIcon checkout;
	public static ExecuteButtonIcon renameBranch;
	public static ExecuteButtonIcon mergeBranch;
	public static ExecuteButtonIcon deleteBranch;
	
	public static ExecuteButton initRepo;
	public static InputBox cloneURL;
	public static ExecuteButton clone;
	
	public static ExecuteButton stageAll;
	public static ExecuteButton unstageAll;
	public static ExecuteButton seeStaged;
	
	public static ExecuteButton commit;
	public static ToggleButton allowEmpty;
	
	public static ExecuteButton push;
	public static ToggleButton forcePush;
	
	public static LastAction lastAction;
	
	// -- Terminal --
	
	public static ToggleButton wordWrap;
	public static ExecuteButtonIcon addTerminal;
	public static ExecuteButton showOverlay;
	public static TextArea textArea;
	
	public static int MINIMUM_Y = 200 + Screen.DECORATION_HEIGHT;
	
	public static ExplorerMode explorerMode = ExplorerMode.EXPLORER;
	
	public static boolean hoveringListableFile;
	
	public static boolean showBaseFolderCard = false;
	public static boolean showFolderPathCard = false;
	
	public static boolean dragging = false;
	
	public static String baseFolderName;
	
	public static int minDrag = 192;
	
	public int maxTitleWidth = width / 24;
	public int maxTextWidth = width / 16;
	public int maxFileCreateWidth = width / 18;
	
    public Explorer(int x, int y, int width, int height) {
        super(x, y, width, height, null);
        
        tabs = new ArrayList<>();
        
        files = new ArrayList<>();
        toRemove = new ArrayList<>();
    }
    
    public static void fetchStatus() {
    	if (isBaseFolderRepository()) {
	    	gitStatus = GitStatus.fetch();
	    }
	    else {
	    	gitStatus = null;
	    }
    	
    	Main.editor.refreshText();
    }
    
    public void addTabs() {
    	tabs.add(new ExplorerTab(1, Main.explorerTab, ExplorerMode.EXPLORER, Texts.explorerText) {
    		public void select() {
    			explorerMode = ExplorerMode.EXPLORER;
    			selected = null;
    			
    			SearchReplaceCore.dispose();
    			GitCore.dispose();
    			TerminalCore.dispose();
    			
    			ReloadButton.reloadExplorer();
    		}
    	});
    	tabs.add(new ExplorerTab(1 + 3 + ExplorerTab.SIZE, Main.searchReplaceTab, ExplorerMode.SEARCHREPLACE, Texts.searchReplace) {
    		public void select() {
    			if (Main.editor.tabs.isEmpty()) return;
    			
    			Main.editor.execute("searchrep");
    			GitCore.dispose();
    			TerminalCore.dispose();
    		}
    	});
    	tabs.add(new ExplorerTab(1 + 6 + (ExplorerTab.SIZE * 2), Main.gitTab, ExplorerMode.GIT, "Git") {
    		public void select() {
    			if (Main.baseFolder == null) return;
    			
    			GitCore.init();
    			SearchReplaceCore.dispose();
    			TerminalCore.dispose();
    			fetchStatus();
    		}
    	});
    	tabs.add(new ExplorerTab(1 + 9 + (ExplorerTab.SIZE * 3), Main.terminalTab, ExplorerMode.TERMINAL, "Terminal") {
    		public void select() {
    			TerminalCore.init();
    			GitCore.dispose();
    			SearchReplaceCore.dispose();
    		}
    	});
    }
    
    public static boolean isBaseFolderRepository() {
    	if (Main.baseFolder == null || !Main.baseFolder.exists())
    		return false;
    	
    	// verificar se é oculto (f.isHidden())
    	for (File f : Main.baseFolder.listFiles())
    		if (f.getName().equals(".git") && f.isDirectory())
    			return true;
    	
    	return false;
    }
    
    public static String getScopePath() {
    	if (scope == null) return Main.baseFolder.getAbsolutePath();
    	
    	return scope.getRegent().getAbsolutePath();
    }
    
    
    public int getWidth() {
    	return width;
    }
    
    public int getHeight() {
    	return height;
    }
    
    public void setDrag(int drag) {
    	width = drag;
		Main.editor.setX(width);
		Main.editor.setWidth(Main.screen.getWidth());
		
		if (width < minDrag) {
    		width = minDrag;
    		Main.editor.setX(width);
    		Main.editor.setWidth(Main.screen.getWidth());
    	}
    	
    	if (width > Main.screen.getWidth() - 60) {
    		width = Main.screen.getWidth() - 60;
    		Main.editor.setX(width);
    		Main.editor.setWidth(Main.screen.getWidth());
    	}
    }
    
    public void tick() {
    	if (SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added) {
    		Main.screen.setCursor(Cursor.getDefaultCursor());
    		return;
    	}
    	
    	if (CommandTerminal.expOff) return;
    	
	    height = Main.screen.getHeight();
	    
	    /*if (WindowInput.isMaximized() || !WindowInput.isActivated())
	    	ReloadButton.reloadExplorer();*/
	    /*
	    if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE && explorerMode != ExplorerMode.EXPLORER && !RightClickOption.isRightClickActive() && !SetBranchName.added) {
	    	Explorer.explorerMode = ExplorerMode.EXPLORER;
			Explorer.selected = null;
	    	SearchReplaceCore.dispose();
	    	GitCore.dispose();
	    }
	    */
	   	// Drag
	   	
	    if (MouseInput.hovered(x + width - 5, y, 10, height) && !ListableFile.isListableFileHovered() && !(SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || SetBranchName.added || SetCommitName.added)) {
			Main.screen.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
			
			if (MouseInput.leftDragged() && (!Main.editor.selecting || Main.editor.editing == null) && Tab.dragging == null)
				dragging = true;
		}
	    
	    if (!MouseInput.leftDragged()) dragging = false;
	    
	    if (dragging) {
	    	width = MouseInput.getMouseX();
	    	Main.editor.setX(width);
	    	Main.editor.setWidth(Main.screen.getWidth());
	    	
	    	Main.screen.setCursor(new Cursor(Cursor.W_RESIZE_CURSOR));
	    }
	    
	    if (width < minDrag) {
	    	width = minDrag;
	    	Main.editor.setX(width);
	    	Main.editor.setWidth(Main.screen.getWidth());
	    }
	    
	    if (width > Main.screen.getWidth() - 60) {
	    	width = Main.screen.getWidth() - 60;
	    	Main.editor.setX(width);
	    	Main.editor.setWidth(Main.screen.getWidth());
	    }
	    
	    // Atalho Universal
	    
	    if (hovered())
	    	Main.screen.setCursor(Cursor.getDefaultCursor());
	    
	    if (explorerMode == ExplorerMode.GIT) {
	    	if (MouseInput.hovered(0, Screen.DECORATION_HEIGHT + 380, width, 40) && leftClicked() && gitStatus.changedFiles.length > 0) {
	    		int widthDraw = getHighestNumber(arrayOfLengths(gitStatus.changedFiles)) * 16;
	    		
	    		if (widthDraw < Texts.filesChangedTitle.length() * 16)
	    			widthDraw = Texts.filesChangedTitle.length() * 16;
	    		
	    		List<RightClickOption> list = new ArrayList<>();
	    		
	    		list.add(new RightClickOption(0, 0, widthDraw, false, Texts.filesChangedTitle, (a) -> { }, "", true));
	    		
	    		for (String s : gitStatus.changedFiles) {
	    			list.add(new RightClickOption(0, 0, widthDraw, s, (a) -> {
	    				String[] split = s.split(" ");
	    				String[] removeFirst = new String[split.length - 1];
	    				
	    				for (int i = 0; i < split.length - 1; i++) {
	    					removeFirst[i] = split[i + 1];
	    				}
	    				
	    				String fileName = String.join(" ", s.startsWith(" ") ? split : removeFirst);
	    				
	    				File file = new File(Main.baseFolder.getAbsolutePath() + File.separator + fileName);
	    				
	    				if (file.isFile())
	    					ListableFile.addTab(ListableFile.newListableFile(file), false);
	    				else {
	    					Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(file));
	    					
	    					explorerMode = ExplorerMode.EXPLORER;
	    	    			selected = null;
	    	    			
	    	    			SearchReplaceCore.dispose();
	    	    			GitCore.dispose();
	    	    			
	    	    			ReloadButton.reloadExplorer();
	    				}
	    				
	    			}, ""));
	    		}
	    		
	    		IDEComponent.addRightClickOptions(width, Screen.DECORATION_HEIGHT + 400, list.toArray(new RightClickOption[list.size()]));
	    	}
	    }
	    else if (explorerMode == ExplorerMode.TERMINAL) {
	    	for (TerminalTab t : TerminalCore.tabs)
	    		t.tick();
	    }
	    else if (explorerMode == ExplorerMode.EXPLORER) {
	    	if (ListableFile.files.isEmpty() && files.isEmpty()) hoveringListableFile = false;
	    	
	    	if (Main.baseFolder == null || !Main.baseFolder.exists()) {
	    		CommandTerminal.runCommand("closebasefolder");
	    		
	    		return;
	    	}
	    	
	    	if (hovered() && !ListableFile.isListableFileHovered() && !MouseInput.hovered(x + width - 5, y, 10, height))
	    		Main.screen.setCursor(Cursor.getDefaultCursor());
	    	
	    	maxTitleWidth =  (width / 24) + 2;
	    	maxTextWidth =   (width / 16) + 2;
	    	maxFileCreateWidth = width / 18 + 2;
	    	
	    	// Media Queries (só que em Java kkkk)
	    	
	    	if (width < DEFAULT_WIDTH - 10) {
//	    		Main.newFile.setWidth(24);
//	    		Main.newFile.setHeight(24);
//	    		
//	    		
//	    		
//	    		Main.newFolder.setWidth(24);
//	    		Main.newFolder.setHeight(24);
//	    		
//	    		Main.oneFolder.setWidth(24);
//	    		Main.oneFolder.setHeight(24);
//	    		
//	    		Main.returnBase.setWidth(24);
//	    		Main.returnBase.setHeight(24);
//	    		
//	    		Main.reload.setWidth(24);
//	    		Main.reload.setHeight(24);
	    		
	    		if (Main.newFile.getWidth() > 24) {
	    			Main.newFile.setWidth(Main.newFile.getWidth() - 1);
	    			Main.newFile.setHeight(Main.newFile.getHeight() - 1);
	    			
	    			Main.canRunLoop = true;
	    		}
	    		
	    		if (Main.newFolder.getWidth() > 24) {
	    			Main.newFolder.setWidth(Main.newFolder.getWidth() - 1);
	    			Main.newFolder.setHeight(Main.newFolder.getHeight() - 1);
	    			
	    			Main.canRunLoop = true;
	    		}
	    		
	    		if (Main.oneFolder.getWidth() > 24) {
	    			Main.oneFolder.setWidth(Main.oneFolder.getWidth() - 1);
	    			Main.oneFolder.setHeight(Main.oneFolder.getHeight() - 1);
	    			
	    			Main.canRunLoop = true;
	    		}
	    		
	    		if (Main.returnBase.getWidth() > 24) {
	    			Main.returnBase.setWidth(Main.returnBase.getWidth() - 1);
	    			Main.returnBase.setHeight(Main.returnBase.getHeight() - 1);
	    			
	    			Main.canRunLoop = true;
	    		}
	    		
	    		if (Main.reload.getWidth() > 24) {
	    			Main.reload.setWidth(Main.reload.getWidth() - 1);
	    			Main.reload.setHeight(Main.reload.getHeight() - 1);
	    			
	    			Main.canRunLoop = true;
	    		}
	    		
	    		Main.newFile.setX(Main.openBase.getX() + Main.openBase.getWidth() + 4);
	    		Main.newFolder.setX(Main.newFile.getX() + Main.newFile.getWidth() + 2);
	    		Main.oneFolder.setX(Main.newFolder.getX() + Main.newFolder.getWidth() + 2);
	    		Main.returnBase.setX(Main.oneFolder.getX() + Main.oneFolder.getWidth() + 2);
	    		Main.reload.setX(Main.returnBase.getX() + Main.returnBase.getWidth() + 2);
	    	}
	    	else if (width >= DEFAULT_WIDTH - 10) {
	    		if (allowAnimations) {
	    			if (Main.newFile.getWidth() < 32) {
	    				Main.newFile.setWidth(Main.newFile.getWidth() + 1);
	    				Main.newFile.setHeight(Main.newFile.getHeight() + 1);

	    				Main.canRunLoop = true;
	    			}

	    			if (Main.newFolder.getWidth() < 32) {
	    				Main.newFolder.setWidth(Main.newFolder.getWidth() + 1);
	    				Main.newFolder.setHeight(Main.newFolder.getHeight() + 1);

	    				Main.canRunLoop = true;
	    			}

	    			if (Main.oneFolder.getWidth() < 32) {
	    				Main.oneFolder.setWidth(Main.oneFolder.getWidth() + 1);
	    				Main.oneFolder.setHeight(Main.oneFolder.getHeight() + 1);

	    				Main.canRunLoop = true;
	    			}

	    			if (Main.returnBase.getWidth() < 32) {
	    				Main.returnBase.setWidth(Main.returnBase.getWidth() + 1);
	    				Main.returnBase.setHeight(Main.returnBase.getHeight() + 1);

	    				Main.canRunLoop = true;
	    			}

	    			if (Main.reload.getWidth() < 32) {
	    				Main.reload.setWidth(Main.reload.getWidth() + 1);
	    				Main.reload.setHeight(Main.reload.getHeight() + 1);

	    				Main.canRunLoop = true;
	    			}
	    		}
	    		else {
	    			Main.newFile.setWidth(32);
		    		Main.newFile.setHeight(32);
		    		
		    		Main.newFolder.setWidth(32);
		    		Main.newFolder.setHeight(32);
		    		
		    		Main.oneFolder.setWidth(32);
		    		Main.oneFolder.setHeight(32);
		    		
		    		Main.returnBase.setWidth(32);
		    		Main.returnBase.setHeight(32);
		    		
		    		Main.reload.setWidth(32);
		    		Main.reload.setHeight(32);
	    		}
	    		
	    		Main.newFile.setX(width - 200);
	    		Main.newFolder.setX(Main.newFile.getX() + Main.newFile.getWidth() + 6);
	    		Main.oneFolder.setX(Main.newFolder.getX() + Main.newFolder.getWidth() + 6);
	    		Main.returnBase.setX(Main.oneFolder.getX() + Main.oneFolder.getWidth() + 6);
	    		Main.reload.setX(Main.returnBase.getX() + Main.returnBase.getWidth() + 6);
	    	}
	    	
	    	if (width < MINIMUM_Y) {
	    		Main.openBase.setY(Screen.DECORATION_HEIGHT + 75);
	    		
	    		if (allowAnimations) {
		    		if (Main.openBase.getWidth() < 36) {
		    			Main.openBase.setWidth(Main.openBase.getWidth() + 1);
		    			Main.openBase.setHeight(Main.openBase.getHeight() + 1);
		    		}
		    		else if (Main.openBase.getWidth() > 36) {
		    			Main.openBase.setWidth(Main.openBase.getWidth() - 1);
		    			Main.openBase.setHeight(Main.openBase.getHeight() - 1);
		    		}
	    		}
	    		else {
	    			Main.openBase.setWidth(36);
		    		Main.openBase.setHeight(36);
	    		}
	    	}
	    	if (width > MINIMUM_Y) {
	    		Main.openBase.setY(Screen.DECORATION_HEIGHT + 70);
	    		
	    		if (allowAnimations) {
		    		if (Main.openBase.getWidth() < 48) {
		    			Main.openBase.setWidth(Main.openBase.getWidth() + 1);
		    			Main.openBase.setHeight(Main.openBase.getHeight() + 1);
		    		}
		    		else if (Main.openBase.getWidth() > 48) {
		    			Main.openBase.setWidth(Main.openBase.getWidth() - 1);
		    			Main.openBase.setHeight(Main.openBase.getHeight() - 1);
		    		}
	    		}
	    		else {
	    			Main.openBase.setWidth(48);
		    		Main.openBase.setHeight(48);
	    		}
	    	}
	    	
	    	files.forEach((l) -> l.setWidth(width));
	    	
	    	///
	    	
	    	if (scope != null) {
	    		if (scope.getRegent().getParentFile().equals(Main.baseFolder))
	    			scope.setParent(null);
	    		
	    		if (scope.getRegent().equals(Main.baseFolder))
	    			scope = null;
	    	}
	    	
	    	showBaseFolderCard = false;
	    	showFolderPathCard = false;
	    	
	    	/*if (selected == null && KeyInput.isKeyPressed() && KeyInput.isControlDown()) {
	    		ListableFile first = files.get(0);
	    		ListableFile last = files.get(files.size() - 1);
	    		
	    		if (!KeyInput.isAltDown()) {
					if (MouseInput.wheelUp() && first.getY() < MINIMUM_Y) first.setY(first.getY() + 30);
					else if (MouseInput.wheelDown() && last.getY() > MINIMUM_Y) first.setY(first.getY() - 30);
	    		}
	    		else {
	    			if (MouseInput.wheelUp() && first.getY() < MINIMUM_Y) first.setY(first.getY() + 90);
					else if (MouseInput.wheelDown() && last.getY() > MINIMUM_Y) first.setY(first.getY() - 90);
	    		}
	    		
	    		if (first.getY() > MINIMUM_Y) first.setY(MINIMUM_Y);
	    		if (last.getY() < MINIMUM_Y) first.setY(230 - (files.size() * 30));
	    	}*/
	    	
	    	if (KeyInput.isKeyPressed()) {
	    		// Atalhos
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_Q) { // Ctrl + Q (Selecionar Pasta Base)
	    			KeyInput.updateKeys();
	    			
	    			OpenBaseFolderButton.openBaseFolder();
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_F) { // Ctrl + Shift + F (Criar Nova Pasta)
	    			KeyInput.updateKeys();
	    			
	    			Main.editor.execute("newfolder");
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_N) { // Ctrl + Shift + N (Criar Novo Arquivo)
	    			KeyInput.updateKeys();
	    			
	    			Main.editor.execute("newfile");
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_H) { // Ctrl + H (Uma Pasta Acima)
	    			KeyInput.updateKeys();
	    			
	    			if (SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added || Explorer.selected != null) return;
	    			
	    			OneFolderUpButton.oneFolderUp();
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_G) { // Ctrl + G (Retornar à Pasta Base)
	    			KeyInput.updateKeys();
	    			
	    			ReturnToBaseFolderButton.returnToBaseFolder();
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_W) { // Ctrl + W (Recarregar)
	    			KeyInput.updateKeys();
	    			
	    			ReloadButton.reloadExplorer();
	    			
	    			return;
	    		}
	    	}
	    	
	    	if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered())) && !hoveringListableFile) {
	    		int widthDraw = Main.lang == Language.PORT ? 440 : 420;
	    		
	    		List<RightClickOption> list = new ArrayList<>();
	    		
	    		list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), widthDraw, Texts.createFile, (s) -> Main.editor.execute(s), "newfile", true));
				list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, widthDraw, Texts.createFolder, (s) -> Main.editor.execute(s), "newfolder"));
	    		
				list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, widthDraw, Texts.openCmd, (s) -> Main.editor.execute(s), "cmd"));
				list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, widthDraw, Texts.openTerminal, (s) -> Main.editor.execute(s), "term"));
				
				if (Main.baseFolder != null) {
					list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, widthDraw, Texts.openExplorer, (s) -> Main.editor.execute(s), "sysexp"));
					list.add(new RightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 150, widthDraw, Texts.setBaseFolder, (s) -> Main.editor.execute(s), "setbase"));
				}
				
				IDEComponent.addRightClickOptions(MouseInput.getMouseX(), MouseInput.getMouseY(), list.toArray(new RightClickOption[list.size()]));
			}
	    	
	    	if (Explorer.scope == null) Explorer.folderPath = "";
	    	else if (Explorer.scope.getParent() == null) Explorer.folderPath = Explorer.scope.getRegent().getName();
			else Explorer.folderPath = Explorer.scope.getParent().getRegent().getName() + " / " + Explorer.scope.getRegent().getName();
	    	
	    	folderPathFull = folderPath;
	    	
	    	if (files.size() != 0) {
		    	if (MouseInput.isMouseRolling() && hovered()) {
		    		for (IDEComponent i : components) {
		    			if (i instanceof RightClickOption)
		    				IDEComponent.toRemove.add(i);
		    		}
		    		
		    		ListableFile first = files.get(0);
		    		ListableFile last = files.get(files.size() - 1);
		    		
		    		if (!KeyInput.isControlDown()) {
						if (MouseInput.wheelUp() && first.getY() < MINIMUM_Y) first.setY(first.getY() + 30);
						else if (MouseInput.wheelDown() && last.getY() > MINIMUM_Y) first.setY(first.getY() - 30);
		    		}
		    		else {
		    			if (MouseInput.wheelUp() && first.getY() < MINIMUM_Y) first.setY(first.getY() + 90);
						else if (MouseInput.wheelDown() && last.getY() > MINIMUM_Y) first.setY(first.getY() - 90);
		    		}
		    		
		    		if (first.getY() > MINIMUM_Y) first.setY(MINIMUM_Y);
		    		if (last.getY() < MINIMUM_Y) first.setY(230 - (files.size() * 30));
				}
	    	}
	    	
	    	hoveringListableFile = false;
	    	
	    	if (folderPath.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4) >= width) {
	        	folderPath = folderPath.substring(0, maxTextWidth - 3) + "...";
	        	showFolderPathCard = true;
	    	}
	    	
//	    	if (baseFolderName != null && baseFolderName.length() * 20 /*24 - 4*/ >= width) {
//	        	baseFolderName = baseFolderName.substring(0, maxTextWidth - 3 > baseFolderName.length() ? baseFolderName.length() : maxTextWidth - 3) + "...";
//	        	showBaseFolderCard = true;
//	    	}
	    	
	    	//System.out.println(Main.baseFolder.getName().substring(0, maxTitleWidth - 3));
	    	
	    	baseFolderName = Main.baseFolder.getName().length() > maxTitleWidth ? Main.baseFolder.getName().substring(0, maxTitleWidth - 3) + "..." : Main.baseFolder.getName();
	    	
	    	if (Main.baseFolder.getName().length() > maxTitleWidth)
	    		showBaseFolderCard = true;
	    	
	    	// if (f.getY() < MINIMUM_Y || f.getY() > Main.screen.getHeight()) continue;
	    	
	    	try {
		    	for (ListableFile f : Explorer.files)
		        	f.tick();
	    	} catch (Exception e) { return; }
	    }
	    
	    for (ExplorerTab t : tabs)
	    	t.tick();
    }
    
    private void renderExplorer(Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	if (Main.baseFolder == null || baseFolderName == null) {
        	for (ExplorerTab t : tabs)
    	    	t.render(g);
    	    
    	    // linha encima do explorer
    	    g.setColor(Colors.textLight);
    		g2.setStroke(new BasicStroke(3f));
    	    g2.drawLine(0, ExplorerTab.Y + ExplorerTab.SIZE, width - 4, ExplorerTab.Y + ExplorerTab.SIZE);
    	    
    	    // Desenhar encima da tab
    	    for (ExplorerTab t : tabs) {
    	    	if (Explorer.explorerMode == t.regent) {
    	    		Color bg = t.hovered() ? Colors.explorerLight : Colors.codeEditor;
    	    		
    				g.setColor(bg);
    				g.fillRect(t.getX() + 2, ExplorerTab.Y + ExplorerTab.SIZE - 3, ExplorerTab.SIZE - 3, 8);
    			}
    	    }
    	    
    	    for (Tab t : Main.editor.tabs) {
    	    	if (Main.editor.editing == t && Main.editor.editing.getX() + Main.editor.tabScr == Main.editor.getX()) {
    	    		g.setColor(Colors.textLight);
    	    		g2.setStroke(new BasicStroke(3f));
    	    		
    	    		// linha à esquerda da primeira tab
    	    		g.drawLine(Main.editor.getX(), Screen.DECORATION_HEIGHT + 3, Main.editor.getX(), CodeEditor.MIN_Y - 1);
    	        }
    	    }
        	
        	return;
        }
        
        Fonts.drawString(baseFolderName, x + 10, y + 140, new IDEFont(Fonts.lightGrayNormal, 24), false, g);
    
    	g2.setStroke(new BasicStroke(4f));
        g.setColor(Colors.explorerLight);
        g2.drawLine(0, Screen.DECORATION_HEIGHT + 199, width - 1, Screen.DECORATION_HEIGHT + 199); // linha que divide os listablefiles
        
        Fonts.drawString(folderPath, x + 10, Screen.DECORATION_HEIGHT + 170, new IDEFont(Fonts.lightGrayNormal, 16), g);
        
        try {
	        for (ListableFile f : Explorer.files) {
	        	if (f.getY() < MINIMUM_Y || f.getY() > Main.screen.getHeight()) continue;
	        	
	        	f.render(g);
	        }
        } catch (Exception e) { return; }
    }
    
    private void renderSearchReplace(Graphics g) {
    	String text = Texts.file + ": " + (Main.editor.editing != null ? Main.editor.editing.getRegent().getRegent().getName() : "");
    	int cutLength = 0;
    	
    	if ((text.length() * 12) + 20 >= width) {
    		while ((text.substring(0, text.length() - cutLength).length() * 12) + 20 >= width)
    			cutLength++;
    	}
    	
    	text = text.substring(0, text.length() - cutLength);
    	
    	Fonts.drawString(text, x + 20, y + 60, new IDEFont(Fonts.lightGrayNormal, 16), g);
    	
		Fonts.drawString(Texts.search + ":", x + 20, y + 95, new IDEFont(Fonts.lightGrayNormal, 16), g);
    	Fonts.drawString(Texts.replace + ":", x + 20, y + 165, new IDEFont(Fonts.lightGrayNormal, 16), g);
	}
    
    private void renderGit(Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	if (isBaseFolderRepository()) {
	    	Fonts.drawString("Branches", 20, Screen.DECORATION_HEIGHT + 50, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	g2.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(2f));
	    	g2.drawLine(20 + ("Branches".length() * 12) + 10, Screen.DECORATION_HEIGHT + 60, width - 20, Screen.DECORATION_HEIGHT + 60);
	
	    	g.drawImage(Main.branch, 15, Screen.DECORATION_HEIGHT + 80, 32, 32, null);
	    	Fonts.drawString("| " + gitStatus.branches[gitStatus.currentBranch], 55, Screen.DECORATION_HEIGHT + 85, new IDEFont(Fonts.lightGrayEditor, CodeEditor.DEFAULT_FONT_SIZE), width, g);
	
	    	Fonts.drawString(Texts.general, 20, Screen.DECORATION_HEIGHT + 200, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	g2.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(2f));
	    	g2.drawLine(20 + (Texts.general.length() * 12) + 10, Screen.DECORATION_HEIGHT + 210, width - 20, Screen.DECORATION_HEIGHT + 210);
	
	    	Fonts.drawString("URL:", 20, Screen.DECORATION_HEIGHT + 270, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	if (components.indexOf(stageAll) < 0) {
	    		GitCore.init();
	    	}
	
	    	Fonts.drawString(gitStatus.changedFiles.length + " " + (gitStatus.changedFiles.length == 1 ? Texts.fileChanged : Texts.filesChanged), 20, Screen.DECORATION_HEIGHT + 400, new IDEFont(Fonts.lightGrayNormal, 16), Main.explorer.getWidth(), g);
	    	
	    	Fonts.drawString("Staging", 20, Screen.DECORATION_HEIGHT + 370, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	g2.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(2f));
	    	g2.drawLine(20 + ("Staging".length() * 12) + 10, Screen.DECORATION_HEIGHT + 380, width - 20, Screen.DECORATION_HEIGHT + 380);
	    	
	    	Fonts.drawString("Commit & Push", 20, Screen.DECORATION_HEIGHT + 510, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	g2.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(2f));
	    	g2.drawLine(20 + ("Commit & Push".length() * 12) + 10, Screen.DECORATION_HEIGHT + 520, width - 20, Screen.DECORATION_HEIGHT + 520);
	    	
	    	/*if (MouseInput.hovered(0, Screen.DECORATION_HEIGHT + 380, width, 40)) {
	    		Explorer.renderCardText(new String[] { "Click to see changed files" }, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), g);
	    	}*/
    	}
    	else {
    		Fonts.drawString(Texts.general, 20, Screen.DECORATION_HEIGHT + 50, new IDEFont(Fonts.lightGrayNormal, 16), g);
	    	g2.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(2f));
	    	g2.drawLine(20 + (Texts.general.length() * 12) + 10, Screen.DECORATION_HEIGHT + 60, width - 20, Screen.DECORATION_HEIGHT + 60);
	
	    	Fonts.drawString("URL:", 20, Screen.DECORATION_HEIGHT + 130, new IDEFont(Fonts.lightGrayNormal, 16), g);
    	}
    }
    
    private void renderTerminal(Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	Fonts.drawString(Texts.settings, 20, Screen.DECORATION_HEIGHT + 50, new IDEFont(Fonts.lightGrayNormal, 16), g);
    	g2.setColor(Colors.textLight);
    	g2.setStroke(new BasicStroke(2f));
    	g2.drawLine(20 + (Texts.settings.length() * 12) + 10, Screen.DECORATION_HEIGHT + 60, width - 20, Screen.DECORATION_HEIGHT + 60);
    	
    	Fonts.drawString("Terminal", 20, Screen.DECORATION_HEIGHT + 170, new IDEFont(Fonts.lightGrayNormal, 16), g);
    	g2.setColor(Colors.textLight);
    	g2.setStroke(new BasicStroke(2f));
    	g2.drawLine(20 + ("Terminal".length() * 12) + 10, Screen.DECORATION_HEIGHT + 180, width - 20, Screen.DECORATION_HEIGHT + 180);
    	
    	for (TerminalTab t : TerminalCore.tabs)
    		t.render(g);
    	
    	if (TerminalCore.selected != null) {
	    	// linha das terminaltabs
    		g.setColor(Colors.textLight);
	    	g2.setStroke(new BasicStroke(3f));
	    	g.drawLine(0, TerminalTab.Y_EXPLORER + TerminalTab.HEIGHT, width - 4, TerminalTab.Y_EXPLORER + TerminalTab.HEIGHT);
	    	
	    	g.setColor(TerminalCore.selected.hovered() ? Colors.explorerLight : Colors.explorer);
	    	g.fillRect(TerminalCore.selected.getX() + 2, TerminalTab.Y_EXPLORER + TerminalTab.HEIGHT - 4, TerminalCore.selected.getWidth() - 3, 6);
    	}
    }
    
    public static int getHighestNumber(int... arr) {
    	if (arr.length == 0) return -1;
    	
    	int highest = arr[0];
    	
    	for (int i = 0; i < arr.length; i++) {
    		if (arr[i] > highest)
    			highest = arr[i];
    	}
    	
    	return highest;
    }
    
    public static int[] arrayOfLengths(String... arr) {
    	int[] ret = new int[arr.length];
    	
    	for (int i = 0; i < arr.length; i++) {
    		ret[i] = arr[i].length();
    	}
    	
    	return ret;
    }
    
    public static void renderDescriptionText(String s, int x, int y, Graphics g) {
    	g.setColor(Colors.setAlpha(Color.black, CodeEditor.CURSOR_OPACITY));
    	g.fillRect(x, y, (s.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6, 27);
    	
    	Fonts.drawString(s, x + 4, y + 4, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
    }
    
    public static void renderCardText(String[] s, int x, int y, Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	Rectangle bounds = new Rectangle(x - 5, y - 5, (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10, (s.length * 20) + 15);
    	
    	Rectangle intBottom = bounds.intersection(new Rectangle(0, Main.screen.getHeight() - 2, Main.screen.getWidth(), 9999));
    	Rectangle intRight = bounds.intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 9999, Main.screen.getHeight()));
    	
    	int drawX = bounds.x - (intRight.width > 0 ? intRight.width : 0);
    	int drawY = bounds.y - (intBottom.height > 0 ? intBottom.height : 0);
    	int drawW = (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10;
    	int drawH = (s.length * 20) + 15;
    	
    	g2.setStroke(new BasicStroke(2));
    	
    	g.setColor(Colors.explorerLight);
    	g.fillRect(drawX, drawY, drawW, drawH);
    	g.setColor(Colors.textLight);
    	g.drawRect(drawX, drawY, drawW, drawH);
    	
    	int i = 0;
    	for (String ss : s) {
    		Fonts.drawString(ss, drawX + 5, drawY + 5 + (20 * i++), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
    	}
    }
    
    public static void renderCardText(String[] s, int x, int y, int margin, Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	Rectangle bounds = new Rectangle(x - 5, y - 5, (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10, (s.length * 20) + 15);
    	
    	Rectangle intBottom = bounds.intersection(new Rectangle(0, Main.screen.getHeight() - 2, Main.screen.getWidth(), 9999));
    	Rectangle intRight = bounds.intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 9999, Main.screen.getHeight()));
    	
    	int drawX = bounds.x - (intRight.width > 0 ? intRight.width : 0);
    	int drawY = bounds.y - (intBottom.height > 0 ? intBottom.height : 0);
    	int drawW = (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10;
    	int drawH = (s.length * (20 + margin)) + 15;
    	
    	drawH -= margin;
    	
    	g2.setStroke(new BasicStroke(2));
    	
    	g.setColor(Colors.explorerLight);
    	g.fillRect(drawX, drawY, drawW, drawH);
    	g.setColor(Colors.textLight);
    	g.drawRect(drawX, drawY, drawW, drawH);
    	
    	int i = 0;
    	for (String ss : s) {
    		Fonts.drawString(ss, drawX + 5 + (margin / 4), drawY + 5 + (margin / 4) + ((20 + margin) * i++), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
    	}
    }
    
    public static void renderCardText(String[] s, int x, int y, int margin, boolean overlay, Graphics g) {
    	Graphics2D g2 = (Graphics2D) g;
    	
    	Rectangle bounds = new Rectangle(x - 5, y - 5, (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10, (s.length * 20) + 15);
    	
    	Rectangle intBottom = bounds.intersection(new Rectangle(0, Main.screen.getHeight() - 2, Main.screen.getWidth(), 9999));
    	Rectangle intRight = bounds.intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 9999, Main.screen.getHeight()));
    	
    	int drawX = bounds.x - (intRight.width > 0 ? intRight.width : 0);
    	int drawY = bounds.y - (intBottom.height > 0 ? intBottom.height : 0);
    	int drawW = (getHighestNumber(arrayOfLengths(s)) * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6 + 10;
    	int drawH = (s.length * (20 + margin)) + 15;
    	
    	drawH -= margin;
    	
    	g2.setStroke(new BasicStroke(2));
    	
    	g.setColor(Colors.explorerLight);
    	g.fillRect(drawX, drawY, drawW, drawH);
    	g.setColor(Colors.textLight);
    	g.drawRect(drawX, drawY, drawW, drawH);
    	
    	int i = 0;
    	for (String ss : s) {
    		Fonts.drawString(ss, drawX + 5 + (margin / 4), drawY + 5 + (margin / 4) + ((20 + margin) * i++), new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
    	}
    	
    	if (overlay) {
	    	g.setColor(Colors.setAlpha(Color.black, 50));
	    	g.fillRect(drawX - 1, drawY - 1, drawW + 2, drawH + 2);
    	}
    }

    public synchronized void render(Graphics g) {
    	if (CommandTerminal.expOff) return;
    	
    	Graphics2D g2 = (Graphics2D) g;
    	
        g.setColor(Colors.explorer);
        g.fillRect(x, y, width, height);

        g.setColor(Colors.explorerLight);
	    g2.setStroke(new BasicStroke(3f));
	    g2.drawLine(width - 1, Screen.DECORATION_HEIGHT, width - 1, height); // linha vertical que divide do codeeditor
	        
	    if (explorerMode == ExplorerMode.EXPLORER)
	        renderExplorer(g);
	    else if (explorerMode == ExplorerMode.SEARCHREPLACE)
	    	renderSearchReplace(g);
	    else if (explorerMode == ExplorerMode.GIT)
	    	renderGit(g);
	    else if (explorerMode == ExplorerMode.TERMINAL)
	    	renderTerminal(g);
	    
	    for (Tab t : Main.editor.tabs) {
	    	if (Main.editor.editing == t && Main.editor.editing.getX() + Main.editor.tabScr == Main.editor.getX() - 1) {
	    		g.setColor(Colors.textLight);
	    		g2.setStroke(new BasicStroke(3f));
	    		
	    		// linha à esquerda da primeira tab
	    		g.drawLine(Main.editor.getX() - 1, Screen.DECORATION_HEIGHT + 3, Main.editor.getX() - 1, CodeEditor.MIN_Y + 3); // + 4
	        }
	    }
	    
	    // desenhar linha menor do mesmo jeito pra conectar as duas linhas
	    if (!Main.editor.tabs.isEmpty()) {
		    g.setColor(Colors.textLight);
			g2.setStroke(new BasicStroke(3f));
			g.drawLine(Main.editor.getX() - 1, CodeEditor.MIN_Y - 2, Main.editor.getX() - 1, CodeEditor.MIN_Y + 3);
	    }
	    
	    for (ExplorerTab t : tabs)
	    	t.render(g);
	    
	    // linha encima do explorer
	    g.setColor(Colors.textLight);
		g2.setStroke(new BasicStroke(3f));
	    g2.drawLine(0, ExplorerTab.Y + ExplorerTab.SIZE, Main.editor.tabs.isEmpty() ? width - 4 : width - 1, ExplorerTab.Y + ExplorerTab.SIZE); // width
	    
	    // Desenhar encima da tab
	    for (ExplorerTab t : tabs) {
	    	if (Explorer.explorerMode == t.regent) {
	    		Color bg = t.hovered() ? Colors.explorerLight : Colors.explorer;
	    		
				g.setColor(bg);
				g.fillRect(t.getX() + 2, ExplorerTab.Y + ExplorerTab.SIZE - 3, ExplorerTab.SIZE - 3, 8);
			}
	    }
    }
}
