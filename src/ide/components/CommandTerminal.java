package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.IDELine;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;
import ide.util.Language;
import ide.util.Spritesheet;
import ide.util.Texts;

/**
 * Um terminal onde você coloca os comandos nele e ele executa de acordo com que você mandar. Simples, não?
 * 
 * @author Juninho
 *
 */
public class CommandTerminal extends IDEComponent {
	
	public static boolean active = false;
	private int cursorIndex = 0;
	
	public static boolean expOff = false;

	private StringBuilder builder;
	
	private boolean showCursor;
	private Animation cursor;
	
	public static String lastCommand = "";
	
	private static JFileChooser chooser;
	
	private static boolean changeHints = true;
	private static int comIndex = 0;
	
	/*
	 * 	 Nota para novos comandos, queridos adicionadores de comandos
	 *  
	 *   [...]
	 *  
	 *   n° 103: 
	 *   Se colocar num array tem que colocar no outro tbm senão não dá certo
	 *   
	 *   n° 104:
	 *   Somente adicionem comandos se realmente forem necessários
	 *   
	 *   n° 105:
	 *   O comando somente vai ser adicioando se estiver em ordem correta nos dois arrays, e aparecerá no autocomplete
	 *   
	 *   [...]
	 */
	
	// O Emmet não está disponível ainda, talvez na v4.0 ele venha
	
	public static final String[] commands = { "cmd", "sysexp", "closealltabs", "resettabscroll", "reloadconfigfile",
			"reseteditorscroll", "deselect", "copy", "del", "cut", "paste", "selectline",
			"selectall", "generateconfigfile", "toggleexplorer", "loadconfigfile", "unloadconfigfile",
			"sysout", "syso", "cout", "stdcout", "writeline", "syserr", "clog", "gendiv", "closebasefolder",
			"revertconfigfile", "togglecodehelpers", "gotocursor", "togglereadonly", "closetab int:tab_index",
			"gotoline int:line", "setfontsize int:size/default", "insertchar int:ascii_code",
			"gendiv str:class_name", "genbase str:type", //"emmet str:expression",
			"lorem int:num_words", "ordertab int:tab_from int:tab_to",
			"setcursorpos int:x int:y",
			"gengetter str:lang str:variable_name str:variable_type",
			"gensetter str:lang str:variable_name str:variable_type" };
	
	public static final String[] onlyCommands = { "cmd", "sysexp", "closealltabs", "resettabscroll", "reloadconfigfile",
			"reseteditorscroll", "deselect", "copy", "del", "cut", "paste", "selectline",
			"selectall", "generateconfigfile", "toggleexplorer", "loadconfigfile", "unloadconfigfile",
			"sysout", "syso", "cout", "stdcout", "writeline", "syserr", "clog", "gendiv", "closebasefolder",
			"revertconfigfile", "togglecodehelpers", "gotocursor", "togglereadonly", "closetab",
			"gotoline", "setfontsize", "insertchar",
			"gendiv", "genbase", //"emmet",
			"lorem", "ordertab",
			"setcursorpos",
			"gengetter",
			"gensetter" };
	
	public static List<String> commandHints = new ArrayList<>();
	
	public CommandTerminal(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		commandHints.clear();
		
		builder = new StringBuilder();
		
		CodeEditor.setSystemLook();
		
		cursor = new Animation(2, true) {
			private boolean flip = false;
			
			public void play() {
				showCursor = !flip;
				
				flip = !flip;
				
				super.play();
			}
		};
		
		new Thread() {
			public void run() {
				cursor.play();
			}
		}.start();
		
		chooser = new JFileChooser(Texts.save + "/" + Texts.open);
	}
	
