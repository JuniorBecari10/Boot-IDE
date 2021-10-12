package ide.main;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.Logo;
import ide.components.NewFileButton;
import ide.components.NewFolderButton;
import ide.components.OneFolderUpButton;
import ide.components.OpenBaseFolderButton;
import ide.components.ReloadButton;
import ide.components.RenameFile;
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
import ide.util.Language;
import ide.util.Spritesheet;
import ide.util.Texts;
import ide.util.Tickable;

public class Main implements Runnable, Tickable {

    private boolean running = false;

    public static Screen screen;
    public static Toolkit toolkit;

    private Thread t;

    public static Explorer explorer;
    public static CodeEditor editor;
    
    public static Spritesheet spritesheet;
    public static Spritesheet icons;
    
    public static Logo logo;
    
    public static OpenBaseFolderButton openBase;
    
    public static OneFolderUpButton oneFolder;
    public static ReturnToBaseFolderButton returnBase;
    public static NewFileButton newFile;
    public static NewFolderButton newFolder;
    public static ReloadButton reload;
    
    public static File baseFolder;
    
    public static String sprsh = "/spritesheet.png";
    public static String fntnr = "/font.png";
    public static String fntbl = "/bold.png";
    public static String conffile = "none";
    public static String iconsfile = "/autocomplete-icons.png";
    
    private static int tabindex = -1;
    
    public static Desktop desktop;
    public static String[] args;
    
    public static boolean hasConfigFile = false;
    public static Language lang;
    
    public static final File settingsFile = new File(System.getProperty("user.dir") + "\\settings.conf"); // 08/05/2021 - 15:48
    
    public static final String VERSION = "v4.1";
    
    // Sprites
    
    public static BufferedImage baseFolderSpr;
    
    public static BufferedImage newFileSpr;
    public static BufferedImage newFolderSpr;
    public static BufferedImage folderUp;
    public static BufferedImage backBaseFolder;
    public static BufferedImage reloadSpr;
    
    public static BufferedImage folder;
    public static BufferedImage star;
    
    public static BufferedImage closeTab;
    public static BufferedImage notSelectedCloseTab;
    
    public static BufferedImage notSavedTab;
    public static BufferedImage notSelectedNotSavedTab;
    
    public static BufferedImage lock;
    
    ///
    
    // TODO verificar se o args 0 contém boot ou ide e pegar o args 1 e fazer o abrir com
    
