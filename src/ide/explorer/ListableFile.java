package ide.explorer;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.ExecuteCommand;
import ide.components.IDEComponent;
import ide.components.RenameFile;
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

public class ListableFile extends IDEComponent implements ExecuteCommand, Serializable {
	
	public static boolean hasAltered = false;
	
	private static final long serialVersionUID = 1L;

	public static transient FileType[] types = {
			new FileType(".java", Main.spritesheet.getSprite (0, 16, 16, 16)),	// adicionar suporte aos formatos .o e .out
			new FileType(".class",Main.spritesheet.getSprite(272,32, 16, 16)),
			new FileType(".c", Main.spritesheet.getSprite   (16, 16, 16, 16)),
			new FileType(".cpp", Main.spritesheet.getSprite (32, 16, 16, 16)),
			new FileType(".cc", Main.spritesheet.getSprite  (32, 16, 16, 16)),
			new FileType(".cxx", Main.spritesheet.getSprite (32, 16, 16, 16)),
			new FileType(".cs", Main.spritesheet.getSprite  (48, 16, 16, 16)),
			new FileType(".py", Main.spritesheet.getSprite  (64, 16, 16, 16)),
			new FileType(".pyd", Main.spritesheet.getSprite (64, 16, 16, 16)),
			new FileType(".js", Main.spritesheet.getSprite  (80, 16, 16, 16)),
			new FileType(".mjs", Main.spritesheet.getSprite (80, 16, 16, 16)),
			new FileType(".bat", Main.spritesheet.getSprite (96, 16, 16, 16)),
			new FileType(".com", Main.spritesheet.getSprite(592, 32, 16, 16)),
			new FileType(".cmd", Main.spritesheet.getSprite(592, 32, 16, 16)),
			new FileType(".h", Main.spritesheet.getSprite  (112, 16, 16, 16)),
			new FileType(".hh", Main.spritesheet.getSprite (112, 16, 16, 16)),
			new FileType(".hxx", Main.spritesheet.getSprite(112, 16, 16, 16)),
			new FileType(".hpp", Main.spritesheet.getSprite(112, 16, 16, 16)),
			new FileType(".asm", Main.spritesheet.getSprite(128, 16, 16, 16)),
			new FileType(".s", Main.spritesheet.getSprite  (128, 16, 16, 16)),
			new FileType(".lua", Main.spritesheet.getSprite(144, 16, 16, 16)),
			new FileType(".sql", Main.spritesheet.getSprite(160, 16, 16, 16)),
			new FileType(".swift",Main.spritesheet.getSprite(176,16, 16, 16)),
			new FileType(".rs", Main.spritesheet.getSprite (192, 16, 16, 16)),
			new FileType(".php", Main.spritesheet.getSprite(208, 16, 16, 16)),
			new FileType(".kt", Main.spritesheet.getSprite (224, 16, 16, 16)),
			new FileType(".vue", Main.spritesheet.getSprite(240, 16, 16, 16)),
			new FileType(".rb", Main.spritesheet.getSprite (256, 16, 16, 16)),
			new FileType(".ino", Main.spritesheet.getSprite(272, 16, 16, 16)),
			new FileType(".ts", Main.spritesheet.getSprite (288, 16, 16, 16)),
			new FileType(".go", Main.spritesheet.getSprite (304, 16, 16, 16)),
			new FileType(".r",  Main.spritesheet.getSprite (320, 16, 16, 16)),
			new FileType(".jl", Main.spritesheet.getSprite (336, 16, 16, 16)),
			new FileType(".pl", Main.spritesheet.getSprite (352, 16, 16, 16)),
			new FileType(".has", Main.spritesheet.getSprite(368, 16, 16, 16)),
			new FileType(".hs", Main.spritesheet.getSprite (368, 16, 16, 16)),
			new FileType(".fs", Main.spritesheet.getSprite (384, 16, 16, 16)),
			new FileType(".coffee",Main.spritesheet.getSprite(400,16,16, 16)),
			new FileType(".m", Main.spritesheet.getSprite  (416, 16, 16, 16)),
			new FileType(".pas", Main.spritesheet.getSprite(432, 16, 16, 16)),
			new FileType(".pp", Main.spritesheet.getSprite (432, 16, 16, 16)),
			new FileType(".scala",Main.spritesheet.getSprite(448,16, 16, 16)),
			new FileType(".dart",Main.spritesheet.getSprite(464, 16, 16, 16)),
			new FileType(".zig", Main.spritesheet.getSprite(480, 16, 16, 16)),
			new FileType(".scss",Main.spritesheet.getSprite(496, 16, 16, 16)),
			new FileType(".ipynb",Main.spritesheet.getSprite(512,16, 16, 16)),
			new FileType(".vb", Main.spritesheet.getSprite (528, 16, 16, 16)),
			
			new FileType(".html", Main.spritesheet.getSprite (0, 32, 16, 16)),
			new FileType(".xhtml", Main.spritesheet.getSprite(0, 32, 16, 16)),
			new FileType(".htm", Main.spritesheet.getSprite  (0, 32, 16, 16)),
			new FileType(".css", Main.spritesheet.getSprite (16, 32, 16, 16)),
			new FileType(".xml", Main.spritesheet.getSprite (32, 32, 16, 16)),
			new FileType(".json", Main.spritesheet.getSprite(48, 32, 16, 16)),
			new FileType(".jsonc",Main.spritesheet.getSprite(48, 32, 16, 16)),
			new FileType(".md", Main.spritesheet.getSprite  (64, 32, 16, 16)),
			new FileType(".markdown",Main.spritesheet.getSprite(64,32,16,16)),
			new FileType(".txt", Main.spritesheet.getSprite (80, 32, 16, 16)),
			new FileType(".log", Main.spritesheet.getSprite (80, 32, 16, 16)),
			new FileType(".pdf", Main.spritesheet.getSprite (96, 32, 16, 16)),
			new FileType(".jar", Main.spritesheet.getSprite(112, 32, 16, 16)),
			new FileType(".exe", Main.spritesheet.getSprite(128, 32, 16, 16)),
			new FileType(".svg", Main.spritesheet.getSprite(144, 32, 16, 16)),
			new FileType(".urna",Main.spritesheet.getSprite(160, 32, 16, 16)),		// easter egg! (Criador de Urnas)
			new FileType(".save",Main.spritesheet.getSprite(176, 32, 16, 16)),		// easter egg! (World's Hardest Game Maker 2)
			new FileType(".conf",Main.spritesheet.getSprite(192, 32, 16, 16)),
			new FileType(".mk", Main.spritesheet.getSprite (352, 32, 16, 16)),
			new FileType(".make",Main.spritesheet.getSprite(208, 32, 16, 16)),
			new FileType(".sh", Main.spritesheet.getSprite (224, 32, 16, 16)),
			new FileType(".gitignore",Main.spritesheet.getSprite(240,32,16,16)),
			new FileType(".dockerfile",Main.spritesheet.getSprite(256,32,16,16)),
			new FileType(".jsx", Main.spritesheet.getSprite(368, 32, 16, 16)),
			new FileType(".config",Main.spritesheet.getSprite(352,32,16, 16)),
			new FileType(".cfg", Main.spritesheet.getSprite (352, 32,16, 16)),
			new FileType(".ps1", Main.spritesheet.getSprite(320, 32, 16, 16)),
			new FileType(".license",Main.spritesheet.getSprite(336,32,16,16)),
			new FileType(".docx",Main.spritesheet.getSprite(384, 32, 16, 16)),
			new FileType(".xlsx",Main.spritesheet.getSprite(400, 32, 16, 16)),
			new FileType(".docx",Main.spritesheet.getSprite(384, 32, 16, 16)),
			new FileType(".pptx",Main.spritesheet.getSprite(416, 32, 16, 16)),
			new FileType(".one", Main.spritesheet.getSprite(432, 32, 16, 16)),
			new FileType(".psd",Main.spritesheet.getSprite (448, 32, 16, 16)),
			new FileType(".aed",Main.spritesheet.getSprite (464, 32, 16, 16)),
			new FileType(".ai", Main.spritesheet.getSprite (480, 32, 16, 16)),
			new FileType(".indd",Main.spritesheet.getSprite(496, 32, 16, 16)),
			new FileType(".ejs", Main.spritesheet.getSprite(512, 32, 16, 16)),
			new FileType(".ld", Main.spritesheet.getSprite (528, 32, 16, 16)),
			new FileType(".lock",Main.spritesheet.getSprite(544, 32, 16, 16)),
			new FileType(".ini", Main.spritesheet.getSprite(560, 32, 16, 16)),
			new FileType(".dll", Main.spritesheet.getSprite(576, 32, 16, 16)),
			new FileType(".makefile",Main.spritesheet.getSprite(208,32,16,16)),
			new FileType(".url", Main.spritesheet.getSprite(608, 32, 16, 16)),
			
			new FileType(".png", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".jpg", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".jpeg", Main.spritesheet.getSprite (0, 48, 16, 16)),
			new FileType(".gif", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".bmp", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".ico", Main.spritesheet.getSprite (64, 48, 16, 16)),
			new FileType(".webp", Main.spritesheet.getSprite (0, 48, 16, 16)),
			
			new FileType(".mp4", Main.spritesheet.getSprite (16, 48, 16, 16)),
			new FileType(".wmv", Main.spritesheet.getSprite (16, 48, 16, 16)),
			new FileType(".avi", Main.spritesheet.getSprite (16, 48, 16, 16)),
			
			new FileType(".wav", Main.spritesheet.getSprite (32, 48, 16, 16)),
			new FileType(".mp3", Main.spritesheet.getSprite (32, 48, 16, 16)),
			new FileType(".ogg", Main.spritesheet.getSprite (32, 48, 16, 16)),
			
			new FileType(".otf", Main.spritesheet.getSprite (48, 48, 16, 16)),
			new FileType(".ttf", Main.spritesheet.getSprite (48, 48, 16, 16)),
			new FileType(".woff",Main.spritesheet.getSprite (48, 48, 16, 16)),
			new FileType(".woff2",Main.spritesheet.getSprite(48, 48, 16, 16)),
			
			new FileType(".zip", Main.spritesheet.getSprite(288, 32, 16, 16)),
			new FileType(".gz",  Main.spritesheet.getSprite(288, 32, 16, 16)),
			new FileType(".rar", Main.spritesheet.getSprite(288, 32, 16, 16)),
			new FileType(".7z", Main.spritesheet.getSprite (288, 32, 16, 16)),
			
			new FileType(".bin", Main.spritesheet.getSprite(304, 32, 16, 16)),
			new FileType(".img", Main.spritesheet.getSprite(640, 32, 16, 16)),
			new FileType(".iso", Main.spritesheet.getSprite(640, 32, 16, 16)),
			new FileType(".flp", Main.spritesheet.getSprite(656, 32, 16, 16)),
			new FileType(".o",  Main.spritesheet.getSprite (672, 32, 16, 16)),
			new FileType(".out", Main.spritesheet.getSprite(672, 32, 16, 16)),
			new FileType(".obj", Main.spritesheet.getSprite(672, 32, 16, 16)),
			
			// Specials
			
			new FileType("makefile",Main.spritesheet.getSprite(208, 32, 16, 16)),
			new FileType("dockerfile",Main.spritesheet.getSprite(256,32,16, 16)),
			new FileType("license", Main.spritesheet.getSprite(336, 32, 16, 16)),
			new FileType("authors", Main.spritesheet.getSprite(624, 32, 16, 16)),
			new FileType("gitignore", Main.spritesheet.getSprite (240,32,16,16)),
	};
	
