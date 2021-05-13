/**
 * Boot IDE
 * 
 * Actual Version: Release 1.2
 * 
 * Changelog:
 * 
 * 1.0
 * 
 * - Release
 * 
 * 1.1
 * 
 * - Adicionada opção "Limpar linha".
 * - Adicionado suporte para a linguagem Lua.
 * - Terminado coloração de comentários de uma linha só pra todas as linguagens.
 * - Ainda não há coloração para comentários multi-linha
 * 
 * 1.2
 * 
 * - Corrigido Bugs:
 *  * Alternar para outra guia sem salvar o arquivo o corrompe;
 *  * Deletar o arquivo e a sua guia correspondente ficar aberta;
 *  * <Ainda não corrigido> Clicar em um arquivo que já tem guia aberta e cria de novo outra guia com o mesmo arquivo. // Esse pode para corrigir mais pra frente pois ele não é crítico.
 *  
 *  - Adicionado suporte para a linguagem SQL.
 */

package ide.main;

import java.awt.Graphics;
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
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
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
    
    public static String[] args;
    
    public static final File settingsFile = new File(System.getProperty("user.dir") + "\\settings.conf");
    
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

        
        IDEComponent.components.add(explorer);
        IDEComponent.components.add(editor);
        
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
			
			wr.write((fntnr.equals("/font.png")) ? "default\n" : fntnr + "\n");
			wr.write((fntbl.equals("/bold.png")) ? "default\n" : fntbl + "\n");
			wr.write((sprsh.equals("/spritesheet.png")) ? "default\n" : sprsh + "\n");
			wr.write(baseFolder.getPath() + "\n");
			wr.write(conffile + "\n");
			wr.write(CodeEditor.tabs.indexOf(CodeEditor.editing) + "\n");
			wr.write(CodeEditor.scrX + "\n");
			wr.write(CodeEditor.scrY + "\n");
			wr.write(CodeEditor.tabScr + "\n");
			wr.write(CommandTerminal.expOff + "\n");
			
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
				
				if (i == 0)
					fntnr = (fntnr.equals("default")) ? s : "/font.png";
				else if (i == 1)
					fntbl = (fntbl.equals("default")) ? s : "/bold.png";
				else if (i == 2)
					sprsh = (sprsh.equals("default")) ? s : "/spritesheet.png";
				else if (i == 3) {
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
				else if (i == 4)
					conffile = s;
				
				else if (i == 5)
					tabindex = Integer.parseInt(s);
				
				else if (i == 6)
					CodeEditor.scrX = Integer.parseInt(s);
				
				else if (i == 7)
					CodeEditor.scrY = Integer.parseInt(s);
				
				else if (i == 8)
					CodeEditor.tabScr = Integer.parseInt(s);
				
				else if (i == 9) {
					CommandTerminal.expOff = Boolean.parseBoolean(s);
					
					if (CommandTerminal.expOff == true) {
						System.out.println("a");
						CommandTerminal.runCommand("toggleexplorer");
					}
				}
				
				if (i > 9) {
					if (!ListableFile.isPath(s)) continue;
					
					File reg = new File(s);
					File par = reg.getParentFile();
					
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
    }

    public synchronized void start() {
        running = true;

        t = new Thread(this);
        t.start();
    }

    @Override
    public void tick() {
    	if (WindowInput.isClosing())
    		writeFile(settingsFile);
    	
        for (IDEComponent c : IDEComponent.components)
            c.tick();
        
        for (ListableFile f : Explorer.files)
        	f.tick();
        
        MouseInput.updateMouse();
        
        IDEComponent.components.removeAll(IDEComponent.toRemove);
        IDEComponent.toRemove.clear();
        
        IDEComponent.components.addAll(IDEComponent.toAdd);
        IDEComponent.toAdd.clear();
        
        Explorer.files.removeAll(Explorer.toRemove);
        Explorer.toRemove.clear();
        
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

        g.setColor(Colors.background);
        g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);

        for (IDEComponent c : IDEComponent.components)
            c.render(g);
        
        for (ListableFile f : Explorer.files)
        	f.render(g);
        
        g.dispose();
        g = bs.getDrawGraphics();

        g.drawImage(screen.layer, 0, 0, Screen.WIDTH, Screen.HEIGHT, null);

        bs.show();
    }

    @Override
    public void run() {
        while (running) {
            tick();
            render();
            
            try {
				Thread.sleep(1000/120);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }
}
