package ide.explorer;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.LineEnding;
import ide.codeeditor.Tab;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.MessageBox;
import ide.components.ReloadButton;
import ide.components.RenameFile;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorercomponents.Execute;
import ide.explorercomponents.SetBranchName;
import ide.explorercomponents.SetCommitName;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.git.GitCore;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.main.OS;
import ide.screen.Screen;
import ide.terminal.TerminalCore;
import ide.util.Colors;
import ide.util.ExecuteCommand;
import ide.util.Language;
import ide.util.Texts;

public class ListableFile extends IDEComponent implements ExecuteCommand {

	public static boolean hasAltered = false;
	
	public static boolean cutFlag = false;
	public static File copy;
	
	public static FileType[] types = new FileType[] {};

	private ListableFile parent;
	private File regent;

	public static List<ListableFile> files = new ArrayList<ListableFile>(Explorer.files);

	public ListableFile(int x, int y, int width, int height, File regent, ListableFile parent) {
		super(x, y, width, height, null);

		this.regent = regent;
		this.parent = parent;

		// initTypes();
	}

	public static void updateTypes() {
			List<FileType> t = new ArrayList<>();
			
			t.add(new FileType("readme.md",Main.spritesheet.getSprite(752,32,16,16)));
			t.add(new FileType("package.json",Main.spritesheet.getSprite(576,16,16,16)));
			t.add(new FileType("package-lock.json",Main.spritesheet.getSprite(576,16,16,16)));
			
			t.add(new FileType(".java", Main.spritesheet.getSprite (0, 16, 16, 16)));
			t.add(new FileType(".class",Main.spritesheet.getSprite(272,32, 16, 16)));
			t.add(new FileType(".c", Main.spritesheet.getSprite   (16, 16, 16, 16)));
			t.add(new FileType(".cpp", Main.spritesheet.getSprite (32, 16, 16, 16)));
			t.add(new FileType(".cc", Main.spritesheet.getSprite  (32, 16, 16, 16)));
			t.add(new FileType(".cxx", Main.spritesheet.getSprite (32, 16, 16, 16)));
			t.add(new FileType(".cs", Main.spritesheet.getSprite  (48, 16, 16, 16)));
			t.add(new FileType(".py", Main.spritesheet.getSprite  (64, 16, 16, 16)));
			t.add(new FileType(".pyi", Main.spritesheet.getSprite (64, 16, 16, 16)));
			t.add(new FileType(".pyd", Main.spritesheet.getSprite (64, 16, 16, 16)));
			t.add(new FileType(".pyx", Main.spritesheet.getSprite (64, 16, 16, 16)));
			t.add( new FileType(".js", Main.spritesheet.getSprite (80, 16, 16, 16)));
			t.add( new FileType(".mjs", Main.spritesheet.getSprite(80, 16, 16, 16)));
			t.add(new FileType(".bat", Main.spritesheet.getSprite (96, 16, 16, 16)));
			t.add(new FileType(".com", Main.spritesheet.getSprite(592, 32, 16, 16)));
			t.add(new FileType(".cmd", Main.spritesheet.getSprite(592, 32, 16, 16)));
			t.add(new FileType(".h", Main.spritesheet.getSprite  (112, 16, 16, 16)));
			t.add(new FileType(".hh", Main.spritesheet.getSprite (112, 16, 16, 16)));
			t.add(new FileType(".hxx", Main.spritesheet.getSprite(112, 16, 16, 16)));
			t.add(new FileType(".hpp", Main.spritesheet.getSprite(112, 16, 16, 16)));
			t.add(new FileType(".asm", Main.spritesheet.getSprite(128, 16, 16, 16)));
			t.add(new FileType(".s", Main.spritesheet.getSprite  (128, 16, 16, 16)));
			t.add(new FileType(".lua", Main.spritesheet.getSprite(144, 16, 16, 16)));
			t.add(new FileType(".sql", Main.spritesheet.getSprite(160, 16, 16, 16)));
			t.add(new FileType(".swift",Main.spritesheet.getSprite(176,16, 16, 16)));
			t.add(new FileType(".rs", Main.spritesheet.getSprite (192, 16, 16, 16)));
			t.add(new FileType(".php", Main.spritesheet.getSprite(208, 16, 16, 16)));
			t.add(new FileType(".kt", Main.spritesheet.getSprite (224, 16, 16, 16)));
			t.add(new FileType(".vue", Main.spritesheet.getSprite(240, 16, 16, 16)));
			t.add(new FileType(".rb", Main.spritesheet.getSprite (256, 16, 16, 16)));
			t.add(new FileType(".ino", Main.spritesheet.getSprite(272, 16, 16, 16)));
			t.add(new FileType(".ts", Main.spritesheet.getSprite (288, 16, 16, 16)));
			t.add(new FileType(".tsx", Main.spritesheet.getSprite(544, 16, 16, 16)));
			t.add(new FileType(".go", Main.spritesheet.getSprite (304, 16, 16, 16)));
			t.add(new FileType(".r",  Main.spritesheet.getSprite (320, 16, 16, 16)));
			t.add(new FileType(".jl", Main.spritesheet.getSprite (336, 16, 16, 16)));
			t.add(new FileType(".pl", Main.spritesheet.getSprite (352, 16, 16, 16)));
			t.add(new FileType(".ts", Main.spritesheet.getSprite (352, 16, 16, 16)));
			t.add(new FileType(".has", Main.spritesheet.getSprite(368, 16, 16, 16)));
			t.add(new FileType(".hs", Main.spritesheet.getSprite (368, 16, 16, 16)));
			t.add(new FileType(".fs", Main.spritesheet.getSprite (384, 16, 16, 16)));
			t.add(new FileType(".coffee",Main.spritesheet.getSprite(400,16,16, 16)));
			t.add(new FileType(".m", Main.spritesheet.getSprite  (416, 16, 16, 16)));
			t.add(new FileType(".mm", Main.spritesheet.getSprite (592, 16, 16, 16)));
			t.add(new FileType(".pas", Main.spritesheet.getSprite(432, 16, 16, 16)));
			t.add(new FileType(".lpr", Main.spritesheet.getSprite(432, 16, 16, 16)));
			t.add(new FileType(".pp", Main.spritesheet.getSprite (432, 16, 16, 16)));
			t.add(new FileType(".scala",Main.spritesheet.getSprite(448,16, 16, 16)));
			t.add(new FileType(".dart",Main.spritesheet.getSprite(464, 16, 16, 16)));
			t.add(new FileType(".zig", Main.spritesheet.getSprite(480, 16, 16, 16)));
			t.add(new FileType(".scss",Main.spritesheet.getSprite(496, 16, 16, 16)));
			t.add(new FileType(".ipynb",Main.spritesheet.getSprite(512,16, 16, 16)));
			t.add(new FileType(".vb", Main.spritesheet.getSprite (528, 16, 16, 16)));
			t.add(new FileType(".bf", Main.spritesheet.getSprite (560, 16, 16, 16)));
			t.add(new FileType(".gd", Main.spritesheet.getSprite (608, 16, 16, 16)));
			t.add(new FileType(".mcfunction",Main.spritesheet.getSprite(624,16,16,16)));
			t.add(new FileType(".por", Main.spritesheet.getSprite(640, 16, 16, 16)));
			t.add(new FileType(".cmxa",Main.spritesheet.getSprite(656, 16, 16, 16)));
			t.add(new FileType(".ml", Main.spritesheet.getSprite (656, 16, 16, 16)));
			t.add(new FileType(".mli", Main.spritesheet.getSprite(656, 16, 16, 16)));
			t.add(new FileType(".mly", Main.spritesheet.getSprite(656, 16, 16, 16)));
			t.add(new FileType(".cmt", Main.spritesheet.getSprite(656, 16, 16, 16)));
			t.add(new FileType(".vbs", Main.spritesheet.getSprite(672, 16, 16, 16)));
			t.add(new FileType(".v", Main.spritesheet.getSprite  (688, 16, 16, 16)));
			t.add(new FileType(".vh", Main.spritesheet.getSprite (688, 16, 16, 16)));
			t.add(new FileType(".vsh", Main.spritesheet.getSprite(688, 16, 16, 16)));
			t.add(new FileType(".bas", Main.spritesheet.getSprite(704, 16, 16, 16)));
			t.add(new FileType(".sm", Main.spritesheet.getSprite (720, 16, 16, 16)));
			
			t.add(new FileType(".html", Main.spritesheet.getSprite (0, 32, 16, 16)));
			t.add(new FileType(".xhtml", Main.spritesheet.getSprite(0, 32, 16, 16)));
			t.add(new FileType(".htm", Main.spritesheet.getSprite  (0, 32, 16, 16)));
			t.add(new FileType(".css", Main.spritesheet.getSprite (16, 32, 16, 16)));
			t.add(new FileType(".xml", Main.spritesheet.getSprite (32, 32, 16, 16)));
			t.add(new FileType(".json", Main.spritesheet.getSprite(48, 32, 16, 16)));
			t.add(new FileType(".jsonc",Main.spritesheet.getSprite(48, 32, 16, 16)));
			t.add(new FileType(".md", Main.spritesheet.getSprite  (64, 32, 16, 16)));
			t.add(new FileType(".markdown",Main.spritesheet.getSprite(64,32,16,16)));
			t.add(new FileType(".txt", Main.spritesheet.getSprite (80, 32, 16, 16)));
			t.add(new FileType(".log", Main.spritesheet.getSprite (80, 32, 16, 16)));
			t.add(new FileType(".pdf", Main.spritesheet.getSprite (96, 32, 16, 16)));
			t.add(new FileType(".jar", Main.spritesheet.getSprite(112, 32, 16, 16)));
			t.add(new FileType(".exe", Main.spritesheet.getSprite(128, 32, 16, 16)));
			t.add(new FileType(".svg", Main.spritesheet.getSprite(144, 32, 16, 16)));
			t.add(new FileType(".urna",Main.spritesheet.getSprite(160, 32, 16, 16)));		// easter egg! (Criador de Urnas)
			t.add(new FileType(".save",Main.spritesheet.getSprite(176, 32, 16, 16)));		// easter egg! (World's Hardest Game Maker 2)
			t.add(new FileType(Main.CONFIG_FILE_EXTENSION, Main.spritesheet.getSprite(192, 32, 16, 16)));
			t.add(new FileType(".mk", Main.spritesheet.getSprite (208, 32, 16, 16)));
			t.add(new FileType(".mak", Main.spritesheet.getSprite (208, 32, 16, 16)));
			t.add(new FileType(".make",Main.spritesheet.getSprite(208, 32, 16, 16)));
			t.add(new FileType(".sh", Main.spritesheet.getSprite (224, 32, 16, 16)));
			t.add(new FileType(".gitignore",Main.spritesheet.getSprite(240,32,16,16)));
			t.add(new FileType(".dockerfile",Main.spritesheet.getSprite(256,32,16,16)));
			t.add(new FileType(".jsx", Main.spritesheet.getSprite(368, 32, 16, 16)));
			t.add(new FileType(".config",Main.spritesheet.getSprite(352,32,16, 16)));
			t.add(new FileType(".cfg", Main.spritesheet.getSprite (352, 32,16, 16)));
			t.add(new FileType(".ps1", Main.spritesheet.getSprite(320, 32, 16, 16)));
			t.add(new FileType(".license",Main.spritesheet.getSprite(336,32,16,16)));
			t.add(new FileType(".docx",Main.spritesheet.getSprite(384, 32, 16, 16)));
			t.add(new FileType(".xlsx",Main.spritesheet.getSprite(400, 32, 16, 16)));
			t.add(new FileType(".docx",Main.spritesheet.getSprite(384, 32, 16, 16)));
			t.add(new FileType(".pptx",Main.spritesheet.getSprite(416, 32, 16, 16)));
			t.add(new FileType(".one", Main.spritesheet.getSprite(432, 32, 16, 16)));
			t.add(new FileType(".psd",Main.spritesheet.getSprite (448, 32, 16, 16)));
			t.add(new FileType(".aed",Main.spritesheet.getSprite (464, 32, 16, 16)));
			t.add(new FileType(".ai", Main.spritesheet.getSprite (480, 32, 16, 16)));
			t.add(new FileType(".indd",Main.spritesheet.getSprite(496, 32, 16, 16)));
			t.add(new FileType(".ejs", Main.spritesheet.getSprite(512, 32, 16, 16)));
			t.add(new FileType(".ld", Main.spritesheet.getSprite (528, 32, 16, 16)));
			t.add(new FileType(".lock",Main.spritesheet.getSprite(544, 32, 16, 16)));
			t.add(new FileType(".ini", Main.spritesheet.getSprite(560, 32, 16, 16)));
			t.add(new FileType(".dll", Main.spritesheet.getSprite(576, 32, 16, 16)));
			t.add(new FileType(".makefile",Main.spritesheet.getSprite(208,32,16,16)));
			t.add(new FileType(".url", Main.spritesheet.getSprite(608, 32, 16, 16)));
			t.add(new FileType(".prefs",Main.spritesheet.getSprite(688,32, 16, 16)));
			t.add(new FileType(".classpath",Main.spritesheet.getSprite(704,32,16,16)));
			t.add(new FileType(".project",Main.spritesheet.getSprite(720,32,16,16)));
			t.add(new FileType(".csproj", Main.spritesheet.getSprite(768,32,16,16)));
			t.add(new FileType(Main.SETTINGS_FILE_EXTENSION, Main.spritesheet.getSprite(192,32,16,16)));
			t.add(new FileType(".rtf", Main.spritesheet.getSprite(784, 32, 16, 16)));
			t.add(new FileType(".bashrc", Main.spritesheet.getSprite(800,32,16,16)));
			t.add(new FileType(".bash_profile",Main.spritesheet.getSprite(800,32,16,16)));
			t.add(new FileType(".toml",Main.spritesheet.getSprite(352, 32, 16, 16)));
			t.add(new FileType(".svelte", Main.spritesheet.getSprite(816,32,16,16)));
			t.add(new FileType(".tf", Main.spritesheet.getSprite (832, 32, 16, 16)));
			t.add(new FileType(".mod", Main.spritesheet.getSprite(848, 32, 16, 16)));
			t.add(new FileType(".db",  Main.spritesheet.getSprite(160, 16, 16, 16)));
			t.add(new FileType(".yml", Main.spritesheet.getSprite(864, 32, 16, 16)));
			t.add(new FileType(".yaml",Main.spritesheet.getSprite(864, 32, 16, 16)));
			t.add(new FileType(".hex", Main.spritesheet.getSprite(880, 32, 16, 16)));
			t.add(new FileType(".sum", Main.spritesheet.getSprite(848, 32, 16, 16))); 
			
			t.add(new FileType(".png", Main.spritesheet.getSprite  (0, 48, 16, 16)));
			t.add(new FileType(".jpg", Main.spritesheet.getSprite  (0, 48, 16, 16)));
			t.add(new FileType(".jpeg", Main.spritesheet.getSprite (0, 48, 16, 16)));
			t.add(new FileType(".gif", Main.spritesheet.getSprite  (0, 48, 16, 16)));
			t.add(new FileType(".bmp", Main.spritesheet.getSprite  (0, 48, 16, 16)));
			t.add(new FileType(".ico", Main.spritesheet.getSprite (64, 48, 16, 16)));
			t.add(new FileType(".webp", Main.spritesheet.getSprite (0, 48, 16, 16)));
			
			t.add(new FileType(".mp4", Main.spritesheet.getSprite (16, 48, 16, 16)));
			t.add(new FileType(".wmv", Main.spritesheet.getSprite (16, 48, 16, 16)));
			t.add(new FileType(".avi", Main.spritesheet.getSprite (16, 48, 16, 16)));
			
			t.add(new FileType(".wav", Main.spritesheet.getSprite (32, 48, 16, 16)));
			t.add(new FileType(".mp3", Main.spritesheet.getSprite (32, 48, 16, 16)));
			t.add(new FileType(".ogg", Main.spritesheet.getSprite (32, 48, 16, 16)));
			
			t.add(new FileType(".otf", Main.spritesheet.getSprite (48, 48, 16, 16)));
			t.add(new FileType(".ttf", Main.spritesheet.getSprite (48, 48, 16, 16)));
			t.add(new FileType(".woff",Main.spritesheet.getSprite (48, 48, 16, 16)));
			t.add(new FileType(".woff2",Main.spritesheet.getSprite(48, 48, 16, 16)));
			
			t.add(new FileType(".zip", Main.spritesheet.getSprite(288, 32, 16, 16)));
			t.add(new FileType(".gz",  Main.spritesheet.getSprite(288, 32, 16, 16)));
			t.add(new FileType(".rar", Main.spritesheet.getSprite(288, 32, 16, 16)));
			t.add(new FileType(".7z", Main.spritesheet.getSprite (288, 32, 16, 16)));
		
			t.add(new FileType(".bin", Main.spritesheet.getSprite(304, 32, 16, 16)));
			t.add(new FileType(".img", Main.spritesheet.getSprite(640, 32, 16, 16)));
			t.add(new FileType(".iso", Main.spritesheet.getSprite(640, 32, 16, 16)));
			t.add(new FileType(".flp", Main.spritesheet.getSprite(656, 32, 16, 16)));
			t.add(new FileType(".o",  Main.spritesheet.getSprite (672, 32, 16, 16)));
			t.add(new FileType(".out", Main.spritesheet.getSprite(672, 32, 16, 16)));
			t.add(new FileType(".obj", Main.spritesheet.getSprite(672, 32, 16, 16)));
			
			// Specials
			
			t.add(new FileType("makefile",Main.spritesheet.getSprite(208, 32, 16, 16)));
			t.add(new FileType("dockerfile",Main.spritesheet.getSprite(256,32,16, 16)));
			t.add(new FileType("license", Main.spritesheet.getSprite(336, 32, 16, 16)));
			t.add(new FileType("authors", Main.spritesheet.getSprite(624, 32, 16, 16)));
			t.add(new FileType("gitignore", Main.spritesheet.getSprite (240,32,16,16)));
			
			types = t.toArray(new FileType[t.size()]);
	}

