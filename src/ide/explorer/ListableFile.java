package ide.explorer;

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.ExecuteCommand;
import ide.components.IDEComponent;
import ide.components.SetFileName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.MouseInput;
import ide.main.Main;
import ide.util.Colors;

public class ListableFile extends IDEComponent implements ExecuteCommand, Serializable {
	
	private static final long serialVersionUID = 1L;

	public static transient FileType[] types = {
			new FileType(".java", Main.spritesheet.getSprite (0, 16, 16, 16)),
			new FileType(".class", Main.spritesheet.getSprite(0, 16, 16, 16)),
			new FileType(".c", Main.spritesheet.getSprite   (16, 16, 16, 16)),
			new FileType(".cpp", Main.spritesheet.getSprite (32, 16, 16, 16)),
			new FileType(".cxx", Main.spritesheet.getSprite (32, 16, 16, 16)),
			new FileType(".cs", Main.spritesheet.getSprite  (48, 16, 16, 16)),
			new FileType(".py", Main.spritesheet.getSprite  (64, 16, 16, 16)),
			new FileType(".js", Main.spritesheet.getSprite  (80, 16, 16, 16)),
			new FileType(".bat", Main.spritesheet.getSprite (96, 16, 16, 16)),
			new FileType(".com", Main.spritesheet.getSprite (96, 16, 16, 16)),
			new FileType(".h", Main.spritesheet.getSprite  (112, 16, 16, 16)),
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
			
			new FileType(".html", Main.spritesheet.getSprite (0, 32, 16, 16)),
			new FileType(".htm", Main.spritesheet.getSprite  (0, 32, 16, 16)),
			new FileType(".css", Main.spritesheet.getSprite (16, 32, 16, 16)),
			new FileType(".xml", Main.spritesheet.getSprite (32, 32, 16, 16)),
			new FileType(".json", Main.spritesheet.getSprite(48, 32, 16, 16)),
			new FileType(".md", Main.spritesheet.getSprite  (64, 32, 16, 16)),
			new FileType(".txt", Main.spritesheet.getSprite (80, 32, 16, 16)),
			new FileType(".log", Main.spritesheet.getSprite (80, 32, 16, 16)),
			new FileType(".pdf", Main.spritesheet.getSprite (96, 32, 16, 16)),
			new FileType(".jar", Main.spritesheet.getSprite(112, 32, 16, 16)),
			new FileType(".exe", Main.spritesheet.getSprite(128, 32, 16, 16)),
			new FileType(".svg", Main.spritesheet.getSprite(144, 32, 16, 16)),
			new FileType(".urna",Main.spritesheet.getSprite(160, 32, 16, 16)),		// easter egg! (Criador de Urnas)
			new FileType(".save",Main.spritesheet.getSprite(176, 32, 16, 16)),		// easter egg! (World's Hardest Game Maker 2)
			new FileType(".conf",Main.spritesheet.getSprite(192, 32, 16, 16)),
			new FileType(".mk", Main.spritesheet.getSprite (208, 32, 16, 16)),
			new FileType(".make",Main.spritesheet.getSprite(208, 32, 16, 16)),
			new FileType(".sh", Main.spritesheet.getSprite (224, 32, 16, 16)),
			new FileType(".gitignore",Main.spritesheet.getSprite(240,32,16,16)),
			new FileType(".dockerfile",Main.spritesheet.getSprite(256,32,16,16)),
			
			new FileType(".png", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".jpg", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".jpeg", Main.spritesheet.getSprite (0, 48, 16, 16)),
			new FileType(".gif", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			new FileType(".bmp", Main.spritesheet.getSprite  (0, 48, 16, 16)),
			
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
			
			// Specials
			
			new FileType("makefile",Main.spritesheet.getSprite(208, 32, 16, 16)),
			new FileType("dockerfile",Main.spritesheet.getSprite(256, 32, 16, 16)),
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
	
	@Override
	public String toString() {
		return "ListableFile: [parent: " + parent + ", regent: " + regent + "]";
	}
	
	public static String getFileExtension(File file) { // Fonte: StackOverflow
	    String name = file.getName();
	    int lastIndexOf = name.lastIndexOf(".");
	    
	    if (lastIndexOf == -1) {
	        return ""; // empty extension
	    }
	    return name.substring(lastIndexOf);
	}
	
	/**
	 * Retorna true ou false se o caminho especificado em path é um caminho válido.
	 * 
	 * @param path - O caminho
	 * @return true, se é um caminho válido, false se não.
	 */
	public static boolean isPath(String path) {
		try {
			Paths.get(path);
		} catch (InvalidPathException | NullPointerException e) {
			return false;
		}
		
		return true;
	}
	
	public static ListableFile search(File regent) {
		for (ListableFile l : Explorer.files) {
			if (l.getRegent().equals(regent))
				return l;
		}
		System.out.println("não achei nada");
		return null;
	}
	
	public static ListableFile search(File regent, File folder) { // deu certo pq ficou pegando o parent sempre da pasta que está o scope, e vai indo até a pasta base
		ListableFile prdoparent = folder.getParentFile().getAbsolutePath().equals(Main.baseFolder.getAbsolutePath()) ? new ListableFile(0, 0, 0, 0, Main.baseFolder, null) : new ListableFile(0, 0, 0, 0, folder.getParentFile(), null);
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
		System.out.println("não achei nada");
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
		String s = pathStr + pathStr.contains(".conf") != null ? "" : ".conf";
		
		System.out.println(s);
		
		Path path = Paths.get(s);
		
		try {
			BufferedWriter w = Files.newBufferedWriter(path, StandardCharsets.UTF_8); //new BufferedWriter(new FileWriter(s));
			
			w.write("Arquivo de Configurações da Boot IDE\n");
			w.write("\n");
			w.write("- Colors\n");
			w.write("\n");
			w.write("background: default\n");
			w.write("backgroundLight: default\n");
			w.write("explorer: default\n");
			w.write("explorerLight: default\n");
			w.write("textLight: default\n");
			w.write("textLighter: default\n");
			w.write("objects: default\n");
			w.write("methods: default\n");
			w.write("numbers: default\n");
			w.write("keywords: default\n");
			w.write("variables: default\n");
			w.write("comments: default\n");
			w.write("strings: default\n");
			w.write("generics: default\n");
			w.write("select1: default\n");
			w.write("select2: default\n");
			w.write("selectCursor: default\n");
			w.write("other: default\n");
			w.write("\n");
			w.write("- Files\n");
			w.write("\n");
			w.write("spritesheet: default\n");
			w.write("font-normal: default\n");
			w.write("font-bold: default\n");
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
			
			w.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readConfigFile(String path) {
		File f = new File(path);
		Path p = f.toPath();
		
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
				
				Colors.background = Color.decode(split[1]);
				
				break;
				
			case "backgroundLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.backgroundLight = Color.decode(split[1]);
				
				break;
				
			case "explorer:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.explorer = Color.decode(split[1]);
				
				break;
				
			case "explorerLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.explorerLight = Color.decode(split[1]);
				
				break;
				
			case "textLight:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.textLight = Color.decode(split[1]);
				
				break;
				
			case "textLighter:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.textLighter = Color.decode(split[1]);
				
				break;
				
			case "objects:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.objects = Color.decode(split[1]);
				
				break;
				
			case "methods:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.methods = Color.decode(split[1]);
				
				break;
				
			case "numbers:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.numbers = Color.decode(split[1]);
				
				break;
				
			case "keywords:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.keywords = Color.decode(split[1]);
				
				break;
				
			case "variables:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.variables = Color.decode(split[1]);
				
				break;
				
			case "comments:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.comments = Color.decode(split[1]);
				
				break;
				
			case "strings:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.strings = Color.decode(split[1]);
				
				break;
				
			case "symbols:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.symbols = Color.decode(split[1]);
				
				break;
				
			case "select1:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.select1 = Color.decode(split[1]);
				
				break;
				
			case "select2:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.select2 = Color.decode(split[1]);
				
				break;
				
			case "selectCursor:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.selectCursor = Color.decode(split[1]);
				
				break;
				
			case "other:":
				if (split[1].equals("default")) break;
				
				if (!split[1].startsWith("#")) split[1] = "#" + split[1];
				
				Colors.other = Color.decode(split[1]);
				
				break;
				
				////////
				
			case "spritesheet:":
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
				
				break;
				
			case "font_size:":
				if (split[1].equals("default")) break;
				
				int size = Integer.parseInt(split[1]);
				
				CodeEditor.FONT_SIZE = size;
				
				break;
			}
		}
	}
	
	public static List<ListableFile> loadFolder(ListableFile folder) {
		Explorer.scope = folder;
		
		List<ListableFile> files = new ArrayList<>();
		
		if (folder != null) {
			if (folder.regent.isDirectory()) {
				int index = 0;
				
				for (File f : folder.regent.listFiles()) {
					files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, folder));
					
					index++;
				}
			}
		}
		else {
			int index = 0;
			
			for (File f : Main.baseFolder.listFiles()) {
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
			String[] options = { "Sim", "Não" };
			
			int selectedOption = JOptionPane.showOptionDialog(null, "Tem certeza de que deseja deletar esse arquivo?", "Confirmar Exclusão", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
			
			if (selectedOption != 0) break;
			
			if (!regent.delete())
				JOptionPane.showMessageDialog(null, "Ocorreu um erro ao deletar. Lembre-se que pastas não podem ser excluídas se não estiverem vazias!", "Não foi possível deletar.", JOptionPane.OK_OPTION);
			
			for (Tab t : CodeEditor.tabs)
				if (t.getRegent().equals(this)) t.close();
			
			IDEComponent.toRemove.add(this);
				
			Explorer.files.clear();
			ListableFile.files.clear();
			
			Explorer.files = ListableFile.loadFolder(Explorer.scope);
			break;
			
		case "run":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start", regent.getName());
				File dir = Explorer.scope != null ? Explorer.scope.regent : new File(Explorer.getScopePath());
				
				pb.directory(dir);
				
				pb.start();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "runbash":
			try {
				ProcessBuilder pb = new ProcessBuilder("sh", "-c", "start", regent.getName());
				File dir = Explorer.scope != null ? Explorer.scope.regent : new File(Explorer.getScopePath());
				
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
				Runtime.getRuntime().exec("explorer.exe /select," + regent.getPath());
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
		}
	}
	
	public void tick() {
		if (CommandTerminal.expOff) return;
		
		if (!regent.exists() && CodeEditor.tabs != null) {
			Explorer.toRemove.add(this);
			
			for (Tab t : CodeEditor.tabs)
				if (t.getRegent().getRegent().getPath().equals(this.regent.getPath()))
					t.close();
				
			Explorer.toRemove.addAll(Explorer.files);
			ListableFile.files.clear();
			
			files = ListableFile.loadFolder(Explorer.scope);
		}
		
		if (hovered())
			Explorer.hoveringListableFile = true;
		
		if (leftClicked()) {
			MouseInput.updateMouse();
			
			if (getFileExtension(regent).equals(".png") ||
					getFileExtension(regent).equals(".jpg") ||
					getFileExtension(regent).equals(".jpeg")||
					getFileExtension(regent).equals(".png") ||
					getFileExtension(regent).equals(".gif") ||
					getFileExtension(regent).equals(".bmp") ||
					getFileExtension(regent).equals(".wav") ||
					getFileExtension(regent).equals(".mp3") ||
					getFileExtension(regent).equals(".ogg") ||
					getFileExtension(regent).equals(".mp4") ||
					getFileExtension(regent).equals(".wmv") ||
					getFileExtension(regent).equals(".avi")) {
					try {
						Main.desktop.open(regent);
					} catch (IOException e) {
						e.printStackTrace();
					}
					
					return;
				}
			
			if (y > 199 && regent.isDirectory()) {
				files = loadFolder(this);
				
				if (files.size() == 0)
					Explorer.toRemove.addAll(Explorer.files);
			}
			
			if (regent.isFile() && CodeEditor.tabs != null) {
				int lastX = CodeEditor.tabs.size() > 0 ? CodeEditor.tabs.get(CodeEditor.tabs.size() - 1).getX() : Tab.MIN_X;
				
				new Thread() {
					public void run() {
						try {
							CodeEditor.lines = CodeEditor.readFile(regent);
						} catch (IOException e) {
							JOptionPane.showMessageDialog(null, "Esse arquivo não é suportado, por favor escolha outro. \n Pode ser que esse arquivo seja codificado em um formato diferente do que UTF-8 ou ele seja binário.", "Esse arquivo não é compatível", JOptionPane.OK_OPTION);
							
							return;
						}
					}
				}.start();
				
				Tab toAdd = new Tab((lastX + Tab.WIDTH) + 3, this);
				
				CodeEditor.cursorX = 0;
				CodeEditor.cursorY = 1;
				
				CodeEditor.scrY = 0;
				
				for (Tab t : CodeEditor.tabs)
					if (t.getRegent().getRegent().getPath().equals(this.regent.getPath())) {
						CodeEditor.editing = t;
						
						return;
					}
				
				CodeEditor.toAdd.add(toAdd);
				CodeEditor.editing = toAdd;
			}
		}
		
		if (rightClicked()) {
			MouseInput.updateMouse();
			
			IDEComponent.addRightClickOption((x + width), y, 540, "Deletar", (s) -> execute(s), "del");
			IDEComponent.addRightClickOption((x + width), y + 30, 540, "Abrir Prompt de Comando", (s) -> execute(s), "cmd");
			IDEComponent.addRightClickOption((x + width), y + 60, 540, "Abrir Terminal de Comando", (s) -> execute(s), "term");
			IDEComponent.addRightClickOption((x + width), y + 90, 540, "Abrir no Explorador de Arquivos", (s) -> execute(s), "sysexp");
			IDEComponent.addRightClickOption((x + width), y + 120, 540, "Definir pasta atual como Pasta Base", (s) -> execute(s), "setbase");
			
			boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
			
			if (getFileExtension(regent).equals(".bat") && isWindows)
				IDEComponent.addRightClickOption((x + width), y + 150, 540, "Executar", (s) -> execute(s), "run");
			
			if (getFileExtension(regent).equals(".sh") && !isWindows)
				IDEComponent.addRightClickOption((x + width), y + 150, 540, "Executar", (s) -> execute(s), "runbash");
		}
		
		int index = Explorer.files.indexOf(this);
		
		if (index <= 0) return;
		
		y = Explorer.files.get(index - 1).y + height;
	}
	
	public void render(Graphics g) {
		if (y < 200 || y > Main.screen.getHeight()) return;
		
		if (CommandTerminal.expOff) return;
		
		if (y < 199) return;
		
		if (hovered() && !SetFileName.added && !CommandTerminal.active && !CodeEditor.selectMode) {
			g.setColor(Colors.explorerLight);
			g.fillRect(0, y, Main.explorer.getWidth(), height);
		}
		
		if (regent.isDirectory()) {
			Fonts.drawString(regent.getName(), x + 40, y + 4, new IDEFont(Fonts.lightGrayNormal, 16), width, g);
			
			g.drawImage(Main.spritesheet.getSprite(48, 0, 16, 16), x + 6, y, height - 5, height - 5, null);
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
