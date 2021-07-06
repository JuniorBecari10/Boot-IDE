/**
 * Boot IDE
 * 
 * Actual Version: Release 3.0
 * 
 * Changelog:
 * 
 * 1.0
 * 
 * - Release
 * 
 * 1.1
 * 
 * - Adicionada op√ß√£o "Limpar linha".
 * - Adicionado suporte para a linguagem Lua.
 * - Terminado colora√ß√£o de coment√°rios de uma linha s√≥ pra todas as linguagens.
 * - Ainda n√£o h√° colora√ß√£o para coment√°rios multi-linha
 * 
 * 1.2
 * 
 * - Corrigido Bugs:
 *  * Alternar para outra guia sem salvar o arquivo o corrompe;
 *  * Deletar o arquivo e a sua guia correspondente ficar aberta;
 *  * <Ainda n√£o corrigido> Clicar em um arquivo que j√° tem guia aberta e cria de novo outra guia com o mesmo arquivo. // Esse pode para corrigir mais pra frente pois ele n√£o √© cr√≠tico.
 *  
 *  - Adicionado suporte para a linguagem SQL.
 */

package ide.main;

import java.awt.BasicStroke;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.Logo;
import ide.components.NewFileButton;
import ide.components.NewFolderButton;
import ide.components.OneLevelAboveButton;
import ide.components.OpenBaseFolderButton;
import ide.components.ReloadButton;
import ide.components.ReturnToBaseFolderButton;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.ComponentInput;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.util.Colors;
import ide.util.Spritesheet;
import ide.util.Tickable;

public class Main implements Runnable, Tickable {

    private boolean running = false;

    public static Screen screen;
    public static Toolkit toolkit;

    private Thread t;

    public static Explorer explorer;
    public static CodeEditor editor;
    
    public static Spritesheet spritesheet;
    public static Logo logo;
    
    public static OpenBaseFolderButton openBase;
    
    public static OneLevelAboveButton oneLevel;
    public static ReturnToBaseFolderButton returnBase;
    public static NewFileButton newFile;
    public static NewFolderButton newFolder;
    public static ReloadButton reload;
    
    public static File baseFolder;
    public static File cnfFile;
    
    public static String sprsh = "/spritesheet.png";
    public static String fntnr = "/font.png";
    public static String fntbl = "/bold.png";
    public static String conffile = "none";
    
    private static int tabindex = -1;
    
    public static Desktop desktop;
    
    public static String[] args;
    
    public static boolean hasConfigFile = false;
    
    public static final File settingsFile = new File(System.getProperty("user.dir") + "\\settings.conf"); // 08/05/2021 - 15:48
    
    public Main() {
    	if (args == null)
    		args = new String[10];
    	
        toolkit = Toolkit.getDefaultToolkit();
        screen = new Screen("Boot IDE");
        
        Fonts.initFonts(fntnr, fntbl);
        spritesheet = new Spritesheet(sprsh);
        
        explorer = new Explorer(0, 0, 280, Screen.HEIGHT);
        editor = new CodeEditor(280, 0, Screen.WIDTH - 280, Screen.HEIGHT); // esses 2 precisa ser inicializados depois das fontes e da spritesheet
        
        logo = new Logo(Screen.WIDTH / 2 + 80, Screen.HEIGHT / 2 - 120, 160, 160, spritesheet.getSprite(32, 0, 16, 16));
        
        screen.setFrameIcon(spritesheet.getSprite(32, 0, 16, 16));
        
        openBase = new OpenBaseFolderButton(20, 70, 48, 48, spritesheet.getSprite(0, 0, 16, 16));
        oneLevel = new OneLevelAboveButton(160, 85, 32, 32, spritesheet.getSprite(64, 0, 16, 16));
        returnBase = new ReturnToBaseFolderButton(200, 85, 32, 32, spritesheet.getSprite(80, 0, 16, 16));
        newFile = new NewFileButton(80, 85, 32, 32, spritesheet.getSprite(96, 0, 16, 16));
        newFolder = new NewFolderButton(120, 85, 32, 32, spritesheet.getSprite(112, 0, 16, 16));
        reload = new ReloadButton(240, 85, 32, 32, spritesheet.getSprite(128, 0, 16, 16));

        desktop = Desktop.getDesktop();
        
        IDEComponent.components.add(editor);
        IDEComponent.components.add(explorer);
        
        IDEComponent.components.add(logo);
        
        IDEComponent.components.add(openBase);
        
        if (settingsFile.exists())
    		readFile(settingsFile);
        
        ListableFile.readConfigFile(conffile);
        Fonts.initFonts(fntnr, fntbl);
        spritesheet = new Spritesheet(sprsh);
        
        //openWith();
    }
    
