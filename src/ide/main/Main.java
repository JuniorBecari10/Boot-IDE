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
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.FileReadMode;
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
import ide.components.SettingsButton;
import ide.explorer.Explorer;
import ide.explorer.ExplorerMode;
import ide.explorer.ListableFile;
import ide.explorercomponents.SetBranchName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.ComponentInput;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.screen.Screen;
import ide.topcomponents.CloseWindow;
import ide.topcomponents.MaximizeWindow;
import ide.topcomponents.MinimizeWindow;
import ide.topcomponents.TopComponent;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Spritesheet;
import ide.util.Texts;
import ide.util.Tickable;

public class Main implements Runnable, Tickable {
	
	public static final String CONFIG_FILE_EXTENSION = ".conf";
    public static final String SETTINGS_FILE_EXTENSION = ".setconf";
    
    public static final String DEFAULT_CONFIG_FILE_NAME = "config" + CONFIG_FILE_EXTENSION;
    
    public static final String RESOURCE_FOLDER_NAME = "Resources";
    public static final String FONT_FILE_NAME = "Resources/font.png";
    public static final String EDITORFONT_FILE_NAME = "Resources/editorfont.png";
    public static final String SPRITESHEET_FILE_NAME = "Resources/spritesheet.png";
    //public static final String EMOJI_FONT_FILE_NAME = "emojifont.ttf";
    
    public static final String LOG_FILE_NAME = "Exception.log";
    
    public static final String PROGRAM_NAME = "Boot IDE";
    public static final String VERSION = "Beta 3 v4.5";
    
    public static final String userDir = System.getProperty("user.dir");
    
    public static boolean forceMacButtons = false;
    
    public static boolean canRunLoop = true;
    
    public static BufferedImage UNKNOWN_FILE_ICON = null;
	
    private boolean running = false;
    
    public static Screen screen;
    public static Toolkit toolkit;
    
    public static boolean writeFiles = true;

    private Thread t;

    public static Explorer explorer;
    public static CodeEditor editor;
    
    public static Spritesheet spritesheet;
    
    public static Logo logo;
    
    public static OpenBaseFolderButton openBase;
    
    public static OneFolderUpButton oneFolder;
    public static ReturnToBaseFolderButton returnBase;
    public static NewFileButton newFile;
    public static NewFolderButton newFolder;
    public static ReloadButton reload;
    
    public static File baseFolder;
    
    public static CloseWindow closeWindow;
    public static MinimizeWindow minimizeWindow;
    public static MaximizeWindow maximizeWindow;
    
    public static SettingsButton settingsButton;
    
    public static String sprsh = "/spritesheet.png"; // sprsh - spritesheet
    public static String fntnr = "/font.png"; // fntnr - font normal
    public static String fnted = "/editorfont.png"; // fnted - font editor
    public static String conffile = "none"; // conffile - config(uration) file
    
    public static Spritesheet originalSpritesheet;
    
    public static Spritesheet modifiedSpritesheet;
    public static Spritesheet modifiedFontNormal;
    public static Spritesheet modifiedIcons;
    
    public static int inheritCx, inheritCy;
    public static int inheritScrX, inheritScrY;
    
    private static int tabindex = -1;
    
    public static Desktop desktop;
    public static String[] args;
    
    public static boolean alreadyLoaded = false;
    
    public static boolean hasConfigFile = false;
    public static Language lang;
    
    public static OS os;
    
    public static Main main;
    
    public static final File settingsFile = new File(System.getProperty("user.dir") + File.separator + "settings" + SETTINGS_FILE_EXTENSION);
    public static final File defaultConfigFile = new File(System.getProperty("user.dir") + File.separator + DEFAULT_CONFIG_FILE_NAME);
    
    public static final File resourcesFolder = new File(System.getProperty("user.dir") + File.separator + RESOURCE_FOLDER_NAME);
    
    public static final File fontFile = new File(System.getProperty("user.dir") + File.separator + FONT_FILE_NAME);
    public static final File editorFontFile = new File(System.getProperty("user.dir") + File.separator + EDITORFONT_FILE_NAME);
    public static final File spritesheetFile = new File(System.getProperty("user.dir") + File.separator + SPRITESHEET_FILE_NAME);
    
    public static final File logFile = new File(System.getProperty("user.dir") + File.separator + LOG_FILE_NAME);
    
    public static final String[] errorKeywords = { "fatal", "error" };
    public static final String[] warningKeywords = { "warning" };
    
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
    public static BufferedImage back;
    
    public static BufferedImage caseSensitive;
    public static BufferedImage regex;
    
    public static BufferedImage entireDocument;
    public static BufferedImage selectedLines;
    