	private ListableFile parent;
	private File regent;
	
	public static List<ListableFile> files = new ArrayList<ListableFile>(Explorer.files);

	public ListableFile(int x, int y, int width, int height, File regent, ListableFile parent) {
		super(x, y, width, height, null);
		
		this.regent = regent;
		this.parent = parent;
	}
	
	public ListableFile getParent() {
		return parent;
	}
	
	public File getRegent() {
		return regent;
	}
	
	public void setParent(ListableFile parent) {
		this.parent = parent;
	}
	
	@Override
	public String toString() {
		return "ListableFile: [parent: " + parent + ", regent: " + regent + "]";
	}
	/*
	 * Fazer não renderizar nem tickar ListableFiles nem Tabs fora da tela
	 * 
	 * if (y < 200 || y > Main.screen.getHeight()) return;
	 */
	
	public static String getFileExtension(File file) { // Fonte: StackOverflow
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}
	
	public static boolean fileHasExtension(File file) {
		return file.getName().contains(".");
	}
	
	public static boolean fileHasExtension(String name) {
		return name.contains(".");
	}
	
	/**
	 * Retorna true ou false se o caminho especificado em path é um caminho válido, ou seja, se o arquivo existe.
	 * 
	 * @param path - O caminho
	 * @return true, se é um caminho válido, false se não.
	 */
	public static boolean isPath(String path) {
		return new File(path).exists();
	}
	