    /*private void openWith() {
    	if (args == null || args[0] == null) return;
    	
    	try {
    		File f = new File(args[0]);
    		
    		baseFolder = f.getParentFile();
    		
    		CodeEditor.tabs.add(new Tab(Tab.MIN_X, ListableFile.search(f.getParentFile())));
			
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
			
			IDEComponent.toAdd.add(Main.oneLevel);
			IDEComponent.toAdd.add(Main.returnBase);
			IDEComponent.toAdd.add(Main.newFile);
			IDEComponent.toAdd.add(Main.newFolder);
			IDEComponent.toAdd.add(Main.reload);
    	} catch (NullPointerException e) {
    		return;
    	}
    }*/
    
    public static void writeFile(File setFile) {
    	try {
			BufferedWriter wr = new BufferedWriter(new FileWriter(setFile));
			
			/*wr.write((fntnr.equals("/font.png")) ? "default\n" : fntnr + "\n");
			wr.write((fntbl.equals("/bold.png")) ? "default\n" : fntbl + "\n");
			wr.write((sprsh.equals("/spritesheet.png")) ? "default\n" : sprsh + "\n");*/
			wr.write((baseFolder != null ? baseFolder.getPath() : "none") + "\n");
			wr.write(conffile + "\n");
			wr.write(CodeEditor.tabs.indexOf(CodeEditor.editing) + "\n");
			wr.write(CodeEditor.scrX + "\n");
			wr.write(CodeEditor.scrY + "\n");
			wr.write(CodeEditor.tabScr + "\n");
			
			if (CodeEditor.tabs.size() > 0) {
				for (int i = 0; i < CodeEditor.tabs.size(); i++) {
					Tab t = CodeEditor.tabs.get(i);
					
					String s = t.getRegent().getRegent().getAbsolutePath().charAt(0) < 10 ? t.getRegent().getRegent().getAbsolutePath().substring(1) : t.getRegent().getRegent().getAbsolutePath();
					
					wr.write(s + "\n");
				}
			}
			
			wr.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    
    public static void readFile(File setFile) {
    	try {
	    	Path p = setFile.toPath();
	    	
	    	try {
				List<String> lines = new ArrayList<>();
				
				try {
					lines = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
				}
				catch (Exception e) {
					lines = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
				}
				
				for (int i = 0; i < lines.size(); i++) {
					String s = lines.get(i);
					
					/*if (i == 0)
						fntnr = (fntnr.equals("default")) ? s : "/font.png";
					else if (i == 1)
						fntbl = (fntbl.equals("default")) ? s : "/bold.png";
					else if (i == 2)
						sprsh = (sprsh.equals("default")) ? s : "/spritesheet.png";*/
					if (i == 0) {
						Fonts.initFonts(fntnr, fntbl);
				        spritesheet = new Spritesheet(sprsh);
						
						baseFolder = new File(s);
						
						if (Explorer.files.size() == 0) {
							int index = 0;
							
							for (File f : Main.baseFolder.listFiles()) {
								Explorer.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
									
								index++;
							}
						}
					}
					else if (i == 1) {
						conffile = s;
						
						if (!conffile.equals("none"))
							hasConfigFile = true;
					}
					
					else if (i == 2)
						tabindex = Integer.parseInt(s);
					
					else if (i == 3)
						CodeEditor.scrX = Integer.parseInt(s);
					
					else if (i == 4)
						CodeEditor.scrY = Integer.parseInt(s);
					
					else if (i == 5)
						CodeEditor.tabScr = Integer.parseInt(s);
					
					if (i > 5) {
						if (!ListableFile.isPath(s)) continue;
						
						File reg = new File(s);
						File par = reg.getParentFile();
						
						if (!reg.exists()) continue;
						
						CodeEditor.tabs.add(new Tab((i - 4) * Tab.WIDTH, ListableFile.search(reg, par))); // 12/05/2021 - 16:17
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	if (CodeEditor.tabs.size() > 0) {
				CodeEditor.editing = CodeEditor.tabs.get(tabindex);
	    	
		    	try {
					CodeEditor.lines = CodeEditor.readFile(CodeEditor.tabs.get(tabindex).getRegent().getRegent());
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
	    	
			IDEComponent.toAdd.add(Main.oneLevel);
			IDEComponent.toAdd.add(Main.returnBase);
			IDEComponent.toAdd.add(Main.newFile);
			IDEComponent.toAdd.add(Main.newFolder);
			IDEComponent.toAdd.add(Main.reload);
    	} catch (Exception e) {}
    }

    public synchronized void start() {
        running = true;

        t = new Thread(this);
        t.start();
    }
    
    public static boolean hasUserInteraction() {
    	return KeyInput.isKeyPressed() | KeyInput.isControlDown() | KeyInput.isShiftDown() |
    		   KeyInput.isAltDown() | KeyInput.isAltGrDown() | MouseInput.mouseMoved() |
    		   MouseInput.isMousePressed() | MouseInput.isMouseClicked() | MouseInput.isMouseDragged() |
    		   WindowInput.isActivated() | ComponentInput.windowMoved() | ComponentInput.windowResized() |
    		   WindowInput.isActivated() | CommandTerminal.active | SetFileName.added;
    }

    @Override
    public void tick() {
        for (IDEComponent c : IDEComponent.components)
            c.tick();
        
        for (ListableFile f : Explorer.files)
        	f.tick();
        
        MouseInput.updateMouse();
        ComponentInput.update();
        
        IDEComponent.components.removeAll(IDEComponent.toRemove);
        IDEComponent.toRemove.clear();
        
        IDEComponent.components.addAll(IDEComponent.toAdd);
        IDEComponent.toAdd.clear();
        
        Explorer.files.removeAll(Explorer.toRemove);
        Explorer.toRemove.clear();
        
        IDEComponent.components = CodeEditor.removeAllDuplicates(IDEComponent.components);
        
        if (!ListableFile.files.isEmpty())
        	Explorer.files = new ArrayList<>(ListableFile.files);
    }

    public void render() {
        BufferStrategy bs = screen.getBufferStrategy();

        if (bs == null) {
            screen.createBufferStrategy(3);
            return;
        }

        Graphics g = screen.layer.getGraphics();
        
        Graphics2D g2 = (Graphics2D) g;

        g.setColor(Colors.background);
        g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);

        for (IDEComponent c : IDEComponent.components)
            c.render(g);
        
        for (ListableFile f : Explorer.files)
        	f.render(g);
        
        for (Tab t : CodeEditor.tabs) {
			if (t.hovered() && CodeEditor.editing == t && t.getX() + CodeEditor.tabScr >= editor.getX() && !t.button.hovered()) { // por algum motivo È + e n„o -
				int index = t.getRegent().getRegent().getPath().contains(Main.baseFolder.getName()) ? t.getRegent().getRegent().getPath().indexOf(Main.baseFolder.getName()) : 0;
				
				int width = 10 + t.getRegent().getRegent().getPath().substring(index).length() * 15;
				int height = CodeEditor.linesWithErrors.size() == 0 ? 70 : 50 + (CodeEditor.linesWithErrors.size() * 40);
				
				/*if (!CodeEditor.syntaxErrorsOn)
					height = 70;*/
				
				/*if (CodeEditor.syntaxErrorsOn) {
					if (CodeEditor.linesWithErrors.size() > 0)
						width = 700;
					else
						width = 500;
					
					if (t.getRegent().getRegent().getPath().substring(index).length() > "N„o foram encontrados erros de sintaxe.".length())
						width = 20 + t.getRegent().getRegent().getPath().substring(index).length() * 12;
				}
				else {
					width = 650;
					height = 100;
					
					if (t.getRegent().getRegent().getPath().substring(index).length() > "Foram encontrados erros de sintaxe nas seguintes linhas:".length())
						width = 20 + t.getRegent().getRegent().getPath().substring(index).length() * 12;
				}
				
				g.setColor(Colors.explorerLight);
				g.fillRect(MouseInput.getMouseX() + 10, MouseInput.getMouseY(), width, height);
				
				g.setColor(Colors.textLighter);
				g2.setStroke(new BasicStroke(2f));
				g2.drawRect(MouseInput.getMouseX() + 10, MouseInput.getMouseY(), width, height);
				
				Fonts.drawString(t.getRegent().getRegent().getPath().substring(index), MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 10, new IDEFont(Fonts.lightGrayNormal, 16), g2);
			
				if (CodeEditor.syntaxErrorsOn) {
					CodeEditor.syntaxErrorsOn = false; // quando for mudar os erros de sintaxe, desative isso
					
					if (CodeEditor.linesWithErrors.size() == 0)
						Fonts.drawString("N„o foram encontrados erros de sintaxe.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					else {
						Fonts.drawString("Foram encontrados erros de sintaxe nas seguintes linhas:", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.errorNormal, 16), g2);
						
						int count = 0;
						
						for (Integer i : CodeEditor.linesWithErrors) {
							Fonts.drawString(new Integer(i + 1).toString(), MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 65 + count * 16, new IDEFont(Fonts.errorNormal, 16), g2);
						
							count++;
						}
					}
				}
				else*/
				
				width = 20 + t.getRegent().getRegent().getPath().substring(index).length() * 12;
				height = 100;
				
				if (!hasConfigFile) {
					if (width < 600)
						width = 600;
				}
				else {
					if (width < 435)
						width = 435;
				}
				
				if (CodeEditor.editing.isReadOnly)
					height = 130;
				
				g.setColor(Colors.explorerLight);
				g.fillRect(MouseInput.getMouseX() + 10, MouseInput.getMouseY(), width, height);
				
				g.setColor(Colors.textLighter);
				g2.setStroke(new BasicStroke(2f));
				g2.drawRect(MouseInput.getMouseX() + 10, MouseInput.getMouseY(), width, height);
				
				Fonts.drawString(t.getRegent().getRegent().getPath().substring(index), MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 10, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				
				if (!hasConfigFile)
					Fonts.drawString("N„o h· nenhum Arquivo de ConfiguraÁıes carregado.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				else
					Fonts.drawString("Arquivo de ConfiguraÁıes carregado.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				
				if (CodeEditor.codeHintsOn)
					Fonts.drawString("Os CodeHints est„o ativados.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				else
					Fonts.drawString("Os CodeHints est„o desativados.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
			
				if (CodeEditor.editing.isReadOnly)
					Fonts.drawString("Esse arquivo est· como somente leitura.", MouseInput.getMouseX() + 20, MouseInput.getMouseY() + 100, new IDEFont(Fonts.lightGrayNormal, 16), g2);
			}
		}
        
        g.dispose();
        g = bs.getDrawGraphics();

        g.drawImage(screen.layer, 0, 0, Screen.WIDTH, Screen.HEIGHT, null);

        bs.show();
    }

    @Override
    public void run() {
        while (running) {        	
        	if (hasUserInteraction()) {
		        tick();
		        render();
            }
        	
        	if (WindowInput.isClosing()) {
	    		if (CodeEditor.editing != null)
	    			CodeEditor.editing.save();
	    		
	    		writeFile(settingsFile);
	    	}
        	
            try {
				Thread.sleep(1000/120); // 120
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}