    public static BufferedImage hexView;
    public static BufferedImage binView;
    
    public static BufferedImage closeWindowSpr;
    public static BufferedImage minimizeWindowSpr;
    public static BufferedImage maximizeWindowSpr;
    public static BufferedImage maximizedWindowSpr;
    
    public static BufferedImage closeWindowMacSpr;
    public static BufferedImage minimizeWindowMacSpr;
    public static BufferedImage maximizeWindowMacSpr;
    
    public static BufferedImage closeWindowHoverSpr;
    public static BufferedImage minimizeWindowHoverSpr;
    public static BufferedImage maximizeWindowHoverSpr;
    
    public static BufferedImage deactivatedMacButtons;
    
    public static BufferedImage explorerTab;
    public static BufferedImage searchReplaceTab;
    public static BufferedImage gitTab;
    public static BufferedImage terminalTab;
    
    public static BufferedImage settingsButtonSpr;
    
    public static BufferedImage gitError;
    public static BufferedImage gitProgress;
    public static BufferedImage gitWarning;
    public static BufferedImage gitDone;
    
    public static BufferedImage branch;
    
    public static BufferedImage createBranchSpr;
    public static BufferedImage checkoutSpr;
    public static BufferedImage renameBranchSpr;
    
    ///
    
    // TODO verificar se o args 0 contém boot ou ide e pegar o args 1 e fazer o abrir com
    