	/**
	 * Esse é o meu primeiro lexer/parser custom!
	 * 
	 * @param command - o comando, oras
	 */
	public static void runCommand(String command) { // o lastCommand muda pq vc seleciona e dps desseleciona e o sistema executa o comando, e salva ele
		String[] tokens = command.split(" ");
		
		String com = tokens[0];
		String[] args = new String[tokens.length - 1];
		
		for (int i = 1; i < tokens.length; i++) // é 1 mesmo viu
			args[i - 1] = tokens[i];
		
		if (args.length == 0) {
			switch (com) {
			case "cmd":
				try {
					ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "start");
					
					File dir = Explorer.scope != null ? Explorer.scope.getRegent() : Main.baseFolder; // eu tava fazendo o equivalente a isso: null.regent != null
					
					pb.directory(dir);
					
					pb.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			case "sysexp":
				try {
					Main.desktop.open(new File(Explorer.files.get(0).getRegent().getPath()).getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			case "closealltabs":
				Main.editor.tabs.clear();
				break;
				
			case "resettabscroll":
				Main.editor.tabScr = 0;
				break;
				
			case "reseteditorscroll":
				Main.editor.scrX = 0;
				Main.editor.scrY = 0;
				break;
				
			case "deselect":
				if (Main.editor.isReadOnly) break;
				if (Main.editor.editing == null) break;
				
				Main.editor.line1 = 0; // 0, não é 1 não?
				Main.editor.line2 = 0;
				
				Main.editor.index1 = 0;
				Main.editor.index2 = 0;
				
				Main.editor.selecting = false;
				break;
				
			case "copy":
				if (Main.editor.editing == null) break;
				if (!Main.editor.selecting || Main.editor.isReadOnly) break;
				
				List<String> lines = new ArrayList<>();
				String str = "";
				
				if (Main.editor.line1 != Main.editor.line2) { // se não selecionou uma linha só (selecionou várias)
					for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) {
						if (i == Main.editor.line1 - 1) {
							lines.add(new String(CodeEditor.toCharArray(Main.editor.lines.get(i).getChars().subList(Main.editor.index1, Main.editor.lines.get(i).getChars().size()))));
							
							continue;
						}
						
						if (i == Main.editor.line2 - 1) {
							lines.add(new String(CodeEditor.toCharArray(Main.editor.lines.get(i).getChars().subList(0, Main.editor.index2))));
							
							continue;
						}
						
						lines.add(new String(CodeEditor.toCharArray(Main.editor.lines.get(i).getChars())));
					}
					
					for (String s : lines) {
						str += s;
						
						if (s != lines.get(lines.size() - 1)) // se não for a última linha (para não adicionar quebras de linha adicionais desnecessárias)
							str += "\n";
					}
				}
				else {
					if (Main.editor.index2 < Main.editor.index1) { // que coisa não? (provavelmente isso nunca vai acontecer)
						runCommand("deselect");
						
						break;
					}
					
					str = new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.line1 - 1).getChars().subList(Main.editor.index1, Main.editor.index2)));
				}
				
				StringSelection sel = new StringSelection(str);
				Clipboard clip = Main.toolkit.getSystemClipboard();
				
				clip.setContents(sel, sel);
				
				break;
				
			case "del":
				if (Main.editor.editing == null) break;
				if (!Main.editor.selecting) break;
				if (Main.editor.isReadOnly) break;
				
				try {
					StringBuilder s = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.line1 - 1).getChars())));
					
					if (Main.editor.line1 != Main.editor.line2) { // se não selecionou uma linha só (selecionou várias)
						for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) {
							if (i == Main.editor.line1 - 1) {
								Main.editor.lines.get(i).setChars(Main.editor.lines.get(i).getChars().subList(0, Main.editor.index1));
								
								continue;
							}
							
							if (i == Main.editor.line2 - 1) {
								if (Main.editor.index2 != Main.editor.lines.get(i).getChars().size())
									Main.editor.lines.get(i).setChars(Main.editor.lines.get(i).getChars().subList(Main.editor.index2, Main.editor.lines.get(i).getChars().size()));
								else
									Main.editor.linesToRemove.add(Main.editor.lines.get(i));
								
								continue;
							}
							
							Main.editor.linesToRemove.add(Main.editor.lines.get(i)); // pra evitar concurrentmodificationexception
						}
						
							Main.editor.cursorX = Main.editor.index1;
							Main.editor.cursorY = Main.editor.line1;
							
							runCommand("deselect");
							Main.editor.setCursorWithinBounds();
							
							Main.editor.editing.setSaved(false);
					
					/*Main.editor.cursorX = Main.editor.mx;		// tomar cuidado quando o comando é chamado pelo sistema e vc ver seu cursor andando adoidado por ai viu TODO
					Main.editor.cursorY = Main.editor.my;			// melhor desabilitar isso
					
					Main.editor.setCursorWithinBounds();*/
					
						break;
					}
					else {
						if (Main.editor.index2 < Main.editor.index1) {
							runCommand("deselect");
							
							break;
						}
						
						s.delete(Main.editor.index1, Main.editor.index2);
						
						Main.editor.register(s, Main.editor.line1 - 1);
					}
					
					Main.editor.cursorX = Main.editor.index1;
					
					runCommand("deselect");
					Main.editor.editing.setSaved(false);
				
				} catch (Exception e) {
					runCommand("deselect"); // TODO talvez não desselecionar, deletar com algum index no 0, né?
				}
				break;
				
			case "cut":
				if (Main.editor.editing == null) break;
				if (!Main.editor.selecting) break;
				if (Main.editor.isReadOnly) break;
				
				runCommand("copy");	// hehe :)
				runCommand("del");
				break;
				
			case "paste":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				Main.editor.paste();
				
				break;
				
			case "selectline":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				int y = Main.editor.cursorY - 1;
				
				Main.editor.index1 = 0;
				Main.editor.index2 = Main.editor.lines.get(y).getChars().size();
				
				Main.editor.line1 = y + 1;
				Main.editor.line2 = y + 1;
				
				Main.editor.selecting = true;
				break;
				
			case "selectall":
				if (Main.editor.isReadOnly) break;
				
				Main.editor.index1 = 0;
				Main.editor.index2 = Main.editor.lines.get(Main.editor.lines.size() - 1).getChars().size();
				
				Main.editor.line1 = 1;
				Main.editor.line2 = Main.editor.lines.size();
				
				Main.editor.cursorX = 0;
				Main.editor.cursorY = 1;
				
				Main.editor.selecting = true;
				break;
				
			case "generateconfigfile":
				int option = chooser.showSaveDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					File fl = chooser.getSelectedFile();
					
					ListableFile.generateConfigFile(fl);
					
					CodeEditor.setSystemLook();
					String[] options = { Texts.openFolder, Texts.cancel };
    				
    				CodeEditor.setSystemLook();
    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.wantOpenFile, Texts.wouldEdit, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    				
    				/*if (selectedOption == 0) {
    					Main.baseFolder = fl.getParentFile();
		        	  	
		        	  	Explorer.files.clear();
						ListableFile.files.clear();
						
						Explorer.scope = null;
		        	  	
		        	  	int index = 0;
						
						for (File f : ListableFile.listFilesOrdered(Main.baseFolder)) {
							ListableFile.files.add(new ListableFile(0, 200 + (index * 30), Main.explorer.getWidth(), 30, f, null));
							
							index++;
						}
		          
				int lastX = Main.editor.tabs.size() > 0 ? Main.editor.tabs.get(Main.editor.tabs.size() - 1).getX() : Tab.MIN_X;
	        	
				if (!(fl.getName().equalsIgnoreCase(".pdf") || fl.getName().equalsIgnoreCase(".jar") || fl.getName().equalsIgnoreCase(".iso") || fl.getName().equalsIgnoreCase(".img") || fl.getName().equalsIgnoreCase(".flp") || fl.getName().equalsIgnoreCase(".class") || fl.getName().equalsIgnoreCase(".exe") || fl.getName().equalsIgnoreCase(".urna") || fl.getName().equalsIgnoreCase(".save") || fl.getName().equalsIgnoreCase(".docx") || fl.getName().equalsIgnoreCase(".pptx") || fl.getName().equalsIgnoreCase(".one") || fl.getName().equalsIgnoreCase(".psd") || fl.getName().equalsIgnoreCase(".aed") || fl.getName().equalsIgnoreCase(".ai") || fl.getName().equalsIgnoreCase(".indd") || fl.getName().equalsIgnoreCase(".ini") || fl.getName().equalsIgnoreCase(".dll") || fl.getName().equalsIgnoreCase(".png") || fl.getName().equalsIgnoreCase(".jpg") || fl.getName().equalsIgnoreCase(".jpeg") || fl.getName().equalsIgnoreCase(".gif") || fl.getName().equalsIgnoreCase(".bmp") || fl.getName().equalsIgnoreCase(".ico") || fl.getName().equalsIgnoreCase(".webp") || fl.getName().equalsIgnoreCase(".mp4") || fl.getName().equalsIgnoreCase(".wmv") || fl.getName().equalsIgnoreCase(".avi") || fl.getName().equalsIgnoreCase(".wav") || fl.getName().equalsIgnoreCase(".mp3") || fl.getName().equalsIgnoreCase(".ogg") || fl.getName().equalsIgnoreCase(".otf") || fl.getName().equalsIgnoreCase(".ttf") || fl.getName().equalsIgnoreCase(".woff") || fl.getName().equalsIgnoreCase(".woff2") || fl.getName().equalsIgnoreCase(".zip") || fl.getName().equalsIgnoreCase(".rar") || fl.getName().equalsIgnoreCase(".7z") || fl.getName().equalsIgnoreCase(".bin"))) {
		        	Tab toAdd = new Tab(Main.editor.tabs.size() > 0 ? (lastX + Tab.WIDTH) + 3 : Tab.MIN_X - Tab.WIDTH, ListableFile.searchListableFiles(fl));
		        	
	  				Main.editor.cursorX = 0;
	  				Main.editor.cursorY = 1;
	  				
	  				Main.editor.scrX = 0;
	  				Main.editor.scrY = 0;
	  				
		        	  	Main.editor.editing = toAdd;
		        	  	Main.editor.tabs.add(toAdd);
						
		        	  	new Thread() {
							public void run() {
								try {
									Main.editor.lines = Main.editor.readFile(fl);
								} catch (IOException e) { // não suportado, se caiu aqui
									return;
								}
							}
						}.start();
		        	  	
						Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
    				}*/
    				if (selectedOption == 0) {
    					try {
							Main.desktop.open(fl.getParentFile());
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
				}
				break;
				
			case "toggleexplorer":
				if (expOff)
					Main.editor.setX(280);
				else
					Main.editor.setX(0);
				
				expOff ^= true;	// uma forma de togglar boolean (^ é xor gate)
				
				break;
				
			case "reloadconfigfile":
				Main.load();
				break;
				
			case "loadconfigfile":
				option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					Main.conffile = chooser.getSelectedFile().getPath();
					
					Main.load();
					
					if (!ListableFile.hasAltered) {
						String[] options = { Texts.yes, Texts.no };
						
						CodeEditor.setSystemLook();
						int selectedOption = JOptionPane.showOptionDialog(null, Texts.configFileNotChanged, Texts.didNothing, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
						
						if (selectedOption != 1) break;
						
						Main.conffile = "none";
						Main.hasConfigFile = false;
						
						runCommand("revertconfigfile");
					}
				}
				
				break;
				
			case "unloadconfigfile":
				Main.conffile = "none";
				
				Main.writeFile(Main.settingsFile);
				runCommand("revertconfigfile");
				
				Main.hasConfigFile = false;
				
				break;
			
			case "sysout":
			case "syso":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "System.out.println();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 19;
				
				break;
				
			case "cout":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "cout << \"\" << endl;");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 9;
				
				break;
				
			case "stdcout":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "std::cout << \"\" << std::endl;");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 14;
				
				break;
				
			case "writeline":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "Console.WriteLine();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 18;
				
				break;
				
			case "syserr":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "System.err.println();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 19;
				
				break;
				
			case "clog":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "console.log();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 12;
				
				break;
				
			case "gendiv":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "<div></div>");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			case "closebasefolder":
				Main.editor.tabs.clear();
				Main.baseFolder = null;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.scope = null;
				Main.editor.editing = null;
				
				Main.screen.frame.setTitle("Boot IDE");
				
				IDEComponent.toRemove.add(Main.oneLevel);
				IDEComponent.toRemove.add(Main.returnBase);
				IDEComponent.toRemove.add(Main.newFile);
				IDEComponent.toRemove.add(Main.newFolder);
				IDEComponent.toRemove.add(Main.reload);
				
				Main.writeFile(Main.settingsFile);
				
				break;
				
			case "revertconfigfile":
				Colors.revertColors();
				
				Fonts.initFonts(Main.fntnr, Main.fntbl);
				Main.spritesheet = new Spritesheet(Main.sprsh);
				
				CodeEditor.FONT_SIZE = 16;
				
				Main.lang = Language.ENG;
				Texts.setTexts(Main.lang);
				
				break;
				
			case "togglecodehelpers":
				if (Main.editor.editing == null) break;
				
				Main.editor.codeHelpersOn ^= true; // método prático de inverter boolean, porque em Assembly mais ou menos seria assim: xor syntaxerrorson, true (lógico que o nome da variável n seria esse né :/)
				break;
				
			case "gotocursor":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				Main.editor.scrY = (Main.editor.cursorY * (CodeEditor.FONT_SIZE)); // TODO arrumar isso aqui
				
				if (Main.editor.cursorY <= 7) Main.editor.scrY = 0;
				
				break;
				
			case "togglereadonly":
				if (Main.editor.editing == null) break;
				
				runCommand("deselect");
				
				Main.editor.isReadOnly ^= true;
				Main.editor.editing.isReadOnly ^= true;
				
				break;
			}
		}
		
		else if (args.length == 1) {
			switch (com) {
			case "closetab":
				try {
					int args0 = Integer.parseInt(args[0]);
					
					if (Main.editor.tabs.size() == 0 ||
						    args0 < 0 ||
						    args0 > Main.editor.tabs.size())
							return;
						
					Main.editor.tabs.get(args0).close();
						
					break;
				} catch (NumberFormatException e) {
					break;
				}
				
			case "gotoline":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				try {
					Main.editor.cursorY = Integer.parseInt(args[0]);
					
					runCommand("gotocursor");
				} catch (NumberFormatException e) {
					break;
				}
				break;
				
			case "setfontsize":
				if (Main.editor.editing == null) break;
				
				if (args[0].equals("default"))
					CodeEditor.FONT_SIZE = 16;
				
				try {
					int a0 = Integer.parseInt(args[0]);
						
					CodeEditor.FONT_SIZE = a0;
						
					if (Main.editor.editing != null)
						Main.editor.lines = Main.editor.readFile(Main.editor.editing.getRegent().getRegent());
				} catch (NumberFormatException | IOException e) {
					CodeEditor.FONT_SIZE = 16;
				}
				break;
				
			case "insertchar":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				int ascii = 0;
				
				try {
					ascii = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {}
				
				char c = (char) ascii;
				
				//if (!Character.isLetterOrDigit(c)) break; // não vai verificar mais, pode colocar o que quiser aqui :\
				
				StringBuilder sb = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				sb.insert(Main.editor.cursorX, c);
				
				Main.editor.register(sb, Main.editor.cursorY - 1);
				
				Main.editor.cursorX++;
				Main.editor.editing.setSaved(false);
				
				break;
				
			case "gendiv":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "<div class=\"" + args[0] + "\"></div>");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			/*case "rename":
				int option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					File toRename = chooser.getSelectedFile();
					
					String newPath = toRename.getParent() + "\\" + args[0];
					File newFile = new File(newPath);
					
					toRename.renameTo(newFile);
				}
				break;*/
				
			case "genbase":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				String[] strs = { };
				
				String classname = ListableFile.getFileNameWithoutExtension(Main.editor.editing.getRegent().getRegent());
				
				switch (args[0].toLowerCase()) {
				case "html":
					String[] htmlstrs = { "<!DOCTYPE html>", "<html>", "    <head>", "        <title></title>", "        ", "        <meta charset=\"UTF-8\">", "    </head>", "    <body>", "    </body>", "</html>" };
					
					strs = htmlstrs;
					
					break;
					
				case "css":
					String[] cssstrs = { "* {", "    margin: 0;", "    padding: 0;", "    box-sizing: border-box;", "    font-family: sans-serif;", "}"};
					
					strs = cssstrs;
					
					break;
					
				case "java":
					String[] javastrs = { "public class " + classname + " {", "    ", "}"};
					
					strs = javastrs;
					
					break;
					
				case "javainterface":
					String[] javaintstrs = { "public interface " + classname + " {", "    ", "}"};
					
					strs = javaintstrs;
					
					break;
					
				case "javaenum":
					String[] javaenstrs = { "public enum " + classname + " {", "    ", "}"};
					
					strs = javaenstrs;
					
					break;
					
				case "javamain":
					String[] javamstrs = { "public class " + classname + " {", "", "    public static void main(String[] args) {", "        ", "    }", "}"};
					
					strs = javamstrs;
					
					break;
					
				case "cs":
					String[] csstrs = { "using System;", "using System.Collections.Generic;", "using System.Linq;", "using System.Text;", "using System.Threading.Tasks;", "", "namespace " + classname + " ", "{", "    ", "    public class Program ", "    {", "        ", "    }", "}"};
					
					strs = csstrs;
					
					break;
					
				case "csmain":
					String[] csmstrs = { "using System;", "using System.Collections.Generic;", "using System.Linq;", "using System.Text;", "using System.Threading.Tasks;", "", "namespace " + classname + " ", "{", "    ", "    public class Program ", "    {", "        ", "        static void Main(string[] args)", "        {", "            ", "        }", "    }", "}"};
					
					strs = csmstrs;
					
					break;
					
				case "cpp":
					String[] cppstrs = { "#include <iostream>", "", "using namespace std;" };
					
					strs = cppstrs;
					
					break;
					
				case "cppmain":
					String[] cppmstrs = { "#include <iostream>", "", "using namespace std;", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cppmstrs;
					
					break;
					
				case "c":
					String[] cstrs = { "#include <stdio.h>", "#include <stdlib.h>" };
					
					strs = cstrs;
					
					break;
					
				case "cmain":
					String[] cmstrs = { "#include <stdio.h>", "#include <stdlib.h>", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cmstrs;
					
					break;
					
				case "ino":
					String[] inostrs = { "void setup()", "{", "    ", "}", "", "void loop()", "{", "    ", "}"};
					
					strs = inostrs;
					
					break;
					
				case "html5":
					String[] htmlnewstrs = { "<!DOCTYPE html>", "<html>", "    <head>", "        <title></title>", "        ", "        <meta charset=\"UTF-8\">", "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">", "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">", "    </head>", "    <body>", "    </body>", "</html>" };
					
					strs = htmlnewstrs;
					
					break;
				}
				
				if (strs.length == 0) return;
				
				for (int i = 0; i < strs.length; i++) {
					if ((Main.editor.cursorY - 1) + i >= Main.editor.lines.size())
						Main.editor.lines.add(new IDELine(new ArrayList<>(), new ArrayList<>()));
					
					b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get((Main.editor.cursorY - 1) + i).getChars())));
					
					b.insert(Main.editor.cursorX, strs[i]);
					
					Main.editor.register(b, (Main.editor.cursorY - 1) + i);
				}
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			/*case "search":									// Deprecated. Use Search/Replace.
				List<Integer> linesfound = new ArrayList<>();
				
				//args[0] = Main.editor.arrayToStr(args); // -- n da certo pq esse comando n vai ser executado pq o numero de args é maior
				
				for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				// como é automaticamente occur 0, pegamos automaticamente ela.
				
				Main.editor.scrY = (linesfound.get(0) + 1) * (Main.editor.FONT_SIZE);// + 4);
				Main.editor.cursorY = (linesfound.get(0) - 1) + 2;
				
				break;
				
			case "searchsel":
				if (!Main.editor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				// como é automaticamente occur 0, pegamos automaticamente ela.
				
				Main.editor.scrY = (linesfound.get(0) - 1) * (Main.editor.FONT_SIZE);// + 4);
				Main.editor.cursorY = (linesfound.get(0) - 1) + 2;
				
				break;*/
				
			case "lorem":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				StringBuilder bl = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				try {
					String lorem = CodeEditor.generateLoremIpsum(Integer.parseInt(args[0]));
					
					bl.insert(Main.editor.cursorX, lorem);
					
					Main.editor.register(bl, Main.editor.cursorY - 1);
				} catch (Exception e) { break; }
					
				break;
			}
		}
		
		else if (args.length == 2) {
			switch (com) {
			case "ordertab":
				try {
					if (Main.editor.tabs.size() < 2) break;
					
					int idx1 = Integer.parseInt(args[0]);
					int idx2 = Integer.parseInt(args[1]);
					
					Collections.swap(Main.editor.tabs, idx1, idx2);
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					break;
				}
				
				break;
				
			case "setcursorpos":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				try {
					int x = Integer.parseInt(args[0]) - 1;
					int y = Integer.parseInt(args[1]);
					
					Main.editor.cursorX = x;
					Main.editor.cursorY = y;
					
					Main.editor.setCursorWithinBounds();
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			/*case "search":
				List<Integer> linesfound = new ArrayList<>();
				
				for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars()));
					
					if (s.contains(args[0])) linesfound.add(i);
				}
				
				if (linesfound.size() == 0) return;
				
				int occurnum = Integer.parseInt(args[1]); // base 1 viu
				
				if (occurnum == 0) {
					runCommand("search " + args[0]);
					break;
				}
				
				if (occurnum > linesfound.size())
					occurnum = linesfound.size();
				
				Main.editor.scrY = (linesfound.get(occurnum - 1) - 1) * (Main.editor.FONT_SIZE);// + 4);
				Main.editor.cursorY = (linesfound.get(occurnum - 1) - 1) + 2;
				
				break;
				
			case "searchsel":
				if (Main.editor.isReadOnly) break;
				
				if (!Main.editor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = Main.editor.line1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				occurnum = Integer.parseInt(args[1]); // base 1 viu
				
				if (occurnum > linesfound.size())
					occurnum = linesfound.size();
				
				Main.editor.scrY = (linesfound.get(occurnum - 1) - 1) * (Main.editor.FONT_SIZE);// + 4);
				Main.editor.cursorY = (linesfound.get(occurnum - 1) - 1) + 2;
				
				break;*/
				
			/*case "replace":						// Deprecated. Use Search/Replace.
				if (Main.editor.isReadOnly) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = 0; i < Main.editor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				for (Integer i : linesfound) {
					String s = new String(Main.editor.toCharArray(Main.editor.lines.get(i).getChars()));
					
					s = s.replaceAll(args[0], args[1]);
					
					Main.editor.register(new StringBuilder(s), i);
				}
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			case "replacesel":
				if (Main.editor.isReadOnly) break;
				
				if (!Main.editor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = Main.editor.lines.get(i);
					String s = new String(Main.editor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				for (Integer i : linesfound) {
					String s = new String(Main.editor.toCharArray(Main.editor.lines.get(i).getChars()));
					
					s = s.replaceAll(args[0], args[1]);
					
					Main.editor.register(new StringBuilder(s), i);
				}
				
				Main.editor.editing.setSaved(false);
				
				break;*/
				
			/*case "select":
				line1 = Integer.parseInt(args[0]);
				line2 = Integer.parseInt(args[1]);
				
				index1 = 0;
				index2 = Main.editor.lines.get(line2 - 1).getChars().size(); // é -1 porque no array é base 0, aqui é base 1
				
				selecting = true;
				
				break;*/
			}
		}
		else if (args.length == 3) {
			switch (com) {
			case "gengetter":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				String[] strs = { };
				
				switch (args[0].toLowerCase()) { // TODO o gengetter do cs tbm tem o setter, talvez dar uma arrumada nisso
				case "java":
					String[] javastrs = { "public " + args[2] + " get" + CodeEditor.capitalizeFirstLetter(args[1]) + "() {", "    return " + args[1] + ";", "}"};
					
					strs = javastrs;
					
					break;
					
				case "cs":
					String[] csstrs = { "public " + args[2] + " " + args[1] + " { get { return " + args[1] + "; } set { " + args[1] + " = value; } };" };
					
					strs = csstrs;
					
					break;
				}
				
				if (strs.length == 0) return;
				
				for (int i = 0; i < strs.length - 1; i++) {
					StringBuilder spaces = new StringBuilder();
					
					for (int j = 0; j < Main.editor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					Main.editor.addNewLine(Main.editor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strs.length; i++) {
					StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get((Main.editor.cursorY - 1) + i).getChars())));
					
					b.append(strs[i]);
					
					Main.editor.register(b, (Main.editor.cursorY - 1) + i);
				}
				
				//Main.editor.cursorY += strs.length - 1;
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			case "gensetter":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				String[] strss = { };
				
				switch (args[0].toLowerCase()) {
				case "java":
					String[] javastrs = { "public void set" + CodeEditor.capitalizeFirstLetter(args[1]) + "(" + args[2] + " " + args[1] + ") {", "    this." + args[1] + " = " + args[1] + ";", "}"};
					
					strss = javastrs;
					
					break;
				}
				
				if (strss.length == 0) return;
				
				for (int i = 0; i < strss.length - 1; i++) {
					StringBuilder spaces = new StringBuilder();
					
					for (int j = 0; j < Main.editor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					Main.editor.addNewLine(Main.editor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strss.length; i++) {
					StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get((Main.editor.cursorY - 1) + i).getChars())));
					
					b.append(strss[i]);
					
					Main.editor.register(b, (Main.editor.cursorY - 1) + i);
				}
				
				//Main.editor.cursorY += strss.length - 1;
				
				Main.editor.editing.setSaved(false);
				
				break;
			}
		}
		
		//Main.writeFile(Main.settingsFile);
		Main.editor.setCursorWithinBounds();
	}
	
	public void tick() {
		x = Main.screen.getWidth() / 2 - 250;
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
			IDEComponent.toRemove.add(this);
			
			active = false;
		}
		
		if (KeyInput.isKeyPressed()) {
			KeyInput.updateKeys();
			
			// Shortcuts Area
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				builder = new StringBuilder();
				cursorIndex = 0;
			}
			
			if (KeyInput.isControlDown() || KeyInput.isAltDown() || KeyInput.isAltGrDown()) return;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
				builder = new StringBuilder(lastCommand);
				
				cursorIndex = lastCommand.length();
			
				commandHints.clear();
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
				builder = new StringBuilder();
			
				commandHints.clear();
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && cursorIndex > 0) {
				cursorIndex--;
				
				return;
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT && cursorIndex < builder.length()) {
				cursorIndex++;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE) {
				if (builder.length() == 0 || cursorIndex == builder.length()) builder.append(" ");
				else builder.insert(cursorIndex, " ");
				
				cursorIndex++;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE && cursorIndex > 0) {
				builder.deleteCharAt(cursorIndex - 1);
				
				cursorIndex--;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE && cursorIndex < builder.length()) {
				builder.deleteCharAt(cursorIndex);
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				lastCommand = builder.toString(); // tem que ser o último que você digitou, não o último que executou (pq o sistema executa)
				
				runCommand(builder.toString());
				
				IDEComponent.toRemove.add(this);
				
				active = false;
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB) {
				KeyInput.updateKeys();
				
				if (commandHints.isEmpty()) return;
				
				builder = new StringBuilder(commandHints.get(comIndex >= commandHints.size() ? commandHints.size() - 1 : comIndex).split(" ")[0]);
				
				cursorIndex = builder.length();
				comIndex++;
				
				if (comIndex > commandHints.size()) comIndex = 0;
				
				changeHints = false;
				
				return;
			}
			
			if (cursorIndex < 0) cursorIndex = 0;
			if (cursorIndex > builder.length()) cursorIndex = builder.length();
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = Main.editor.addAccents(keyCode, c);
			
			comIndex = 0;
			changeHints = true;
			
			if (KeyInput.getCharPressed() < 33 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
			
			if (builder.length() == 0 || cursorIndex == builder.length()) builder.append(c);
			else builder.insert(cursorIndex, c); // o erro era ordem de parâmetros, pois usar um char como int tbm vale
			
			cursorIndex++;
		}
		
		if (changeHints) {
			commandHints.clear();
			
			for (int i = 0; i < onlyCommands.length; i++) {
				String s = onlyCommands[i];
				String dgt = builder.toString().split(" ")[0]; // dgt = digitado
				
				if (s.contains(dgt)) commandHints.add(commands[i]);
			}
			
			if (builder.toString().equals("")) commandHints.clear();
		}
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(new Color(0, 0, 0, 0.3f));
		g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);
		
		g2.setStroke(new BasicStroke(3f));
		
		if (!builder.toString().isEmpty()) {
			g.setColor(Colors.explorer);
			g2.fillRect(x - 155, y + height + 15, width + 320, height * 20); // centralizar essa borda
			
			g.setColor(Colors.explorerLight);
			g2.drawRect(x - 155, y + height + 15, width + 320, height * 20);
		}
		
		g.setColor(Colors.explorer);
		g2.fillRect(x - 100, y, width + 200, height);
			
		g.setColor(Colors.explorerLight);
		g2.drawRect(x - 100, y, width + 200, height);
		
		Fonts.drawString(Texts.insertCommand, Main.screen.getWidth() / 2 - 100, y - 25, new IDEFont(Fonts.otherNormal, 20), g);
		
		Fonts.drawString(builder.toString(), (x - 100) + 4, y + 8, new IDEFont(Fonts.otherNormal, 18), g);
		
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor) {
			g.setColor(Colors.cursor);
			g.drawLine(((x - 100) + 4) + (cursorIndex * 14), y + 8, (x - 100) + 4 + (cursorIndex * 14), y + 8 + 18);
		}
		
		for (int i = 0; i < commandHints.size(); i++) {
			String cmd = commandHints.get(i);
			
			IDEFont font = (!changeHints && i == comIndex - 1) || (comIndex == commandHints.size() && i == comIndex) ? new IDEFont(Fonts.lightGrayNormal, 20) : new IDEFont(Fonts.otherNormal, 20);
			
			Fonts.drawString(cmd, x - 145, y + height + 20 + (22 * i), font, g2);
		}
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.enter_Execute, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.ctrl_del_Clear, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.tab_Cycle, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 90, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
