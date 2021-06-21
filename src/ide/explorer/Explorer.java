package ide.explorer;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;
import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.List;

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
    if (scope == null)
      return Main.baseFolder.getAbsolutePath(); 
    return scope.getRegent().getAbsolutePath();
  }
  
  public void tick() {
    if (CommandTerminal.expOff)
      return; 
    if (Main.baseFolder == null || !Main.baseFolder.exists()) {
      CommandTerminal.runCommand("closebasefolder");
      return;
    } 
    if (scope != null) {
      if (scope.getRegent().equals(Main.baseFolder))
        scope = null; 
      if (scope.getRegent().getParentFile().equals(Main.baseFolder))
        scope.setParent((ListableFile)null); 
    } 
    if (rightClicked() && !hoveringListableFile) {
      IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), 540, "Abrir Prompt de Comando", s -> Main.editor.execute(s), "cmd");
      IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, 540, "Abrir Terminal de Comando", s -> Main.editor.execute(s), "term");
      if (Main.baseFolder != null) {
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, 540, "Abrir no Explorador de Arquivos", s -> Main.editor.execute(s), "sysexp");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, 540, "Definir pasta atual como Pasta Base", s -> Main.editor.execute(s), "setbase");
      } 
    } 
    if (scope == null) {
      folderPath = "";
    } else if (scope.getParent() == null) {
      folderPath = scope.getRegent().getName();
    } else {
      folderPath = String.valueOf(scope.getParent().getRegent().getName()) + " / " + scope.getRegent().getName();
    } 
    if (files.size() == 0)
      return; 
    if (MouseInput.isMouseRolling() && hovered()) {
      ListableFile first = files.get(0);
      ListableFile last = files.get(files.size() - 1);
      if (MouseInput.wheelUp() && first.getY() < 200) {
        first.setY(first.getY() + 30);
      } else if (MouseInput.wheelDown() && last.getY() > 200) {
        first.setY(first.getY() - 30);
      } 
    } 
    hoveringListableFile = false;
  }
  
  public void render(Graphics g) {
    if (CommandTerminal.expOff)
      return; 
    Graphics2D g2 = (Graphics2D)g;
    g.setColor(Colors.explorer);
    g.fillRect(this.x, this.y, this.width, this.height);
    Fonts.drawString("Explorador", this.x + 40, this.y + 30, new IDEFont(Fonts.lightGrayNormal, 23), g);
    g.setColor(Colors.textLight);
    g2.setStroke(new BasicStroke(2.0F));
    g.drawLine(this.x + 40, this.y + 60, this.x + 220, this.y + 60);
    if (Main.baseFolder != null) {
      Fonts.drawString((Main.baseFolder.getName().length() > 15) ? (String.valueOf(Main.baseFolder.getName().substring(0, 12)) + "...") : Main.baseFolder.getName(), this.x + 10, this.y + 140, new IDEFont(Fonts.lightGrayNormal, 23), g);
      g2.setStroke(new BasicStroke(4.0F));
      g.setColor(Colors.explorerLight);
      g2.drawLine(0, 199, this.width, 199);
    } 
    Fonts.drawString((folderPath.length() > 21) ? (String.valueOf(folderPath.substring(0, 19)) + "...") : folderPath, this.x + 10, 170, new IDEFont(Fonts.lighterGrayNormal, 15), g);
  }
}