    public Main() {
    	try {
	    	if (args == null)
	    		args = new String[1];
	    	
	    	//System.getProperties().list(System.out);
	    	
	    	os = getOS();
	    	
	    	originalSpritesheet = new Spritesheet(sprsh);
	        modifiedSpritesheet = new Spritesheet(spritesheetFile);
	        spritesheet = spritesheetFile.exists() ? modifiedSpritesheet : originalSpritesheet;
	        
	        if (fontFile.exists())
	        	fntnr = FONT_FILE_NAME;
	        
	        if (editorFontFile.exists())
	        	fnted = EDITORFONT_FILE_NAME;
	        
	        //System.out.println(fntnr + " " + fnted);
	        
	        toolkit = Toolkit.getDefaultToolkit();
	        screen = new Screen(PROGRAM_NAME);
	        
	        MaximizeWindow.maximize();
	        
	        lang = Language.ENG; // default
	        
	        UNKNOWN_FILE_ICON = Main.spritesheet.getSprite(0, 64, 16, 16);
	        
	        //Fonts.initFonts(fntnr, fnted);
	        
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
	        back = spritesheet.getSprite(168, 0, 8, 8);
	        
	        caseSensitive = spritesheet.getSprite(208, 0, 16, 16);
	        regex = spritesheet.getSprite(224, 0, 16, 16);
	        
	        entireDocument = spritesheet.getSprite(240, 0, 16, 16);
	        selectedLines = spritesheet.getSprite(256, 0, 16, 16);
	        
	        hexView = spritesheet.getSprite(24, 0, 8, 8);
	        binView = spritesheet.getSprite(24, 8, 8, 8);
	        
	        closeWindowSpr = spritesheet.getSprite(272, 0, 7, 7);
	        minimizeWindowSpr = spritesheet.getSprite(279, 0, 7, 7);
	        maximizeWindowSpr = spritesheet.getSprite(272, 7, 7, 7);
	        maximizedWindowSpr = spritesheet.getSprite(279, 7, 7, 7);
	        
	        closeWindowMacSpr = spritesheet.getSprite(288, 0, 7, 7);
        	minimizeWindowMacSpr = spritesheet.getSprite(295, 0, 7, 7);
        	maximizeWindowMacSpr = spritesheet.getSprite(288, 7, 7, 7);
        	
        	closeWindowHoverSpr = spritesheet.getSprite(304, 0, 7, 7);
        	minimizeWindowHoverSpr = spritesheet.getSprite(311, 0, 7, 7);
        	maximizeWindowHoverSpr = spritesheet.getSprite(304, 7, 7, 7);
        	
        	deactivatedMacButtons = spritesheet.getSprite(295, 7, 7, 7);
	        
	        explorerTab = spritesheet.getSprite(320, 0, 16, 16);
	        searchReplaceTab = spritesheet.getSprite(336, 0, 16, 16);
	        gitTab = spritesheet.getSprite(352, 0, 16, 16);
	        terminalTab = spritesheet.getSprite(368, 0, 16, 16);
	        
	        settingsButtonSpr = spritesheet.getSprite(384, 0, 16, 16);
	        
	        gitError = spritesheet.getSprite(160, 0, 5, 5);
	        gitProgress = spritesheet.getSprite(160, 5, 5, 5);
	        gitWarning = spritesheet.getSprite(165, 0, 5, 5);
	        gitDone = spritesheet.getSprite(160, 10, 5, 5);
	        
	        branch = spritesheet.getSprite(400, 0, 16, 16);
	        
	        createBranchSpr = spritesheet.getSprite(416, 0, 16, 16);
	        checkoutSpr = spritesheet.getSprite(432, 0, 16, 16);
	        renameBranchSpr = spritesheet.getSprite(448, 0, 16, 16);
	        
	        ///////
	        
	        explorer = new Explorer(0, Screen.DECORATION_HEIGHT, 280, Screen.HEIGHT);
	        editor = new CodeEditor(280, Screen.DECORATION_HEIGHT, Screen.WIDTH - 280, Screen.HEIGHT); // esses 2 precisa ser inicializados depois das fontes e da spritesheet
	        
	        logo = new Logo(Screen.WIDTH / 2 + 80, Screen.DECORATION_HEIGHT + (Screen.HEIGHT / 2 - 120), 160, 160, star);
	        
	        screen.setFrameIcon(spritesheet.getSprite(144, 0, 16, 16));
	        
	        openBase = new OpenBaseFolderButton(20, Screen.DECORATION_HEIGHT + 70, 48, 48, baseFolderSpr);
	        newFile = new NewFileButton(80, Screen.DECORATION_HEIGHT + 85, 32, 32, newFileSpr);
	        newFolder = new NewFolderButton(120, Screen.DECORATION_HEIGHT + 85, 32, 32, newFolderSpr);
	        oneFolder = new OneFolderUpButton(160, Screen.DECORATION_HEIGHT + 85, 32, 32, folderUp);
	        returnBase = new ReturnToBaseFolderButton(200, Screen.DECORATION_HEIGHT + 85, 32, 32, backBaseFolder);
	        reload = new ReloadButton(240, Screen.DECORATION_HEIGHT + 85, 32, 32, reloadSpr);
	        
	        closeWindow = new CloseWindow(screen.getWidth() - Screen.DECORATION_HEIGHT, 0, Screen.DECORATION_HEIGHT, Screen.DECORATION_HEIGHT, closeWindowSpr);
	        maximizeWindow = new MaximizeWindow(screen.getWidth() - Screen.DECORATION_HEIGHT * 2, 0, Screen.DECORATION_HEIGHT, Screen.DECORATION_HEIGHT, maximizeWindowSpr);
	        minimizeWindow = new MinimizeWindow(screen.getWidth() - Screen.DECORATION_HEIGHT * 3, 0, Screen.DECORATION_HEIGHT, Screen.DECORATION_HEIGHT, minimizeWindowSpr);
	        
	        settingsButton = new SettingsButton(explorer.getWidth() - 34, Screen.DECORATION_HEIGHT + 2, 32, 32, settingsButtonSpr);
	        
	        desktop = Desktop.getDesktop();
	        
	        IDEComponent.components.add(editor);
	        IDEComponent.components.add(explorer);
	        
	        IDEComponent.components.add(logo);
	        
	        IDEComponent.components.add(openBase);
	        
	        if (settingsFile.exists())
	    		readFile(settingsFile);
	        
	        if (defaultConfigFile.exists()) {
	        	conffile = defaultConfigFile.getPath();
	        	hasConfigFile = true;
	        }
	        else {
	        	ListableFile.generateConfigFile(defaultConfigFile);
	        	
	        	conffile = defaultConfigFile.getPath();
	        	hasConfigFile = true;
	        }
	        
	        if (!alreadyLoaded)
	        	load(conffile);
	        
	        TopComponent.topComponents.add(closeWindow);
	        TopComponent.topComponents.add(maximizeWindow);
	        TopComponent.topComponents.add(minimizeWindow);
	        
	        ListableFile.updateTypes();
	        
	        IDEComponent.toAdd.add(Main.newFile);
			IDEComponent.toAdd.add(Main.newFolder);
			IDEComponent.toAdd.add(Main.oneFolder);
			IDEComponent.toAdd.add(Main.returnBase);
			IDEComponent.toAdd.add(Main.reload);
			
			IDEComponent.toAdd.add(settingsButton);
			
			Explorer.fetchStatus();
			
			// -------
			
			if (!editor.tabs.isEmpty() && editor.editing == null)
				editor.tabs.get(0).select();
			
    	} catch (Exception e) {
    		writeLog(e);
    		
    		System.exit(1);
    	}
    }
    