	public static ListableFile search(File regent) {
		for (ListableFile l : Explorer.files) {
			if (l.getRegent().equals(regent))
				return l;
		}
		
		// Não achei nada
		return null;
	}
	
	public static ListableFile searchListableFiles(File regent) {
		for (ListableFile l : files) {
			if (l.getRegent().equals(regent))
				return l;
		}
		
		// Não achei nada
		return null;
	}
	
	public static boolean hasDuplicateFileNames(String name, File folder) {
		String[] list = folder.list();
		
		if (list == null) list = new String[0];
		
		for (String s : list) {
			if (s.equalsIgnoreCase(name)) return true;
		}
		
		return false;
	}
	
	public static ListableFile search(File regent, File folder) { // deu certo pq ficou pegando o parent sempre da pasta que está o scope, e vai indo até a pasta base
		ListableFile prdoprdoparent = folder.getParentFile().getAbsolutePath().equals(Main.baseFolder.getAbsolutePath()) ? new ListableFile(0, 0, 0, 0, Main.baseFolder, null) : new ListableFile(0, 0, 0, 0, folder.getParentFile().getParentFile(), null);
		ListableFile prdoparent = folder.getParentFile().getAbsolutePath().equals(Main.baseFolder.getAbsolutePath()) ? new ListableFile(0, 0, 0, 0, Main.baseFolder, null) : new ListableFile(0, 0, 0, 0, folder.getParentFile(), prdoprdoparent);
		ListableFile parent = new ListableFile(0, 0, 0, 0, folder, prdoparent); // o parent não precisa ter outro parent, ou precisa?
		
		if (folder.getAbsolutePath().equals(Main.baseFolder.getAbsolutePath())) // se o parent for a pasta base, defina o
			parent = null;														// parent como null.
		
		List<ListableFile> fl = new ArrayList<>();
		
		int index = 0;
		
		for (File f : folder.listFiles()) {
			fl.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, parent));
			
