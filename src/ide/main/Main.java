package ide.main;

import java.awt.BasicStroke;
import java.awt.Desktop;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
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

import javax.swing.JOptionPane;

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
    
    public static Language lang;
    
    public static final File settingsFile = new File(System.getProperty("user.dir") + "\\settings.conf"); // 08/05/2021 - 15:48
    
    public Main() {
    	if (args == null)
    		args = new String[10];
    	
        toolkit = Toolkit.getDefaultToolkit();
        screen = new Screen("Boot IDE");
        
        lang = Language.ENG; // default
        
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
        
        //System.out.println(args[0]);
        
        //openWith();
        
        ListableFile.readConfigFile(conffile);
        Texts.setTexts(lang);
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
    	BufferedWriter wr = null;
    	
    	try {
			wr = new BufferedWriter(new FileWriter(setFile));
			
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
    
    /*public static boolean hasUserInteraction() {
    	return KeyInput.isKeyPressed() || MouseInput.mouseMoved() ||
    		   MouseInput.isMousePressed() || MouseInput.isMouseClicked() || MouseInput.isMouseDragged() ||
    		   WindowInput.isActivated() || ComponentInput.windowMoved() || ComponentInput.windowResized();
    }*/

    public synchronized void start() {
        running = true;

        t = new Thread(this, "Main");
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
        
        if (!(CommandTerminal.active || SetFileName.added || RenameFile.added))
	        for (Tab t : CodeEditor.tabs) {
				if (t.hovered() && CodeEditor.editing == t && t.getX() + CodeEditor.tabScr >= editor.getX() && !t.button.hovered()) { // por algum motivo é + e não -
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
					else if (CodeEditor.editing.isReadOnly) {
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
					
					if (CodeEditor.editing.isReadOnly)
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
					
					if (CodeEditor.codeHelpersOn)
						Fonts.drawString(Texts.codeHelpersOn, (x - 10) + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					else
						Fonts.drawString(Texts.codeHelpersOff, (x - 10) + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				
					if (CodeEditor.editing.isReadOnly)
						Fonts.drawString(Texts.fileAsReadOnly, (x - 10) + 20, (y - 10)+ 100, new IDEFont(Fonts.lightGrayNormal, 16), g2);
				}
	        }
        
        if (explorer.hovered() && !CommandTerminal.expOff) {
        	if (MouseInput.hovered(explorer.getX() + 10, 140, explorer.getWidth() - 10, 23) && Explorer.showBaseFolderCard) {
        		int xdr = MouseInput.getMouseX() + 10;
    			int ydr = MouseInput.getMouseY() - 10;
    			
    			final int wdr = 15 + Main.baseFolder.getName().length() * 12;
    			final int hdr = 70;
    			
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
        	
        	if (MouseInput.hovered(explorer.getX() + 10, 170, explorer.getWidth() - 10, 23) && Explorer.showFolderPathCard) {
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
	        		
		    		if (CodeEditor.editing != null) { // não for nulo
		    			if (!CodeEditor.editing.isSaved()) { // não estiver salvo
		    				String[] options = { Texts.yes, Texts.no, Texts.cancel };
		    				
		    				CodeEditor.setSystemLook();
		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + CodeEditor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		    				
		    				if (selectedOption == 0) CodeEditor.editing.save();
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
	        		
		    		if (CodeEditor.editing != null) { // não for nulo
		    			if (!CodeEditor.editing.isSaved()) { // não estiver salvo
		    				String[] options = { Texts.yes, Texts.no, Texts.cancel };
		    				
		    				CodeEditor.setSystemLook();
		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + CodeEditor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
		    				
		    				if (selectedOption == 0) CodeEditor.editing.save();
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
    	
    	long lastTime = System.nanoTime();
    	double targetFps = 60.0;
    	double ns = 1E9 / targetFps;
    	double delta = 0;
    	
    	int frames = 0;
    	double timer = System.currentTimeMillis();
    	
    	while (running) {
    		long now = System.nanoTime();
    		
    		delta += (now - lastTime) / ns;
    		lastTime = now;
    		
    		if (delta >= 1) {
    			if (WindowInput.isActivated() && ((MouseInput.mouseMoved() || KeyInput.isKeyPressed()) || (ComponentInput.windowMoved() || ComponentInput.windowResized()))) {
	            	tick();
	            	render();
    			}
            	
            	closing:
    	        	if (WindowInput.isClosing()) {
    	        		writeFile(settingsFile);
    	        		
    		    		if (CodeEditor.editing != null) { // não for nulo
    		    			if (!CodeEditor.editing.isSaved()) { // não estiver salvo
    		    				String[] options = { Texts.yes, Texts.no, Texts.cancel };
    		    				
    		    				CodeEditor.setSystemLook();
    		    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + CodeEditor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    		    				
    		    				if (selectedOption == 0) CodeEditor.editing.save();
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
    			System.out.println("FPS: " + frames);
    			
    			frames = 0;
    			timer += 1000;
    		}
    	}
    }
}