	public static <T> T[] initArray(T[] arr) {
		return arr;
	}

	public ListableFile getParent() {
		return parent;
	}

	public File getRegent() {
		return regent;
	}

	public void setRegent(File regent) {
		this.regent = regent;
	}

	public void setParent(ListableFile parent) {
		this.parent = parent;
	}

	@Override
	public String toString() {
		return "ListableFile: [parent: " + parent + ", regent: " + regent + "]";
	}
	/*
	 * Fazer nao renderizar nem tickar ListableFiles nem Tabs fora da tela
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

	public static String getFileExtension(String name) { // Fonte: StackOverflow
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
	 * Retorna true ou false se o caminho especificado em path a um caminho valido,
	 * ou seja, se o arquivo existe.
	 * 
	 * @param path - O caminho
	 * @return true, se a um caminho valido, false se nao.
	 */
	public static boolean isPath(String path) {
		return new File(path).exists();
	}

	public static ListableFile search(File regent) {
		for (ListableFile l : Explorer.files) {
			if (l.getRegent().equals(regent))
				return l;
		}

		// Nao achei nada
		return null;
	}

	public static ListableFile searchListableFiles(File regent) {
		for (ListableFile l : files) {
			if (l.getRegent().equals(regent))
				return l;
		}

		// Nao achei nada
		return null;
	}

