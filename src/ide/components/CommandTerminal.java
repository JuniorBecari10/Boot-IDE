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
import ide.util.Spritesheet;

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
	
	private static String lastCommand = "";
	
	private static JFileChooser chooser;
	
	public CommandTerminal(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		builder = new StringBuilder();
		chooser = new JFileChooser();
		
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
	 * @param command
	 */
	public static void runCommand(String command) {
		lastCommand = command;
		
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
					Runtime.getRuntime().exec("explorer.exe /select," + Explorer.files.get(0).getRegent().getPath());
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
				CodeEditor.line1 = 0;
				CodeEditor.line2 = 0;
				
				CodeEditor.index1 = 0;
				CodeEditor.index2 = 0;
				
				CodeEditor.selecting = false;
				break;
				
			case "copy":
				if (!CodeEditor.selecting) break;
				
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
					if (CodeEditor.index2 < CodeEditor.index1) {
						JOptionPane.showMessageDialog(null, "O index 2 não pode ser maior que o index 1!", "Valores invertidos", JOptionPane.OK_OPTION);
						
						runCommand("deselect");
						
						break;
					}
					
					str = new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.line1 - 1).getChars().subList(CodeEditor.index1, CodeEditor.index2)));
				}
				
				StringSelection sel = new StringSelection(str);
				Clipboard clip = Main.toolkit.getSystemClipboard();
				
				clip.setContents(sel, sel);
				
				break;
				
			case "del":										// (21/04/2021 - 15:56)
				if (!CodeEditor.selecting) break;			// 30/04/2021 - 12:42
				
				StringBuilder s = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.line1 - 1).getChars())));
				
				if (CodeEditor.line1 != CodeEditor.line2) { // se não selecionou uma linha só (selecionou várias)
					for (int i = CodeEditor.line1 - 1; i < CodeEditor.line2; i++) {
						if (i == CodeEditor.line1 - 1) {
							CodeEditor.lines.get(i).setChars(CodeEditor.lines.get(i).getChars().subList(0, CodeEditor.index1));
							
							continue;
						}
						
						if (i == CodeEditor.line2 - 1) {
							CodeEditor.lines.get(i).setChars(CodeEditor.lines.get(i).getChars().subList(CodeEditor.index2, CodeEditor.lines.get(i).getChars().size()));
						
							continue;
						}
						
						/*CodeEditor.lines.get(i).setChars(new ArrayList<>());
						CodeEditor.lines.get(i).setFonts(new ArrayList<>());*/
						
						CodeEditor.linesToRemove.add(CodeEditor.lines.get(i)); // pra evitar concurrentmodificationexception
					}
					
					runCommand("deselect");
					CodeEditor.setCursorWithinBounds();
					CodeEditor.editing.setSaved(false);
					
					break;
				}
				else {
					if (CodeEditor.index2 < CodeEditor.index1) {
						JOptionPane.showMessageDialog(null, "O index 2 não pode ser maior que o index 1!", "Valores invertidos", JOptionPane.OK_OPTION);
						
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
				
				runCommand("copy");	// hehe :)
				runCommand("del");
				break;
				
			case "paste":
				Main.editor.paste();
				
				break;
				
			case "selectentireline":
				int y = CodeEditor.cursorY - 1;
				
				CodeEditor.index1 = 0;
				CodeEditor.index2 = CodeEditor.lines.get(y).getChars().size();
				
				CodeEditor.line1 = y + 1;
				CodeEditor.line2 = y + 1;
				
				CodeEditor.selecting = true;
				break;
				
			case "selectmode":
				CodeEditor.selectMode = true;
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
				
			case "readconfigfile":
				option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					Main.conffile = chooser.getSelectedFile().getPath();
					ListableFile.readConfigFile(chooser.getSelectedFile().getPath());
					
					//JOptionPane.showMessageDialog(null, "As mudanças serão aplicadas na próxima vez que você iniciar a Boot IDE!");
					
					Main.cnfFile = chooser.getSelectedFile();
					
					Fonts.initFonts(Main.fntnr, Main.fntbl);
					Main.spritesheet = new Spritesheet(Main.sprsh);
				}
				
				break;
				
			case "releaseconfigfile":		// ÚLTIMO: 13/05/2021 - 08:30
				Main.conffile = "none";
				
				Main.writeFile(Main.settingsFile);
				runCommand("revertcolors");
				
				break;
			
			case "sysout":
			case "syso":
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "System.out.println();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 19;
				
				break;
				
			case "cout":
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "cout << \"\" << endl;");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 19;
				
				break;
				
			case "syserr":
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "System.err.println();");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				CodeEditor.cursorX += 19;
				
				break;
				
			case "gendiv":
				b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "<div></div>");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "gennewhtmlbase":
				String[] strss = { "<html>", " <head>", "  <title></title>", "  ", "  <meta charset=\"UTF-8\">", "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">", "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">", " </head>", " <body>", " </body>", "</html>" };
				
				for (int i = 0; i < strss.length; i++) {
					if ((CodeEditor.cursorY - 1) + i >= CodeEditor.lines.size())
						CodeEditor.lines.add(new IDELine(new ArrayList<>(), new ArrayList<>()));
					
					b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get((CodeEditor.cursorY - 1) + i).getChars())));
					
					b.insert(CodeEditor.cursorX, strss[i]);
					
					Main.editor.register(b, (CodeEditor.cursorY - 1) + i);
				}
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "closebasefolder":
				if (Main.baseFolder == null) return;
				
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
				break;
				
			case "revertcolors":
				Colors.revertColors();
				
				Fonts.initFonts(Main.fntnr, Main.fntbl);
				Main.spritesheet = new Spritesheet(Main.sprsh);
				
				CodeEditor.FONT_SIZE = 16;
				
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
				try {
					int pos = Integer.parseInt(args[0]) * (CodeEditor.FONT_SIZE + 4);
					
					double round = Math.round(pos / ((CodeEditor.FONT_SIZE + 4) * 3)) * ((CodeEditor.FONT_SIZE + 4) * 3);
					
					CodeEditor.scrY = (int) round;
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
						
					CodeEditor.lines = CodeEditor.readFile(CodeEditor.editing.getRegent().getRegent());
				} catch (NumberFormatException | IOException e) {
					CodeEditor.FONT_SIZE = 16;
				}
				break;
				
			case "insertchar":
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
				StringBuilder b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get(CodeEditor.cursorY - 1).getChars())));
				
				b.insert(CodeEditor.cursorX, "<div class='" + args[0] + "'></div>");
				
				Main.editor.register(b, CodeEditor.cursorY - 1);
				
				CodeEditor.editing.setSaved(false);
				
				break;
				
			case "rename":
				int option = chooser.showOpenDialog(Main.screen.frame);
				
				if (option == JFileChooser.APPROVE_OPTION) {
					File toRename = chooser.getSelectedFile();
					
					String newPath = toRename.getParent() + "\\" + args[0];
					File newFile = new File(newPath);
					
					toRename.renameTo(newFile);
				}
				break;
				
			case "genbase":
				String[] strs = { };
				
				String classname = ListableFile.getFileNameWithoutExtension(CodeEditor.editing.getRegent().getRegent());
				
				switch (args[0].toLowerCase()) {
				case "html":
					String[] htmlstrs = { "<html>", " <head>", "  <title></title>", "  ", "  <meta charset=\"UTF-8\">", " </head>", " <body>", " </body>", "</html>" };
					
					strs = htmlstrs;
					
					break;
					
				case "css":
					String[] cssstrs = { "* {", "margin: 0;", "padding: 0;", "box-sizing: border-box;", "font-family: sans-serif;", "}"};
					
					strs = cssstrs;
					
					break;
					
				case "java":
					String[] javastrs = { "public class " + classname + " {", "", " public static void main(String[] args) {", "  ", " }", "}"};
					
					strs = javastrs;
					
					break;
					
				case "cs":
					String[] csstrs = { "using System;", "using System.Collections.Generic;", "using System.Linq;", "using System.Text;", "using System.Threading.Tasks;", "", "namespace " + classname, "{", " ", " public class Program", " {", " ", "  static void main(String[] args)", "  {", "   ", "  }", " }", "}"};
					
					strs = csstrs;
					
					break;
					
				case "cpp":
					String[] cppstrs = { "#include <iostream>", "", "using namespace std;", "", "int main()", "{", " ", " return 0;", "}"};
					
					strs = cppstrs;
					
					break;
					
				case "c":
					String[] cstrs = { "#include <stdio.h>", "", "int main()", "{", " ", " return 0;", "}"};
					
					strs = cstrs;
					
					break;
					
				case "ino":
					String[] inotrs = { "void setup()", "{", " ", "}", "", "void loop()", "{", " ", "}"};
					
					strs = inotrs;
					
					break;
				}
				
				for (int i = 0; i < strs.length; i++) {
					if ((CodeEditor.cursorY - 1) + i >= CodeEditor.lines.size())
						CodeEditor.lines.add(new IDELine(new ArrayList<>(), new ArrayList<>()));
					
					b = new StringBuilder(new String(CodeEditor.toCharArray(CodeEditor.lines.get((CodeEditor.cursorY - 1) + i).getChars())));
					
					b.insert(CodeEditor.cursorX, strs[i]);
					
					Main.editor.register(b, (CodeEditor.cursorY - 1) + i);
				}
				
				CodeEditor.editing.setSaved(false);
				
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
				
			/*case "select":
				line1 = Integer.parseInt(args[0]);
				line2 = Integer.parseInt(args[1]);
				
				index1 = 0;
				index2 = CodeEditor.lines.get(line2 - 1).getChars().size(); // é -1 porque no array é base 0, aqui é base 1
				
				selecting = true;
				
				break;*/
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
			
			if (KeyInput.isControlDown() || KeyInput.isAltDown() || KeyInput.isAltGrDown()) return;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP)
				builder = new StringBuilder(lastCommand);
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN)
				builder = new StringBuilder();
			
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
			else builder.insert(cursorIndex, c); // o erro era ordem de parâmetros
			
			cursorIndex++;
		}
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(new Color(0, 0, 0, 0.3f));
		g.fillRect(0, 0, Screen.WIDTH, Screen.HEIGHT);
		
		g2.setStroke(new BasicStroke(3f));
		
		g.setColor(Colors.explorer);
		g2.fillRect(x, y, width, height);
		
		g.setColor(Colors.explorerLight);
		g2.drawRect(x, y, width, height);
		
		Fonts.drawString("Insira o comando:", Main.screen.getWidth() / 2 - 100, y - 25, new IDEFont(Fonts.otherNormal, 20), g);
		
		Fonts.drawString(builder.toString(), x + 4, y + 5, new IDEFont(Fonts.otherNormal, 18), g);
		
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor) {
			g.setColor(Color.white);
			g.drawLine((x + 4) + (cursorIndex * 14), y + 5, (x + 4) + (cursorIndex * 14), y + 5 + 18);
		}
		
		Fonts.drawString("[Esc] Cancelar", MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Enter] Executar", MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
