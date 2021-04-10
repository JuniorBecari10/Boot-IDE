package ide.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;

/**
 * Um terminal onde você coloca os comandos nele e ele executa de acordo com que você mandar. Simples, não?
 * 
 * @author Juninho
 *
 */
public class CommandTerminal extends IDEComponent {
	
	public static boolean active = false;
	private int cursorIndex = 0;

	private StringBuilder builder;
	
	private boolean showCursor;
	private Animation cursor;
	
	public static List<String> usedCommands = new ArrayList<>();
	public static int idx = 0;
	
	public CommandTerminal(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		builder = new StringBuilder();
		
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
	private void runCommand(String command) {
		usedCommands.add(command);
		
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
				/*
			case "del":
				if (!selecting) return; // terminar isso
				
				for (int i = line1 - 1; i < line2 - 1; i++) {
					if (i == line1 - 1) {
						CodeEditor.lines.get(line1 - 1).getChars().subList(index1, CodeEditor.lines.get(line1 - 1).getChars().size()).clear();
						
						return;
					}
					
					if (i == line2 - 2) {
						CodeEditor.lines.get(line1 - 1).getChars().subList(0, index2).clear();
						
						return;
					}
					
					CodeEditor.lines.remove(i);
				}
				break;*/
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
				
			/*case "select":
				line1 = Integer.parseInt(args[0]);
				line2 = Integer.parseInt(args[1]);
				
				index1 = 0;
				index2 = CodeEditor.lines.get(line2 - 1).getChars().size(); // é -1 porque no array é base 0, aqui é base 1
				
				selecting = true;
				
				break;*/
			}
		}
	}
	
	public void tick() {
		x = Main.screen.getWidth() / 2 - 250;
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
			IDEComponent.toRemove.add(this);
			
			active = false;
		}
		
		if (KeyInput.isKeyPressed()) {
			KeyInput.updateKeys();
			
			List<String> rvs = usedCommands;
			
			Collections.reverse(rvs);
			
			if (KeyInput.isControlDown() || KeyInput.isAltDown() || KeyInput.isAltGrDown()) return;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP && idx < rvs.size()) {
				builder = new StringBuilder(rvs.get(idx));
				
				idx++;
			}
			
			else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN && idx > 0) {
				builder = new StringBuilder(rvs.get(idx));
				
				idx--;
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
				runCommand(builder.toString());
				
				IDEComponent.toRemove.add(this);
				
				active = false;
				
				return;
			}
			
			if (KeyInput.getCharPressed() < 33 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
			
			if (builder.length() == 0 || cursorIndex == builder.length()) builder.append(KeyInput.getCharPressed());
			else builder.insert(KeyInput.getCharPressed(), cursorIndex);
			
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
		
		Fonts.drawString("Insira o comando:", Main.screen.getWidth() / 2 - 100, y - 25, new IDEFont(Fonts.normal, 20), g);
		
		Fonts.drawString(builder.toString(), x, y + 5, new IDEFont(Fonts.normal, 18), g);
		
		g2.setStroke(new BasicStroke(2f));
		
		if (showCursor) {
			g.setColor(Color.white);
			g.drawLine(x + (cursorIndex * 14), y + 5, x + (cursorIndex * 14), y + 5 + 18);
		}
		
		Fonts.drawString("[Esc] Cancelar", MouseInput.getMouseX() + 30, MouseInput.getMouseY(), new IDEFont(Fonts.lightGrayNormal, 20), g);
		Fonts.drawString("[Enter] Executar", MouseInput.getMouseX() + 30, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lightGrayNormal, 20), g);
	}
}
