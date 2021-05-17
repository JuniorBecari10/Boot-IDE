package ide.explorer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class Explorer extends IDEComponent {
	
	public static List<ListableFile> files;
	public static List<ListableFile> toRemove;
	
	public static ListableFile scope;

	public static String folderPath = "";
	
	public static boolean hoveringListableFile;
	
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
    	if (CommandTerminal.expOff) return;
    	
    	if (rightClicked() && !hoveringListableFile) {
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), 540, "Abrir Prompt de Comando", (s) -> Main.editor.execute(s), "cmd");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, 540, "Abrir Terminal de Comando", (s) -> Main.editor.execute(s), "term");
			
			if (Main.baseFolder != null) {
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, 540, "Abrir no Explorador de Arquivos", (s) -> Main.editor.execute(s), "sysexp");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, 540, "Definir pasta atual como Pasta Base", (s) -> Main.editor.execute(s), "setbase");
			}
		}
    	
    	if (Explorer.scope == null) Explorer.folderPath = "";
    	else if (Explorer.scope.getParent() == null) Explorer.folderPath = Explorer.scope.getRegent().getName();
		else Explorer.folderPath = Explorer.scope.getParent().getRegent().getName() + " / " + Explorer.scope.getRegent().getName();
    	
    	if (files.size() == 0) return;
    	
    	if (MouseInput.isMouseRolling() && hovered()) {
    		ListableFile first = files.get(0);
    		ListableFile last = files.get(files.size() - 1);
    		
			if (MouseInput.wheelUp() && first.getY() < 200) first.setY(first.getY() + 30);
			else if (MouseInput.wheelDown() && last.getY() > 200) first.setY(first.getY() - 30);
		}
    	
    	hoveringListableFile = false;
    }

    public void render(Graphics g) {
    	if (CommandTerminal.expOff) return;
    	
    	Graphics2D g2 = (Graphics2D) g;
    	
        g.setColor(Colors.explorer);
        g.fillRect(x, y, width, height);   
        
        Fonts.drawString("Explorador", x + 40, y + 30, new IDEFont(Fonts.lightGrayNormal, 23), g);
        g.setColor(Color.decode("#95afc0"));
        
        g2.setStroke(new BasicStroke(2f));
        g.drawLine(x + 40, y + 60, x + 220, y + 60);
        
        if (Main.baseFolder != null) {
    		Fonts.drawString(Main.baseFolder.getName(), x + 10, y + 140, new IDEFont(Fonts.lightGrayNormal, 23), g);
        
    		g2.setStroke(new BasicStroke(4f));
            g.setColor(Colors.explorerLight);
            g2.drawLine(0, 199, width, 199);
        }
        
        Fonts.drawString(folderPath, x + 10, 170, new IDEFont(Fonts.lighterGrayNormal, 15), g);
    }
}