    public Main() {
    	if (args == null)
    		args = new String[1];
    	
        toolkit = Toolkit.getDefaultToolkit();
        screen = new Screen("Boot IDE");
        
        lang = Language.ENG; // default
        
        //Fonts.initFonts(fntnr, fntbl);
        
        spritesheet = new Spritesheet(sprsh);
        icons = new Spritesheet(iconsfile);
        
        ///////
        
        baseFolderSpr = spritesheet.getSprite(0, 0, 16, 16);
        
        newFileSpr = spritesheet.getSprite(96, 0, 16, 16);
        newFolderSpr = spritesheet.getSprite(112, 0, 16, 16);
        folderUp = spritesheet.getSprite(64, 0, 16, 16);
        backBaseFolder = spritesheet.getSprite(80, 0, 16, 16);
        reloadSpr = spritesheet.getSprite(128, 0, 16, 16);
        
        folder = spritesheet.getSprite(48, 0, 16, 16);
        star = spritesheet.getSprite(32, 0, 16, 16);
        
        closeTab = spritesheet.getSprite(16, 0, 5, 5);
        notSelectedCloseTab = spritesheet.getSprite(16, 0, 5, 5);
        
        notSavedTab = spritesheet.getSprite(16, 5, 5, 5);
        notSelectedNotSavedTab = spritesheet.getSprite(16, 5, 5, 5);
        
        lock = spritesheet.getSprite(16, 10, 5, 5);
        
        ///////
        
        explorer = new Explorer(0, 0, 280, Screen.HEIGHT);
        editor = new CodeEditor(280, 0, Screen.WIDTH - 280, Screen.HEIGHT); // esses 2 precisa ser inicializados depois das fontes e da spritesheet
        
        logo = new Logo(Screen.WIDTH / 2 + 80, Screen.HEIGHT / 2 - 120, 160, 160, star);
        
        screen.setFrameIcon(spritesheet.getSprite(144, 0, 16, 16));
        
        openBase = new OpenBaseFolderButton(20, 70, 48, 48, baseFolderSpr);
        oneFolder = new OneFolderUpButton(160, 85, 32, 32, folderUp);
        returnBase = new ReturnToBaseFolderButton(200, 85, 32, 32, backBaseFolder);
        newFile = new NewFileButton(80, 85, 32, 32, newFileSpr);
        newFolder = new NewFolderButton(120, 85, 32, 32, newFolderSpr);
        reload = new ReloadButton(240, 85, 32, 32, reloadSpr);
        
        desktop = Desktop.getDesktop();
        
        IDEComponent.components.add(editor);
        IDEComponent.components.add(explorer);
        
        IDEComponent.components.add(logo);
        
        IDEComponent.components.add(openBase);
        
        if (settingsFile.exists())
    		readFile(settingsFile);
        
        	load();
        
        /*try {
        	String arg = args[0].toLowerCase().contains("boot") || args[0].toLowerCase().contains("ide") ? args[1] : args[0];
        	
        	openWith(arg);
        } catch (NullPointerException | IndexOutOfBoundsException e) {
        	e.printStackTrace();
        	System.err.println("[PORT] Não há argumentos suficientes! \n [ENG] No enough arguments!");
        }*/
        
        /*ListableFile.readConfigFile(conffile);
        Texts.setTexts(lang);
        
        ////////
        
        baseFolderSpr = Colors.swapColor(baseFolderSpr, Colors.textLightDefault, Colors.textLight);
        
        newFileSpr = Colors.swapColor(newFileSpr, Colors.textLightDefault, Colors.textLight);
        newFolderSpr = Colors.swapColor(newFolderSpr, Colors.textLightDefault, Colors.textLight);
        folderUp = Colors.swapColor(folderUp, Colors.textLightDefault, Colors.textLight);
        backBaseFolder = Colors.swapColor(backBaseFolder, Colors.textLightDefault, Colors.textLight);
        reloadSpr = Colors.swapColor(reloadSpr, Colors.textLightDefault, Colors.textLight);
        
        star = Colors.swapColor(star, Colors.textLightDefault, Colors.textLight);
        folder = Colors.swapColor(folder, Colors.textLightDefault, Colors.textLight);
        
        closeTab = Colors.swapColor(closeTab, Colors.textLightDefault, Colors.textLight);
        notSavedTab = Colors.swapColor(notSavedTab, Colors.textLightDefault, Colors.textLight);
        
        lock = Colors.swapColor(lock, Colors.textLightDefault, Colors.textLight);
        
        ///
        
        CodeEditor.functions = Colors.swapColor(CodeEditor.functions, Colors.textLightDefault, Colors.textLight);
        CodeEditor.objects = Colors.swapColor(CodeEditor.objects, Colors.textLightDefault, Colors.textLight);
        CodeEditor.keywords = Colors.swapColor(CodeEditor.keywords, Colors.textLightDefault, Colors.textLight);
        CodeEditor.variables = Colors.swapColor(CodeEditor.variables, Colors.textLightDefault, Colors.textLight);
        
        ////////
        
        Fonts.initFonts(fntnr, fntbl);*/
        
        IDEComponent.toAdd.add(Main.newFile);
		IDEComponent.toAdd.add(Main.newFolder);
		IDEComponent.toAdd.add(Main.oneFolder);
		IDEComponent.toAdd.add(Main.returnBase);
		IDEComponent.toAdd.add(Main.reload);
    }
    
