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
import java.util.Stack;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import ide.codeeditor.CodeEditor;
import ide.codeeditor.FileReadMode;
import ide.codeeditor.Tab;
import ide.explorer.Explorer;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.searchreplace.SearchReplaceCore;
import ide.util.Animation;
import ide.util.Colors;
import ide.util.Language;
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
	
	private int originalWidth;
	
	public static Stack<String> typedCommands = new Stack<>();
	public static int tcIndex = 0; // typed commands index
	
	private static boolean changeHints = true;
	private static int comIndex = 0;
	
	public static final String[] commands = { "cmd", "sysexp", "closealltabs", "resettabscroll", "generateconfigfile", "getlang", "getfontsize", "getwhitespaces",
			"reseteditorscroll", "deselect", "copy", "del", "cut", "paste", "selectline", "version", "resetexplorerdrag", "resetundoredo",
			"selectall", "toggleexplorer", "loadconfigfile", "resetreadmode", "resetfontsize", "togglewhitespaces",
			"sysout", "syso", "cout", "coutend", "stdcout", "stdcoutend", "writeline", "readline", "syserr", "clog", "cerr", "gendiv", "closebasefolder",
			"revertcolors", "togglecodehelpers", "gotocursor", "togglereadonly", "closetab int:tab_index", "setexplorerdrag int:px",
			"setfontsize int:size/default", "insertchar int:ascii_code", "setreadmode str:mode", "setlang str:lang", "setwhitespaces bool:true/false",
			"gendiv str:class_name", "gensnippet str:type", "selecttab int:index",
			"lorem int:num_words", "swaptabs int:tab_from int:tab_to", "openfile str:file",
			"setcursorpos int:x int:y",
			"getproperty str:property",
			"setproperty str:property str:new_value",
			"gengetter str:lang str:variable_name str:variable_type",
			"gensetter str:lang str:variable_name str:variable_type",
			"gensnippet cs/csmain str:class_name str:namespace"
			};
	
	public static List<String> commandHints = new ArrayList<>();
	
	private static JFileChooser chooser;
	
	private static boolean typedFlag = false; // é necessário que exista, pelo menos por enquanto
	
	public CommandTerminal(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		originalWidth = width;
		
		commandHints.clear();
		
		builder = new StringBuilder();
		
		CodeEditor.setSystemLook();
		
		cursor = new Animation() {
			private boolean flip = false;
			
			public void play() {
				while (true) {
					showCursor = !flip;
	
					flip = !flip;
					
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
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
					
					File dir = Explorer.scope != null ? Explorer.scope.getRegent() : (Main.baseFolder == null ? new File(System.getProperty("user.dir")) : Main.baseFolder); // eu tava fazendo o equivalente a isso: null.regent != null
					
					pb.directory(dir);
					
					pb.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			case "sysexp":
				if (Main.baseFolder == null) return;
				
				try {
					Main.desktop.open(new File(Explorer.files.get(0).getRegent().getPath()).getParentFile());
				} catch (IOException e) {
					e.printStackTrace();
				}
				break;
				
			case "closealltabs":
				if (Main.baseFolder == null) return;
				
				Main.editor.tabs.clear();
				break;
				
			case "resettabscroll":
				if (Main.baseFolder == null) return;
				
				Main.editor.tabScr = 0;
				break;
				
			case "reseteditorscroll":
				if (Main.baseFolder == null) return;
				
				Main.editor.scrX = 0;
				Main.editor.scrY = 0;
				break;
				
			case "resetreadmode":
				if (Main.baseFolder == null) return;
				
				runCommand("setreadmode normal");
				break;
				
			case "resetfontsize":
				if (Main.baseFolder == null) return;
				
				runCommand("setfontsize 16");
				break;
				
			case "getlang":
				CodeEditor.setSystemLook();
				
				JOptionPane.showMessageDialog(null, Texts.langIs + " " + (Main.lang == Language.PORT ? "Português." : "English."), Texts.getLang, JOptionPane.INFORMATION_MESSAGE);
				break;
				
			case "getfontsize":
				CodeEditor.setSystemLook();
				
				JOptionPane.showMessageDialog(null, Texts.fontSizeIs + " " + CodeEditor.FONT_SIZE + " pixels.", Texts.getFontSize, JOptionPane.INFORMATION_MESSAGE);
				break;
				
			case "getwhitespaces":
				CodeEditor.setSystemLook();
				
				JOptionPane.showMessageDialog(null, Texts.whitespaceIs + " " + (Main.lang == Language.PORT ? (CodeEditor.showWhitespace ? "ligados." : "desligados.") : (CodeEditor.showWhitespace ? "on." : "off.")), Texts.getLang, JOptionPane.INFORMATION_MESSAGE);
				break;
				
			case "version":
				CodeEditor.setSystemLook();
				
				JOptionPane.showMessageDialog(null, Main.PROGRAM_NAME + " " + Texts.version + " " + Main.VERSION + " © Boot 2022, All Rights Reserved.", Texts.version, JOptionPane.INFORMATION_MESSAGE);
				break;
				
			case "resetexplorerdrag":
				Main.explorer.setDrag(280);
				break;
				
			case "deselect":
				if (Main.editor.editing == null) break;
				
				Main.editor.line1 = 0; // 0, não é 1 não?
				Main.editor.line2 = 0;
				
				Main.editor.index1 = 0;
				Main.editor.index2 = 0;
				
				Main.editor.selecting = false;
				break;
				
			case "resetundoredo":
				/*if (Main.editor.cursorThread.isAlive() || Main.editor.cursorThread.getState() != State.TERMINATED) {
					try {
						Main.editor.cursorThread.interrupt();
					} catch (Exception e) {} // usar somente quando necessário
				}
				
				Main.editor.cursorThread = new Thread() {
					public void run() {
						Main.editor.cursor.play();
					}
				};
				
				Main.editor.cursorThread.start();*/
				
				Main.editor.undo.clear();
				Main.editor.redo.clear();
				
				break;
				
			case "copy":
				if (Main.editor.editing == null) break;
				if (!Main.editor.selecting) break;
				
				try {
					List<String> lines = new ArrayList<>();
					String str = "";
					
					if (Main.editor.line1 != Main.editor.line2) { // se não selecionou uma linha só (selecionou várias)
						for (int i = Main.editor.line1 - 1; i < Main.editor.line2; i++) {
							if (i == Main.editor.line1 - 1) {
								if (Main.editor.index1 > Main.editor.lines.get(i).getChars().size())
									Main.editor.index1 = Main.editor.lines.get(i).getChars().size();
								
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
				} catch (Exception e) {
					return;
				}
				
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
							Main.editor.addToUndo();
							
							Main.editor.editing.setSaved(false);
					
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
					//runCommand("deselect"); // TODO talvez não desselecionar, deletar com algum index no 0, né?
					
					if (Main.editor.index1 >= Main.editor.lines.get(Main.editor.line1 - 1).getChars().size())
						Main.editor.index1 = Main.editor.lines.get(Main.editor.line1 - 1).getChars().size();
					
					if (Main.editor.index2 >= Main.editor.lines.get(Main.editor.line2 - 1).getChars().size())
						Main.editor.index2 = Main.editor.lines.get(Main.editor.line2 - 1).getChars().size();
					
					runCommand("del");
				}
				
				Main.editor.setCursorWithinBounds();
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
				
				int y = Main.editor.cursorY - 1;
				
				Main.editor.index1 = 0;
				Main.editor.index2 = Main.editor.lines.get(y).getChars().size();
				
				Main.editor.line1 = y + 1;
				Main.editor.line2 = y + 1;
				
				Main.editor.selecting = true;
				break;
				
			case "selectall":
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
					
					if (!fl.getName().contains(Main.CONFIG_FILE_EXTENSION)) fl = new File(fl.getName() + Main.CONFIG_FILE_EXTENSION);
					
					ListableFile.generateConfigFile(fl);
					
					CodeEditor.setSystemLook();
					String[] options = { Texts.openFolder, Texts.cancel, /*Texts.openInNewTab*/ Texts.openInDefaultEditor };
    				
    				CodeEditor.setSystemLook();
    				int selectedOption = JOptionPane.showOptionDialog(null, Texts.wantOpenFile, Texts.wouldEdit, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
    				
    				if (selectedOption == 0) {
    					try {
							Main.desktop.open(fl.getParentFile());
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    				
    				if (selectedOption == 2) {
    					try {
							Main.desktop.open(fl);
						} catch (IOException e) {
							e.printStackTrace();
						}
    				}
    				
    				/*if (selectedOption == 2) {
    					if (!fl.exists())
    						System.out.println("a");
    					
    					ListableFile.addTab(ListableFile.newListableFile(fl), true);
    				}*/
				}
				break;
				
			case "toggleexplorer":
				if (expOff)
					Main.editor.setX(Main.explorer.getWidth());
				else
					Main.editor.setX(0);
				
				expOff ^= true;	// uma forma de togglar boolean (^ é xor gate)
				
				if (Explorer.searchReplaceActive)
					SearchReplaceCore.dispose();
				
				break;
				
			case "togglewhitespaces":
				CodeEditor.showWhitespace ^= true;
				break;
				
			case "loadconfigfile":
				option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					//Main.conffile = chooser.getSelectedFile().getPath();
					
					Main.load(chooser.getSelectedFile().getPath());
					
					if (!ListableFile.hasAltered) {
						String[] options = { Texts.yes, Texts.no };
						
						CodeEditor.setSystemLook();
						int selectedOption = JOptionPane.showOptionDialog(null, Texts.configFileNotChanged, Texts.didNothing, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
						
						if (selectedOption != 1) break;
						
						Main.conffile = "none";
						Main.hasConfigFile = false;
						
						runCommand("revertcolors");
					}
					
					if (typedFlag) {
						String[] options = {"  Ok  " };
						
						CodeEditor.setSystemLook();
						JOptionPane.showOptionDialog(null, Texts.pleaseRestart, Texts.restartRequired, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
					}
				}
				
				break;
				
			/*case "unloadconfigfile":
				Main.conffile = "none";
				
				Main.writeFile(Main.settingsFile);
				runCommand("revertconfigfile");
				
				Main.hasConfigFile = false;
				
				if (typedFlag) {
					String[] options = {"  Ok  " };
					
					CodeEditor.setSystemLook();
					JOptionPane.showOptionDialog(null, Texts.pleaseRestart, Texts.restartRequired, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				}
				
				break;*/
			
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
				
				b.insert(Main.editor.cursorX, "cout << \"\";");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 9;
				
				break;
				
			case "coutend":
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
				
				b.insert(Main.editor.cursorX, "std::cout << \"\";");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 14;
				
				break;
				
			case "stdcoutend":
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
				
			case "readline":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "Console.ReadLine();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 17;
				
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
				
			case "cerr":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				b.insert(Main.editor.cursorX, "console.error();");
				
				Main.editor.register(b, Main.editor.cursorY - 1);
				
				Main.editor.editing.setSaved(false);
				
				Main.editor.cursorX += 14;
				
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
				
				IDEComponent.toRemove.add(Main.oneFolder);
				IDEComponent.toRemove.add(Main.returnBase);
				IDEComponent.toRemove.add(Main.newFile);
				IDEComponent.toRemove.add(Main.newFolder);
				IDEComponent.toRemove.add(Main.reload);
				
				Main.writeFile(Main.settingsFile);
				
				break;
				
			case "revertcolors":
				Colors.revertColors();
				
				String[] options = { "Ok" };
				
				CodeEditor.setSystemLook();
				JOptionPane.showOptionDialog(null, Texts.pleaseRestart, Texts.restartRequired, JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, options, options[0]);
				
				/*Fonts.initFonts(Main.fntnr, Main.fntbl);
				Main.spritesheet = new Spritesheet(Main.sprsh);
				
				CodeEditor.FONT_SIZE = 16;
				
				Main.lang = Language.ENG;
				Texts.setTexts(Main.lang);*/
				
				//Main.load(Main.conffile);
				
				break;
				
			case "togglecodehelpers":
				if (Main.editor.editing == null) break;
				
				Main.editor.codeHelpersOn ^= true; // método prático de inverter boolean, porque em Assembly mais ou menos seria assim: xor codehelperson, true (lógico que o nome da variável n seria esse né :/)
				break;
				
			case "gotocursor":
				if (Main.editor.editing == null) break;
				if (!(Main.editor.drawcx < Main.editor.getX() + (CodeEditor.FONT_SIZE * 4) || Main.editor.drawcx > Main.screen.getWidth() || Main.editor.drawcy < Main.editor.getY() || Main.editor.drawcy > Main.screen.getHeight())) break;
				
				//Main.editor.scrY = (Main.editor.cursorY * (CodeEditor.FONT_SIZE + (CodeEditor.FONT_SIZE / 3)));
				
				//Main.editor.scrX = CodeEditor.ruleOf3(100, 840, Main.editor.cursorX);
				//Main.editor.scrY = CodeEditor.ruleOf3(1000, 16791, Main.editor.cursorY - 1); // se o cursor y for 1000, o scroll y é 16791, agora, se o cursor y for x, quantos será o scroll y? Esse método faz isso.
				
				//if (Main.editor.cursorY <= 7) Main.editor.scrY = 0;
				
				int drawcx = Main.editor.drawcx;
				int drawcy = Main.editor.drawcy;
				
				while (drawcx < Main.editor.getX() + (CodeEditor.FONT_SIZE * 10)) { // * 6 pra dar um espaço
					Main.editor.scrX -= CodeEditor.FONT_SIZE;
					drawcx += CodeEditor.FONT_SIZE;
				}
				
				while (drawcx > Main.screen.getWidth() - (Main.screen.getWidth() / 10)) {
					Main.editor.scrX += CodeEditor.FONT_SIZE;
					drawcx -= CodeEditor.FONT_SIZE;
				}
				
				///////
				
				while (drawcy < CodeEditor.MIN_Y) {
					Main.editor.scrY -= CodeEditor.FONT_SIZE;
					drawcy += CodeEditor.FONT_SIZE;
				}
				
				while (drawcy > Main.screen.getHeight() - (Main.screen.getHeight() / 10) - 30) {
					Main.editor.scrY += CodeEditor.FONT_SIZE;
					drawcy -= CodeEditor.FONT_SIZE;
				}
				
				break;
				
			case "togglereadonly":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly && (Main.editor.readMode != FileReadMode.NORMAL || !Main.editor.editing.getRegent().getRegent().canWrite())) break;
				
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
				
			case "selecttab":
				try {
					int args0 = Integer.parseInt(args[0]);
					
					if (Main.editor.tabs.size() == 0 ||
						    args0 < 0 ||
						    args0 > Main.editor.tabs.size())
							return;
					
					Tab sel = Main.editor.tabs.get(args0);
					
					sel.select();
					
					while (sel.getX() + Main.editor.tabScr >= Main.screen.getWidth())
						Main.editor.tabScr -= 203;
					
					while (sel.getX() + Main.editor.tabScr < Main.editor.getX())
						Main.editor.tabScr += 203;
					
					break;
				} catch (NumberFormatException e) {
					break;
				}
			
			case "openfile":
				File f = new File(Explorer.getScopePath() + "/" + args[0]);
				if (!f.exists()) break;
				
				if (!CodeEditor.isBinary(ListableFile.getFileExtension(f)))
					ListableFile.addTab(ListableFile.search(f, f.getParentFile()), true);
				
				break;
			
			case "setreadmode":
				try {
					Main.editor.readMode = FileReadMode.valueOf(args[0].toUpperCase());
					
					Main.editor.lines = Main.editor.readFile(Main.editor.editing.getRegent().getRegent());
					
					if (Main.editor.readMode == FileReadMode.NORMAL && Main.editor.isReadOnly && !CodeEditor.isBinary(ListableFile.getFileExtension(Main.editor.editing.getRegent().getRegent()))) runCommand("togglereadonly");
					if (Main.editor.readMode != FileReadMode.NORMAL && !Main.editor.isReadOnly) runCommand("togglereadonly");
				} catch (Exception e) {} // argumento errado
				break;
				
			case "setwhitespaces":
				CodeEditor.showWhitespace = Boolean.valueOf(args[0]);
				break;
				
			case "setexplorerdrag":
				try {
					int px = Integer.parseInt(args[0]);
					
					Main.explorer.setDrag(px);
				} catch (NumberFormatException e) {
					break;
				}
				break;
				
			case "setfontsize":
				if (Main.editor.editing == null) break;
				
				Main.editor.editing.save();				
				int prevsize = CodeEditor.FONT_SIZE;
				
				if (args[0].equals("default"))
					CodeEditor.FONT_SIZE = prevsize;
				
				try {
					int a0 = Integer.parseInt(args[0]);
					
					if (a0 < 8) {
						//CodeEditor.setSystemLook();
						
						//JOptionPane.showMessageDialog(null, Texts.fontBelowMinimum, Texts.belowMinimum, JOptionPane.OK_OPTION);
						
						return;
					}
					
					CodeEditor.FONT_SIZE = a0;
					//CodeEditor.LINE_NUMBER_WIDTH = CodeEditor.FONT_SIZE * 4;
						
					if (Main.editor.editing != null)
						Main.editor.lines = Main.editor.readFile(Main.editor.editing.getRegent().getRegent());
				} catch (NumberFormatException | IOException e) {
					CodeEditor.FONT_SIZE = prevsize;
				}
				
				runCommand("gotocursor");
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
				
			case "gensnippet":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				String[] strs = { };
				
				String classname = ListableFile.getFileNameWithoutExtension(Main.editor.editing.getRegent().getRegent());
				
				switch (args[0].toLowerCase()) {
				case "html":
					String[] htmlstrs = { "<!DOCTYPE html>", "<html>", "    <head>", "        <title></title>", "        ", "        <meta charset=\"UTF-8\">", "    </head>", "    <body>", "    </body>", "</html>" };
					
					strs = htmlstrs;
					
					break;
					
				case "html5":
					String[] htmlnewstrs = { "<!DOCTYPE html>", "<html>", "    <head>", "        <title></title>", "        ", "        <meta charset=\"UTF-8\">", "        <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">", "        <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">", "    </head>", "    <body>", "    </body>", "</html>" };
					
					strs = htmlnewstrs;
					
					break;
					
				case "css":
					String[] cssstrs = { "* {", "    margin: 0;", "    padding: 0;", "    box-sizing: border-box;", "    font-family: sans-serif;", "}"};
					
					strs = cssstrs;
					
					break;
					
				case "csssimple":
					String[] csssstrs = { "* {", "    margin: 0;", "    padding: 0;", "}"};
					
					strs = csssstrs;
					
					break;
					
				case "vbmain":
					String[] vbmstrs = { "Imports System", "", "Module " + classname, "    ", "    Sub Main()", "        ", "    End Sub", "    ", "End Module" };
					
					strs = vbmstrs;
					
					break;
					
				case "vb":
					String[] vbstrs = { "Imports System", "", "Module " + classname, "    ", "End Module" };
					
					strs = vbstrs;
					
					break;
					
				case "portugol":
					String[] portstrs = { "programa {", "    ", "    funcao inicio() {", "        ", "    }", "}" };
					
					strs = portstrs;
					
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
					String[] javamstrs = { "public class " + classname + " {", "    ", "    public static void main(String[] args) {", "        ", "    }", "}"};
					
					strs = javamstrs;
					
					break;
					
				case "cpp":
					String[] cppstrs = { "#include <iostream>", "", "using namespace std;" };
					
					strs = cppstrs;
					
					break;
					
				case "cppmain":
					String[] cppmstrs = { "#include <iostream>", "", "using namespace std;", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cppmstrs;
					
					break;
					
				case "cppmainargs":
					String[] cppmastrs = { "#include <iostream>", "", "using namespace std;", "", "int main(int argc, char *argv[])", "{", "    return 0;", "}"};
					
					strs = cppmastrs;
					
					break;
					
				case "c":
					String[] cstrs = { "#include <stdio.h>" };
					
					strs = cstrs;
					
					break;
					
				case "cmain":
					String[] cmstrs = { "#include <stdio.h>", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cmstrs;
					
					break;
					
				case "cmainargs":
					String[] cmastrs = { "#include <stdio.h>", "", "int main(int argc, char *argv[])", "{", "    return 0;", "}"};
					
					strs = cmastrs;
					
					break;
					
				case "h":
					String[] hstrs = { "#ifndef " + classname.toUpperCase() + "_H", "#define " + classname.toUpperCase() + "_H", "", "#endif" };
					
					strs = hstrs;
					
					break;
					
				case "ino":
					String[] inostrs = { "void setup()", "{", "    ", "}", "", "void loop()", "{", "    ", "}"};
					
					strs = inostrs;
					
					break;
				}
				
				if (strs.length == 0) return;
				
				for (int i = 0; i < strs.length - 1; i++) {
					StringBuilder spaces = new StringBuilder();
					
					for (int j = 0; j < CodeEditor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					Main.editor.addNewLine(Main.editor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strs.length; i++) {
					StringBuilder bb = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get((Main.editor.cursorY - 1) + i).getChars())));
					
					bb.append(strs[i]);
					
					Main.editor.register(bb, (Main.editor.cursorY - 1) + i);
				}
				
				//Main.editor.cursorY += strs.length - 1;
				
				Main.editor.editing.setSaved(false);
				
				break;
				
			case "getproperty":
				try {
					String s = System.getProperty(args[0]);
					
					if (s == null || s == "null") {
						CodeEditor.setSystemLook();
						
						JOptionPane.showMessageDialog(null, Texts.propertyDoesntExist, Texts.getProperty, JOptionPane.ERROR_MESSAGE);
						
						break;
					}
					
					CodeEditor.setSystemLook();
					
					JOptionPane.showMessageDialog(null, Texts.valueOfTheProperty + " " + args[0] + " " + Texts.is + " " + s, Texts.getProperty, JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					CodeEditor.setSystemLook();
					
					JOptionPane.showMessageDialog(null, Texts.propertyDoesntExist, Texts.getProperty, JOptionPane.ERROR_MESSAGE);
				}
				
				break;
				
			case "lorem":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				StringBuilder bl = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())));
				
				try {
					String lorem = CodeEditor.generateLoremIpsum(Integer.parseInt(args[0]));
					
					bl.insert(Main.editor.cursorX, lorem);
					
					Main.editor.register(bl, Main.editor.cursorY - 1);
					Main.editor.editing.setSaved(false);
				} catch (Exception e) { break; }
					
				break;
			}
		}
		
		else if (args.length == 2) {
			switch (com) {
			case "swaptabs":
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
				
			case "setproperty":
				try {
					System.setProperty(args[0], args[1]);
					
					String s = System.getProperty(args[0]);
					
					if (s == null || s == "null") {
						CodeEditor.setSystemLook();
						
						JOptionPane.showMessageDialog(null, Texts.propertyDoesntExist, Texts.setProperty, JOptionPane.ERROR_MESSAGE);
						
						break;
					}
					
					CodeEditor.setSystemLook();
					
					JOptionPane.showMessageDialog(null, Texts.newValueOfTheProperty + " " + args[0] + " " + Texts.is + " " + s + ".", Texts.setProperty, JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception e) {
					CodeEditor.setSystemLook();
					
					JOptionPane.showMessageDialog(null, Texts.propertyDoesntExist, Texts.setProperty, JOptionPane.ERROR_MESSAGE);
				}
				
				break;
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
					
					for (int j = 0; j < CodeEditor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
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
					
					for (int j = 0; j < CodeEditor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
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
				
			case "gensnippet":
				if (Main.editor.editing == null) break;
				if (Main.editor.isReadOnly) break;
				
				String[] strs1 = { };
				
				String classname = args[1];
				String namespace = args[2];
				
				switch (args[0].toLowerCase()) {
				
				case "cs":
					String[] csstrs = { "using System;", "using System.Collections.Generic;", "using System.Linq;", "using System.Text;", "using System.Threading.Tasks;", "", "namespace " + namespace, "{", "    ", "    public class " + classname, "    {", "        ", "    }", "}"};
					
					strs1 = csstrs;
					
					break;
					
				case "csmain":
					String[] csmstrs = { "using System;", "using System.Collections.Generic;", "using System.Linq;", "using System.Text;", "using System.Threading.Tasks;", "", "namespace " + namespace, "{", "    ", "    public class " + classname, "    {", "        ", "        static void Main(string[] args)", "        {", "            ", "        }", "    }", "}"};
					
					strs1 = csmstrs;
					
					break;
				}
				
				if (strs1.length == 0) return;
				
				for (int i = 0; i < strs1.length - 1; i++) {
					StringBuilder spaces = new StringBuilder();
					
					for (int j = 0; j < CodeEditor.countChar(new String(CodeEditor.toCharArray(Main.editor.lines.get(Main.editor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					Main.editor.addNewLine(Main.editor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strs1.length; i++) {
					StringBuilder bb = new StringBuilder(new String(CodeEditor.toCharArray(Main.editor.lines.get((Main.editor.cursorY - 1) + i).getChars())));
					
					bb.append(strs1[i]);
					
					Main.editor.register(bb, (Main.editor.cursorY - 1) + i);
				}
				
				//Main.editor.cursorY += strs.length - 1;
				
				Main.editor.editing.setSaved(false);
				
				break;
			}
		}
		
		Main.editor.callAutomaticColor();
		//Main.writeFile(Main.settingsFile);
		Main.editor.setCursorWithinBounds();
		
		typedFlag = false;
	}
	
	public void tick() {
		x = Main.screen.getWidth() / 2 - 250;
		
		if (x < 110) x = 110;
		
		if (x + width > Main.screen.getWidth() - 10) width = Main.screen.getWidth() - 10;
		else width = originalWidth;
		
		//System.out.println(width + ", " + Main.screen.getWidth());
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
			IDEComponent.toRemove.add(this);
			
			active = false;
		}
		
		if (changeHints) {
			commandHints.clear();
			
			String[] onlyCommands = new String[commands.length];
			
			for (int i = 0; i < commands.length; i++) {
				String s = commands[i];
				
				onlyCommands[i] = s.split(" ")[0];
			}
			
			for (int i = 0; i < onlyCommands.length; i++) {
				String s = onlyCommands[i];
				String dgt = builder.toString().split(" ")[0]; // dgt = digitado
				
				if (s.contains(dgt)) commandHints.add(commands[i]);
			}
			
			if (builder.toString().equals("")) commandHints.clear();
		}
	}
	
	public synchronized void type() {
		if (SetFileName.added || !CommandTerminal.active || RenameFile.added || Explorer.selected != null) return;
		
		if (KeyInput.isKeyPressed()) {
			
			// Shortcuts Area
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) { // Ctrl + Delete - Limpar
				builder = new StringBuilder();
				cursorIndex = 0;
				
				commandHints.clear();
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C - Copiar (Tudo)
				CodeEditor.copyText(builder.toString());
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V - Colar
				if (cursorIndex >= builder.length()) {
					builder.append(CodeEditor.clipboard);
					cursorIndex += CodeEditor.clipboard.length();
				}
				else {
					builder.insert(cursorIndex, CodeEditor.clipboard);
					cursorIndex += CodeEditor.clipboard.length();
				}
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X - Recortar (Tudo)
				CodeEditor.copyText(builder.toString());
				
				builder = new StringBuilder();
				cursorIndex = 0;
				
				commandHints.clear();
			}
			
			if (KeyInput.isControlDown() || KeyInput.isAltDown() || KeyInput.isAltGrDown()) return;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
				if (tcIndex < 0)
					tcIndex = 0;
				
				builder = new StringBuilder(typedCommands.get(tcIndex));
				
				cursorIndex = typedCommands.get(tcIndex).length();
				tcIndex--;
				
				commandHints.clear();
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
				tcIndex++;
				
				if (tcIndex >= typedCommands.size()) {
					tcIndex = typedCommands.size() - 1;
				}
				
				builder = new StringBuilder(typedCommands.get(tcIndex));
				
				cursorIndex = typedCommands.get(tcIndex).length();
			
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
				//lastCommand = builder.toString(); // tem que ser o último que você digitou, não o último que executou (pq o sistema executa)
				
				typedCommands.push(builder.toString());
				tcIndex = typedCommands.size() - 1;
				
				typedFlag = true;
				
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
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(new Color(0, 0, 0, 0.3f));
		g.fillRect(0, 0, Main.screen.getWidth(), Main.screen.getHeight());
		
		g2.setStroke(new BasicStroke(3f));
		
		if (!builder.toString().isEmpty()) {
			g.setColor(Colors.explorer);
			g2.fillRect(x - 155 > 10 ? x - 155 : 10, y + height + 15, width + 320, height * 20); // centralizar essa borda
			
			g.setColor(Colors.explorerLight);
			g2.drawRect(x - 155 > 10 ? x - 155 : 10, y + height + 15, width + 320, height * 20);
		}
		
		g.setColor(Colors.explorer);
		g2.fillRect(x - 100, y, width + 200, height);
			
		g.setColor(Colors.explorerLight);
		g2.drawRect(x - 100, y, width + 200, height);
		
		Fonts.drawString(Texts.insertCommand, Main.screen.getWidth() / 2 - 100 > 10 ? Main.screen.getWidth() / 2 - 100 : 10, y - 25, new IDEFont(Fonts.otherNormal, 20), g);
		
		Fonts.drawString(builder.toString(), (x - 100) + 4, y + 8, new IDEFont(Fonts.otherNormal, 18), g);
		
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor) {
			g.setColor(Colors.cursor);
			g.drawLine(((x - 100) + 4) + (cursorIndex * 14), y + 8, (x - 100) + 4 + (cursorIndex * 14), y + 8 + 18);
		}
		
		for (int i = 0; i < commandHints.size(); i++) {
			String cmd = commandHints.get(i);
			IDEFont font = (!changeHints && i == comIndex - 1) || (comIndex == commandHints.size() && i == comIndex) ? new IDEFont(Fonts.lightGrayNormal, 20) : new IDEFont(Fonts.otherNormal, 20);
			
			Fonts.drawString(cmd, x - 145 > 20 ? x - 145 : 20, y + height + 20 + (22 * i), font, g2);
		}
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.enter_Execute, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.ctrl_del_Clear, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.tab_Cycle, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 90, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Ctrl + C] " + Texts.copy, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 130, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Ctrl + V] " + Texts.paste, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 160, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Ctrl + X] " + Texts.cut, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 190, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
