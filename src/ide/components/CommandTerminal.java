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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

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
	
	public static String lastCommand = ""; // TODO debugar isso aqui
	
	private static JFileChooser chooser;
	
	public static final String[] commands = { "cmd", "sysexp", "closealltabs", "resettabscroll",
			"reseteditorscroll", "deselect", "copy", "del", "cut", "paste", "selectline",
			"selectall", "generateconfigfile", "toggleexplorer", "loadconfigfile", "unloadconfigfile",
			"sysout", "syso", "cout", "stdcout", "writeline", "syserr", "clog", "gendiv", "closebasefolder",
			"revertconfigfile", "togglecodehelpers", "gotocursor", "togglereadonly", "closetab int:tab_index",
			"gotoline int:line", "setfontsize int:size/default", "insertchar int:ascii_code",
			"gendiv str:class_name", "genbase str:type",
			"search str:word", "searchsel str:word", "lorem int:num_words", "ordertab int:tab_from int:tab_to",
			"setcursorpos int:x int:y", "search str:word int:occurence_index", "searchsel str:word int:occurence_index",
			"replace str:word_from str:word_to", "replacesel str:word_from str:word_to",
			"gengetter str:lang str:variable_name str:variable_type",
			"gensetter str:lang str:variable_name str:variable_type" };
	
	public static final String[] onlyCommands = { "cmd", "sysexp", "closealltabs", "resettabscroll",
			"reseteditorscroll", "deselect", "copy", "del", "cut", "paste", "selectline",
			"selectall", "generateconfigfile", "toggleexplorer", "loadconfigfile", "unloadconfigfile",
			"sysout", "syso", "cout", "stdcout", "writeline", "syserr", "clog", "gendiv", "closebasefolder",
			"revertconfigfile", "togglecodehelpers", "gotocursor", "togglereadonly", "closetab",
			"gotoline", "setfontsize", "insertchar",
			"gendiv", "genbase",
			"search", "searchsel", "lorem", "ordertab",
			"setcursorpos", "search", "searchsel",
			"replace", "replacesel",
			"gengetter",
			"gensetter" };
	
	public static List<String> commandHints = new ArrayList<>();
	
	public CommandTerminal(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		commandHints.clear();
		
		builder = new StringBuilder();
		
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				
			chooser = new JFileChooser();
			chooser.setDialogTitle(Texts.open + "/" + Texts.save);
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		
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
				CodeEditor.tabs.clear();
				break;
				
			case "resettabscroll":
				CodeEditor.tabScr = 0;
				break;
				
			case "reseteditorscroll":
				CodeEditor.scrX = 0;
				CodeEditor.scrY = 0;
				break;
				
			case "deselect":
				if (CodeEditor.isReadOnly) break;
				
				CodeEditor.line1 = 0; // 0, não é 1 não?
				CodeEditor.line2 = 0;
				
				CodeEditor.index1 = 0;
				CodeEditor.index2 = 0;
				
				CodeEditor.selecting = false;
				break;
				
			case "copy":
				if (!CodeEditor.selecting || CodeEditor.isReadOnly) break;
				
				List<String> lines = new ArrayList<>();
				String str = "";
				
				if (CodeEditor.line1 != CodeEditor.line2) { // se não selecionou uma linha só (selecionou várias)
					for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) {
						if (i == CodeEditor.line1 - 1) {
							lines.add(new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars().subList(CodeEditor.index1, CodeEditor.lines.get(i).getChars().size()))));
							
							continue;
						}
						
						if (i == CodeEditor.line2 - 1) {
							lines.add(new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars().subList(0, CodeEditor.index2))));
							
							continue;
						}
						
						lines.add(new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars())));
					}
					
					for (String s : lines) {
						str += s;
						
						if (s != lines.get(lines.size() - 1)) // se não for a última linha (para não adicionar quebras de linha adicionais desnecessárias)
							str += "\n";
					}
				}
				else {
					if (CodeEditor.index2 < CodeEditor.index1) { // que coisa não? (provavelmente isso nunca vai acontecer)
						runCommand("deselect");
						
						break;
					}
					
					str = new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.line1 - 1).getChars().subList(CodeEditor.index1, CodeEditor.index2)));
				}
				
				StringSelection sel = new StringSelection(str);
				Clipboard clip = Main.toolkit.getSystemClipboard();
				
				clip.setContents(sel, sel);
				
				break;
				
			case "del":
				if (!CodeEditor.selecting) break;
				if (CodeEditor.isReadOnly) break;
				
				StringBuilder s = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.line1 - 1).getChars())));
				
				if (CodeEditor.line1 != CodeEditor.line2) { // se não selecionou uma linha só (selecionou várias)
					for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) {
						if (i == CodeEditor.line1 - 1) {
							if (CodeEditor.lines.get(i).getChars().size() < CodeEditor.index1 || CodeEditor.lines.get(i).getChars().size() < CodeEditor.index2) continue;
							
							CodeEditor.lines.get(i).setChars(CodeEditor.lines.get(i).getChars().subList(0, CodeEditor.index1));
							
							continue;
						}
						
						if (i == CodeEditor.line2 - 1) {
							CodeEditor.lines.get(i).setChars(CodeEditor.lines.get(i).getChars().subList(CodeEditor.index2, CodeEditor.lines.get(i).getChars().size()));
						
							continue;
						}
						
						CodeEditor.linesToRemove.add(CodeEditor.lines.get(i)); // pra evitar concurrentmodificationexception
					}
					
					runCommand("deselect");
					CodeEditor.setCursorWithinBounds();
					CodeEditor.editing.setSaved(false);
					
					/*CodeEditor.cursorX = CodeEditor.mx;		// tomar cuidado quando o comando é chamado pelo sistema e vc ver seu cursor andando adoidado por ai viu TODO
					CodeEditor.cursorY = CodeEditor.my;			// melhor desabilitar isso
					
					CodeEditor.setCursorWithinBounds();*/
					
					break;
				}
				else {
					if (CodeEditor.index2 < CodeEditor.index1) {
						runCommand("deselect");
						
						break;
					}
					
					s.delete(CodeEditor.index1, CodeEditor.index2);
					
					Main.editor.register(s, CodeEditor.line1 - 1);
				}
				
				runCommand("deselect");
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "cut":
				if (!CodeEditor.selecting) break;
				if (CodeEditor.isReadOnly) break;
				
				runCommand("copy");	// hehe :)
				runCommand("del");
				break;
				
			case "paste":
				if (CodeEditor.isReadOnly) break;
				
				Main.editor.paste();
				
				break;
				
			case "selectline":
				if (CodeEditor.isReadOnly) break;
				
				int y = CodeEditor.cursorY - 1;
				
				CodeEditor.index1 = 0;
				CodeEditor.index2 = CodeEditor.lines.get(y).getChars().size();
				
				CodeEditor.line1 = y + 1;
				CodeEditor.line2 = y + 1;
				
				CodeEditor.selecting = true;
				break;
				
			case "selectall":
				if (CodeEditor.isReadOnly) break;
				
				CodeEditor.index1 = 0;
				CodeEditor.index2 = CodeEditor.lines.get(CodeEditor.lines.size() - 1).getChars().size();
				
				CodeEditor.line1 = 1;
				CodeEditor.line2 = CodeEditor.lines.size();
				
				CodeEditor.cursorX = 0;
				CodeEditor.cursorY = 1;
				
				CodeEditor.selecting = true;
				break;
				
			case "generateconfigfile":
				int option = chooser.showSaveDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					File f = chooser.getSelectedFile();
					
					ListableFile.generateConfigFile(f);
				}
				
				break;
				
			case "toggleexplorer":
				if (expOff)
					Main.editor.setX(280);
				else
					Main.editor.setX(0);
				
				expOff ^= true;	// uma forma de togglar boolean (^ é xor gate)
				
				break;
				
			case "loadconfigfile":
				option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					Main.conffile = chooser.getSelectedFile().getPath();
					ListableFile.readConfigFile(chooser.getSelectedFile().getPath());
					
					//JOptionPane.showMessageDialog(null, "As mudanças serão aplicadas na próxima vez que você iniciar a Boot IDE!");
					
					Main.cnfFile = chooser.getSelectedFile();
					
					Fonts.initFonts(Main.fntnr, Main.fntbl);
					Main.spritesheet = new Spritesheet(Main.sprsh);
					
					Main.hasConfigFile = true;
					
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
				if (CodeEditor.isReadOnly) break;
				
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "System.out.println();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 19;
				
				break;
				
			case "cout":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "cout << \"\" << endl;");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 9;
				
				break;
				
			case "stdcout":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "std::cout << \"\" << std::endl;");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 14;
				
				break;
				
			case "writeline":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "Console.WriteLine();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 18;
				
				break;
				
			case "syserr":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "System.err.println();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 19;
				
				break;
				
			case "clog":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "console.log();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 12;
				
				break;
				
			case "gendiv":
				if (CodeEditor.isReadOnly) break;
				
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "<div></div>");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "closebasefolder":
				CodeEditor.tabs.clear();
				Main.baseFolder = null;
				
				Explorer.files.clear();
				ListableFile.files.clear();
				
				Explorer.scope = null;
				CodeEditor.editing = null;
				
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
				
				break;
				
			case "togglecodehelpers":
				CodeEditor.codeHelpersOn ^= true; // método prático de inverter boolean, porque em Assembly mais ou menos seria assim: xor syntaxerrorson, true (lógico que o nome da variável n seria esse né :/)
				break;
				
			case "gotocursor":
				if (CodeEditor.isReadOnly) break;
				
				CodeEditor.scrY = (CodeEditor.cursorY * (CodeEditor.FONT_SIZE + 4)); // TODO arrumar isso aqui
				break;
				
			case "togglereadonly":
				runCommand("deselect");
				
				CodeEditor.isReadOnly ^= true;
				CodeEditor.editing.isReadOnly ^= true;
				
				break;
			}
		}
		
		else if (args.length == 1) {
			switch (com) {
			case "closetab":
				try {
					int args0 = Integer.parseInt(args[0]);
					
					if (CodeEditor.tabs.size() == 0 ||
						    args0 < 0 ||
						    args0 > CodeEditor.tabs.size())
							return;
						
					CodeEditor.tabs.remove(args0);
						
					break;
				} catch (NumberFormatException e) {
					break;
				}
				
			case "gotoline":
				if (CodeEditor.isReadOnly) break;
				
				try {
					CodeEditor.cursorY = Integer.parseInt(args[0]);
					
					runCommand("gotocursor");
				} catch (NumberFormatException e) {
					break;
				}
				break;
				
			case "setfontsize":
				if (args[0].equals("default"))
					CodeEditor.FONT_SIZE = 16;
				
				try {
					int a0 = Integer.parseInt(args[0]);
						
					CodeEditor.FONT_SIZE = a0;
						
					if (CodeEditor.editing != null)
						CodeEditor.lines = CodeEditor.readFile(CodeEditor.editing.getRegent().getRegent());
				} catch (NumberFormatException | IOException e) {
					CodeEditor.FONT_SIZE = 16;
				}
				break;
				
			case "insertchar":
				if (CodeEditor.isReadOnly) break;
				
				int ascii = 0;
				
				try {
					ascii = Integer.parseInt(args[0]);
				} catch (NumberFormatException e) {}
				
				char c = (char) ascii;
				
				//if (!Character.isLetterOrDigit(c)) break; // não vai verificar mais, pode colocar o que quiser aqui :\
				
				StringBuilder sb = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				sb.insert(CodeEditor.cursorX, c);
				
				Main.editor.register(sb, CodeEditor.cursorY - 1);
				
				CodeEditor.cursorX++;
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "gendiv":
				if (CodeEditor.isReadOnly) break;
				
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "<div class='" + args[0] + "'></div>");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
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
				if (CodeEditor.isReadOnly) break;
				
				String[] strs = { };
				
				String classname = ListableFile.getFileNameWithoutExtension(CodeEditor.editing.getRegent().getRegent());
				
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
					String[] cppstrs = { "#include <iostream>", "", "using namespace std;", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cppstrs;
					
					break;
					
				case "c":
					String[] cstrs = { "#include <stdio.h>", "", "int main()", "{", "    return 0;", "}"};
					
					strs = cstrs;
					
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
					if ((CodeEditor.cursorY - 1) + i >= CodeEditor.lines.size())
						CodeEditor.lines.add(new IDELine(new ArrayList<>(), new ArrayList<>()));
					
					b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get((CodeEditor.cursorY - 1) + i).getChars())));
					
					b.insert(CodeEditor.cursorX, strs[i]);
					
					Main.editor.register(b, (CodeEditor.cursorY - 1) + i);
				}
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "search":
				List<Integer> linesfound = new ArrayList<>();
				
				//args[0] = CodeEditor.arrayToStr(args); // -- n da certo pq esse comando n vai ser executado pq o numero de args é maior
				
				for (int i = 0; i < CodeEditor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				// como é automaticamente occur 0, pegamos automaticamente ela.
				
				CodeEditor.scrY = (linesfound.get(0) + 1) * (CodeEditor.FONT_SIZE);// + 4);
				CodeEditor.cursorY = (linesfound.get(0) - 1) + 2;
				
				break;
				
			case "searchsel":
				if (!CodeEditor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				// como é automaticamente occur 0, pegamos automaticamente ela.
				
				CodeEditor.scrY = (linesfound.get(0) - 1) * (CodeEditor.FONT_SIZE);// + 4);
				CodeEditor.cursorY = (linesfound.get(0) - 1) + 2;
				
				break;
				
			case "lorem":
				if (CodeEditor.isReadOnly) break;
				
				StringBuilder bl = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				try {
					String lorem = CodeEditor.generateLoremIpsum(Integer.parseInt(args[0]));
					
					bl.insert(CodeEditor.cursorX, lorem);
					
					Main.editor.register(bl, CodeEditor.cursorY - 1);
				} catch (Exception e) { break; }
					
				break;
			}
		}
		
		else if (args.length == 2) {
			switch (com) {
			case "ordertab":
				try {
					if (CodeEditor.tabs.size() < 2) break;
					
					int idx1 = Integer.parseInt(args[0]);
					int idx2 = Integer.parseInt(args[1]);
					
					Collections.swap(CodeEditor.tabs, idx1, idx2);
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					break;
				}
				
				break;
				
			case "setcursorpos":
				if (CodeEditor.isReadOnly) break;
				
				try {
					int x = Integer.parseInt(args[0]) - 1;
					int y = Integer.parseInt(args[1]);
					
					CodeEditor.cursorX = x;
					CodeEditor.cursorY = y;
					
					CodeEditor.setCursorWithinBounds();
				} catch (NumberFormatException e) {
					break;
				}
				
				break;
				
			case "search":
				List<Integer> linesfound = new ArrayList<>();
				
				for (int i = 0; i < CodeEditor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars()));
					
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
				
				CodeEditor.scrY = (linesfound.get(occurnum - 1) - 1) * (CodeEditor.FONT_SIZE);// + 4);
				CodeEditor.cursorY = (linesfound.get(occurnum - 1) - 1) + 2;
				
				break;
				
			case "searchsel":
				if (CodeEditor.isReadOnly) break;
				
				if (!CodeEditor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = CodeEditor.line1; i < CodeEditor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				occurnum = Integer.parseInt(args[1]); // base 1 viu
				
				if (occurnum > linesfound.size())
					occurnum = linesfound.size();
				
				CodeEditor.scrY = (linesfound.get(occurnum - 1) - 1) * (CodeEditor.FONT_SIZE);// + 4);
				CodeEditor.cursorY = (linesfound.get(occurnum - 1) - 1) + 2;
				
				break;
				
			case "replace":
				if (CodeEditor.isReadOnly) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = 0; i < CodeEditor.lines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				for (Integer i : linesfound) {
					String s = new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars()));
					
					s = s.replaceAll(args[0], args[1]);
					
					Main.editor.register(new StringBuilder(s), i);
				}
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "replacesel":
				if (CodeEditor.isReadOnly) break;
				
				if (!CodeEditor.selecting) break;
				
				linesfound = new ArrayList<>();
				
				for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) { // tem que ser for normal mesmo pq preciso do numero
					IDELine l = CodeEditor.lines.get(i);
					String s = new String(CodeEditor.toCharArray(l.getChars())).toLowerCase();
					
					if (s.contains(args[0].toLowerCase())) linesfound.add(i); // viu pq precisa do numero?
				}
				
				if (linesfound.size() == 0) return;
				
				for (Integer i : linesfound) {
					String s = new String(CodeEditor.toCharArray(CodeEditor.lines.get(i).getChars()));
					
					s = s.replaceAll(args[0], args[1]);
					
					Main.editor.register(new StringBuilder(s), i);
				}
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			/*case "select":
				line1 = Integer.parseInt(args[0]);
				line2 = Integer.parseInt(args[1]);
				
				index1 = 0;
				index2 = CodeEditor.lines.get(line2 - 1).getChars().size(); // é -1 porque no array é base 0, aqui é base 1
				
				selecting = true;
				
				break;*/
			}
		}
		else if (args.length == 3) {
			switch (com) {
			case "gengetter":
				if (CodeEditor.isReadOnly) break;
				
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
					
					for (int j = 0; j < Main.editor.countChar(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					CodeEditor.addNewLine(CodeEditor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strs.length; i++) {
					StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get((CodeEditor.cursorY - 1) + i).getChars())));
					
					b.append(strs[i]);
					
					Main.editor.register(b, (CodeEditor.cursorY - 1) + i);
				}
				
				//CodeEditor.cursorY += strs.length - 1;
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "gensetter":
				if (CodeEditor.isReadOnly) break;
				
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
					
					for (int j = 0; j < Main.editor.countChar(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())), ' '); j++)
						spaces.append(' ');
					
					CodeEditor.addNewLine(CodeEditor.cursorY - 1, spaces.toString());
				}
				
				for (int i = 0; i < strss.length; i++) {
					StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get((CodeEditor.cursorY - 1) + i).getChars())));
					
					b.append(strss[i]);
					
					Main.editor.register(b, (CodeEditor.cursorY - 1) + i);
				}
				
				//CodeEditor.cursorY += strss.length - 1;
				
				CodeEditor.editing.setSaved(false);
				
				break;
			}
		}
		
		new Thread() {
			public void run() {
				if (CodeEditor.editing != null && CodeEditor.editing.getRegent() != null && CodeEditor.editing.getRegent().getRegent() != null)
				for (IDELine l : CodeEditor.lines) {
					l.setFonts(
							CodeEditor.automaticColor(
									CodeEditor.toCharArray(
											l.getChars()), ListableFile.getFileExtension(CodeEditor.editing.getRegent().getRegent())));
				
				}
			}
		}.start();
		
		Main.writeFile(Main.settingsFile);
		CodeEditor.setCursorWithinBounds();
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
			
			if (cursorIndex < 0) cursorIndex = 0;
			if (cursorIndex > builder.length()) cursorIndex = builder.length();
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = Main.editor.addAccents(keyCode, c);
			
			if (KeyInput.getCharPressed() < 33 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
			
			if (builder.length() == 0 || cursorIndex == builder.length()) builder.append(c);
			else builder.insert(cursorIndex, c); // o erro era ordem de parâmetros, pois usar um char como int tbm vale
			
			cursorIndex++;
		}
		
		commandHints.clear();
		
		for (int i = 0; i < onlyCommands.length; i++) {
			String s = onlyCommands[i];
			String dgt = builder.toString().split(" ")[0];// dgt = digitado
			
			if (s.contains(dgt)) commandHints.add(commands[i]);
		}
		
		if (builder.toString().equals("")) commandHints.clear();
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(new Color(0, 0, 0, 0.3f));
		g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);
		
		g2.setStroke(new BasicStroke(3f));
		
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
			
			Fonts.drawString(cmd, x - 100, y + height + 20 + (22 * i), new IDEFont(Fonts.otherNormal, 20), g2);
		}
		
		Fonts.drawString(Texts.esc_Cancel, MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.enter_Execute, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString(Texts.ctrl_del_Clear, MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 60, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