    public static synchronized void load() {
    	if (hasConfigFile)
			ListableFile.readConfigFile(conffile);
    	
        Texts.setTexts(lang);
        Fonts.initFonts(fntnr, fntbl);
        
        ////////
        
        baseFolderSpr = Colors.swapColor(baseFolderSpr, Colors.textLightDefault, Colors.textLight);
        
        newFileSpr = Colors.swapColor(newFileSpr, Colors.textLightDefault, Colors.textLight);
        newFolderSpr = Colors.swapColor(newFolderSpr, Colors.textLightDefault, Colors.textLight);
        folderUp = Colors.swapColor(folderUp, Colors.textLightDefault, Colors.textLight);
        backBaseFolder = Colors.swapColor(backBaseFolder, Colors.textLightDefault, Colors.textLight);
        reloadSpr = Colors.swapColor(reloadSpr, Colors.textLightDefault, Colors.textLight);
        
        star = Colors.swapColor(star, Colors.textLightDefault, Colors.textLight);
        folder = Colors.swapColor(folder, Colors.textLightDefault, Colors.textLight);
        
        closeTab = Colors.swapColor(closeTab, Colors.textLightDefault, Colors.textLight);
        notSavedTab = Colors.swapColor(notSavedTab, Colors.textLightDefault, Colors.textLight);
        
        lock = Colors.swapColor(lock, Colors.textLightDefault, Colors.textLight);
        
        /// Change some colors ///
        
        final int sub = 30; // subtract
        
        int r = Colors.textLight.getRed();
        int g = Colors.textLight.getGreen();
        int b = Colors.textLight.getBlue();
        
        r -= sub;
        g -= sub;
        b -= sub;
        
        Color esc = new Color(r, g, b);
        
        System.out.println(esc.equals(Colors.textLight));
        
        notSelectedCloseTab = Colors.swapColor(notSelectedCloseTab, Colors.textLightDefault, esc);
        notSelectedNotSavedTab = Colors.swapColor(notSelectedNotSavedTab, Colors.textLightDefault, esc);
        
        ///
        
        ////////
        
        hasConfigFile = true;
    }
    
   /*private void openWith(String locale) {
    	if (locale == null) return;
    	
    	try {
    		File file = new File(locale);
			
			if (Main.baseFolder == null) {
				IDEComponent.toAdd.add(Main.newFile);
				IDEComponent.toAdd.add(Main.newFolder);
				IDEComponent.toAdd.add(Main.oneFolder);
				IDEComponent.toAdd.add(Main.returnBase);
				IDEComponent.toAdd.add(Main.reload);
			}
			
			 ListableFile.files.clear();
			  Explorer.files.clear();
	          
	          		if (file.isDirectory()) {
	          			Main.baseFolder = file;
						
						Explorer.scope = null;
		        	  	
		        	  	int index = 0;
						
						for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
							ListableFile.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
							
							index++;
						}
	          			return;
	          		}
	          
	        	  	Main.baseFolder = file.getParentFile();
	        	  	
	        	  	Explorer.files.clear();
					ListableFile.files.clear();
					
					Explorer.scope = null;
	        	  	
	        	  	int index = 0;
					
					for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
						ListableFile.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
						
						index++;
					}
	          
			int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX() : Tab.MIN_X;
       	
			if (!(file.getName().equalsIgnoreCase(".pdf") || file.getName().equalsIgnoreCase(".jar") || file.getName().equalsIgnoreCase(".iso") || file.getName().equalsIgnoreCase(".img") || file.getName().equalsIgnoreCase(".flp") || file.getName().equalsIgnoreCase(".class") || file.getName().equalsIgnoreCase(".exe") || file.getName().equalsIgnoreCase(".urna") || file.getName().equalsIgnoreCase(".save") || file.getName().equalsIgnoreCase(".docx") || file.getName().equalsIgnoreCase(".pptx") || file.getName().equalsIgnoreCase(".one") || file.getName().equalsIgnoreCase(".psd") || file.getName().equalsIgnoreCase(".aed") || file.getName().equalsIgnoreCase(".ai") || file.getName().equalsIgnoreCase(".indd") || file.getName().equalsIgnoreCase(".ini") || file.getName().equalsIgnoreCase(".dll") || file.getName().equalsIgnoreCase(".png") || file.getName().equalsIgnoreCase(".jpg") || file.getName().equalsIgnoreCase(".jpeg") || file.getName().equalsIgnoreCase(".gif") || file.getName().equalsIgnoreCase(".bmp") || file.getName().equalsIgnoreCase(".ico") || file.getName().equalsIgnoreCase(".webp") || file.getName().equalsIgnoreCase(".mp4") || file.getName().equalsIgnoreCase(".wmv") || file.getName().equalsIgnoreCase(".avi") || file.getName().equalsIgnoreCase(".wav") || file.getName().equalsIgnoreCase(".mp3") || file.getName().equalsIgnoreCase(".ogg") || file.getName().equalsIgnoreCase(".otf") || file.getName().equalsIgnoreCase(".ttf") || file.getName().equalsIgnoreCase(".woff") || file.getName().equalsIgnoreCase(".woff2") || file.getName().equalsIgnoreCase(".zip") || file.getName().equalsIgnoreCase(".rar") || file.getName().equalsIgnoreCase(".7z") || file.getName().equalsIgnoreCase(".bin"))) {
	        	Tab toAdd = new Tab(Main.editor.tabs.size() > 0 ? (lastX + Tab.WIDTH) + 3 : Tab.MIN_X - Tab.WIDTH, ListableFile.searchListableFiles(file));
	        	
 				Main.editor.cursorX = 0;
 				Main.editor.cursorY = 1;
 				
 				Main.editor.scrX = 0;
 				Main.editor.scrY = 0;
 				
	        	  	Main.editor.editing = toAdd;
	        	  	Main.editor.tabs.add(toAdd);
					
	        	  	new Thread() {
						public void run() {
							try {
								Main.editor.lines = Main.editor.readFile(file);
							} catch (IOException e) { // não suportado, se caiu aqui
								return;
							}
						}
					}.start();
	        	  	
					Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
			}
			
    	} catch (NullPointerException e) {
    		return;
    	}
    }*/
    