    public static OS getOS() {
    	String name = System.getProperty("os.name").toLowerCase();
    	
    	if (name.contains("windows")) return OS.WINDOWS;
    	else if (name.contains("mac")) return OS.MAC;
    	else if (name.contains("linux")) return OS.LINUX;
    	
    	return OS.OTHER;
    }
    
    public static synchronized void load(String conffile) {
    	alreadyLoaded = true;
    	
    	ListableFile.readConfigFile(conffile);
    	
        Texts.setTexts(lang);
        Fonts.initFonts(fntnr, fnted);
        
        /*Fonts.emojiStream = ClassLoader.getSystemClassLoader().getResourceAsStream(EMOJI_FONT_FILE_NAME);
        
        try {
			Fonts.emojiFont = Font.createFont(Font.TRUETYPE_FONT, Fonts.emojiStream);
		} catch (FontFormatException | IOException e) {
			e.printStackTrace();
		}*/
        
        ////////
        
        baseFolderSpr = Colors.swapColor(baseFolderSpr, Colors.textLightDefault, Colors.textLight);
        
        newFileSpr = Colors.swapColor(newFileSpr, Colors.textLightDefault, Colors.textLight);
        newFolderSpr = Colors.swapColor(newFolderSpr, Colors.textLightDefault, Colors.textLight);
        folderUp = Colors.swapColor(folderUp, Colors.textLightDefault, Colors.textLight);
        backBaseFolder = Colors.swapColor(backBaseFolder, Colors.textLightDefault, Colors.textLight);
        reloadSpr = Colors.swapColor(reloadSpr, Colors.textLightDefault, Colors.textLight);
        
        CodeEditor.functions = Colors.swapColor(CodeEditor.functions, Colors.textLightDefault, Colors.textLight);
        CodeEditor.objects = Colors.swapColor(CodeEditor.objects, Colors.textLightDefault, Colors.textLight);
        CodeEditor.keywords = Colors.swapColor(CodeEditor.keywords, Colors.textLightDefault, Colors.textLight);
        CodeEditor. variables = Colors.swapColor(CodeEditor.variables, Colors.textLightDefault, Colors.textLight);
        
        star = Colors.swapColor(star, Colors.textLightDefault, Colors.textLight);
        folder = Colors.swapColor(folder, Colors.textLightDefault, Colors.textLight);
        
        closeTab = Colors.swapColor(closeTab, Colors.textLightDefault, Colors.textLight);
        notSavedTab = Colors.swapColor(notSavedTab, Colors.textLightDefault, Colors.textLight);
        
        lock = Colors.swapColor(lock, Colors.textLightDefault, Colors.textLight);
        back = Colors.swapColor(back, Colors.textLightDefault, Colors.textLight);
        
        caseSensitive = Colors.swapColor(caseSensitive, Colors.textLightDefault, Colors.textLight);
        regex = Colors.swapColor(regex, Colors.textLightDefault, Colors.textLight);
        
        entireDocument = Colors.swapColor(entireDocument, Colors.textLightDefault, Colors.textLight);
        selectedLines = Colors.swapColor(selectedLines, Colors.textLightDefault, Colors.textLight);
        
        hexView = Colors.swapColor(hexView, Colors.textLightDefault, Colors.textLight);
        binView = Colors.swapColor(binView, Colors.textLightDefault, Colors.textLight);
        
        closeWindowSpr = Colors.swapColor(closeWindowSpr, Colors.textLightDefault, Colors.textLight);
        closeWindowMacSpr = Colors.swapColor(closeWindowMacSpr, Colors.textLightDefault, Colors.textLight);
        closeWindowHoverSpr = Colors.swapColor(closeWindowHoverSpr, Colors.textLightDefault, Colors.textLight);
        
        minimizeWindowSpr = Colors.swapColor(minimizeWindowSpr, Colors.textLightDefault, Colors.textLight);
        minimizeWindowMacSpr = Colors.swapColor(minimizeWindowMacSpr, Colors.textLightDefault, Colors.textLight);
        minimizeWindowHoverSpr = Colors.swapColor(minimizeWindowHoverSpr, Colors.textLightDefault, Colors.textLight);
        
        maximizeWindowSpr = Colors.swapColor(maximizeWindowSpr, Colors.textLightDefault, Colors.textLight);
        maximizeWindowMacSpr = Colors.swapColor(maximizeWindowMacSpr, Colors.textLightDefault, Colors.textLight);
        maximizeWindowHoverSpr = Colors.swapColor(maximizeWindowHoverSpr, Colors.textLightDefault, Colors.textLight);
        
        maximizedWindowSpr = Colors.swapColor(maximizedWindowSpr, Colors.textLightDefault, Colors.textLight);
        deactivatedMacButtons = Colors.swapColor(deactivatedMacButtons, Colors.textLightDefault, Colors.textLight);
        
        explorerTab = Colors.swapColor(explorerTab, Colors.textLightDefault, Colors.textLight);
        searchReplaceTab = Colors.swapColor(searchReplaceTab, Colors.textLightDefault, Colors.textLight);
        gitTab = Colors.swapColor(gitTab, Colors.textLightDefault, Colors.textLight);
        terminalTab = Colors.swapColor(terminalTab, Colors.textLightDefault, Colors.textLight);
        
        settingsButtonSpr = Colors.swapColor(settingsButtonSpr, Colors.textLightDefault, Colors.textLight);
        
        gitError = Colors.swapColor(gitError, Colors.textLightDefault, Colors.textLight);
        gitProgress = Colors.swapColor(gitProgress, Colors.textLightDefault, Colors.textLight);
        gitWarning = Colors.swapColor(gitWarning, Colors.textLightDefault, Colors.textLight);
        gitDone = Colors.swapColor(gitDone, Colors.textLightDefault, Colors.textLight);
        
        branch = Colors.swapColor(branch, Colors.textLightDefault, Colors.textLight);
        
        createBranchSpr = Colors.swapColor(createBranchSpr, Colors.textLightDefault, Colors.textLight);
        checkoutSpr = Colors.swapColor(checkoutSpr, Colors.textLightDefault, Colors.textLight);
        renameBranchSpr = Colors.swapColor(renameBranchSpr, Colors.textLightDefault, Colors.textLight);
        
        /// Change some colors ///
        
        final int sub = 30; // subtract
        
        int r = Colors.textLight.getRed();
        int g = Colors.textLight.getGreen();
        int b = Colors.textLight.getBlue();
        
        r -= sub;
        g -= sub;
        b -= sub;
        
        Color esc = new Color(r, g, b);
        
        notSelectedCloseTab = Colors.swapColor(notSelectedCloseTab, Colors.textLightDefault, esc);
        notSelectedNotSavedTab = Colors.swapColor(notSelectedNotSavedTab, Colors.textLightDefault, esc);
        
        ///
        
        ////////
        
        if (!conffile.equals("none"))
        	hasConfigFile = true;
        
        Explorer.tabs.clear();
        explorer.addTabs();
    }
    
