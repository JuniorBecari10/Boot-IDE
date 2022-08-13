package ide.explorer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.OneFolderUpButton;
import ide.components.OpenBaseFolderButton;
import ide.components.ReloadButton;
import ide.components.RenameFile;
import ide.components.ReturnToBaseFolderButton;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorercomponents.ExecuteButton;
import ide.explorercomponents.ExplorerTab;
import ide.explorercomponents.InputBox;
import ide.explorercomponents.SearchReplaceCore;
import ide.explorercomponents.SearchReplaceRadioButton;
import ide.explorercomponents.ToggleButton;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.screen.Screen;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class Explorer extends IDEComponent {
	
	public static List<ListableFile> files;
	public static List<ListableFile> toRemove;
	
	public static List<ExplorerTab> tabs;
	
	public static ListableFile scope;

	public static String folderPath = "";
	public static String folderPathFull = "";
	
	public static SetFileName setFileName;
	public static RenameFile renameFile;
	
	public static InputBox selected;
	
	public static InputBox search, replace;
	public static ToggleButton caseSensitive, regex;
	public static SearchReplaceRadioButton entireDocument, selectedLines;
	public static ExecuteButton searchNext, replaceAll;
	
	public static int MINIMUM_Y = 200 + Screen.DECORATION_HEIGHT;
	
	public static ExplorerMode explorerMode = ExplorerMode.EXPLORER;
	
	public static boolean hoveringListableFile;
	
	public static boolean showBaseFolderCard = false;
	public static boolean showFolderPathCard = false;
	
	public static boolean dragging = false;
	
	public static String baseFolderName;
	
	public static int minDrag = 192;
	
	public int maxTitleWidth = width / 23;
	public int maxTextWidth = width / 16;
	public int maxFileCreateWidth = width / 18;
	
    public Explorer(int x, int y, int width, int height) {
        super(x, y, width, height, null);
        
        tabs = new ArrayList<>();
        
        files = new ArrayList<>();
        toRemove = new ArrayList<>();
    }
    
    public void addTabs() {
    	tabs.add(new ExplorerTab(1, Main.explorerTab, ExplorerMode.EXPLORER, Texts.explorerText) {
    		public void select() {
    			SearchReplaceCore.dispose();
    		}
    	});
    	tabs.add(new ExplorerTab(1 + 3 + ExplorerTab.SIZE, Main.searchReplaceTab, ExplorerMode.SEARCHREPLACE, Texts.searchReplace) {
    		public void select() {
    			Main.editor.execute("searchrep");
    		}
    	});
    	tabs.add(new ExplorerTab(1 + 6 + (ExplorerTab.SIZE * 2), Main.gitTab, ExplorerMode.GIT, "Git"));
    	tabs.add(new ExplorerTab(1 + 9 + (ExplorerTab.SIZE * 3), Main.terminalTab, ExplorerMode.TERMINAL, "Terminal"));
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
    	if (SetFileName.added || CommandTerminal.active || RenameFile.added) return;
    	if (CommandTerminal.expOff) return;
    	
	    height = Main.screen.getHeight();
	    
	    /*if (WindowInput.isMaximized() || !WindowInput.isActivated())
	    	ReloadButton.reloadExplorer();*/
	    
	    if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE && explorerMode == ExplorerMode.SEARCHREPLACE) {
	    	SearchReplaceCore.dispose();
	    }
	    
	   	// Drag
	   	
	    if (MouseInput.hovered(x + width - 5, y, 10, height) && !Main.editor.selecting && !ListableFile.isListableFileHovered()) {
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
	    if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + T (Terminal)
			KeyInput.updateKeys();

			Main.editor.execute("term");

			return;
		}

	    
	    if (hovered())
	    	Main.screen.setCursor(Cursor.getDefaultCursor());
			
			if (explorerMode == ExplorerMode.SEARCHREPLACE) {
				if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_K) {
					KeyInput.updateKeys();
					
					CommandTerminal.runCommand("toggleexplorer");
				}
	    }
	    
	    if (explorerMode == ExplorerMode.EXPLORER) {
	    	if (ListableFile.files.isEmpty() && files.isEmpty()) hoveringListableFile = false;
	    	
	    	if (Main.baseFolder == null || !Main.baseFolder.exists()) {
	    		CommandTerminal.runCommand("closebasefolder");
	    		
	    		return;
	    	}
	    	
	    	if (hovered() && !ListableFile.isListableFileHovered() && !MouseInput.hovered(x + width - 5, y, 10, height))
	    		Main.screen.setCursor(Cursor.getDefaultCursor());
	    	
	    	maxTitleWidth =  (width / 23) + 2;
	    	maxTextWidth =   (width / 16) + 2;
	    	maxFileCreateWidth = width / 18 + 2;
	    	
	    	// Media Queries (só que em Java kkkk)
	    	
	    	if (width < 260) {
	    		Main.newFile.setWidth(24);
	    		Main.newFile.setHeight(24);
	    		
	    		Main.newFolder.setWidth(24);
	    		Main.newFolder.setHeight(24);
	    		
	    		Main.oneFolder.setWidth(24);
	    		Main.oneFolder.setHeight(24);
	    		
	    		Main.returnBase.setWidth(24);
	    		Main.returnBase.setHeight(24);
	    		
	    		Main.reload.setWidth(24);
	    		Main.reload.setHeight(24);
	    	}
	    	else if (width > 260) {
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
	    	
	    	if (width < MINIMUM_Y) {
	    		Main.openBase.setY(Screen.DECORATION_HEIGHT + 75);
	    		
	    		Main.openBase.setWidth(36);
	    		Main.openBase.setHeight(36);
	    	}
	    	if (width > MINIMUM_Y) {
	    		Main.openBase.setY(Screen.DECORATION_HEIGHT + 70);
	    		
	    		Main.openBase.setWidth(48);
	    		Main.openBase.setHeight(48);
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
	    		
	    		if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_N) { // Ctrl + Shift + N (Criar Nova Pasta)
	    			KeyInput.updateKeys();
	    			
	    			Main.editor.execute("newfolder");
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_N) { // Ctrl + N (Criar Novo Arquivo)
	    			KeyInput.updateKeys();
	    			
	    			Main.editor.execute("newfile");
	    			
	    			return;
	    		}
	    		
	    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_H) { // Ctrl + H (Uma Pasta Acima)
	    			KeyInput.updateKeys();
	    			
	    			if (SetFileName.added || CommandTerminal.active || RenameFile.added || Explorer.selected != null) return;
	    			
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
	    		int widthDraw = Main.lang == Language.PORT ? 540 : 520;
	    		
	    		IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), widthDraw, Texts.createFile, (s) -> Main.editor.execute(s), "newfile");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, widthDraw, Texts.createFolder, (s) -> Main.editor.execute(s), "newfolder");
	    		
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, widthDraw, Texts.openCmd, (s) -> Main.editor.execute(s), "cmd");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, widthDraw, Texts.openTerminal, (s) -> Main.editor.execute(s), "term");
				
				if (Main.baseFolder != null) {
					IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, widthDraw, Texts.openExplorer, (s) -> Main.editor.execute(s), "sysexp");
					IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 150, widthDraw, Texts.setBaseFolder, (s) -> Main.editor.execute(s), "setbase");
				}
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
	    	
	    	if (baseFolderName.length() * (23 - 4) >= width) {
	        	baseFolderName = baseFolderName.substring(0, maxTextWidth - 3) + "...";
	        	showBaseFolderCard = true;
	    	}
	    	
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
        
        Fonts.drawString(baseFolderName, x + 10, y + 140, new IDEFont(Fonts.lightGrayNormal, 23), g);
    
    	g2.setStroke(new BasicStroke(4f));
        g.setColor(Colors.explorerLight);
        g2.drawLine(0, Screen.DECORATION_HEIGHT + 199, width - 1, Screen.DECORATION_HEIGHT + 199); // linha que divide os listablefiles
        
        Fonts.drawString(folderPath, x + 10, Screen.DECORATION_HEIGHT + 170, new IDEFont(Fonts.lighterGrayNormal, 16), g);
        
        try {
	        for (ListableFile f : Explorer.files) {
	        	if (f.getY() < MINIMUM_Y || f.getY() > Main.screen.getHeight()) continue;
	        	
	        	f.render(g);
	        }
        } catch (Exception e) { return; }
    }
    
    public static void renderDescriptionText(String s, int x, int y, Graphics g) {
    	g.setColor(Colors.setAlpha(Color.black, CodeEditor.CURSOR_OPACITY));
    	g.fillRect(x, y, (s.length() * (CodeEditor.DEFAULT_FONT_SIZE - 4)) + 6, 27);
    	
    	Fonts.drawString(s, x + 4, y + 4, new IDEFont(Fonts.lightGrayNormal, CodeEditor.DEFAULT_FONT_SIZE), g);
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
	    
	    for (Tab t : Main.editor.tabs) {
	    	if (Main.editor.editing == t && Main.editor.editing.getX() + Main.editor.tabScr == Main.editor.getX()) {
	    		g.setColor(Colors.textLight);
	    		g2.setStroke(new BasicStroke(3f));
	    		
	    		// linha à esquerda da primeira tab
	    		g.drawLine(Main.editor.getX(), Screen.DECORATION_HEIGHT + 3, Main.editor.getX(), CodeEditor.MIN_Y - 1);
	        }
	    }
	    
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
    }
}