	public static boolean hasDuplicateFileNames(String name, File folder) {
		String[] list = folder.list();

		if (list == null)
			list = new String[0];

		for (String s : list) {
			if (s.equals(name))
				return true;
		}

		return false;
	}

	public static ListableFile search(File regent, File folder) { // deu certo pq ficou pegando o parent sempre da pasta
																	// que esta o scope, e vai indo ata a pasta base
		ListableFile prdoprdoparent = folder.getParentFile().getAbsolutePath().equals(Main.baseFolder.getAbsolutePath())
				? new ListableFile(0, 0, 0, 0, Main.baseFolder, null)
				: new ListableFile(0, 0, 0, 0, folder.getParentFile().getParentFile(), null);
		ListableFile prdoparent = folder.getParentFile().getAbsolutePath().equals(Main.baseFolder.getAbsolutePath())
				? new ListableFile(0, 0, 0, 0, Main.baseFolder, null)
				: new ListableFile(0, 0, 0, 0, folder.getParentFile(), prdoprdoparent);
		ListableFile parent = new ListableFile(0, 0, 0, 0, folder, prdoparent); // o parent nao precisa ter outro
																				// parent, ou precisa?

		if (folder.getAbsolutePath().equals(Main.baseFolder.getAbsolutePath())) // se o parent for a pasta base, defina
																				// o
			parent = null; // parent como null.

		List<ListableFile> fl = new ArrayList<>();

		int index = 0;

		File[] listFiles = folder.listFiles() == null ? new File[0] : folder.listFiles();

		for (File f : listFiles) {
			fl.add(new ListableFile(0, Explorer.MINIMUM_Y + (index * 30), Main.explorer.getWidth(), 30, f, parent));

			index++;
		}

		for (ListableFile l : fl) {
			if (l.getRegent().equals(regent))
				return l;
		}

		// Não achei nada
		return null;
	}