    // Checks whether a command's output is an error or not through parsing.
    public static boolean isError(String[] output) {
    	for (String s : output) {
    		for (String k : errorKeywords) {
    			if (s.toLowerCase().contains(k.toLowerCase())) return true;
    		}
    	}
    	
    	return false;
    }
    
    public static boolean isWarning(String[] output) {
    	for (String s : output) {
    		for (String k : warningKeywords) {
    			if (s.toLowerCase().contains(k.toLowerCase())) return true;
    		}
    	}
    	
    	return false;
    }
    
    public static String[] listToString(List<String> list) {
    	String[] ret = new String[list.size()];
    	
    	for (int i = 0, n = list.size(); i < n; i++) {
    		ret[i] = list.get(i);
    	}
    	
    	return ret;
    }
    
    public static String[] runCommand(File directory, String... commands) {
    	if (os == OS.WINDOWS) {
    		String[] preCommands = { "cmd", "/c" };
    		String[] arr = new String[preCommands.length + commands.length];
    		
    		int i;
    		for (i = 0; i < preCommands.length; i++)
    			arr[i] = preCommands[i];
    		
    		for (i = 0; i < commands.length; i++)
    			arr[preCommands.length + i] = commands[i];
    		
    		return listToString(run(directory, arr));
    	}
    	else {
    		return listToString(run(directory, commands));
    	}
    }
    
    public static List<String> run(File directory, String... commands) {
        List<String> lines = new ArrayList<>();
        /*Runtime rt = Runtime.getRuntime();
        Process p = rt.exec(commands);*/
        try {
	        ProcessBuilder builder = new ProcessBuilder(commands);
	        builder.redirectErrorStream(true);
	        //builder.redirectInput(ProcessBuilder.Redirect.INHERIT);
	        //builder.redirectOutput(ProcessBuilder.Redirect.INHERIT); // esse faz com que printa na tela os comandos
	        builder.directory(directory);
	        Process p = builder.start();
	        
	        // check if the value's non zero
	        p.waitFor();
	        
	        BufferedReader stdin = new BufferedReader(new InputStreamReader(p.getInputStream()));
	        BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
	        
	        String s = null;
	        while ((s = stdin.readLine()) != null) {
	            lines.add(s);
	        }
	        
	        // Maybe separate these two outputs?
	        while ((s = stderr.readLine()) != null) {
	            lines.add(s);
	        }
        } catch (Exception e) {
        	return lines;
        }
        
        return lines;
    }
    
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
    	int lccx = 0, lccy = 0; // local cx (cursor x)
    	
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
						Fonts.initFonts(fntnr, fnted);
						spritesheet = spritesheetFile.exists() ? modifiedSpritesheet : originalSpritesheet; // fazer o closebasefolder nao descarregar o config file
						
