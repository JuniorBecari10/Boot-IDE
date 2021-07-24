package ide.explorer;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RenameFile;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
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
	
	public static String baseFolderName;
	
    public Explorer(int x, int y, int width, int height) {
        super(x, y, width, height, null);
        
        files = new ArrayList<>();
        toRemove = new ArrayList<>();
    }
    
    public static String getScopePath() {
    	if (scope == null) return Main.baseFolder.getAbsolutePath();
    	
    	return scope.getRegent().getAbsolutePath();
    }
    
    public void tick() {
    	if (SetFileName.added || CommandTerminal.active || RenameFile.added) return;
    	if (CommandTerminal.expOff) return;
    	
    	if (ListableFile.files.isEmpty() && files.isEmpty()) hoveringListableFile = false;
    	
    	if (Main.baseFolder == null || !Main.baseFolder.exists()) {
    		CommandTerminal.runCommand("closebasefolder");
    		
    		return;
    	}
    	
    	if (scope != null) {
    		if (scope.getRegent().equals(Main.baseFolder))
    			scope = null;
    		
    		if (scope.getRegent().getParentFile().equals(Main.baseFolder))
    			scope.setParent(null);
    	}
    	
    	showBaseFolderCard = false;
    	showFolderPathCard = false;
    	
    	if (rightClicked() && !hoveringListableFile) {
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
    	
    	if (folderPath.length() > 22) {
        	folderPath = folderPath.substring(0, 19) + "...";
        	showFolderPathCard = true;
    	}
    	
    	baseFolderName = Main.baseFolder.getName().length() > 15 ? Main.baseFolder.getName().substring(0, 12) + "..." : Main.baseFolder.getName();
    	
    	if (Main.baseFolder.getName().length() > 15)
    		showBaseFolderCard = true;
    	
    	// if (f.getY() < 200 || f.getY() > Main.screen.getHeight()) continue;
    	
    	for (ListableFile f : Explorer.files)
        	f.tick();
    }

    public void render(Graphics g) {
    	if (CommandTerminal.expOff) return; // melhorar o ícone do img, e adicionar suorte ao formato .o
    	
    	Graphics2D g2 = (Graphics2D) g;
    	
        g.setColor(Colors.explorer);
        g.fillRect(x, y, width, height); 
        
        int xd = Main.lang == Language.PORT ? x + 40 : x + 60;
        int x2d = Main.lang == Language.PORT ? x + 220 : x + 200;
        
        Fonts.drawString(Texts.explorerText, xd, y + 30, new IDEFont(Fonts.lightGrayNormal, 23), g);
        g.setColor(Colors.textLight);
        
        g2.setStroke(new BasicStroke(2f));
        g.drawLine(xd, y + 60, x2d, y + 60);
        
        if (Main.baseFolder == null || baseFolderName == null) return;
        
        Fonts.drawString(baseFolderName, x + 10, y + 140, new IDEFont(Fonts.lightGrayNormal, 23), g);
        
    	g2.setStroke(new BasicStroke(4f));
        g.setColor(Colors.explorerLight);
        g2.drawLine(0, 199, width, 199);
        
        Fonts.drawString(folderPath, x + 10, 170, new IDEFont(Fonts.lighterGrayNormal, 15), g);
        
        g.setColor(Colors.explorerLight);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(width - 1, 0, width - 1, height);
        
        for (ListableFile f : Explorer.files) {
        	if (f.getY() < 200 || f.getY() > Main.screen.getHeight()) continue;
        	
        	f.render(g);
        }
    }
}