	/*
	 * Como é composto o Arquivo:
	 * 
	 * Arquivo de Configurações para Boot IDE
	 * 
	 * - Colors
	 * 
	 * background: default backgroundLight: default explorer: default explorerLight:
	 * default textLight: default textLighter: default objects: default methods:
	 * default numbers: default keywords: default variables: default comments:
	 * default strings: default generics: default select1: default select2: default
	 * 
	 * - Files
	 * 
	 * spritesheet: default font-normal: default font-bold: default
	 * 
	 * - Color Mode
	 * 
	 * objects: normal methods: normal numbers: normal keywords: normal variables:
	 * normal comments: normal strings: normal generics: normal
	 * 
	 * - Settings
	 * 
	 * Lembrar de abas quando fechar a Boot IDE: true Lembrar do arquivo de
	 * configuraaaes: true
	 * 
	 * Colorir Objetos: true Colorir Matodos: true Colorir Nameros: true Colorir
	 * Palavras-chave: true Colorir Variaveis: true Colorir Comentarios: true
	 * Colorir Strings: true Colorir Genaricos: true
	 */

	public static void generateConfigFile(File file) {
		String pathStr = file.getAbsolutePath();
		String s = pathStr.contains(Main.CONFIG_FILE_EXTENSION) ? pathStr + "" : pathStr + Main.CONFIG_FILE_EXTENSION;

		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(s));

			w.write("Boot IDE Configuration File\n");
			w.write("\n");
			w.write("Colors\n");
			w.write("\n");
			w.write("background: #353b48\n");
			w.write("background2: #29394a\n");
			w.write("background_light: #28394d\n");
			w.write("explorer: #222f3e\n");
			w.write("code_editor: #222f3e\n");
			w.write("explorer_light: #2d3f54\n");
			w.write("explorer_lighter: #354d69\n");
			w.write("text_light: #95afc0\n");
			w.write("text_lighter: #a9c4c2\n");
			w.write("objects: #94fa92\n");
			w.write("methods: #e7d789\n");
			w.write("numbers: #5485b6\n");
			w.write("keywords: #95bddc\n");
			w.write("variables: #80d1f2\n");
			w.write("comments: #a9c4c2\n");
			w.write("strings: #95afc0\n");
			w.write("symbols: #a9c4c2\n");
			w.write("cursor: #ffffff\n");
			w.write("selection: #8c8c8c\n");
			w.write("other: #ffffff\n");
			w.write("lower_bar: #28394d\n");
			w.write("error: #ff6961\n");
			w.write("line_number: #95afc0\n");
			w.write("selected_line_number: #c5d5ea\n");

			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateConfigFileLoaded(File file) {
		String pathStr = file.getAbsolutePath();
		String s = pathStr.contains(Main.CONFIG_FILE_EXTENSION) ? pathStr + "" : pathStr + Main.CONFIG_FILE_EXTENSION;

		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(s));

			w.write("Boot IDE Configuration File\n");
			w.write("\n");
			w.write("Colors\n");
			w.write("\n");
			w.write("background: " + Colors.toHex(Colors.background) + "\n");
			w.write("background2: " + Colors.toHex(Colors.background2) + "\n");
			w.write("background_light: " + Colors.toHex(Colors.backgroundLight) + "\n");
			w.write("explorer: " + Colors.toHex(Colors.explorer) + "\n");
			w.write("code_editor: " + Colors.toHex(Colors.codeEditor) + "\n");
			w.write("explorer_light: " + Colors.toHex(Colors.explorerLight) + "\n");
			w.write("explorer_lighter: " + Colors.toHex(Colors.explorerLighter) + "\n");
			w.write("text_light: " + Colors.toHex(Colors.textLight) + "\n");
			w.write("text_lighter: " + Colors.toHex(Colors.textLighter) + "\n");
			w.write("objects: " + Colors.toHex(Colors.objects) + "\n");
			w.write("methods: " + Colors.toHex(Colors.methods) + "\n");
			w.write("numbers: " + Colors.toHex(Colors.numbers) + "\n");
			w.write("keywords: " + Colors.toHex(Colors.keywords) + "\n");
			w.write("variables: " + Colors.toHex(Colors.variables) + "\n");
			w.write("comments: " + Colors.toHex(Colors.comments) + "\n");
			w.write("strings: " + Colors.toHex(Colors.strings) + "\n");
			w.write("symbols: " + Colors.toHex(Colors.symbols) + "\n");
			w.write("cursor: " + Colors.toHex(Colors.cursor) + "\n");
			w.write("selection: " + Colors.toHex(Colors.selection) + "\n");
			w.write("other: " + Colors.toHex(Colors.other) + "\n");
			w.write("lower_bar: " + Colors.toHex(Colors.lowerBar) + "\n");
			w.write("error: " + Colors.toHex(Colors.error) + "\n");
			w.write("line_number: " + Colors.toHex(Colors.lineNumber) + "\n");
			w.write("selected_line_number: " + Colors.toHex(Colors.selectedLineNumber) + "\n");

			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void generateLocalConfigFile(File file) {
		String pathStr = file.getAbsolutePath();
		String s = pathStr.contains(Main.CONFIG_FILE_EXTENSION) ? pathStr + "" : pathStr + Main.CONFIG_FILE_EXTENSION;

		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(s));