    public static void writeFile(File setFile) {
    	BufferedWriter wr = null;
    	
    	try {
			wr = new BufferedWriter(new FileWriter(setFile));
			
			wr.write((baseFolder != null ? baseFolder.getPath() : "none") + "\n");
			wr.write(conffile + "\n");
			wr.write(Main.editor.tabs.indexOf(Main.editor.editing) + "\n");
			wr.write(Main.editor.scrX + "\n");
			wr.write(Main.editor.scrY + "\n");
			wr.write(Main.editor.tabScr + "\n");
			wr.write(Main.explorer.getWidth() + "\n");
			
			if (Main.editor.tabs.size() > 0) {
				for (int i = 0; i < Main.editor.tabs.size(); i++) {
					Tab t = Main.editor.tabs.get(i);
					
					String s = t.getRegent().getRegent().getAbsolutePath().charAt(0) < 10 ? t.getRegent().getRegent().getAbsolutePath().substring(1) : t.getRegent().getRegent().getAbsolutePath();
					
					wr.write(s + "\n");
				}
			}
			
			wr.flush();
			wr.close();
			
		} catch (IOException e) {
			//e.printStackTrace();
			
			try {
				wr.flush();
				wr.close();
			} catch (IOException f) {
				f.printStackTrace();
			}
		}
    }
    
    // o settings file tá
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
					