			index++;
		}
		
		for (ListableFile l : fl) {
			if (l.getRegent().equals(regent))
				return l;
		}
		
		// Não achei nada
		return null;
	}
	
	/* Como é composto o Arquivo:
	 * 
	 * Arquivo de Configurações para Boot IDE
	 * 
	 * - Colors
	 * 
	 * background: default
	 * backgroundLight: default
	 * explorer: default
	 * explorerLight: default
	 * textLight: default
	 * textLighter: default
	 * objects: default
	 * methods: default
	 * numbers: default
	 * keywords: default
	 * variables: default
	 * comments: default
	 * strings: default
	 * generics: default
	 * select1: default
	 * select2: default
	 * 
	 * - Files
	 * 
	 * spritesheet: default
	 * font-normal: default
	 * font-bold: default
	 * 
	 * - Color Mode
	 * 
	 * objects: normal
	 * methods: normal
	 * numbers: normal
	 * keywords: normal
	 * variables: normal
	 * comments: normal
	 * strings: normal
	 * generics: normal
	 * 
	 * - Settings
	 * 
	 * Lembrar de abas quando fechar a Boot IDE: true
	 * Lembrar do arquivo de configurações: true
	 * 
	 * Colorir Objetos: true
	 * Colorir Métodos: true
	 * Colorir Números: true
	 * Colorir Palavras-chave: true
	 * Colorir Variáveis: true
	 * Colorir Comentários: true
	 * Colorir Strings: true
	 * Colorir Genéricos: true
	 * */
	 
	public static void generateConfigFile(File file) {
		String pathStr = file.getAbsolutePath();
		String s = pathStr.contains(".conf") ? pathStr + "" : pathStr + ".conf";
		
		//Path path = Paths.get(s);
		
		try {
			BufferedWriter w = /*Files.newBufferedWriter(path, StandardCharsets.UTF_8);*/ new BufferedWriter(new FileWriter(s));
			
			w.write(Main.lang == Language.PORT ? "Arquivo de Configurações da Boot IDE" : "Boot IDE Configuration File" + "\n");
			w.write("\n");
			w.write("- Colors\n");
			w.write("\n");
			w.write("background: default\n");
			w.write("background2: default\n");
			w.write("backgroundLight: default\n");
			w.write("explorer: default\n");
			w.write("explorerLight: default\n");
			w.write("explorerLighter: default\n");
			w.write("textLight: default\n");
			w.write("textLighter: default\n");
			w.write("objects: default\n");
			w.write("methods: default\n");
			w.write("numbers: default\n");
			w.write("keywords: default\n");
			w.write("variables: default\n");
			w.write("comments: default\n");
			w.write("strings: default\n");
			w.write("symbols: default\n");
			w.write("cursor: default\n");
			w.write("selection: default\n");
			w.write("other: default\n");
			w.write("lowerBar: default\n");
			w.write("error: default\n");
			w.write("lineNumber: default\n");
			w.write("selectedLineNumber: default\n");
			w.write("\n");
			/*w.write("- Files\n");
			w.write("\n");
			w.write("spritesheet: default\n");
			w.write("font-normal: default\n");
			w.write("font-bold: default\n");*/
			/*w.write("\n");
			w.write("- Color Mode\n");
			w.write("\n");
			w.write("objectsMode: normal\n");
			w.write("methodsMode: normal\n");
			w.write("numbersMode: normal\n");
			w.write("keywordsMode: normal\n");
			w.write("variablesMode: normal\n");
			w.write("commentsMode: normal\n");
			w.write("stringsMode: normal\n");
			w.write("genericsMode: normal\n");*/
			w.write("\n");
			w.write("- Settings\n");
			w.write("\n");
			/*w.write("Lembrar de abas quando fechar a Boot IDE: true\n");
			w.write("Lembrar do arquivo de configurações: true\n");
			w.write("\n");
			w.write("Colorir Objetos: true\n");
			w.write("Colorir Métodos: true\n");
			w.write("Colorir Números: true\n");
			w.write("Colorir Palavras-chave: true\n");
			w.write("Colorir Variáveis: true\n");
			w.write("Colorir Comentários: true\n");
			w.write("Colorir Comentários: true\n");
			w.write("Colorir Strings: true\n");
			w.write("Colorir Genéricos: true\n");*/
			w.write("font_size: default\n");
			w.write("language: default\n");
			w.write("autocomplete_active: default\n");
			w.write("automatically_open_tabs: default");
			
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readConfigFile(String path) {
		File f = new File(path);
		Path p = f.toPath();
		
		if (!f.exists()) CommandTerminal.runCommand("unloadconfigfile");
		
		hasAltered = false;
		
		List<String> lines = new ArrayList<>();
		
		try {
			lines = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
		}
		catch (Exception e) {
			try {
				lines = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
			} catch (Exception ff) {}
		}
		
		for (String s : lines) {
			//if (s.startsWith("-") || s.startsWith("\n")) continue;
			String[] split = s.split(" ");
			
			switch (split[0]) {
			case "background:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.background = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "background2:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.background2 = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "backgroundLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.backgroundLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "explorer:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.explorer = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "explorerLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.explorerLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "explorerLighter:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.explorerLighter = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "textLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.textLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "textLighter:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.textLighter = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "objects:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.objects = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "methods:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.methods = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "numbers:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.numbers = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "keywords:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.keywords = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "variables:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.variables = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "comments:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.comments = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "strings:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.strings = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "symbols:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.symbols = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "selection:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.selection = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "cursor:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.cursor = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "other:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.other = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
			
			case "lowerBar:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.lowerBar = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "error:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.error = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "lineNumber:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.lineNumber = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "selectedLineNumber:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				hasAltered = true;
				
				try {
					Colors.selectedLineNumber = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
				////////
				
			/*case "spritesheet:":
				if (split[1].equals("default")) break;
				
				Main.sprsh = split[1];
				
				break;
				
			case "font_normal:":
				if (split[1].equals("default")) break;
				
				Main.fntnr = split[1];
				
				break;
				
			case "font_bold:":
				if (split[1].equals("default")) break;
				
				Main.fntbl = split[1];
				
				break;*/
				
			case "font_size:":
				if (split[1].equals("default")) break;
				
				int size = 0;
				
				try {
					size = Integer.parseInt(split[1]);
				} catch (Exception e) {
					size = 16;
				}
				
				CodeEditor.FONT_SIZE = size;
				hasAltered = true;
				
				break;
				
			case "autocomplete_active:":
				if (split[1].equals("default")) break;
				
				CodeEditor.isAutoCompleteActive = Boolean.valueOf(split[1]);
				
				hasAltered = true;
				
				break;
				
			case "automatically_open_tabs:":
				if (split[1].equals("default")) break;
				
				CodeEditor.automaticallyOpenTabs = Boolean.valueOf(split[1]);
				
				hasAltered = true;
				
				break;
				
			case "language:":
				if (split[1].equals("default")) break;
				
				try {
					Main.lang = Language.valueOf(split[1].toUpperCase());
				} catch (IllegalArgumentException e) {
					break;
				}
					
				hasAltered = true;
				
				break;
			}
		}
	}
	
	public static List<File> listFilesOrdered(File folder) {
		File[] dirs = folder.listFiles(new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			File f = new File(dir, name);
    			
    			return f.isDirectory();
    		}
    	});
    	
    	File[] fls = folder.listFiles(new FilenameFilter() {
    		public boolean accept(File dir, String name) {
    			File f = new File(dir, name);
    			
    			return f.isFile();
    		}
    	});
    	
    	if (dirs == null) dirs = new File[0];
    	if (fls == null) fls = new File[0];
    	
    	List<File> dirsList = Arrays.asList(dirs);
    	List<File> flsList = Arrays.asList(fls);
    	
    	List<File> all = new ArrayList<>();
    	
    	all.addAll(dirsList);
    	all.addAll(flsList);
    	
    	return all;
	}
	
	public static List<ListableFile> loadFolder(ListableFile folder) {
		Explorer.scope = folder;
		
		List<ListableFile> files = new ArrayList<>();
		
		if (folder != null) {
			if (folder.regent.isDirectory()) {
				int index = 0;
				
				for (File f : listFilesOrdered(folder.regent)) {
					files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, folder));
					
					index++;
				}
			}
		}
		else {
			int index = 0;
			
			for (File f : listFilesOrdered(Main.baseFolder)) {
				files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
				
				index++;
			}
		}
		
		return files;
	}
	
	public static String getFileNameWithoutExtension(File file) {
        String fileName = "";
 
        try {
            if (file != null && file.exists()) {
                String name = file.getName();
                fileName = name.replaceFirst("[.][^.]+$", "");
            }
        } catch (Exception e) {
            e.printStackTrace();
            fileName = "";
        }
 
        return fileName;
 
    }
	
	@Override
	public void execute(String arg) {
		switch (arg) {
		case "del":
			String[] options = { Texts.yes, Texts.no };
			
			CodeEditor.setSystemLook();
			int selectedOption = JOptionPane.showOptionDialog(null, Texts.sureDelete + " " + regent.getName() + "?", Texts.confirmDelete, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			
			if (selectedOption != 0) break;
			
			if (!regent.delete()) {
				CodeEditor.setSystemLook();
				
				JOptionPane.showMessageDialog(null, Texts.delError, Texts.cantDelete, JOptionPane.OK_OPTION);
			}
			
			for (Tab t : Main.editor.tabs)
				if (t.getRegent().equals(this)) t.close();
			
			IDEComponent.toRemove.add(this);
				
			Explorer.files.clear();
			ListableFile.files.clear();
			
			Explorer.files = ListableFile.loadFolder(Explorer.scope);
			break;
			
		case "run":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", regent.getName());
				File dir = regent.getParentFile();
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "runbash":
			try {
				ProcessBuilder pb = new ProcessBuilder("sh", "-c", "start", regent.getName());
				File dir = regent.getParentFile();
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "cmd":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start");
				
				File dir = Explorer.scope != null ? Explorer.scope.regent : Main.baseFolder; // eu tava fazendo o equivalente a isso: null.regent != null
				
				pb.directory(dir);
				
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "sysexp":
			try {
				Main.desktop.open(new File(regent.getPath()).getParentFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "term":
			CodeEditor.execTerminal();
			break;
			
		case "setbase":
			Main.baseFolder = new File(Explorer.getScopePath());
			Explorer.folderPath = "";
			
			ListableFile.files = ListableFile.loadFolder(null);
			
			break;
			
		case "opendef":
			new Thread() {
				public void run() {
					try {
						Main.desktop.open(regent);
					} catch (Exception e) {
						CodeEditor.setSystemLook();
						
						JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound, JOptionPane.OK_OPTION);
					}
				}
			}.start();
			break;
			
		case "rename":
			RenameFile ren = new RenameFile(0, y, Main.explorer.getWidth() - 3, 30, regent);
			
			if (RenameFile.added) return;
			
			RenameFile.added = true;
			
			IDEComponent.toAdd.add(ren);
			
			break;
			
		case "newfile":
			int y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;
			
			SetFileName set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, true);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(set);
			break;
			
		case "newfolder":
			y = 200;
			
			if (Explorer.files.size() > 0) y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;
			
			set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, false);
			
			if (SetFileName.added) return;
			
			SetFileName.added = true;
			
			IDEComponent.toAdd.add(set);
			break;
		}
	}
	
	public static void addTab(ListableFile file, boolean isAutomatic) {
		if (!CodeEditor.automaticallyOpenTabs && isAutomatic) return;
		
		System.out.println("aa");
		
		if (file.getRegent().isFile() && Main.editor.tabs != null) {
			int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX() : Tab.MIN_X;
			
			new Thread() {
				public void run() {
					try {
						Main.editor.lines = Main.editor.readFile(file.getRegent());
					} catch (IOException e) { // não suportado, se caiu aqui
						return;
					}
				}
			}.start();
			
			Tab toAdd = new Tab((lastX + Tab.WIDTH) + 3, file);
			
			Main.editor.cursorX = 0;
			Main.editor.cursorY = 1;
			
			Main.editor.scrX = 0;
			Main.editor.scrY = 0;
			
			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			
			for (Tab t : Main.editor.tabs)
				if (t.getRegent().getRegent().getPath().equals(file.getRegent().getPath())) {
					Main.editor.editing = t;
					
					return;
				}
			
			Main.editor.toAdd.add(toAdd);
			Main.editor.editing = toAdd;
		}
	}
	
	public static boolean formatNotSupported(String format) {
		return  format.equalsIgnoreCase(".png") ||
				format.equalsIgnoreCase(".jpg") || // provavelmente fazer isso com excel e mais coisas do word
				format.equalsIgnoreCase(".jpeg")||
				format.equalsIgnoreCase(".png") ||
				format.equalsIgnoreCase(".ico") ||
				format.equalsIgnoreCase(".gif") ||
				format.equalsIgnoreCase(".bmp") ||
				format.equalsIgnoreCase(".wav") ||
				format.equalsIgnoreCase(".mp3") ||
				format.equalsIgnoreCase(".ogg") ||
				format.equalsIgnoreCase(".mp4") ||
				format.equalsIgnoreCase(".wmv") ||
				format.equalsIgnoreCase(".avi") ||
				format.equalsIgnoreCase(".exe") ||
				format.equalsIgnoreCase(".pdf") ||
				format.equalsIgnoreCase(".webp");
	}
	
	public void tick() {
		if (SetFileName.added || CommandTerminal.active || RenameFile.added) return;
		if (CommandTerminal.expOff) return;
		
		if (!regent.exists() && Main.editor.tabs != null) {
			Explorer.toRemove.add(this);
			
			for (Tab t : Main.editor.tabs)
				if (t.getRegent().getRegent().getPath().equals(this.regent.getPath()))
					t.close();
				
			Explorer.toRemove.addAll(Explorer.files);
			ListableFile.files.clear();
			
			files = ListableFile.loadFolder(Explorer.scope);
		}
		
		if (hovered() && !RenameFile.added)
			Explorer.hoveringListableFile = true;
		
		if (leftClicked() && !(y < 200 || y > Main.screen.getHeight()) && !RightClickOption.isRightClickActive()) {
			MouseInput.updateMouse();
			
			if (Explorer.folderPath.length() > 22)
				Explorer.folderPath = Explorer.folderPath.substring(0, 19) + "...";
	    	
			Explorer.baseFolderName = Main.baseFolder.getName().length() > 15 ? Main.baseFolder.getName().substring(0, 12) + "..." : Main.baseFolder.getName();
			
			if (formatNotSupported(getFileExtension(regent))) {
					try {
						Main.desktop.open(regent);
					} catch (IOException e) {
						CodeEditor.setSystemLook();
						
						JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound, JOptionPane.OK_OPTION);
					}
					
					return;
				}
			
			if (y > 199 && regent.isDirectory()) {
				files = loadFolder(this);
				
				if (files.size() == 0)
					Explorer.toRemove.addAll(Explorer.files);
			}
			
			if (regent.isFile() && Main.editor.tabs != null) {
				int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX() : Tab.MIN_X;
				
				new Thread() {
					public void run() {
						try {
							Main.editor.lines = Main.editor.readFile(regent);
						} catch (IOException e) { // não suportado, se caiu aqui
							return;
						}
					}
				}.start();
				
				Tab toAdd = new Tab((lastX + Tab.WIDTH) + 3, this);
				
				Main.editor.cursorX = 0;
				Main.editor.cursorY = 1;
				
				Main.editor.scrX = 0;
				Main.editor.scrY = 0;
				
				Main.editor.isMultilineCommenting = false;
				Main.editor.isAnotherIteration = false;
				
				for (Tab t : Main.editor.tabs)
					if (t.getRegent().getRegent().getPath().equals(this.regent.getPath())) {
						Main.editor.editing = t;
						
						return;
					}
				
				Main.editor.wordSinceSpace = "";
				
				Main.editor.toAdd.add(toAdd);
				Main.editor.editing = toAdd;
			}
		}
		
		/*if (KeyInput.isKeyPressed() && hovered()) {
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_F2) { // F2 - Renomear
				KeyInput.updateKeys();
				
				execute("rename");
				
				return;
			}
		}*/
		
		if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered()))) {
			MouseInput.updateMouse();
			
			int widthDraw = Main.lang == Language.PORT ? 540 : 520;
			
			IDEComponent.addRightClickOption((x + width), y - 60, widthDraw, Texts.createFile, (s) -> execute(s), "newfile");
			IDEComponent.addRightClickOption((x + width), y - 30, widthDraw, Texts.createFolder, (s) -> execute(s), "newfolder");
			
			IDEComponent.addRightClickOption((x + width), y, widthDraw, Texts.delete, (s) -> execute(s), "del");
			IDEComponent.addRightClickOption((x + width), y + 30, widthDraw, Texts.rename, (s) -> execute(s), "rename");
			IDEComponent.addRightClickOption((x + width), y + 60, widthDraw, Texts.openCmd, (s) -> execute(s), "cmd");
			IDEComponent.addRightClickOption((x + width), y + 90, widthDraw, Texts.openTerminal, (s) -> execute(s), "term");
			IDEComponent.addRightClickOption((x + width), y + 120, widthDraw, Texts.openExplorer, (s) -> execute(s), "sysexp");
			IDEComponent.addRightClickOption((x + width), y + 150, widthDraw, Texts.setBaseFolder, (s) -> execute(s), "setbase");
			IDEComponent.addRightClickOption((x + width), y + 180, widthDraw, Texts.openDefault, (s) -> execute(s), "opendef");
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if ((getFileExtension(regent).equals(".bat") || getFileExtension(regent).equals(".cmd") || getFileExtension(regent).equals(".com") || getFileExtension(regent).equals(".ps1")) && isWindows)
				IDEComponent.addRightClickOption((x + width), y + 210, widthDraw, Texts.execute, (s) -> execute(s), "run");
			
			if (getFileExtension(regent).equals(".sh") && !isWindows)
				IDEComponent.addRightClickOption((x + width), y + 210, widthDraw, Texts.execute, (s) -> execute(s), "runbash");
		}
		
		int index = Explorer.files.indexOf(this);
		
		if (index <= 0) return;
		
		y = Explorer.files.get(index - 1).y + height;
	}
	
	public void render(Graphics g) {
		if (y < 200 || y > Main.screen.getHeight()) return;
		if (CommandTerminal.expOff) return;
		if (y < 199) return;
		
		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof RenameFile)
				if (((RenameFile) i).old == regent) return;
		}
		
		if (hovered() && !SetFileName.added && !CommandTerminal.active && !RenameFile.added && !RightClickOption.isRightClickActive()) {
			g.setColor(Colors.explorerLight);
			g.fillRect(0, y, Main.explorer.getWidth(), height);
		}
		
		if (regent.isDirectory()) {
			Fonts.drawString(regent.getName(), x + 40, y + 4, new IDEFont(Fonts.lightGrayNormal, 16), width, g);
			
			g.drawImage(Main.folder, x + 6, y, height - 5, height - 5, null);
		}
		else if (regent.isFile()) {
			Fonts.drawString(regent.getName(), x + 40, y + 4, new IDEFont(Fonts.lightGrayNormal, 16), width, g);
			
			String extension = getFileExtension(regent);
			
			for (FileType f : types) {
				if (f.getExtension().equalsIgnoreCase(extension)) {
					g.drawImage(f.getIcon(), x + 5, y, height, height, null);
					
					return;
				}
				
				else if (f.getExtension().equalsIgnoreCase(regent.getName())) {
					g.drawImage(f.getIcon(), x + 5, y, height, height, null);
					
					return;
				}
			}
			g.drawImage(Main.spritesheet.getSprite(0, 64, 16, 16), x + 5, y, height, height, null);
		}
	}
}