			w.write("Boot IDE Configuration File\n");
			w.write("\n");
			w.write("Colors\n");
			w.write("\n");
			w.write("background: " + Colors.toHex(Colors.background) + "\n");
			w.write("background2: " + Colors.toHex(Colors.background2) + "\n");
			w.write("background_light: " + Colors.toHex(Colors.backgroundLight) + "\n");
			w.write("explorer: " + Colors.toHex(Colors.explorer) + "\n");
			w.write("code_editor: " + Colors.toHex(Colors.codeEditor) + "\n");
			w.write("explorer_light: " + Colors.toHex(Colors.explorerLight) + "\n");
			w.write("explorer_lighter: " + Colors.toHex(Colors.explorerLighter) + "\n");
			w.write("text_light: " + Colors.toHex(Colors.textLight) + "\n");
			w.write("text_lighter: " + Colors.toHex(Colors.textLighter) + "\n");
			w.write("objects: " + Colors.toHex(Colors.objects) + "\n");
			w.write("methods: " + Colors.toHex(Colors.methods) + "\n");
			w.write("numbers: " + Colors.toHex(Colors.numbers) + "\n");
			w.write("keywords: " + Colors.toHex(Colors.keywords) + "\n");
			w.write("variables: " + Colors.toHex(Colors.variables) + "\n");
			w.write("comments: " + Colors.toHex(Colors.comments) + "\n");
			w.write("strings: " + Colors.toHex(Colors.strings) + "\n");
			w.write("symbols: " + Colors.toHex(Colors.symbols) + "\n");
			w.write("cursor: " + Colors.toHex(Colors.cursor) + "\n");
			w.write("selection: " + Colors.toHex(Colors.selection) + "\n");
			w.write("other: " + Colors.toHex(Colors.other) + "\n");
			w.write("lower_bar: " + Colors.toHex(Colors.lowerBar) + "\n");
			w.write("error: " + Colors.toHex(Colors.error) + "\n");
			w.write("line_number: " + Colors.toHex(Colors.lineNumber) + "\n");
			w.write("selected_line_number: " + Colors.toHex(Colors.selectedLineNumber) + "\n");
			w.write("\n");
			w.write("Settings\n");
			w.write("\n");
			w.write("font_size: " + CodeEditor.FONT_SIZE + "\n");
			w.write("language: " + Main.lang + "\n");
			w.write("autocomplete_active: " + CodeEditor.isAutoCompleteActive + "\n");
			w.write("indent_with_spaces: " + CodeEditor.indentSpaces + "\n");
			w.write("indent_length: " + CodeEditor.indentLength + "\n");
			w.write("automatically_open_tabs: " + CodeEditor.automaticallyOpenTabs + "\n");
			w.write("allow_animations: " + Explorer.allowAnimations + "\n");
			w.write("force_mac_buttons: " + Main.forceMacButtons + "\n");
			w.write("line_ending: " + CodeEditor.lineEnding + "\n");
			w.write("show_unsaved_title_bar: " + CodeEditor.showUnsavedTitleBar + "\n");
			w.write("use_antialiasing: " + Fonts.useAntiAliasing + "\n");
			w.write("show_caps_lock: " + CodeEditor.showCapsLock + "\n");
			w.write("checkout_to_created_branch: " + GitCore.checkoutToCreatedBranch + "\n");
			w.write("terminal_prompt: '" + TerminalCore.prompt + "'\n");
			w.write("program_name: '" + Main.PROGRAM_NAME + "'\n");
			w.write("font: '" + Main.fntnr + "'\n");
			w.write("editor_font: '" + Main.fnted + "'\n");
			w.write("\n");
			w.write("Advanced\n");
			w.write("\n");
			w.write("line_height_offset: " + CodeEditor.lineHeightOffset + "\n");
			w.write("char_width: " + Fonts.charWidth + "\n");
			w.write("hanging_letters_offset: " + Fonts.hangingLettersOffset + "\n");
			w.write("y_offset: " + Fonts.yOffset + "\n");

			w.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void readConfigFile(String path) {
		File f = new File(path);
		Path p = f.toPath();
		boolean readConfigs = false;

		if (!f.exists())
			CommandTerminal.runCommand("unloadconfigfile");
		
		// se esse config file for o da pasta do programa, pode ler as configurações dele, senão, não leia porque é um outro arquivo lido pelo loadconfigfile
		if (f.getParentFile().getAbsolutePath().equals(Main.userDir))
			readConfigs = true;
		
		hasAltered = false;

		List<String> lines = new ArrayList<>();

		try {
			lines = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
		} catch (Exception e) {
			try {
				lines = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
			} catch (Exception ff) {
			}
		}

		for (String s : lines) {
			// if (s.startsWith("-") || s.startsWith("\n")) continue;
			String[] split = s.split(" ");

			switch (split[0]) {
			case "background:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#353b48";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.background = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "background2:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#29394a";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.background2 = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "background_light:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#28394d";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.backgroundLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "explorer:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#222f3e";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.explorer = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;
				
			case "code_editor:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#222f3e";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.codeEditor = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "explorer_light:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#2d3f54";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.explorerLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "explorer_lighter:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#354d69";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.explorerLighter = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "text_light:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#95afc0";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.textLight = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "text_lighter:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#a9c4c2";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.textLighter = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "objects:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#94fa92";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.objects = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "methods:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#e7d789";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.methods = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "numbers:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] ="#5485b6";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.numbers = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "keywords:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#95bddc";
					break;
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.keywords = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "variables:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#80d1f2";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.variables = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "comments:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#a9c4c2";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.comments = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "strings:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#95afc0";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.strings = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "symbols:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#a9c4c2";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.symbols = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "selection:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#8c8c8c";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.selection = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "cursor:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#ffffff";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.cursor = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "other:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#ffffff";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.other = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "lower_bar:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#28394d";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.lowerBar = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "error:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#ff6961";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.error = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "line_number:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#95afc0";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.lineNumber = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			case "selected_line_number:":
				if (split[1].equalsIgnoreCase("default")) {
					split[1] = "#c5d5ea";
				}

				if (!split[1].startsWith("#"))
					split[1] = "#" + split[1];
				
				hasAltered = true;

				try {
					Colors.selectedLineNumber = Color.decode(split[1]);
				} catch (NumberFormatException e) {
					break;
				}

				break;

			// Settings

			case "font_size:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.FONT_SIZE = CodeEditor.DEFAULT_FONT_SIZE;
					break;
				}

				int size = 0;

				try {
					size = Integer.parseInt(split[1]);
				} catch (Exception e) {
					size = 16;
				}

				CodeEditor.FONT_SIZE = size;
				//CodeEditor.LINE_NUMBER_WIDTH = CodeEditor.FONT_SIZE * 4; 
				hasAltered = true;

				break;

			case "autocomplete_active:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.isAutoCompleteActive = true;
					break;
				}

				CodeEditor.isAutoCompleteActive = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;

			case "automatically_open_tabs:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.automaticallyOpenTabs = true;
					break;
				}

