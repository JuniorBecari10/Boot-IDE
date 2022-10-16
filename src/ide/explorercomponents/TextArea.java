package ide.explorercomponents;

import java.awt.BasicStroke;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.components.IDEComponent;
import ide.explorer.Explorer;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.main.Main;
import ide.terminal.TerminalCore;
import ide.util.Colors;
import ide.util.Texts;

public class TextArea extends IDEComponent {
	
	public static final int MARGIN = 5;
	
	public String[] lines;
	public boolean acceptInput = false;
	
	private int fontSize = 16;
	
	public int cursorX = 0;
	
	private int scrollX = 0;
	private int scrollY = 0;

	public TextArea(int x, int y, int width, int height, String[] lines) {
		super(x, y, width, height, null);
		
		this.lines = lines;
	}
	
	public void scroll() {
		if (Explorer.selected != this) return;
		
		if (MouseInput.wheelDown()) {
			if (KeyInput.isShiftDown())
				scrollX += fontSize - CodeEditor.ruleOf3(16, 4, fontSize);
			else {
				scrollY += fontSize - CodeEditor.ruleOf3(16, 4, fontSize);;
				
				if (y + 5 + ((lines.length - 1) * (fontSize + MARGIN)) - scrollY < y - fontSize + 4)
					scrollY -= fontSize - CodeEditor.ruleOf3(16, 4, fontSize);
			}
		}
		else if (MouseInput.wheelUp()) {
			if (KeyInput.isShiftDown()) {
				scrollX -= fontSize - CodeEditor.ruleOf3(16, 4, fontSize);
				
				if (scrollX < 0) scrollX = 0;
			}
			else {
				scrollY -= fontSize - CodeEditor.ruleOf3(16, 4, fontSize);;
				
				if (scrollY < 0) scrollY = 0;
			}
		}
	}
	