					if (i == 0) {
						Fonts.initFonts(fntnr, fntbl);
				        spritesheet = new Spritesheet(sprsh); // fazer o closebasefolder não descarregar o config file
						
				        if (s.equals("none")) baseFolder = null;
				        else {
				        	baseFolder = new File(s);
				        	Explorer.baseFolderName = baseFolder.getName();
				        }
						
						if (Explorer.files.size() == 0) {
							int index = 0;
							
							for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
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
						Main.editor.scrX = Integer.parseInt(s);
					
					else if (i == 4)
						Main.editor.scrY = Integer.parseInt(s);
					
					else if (i == 5)
						Main.editor.tabScr = Integer.parseInt(s);
					
					else if (i == 6) {
						Main.explorer.setDrag(Integer.parseInt(s));
						
						load();
					}
					
					if (i > 6) {
						//if (!ListableFile.isPath(s)) continue;
						
						File reg = new File(s);
						File par = reg.getParentFile();
						
						//if (!reg.exists()) continue;
						
						Tab t = new Tab((i - 4) * Tab.WIDTH, ListableFile.search(reg, par));
						
						Main.editor.tabs.add(t);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	if (Main.editor.tabs.size() > 0) {
				Main.editor.editing = Main.editor.tabs.get(tabindex);
	    	
		    	try {
					Main.editor.lines = Main.editor.readFile(Main.editor.tabs.get(tabindex).getRegent().getRegent());
				} catch (IOException e) {
					e.printStackTrace();
				}
	    	}
    	} catch (Exception e) {}
    }
    
    /*public static boolean hasUserInteraction() {
    	return KeyInput.isKeyPressed() || MouseInput.mouseMoved() ||
    		   MouseInput.isMousePressed() || MouseInput.isMouseClicked() || MouseInput.isMouseDragged() ||
    		   WindowInput.isActivated() || ComponentInput.windowMoved() || ComponentInput.windowResized();
    }*/

    public synchronized void start() {
        running = true;

        t = new Thread(this, "Main-Thread");
        t.start();
    }

    @Override
    public void tick() {
    	for (IDEComponent c : IDEComponent.components)
            c.tick();
        
        KeyInput.updateKeys();
        MouseInput.updateMouse();
        ComponentInput.update();
        
        IDEComponent.components.removeAll(IDEComponent.toRemove);
        IDEComponent.toRemove.clear();
        
        IDEComponent.components.addAll(IDEComponent.toAdd);
        IDEComponent.toAdd.clear();
        
        Explorer.files.removeAll(Explorer.toRemove);
        Explorer.toRemove.clear();
        
        IDEComponent.components = CodeEditor.removeDuplicates(IDEComponent.components);
        
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
        
        if (!(CommandTerminal.active || SetFileName.added || RenameFile.added))
	        for (Tab t : Main.editor.tabs) {
				if (t.hovered() && Main.editor.editing == t && t.getX() + Main.editor.tabScr >= editor.getX() && !t.button.hovered()) { // por algum motivo é + e não -
					int index = t.getRegent().getRegent().getPath().contains(Main.baseFolder.getName()) ? t.getRegent().getRegent().getPath().indexOf(Main.baseFolder.getName()) : 0;
					
					int width = 20 + t.getRegent().getRegent().getPath().substring(index).length() * 12;
					int height = 100;
					
					int x = MouseInput.getMouseX() + 10;
					int y = MouseInput.getMouseY() + 10;
					
					if (!hasConfigFile) {
						if (lang == Language.PORT)
							if (width < 600)
								width = 600;
						
						if (lang == Language.ENG)
							if (width < 470)
								width = 470;
					}
					else if (Main.editor.editing.isReadOnly) {
						if (lang == Language.PORT)
							if (width < 480)
								width = 480;
						if (lang == Language.ENG)
							if (width < 360)
								width = 360;
					}
					else {
						if (lang == Language.PORT)
							if (width < 435)
								width = 435;
						if (lang == Language.ENG)
							if (width < 360)
								width = 360;
					}
					
					if (Main.editor.editing.isReadOnly)
						height = 130;
					
					Rectangle intersection = new Rectangle(x, y, width, height).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
					
					if (!intersection.isEmpty()) {
						x -= intersection.getWidth();
					}
					
					g.setColor(Colors.explorerLight);
					g.fillRect(x, MouseInput.getMouseY(), width, height);
					
					g.setColor(Colors.textLighter);
					g2.setStroke(new BasicStroke(2f));
					g2.drawRect(x, MouseInput.getMouseY(), width, height);
					
					Fonts.drawString(t.getRegent().getRegent().getPath().substring(index), (x - 10) + 20, (y - 10) + 10, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					
					if (!hasConfigFile)
						Fonts.drawString(Texts.noConfigFileLoaded, (x - 10) + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					else
						Fonts.drawString(Texts.configFileLoaded, (x - 10) + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					
					if (Main.editor.codeHelpersOn)
						Fonts.drawString(Texts.codeHelpersOn, (x - 10) + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					else
						Fonts.drawString(Texts.codeHelpersOff, (x - 10) + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				
					if (Main.editor.editing != null && Main.editor.editing.isReadOnly)
						Fonts.drawString(Texts.fileAsReadOnly, (x - 10) + 20, (y - 10)+ 100, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				}
	        }
        
        if (explorer.hovered() && !CommandTerminal.expOff) {
        	if (MouseInput.hovered(explorer.getX() + 10, 140, explorer.getWidth() - 10, 23) && Explorer.showBaseFolderCard && !(SetFileName.added || CommandTerminal.active || RenameFile.added)) {
        		int xdr = MouseInput.getMouseX() + 10;
    			int ydr = MouseInput.getMouseY() - 10;
    			
    			int wdr = 15 + Main.baseFolder.getName().length() * 12;
    			final int hdr = 70;
    			
    			if (wdr < 165) wdr = 165;
    			
    			Rectangle intersection = new Rectangle(xdr, ydr, wdr, hdr).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
    			
    			if (!intersection.isEmpty())
    				xdr -= intersection.getWidth();
    			
    			g.setColor(Colors.explorerLight);
    			g.fillRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			g.setColor(Colors.textLighter);
    			g2.setStroke(new BasicStroke(2f));
    			g2.drawRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			Fonts.drawString(Texts.baseFolder_, xdr + 10, ydr + 10, new IDEFont(Fonts.lighterGrayNormal, 16), g);
    			Fonts.drawString(Main.baseFolder.getName(), xdr + 10, ydr + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
        	}
        	
        	if (MouseInput.hovered(explorer.getX() + 10, 170, explorer.getWidth() - 10, 23) && Explorer.showFolderPathCard && !(SetFileName.added || CommandTerminal.active || RenameFile.added)) {
        		String scopeStr = Explorer.getScopePath().contains(baseFolder.getName()) ? Explorer.getScopePath().substring(Explorer.getScopePath().indexOf(baseFolder.getName())) : Explorer.getScopePath();
        		
        		int xdr = MouseInput.getMouseX() + 10;
    			int ydr = MouseInput.getMouseY() - 10;
    			
    			final int wdr = 15 + scopeStr.length() * 12;
    			final int hdr = 70;
    			
    			Rectangle intersection = new Rectangle(xdr, ydr, wdr, hdr).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
    			
    			if (!intersection.isEmpty())
    				xdr -= intersection.getWidth();
    			
    			g.setColor(Colors.explorerLight);
    			g.fillRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			g.setColor(Colors.textLighter);
    			g2.setStroke(new BasicStroke(2f));
    			g2.drawRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			Fonts.drawString(Texts.actualFolder_, xdr + 10, ydr + 10, new IDEFont(Fonts.lighterGrayNormal, 16), g);
    			Fonts.drawString(scopeStr, xdr + 10, ydr + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
        	}
        }
        
        if (Explorer.dragging) {
        	int x = MouseInput.getMouseX() + 35;
        	int y = MouseInput.getMouseY();
        	int w = explorer.getWidth() < 1000 ? 83 : 97;
        	int h = 28;
        	
        	Rectangle intr = new Rectangle(x, y, w, h).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
        	
        	if (!intr.isEmpty())
        		x -= intr.getWidth();
        	
        	g.setColor(new Color(0, 0, 0, 0.3f));
			g.fillRect(x, y, w, h);
			
			Fonts.drawString(explorer.getWidth() + "px", x + 5, y, new IDEFont(Fonts.lightGrayNormal, 20), g);
        }
        
        g.dispose();
        g = bs.getDrawGraphics();

        g.drawImage(screen.layer, 0, 0, Screen.WIDTH, Screen.HEIGHT, null);

        bs.show();
    }

   /* @Override
    public void run() {
        while (running) {   	
        	if (hasUserInteraction()) {
        		tick();
        		render();
        	}
        	
        	closing:
	        	if (WindowInput.isClosing()) {
	        		writeFile(settingsFile);
	        		
		    		if (Main.editor.editing != null) { // não for nulo
		    			if (!Main.editor.editing.isSaved()) { // não estiver salvo
		    				String[] options = { Texts.yes, Texts.no, Texts.cancel };
		    				
		    				Main.editor.setSystemLook();
		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		    				
		    				if (selectedOption == 0) Main.editor.editing.save();
		    				else if (selectedOption == 2) {
		    					WindowInput.update();
		    					
		    					break closing;
		    				}
		    			}
		    		}
		    		
		    		System.exit(0);
		    	}
        	
            try {
				Thread.sleep(1000/260);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
        }
    }*/
    
    /*@Override
    public void run() {
    	while (running) {
    		if (WindowInput.isActivated() && ((MouseInput.mouseMoved() || KeyInput.isKeyPressed()) || (ComponentInput.windowMoved() || ComponentInput.windowResized()))) {
            	tick();
            	render();
        	
        	closing:
	        	if (WindowInput.isClosing()) {
	        		writeFile(settingsFile);
	        		
		    		if (Main.editor.editing != null) { // não for nulo
		    			if (!Main.editor.editing.isSaved()) { // não estiver salvo
		    				String[] options = { Texts.yes, Texts.no, Texts.cancel };
		    				
		    				Main.editor.setSystemLook();
		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		    				
		    				if (selectedOption == 0) Main.editor.editing.save();
		    				else if (selectedOption == 2) {
		    					WindowInput.update();
		    					
		    					break closing;
		    				}
		    			}
		    		}
		    		
		    		System.exit(0);
		    	}
			}
    		
    		try {
    			Thread.sleep(1000/100);
    		} catch (InterruptedException e) {
    			e.printStackTrace();
    		}
    	}
    }*/
    
    @Override
    public void run() {
    	screen.requestFocus();
    	
    	long lastTime = System.nanoTime(); // Release v3.9.1 - 14/08/2021 - 14:51
    	double targetFps = 120.0; // 60
    	double ns = 1E9 / targetFps;
    	double delta = 0;
    	
    	//boolean reachedFps = false;
    	
    	int frames = 0;
    	double timer = System.currentTimeMillis();
    	
    	//int tickOverflow = 0;
    	
    	while (running) {
    		long now = System.nanoTime();
    		
    		delta += (now - lastTime) / ns;
    		lastTime = now;
    		
    		if (delta >= 1) {
    			if (frames < targetFps) { // ver isso aqui
			        tick();
			        render(); // o problema é o render
    			} /*else {
    				tickOverflow++;
    			}*/
    			
            	closing:
    	        	if (WindowInput.isClosing()) {
    	        		writeFile(settingsFile);
    	        		
    		    		if (Main.editor.editing != null) { // não for nulo
    		    			if (!Main.editor.editing.isSaved()) { // não estiver salvo
    		    				String[] options = { Texts.save, Texts.dont + " " + Texts.save, Texts.cancel };
    		    				
    		    				CodeEditor.setSystemLook();
    		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    		    				
    		    				if (selectedOption == 0) Main.editor.editing.save();
    		    				else if (selectedOption == 2) {
    		    					WindowInput.update();
    		    					
    		    					break closing;
    		    				}
    		    			}
    		    		}
    		    		
    		    		System.exit(0);
    		    	}
    			
    			delta--;
    			frames++;
    		}
    		
    		if (System.currentTimeMillis() - timer >= 1000) {
    			/*if (tickOverflow > 0)
    				System.out.println("Tick Overflow: Skipping " + tickOverflow + " ticks.");*/
    			
    			System.out.println("FPS: " + frames);
    			
    			frames = 0;
    			timer += 1000;
    		}
    	}
    }
}