				CodeEditor.automaticallyOpenTabs = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "indent_with_spaces:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.indentSpaces = true;
					break;
				}

				CodeEditor.indentSpaces = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "indent_length:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.indentLength = 4;
					break;
				}

				CodeEditor.indentLength = Integer.valueOf(split[1]);

				hasAltered = true;

				break;

			case "language:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Main.lang = Language.ENG;
					break;
				}

				try {
					Main.lang = Language.valueOf(split[1].toUpperCase());
				} catch (IllegalArgumentException e) {
					break;
				}

				hasAltered = true;

				break;
			
			case "allow_animations:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Explorer.allowAnimations = true;
					break;
				}

				Explorer.allowAnimations = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "force_mac_buttons:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Main.forceMacButtons = false;
					break;
				}

				Main.forceMacButtons = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "line_ending:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.lineEnding = LineEnding.LF;
					break;
				}

				try {
					CodeEditor.lineEnding = LineEnding.valueOf(split[1].toUpperCase());
				} catch (IllegalArgumentException e) {
					break;
				}

				hasAltered = true;

				break;
				
			case "show_unsaved_title_bar:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.showUnsavedTitleBar = true;
					break;
				}

				CodeEditor.showUnsavedTitleBar = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "use_antialiasing:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Fonts.useAntiAliasing = true;
					break;
				}

				Fonts.useAntiAliasing = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "show_caps_lock:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.showCapsLock = true;
					break;
				}

				CodeEditor.showCapsLock = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "checkout_to_created_branch:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					GitCore.checkoutToCreatedBranch = true;
					break;
				}

				GitCore.checkoutToCreatedBranch = Boolean.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "terminal_prompt:":
				if (split[1].equalsIgnoreCase("default") || split[1] == "") {
					TerminalCore.prompt = '$';
					break;
				}
				
				hasAltered = true;
				
				int charCount = 0;
				
				while (split[1].charAt(charCount) == '\'' || split[1].charAt(charCount) == '\"')
					charCount++;
				
				TerminalCore.prompt = split[1].charAt(charCount);

				break;
				
			case "program_name:":
				if (split[1].equalsIgnoreCase("default") || split[1] == "") {
					Main.PROGRAM_NAME = Main.DEFAULT_PROGRAM_NAME;
					break;
				}
				
				hasAltered = true;
				
				String[] sub = Arrays.copyOfRange(split, 1, split.length);
				
				Main.PROGRAM_NAME = String.join(" ", sub).replace("'", "").replace("\"", "").trim();
				

				break;
				
			case "font:":
				if (split[1].equalsIgnoreCase("default") || split[1] == "") {
					Main.fntnr = "/font.png";
					break;
				}
				
				hasAltered = true;
				
				sub = Arrays.copyOfRange(split, 1, split.length);
				
				Main.fntnr = String.join(" ", sub).replace("'", "").replace("\"", "").trim();
				
				if (!Main.fntnr.startsWith("/"))
					Main.fntnr = "/" + Main.fntnr;

				break;
				
			case "editor_font:":
				if (split[1].equalsIgnoreCase("default") || split[1] == "") {
					Main.fnted = "/editorfont.png";
					break;
				}
				
				hasAltered = true;
				
				sub = Arrays.copyOfRange(split, 1, split.length);
				
				Main.fnted = String.join(" ", sub).replace("'", "").replace("\"", "").trim();
				
				if (!Main.fnted.startsWith("/"))
					Main.fnted = "/" + Main.fnted;
				
				break;
				
				// Advanced
				
			case "line_height_offset:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					CodeEditor.lineHeightOffset = 0;
					break;
				}

				CodeEditor.lineHeightOffset = Integer.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "char_width:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Fonts.charWidth = Fonts.ACTUAL_CHAR_WIDTH;
					break;
				}

				Fonts.charWidth = Integer.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "hanging_letters_offset:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Fonts.hangingLettersOffset = 2;
					break;
				}

				Fonts.hangingLettersOffset = Integer.valueOf(split[1]);

				hasAltered = true;

				break;
				
			case "y_offset:":
				if (!readConfigs) break;
				
				if (split[1].equalsIgnoreCase("default")) {
					Fonts.yOffset = 0;
					break;
				}

				Fonts.yOffset = Integer.valueOf(split[1]);

				hasAltered = true;

				break;
			/*
			 * case "put_chevrons_on_html_tags:": if (split[1].equalsIgnoreCase("default")) break;
			 * 
			 * CodeEditor.putChevronsOnTags = Boolean.valueOf(split[1]);
			 * 
			 * hasAltered = true;
			 * 
			 * break;
			 */
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

		if (dirs == null)
			dirs = new File[0];
		if (fls == null)
			fls = new File[0];

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
					files.add(new ListableFile(0, Explorer.MINIMUM_Y + (index * 30), Main.explorer.getWidth(), 30, f, folder));

					index++;
				}
			}
		} else {
			int index = 0;

			for (File f : listFilesOrdered(Main.baseFolder)) {
				files.add(new ListableFile(0, Explorer.MINIMUM_Y + (index * 30), Main.explorer.getWidth(), 30, f, null));

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

	public static void runCmd(boolean isWindowsCmd, File file) {
		if (isWindowsCmd) {
			System.out.println("aa");
			// Runtime.getRuntime().exec("cmd /c start " + file.getPath());
		}
	}

	@Override
	public void execute(String arg) {
		switch (arg) {
		case "del":
			String[] options = { Texts.yes, Texts.no };
			
			MessageBox.showDialog(Texts.confirmDelete, new String[] { Texts.sureDelete, regent.getName() + "?" }, options, new Execute[] { () -> {
				if (regent.isFile()) {
					if (!regent.delete()) {
						CodeEditor.setSystemLook();
		
						JOptionPane.showMessageDialog(null, Texts.delError, Texts.cantDelete, JOptionPane.OK_OPTION);
						
						MessageBox.showDialog(Texts.cantDelete, new String[] { Texts.delError }, new String[] { "Ok" }, new Execute[] { () -> {} });
					}
				}
				else {
					try {
						deleteDirectory(regent);
					} catch (IOException e) {
						e.printStackTrace();
					}
				}

				for (Tab t : Main.editor.tabs)
					if (t.getRegent().equals(this))
						t.close();

				IDEComponent.toRemove.add(this);

				Explorer.files.clear();
				ListableFile.files.clear();

				Explorer.files = ListableFile.loadFolder(Explorer.scope);
			}, () -> {  } });

			
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
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", "start", regent.getName());
				File dir = regent.getParentFile();
				pb.directory(dir);

				pb.start();

				// Runtime.getRuntime().exec("sh -c start \"\" " + regent.getName());

			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		/*case "cmd":
			try {
				ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start");

				File dir = Explorer.scope != null ? Explorer.scope.regent : Main.baseFolder; // eu tava fazendo o
																								// equivalente a isso:
																								// null.regent != null

				pb.directory(dir);

				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case "cmdbash":
			try {
				ProcessBuilder pb = new ProcessBuilder("bash", "-c", "start");

				File dir = Explorer.scope != null ? Explorer.scope.regent : Main.baseFolder; // eu tava fazendo o
																								// equivalente a isso:
																								// null.regent != null

				pb.directory(dir);

				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;*/

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
			/*
			 * String userdir = System.getProperty("user.dir");
			 * System.setProperty("user.dir", regent.getParent());
			 */

			new Thread() {
				public void run() {
					try {
						Main.desktop.open(regent);
					} catch (Exception e) {
						CodeEditor.setSystemLook();

						JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound,
								JOptionPane.OK_OPTION);
					}
				}
			}.start();

			// System.setProperty("user.dir", userdir);

			break;

		case "rename":
			Explorer.renameFile = new RenameFile(0, y, Main.explorer.getWidth() - 3, 30, regent);

			if (RenameFile.added)
				return;

			RenameFile.added = true;

			IDEComponent.toAdd.add(Explorer.renameFile);

			break;

		case "newfile":
			int y = 200;

			if (Explorer.files.size() > 0)
				y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;

			SetFileName set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, true);

			if (SetFileName.added)
				return;

			SetFileName.added = true;

			IDEComponent.toAdd.add(set);
			break;

		case "newfolder":
			y = 200;

			if (Explorer.files.size() > 0)
				y = Explorer.files.get(Explorer.files.size() - 1).getY() + 30;

			set = new SetFileName(0, y, Main.explorer.getWidth() - 3, 30, false);

			if (SetFileName.added)
				return;

			SetFileName.added = true;

			IDEComponent.toAdd.add(set);
			break;

		case "openeditor":
			addTab(this, false);
			break;
			
		case "duplicate":
			try {
				int index = 1;
				File newFile = new File(regent.getParent() + File.separator + "(" + index + ") " + getFileNameWithoutExtension(regent) + getFileExtension(regent));
				
				while (newFile.exists()) {
					newFile = new File(regent.getParent() + File.separator + "(" + ++index + ") " + getFileNameWithoutExtension(regent) + getFileExtension(regent));
				}
				
				Files.copy(regent.toPath(), newFile.toPath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			ReloadButton.reloadExplorer();
			
			break;
			
		case "copyfile":
			copy = regent;
			cutFlag = false;
			break;
			
		case "cutfile":
			copy = regent;
			cutFlag = true;
			break;
			
		case "copyrel":
			CodeEditor.copyText(regent.getAbsolutePath().contains(File.separator + Main.baseFolder.getName() + File.separator) ? regent.getAbsolutePath().substring(regent.getAbsolutePath().indexOf(Main.baseFolder.getName())) : regent.getAbsolutePath());
			break;
			
		case "copyabs":
			CodeEditor.copyText(regent.getAbsolutePath());
			break;
		}
	}

	// Fonte: Apache Commons
	public static void deleteDirectory(final File directory) throws IOException {
		if (!directory.exists()) {
			return;
		}

		cleanDirectory(directory);

		if (!directory.delete()) {
			final String message =
					"Unable to delete directory " + directory + ".";
			throw new IOException(message);
		}
	}

	public static void cleanDirectory(final File directory) throws IOException {
		final File[] files = directory.listFiles();

		IOException exception = null;
		for (final File file : files) {
			try {
				forceDelete(file);
			} catch (final IOException ioe) {
				exception = ioe;
			}
		}

		if (null != exception) {
			throw exception;
		}
	}
	
	public static void forceDelete(final File file) throws IOException {
		if (file.isDirectory()) {
			deleteDirectory(file);
		} else {
			final boolean filePresent = file.exists();
			if (!file.delete()) {
				if (!filePresent) {
					throw new FileNotFoundException("File does not exist: " + file);
				}
				final String message =
						"Unable to delete file: " + file;
				throw new IOException(message);
			}
		}
	}

	public static ListableFile newListableFile(File regent) {
		return new ListableFile(0,0,0,0, regent, null);
	}

	public static void addTab(ListableFile file, boolean isAutomatic) {
		if ((!CodeEditor.automaticallyOpenTabs && isAutomatic) || file == null)
			return;
		
		CommandTerminal.runCommand("resetundoredo");
		
		if (Main.editor.editing != null)
			Main.editor.editing.save();
		
		RightClickOption.removeAllRightClickOptions();
		
		if (file.getRegent().isFile() && Main.editor.tabs != null) {
			int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX()
					: Tab.MIN_X;

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

			if (Main.editor.tabs.size() == 0)
				Main.editor.tabScr = 0;

			Main.editor.cursorX = 0;
			Main.editor.cursorY = 1;

			Main.editor.scrX = 0;
			Main.editor.scrY = 0;

			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			
			CommandTerminal.runCommand("resetundoredo");
			
			if (!file.getRegent().canWrite()) {
				Main.editor.isReadOnly = true;
				toAdd.isReadOnly = true;
			}

			for (Tab t : Main.editor.tabs)
				if (t.getRegent().getRegent().getPath().equals(file.getRegent().getPath())) {
					Main.editor.editing = t;

					return;
				}

			Main.editor.toAdd.add(toAdd);
			Main.editor.editing = toAdd;
		}
	}
	
	public static void addTab(ListableFile file, boolean isAutomatic, boolean isTemporary) {
		if ((!CodeEditor.automaticallyOpenTabs && isAutomatic) || file == null)
			return;
		
		CommandTerminal.runCommand("resetundoredo");

		if (file.getRegent().isFile() && Main.editor.tabs != null) {
			int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX()
					: Tab.MIN_X;

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

			if (Main.editor.tabs.size() == 0)
				Main.editor.tabScr = 0;

			Main.editor.cursorX = 0;
			Main.editor.cursorY = 1;

			Main.editor.scrX = 0;
			Main.editor.scrY = 0;

			Main.editor.isMultilineCommenting = false;
			Main.editor.isAnotherIteration = false;
			toAdd.isTemporary = isTemporary;
			
			if (!file.getRegent().canWrite()) {
				Main.editor.isReadOnly = true;
				toAdd.isReadOnly = true;
			}

			for (Tab t : Main.editor.tabs)
				if (t.getRegent().getRegent().getPath().equals(file.getRegent().getPath())) {
					Main.editor.editing = t;

					return;
				}

			Main.editor.toAdd.add(toAdd);
			Main.editor.editing = toAdd;
		}
	}
	
	public static boolean isListableFileHovered() {
		if (Explorer.files == null) return false;
		
		for (ListableFile l : Explorer.files)
			if (l.hovered()) return true;
		
		return false;
	}
	
	public static Image getFileIcon(String filename) {
		for (FileType f : types) {
			if (f.getExtension().equalsIgnoreCase(getFileExtension(filename))) {
				return f.getIcon();
			}

			else if (f.getExtension().equalsIgnoreCase(filename)) {
				return f.getIcon();
			}
		}
		
		return Main.spritesheet.getSprite(0, 64, 16, 16);
	}
	
	public void tick() {
		if (SetFileName.added || CommandTerminal.active || MessageBox.active || RenameFile.added
				|| MouseInput.hovered(Main.explorer.getX() + Main.explorer.getWidth() - 5, Main.explorer.getY(), 10,
						Main.explorer.getHeight()))
			return;
		
		if (CommandTerminal.expOff)
			return;
		
		//if (y < Explorer.MINIMUM_Y) y = Explorer.MINIMUM_Y;
		
		if (hovered())
			Main.screen.setCursor(Cursor.getDefaultCursor());
		
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

		if (leftClicked() && !(y < Screen.DECORATION_HEIGHT + 200 || y > Main.screen.getHeight()) && !RightClickOption.isRightClickActive()) {
			MouseInput.updateMouse();
			
			if (Main.editor.editing != null && regent.isFile())
				Main.editor.editing.saveForced();
			
			for (Tab t : Main.editor.tabs) {
				if (t.regent.getRegent().equals(regent)) {
					t.select();
					
					return;
				}
			}

			if (Explorer.folderPath.length() > Main.explorer.maxTextWidth)
				Explorer.folderPath = Explorer.folderPath.substring(0, Main.explorer.maxTextWidth) + "...";

			Explorer.baseFolderName = Main.baseFolder.getName().length() > 15
					? Main.baseFolder.getName().substring(0, 12) + "..."
					: Main.baseFolder.getName();

			if (CodeEditor.isBinary(getFileExtension(regent))) {
				try {
					Main.desktop.open(regent);
				} catch (IOException e) {
					CodeEditor.setSystemLook();

					JOptionPane.showMessageDialog(null, Texts.cantFindDefault, Texts.nothingFound,
							JOptionPane.OK_OPTION);
				}

				return;
			}

			if (y > Screen.DECORATION_HEIGHT + 199 && regent.isDirectory()) {
				files = loadFolder(this);

				if (files.size() == 0)
					Explorer.toRemove.addAll(Explorer.files);
			}

			if (regent.isFile() && Main.editor.tabs != null) {
				addTab(this, false);
			}
		}

		if (KeyInput.isKeyPressed()) {
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_F2 && hovered()) { // F2 - Renomear
				KeyInput.updateKeys();

				execute("rename");

				return;
			}
		}

		if ((rightClicked() || (KeyInput.getKeyCodePressed() == 525 && hovered()))) {
			MouseInput.updateMouse();

			int widthDraw = Main.lang == Language.PORT ? 440 : 420;
			
			List<RightClickOption> list = new ArrayList<>();

			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.createFile, (s) -> execute(s), "newfile", true));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.createFolder, (s) -> execute(s), "newfolder"));

			list.add(new RightClickOption((x + width), 0, widthDraw, regent.isFile(), Texts.openInEditor, (s) -> execute(s), "openeditor"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.delete, (s) -> execute(s), "del"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.rename, (s) -> execute(s), "rename"));
			list.add(new RightClickOption((x + width), 0, widthDraw, regent.isFile(), Texts.duplicate, (s) -> execute(s), "duplicate"));
			list.add(new RightClickOption((x + width), 0, widthDraw, regent.isFile(), Texts.copy + " " + Texts.file, (s) -> execute(s), "copyfile"));
			list.add(new RightClickOption((x + width), 0, widthDraw, regent.isFile(), Texts.cut + " " + Texts.file, (s) -> execute(s), "cutfile"));
			list.add(new RightClickOption((x + width), 0, widthDraw, ListableFile.copy != null, Texts.paste + " " + Texts.file, (s) -> Main.editor.execute(s), "pastefile"));
			
			list.add(new RightClickOption((x + width), 0, widthDraw, Main.os == OS.WINDOWS, Texts.openCmd, (s) -> Main.editor.execute(s), "cmd"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.openTerminal, (s) -> execute(s), "term"));
			
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.openExplorer, (s) -> execute(s), "sysexp"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.setBaseFolder, (s) -> execute(s), "setbase"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.copyRelativePath, (s) -> execute(s), "copyrel"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.copyAbsolutePath, (s) -> execute(s), "copyabs"));
			list.add(new RightClickOption((x + width), 0, widthDraw, Texts.openDefault, (s) -> execute(s), "opendef"));

			if ((getFileExtension(regent).equalsIgnoreCase(".bat") || getFileExtension(regent).equalsIgnoreCase(".cmd") || getFileExtension(regent).equalsIgnoreCase(".com")) && Main.os == OS.WINDOWS)
				list.add(new RightClickOption((x + width), y + 240, widthDraw, Texts.execute, (s) -> execute(s), "run"));
			
			IDEComponent.addRightClickOptions((x + width), y, list.toArray(new RightClickOption[list.size()]));
			// if (getFileExtension(regent).equalsIgnoreCase(".sh") && !isWindows)
			// IDEComponent.addRightClickOption((x + width), y + 240, widthDraw,
			// Texts.execute, (s) -> execute(s), "runbash");
		}

		int index = Explorer.files.indexOf(this);

		if (index <= 0)
			return;

		y = Explorer.files.get(index - 1).y + height;
	}

	public synchronized void render(Graphics g) {
		if (y < Screen.DECORATION_HEIGHT + 200 || y > Main.screen.getHeight())
			return;
		if (CommandTerminal.expOff)
			return;
		if (y < Screen.DECORATION_HEIGHT + 199)
			return;

		for (IDEComponent i : IDEComponent.components) {
			if (i instanceof RenameFile)
				if (((RenameFile) i).old == regent)
					return;
		}

		if (hovered() && !SetFileName.added && !CommandTerminal.active && !MessageBox.active && !SetBranchName.added && !SetCommitName.added && !RenameFile.added
				&& !RightClickOption.isRightClickActive()) {
			g.setColor(Colors.explorerLight);
			g.fillRect(0, y, Main.explorer.getWidth(), height);
		}

		String name = regent.getName();

		if (name.length() > Main.explorer.maxTextWidth)
			name = name.substring(0, Main.explorer.maxTextWidth - 3) + "...";

		if (regent.isDirectory()) {
			Fonts.drawString(name, x + 40, y + 4, new IDEFont(Fonts.lightGrayNormal, 16), width, g);

			g.drawImage(Main.folder, x + 6, y + 2, height - 5, height - 5, null);
		} else if (regent.isFile()) {
			Fonts.drawString(name, x + 40, y + 4, new IDEFont(Fonts.lightGrayNormal, 16), width, g);

			g.drawImage(ListableFile.getFileIcon(regent.getName()), x + 5, y, height, height, null);
		}
		
		if (regent.isHidden()/* || (copy != null && copy.equals(regent) && cutFlag)*/) {
			g.setColor(new Color(0, 0, 0, 30));
			g.fillRect(x, y, width - 2, height);
		}
	}
}