				        if (s.equals("none")) baseFolder = null;
				        else {
				        	baseFolder = new File(s);
				        	Explorer.baseFolderName = baseFolder.getName();
				        }
						
						if (Explorer.files.size() == 0) {
							Explorer.files = ListableFile.loadFolder(ListableFile.newListableFile(Main.baseFolder));
						}
					}
					else if (i == 1) {
						conffile = s;
						
						if (!conffile.equals("none"))
							hasConfigFile = true;
					}
					
					else if (i == 2)
						tabindex = Integer.parseInt(s);
					
					else if (i == 3) {
						Main.editor.scrX = Integer.parseInt(s);
						
						lccx = Integer.parseInt(s);
					}
					
					else if (i == 4) {
						Main.editor.scrY = Integer.parseInt(s);
						
						lccy = Integer.parseInt(s);
					}
					
					else if (i == 5)
						Main.editor.tabScr = Integer.parseInt(s);
					
					else if (i == 6) {
						Main.explorer.setDrag(Integer.parseInt(s));
						
						load(conffile);
					}
					
					if (i > 6) {
						//if (!ListableFile.isPath(s)) continue;
						File reg = new File(s);
						if (!reg.exists()) continue;
						
						Tab t = new Tab((i - 4) * Tab.WIDTH, ListableFile.newListableFile(reg));
						
						Main.editor.tabs.add(t);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
	    	
	    	if (Main.editor.tabs.size() > 0) {
				Main.editor.editing = Main.editor.tabs.get(tabindex);
				
				inheritCx = lccx;
				inheritCy = lccy;
				
				editor.editing.cx = lccx;
				editor.editing.cy = lccy;
				
				editor.cursorX = lccx;
				editor.cursorY = lccy;
				
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
    
    public static String getStackTrace(Throwable t) { // Fonte: Apache Commons
    	if (t == null) return "";
    	
    	StringWriter sw = new StringWriter();
    	t.printStackTrace(new PrintWriter(sw, true));
    	
    	return sw.toString();
    }

    public synchronized void start() {
    	if (running) return;
    	
        running = true;

        t = new Thread(this, "Main-Thread");
        t.start();
    }
    
    public synchronized void stop() {
    	if (!running) return;
    	
    	running = false;
    	
    	try {
			t.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    }

    @Override
    public void tick() {
    	for (IDEComponent c : IDEComponent.components)
            c.tick();
    	
    	for (TopComponent t : TopComponent.topComponents)
			t.tick();
    	
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
        
        Rectangle bounds = new Rectangle(screen.frame.getBounds().x, screen.frame.getBounds().y, screen.getBounds().width, screen.getBounds().height);
        screen.frame.setBounds(bounds);
    }

    public synchronized void render() {
        BufferStrategy bs = screen.getBufferStrategy();

        if (bs == null) {
            screen.createBufferStrategy(3);
            return;
        }

        Graphics g = null;
        
        try {
        	g = bs.getDrawGraphics();
        } catch (IllegalStateException e) {
        	screen.createBufferStrategy(3);
            return;
        }
        
        Graphics2D g2 = (Graphics2D) g;

        g.setColor(Colors.background);
        g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);

        for (IDEComponent c : IDEComponent.components)
        	c.render(g);
        
        if (!(CommandTerminal.active || SetFileName.added || RenameFile.added))
	        for (Tab t : Main.editor.tabs) {
				if (t.hovered() && Main.editor.editing == t && t.getX() + Main.editor.tabScr >= editor.getX() - 1 && !t.button.hovered() && !Tab.isTabDragged()) { // por algum motivo a + e nao -
					int index = Main.baseFolder != null ? t.getRegent().getRegent().getPath().contains(File.separator + Main.baseFolder.getName()) && !t.getRegent().getRegent().getParent().equalsIgnoreCase(Main.userDir) ? t.getRegent().getRegent().getPath().indexOf(File.separator + Main.baseFolder.getName()) + 1 : 0 : 0;
					String text = ListableFile.getFileExtension(t.getRegent().getRegent()).equalsIgnoreCase(CONFIG_FILE_EXTENSION) && t.getRegent().getRegent().getParent() != null && t.getRegent().getRegent().getParent().equalsIgnoreCase(Main.userDir) ? Texts.seeingConfigFile : t.getRegent().getRegent().getPath().substring(index);
					text = text.replace('\\', '/');
					
					if (editor.editing.isTemporary)
						text = Texts.thisIsTemporary;
					
					int width = 20 + text.length() * 12;
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
					else if (Main.editor.editing != null && Main.editor.editing.isReadOnly) {
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
					
					Rectangle intersection = new Rectangle(x, y, width, height).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999, Main.screen.getHeight()));
					
					if (!intersection.isEmpty()) {
						x -= intersection.getWidth();
					}
					
					g.setColor(Colors.explorerLight);
					g.fillRect(x, MouseInput.getMouseY(), width, height);
					
					g.setColor(Colors.textLighter);
					g2.setStroke(new BasicStroke(2f));
					g2.drawRect(x, MouseInput.getMouseY(), width, height);
					
					Fonts.drawString(text, (x - 10) + 20, (y - 10) + 10, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					
					if (Main.editor.codeHelpersOn)
						Fonts.drawString(Texts.codeHelpersOn, (x - 10) + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					else
						Fonts.drawString(Texts.codeHelpersOff, (x - 10) + 20, MouseInput.getMouseY() + 40, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					
					Fonts.drawString(Texts.fontSizeIs + " " + CodeEditor.FONT_SIZE + " pixels.", (x - 10) + 20, MouseInput.getMouseY() + 70, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					
					if (Main.editor.editing != null && Main.editor.editing.isReadOnly) {
						if (Main.editor.editing.readMode == FileReadMode.NORMAL)
							Fonts.drawString(Texts.fileAsReadOnly, (x - 10) + 20, (y - 10) + 100, new IDEFont(Fonts.lightGrayNormal, 16), g2);
						else
							Fonts.drawString(Texts.fileAsReadOnly.replace(Texts.readOnly, CodeEditor.getReadModeName(Main.editor.editing.readMode)), (x - 10) + 20, (y - 10) + 100, new IDEFont(Fonts.lightGrayNormal, 16), g2);
					}
				}
	        }
        
        if (explorer.hovered() && !CommandTerminal.expOff && Explorer.explorerMode == ExplorerMode.EXPLORER && baseFolder != null) {
        	if (MouseInput.hovered(explorer.getX() + 10, Screen.DECORATION_HEIGHT + 140, explorer.getWidth() - 10, 23) && Explorer.showBaseFolderCard && !(SetFileName.added || CommandTerminal.active || RenameFile.added)) {
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
        	
        	if (MouseInput.hovered(explorer.getX() + 10, Screen.DECORATION_HEIGHT + 170, explorer.getWidth() - 10, 23) && !Explorer.folderPathFull.isEmpty() && !(SetFileName.added || CommandTerminal.active || RenameFile.added)) {
        		String scopeStr = Explorer.getScopePath().contains(File.separator + baseFolder.getName() + File.separator) ? Explorer.getScopePath().substring(Explorer.getScopePath().indexOf(baseFolder.getName())) : Explorer.getScopePath();
        		scopeStr = scopeStr.replace('\\', '/');
        		
        		int xdr = MouseInput.getMouseX() + 10;
    			int ydr = MouseInput.getMouseY() - 10;
    			
    			int wdr = 15 + scopeStr.length() * 12;
    			final int hdr = 70;
    			
    			if (wdr < 193) wdr = 193;
    			
    			Rectangle intersection = new Rectangle(xdr, ydr, wdr, hdr).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
    			
    			if (!intersection.isEmpty())
    				xdr -= intersection.getWidth();
    			
    			g.setColor(Colors.explorerLight);
    			g.fillRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			g.setColor(Colors.textLighter);
    			g2.setStroke(new BasicStroke(2f));
    			g2.drawRect(xdr, MouseInput.getMouseY() - 15, wdr, hdr);
    			
    			Fonts.drawString(Texts.currentFolder, xdr + 10, ydr + 10, new IDEFont(Fonts.lighterGrayNormal, 16), g);
    			Fonts.drawString(scopeStr, xdr + 10, ydr + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
        	}
        }
        
        if (Explorer.dragging) {
        	String text = explorer.getWidth() + "px";
        	
        	int x = MouseInput.getMouseX() + 35;
        	int y = MouseInput.getMouseY();
        	int w = text.length() * 14;
        	int h = 28;
        	
        	Rectangle intr = new Rectangle(x, y, w, h).intersection(new Rectangle(Main.screen.getWidth() - 2, 0, 999999, Main.screen.getHeight()));
        	
        	if (!intr.isEmpty())
        		x -= intr.getWidth();
        	
        	g.setColor(new Color(0, 0, 0, 0.3f));
			g.fillRect(x, y, w, h);
			
			Fonts.drawString(text, x + 5, y + 5, new IDEFont(Fonts.lightGrayNormal, 16), g);
        }
        
        // draw window top
        
        g.setColor(Colors.explorerLight);
        g.fillRect(0, 0, screen.getWidth(), Screen.DECORATION_HEIGHT);
        
        g.setColor(Colors.explorerLighter);
        g2.setStroke(new BasicStroke(3f));
        g2.drawLine(0, Screen.DECORATION_HEIGHT, screen.getWidth(), Screen.DECORATION_HEIGHT);
        
        String text = screen.frame.getTitle();
		Fonts.drawString(text, (screen.frame.getWidth() / 2) - ((text.length() * 12) / 2), Screen.DECORATION_HEIGHT / 2 - (16 / 2), new IDEFont(Fonts.lighterGrayNormal, 16), g);
        
		if (Explorer.explorerMode == ExplorerMode.GIT && Explorer.isBaseFolderRepository() && MouseInput.hovered(0, Screen.DECORATION_HEIGHT + 70, explorer.getWidth(), 40) && !(SetFileName.added || CommandTerminal.active || RenameFile.added || SetBranchName.added))
	    	Explorer.renderCardText(new String[] { Texts.currentBranch + ":", Explorer.gitStatus.branches[Explorer.gitStatus.currentBranch] }, MouseInput.getMouseX() + 20, MouseInput.getMouseY(), g);
		
		for (TopComponent t : TopComponent.topComponents)
			t.render(g);
		
		if (os != OS.MAC && !forceMacButtons)
			g.drawImage(star, 10, Screen.DECORATION_HEIGHT / 2 - (16 / 2), 16, 16, null);
		
		if (WindowInput.isDeactivated()) {
			g.setColor(new Color(0, 0, 0, 0.2f));
			g.fillRect(0, 0, Main.screen.getWidth(), Screen.DECORATION_HEIGHT + 3);
		}
		
        bs.show();
    }
    
    public static void writeLog(Throwable e) {
    	try {
    		BufferedWriter wr = Files.newBufferedWriter(logFile.toPath(), StandardCharsets.UTF_8);
    		
    		String st = getStackTrace(e);
    		Calendar c = Calendar.getInstance();
    		
    		System.err.println("An error occurred (Also written in " + LOG_FILE_NAME + "):\n" + st);
    		
			wr.write("An Exception occurred in " + PROGRAM_NAME + " at " + c.getTime() + ".\n\n");
			wr.write("Message: " + e.getMessage() + "\n");
			wr.write("Localized Message: " + e.getLocalizedMessage() + "\n");
			wr.write("Cause: " + e.getCause() + "\n\n");
			
			wr.write("Stack Trace:\n" + st);
			
			wr.close();
			
			//System.exit(1);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
    }
    
    public static void close(int status) {
    	closing:
        	if (WindowInput.isClosing()) {
        		writeFile(settingsFile);
        		ListableFile.generateLocalConfigFile(defaultConfigFile);
        		
	    		if (Main.editor.editing != null) { // nao for nulo
	    			if (!Main.editor.editing.isSaved()) { // nao estiver salvo
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
	    		
	    		System.exit(status);
	    	}
    }

    public static void closeForced(int status) {
    	writeFile(settingsFile);
    	ListableFile.generateLocalConfigFile(defaultConfigFile);

    	if (Main.editor.editing != null) { // nao for nulo
    		if (!Main.editor.editing.isSaved()) { // nao estiver salvo
    			String[] options = { Texts.save, Texts.dont + " " + Texts.save, Texts.cancel };

    			CodeEditor.setSystemLook();
    			int selectedOption = JOptionPane.showOptionDialog(null, Texts.theFile + " " + Main.editor.editing.getRegent().getRegent().getName() + " " + Texts.isNotSaved, Texts.confirmSave, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);

    			if (selectedOption == 0) Main.editor.editing.save();
    			else if (selectedOption == 2) {
    				WindowInput.update();
    				
    				return;
    			}
    		}
    	}

    	System.exit(status);
    }

    public synchronized void mainLogic() {
    	canRunLoop = true;
    }
    
    public synchronized void closeWindow() {
    	canRunLoop = false;
    	
    	close(0);
    }
    
    @Override
    public void run() {
    	screen.requestFocus();
    	
    	while (running) {
    		try {
    			if (canRunLoop) {
    				tick();
    				render();
    				
    				canRunLoop = false;
    			}

    			Thread.sleep(1000 / 60);

    			close(0);
    		} catch (Throwable e) {
    			writeLog(e);

    			close(1);
    		}
    	}
    }
}