	public void type() {
		if (TerminalCore.selected == null) return;
		
		if (KeyInput.isKeyPressed() && Explorer.selected == this) {
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 61) {
				fontSize++;
				return;
			}
			else if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_MINUS) {
				fontSize--;
				
				if (fontSize < 8) fontSize = 8;
				
				return;
			}
		}

		if (!acceptInput || Explorer.selected != this) return;

		StringBuilder text = new StringBuilder(lines[lines.length - 1]);
		
		KeyInput.updateKeys();
		
		// Shortcuts Area
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) {
			cursorX = 2;
			
			setCursorWithinBounds();
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END) {
			cursorX = text.length();
			
			setCursorWithinBounds();
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C - Copiar (Tudo)
			CodeEditor.copyText(text.toString().substring(2));
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V - Colar
			if (cursorX >= text.length()) {
				text.append(CodeEditor.clipboard);
				cursorX += CodeEditor.clipboard.length();
			}
			else {
				text.insert(cursorX, CodeEditor.clipboard);
				cursorX += CodeEditor.clipboard.length();
			}
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X - Recortar (Tudo)
			CodeEditor.copyText(text.toString().substring(2));
			
			text = new StringBuilder();
			cursorX = 0;
			
			setCursorWithinBounds();
		}
		
		if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) { // Ctrl + Del (Deletar Tudo)
			text = new StringBuilder();
			cursorX = 0;
			
			setCursorWithinBounds();
		}
		
		// Por causa do prompt
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT && cursorX > 2) {
			cursorX--;
			
			setCursorWithinBounds();
		}
		else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT && cursorX < text.length()) {
			cursorX++;
			
			setCursorWithinBounds();
		}
		
		// Para não apagar o prompt
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE && cursorX > 2) {
			text.deleteCharAt(cursorX - 1);
			cursorX--;
			
			lines[lines.length - 1] = new String(text.toString());
			
			setCursorWithinBounds();
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SPACE && cursorX > 0) {
			if (text.length() == 0) text.append(" ");
			else text.insert(cursorX, " ");
			
			cursorX++;
			
			lines[lines.length - 1] = new String(text.toString());
			
			setCursorWithinBounds();
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
			if (cursorX > text.length() - 1) return;
			
			text.deleteCharAt(cursorX);
			
			lines[lines.length - 1] = new String(text.toString());
			
			setCursorWithinBounds();
			return;
		}
		
		if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
			TerminalCore.selected.setLines(lines);
			TerminalCore.selected.write();
			
			TerminalCore.selected.commandRunning = true;
			
			new Thread() {
				public void run() {
					String[] split = lines[lines.length - 1].split(" ");
					String[] c = new String[split.length - 1];
					
					for (int i = 0; i < c.length; i++) {
						c[i] = split[i + 1];
					}
					
					String command = String.join(" ", c);
					
					if (!runInternalCommand(command)) {
						String[] o = Main.runCommand(TerminalCore.selected.getScope(), "python3 " + Main.script.getAbsolutePath() + " " + command + " >> " + TerminalCore.selected.getLog().getAbsolutePath() + " < " + Main.script.getParentFile().getAbsolutePath() + File.separator + "input");
						
						for (String s : o) {
							Main.runCommand(new File(Main.userDir), "echo " + s + " >> " + TerminalCore.selected.getLog().getAbsolutePath());
						}
					}
					
					Main.runCommand(new File(Main.userDir), "echo " + TerminalCore.prompt + " >> " + TerminalCore.selected.getLog().getAbsolutePath());
					
					TerminalCore.selected.commandRunning = false;
					TerminalCore.selected.read();
				}
			}.start();
			
			return;
		}
		
		int keyCode = KeyInput.getKeyCodePressed();
		char c = KeyInput.getCharPressed();
		
		c = Main.editor.addAccents(keyCode, c);
		
		if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256) return;
		
		cursorX++;
		
		if (text.length() == 0) text.append(c);
		else text.insert(cursorX - 1, c);
		
		lines[lines.length - 1] = new String(text.toString());
		setCursorWithinBounds();
	}
	
	public boolean runInternalCommand(String command) {
		// true - executou | false - não executou
		
		/*
		 * Lista:
		 * 
		 * * "" (Nada) - Nada
		 * * cls, clear - Limpa a tela
		 * pwd - Mostrar Pasta Atual
		 * cd - Alterar Pasta Atual
		 * dir, ls - Listar Arquivos na Pasta Atual
		 * boot - Abrir arquivo na Boot IDE (Em construção)
		 * * exit - Fecha a tab
		 * 
		 */
		
		BufferedWriter w = null;
		
		String str = command;
		String[] split = command.split(" ");
		
		if (split.length > 0)
			str = split[0];
		
		switch (str.toLowerCase()) {
		case "":
			return true;
		
		case "cls":
		case "clear":
			try {
				w = new BufferedWriter(new FileWriter(TerminalCore.selected.getLog()));
				
				w.write("");
				w.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			scrollX = 0;
			scrollY = 0;
			return true;
			
		case "pwd":
			try {
				w = new BufferedWriter(new FileWriter(TerminalCore.selected.getLog(), true));
				w.write(TerminalCore.selected.getScope() + "\n");
				w.close();
				
			} catch (IOException e) {
				e.printStackTrace();
			}
			return true;
			
		case "exit":
			TerminalCore.selected.commandRunning = false;
			TerminalCore.selected.close();
		}
		
		return false;
	}
	
	public void setCursorWithinBounds() {
		if (TerminalCore.breakLine) return;
		
		// mover pra frente (o texto vai pra trás)
		while (x + 1 + (cursorX * (fontSize - CodeEditor.ruleOf3(16, 4, fontSize))) - scrollX > width)
			scrollX += fontSize - CodeEditor.ruleOf3(16, 4, fontSize);
		// mover pra trás (o texto vai pra frente)
		while (x + 1 + (cursorX * (fontSize - CodeEditor.ruleOf3(16, 4, fontSize))) - scrollX < x || (x + 1 + (cursorX * (fontSize - CodeEditor.ruleOf3(16, 4, fontSize))) - scrollX == x + 1 && lines[lines.length - 1].length() > 0))
			scrollX -= fontSize - CodeEditor.ruleOf3(16, 4, fontSize);

		if (lines[lines.length - 1].length() == 0 || scrollX < 0 || cursorX <= 2)
			scrollX = 0;
	}
	
	public void tick() {
		if (TerminalCore.selected == null) return;
		
		if (leftClicked())
			Explorer.selected = this;
		
		if (cursorX < 2) cursorX = 2;
		
		acceptInput = !TerminalCore.selected.commandRunning;
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorer);
		g.fillRect(x, y, width, height);
		
		if (TerminalCore.selected == null) {
			g.setColor(Colors.explorerLight);
			g2.setStroke(new BasicStroke(2f));
			g.drawRect(x - 2, y - 2, width + 4, height + 4);
			
			g.drawImage(Main.term12Px, x + (width / 2) - 24, y + 20, 48, 48, null);
			
			Fonts.drawString(Texts.clickOpenTerminal, x + 5 + (width / 2 - ((Texts.clickOpenTerminal.length() * 13) / 2)), y + 80, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
			Fonts.drawString(Texts.toOpenTerminal, x + 5 + (width / 2 - ((Texts.toOpenTerminal.length() * 13) / 2)), y + 100, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
			Fonts.drawString("Terminal!", x + 5 + (width / 2 - (("Terminal!".length() * 13) / 2)), y + 120, new IDEFont(Fonts.lightGrayNormal, 16), x + width, g);
			
			return;
		}
		
		String[] lines = Arrays.copyOf(this.lines, this.lines.length);
		
		int maxChars = Math.floorDiv(width, fontSize - CodeEditor.ruleOf3(16, 4, fontSize)) - 1;
		List<String> linesList = new ArrayList<>();
		
		if (TerminalCore.breakLine) {
			for (String s : lines) {
				if (s.length() > maxChars) {
					String s1 = s.substring(0, maxChars);
					String s2 = s.substring(maxChars);
					
					linesList.add(s1);
					
					while (s2.length() > maxChars) {
						linesList.add(s2);
						s2 = s2.substring(maxChars);
					}
					
					if (s2.length() <= maxChars)
						linesList.add(s2);
				}
				else
					linesList.add(s);
			}
		}
		else
			linesList = Arrays.asList(lines);
		
		lines = linesList.toArray(new String[0]);
		
		int i = 0;
		for (String s : lines) {
			if (y + 5 + (i * (fontSize + MARGIN)) - scrollY < y - fontSize + 4) {
				i++;
				continue;
			}
			
			Fonts.drawString(s, x + 5 - scrollX, y + 5 + (i++ * (fontSize + MARGIN)) - scrollY, new IDEFont(Fonts.otherNormal, fontSize), x, x + width, g);
		}
		
		int cursorX = this.cursorX;
		
		if (Main.editor.showCursor && Explorer.selected == this && !TerminalCore.selected.commandRunning 
				&& y + 5 + ((lines.length - 1) * (fontSize + MARGIN)) - scrollY > y - fontSize + 4 
				&& x + 5 + ((fontSize - CodeEditor.ruleOf3(16, 4, fontSize)) * cursorX) - scrollX > x 
				&& x + 5 + ((fontSize - CodeEditor.ruleOf3(16, 4, fontSize)) * cursorX) - scrollX < width
				&& y + 5 + ((lines.length - 1) * (fontSize + MARGIN)) - scrollY < height) {
			g.setColor(Colors.other);
			g.fillRect(x + 5 + ((fontSize - CodeEditor.ruleOf3(16, 4, fontSize)) * cursorX) - scrollX, y + 5 + (fontSize + MARGIN) * (lines.length - 1) - scrollY, fontSize < 13 ? 1 : 2, fontSize + MARGIN);
		}
		
		g.setColor(Colors.explorerLight);
		g2.setStroke(new BasicStroke(2f));
		g.drawRect(x - 2, y - 2, width + 4, height + 4);
	}
}
