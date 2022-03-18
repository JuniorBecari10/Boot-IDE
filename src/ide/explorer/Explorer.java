package ide.explorer;

import java.awt.BasicStroke;
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
import ide.components.ReloadButton;
import ide.components.RenameFile;
import ide.components.ReturnToBaseFolderButton;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Texts;

public class Explorer extends IDEComponent {
	
	public static List<ListableFile> files;
	public static List<ListableFile> toRemove;
	
	public static ListableFile scope;

	public static String folderPath = "";
	public static String folderPathFull = "";
	
	public static boolean hoveringListableFile;
	
	public static boolean showBaseFolderCard = false;
	public static boolean showFolderPathCard = false;
	
	public static boolean dragging = false;
	
	public static String baseFolderName;
	
	public static int minDrag = 192;
	
	public int maxTitleWidth = width / 23;
	public int maxFolderWidth = width / 15;
	public int maxTextWidth = width / 16;
	public int maxFileCreateWidth = width / 18;
	
    public Explorer(int x, int y, int width, int height) {
        super(x, y, width, height, null);
        
        files = new ArrayList<>();
        toRemove = new ArrayList<>();
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
    	
    	if (ListableFile.files.isEmpty() && files.isEmpty()) hoveringListableFile = false;
    	
    	if (Main.baseFolder == null || !Main.baseFolder.exists()) {
    		CommandTerminal.runCommand("closebasefolder");
    		
    		return;
    	}
    	
    	if (hovered() && !ListableFile.isListableFileHovered() && !MouseInput.hovered(x + width - 5, y, 10, height)) {
    		//System.out.println("a");
    		Main.screen.setCursor(Cursor.getDefaultCursor());
    	}
    	
    	maxTitleWidth =  (width / 23) + 2;
    	maxFolderWidth = (width / 15);
    	maxTextWidth =   (width / 16) + 2;
    	maxFileCreateWidth = width / 18 + 2;
    	
    	// Media Queries
    	
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
    	
    	if (width < 200) {
    		Main.openBase.setY(75);
    		
    		Main.openBase.setWidth(36);
    		Main.openBase.setHeight(36);
    	}
    	if (width > 200) {
    		Main.openBase.setY(70);
    		
    		Main.openBase.setWidth(48);
    		Main.openBase.setHeight(48);
    	}
    	
    	files.forEach((l) -> l.setWidth(width));
    	
    	///
    	
    	if (scope != null) {
    		if (scope.getRegent().equals(Main.baseFolder))
    			scope = null;
    		
    		if (scope.getRegent().getParentFile().equals(Main.baseFolder))
    			scope.setParent(null);
    	}
    	
    	showBaseFolderCard = false;
    	showFolderPathCard = false;
    	
    	if (KeyInput.isKeyPressed()) {
    		// Atalhos
    		
    		if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_N) { // Ctrl + Shift + N (Criar Nova Pasta)
    			KeyInput.updateKeys();
    			
    			Main.editor.execute("newfolder");
    			
    			return;
    		}
    		
    		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_H) { // Ctrl + H (Uma Pasta Acima)
    			KeyInput.updateKeys();
    			
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
	    		
				if (MouseInput.wheelUp() && first.getY() < 200) first.setY(first.getY() + 30);
				else if (MouseInput.wheelDown() && last.getY() > 200) first.setY(first.getY() - 30);
			}
    	}
    	
    	hoveringListableFile = false;
    	
    	if (folderPath.length() > maxFolderWidth) {
        	folderPath = folderPath.substring(0, maxFolderWidth - 3) + "...";
        	showFolderPathCard = true;
    	}
    	
    	baseFolderName = Main.baseFolder.getName().length() > maxTitleWidth ? Main.baseFolder.getName().substring(0, maxTitleWidth - 3) + "..." : Main.baseFolder.getName();
    	
    	if (Main.baseFolder.getName().length() > maxTitleWidth)
    		showBaseFolderCard = true;
    	
    	// if (f.getY() < 200 || f.getY() > Main.screen.getHeight()) continue;
    	
    	try {
	    	for (ListableFile f : Explorer.files)
	        	f.tick();
    	} catch (Exception e) { return; }
    }

    public void render(Graphics g) {
    	if (CommandTerminal.expOff) return; // melhorar o ícone do img, e adicionar suorte ao formato .o
    	
    	Graphics2D g2 = (Graphics2D) g;
    	
        g.setColor(Colors.explorer);
        g.fillRect(x, y, width, height); 
        
        int xd = Main.lang == Language.PORT ? x + 40 : x + 60;
        int x2d = Main.lang == Language.PORT ? xd + 14 : xd + 16; // dar uma arrumada
        
        Fonts.drawString(Texts.explorerText, (width / 2 - xd) - (Main.lang == Language.PORT ? 40 : 0), y + 30, new IDEFont(Fonts.lightGrayNormal, 23), g);
        g.setColor(Colors.textLight);
        
        g2.setStroke(new BasicStroke(2f));
        g.drawLine(width / 2 - xd, y + 60, width / 2 + x2d, y + 60);
        
        g.setColor(Colors.explorerLight);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(width - 1, 0, width - 1, height);
        
        if (Main.baseFolder == null || baseFolderName == null) return;
        
        Fonts.drawString(baseFolderName, x + 10, y + 140, new IDEFont(Fonts.lightGrayNormal, 23), g);
        
    	g2.setStroke(new BasicStroke(4f));
        g.setColor(Colors.explorerLight);
        g2.drawLine(0, 199, width - 1, 199);
        
        Fonts.drawString(folderPath, x + 10, 170, new IDEFont(Fonts.lighterGrayNormal, 15), g);
        
        try {
	        for (ListableFile f : Explorer.files) {
	        	if (f.getY() < 200 || f.getY() > Main.screen.getHeight()) continue;
	        	
	        	f.render(g);
	        }
        } catch (Exception e) { return; }
        
        for (Tab t : Main.editor.tabs) {
        	if (Main.editor.editing == t && Main.editor.editing.getX() + Main.editor.tabScr == Main.editor.getX()) {
    			g.setColor(Colors.textLight);
    			g2.setStroke(new BasicStroke(3f));
    			
    			g.drawLine(Main.editor.getX(), 3, Main.editor.getX(), CodeEditor.MIN_Y - 1);
            }
        }
    }
}
