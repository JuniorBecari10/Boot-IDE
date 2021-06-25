package ide.codeeditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

import ide.components.CommandTerminal;
import ide.components.IDEComponent;
import ide.components.RenameFile;
import ide.components.RightClickOption;
import ide.components.SetFileName;
import ide.explorer.Explorer;
import ide.explorer.FileType;
import ide.explorer.ListableFile;
import ide.fonts.Fonts;
import ide.fonts.IDEFont;
import ide.input.KeyInput;
import ide.input.MouseInput;
import ide.input.WindowInput;
import ide.main.Main;
import ide.main.Screen;
import ide.util.Animation;
import ide.util.Colors;

// Nota: para escrever em vermelho no console, ao invés de digitar System.out.println("texto"); use System.err.println("texto");

public class CodeEditor extends IDEComponent {
	
	public static volatile int FONT_SIZE = 16; // 18, 16 (Padrão: 16)
	
	public static Tab editing;
	
	private boolean showCursorData = false;
	
	public static boolean selectMode;
	public static boolean isSelectingFirst = true;
	
	public static boolean isMultilineCommenting = false;
	
	public static boolean selecting;

	public static int line1, line2;
	public static int index1, index2;
	
	public static boolean isCssPart;
	public static boolean isJSPart;
	
	public static boolean codeHintsOn = true;
	
	public static String codeType = "";
	public static String extType = "";
	
	public static boolean syntaxErrorsOn = true;
	
	public static boolean isAnotherIteration = false;
	
	public static boolean foundExt = false;
	
	private int realcx, realcy; // c = cursor
	private int drawcx = ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, drawcy = MIN_Y + cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2;
	
	private PressedAccent prAcc;
	private boolean pressedAccent = false;
	
	public static List<IDELine> lines = new ArrayList<>();
	public static List<IDELine> linesToRemove = new ArrayList<>();
	
	public static int cursorX = 0;
	public static int cursorY = 1;
	
	public static int scrX = 0;
	public static int scrY = 0;
	
	public static int tabScr = 0;
	
	public static List<Tab> tabs;
	public static List<Tab> toAdd;
	public static List<Tab> toRemove;
	
	public static BufferedImage gradient;
	
	public static String clipboard = "";
	
	public static final int MIN_Y = 35;
	
	private boolean showCursor;
	
	private static Animation cursor;
	
	private static int mx, my;
	
	public static List<Integer> linesWithErrors = new ArrayList<>();
	
	//private Thread syntaxErrors;
	
	//private static List<Integer> numopenbrackets = new ArrayList<>();
	//private static List<Integer> numclosebrackets = new ArrayList<>();

	public CodeEditor(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		
		tabs = new ArrayList<>();
		toAdd = new ArrayList<>();
		toRemove = new ArrayList<>();
		
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
		
		new Thread() {
			public void run() { // 25 pra frente com o explorer desligado, isso é uma gambiarrinha viu
				int offset = CommandTerminal.expOff ? 280 : 0;
				int lcx = !CommandTerminal.expOff ? 0 : 280;
				
				int lcmx = mx;
				int lcmy = my;
				
				while (true) {
					lcmy = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
					lcmx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE));

					while (((lcx + 40) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						lcmx++;
					
					while (((lcx + 40) + lcmx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX + offset > MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						lcmx--;
					
					while (MIN_Y + lcmy * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 < MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						lcmy++;
					
					while (MIN_Y + lcmy * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						lcmy--;
					
					if (FONT_SIZE < 13)
						lcmx--;

					lcmx = setWithinBounds(lcmx, lcmy, true);
					lcmy = setWithinBounds(lcmx, lcmy, false);
					
					//////////////
					
					my = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
					mx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE));

					while (((x + 40) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						mx++;
					
					while (((x + 40) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX > MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
						mx--;
					
					while (MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 < MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						my++;
					
					while (MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
						my--;

					if (FONT_SIZE < 13)
						mx--;
					
					mx = setWithinBounds(mx, my, true);
					my = setWithinBounds(mx, my, false);
					
					if (CommandTerminal.expOff) {
						mx = lcmx;
						my = lcmy;
					}
					
					try {
						Thread.sleep(1000/120);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();
		
		/*syntaxErrors = new Thread() {
			public void run() {
				while (true) {
					try {
						linesWithErrors = syntaxErrors(lines);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		};
		
		syntaxErrors.start();*/
		
		try {
			gradient = ImageIO.read(getClass().getResource("/gradient.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean hovered() {
		Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
        Rectangle comp = new Rectangle(x, MIN_Y, width, height);

        return mouse.intersects(comp);
	}
	
	public static <T> List<T> removeDuplicates(List<T> list) {
		return new ArrayList<>(new LinkedHashSet<>(list));
	}
	
	/*
	 * Pseudo-Código
	 * 
	 * declare lista de linhas de chaves, colchetes e parenteses
	 * declare lista de indexes de chaves, colchetes e parenteses
	 * 
	 * loop (numero de linhas) {
	 *  String s = linha atual
	 *  
	 *  if (s contém "{")
	 *   adicionar na lista de linhas de chaves abertas(linha atual)
	 *   adicionar na lista de indexes de chaves abertas(index atual)
	 *   																													___ são juntos
	 *  // fazer isso pra colchetes, e parênteses, talvez comentários multilinha, tbm em suas versões de fechar, como } ] ) * /.
	 * }
	 * 
	 * loop (linhas de chaves abertas) {
	 *  
	 * }
	 * 
	 */
//	public static List<Integer> syntaxErrors(List<IDELine> lines) {
//		if (lines.size() == 0)
//			return new ArrayList<>();
//		
//		numopenbrackets.clear();
//		numclosebrackets.clear();
//		
//		List<IDELine> getlines = (List<IDELine>) Collections.synchronizedList(CodeEditor.lines); // fazer mostrar os erros somente se o numero de uns 
//		
//		List<Integer> linescounted = new ArrayList<>();
//		List<Integer> linesfound = new ArrayList<>();
//		
//		boolean bracketsHasPair = true;
//		
//		if (getlines.size() == 0)
//			return linesfound;
//		
//		for (int i = 0; i < getlines.size(); i++) { // tem que ser for normal mesmo pq preciso do numero
//			IDELine l = getlines.get(i);
//			
//			if (l == null) break;
//			
//			Object[] arr = l.getChars().toArray(); // muahahahahhaha tirei a exception
//			char[] sa = new char[arr.length];
//			
//			for (int j = 0; j < arr.length; j++) {
//				if (arr[j] == null) continue;
//				
//				sa[j] = (char) arr[j];
//			}
//			
//			String s = new String(sa).toLowerCase(); // converte pra String a sequência de chars
//			
//			if (s.contains("{")) {
//				numopenbrackets.add(i);
//			}
//			
//			if (s.contains("}")) {
//				numclosebrackets.add(i);
//			}
//			
//			for (char c : s.toCharArray()) {
//				if (c == '{') {
//					if (!bracketsHasPair && (numopenbrackets.size() != numclosebrackets.size())) // dps arrumar isso aqui do jeito certo e desgambiarrar
//						linescounted.add(i);
//					
//					bracketsHasPair = false;
//				}
//				
//				if (c == '}') {
//					if (bracketsHasPair && (numopenbrackets.size() != numclosebrackets.size()))
//						linescounted.add(i);
//					
//					bracketsHasPair = true;
//				}
//			}
//			
//			/*
//			if (s.contains("["))
//				numopensquarebrackets.add(i);
//			
//			if (s.contains("]"))
//				numclosesquarebrackets.add(i);
//			
//			if (s.contains("("))
//				numopenparenthesis.add(i);
//			
//			if (s.contains(")"))
//				numcloseparenthesis.add(i);*/
//		}
//		
//		if (numopenbrackets.size() != numclosebrackets.size() && linesfound.size() > 0) { // 03/06/2021 - 14:39 - Quinta-Feira
//			linesfound = linescounted;
//			
//			linesfound = removeDuplicates(linesfound);
//			
//			Integer get0 = linesfound.get(0);
//			
//			linesfound.clear();
//			linesfound.add(get0); // gambiarra - remover isso na próxima atualização
//		}
//
//		
//		///////////////////////////////////////////////////////////////////////////
//		
//		/*if (numopensquarebrackets.size() > numclosesquarebrackets.size())
//			linesfound.add(numopensquarebrackets.get(numopensquarebrackets.size() - 1));
//		
//		if (numclosesquarebrackets.size() > numopensquarebrackets.size())
//			linesfound.add(numclosesquarebrackets.get(numclosesquarebrackets.size() - 1));
//		
//		///////////////////////////////////////////////////////////////////////////
//		
//		if (numopenparenthesis.size() > numcloseparenthesis.size())
//			linesfound.add(numopenparenthesis.get(numopenparenthesis.size() - 1));
//		
//		if (numcloseparenthesis.size() > numopenparenthesis.size())
//			linesfound.add(numcloseparenthesis.get(numcloseparenthesis.size() - 1));*/ // não vai detectar chevrons, pq existem os if's
//		
//		return linesfound;
//	}
	
	public static List<IDELine> readFile(File file) throws IOException {
		CodeEditor.line1 = 0;
		CodeEditor.line2 = 0;
		
		CodeEditor.index1 = 0;
		CodeEditor.index2 = 0;
		
		CodeEditor.selecting = false;
		
		List<String> l = null;
		
		Path p = file.toPath();
			
		/*
		try {														// tenta ler em todos os tipos de codificação, mas n dá
			l = Files.readAllLines(p, StandardCharsets.UTF_8);
		} catch (MalformedInputException a) {
			try {
				l = Files.readAllLines(p, StandardCharsets.UTF_16);
			} catch (MalformedInputException b) {
				try {
					l = Files.readAllLines(p, StandardCharsets.UTF_16BE);
				} catch (MalformedInputException c) {
					try {
						l = Files.readAllLines(p, StandardCharsets.UTF_16LE);
					} catch (MalformedInputException d) {
						try {
							l = Files.readAllLines(p, StandardCharsets.US_ASCII);
						} catch (MalformedInputException e) {
							l = Files.readAllLines(p, StandardCharsets.ISO_8859_1);
						}
					}
				}
			}
		}
		*/
		
		try {
			l = Files.readAllLines(p, StandardCharsets.UTF_8); // utf-8
			codeType = "UTF-8";
		}
		catch (Exception e) {
			l = Files.readAllLines(p, StandardCharsets.ISO_8859_1); // ansi
			codeType = "ANSI";
		}
			
		if (l.isEmpty()) l.add("");
		
		List<IDELine> ls = new ArrayList<>();
		
		for (String s : l) {
			List<Character> cs = new ArrayList<>();
			List<IDEFont> fs = new ArrayList<>();
			
			for (char c : s.toCharArray())
				cs.add(c);
			
			for (int i = 0; i < cs.size(); i++)
				fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
			
			IDELine gen = new IDELine(cs, fs);
			
			ls.add(gen);
		}
		
		new Thread() {
			public void run() {
				if (editing != null && editing.getRegent() != null && editing.getRegent().getRegent() != null)
				for (IDELine l : lines) {
					l.setFonts(
							automaticColor(
									toCharArray(
											l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
				}
			}
		}.start();
		
		return ls;
			
	}
	
	public static List<Integer> findWord(String textString, String word) { // Fonte: baeldung.com
        List<Integer> indexes = new ArrayList<Integer>();
        
        String lowerCaseTextString = textString;//.toLowerCase();		// não vai ter lowercase,
        String lowerCaseWord = word;//.toLowerCase();					//tem q ter diferença de letras capitais

        int index = 0;
        
        while (index != -1) {
            index = lowerCaseTextString.indexOf(lowerCaseWord, index);
            
            if (index != -1) {
                indexes.add(index);
                
                index++;
            }
        }
        return indexes;
    }
	
	public static List<IDEFont> color(int s, int e, IDEFont color, List<IDEFont> fs) {
		if (e < s) return fs;
		
		for (int i = s; i < e; i++)
			fs.set(i, color);
		
		return fs;
	}
	
	public static boolean isCharsEqual(char c1, char c2) {
		String str1 = String.valueOf(c1);
		String str2 = String.valueOf(c2);
		
		return str1.equals(str2);
	}
	
	public static List<IDEFont> automaticColor(char[] chars, String ext) {
		extType = "";
		foundExt = false;
		
		List<IDEFont> fs = new ArrayList<>();
		
		for (int i = 0; i < chars.length; i++)
			fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		
		List<Integer> indxs = new ArrayList<>();
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		if ((ext.equals(".java") || ext.equals(".c") || ext.equals(".cs") || ext.equals(".cpp") || ext.equals(".cxx") || ext.equals(".js") ||
			 ext.equals(".h") || ext.equals(".hpp") || ext.equals(".hxx") || ext.equals(".lua") || ext.equals(".rs") || ext.equals(".asm") ||
			 ext.equals(".php") || ext.equals(".kt") || ext.equals(".vue") || ext.equals(".py") || ext.equals(".pyd") || ext.equals(".rb") || ext.equals(".ino") ||
			 ext.equals(".ts") || ext.equals(".swift")  || ext.equals(".go") || ext.equals(".r") ||
			 ext.equals(".jl") || ext.equals(".pl") || ext.equals(".has") || ext.equals(".hs") || ext.equals(".fs") || ext.equals(".coffee") ||
			 ext.equals(".m") || ext.equals(".jsx") || ext.equals(".ld") || ext.equals(".pas") || ext.equals(".pp") || ext.equals(".scala") ||
			 ext.equals(".dart") || ext.equals(".md") || ext.equals(".markdown"))) { // não verificaremos mais o html aqui
			
			indxs = findWord(new String(chars), ")");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				//boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(') {
					c--;
					len++;
					
					/*if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						
						if (hasSpace)
							break;
					}*/
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "]");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				//boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ':') {
					c--;
					len++;
					
					/*if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						
						if (hasSpace)
							break;
					}*/
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			if (!ext.equals(".md") && !ext.equals(".markdown")) {
				indxs = findWord(new String(chars), ":");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
					
					boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '(') {
						c--;
						len++;
						
						if (chars[c] == ' ') {
							if (hasSpace)
								break;
							
							if (!hasSpace)
								hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
						}
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), ".");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
				}
				
				indxs = findWord(new String(chars), ";"); // ÚLTIMA EDIÇÃO: 14/06/2021 - 17:57 - Segunda-Feira
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
indxs = findWord(new String(chars), ".");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
				}
				
				indxs = findWord(new String(chars), "[");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "->");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
			}
			
			if (!(ext.equals(".html") || ext.equals(".htm") || ext.equals(".md") || ext.equals(".markdown"))) {
			
			String[] cll = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J",
					"K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z" };

			for (String s : cll) {
				indxs = findWord(new String(chars), s);
			
			int len = 0;

			String str = new String(chars);
			
			for (Integer i : indxs) {
				
				if (i - 1 > 0 &&
					(str.charAt(i - 1) == 'a' ||
					 str.charAt(i - 1) == 'b' ||
					 str.charAt(i - 1) == 'c' ||
					 str.charAt(i - 1) == 'd' ||
					 str.charAt(i - 1) == 'e' ||
					 str.charAt(i - 1) == 'f' ||
					 str.charAt(i - 1) == 'g' ||
					 str.charAt(i - 1) == 'h' ||
					 str.charAt(i - 1) == 'i' ||
					 str.charAt(i - 1) == 'j' ||
					 str.charAt(i - 1) == 'k' ||
					 str.charAt(i - 1) == 'l' ||
					 str.charAt(i - 1) == 'm' ||
					 str.charAt(i - 1) == 'n' ||
					 str.charAt(i - 1) == 'o' ||
					 str.charAt(i - 1) == 'p' ||
					 str.charAt(i - 1) == 'q' ||
					 str.charAt(i - 1) == 'r' ||
					 str.charAt(i - 1) == 's' ||
					 str.charAt(i - 1) == 't' ||
					 str.charAt(i - 1) == 'u' ||
					 str.charAt(i - 1) == 'v' ||
					 str.charAt(i - 1) == 'w' ||
					 str.charAt(i - 1) == 'x' ||
					 str.charAt(i - 1) == 'y' ||
					 str.charAt(i - 1) == 'z'))
					continue;
				
				while (i + len < chars.length && 
						!isCharsEqual(chars[i + len], ' ') &&
						!isCharsEqual(chars[i + len], '[') &&
						!isCharsEqual(chars[i + len], ']') &&
						!isCharsEqual(chars[i + len], '(') &&
						!isCharsEqual(chars[i + len], ')') &&
						!isCharsEqual(chars[i + len], ',') &&
						!isCharsEqual(chars[i + len], ';') &&
						!isCharsEqual(chars[i + len], '.') &&
						!isCharsEqual(chars[i + len], ':') &&
						!isCharsEqual(chars[i + len], '=') &&
						!isCharsEqual(chars[i + len], '\"') &&
						!isCharsEqual(chars[i + len], '\'')) {
						len++;
				}

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.objectsNormal, FONT_SIZE), fs);
			}
			
			/*indxs = findWord(new String(chars), "(");
			List<Integer> indxss = findWord(new String(chars), ")");
			
			if (indxs.size() > indxss.size())
				indxss = indxss.subList(0, indxs.size());	// igualar o tamanho das duas
			
			else if (indxss.size() > indxs.size())
				indxs = indxs.subList(0, indxss.size());
			
			for (int i = 0; i < indxs.size(); i++) {
				fs = color(indxs.get(i), indxss.get(i), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}*/ // deu certo n
		}
		}
			
			if (ext.equals(".java")) {
				indxs = findWord(new String(chars), "@");
				
				int len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && 
							!isCharsEqual(chars[i + len], ' ') &&
							!isCharsEqual(chars[i + len], '[') &&
							!isCharsEqual(chars[i + len], ']') &&
							!isCharsEqual(chars[i + len], '(') &&
							!isCharsEqual(chars[i + len], ')') &&
							!isCharsEqual(chars[i + len], ',') &&
							!isCharsEqual(chars[i + len], ';') &&
							!isCharsEqual(chars[i + len], '.') &&
							!isCharsEqual(chars[i + len], ':') &&
							!isCharsEqual(chars[i + len], '=') &&
							!isCharsEqual(chars[i + len], '\"') &&
							!isCharsEqual(chars[i + len], '\'')) {
							len++;
					}

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs); // TODO colorir + escuro
				}
			}
		
			if (!ext.equals(".md") && !ext.equals(".markdown")) {			
			indxs = findWord(new String(chars), "=");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			/*indxs = findWord(new String(chars), "<");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ">");
			
			for (Integer i : indxs) {
				int c = i;
				int len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}*/
		}
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		switch (ext.toLowerCase()) { // olha só :0 - 16/05/2021 - 10:04 - Domingo
		case ".java":
			if (!foundExt) {
				extType = "Java";
				foundExt = true;
			}
			
			String[] javaKeys = { "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
					"continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
					"for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
					"new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
					"switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
					"true", "false", "null", "@interface" };
			
			for (String s : javaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
		
		case ".ejs":
			if (!foundExt) {
				extType = "Embedded JavaScript - EJS";
				foundExt = true;
			}
		case ".cfg":
		case ".config":
			if (!foundExt) {
				extType = "Arquivo de Configurações";
				foundExt = true;
			}
		case ".xml":
			if (!foundExt) {
				extType = "Extensible Markup Language - XML";
				foundExt = true;
			}
		case ".svg":
			if (!foundExt) {
				extType = "Scalable Vector Graphics - SVG";
				foundExt = true;
			}
		case ".htm":	
		case ".html":
			if (!foundExt) {
				extType = "Hyper Text Markup Language - HTML";
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), ">"); // colorir final de tags
			
			for (Integer i : indxs) {
				fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<");

			int len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':' &&
						chars[i + len] != '>')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "</");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "="); // html
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<style");
			
			if (indxs.size() > 0)
				isCssPart = true;
			
			indxs = findWord(new String(chars), "</style");
			
			if (indxs.size() > 0)
				isCssPart = false;
			
			indxs = findWord(new String(chars), "<script");
			
			if (indxs.size() > 0)
				isJSPart = true;
			
			indxs = findWord(new String(chars), "</script");
			
			if (indxs.size() > 0)
				isJSPart = false; // TODO colorir variáveis do JS no html
			
			if (isJSPart) {
				String[] jsKeys = { "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
						"char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
						"enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function",
						"goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
						"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
						"super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
						"var", "void", "volatile", "while", "with", "yield", "undefined", "of" }; // 23/04/2021 - 09:09
				
				for (String s : jsKeys) { // colorir keywordss
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ")");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					//boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '(') {
						c--;
						len++;
						
						/*if (chars[c] == ' ') {
							if (!hasSpace)
								hasSpace = true;
							
							if (hasSpace)
								break;
						}*/
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					//boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ':') {
						c--;
						len++;
						
						/*if (chars[c] == ' ') {
							if (!hasSpace)
								hasSpace = true;
							
							if (hasSpace)
								break;
						}*/
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					indxs = findWord(new String(chars), ":");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ";");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
					}
					
					indxs = findWord(new String(chars), "[");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), "->");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					
					indxs = findWord(new String(chars), "=");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(' &&
								chars[c] != ':') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
			}
			
			if (isCssPart) {
				String[] tagsss = { "a", "abbr", "acronym", "address", "applet", "area", "article",
						"aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
						"canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del",
						"details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
						"font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header",
						"hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main",
						"map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option",
						"output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script",
						"section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table",
						"tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt",
						"u", "ul", "var", "video", "wbr", "important"
				};
				
				String[] props = { "align-content", "align-items", "all", "animation", "animation-direction",
						"animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name",
						"animation-play-state", "animation-timing-function", "backface-visibility", "background",
						"background-attachment", "background-blend-mode", "background-clip", "background-color",
						"background-image", "background-origin", "background-position", "background-repeat",
						"background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
						"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
						"border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice",
						"border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color",
						"border-right-style", "border-right-width", "border-spacing", "border-style", "border-top",
						"border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style",
						"border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing",
						"break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear",
						"clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color",
						"column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content",
						"counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter",
						"flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
						"float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values",
						"font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch",
						"font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps",
						"font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
						"font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows",
						"grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template",
						"grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation",
						"height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes",
						"left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image",
						"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left",
						"margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media",
						"min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity",
						"order", "orphans", "outline", "outline-color", "outline-offset", "outline-style",
						"outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding",
						"padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after",
						"page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events",
						"position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout",
						"text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color",
						"text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation",
						"text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform",
						"transform-origin", "transform-style", "transition", "transition-delay", "transition-duration",
						"transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align",
						"visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap",
						"writing-mode", "z-index" };
				
				String[] units = { "px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax" };
				
				for (String s : tagsss) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : props) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : units) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ".");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
							 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
							 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
							 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
							 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
							 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
							 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
							 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
							 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
							 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
							 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
							 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
							 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
							 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
							 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
							 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
							 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
							 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "#");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
							 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
							 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
							 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
							 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
							 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
							 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
							 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
							 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
							 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
							 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
							 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
							 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
							 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
							 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
							 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
							 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
							 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), ";");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != '.' &&
							chars[c] != '#' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ',' &&
							chars[c] != ';' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
			}
			
			break; // 16/05/2021 - 10:12 - Domingo
			
		case ".css":
			if (!foundExt) {
				extType = "Cascading Style Sheets - CSS";
				foundExt = true;
			}
			
			String[] tagsss = { "a", "abbr", "acronym", "address", "applet", "area", "article",
					"aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
					"canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del",
					"details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
					"font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header",
					"hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main",
					"map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option",
					"output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script",
					"section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table",
					"tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt",
					"u", "ul", "var", "video", "wbr", "important", "from", "to"
			};
			
			String[] props = { "align-content", "align-items", "all", "animation", "animation-direction",
					"animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name",
					"animation-play-state", "animation-timing-function", "backface-visibility", "background",
					"background-attachment", "background-blend-mode", "background-clip", "background-color",
					"background-image", "background-origin", "background-position", "background-repeat",
					"background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
					"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
					"border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice",
					"border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color",
					"border-right-style", "border-right-width", "border-spacing", "border-style", "border-top",
					"border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style",
					"border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing",
					"break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear",
					"clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color",
					"column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content",
					"counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter",
					"flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
					"float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values",
					"font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch",
					"font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps",
					"font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
					"font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows",
					"grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template",
					"grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation",
					"height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes",
					"left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image",
					"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left",
					"margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media",
					"min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity",
					"order", "orphans", "outline", "outline-color", "outline-offset", "outline-style",
					"outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding",
					"padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after",
					"page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events",
					"position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout",
					"text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color",
					"text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation",
					"text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform",
					"transform-origin", "transform-style", "transition", "transition-delay", "transition-duration",
					"transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align",
					"visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap",
					"writing-mode", "z-index", "screen", "and" };
			
			String[] units = { "px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax" };
			
			for (String s : tagsss) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			for (String s : props) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			for (String s : units) { // colorir tags
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ".");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
						 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
						 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
						 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
						 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
						 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
						 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
						 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
						 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
						 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
						 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
						 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
						 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
						 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
						 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
						 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
						 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
						 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "#");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
						 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
						 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
						 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
						 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
						 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
						 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
						 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
						 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
						 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
						 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
						 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
						 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
						 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
						 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
						 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
						 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
						 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ";");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != '.' &&
						chars[c] != '#' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "]");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".py":
		case ".pyd":
			if (!foundExt) {
				extType = "Python";
				foundExt = true;
			}
			
			String[] pyKeys = { "and", "as", "assert", "break", "class",
					"continue", "def", "del", "elif", "else", "except", "False",
					"finally", "for", "from", "global", "if", "import", "in", "is",
					"lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return",
					"True", "try", "while", "with", "yield", "self" };
			for (String s : pyKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".dart":
			if (!foundExt) {
				extType = "Dart";
				foundExt = true;
			}
			
			String[] dartKeys = { "abstract", "else", "import", "super", "as", "enum", "in", "switch", "assert", "export", "interface", "sync", "async", "extends", "is", "this", "await", "extension", "library", "throw", "break", "external", "mixin", "true", "case", "factory", "new", "try", "class", "final", "catch", "false", "null", "typedef", "on", "var", "const", "finally", "operator", "void", "continue", "for", "part", "while", "covariant", "Function", "rethrow", "with", "default", "get", "return", "yield", "deferred", "hide", "set", "do", "if", "show", "dynamic", "implements", "static" };
			for (String s : dartKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".ld":
			if (!foundExt) {
				extType = "LinkerScript";
				foundExt = true;
			}
			
			String[] ldKeys = { "ENTRY", "OUTPUT_FORMAT", "STARTUP", "SEARCH_DIR", "INPUT", "OUTPUT", "MEMORY", "SECTIONS", "KEEP" };
			for (String s : ldKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".pp":
		case ".pas":
			if (!foundExt) {
				extType = "Pascal";
				foundExt = true;
			}
			
			String[] pasKeys = { "and", "begin", "boolean", "break", "byte", "continue", "div", "do", "double",
					"else", "end", "false", "if", "integer", "longint", "mod", "not", "or", "repeat", "shl",
					"shortint", "shr", "single", "then", "true", "until", "while", "word", "xor", "function" };
			
			for (String s : pasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s); // descobrir pq algumas coisas não colorem
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".c":
			if (!foundExt) {
				extType = "C";
				foundExt = true;
			}
			
			String[] cKeys = { "auto", "break", "case", "char", "const",
					"continue", "default", "do", "double", "else", "enum", "extern",
					"float", "for", "goto", "if", "int", "long", "register", "return",
					"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
					"union", "unsigned", "void", "volatile", "while", "true", "false", "null", "include", "bool", "duint", "uint16_t" };
			
			for (String s : cKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}	
			
			break;
		
		case ".h":
			if (!foundExt) {
				extType = "C/C++ Header";
				foundExt = true;
			}
		case ".ino":
			if (!foundExt) {
				extType = "Arduino";
				foundExt = true;
			}
		case ".hpp":
		case ".hxx":
			if (!foundExt) {
				extType = "C++ Header";
				foundExt = true;
			}
		case ".cxx":
		case ".cpp":
			if (!foundExt) {
				extType = "C++";
				foundExt = true;
			}
			
			String[] cppKeys = { "auto", "break", "case", "char", "const",
					"continue", "default", "do", "double", "else", "enum", "extern",
					"float", "for", "goto", "if", "int", "long", "register", "return",
					"short", "signed", "sizeof", "static", "struct", "switch", "typedef",
					"union", "unsigned", "void", "volatile", "while",
					"asm", "dynamic_cast", "namespace", "reinterpret_cast", "bool",
					"explicit", "new", "static_cast", "false", "catch", "operator", "template",
					"friend", "private", "class", "this", "inline", "public", "throw", "const_cast",
					"delete", "mutable", "protected", "true", "try", "typeid", "typename", "using", "virtual",
					"wchar_t", "include", "define", "string", "ifdef", "ifndef", "error", "pragma", "endif", "override" };
			
			for (String s : cppKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".cs":
			if (!foundExt) {
				extType = "C#";
				foundExt = true;
			}
			
			String[] csKeys = { "abstract", "async", "const", "event", "extern", "new",
					"override", "partial", "readonly", "sealed", "static", "unsafe", "virtual",
					"volatile", "public", "private", "internal", "protected", "if", "else", "switch",
					"case", "do", "for", "foreach", "in", "while", "break", "continue", "default", "goto",
					"return", "yield", "throw", "try", "catch", "finally", "checked", "unchecked", "fixed",
					"lock", "params", "ref", "out", "using", "alias", "await", "sizeof", "typeof",
					"stackalloc", "is", "base", "this", "null", "false", "true", "value", "void", "bool", "byte",
					"char", "class", "decimal", "double", "enum", "float", "int", "long", "sbyte", "short", "string",
					"struct", "uint", "ulong", "ushort", "add", "var", "dynamic", "global", "set", "namespace", "object", "as", "get" };
			
			for (String s : csKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".r":
			if (!foundExt) {
				extType = "R";
				foundExt = true;
			}
			
			String[] rKeys = { "if", "else", "repeat", "while", "function", "for", "in", "next", "break",
					"TRUE", "FALSE", "NULL", "Inf", "NaN", "NA", "NA_integer", "NA_real", "NA_complex", "NA_character" };
			
			for (String s : rKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
		
		case ".license":
			if (!foundExt) {
				extType = "Arquivo de Licença";
				foundExt = true;
			}
			break;
			
		case ".ps1":
			if (!foundExt) {
				extType = "Arquivo PowerShell";
				foundExt = true;
			}
		case ".cmd":
		case ".com":
			if (!foundExt) {
				extType = "Arquivo do Prompt de Comando";
				foundExt = true;
			}
		case ".bat":
			if (!foundExt) {
				extType = "Batch";
				foundExt = true;
			}
			
			String[] batCom = { "ver", "assoc", "cd", "cls", "copy", "del", "dir", "date",
					"echo", "@echo", "exit", "md", "move", "path", "pause", "prompt", "rd",
					"rem", "start", "time", "type", "vol", "attrib", "chkdsk", "choice", "cmd",
					"comp", "convert", "driverquery", "expand", "find", "format", "help", "ipconfig",
					"label", "more", "net", "ping", "shutdown", "sort", "subst", "subst", "systeminfo",
					"taskkill", "xcopy", "tree", "fc", "title", "set", "bash", "node", "off", "goto",
					"rmdir", "icacls", "takeown", "if", "for", "else",
					"VER", "ASSOC", "CD", "CLS",
					"COPY", "DEL", "DIR", "DATE", "ECHO", "@ECHO", "EXIT", "MD", "MOVE", "PATH", "PAUSE",
					"PROMPT", "RD", "REM", "START", "TIME", "TYPE", "VOL", "ATTRIB", "CHKDSK", "CHOICE",
					"CMD", "COMP", "CONVERT", "DRIVERQUERY", "EXPAND", "FIND", "FORMAT", "HELP", "IPCONFIG",
					"LABEL", "MORE", "NET", "PING", "SHUTDOWN", "SORT", "SUBST", "SUBST", "SYSTEMINFO",
					"TASKKILL", "XCOPY", "TREE", "FC", "TITLE", "SET", "BASH", "NODE", "OFF", "GOTO",
					"RMDIR", "ICACLS", "TAKEOWN", "IF", "FOR", "ELSE" };
			
			for (String s : batCom) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".jsx":
			if (!foundExt) {
				extType = "React";
				foundExt = true;
			}
		case ".vue":	
			if (!foundExt) {
				extType = "Vue.js";
				foundExt = true;
			}
		case ".js":
			if (!foundExt) {
				extType = "JavaScript";
				foundExt = true;
			}
			
			String[] jsKeys = { "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
					"char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
					"enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function",
					"goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
					"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
					"super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
					"var", "void", "volatile", "while", "with", "yield", "undefined", "of", "async" }; // 23/04/2021 - 09:09
			
			for (String s : jsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ">"); // colorir final de tags
			
			for (Integer i : indxs) {
				fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':' &&
						chars[i + len] != '>')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "</");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "="); // js
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".lua":
			if (!foundExt) {
				extType = "Lua";
				foundExt = true;
			}
			
			String[] luaKeys = { "and", "break", "do", "else", "elseif", "end",
					"false", "for", "function", "if", "in", "local", "nil",
					"not", "or", "repeat", "return", "then", "true", "until", "while" };
			
			for (String s : luaKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
			
		case ".sql":
			if (!foundExt) {
				extType = "Structured Query Language - SQL";
				foundExt = true;
			}
			
			String[] sqlKeys = { "ADD", "ADD CONSTRAINT", "ALTER", "ALTER COLUMN", "ALTER TABLE",
					"ALL", "AND", "ANY", "AS", "ASC", "BACKUP DATABASE", "BETWEEN", "CASE", "CHECK",
					"COLUMN", "CONSTRAINT", "CREATE", "CREATE DATABASE", "CREATE INDEX", "CREATE OR REPLACE VIEW",
					"CREATE TABLE", "CREATE PROCEDURE", "CREATE UNIQUE INDEX", "CREATE VIEW", "DATABASE", "DEFAULT",
					"DELETE", "DESC", "DISTINCT", "DROP", "DROP COLUMN", "DROP CONSTRAINT", "DROP DATABASE",
					"DROP DEFAULT", "DROP INDEX", "DROP TABLE", "DROP VIEW", "EXEC", "EXISTS", "FOREIGN KEY",
					"FROM", "FULL OUTER JOIN", "GROUP BY", "HAVING", "IN", "INDEX", "INNER JOIN", "INSERT INTO",
					"INSERT INTO SELECT", "IS NULL", "IS NOT NULL", "JOIN", "LEFT JOIN", "LIKE", "LIMIT", "NOT",
					"NOT NULL", "OR", "ORDER BY", "OUTER JOIN", "PRIMARY KEY", "PROCEDURE", "RIGHT JOIN", "ROWNUM",
					"SELECT", "SELECT DISTINCT", "SELECT INTO", "SELECT TOP", "SET", "TABLE", "TOP", "TRUNCATE TABLE",
					"UNION", "UNION ALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE", "add", "add constraint", "alter",
					"alter column", "alter table", "all", "and", "any", "as", "asc", "backup database", "between", "case",
					"check", "column", "constraint", "create", "create database", "create index", "create or replace view",
					"create table", "create procedure", "create unique index", "create view", "database", "default",
					"delete", "desc", "distinct", "drop", "drop column", "drop constraint", "drop database",
					"drop default", "drop index", "drop table", "drop view", "exec", "exists", "foreign key",
					"from", "full outer join", "group by", "having", "in", "index", "inner join", "insert into",
					"insert into select", "is null", "is not null", "join", "left join", "like", "limit", "not",
					"not null", "or", "order by", "outer join", "primary key", "procedure", "right join", "rownum",
					"select", "select distinct", "select into", "select top", "set", "table", "top", "truncate table",
					"union", "union all", "unique", "update", "values", "view", "where" };
			
			for (String s : sqlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
		
		case ".s":
		case ".asm":
			if (!foundExt) {
				extType = "Assembly";
				foundExt = true;
			}
			
			String[] asmRegs = { "rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rbp", "rsp", "r8", "r9", "r10", "r11", "r12", "r13",
					"r14", "r15", "eax", "ebx", "ecx", "esi", "edi", "ebp", "esp", "r8d", "r9d", "r10d", "r11d", "r12d", "r13d",
					"r14d", "r15d", "ax", "bx", "cx", "dx", "si", "di", "bp", "sp", "r8w", "r9w", "r10w", "r11w", "r12w", "r13w",
					"r14w", "r15w", "al", "bl", "cl", "dl", "sil", "dil", "bpl", "spl", "r8b", "r9b", "r10b", "r11b", "r12b",
					"r13b", "r14b", "r15b", "ah", "bh", "ch", "dh", "edx" };
			
			for (String s : asmRegs) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			String[] asmKeys = { "global", "db", "dw", "equ", "extern", "include", "times", "org", "syscall", "aaa", "aad", "aam", "aas", "adc",
					"add", "addpd", "addps", "addressing", "addsd", "addss", "align", "and", "andnpd", "andnps", "andpd",
					"andps", "arpl", "as", "commandline", "ELFobjectfile", "macroprocessing", "syntaxUNIXversusIntel", "ascii",
					"assemblerSeeasB", "bcd", "binaryarithmeticinstructions", "bitinstructions", "bound", "bsf", "bsr",
					"bss", "bswap", "bt", "btc", "btr", "bts", "byte", "byte", "byte", "byte", "byteinstructionsC", "call",
					"cbtw", "clc", "cld", "clflush", "cli", "cltd", "cltq", "clts", "cmc", "cmova", "cmova", "cmovae",
					"cmovae", "cmovb", "cmovb", "cmovbe", "cmovbe", "cmovc", "cmovc", "cmove", "cmove", "cmovg", "cmovg",
					"cmovge", "cmovge", "cmovl", "cmovl", "cmovle", "cmovle", "cmovna", "cmovna", "cmovnae", "cmovnae",
					"cmovnb", "cmovnb", "cmovnbe", "cmovnbe", "cmovnc", "cmovnc", "cmovne", "cmovne", "cmovng", "cmovng",
					"cmovnge", "cmovnge", "cmovnl", "cmovnl", "cmovnle", "cmovnle", "cmovno", "cmovno", "cmovnp", "cmovnp",
					"cmovns", "cmovns", "cmovnz", "cmovnz", "cmovo", "cmovo", "cmovp", "cmovp", "cmovpe", "cmovpo", "cmovs",
					"cmovz", "cmp", "cmppd", "cmpps", "cmps", "cmpsb", "cmpsd", "cmpsl", "cmpss", "cmpsw", "cmpxchg",
					"cmpxchgb", "comisd", "comiss", "comm", "comment", "controltransferinstructions", "cpp", "cpuid",
					"cqtd", "cqto", "cvtdqpd", "cvtdqps", "cvtpddq", "cvtpdpi", "cvtpdps", "cvtpipd", "cvtpips", "cvtpsdq",
					"cvtpspd", "cvtpspi", "cvtsdsi", "cvtsdss", "cvtsisd", "cvtsiss", "cvtsssd", "cvtsssi", "cvttpddq",
					"cvttpdpi", "cvttpsdq", "cvttpspi", "cvttsdsi", "cvttsssi", "cwtd", "cwtlD", "daa", "das", "data",
					"datatransferinstructions", "dec", "decimalarithmeticinstructions", "directives", "div", "divpd", "divps",
					"divsd", "divss", "doubleE", "ELFobjectfile", "emms", "enter", "even", "extF", "fxm", "fabs", "fadd",
					"faddp", "fbe", "Seeas", "fbld", "fbstp", "fchs", "fclex", "fcmovb", "fcmovbe", "fcmove", "fcmovnb",
					"fcmovnbe", "fcmovne", "fcmovnu", "fcmovu", "fcom", "fcomi", "fcomip", "fcomp", "fcompp", "fcos",
					"fdecstp", "fdiv", "fdivp", "fdivr", "fdivrp", "ffree", "fiadd", "ficom", "ficomp", "fidiv", "fidivr",
					"fild", "file", "fimul", "fincstp", "finit", "fist", "fistp", "fisub", "fisubr", "flagcontrolinstructions",
					"fld", "fld", "fldcw", "fldenv", "fldle", "fldlt", "fldlg", "fldln", "fldpi", "fldz", "float",
					"floating-pointinstructions", "basicarithmetic", "comparison", "control", "datatransfer", "loadconstants",
					"logarithmic", "Seetranscendental", "transcendental", "trigonometric", "Seetranscendental", "fmul",
					"fmulp", "fnclex", "fninit", "fnop", "fnsave", "fnstcw", "fnstenv", "fnstsw", "fpatan", "fprem", "fprem",
					"fptan", "frndint", "frstor", "fsave", "fscale", "fsin", "fsincos", "fsqrt", "fst", "fstcw", "fstenv",
					"fstp", "fstsw", "fsub", "fsubp", "fsubr", "fsubrp", "ftst", "fucom", "fucomi", "fucomip", "fucomp",
					"fucompp", "fwait", "fxam", "fxch", "fxrstor", "fxsave", "fxtract", "fylx", "fylxp", "G", "gas", "globl",
					"group", "H", "hidden", "hlt", "ident", "identifier", "idiv", "imul", "in", "inc", "ins", "insb", "insl",
					"instruction", "format", "suffixes", "instructions", "binaryarithmetic", "bit", "byte", "controltransfer",
					"datatransfer", "decimalarithmetic", "flagcontrol", "floating-point-", "logical", "miscellaneous", "MMX-",
					"operatingsystemsupport-", "Opteron", "rotate", "segmentregister", "shift", "SIMDstatemanagement", "SSE-",
					"SSE-", "string", "insw", "int", "into", "invd", "invlpg", "iretJ", "ja", "jae", "jb", "jbe", "jc", "jcxz",
					"je", "jecxz", "jg", "jge", "jl", "jle", "jmp", "jnae", "jnb", "jnbe", "jnc", "jne", "jng", "jnge", "jnl",
					"jnle", "jno", "jnp", "jns", "jnz", "jo", "jp", "jpe", "jpo", "js", "jzK", "keywordL", "label", "numeric",
					"symbolic", "lahf", "lar", "lcall", "lcomm", "ldmxcsr", "lds", "lea", "leave", "les", "lfence", "lfs",
					"lgdt", "lgs", "lidt", "lldt", "lmsw", "local", "lock", "lods", "lodsb", "lodsl", "lodsw",
					"logicalinstructions", "long", "loop", "loope", "loopne", "loopnz", "loopz", "lret", "lsl", "lss", "ltr",
					"m", "maskmovdqu", "maskmovq", "maxpd", "maxps", "maxsd", "maxss", "mfence", "minpd", "minps", "minsd",
					"minss", "miscellaneousinstructions", "MMXinstructions", "comparison", "conversion", "datatransfer",
					"logical", "packedarithmetic", "rotate", "shift", "statemanagement", "mov", "movabs", "movabsA", "movapd",
					"movaps", "movd", "movdqq", "movdqa", "movdqu", "movhlps", "movhpd", "movhps", "movlhps", "movlpd",
					"movlps", "movmskpd", "movmskps", "movntdq", "movnti", "movntpd", "movntps", "movntq", "movq", "movqdq",
					"movs", "movsb", "movsd", "movsl", "movss", "movsw", "movupd", "movups", "movzb", "movzw", "mul", "mulpd",
					"mulps", "mulsd", "mulss", "N", "neg", "nop", "not", "numbers", "floatingpoint", "integers", "binary",
					"decimal", "hexadecimal", "octal", "operands", "immediate", "indirect", "memory", "addressing",
					"ordering", "register", "operatingsystemsupportinstructions", "Opteroninstructions", "or", "orpd",
					"orps", "out", "outs", "outsb", "outsl", "outswP", "packssdw", "packsswb", "packuswb", "paddb", "paddd",
					"paddq", "paddsb", "paddsw", "paddusb", "paddusw", "paddw", "pand", "pandn", "pause", "pavgb", "pavgw",
					"pcmpeqb", "pcmpeqd", "pcmpeqw", "pcmpgtb", "pcmpgtd", "pcmpgtw", "pextrw", "pinsrw", "pmaddwd", "pmaxsw",
					"pmaxub", "pminsw", "pminub", "pmovmskb", "pmulhuw", "pmulhw", "pmullw", "pmuludq", "pop", "popa", "popal",
					"popaw", "popf", "popfw", "popsection", "por", "prefetchnta", "prefetcht", "prefetcht", "prefetcht",
					"previous", "psadbw", "pshufd", "pshufhw", "pshuflw", "pshufw", "pslld", "pslldq", "psllq", "psllw", "psrad",
					"psraw", "psrld", "psrldq", "psrlq", "psrlw", "psubb", "psubd", "psubq", "psubsb", "psubsw", "psubusb",
					"psubusw", "psubw", "punpckhbw", "punpckhdq", "punpckhqdq", "punpckhwd", "punpcklbw", "punpckldq",
					"punpcklqdq", "punpcklwd", "push", "pusha", "pushal", "pushaw", "pushf", "pushfw", "pushsection",
					"pxor", "quad", "rcl", "rcpps", "rcpss", "rcr", "rdmsr", "rdpmc", "rdtsc", "rel", "rep", "repnz", "repz",
					"ret", "rol", "ror", "rotateinstructions", "rsm", "rsqrtps", "rsqrtss", "sahf", "sal", "sar", "sbb",
					"scas", "scasb", "scasl", "scasw", "section", "segmentregisterinstructions", "set", "seta", "setae",
					"setb", "setbe", "setc", "sete", "setg", "setge", "setl", "setle", "setna", "setnae", "setnb", "setnbe",
					"setnc", "setne", "setng", "setnge", "setnl", "setnle", "setno", "setnp", "setns", "setnz", "seto", "setp",
					"setpe", "setpo", "sets", "setz", "sfence", "sgdt", "shiftinstructions", "shl", "shld", "shr", "shrd",
					"shufpd", "shufps", "sidt", "SIMDstatemanagementinstructions", "skip", "sldt", "sleb", "smovl", "smsw",
					"sqrtpd", "sqrtps", "sqrtsd", "sqrtss", "SSEinstructions", "compare", "conversion", "datatransfer",
					"integer", "logical", "miscellaneous", "MXCSRstatemanagement", "packedarithmetic", "shuffle", "unpack",
					"SSEinstructions", "compare", "conversion", "datamovement", "logical", "miscellaneous", "packedarithmetic",
					"packedsingle-precisionfloating-point", "shuffle", "SIMDintegerinstructions", "unpack", "statement",
					"empty", "stc", "std", "sti", "stmxcsr", "stos", "stosb", "stosl", "stosw", "str", "string", "string",
					"stringinstructions", "sub", "subpd", "subps", "subsd", "subss", "symbolic", "sysenter", "sysexit", "tbss",
					"tcomm", "tdata", "test", "text", "ucomisd", "ucomiss", "ud", "uleb", "unpckhpd", "unpckhps", "unpcklpd",
					"unpcklps", "value", "verr", "verw", "wait", "wbinvd", "weak", "whitespace", "wrmsr", "xadd", "xchg",
					"xchgA", "xlat", "xlatb", "xor", "xorpd", "xorps", "zero" };
			
			for (String s : asmKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			/*indxs = findWord(new String(chars), ",");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}*/

			indxs = findWord(new String(chars), "db");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] != ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "equ");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] != ' ') {
						if (!hasSpace)
							hasSpace = true;
						else
							break;
					}
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ".");
			
			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length && chars[i + len] != ' ')
					len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".jl":
			extType = "Julia";
			foundExt = true;
			
			String[] jlKeys = { "baremodule", "begin", "break", "catch", "const", "continue", "do", "else",
					"elseif", "end", "export", "false", "finally", "for", "function", "global", "if", "import",
					"let", "local", "macro", "module", "quote", "return", "struct", "true", "try", "using", "while" };
			
			for (String s : jlKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".pl":
			if (!foundExt) {
				extType = "Perl";
				foundExt = true;
			}
			
			String[] plKeys = { "-A", "END", "length", "setpgrp", "-B", "endgrent", "link", "setpriority", "-b",
					"endhostnet", "listen", "setprotoent", "-C", "endnetent", "local", "setpwent", "-c", "endprotoent",
					"localtime", "setservent", "-d", "endpwent", "log", "setsockopt", "-e", "endservent", "lstat",
					"shift", "-f", "eof", "map", "shmctl", "-g", "eval", "mkdir", "shmget", "-k", "exec", "msgctl",
					"shmread", "-l", "exists", "msgget", "shmwrite", "-M", "exit", "msgrcv", "shutdown", "-O", "fcntl",
					"msgsnd", "sin", "-o", "fileno", "my", "sleep", "-p", "flock", "next", "socket", "-r", "fork", "not",
					"socketpair", "-R", "format", "oct", "sort", "-S", "formline", "open", "splice", "-s", "getc", "opendir",
					"split", "-T", "getgrent", "ord", "sprintf", "-t", "getgrgid", "our", "sqrt", "-u", "getgrnam", "pack",
					"srand", "-w", "gethostbyaddr", "pipe", "stat", "-W", "gethostbyname", "pop", "state", "-X", "gethostent",
					"pos", "study", "-x", "getlogin", "print", "substr", "-z", "getnetbyaddr", "printf", "symlink", "abs",
					"getnetbyname", "prototype", "syscall", "accept", "getnetent", "push", "sysopen", "alarm", "getpeername",
					"quotemeta", "sysread", "atan2", "getpgrp", "rand", "sysseek", "AUTOLOAD", "getppid", "read", "system",
					"BEGIN", "getpriority", "readdir", "syswrite", "bind", "getprotobyname", "readline", "tell", "binmode",
					"getprotobynumber", "readlink", "telldir", "bless", "getprotoent", "readpipe", "tie", "break", "getpwent",
					"recv", "tied", "caller", "getpwnam", "redo", "time", "chdir", "getpwuid", "ref", "times", "CHECK",
					"getservbyname", "rename", "truncate", "chmod", "getservbyport", "rename", "umask", "chown", "getsockopt",
					"reverse", "undef", "chr", "glob", "rewinddir", "UNITCHECK", "chroot", "gmtime", "rindex", "unlink",
					"close", "goto", "rmdir", "unpack", "closedir", "grep", "say", "unshift", "connect", "hex", "scalar",
					"untie", "cos", "index", "seek", "use", "crypt", "INIT", "seekdir", "utime", "dbmclose", "int",
					"select", "values", "dbmopen", "ioctl", "semctl", "vec", "defined", "join", "semget", "wait", "delete",
					"keys", "semop", "waitpid", "DESTROY", "kill", "send", "wantarray", "die", "last", "setgrent", "warn",
					"dump", "lc", "sethostent", "write", "each", "lcfirst", "setnetent", "__DATA__", "else", "lock", "qw",
					"__END__", "elsif", "lt", "qx", "__FILE__", "eq", "m", "s", "__LINE__", "exp", "ne", "sub", "__PACKAGE__",
					"for", "no", "tr", "and", "foreach", "or", "unless", "cmp", "ge", "package", "until", "continue", "gt",
					"q", "while", "CORE", "if", "qq", "xor", "do", "le", "qr", "y" };
			
			for (String s : plKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".hs":
		case ".has":
			if (!foundExt) {
				extType = "Haskell";
				foundExt = true;
			}
			
			String[] hasKeys = { "as", "case", "of", "class", "data", "family", "data", "instance",
					"default", "deriving", "do", "forall", "foreign", "hiding", "if", "then", "else",
					"import", "infix", "infixl", "infixr", "let", "in", "mdo", "module", "newtype", "proc",
					"qualified", "rec", "type", "where" };
			
			for (String s : hasKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".fs":
			if (!foundExt) {
				extType = "F#";
				foundExt = true;
			}
			
			String[] fsKeys = { "abstract", "and", "as", "assert", "base", "begin", "class", "default",
					"delegate", "do", "done", "downcast", "downto", "elif", "else", "end", "exception",
					"extern", "false", "finally", "fixed", "for", "fun", "function", "global", "if", "in",
					"inherit", "inline", "interface", "internal", "lazy", "let", "match", "member", "module",
					"mutable", "namespace", "new", "not", "null", "of", "open", "or", "override", "private",
					"public", "rec", "return", "select", "static", "struct", "then", "to", "true", "try", "type",
					"upcast", "use", "val", "void", "when", "while", "with", "yield", "const", "asr", "land", "lor",
					"lsl", "lsr", "lxor", "mod", "sig", "atomic", "break", "checked", "component", "const", "constraint",
					"constructor", "continue", "eager", "event", "external", "functor", "include", "method", "mixin",
					"object", "parallel", "process", "protected", "pure", "sealed", "tailcall", "trait", "virtual", "volatile" };
			
			for (String s : fsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".coffee":
			if (!foundExt) {
				extType = "CoffeeScript";
				foundExt = true;
			}
			
			String[] cfKeys = { "for", "while", "loop", "by", "in", "of", "break", "continue", "if",
					"then", "else", "unless", "switch", "when", "default", "return", "do", "is", "isnt",
					"and", "or", "not", "true", "yes", "on", "false", "no", "off", "throw", "try", "catch",
					"finally", "new", "delete", "class", "extends", "super", "typeof", "instanceof", "this",
					"arguments", "await", "defer", "yield", "null", "undefined", "Infinity", "NaN", "export",
					"import", "package", "let", "case", "debugger", "function", "var", "with", "private",
					"protected", "public", "native", "static", "const", "implements", "interface", "void", "enum" };
			
			for (String s : cfKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".markdown":
		case ".md":
			if (!foundExt) {
				extType = "Markdown";
				foundExt = true;
			}
			
			//for (int i = 0; i < chars.length; i++)
			//	fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
			
			indxs = findWord(new String(chars), "#");
			
			for (Integer i : indxs)
				fs = color(i, fs.size(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			
			break;
		
		case ".log":
			if (!foundExt) {
				extType = "Arquivo de Log";
				foundExt = true;
			}
		case ".txt":
			if (!foundExt) {
				extType = "Arquivo de Texto";
				foundExt = true;
			}
			break;
			
		case ".ini":
			if (!foundExt) {
				extType = "Arquivo de Parâmetros de Configurações";
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), "]");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				//boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '[' &&
						chars[c] != ':') {
					c--;
					len++;
					
					/*if (chars[c] == ' ') {
						if (!hasSpace)
							hasSpace = true;
						
						if (hasSpace)
							break;
					}*/
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "=");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				boolean hasSpace = false;
					
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != '(' &&
						chars[c] != ':') {
					c--;
					len++;
					
					if (chars[c] == ' ') {
						if (hasSpace)
							break;
						
						if (!hasSpace)
							hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
					}
				}
					
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".swift":
			if (!foundExt) {
				extType = "Swift";
				foundExt = true;
			}
			
			String[] swKeys = { "associatedtype", "class", "deinit", "enum", "extension", "fileprivate",
					"func", "import" , "init", "inout", "internal", "let", "open", "operator", "private",
					"protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var",
					"break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for",
					"guard", "if", "in", "repeat", "return", "switch", "where", "while", "as", "Any", "catch",
					"false", "is", "nil", "super", "self", "self", "throw", "throws", "true", "try", "_",
					"#available", "#colorLiteral", "#column", "#else", "#elseif", "#endif", "#error", "#file",
					"#fileID", "#fileLiteral", "#filePath", "#function", "#if", "#imageLiteral", "#line",
					"#selector", "#sourceLocation", "#warning", "associativity", "convenience", "dynamic",
					"didset", "final", "get", "infix", "indirect", "lazy", "left", "mutating", "none", "nonmutating",
					"optional", "override", "postfix", "precendence", "prefix", "Protocol", "required", "right",
					"set", "Type", "unowned", "weak", "willSet" };
			
			for (String s : swKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".rs":
			if (!foundExt) {
				extType = "Rust";
				foundExt = true;
			}
			
			String[] rsKeys = { "as", "break", "const", "continue", "crate", "else", "enum", "extern", "false",
					"fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref",
					"return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe", "use",
					"where", "while", "async", "await", "dyn", "abstract", "become", "box", "do", "final", "macro",
					"override", "priv", "typeof", "unsized", "virtual", "yield", "try", "union", "'static", "dyn" };
			
			for (String s : rsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".sh":
			if (!foundExt) {
				extType = "Bash";
				foundExt = true;
			}
			
			String[] shKeys = { "pwd", "cd", "ls", "cat", "cp", "mv", "mkdir", "rmdir", "rm", "touch", "locate", "find",
					"grep", "sudo", "df", "du", "head", "tail", "diff", "tar", "chmod", "chown", "jobs", "kill", "ping",
					"wget", "uname", "top", "history", "man", "echo", "zip", "unzip", "hostname", "useradd", "userdel",
					"clear" };
			
			for (String s : shKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			break;
			
		case ".php":
			if (!foundExt) {
				extType = "Hypertext Preprocessor - PHP";
				foundExt = true;
			}
			
			String[] phpKeys = { "abstract", "and", "as", "break", "callable", "case", "catch", "class", "clone",
					"const", "continue", "declare", "default", "do", "echo", "else", "elseif", "enddeclare", "endfor",
					"endforeach", "endif", "endswitch", "endwhile", "extends", "final", "finally", "fn", "for", "foreach",
					"function", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof",
					"interface", "match", "namespace", "new", "or", "print", "private", "protected", "public", "require",
					"require_once", "return", "static", "switch", "throw", "trait", "try", "use", "var", "while", "yield",
					"yield from", "__CLASS__", "__DIR__", "__FILE__", "__FUNCTION__", "__LINE__", "__METHOD__", "__NAMESPACE__",
					"__TRAIT__" };
			
			for (String s : phpKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), ">"); // colorir final de tags
			
			for (Integer i : indxs) {
				fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':' &&
						chars[i + len] != '>')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "</");

			len = 0;

			for (Integer i : indxs) {
				while (i + len < chars.length &&
						chars[i + len] != ' ' &&
						chars[i + len] != '[' &&
						chars[i + len] != ']' &&
						chars[i + len] != ',' &&
						chars[i + len] != ';' &&
						chars[i + len] != '.' &&
						chars[i + len] != ':')
						len++;

				if (i + len < chars.length)
					fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "="); // html
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.' &&
						chars[c] != ':') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			indxs = findWord(new String(chars), "<style");
			
			if (indxs.size() > 0)
				isCssPart = true;
			
			indxs = findWord(new String(chars), "</style");
			
			if (indxs.size() > 0)
				isCssPart = false;
			
			indxs = findWord(new String(chars), "<script");
			
			if (indxs.size() > 0)
				isJSPart = true;
			
			indxs = findWord(new String(chars), "</script");
			
			if (indxs.size() > 0)
				isJSPart = false; // TODO colorir variáveis do JS no html
			
			if (isJSPart) {
				String[] jsKeyss = { "abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch",
						"char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else",
						"enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function",
						"goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long",
						"native", "new", "null", "package", "private", "protected", "public", "return", "short", "static",
						"super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof",
						"var", "void", "volatile", "while", "with", "yield", "undefined", "of" }; // 23/04/2021 - 09:09
				
				for (String s : jsKeyss) { // colorir keywordss
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ")");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					//boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '(') {
						c--;
						len++;
						
						/*if (chars[c] == ' ') {
							if (!hasSpace)
								hasSpace = true;
							
							if (hasSpace)
								break;
						}*/
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					//boolean hasSpace = false;
						
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ':') {
						c--;
						len++;
						
						/*if (chars[c] == ' ') {
							if (!hasSpace)
								hasSpace = true;
							
							if (hasSpace)
								break;
						}*/
					}
						
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					indxs = findWord(new String(chars), ":");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ";");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), ".");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != '[' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs); // mais tarde arrumar os outros bugs, ou em outra update
					}
					
					indxs = findWord(new String(chars), "[");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
					
					indxs = findWord(new String(chars), "->");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != ' ' &&
								chars[c] != ']' &&
								chars[c] != ',' &&
								chars[c] != '.' &&
								chars[c] != ':') {
							c--;
							len++;
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
					
					indxs = findWord(new String(chars), "=");
					
					for (Integer i : indxs) {
						int c = i;
						len = 0;
						
						boolean hasSpace = false;
							
						while (c < chars.length && 
								c + len < chars.length &&
								c > 0 &&
								chars[c] != '(' &&
								chars[c] != ':') {
							c--;
							len++;
							
							if (chars[c] == ' ') {
								if (hasSpace)
									break;
								
								if (!hasSpace)
									hasSpace = true; // tem q ser invertido pq muda e dps detecta e da break
							}
						}
							
						fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
					}
			}
			
			if (isCssPart) {
				String[] tagsssss = { "a", "abbr", "acronym", "address", "applet", "area", "article",
						"aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button",
						"canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del",
						"details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure",
						"font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header",
						"hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main",
						"map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option",
						"output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script",
						"section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table",
						"tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt",
						"u", "ul", "var", "video", "wbr", "important" };
				
				String[] propss = { "align-content", "align-items", "all", "animation", "animation-direction",
						"animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name",
						"animation-play-state", "animation-timing-function", "backface-visibility", "background",
						"background-attachment", "background-blend-mode", "background-clip", "background-color",
						"background-image", "background-origin", "background-position", "background-repeat",
						"background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius",
						"border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse",
						"border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice",
						"border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color",
						"border-right-style", "border-right-width", "border-spacing", "border-style", "border-top",
						"border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style",
						"border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing",
						"break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear",
						"clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color",
						"column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content",
						"counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter",
						"flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap",
						"float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values",
						"font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch",
						"font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps",
						"font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position",
						"font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows",
						"grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template",
						"grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation",
						"height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes",
						"left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image",
						"list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left",
						"margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media",
						"min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity",
						"order", "orphans", "outline", "outline-color", "outline-offset", "outline-style",
						"outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding",
						"padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after",
						"page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events",
						"position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout",
						"text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color",
						"text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation",
						"text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform",
						"transform-origin", "transform-style", "transition", "transition-delay", "transition-duration",
						"transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align",
						"visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap",
						"writing-mode", "z-index" };
				
				String[] unitss = { "px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax" };
				
				for (String s : tagsssss) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : propss) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				for (String s : unitss) { // colorir tags
					indxs = findWord(new String(chars), s);
					
					for (Integer i : indxs)
						fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
				}
				
				indxs = findWord(new String(chars), ".");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
							 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
							 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
							 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
							 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
							 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
							 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
							 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
							 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
							 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
							 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
							 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
							 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
							 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
							 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
							 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
							 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
							 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "#");
				
				len = 0;

				for (Integer i : indxs) {
					while (i + len < chars.length && chars[i + len] != ' ')/* (chars[i + len] == 'a' || chars[i + len] == 'b' ||
							 chars[i + len] == 'c' || chars[i + len] == 'd' || chars[i + len] == 'e' ||
							 chars[i + len] == 'f' || chars[i + len] == 'g' || chars[i + len] == 'h' ||
							 chars[i + len] == 'i' || chars[i + len] == 'j' || chars[i + len] == 'k' ||
							 chars[i + len] == 'l' || chars[i + len] == 'm' || chars[i + len] == 'n' ||
							 chars[i + len] == 'o' || chars[i + len] == 'p' || chars[i + len] == 'q' ||
							 chars[i + len] == 'r' || chars[i + len] == 's' || chars[i + len] == 't' ||
							 chars[i + len] == 'u' || chars[i + len] == 'v' || chars[i + len] == 'w' ||
							 chars[i + len] == 'x' || chars[i + len] == 'y' || chars[i + len] == 'z' ||
							 chars[i + len] == 'A' || chars[i + len] == 'B' || chars[i + len] == 'C' ||
							 chars[i + len] == 'D' || chars[i + len] == 'E' || chars[i + len] == 'F' ||
							 chars[i + len] == 'G' || chars[i + len] == 'H' || chars[i + len] == 'I' ||
							 chars[i + len] == 'J' || chars[i + len] == 'K' || chars[i + len] == 'L' ||
							 chars[i + len] == 'M' || chars[i + len] == 'N' || chars[i + len] == 'O' ||
							 chars[i + len] == 'P' || chars[i + len] == 'Q' || chars[i + len] == 'R' ||
							 chars[i + len] == 'S' || chars[i + len] == 'T' || chars[i + len] == 'U' ||
							 chars[i + len] == 'V' || chars[i + len] == 'W' || chars[i + len] == 'X' ||
							 chars[i + len] == 'Y' || chars[i + len] == 'Z'))*/
						len++;

					if (i + len < chars.length)
						fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), ";");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != '.' &&
							chars[c] != '#' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
				}
				
				indxs = findWord(new String(chars), "]");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ',' &&
							chars[c] != ';' &&
							chars[c] != '.' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}
			}
			
			break;
			
		case ".ts":
			if (!foundExt) {
				extType = "TypeScript";
				foundExt = true;
			}
			
			String[] tsKeys = { "break", "as", "any", "switch", "case", "if", "throw",
					"else", "var", "number", "string", "get", "module", "type", "instanceof",
					"typeof", "public", "private", "enum", "export", "finally", "for", "while",
					"void", "null", "super", "this", "new", "in", "return", "true", "false",
					"extends", "static", "let", "package", "implements", "interface", "function",
					"new", "try", "yield", "const", "continue", "do", "catch" };
			
			for (String s : tsKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			break;
		
		case ".jsonc":
			if (!foundExt) {
				extType = "JavaScript Object Notation with Comments - JSONC";
				foundExt = true;
			}
		case ".json":
			if (!foundExt) {
				extType = "JavaScript Object Notation - JSON";
				foundExt = true;
			}
			
			String[] jsonKeys = { "true", "false" };
			
			for (String s : jsonKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".kt":
			if (!foundExt) {
				extType = "Kotlin";
				foundExt = true;
			}
			
			String[] ktKeys = { "as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun",
					"if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return", "super",
					"this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while", "by",
					"catch", "constructor", "delegate", "dynamic", "field", "file", "finally", "get", "import",
					"init", "param", "property", "receiver", "set", "setparam", "value", "class", "where", "actual",
					"abstract", "annotation", "companion", "const", "crossinline", "data", "enum", "expect",
					"external", "final", "infix", "inline", "inner", "internal", "lateinit", "noinline", "open",
					"operator", "out", "override", "private", "protected", "public", "reified", "sealed", "suspend",
					"tailrec", "vararg", "field", "it" };
			
			for (String s : ktKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			break;
			
		case ".rb":
			if (!foundExt) {
				extType = "Ruby";
				foundExt = true;
			}
			
			String[] rbKeys = { "_ENCODING_", "_LINE_", "_FILE_", "BEGIN", "END", "alias", "and", "begin",
					"break", "case", "class", "def", "defined?", "do", "else", "elsif", "end", "ensure", "false",
					"for", "if", "in", "module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return",
					"self", "super", "then", "true", "undef", "unless", "until", "when", "while", "yield" };
			
			for (String s : rbKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".scala":
			if (!foundExt) {
				extType = "Scala";
				foundExt = true;
			}
			
			String[] scaKeys = { "abstract", "finally", "object", "trait", "catch", "forSome", "package",
					"try", "class", "if", "private", "type", "def", "implicit", "protected", "val", "else",
					"lazy", "sealed", "while", "false", "new", "this", "yield", "final", "null", "throw" };
			
			for (String s : scaKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".go":
			if (!foundExt) {
				extType = "Go";
				foundExt = true;
			}
			
			String[] goKeys = { "break", "default", "func", "interface", "select", "case",
					"defer", "go", "map", "struct", "chan", "else", "goto", "package", "switch",
					"const", "fallthrough", "if", "range", "type", "continue", "for", "import", "return", "var" };
			
			for (String s : goKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".m":
			if (!foundExt) {
				extType = "Objective-C";
				foundExt = true;
			}
			
			String[] objKeys = { "auto", "break", "case", "char", "const", "continue", "default", "do", "double",
					"else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register",
					"restrict", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef",
					"union", "unsigned", "void", "volatile", "while", "_Bool", "_Complex", "_Imaginary", "BOOL",
					"Class", "bycopy", "byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL", "oneway", "out",
					"Protocol", "SEL", "self", "super", "YES", "@interface", "@end", "@implementation", "@protocol",
					"@class", "@public", "@protected", "@private", "@property", "@try", "@throw", "@catch", "@finally",
					"@synthesize", "@dynamic", "@selector", "atomic", "nonatomic", "retain" };
			
			for (String s : objKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			break;
			
		case ".pdf":
			if (!foundExt) {
				extType = "Portable Document Format - PDF";
				foundExt = true;
			}
			break;
			
		case ".jar":
			if (!foundExt) {
				extType = "Arquivo Jar";
				foundExt = true;
			}
			break;
			
		case ".exe":
			extType = "Executável do Windows - EXE";
			foundExt = true;
			break;
			
		case ".urna":
			if (!foundExt) {
				extType = "Urna Salva do Criador de Urnas";
				foundExt = true;
			}
			break;
			
		case ".class":
			if (!foundExt) {
				extType = "Arquivo Compilado do Java";
				foundExt = true;
			}
			break;
			
		case ".save":
			if (!foundExt) {
				extType = "Jogo Salvo do World's Hardest Game Maker 2";
				foundExt = true;
			}
			break;
			
		case ".conf":
			if (!foundExt) {
				extType = "Arquivo de Configurações da Boot IDE";
				foundExt = true;
			}
			
			String[] confKeys = { "Arquivo de Configurações da Boot IDE", "Colors", "Files", "Settings", "default" };
			
			for (String s : confKeys) { // colorir keywordss
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs); // tem q dar offset
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
			
			break;
		
		case ".7z":
		case ".rar":
		case ".zip":
			if (!foundExt) {
				extType = "Arquivo Compactado";
				foundExt = true;
			}
			break;
			
		case ".bin":
			if (!foundExt) {
				extType = "Arquivo Binário";
				foundExt = true;
			}
			break;
			
		case ".mk":
		case ".make":
			if (!foundExt) {
				extType = "Makefile";
				foundExt = true;
			}
			
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';' &&
						chars[c] != '.') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			break;

		case ".ttf":
		case ".otf":
		case ".woff":
		case ".woff2":
			if (!foundExt) {
				extType = "Arquivo de Fonte";
				foundExt = true;
			}
			break;
			
		case ".dll":
			if (!foundExt) {
				extType = "Dynamic Link Library";
				foundExt = true;
			}
			
			break;
			
		case ".lock":
			if (!foundExt) {
				extType = "Lock";
				foundExt = true;
			}
			break;
			
		case ".gitignore":
			if (!foundExt) {
				extType = "Git Ignore";
				foundExt = true;
			}
			
			break;
			
		case ".docx":
			if (!foundExt) {
				extType = "Documento do Microsoft Word";
				foundExt = true;
			}
			
			break;
			
		case ".xlsx":
			if (!foundExt) {
				extType = "Planilha do Microsoft Excel";
				foundExt = true;
			}
			
			break;
			
		case ".pptx":
			if (!foundExt) {
				extType = "Apresentação do Microsoft PowerPoint";
				foundExt = true;
			}
			
			break;
			
		case ".one":
			if (!foundExt) {
				extType = "Arquivo do Microsoft OneNote";
				foundExt = true;
			}
			
			break;
			
		case ".psd":
			if (!foundExt) {
				extType = "Arquivo do Photoshop";
				foundExt = true;
			}
			
			break;
			
		case ".aed":
			if (!foundExt) {
				extType = "Arquivo do After Effects";
				foundExt = true;
			}
			
			break;
			
		case ".ai":
			if (!foundExt) {
				extType = "Arquivo do Illustrator";
				foundExt = true;
			}
			
			break;
			
		case ".indd":
			if (!foundExt) {
				extType = "Arquivo do InDesign";
				foundExt = true;
			}
			
			break;
			
		case ".dockerfile":
			if (!foundExt) {
				extType = "Dockerfile";
				foundExt = true;
			}
			
			String[] dkKeys = { "FROM", "RUN", "VOLUME", "WORKDIR", "ADD", "CMD", "ENTRYPOINT", "ENV", "EXPOSE", "MAINTAINER", "USER",
					"from", "run", "volume", "workdir", "add", "cmd", "entrypoint", "env", "expose", "maintainer", "user" };
			
			for (String s : dkKeys) { // colorir keywords
				indxs = findWord(new String(chars), s);
				
				for (Integer i : indxs)
					fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
			}
			
			break;
		}
		
		String[] nums = { "1", "2", "3", "4", "5", "6", "7", "8", "9", "0",
				  "1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a", "0a", // hex
				  "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b", "0b",
				  "1c", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "0c",
				  "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "0d",
				  "1e", "2e", "3e", "4e", "5e", "6e", "7e", "8e", "9e", "0e",
				  "1f", "2f", "3f", "4f", "5f", "6f", "7f", "8f", "9f", "0f",
				  "1l", "2l", "3l", "4l", "5l", "6l", "7l", "8l", "9l", "0l",
				  "1A", "2A", "3A", "4A", "5A", "6A", "7A", "8A", "9A", "0A", // HEX
				  "1B", "2B", "3B", "4B", "5B", "6B", "7B", "8B", "9B", "0B",
				  "1C", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "0C",
				  "1D", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "0D",
				  "1E", "2E", "3E", "4E", "5E", "6E", "7E", "8E", "9E", "0E",
				  "1F", "2F", "3F", "4F", "5F", "6F", "7F", "8F", "9F", "0F",
				  "1L", "2L", "3L", "4L", "5L", "6L", "7L", "8L", "9L", "0L",
				  "0x", "0X" }; // long
		
		for (String s : nums) { // colorir números
			indxs = findWord(new String(chars), s);

			for (Integer i : indxs)
				fs = color(i, i + s.length(), new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
		}
		
		indxs = findWord(new String(chars), "0x");
		
		int len = 0;

		for (Integer i : indxs) {
			while (i + len < chars.length &&
					chars[i + len] != ' ' &&
					chars[i + len] != '[' &&
					chars[i + len] != ']' &&
					chars[i + len] != '(' &&
					chars[i + len] != ')' &&
					chars[i + len] != ',' &&
					chars[i + len] != ';' &&
					chars[i + len] != '.' &&
					chars[i + len] != ':')
					len++;

			if (i + len < chars.length)
				fs = color(i, i + len, new IDEFont(Fonts.numbersNormal, FONT_SIZE), fs);
		}
		
		if ((ext.equals(".java") || ext.equals(".c") || ext.equals(".cs") || ext.equals(".css") || ext.equals(".cpp") || ext.equals(".cxx") || ext.equals(".js") ||
				 ext.equals(".h") || ext.equals(".hpp") || ext.equals(".hxx") || ext.equals(".lua") || ext.equals(".rs") || ext.equals(".asm") ||
				 ext.equals(".php") || ext.equals(".kt") || ext.equals(".vue") || ext.equals(".py") || ext.equals(".pyd") || ext.equals(".rb") || ext.equals(".ino") ||
				 ext.equals(".ts") || ext.equals(".swift") || ext.equals(".html") || ext.equals(".htm") || ext.equals(".go") || ext.equals(".r") ||
				 ext.equals(".jl") || ext.equals(".pl") || ext.equals(".has") || ext.equals(".hs") || ext.equals(".fs") || ext.equals(".coffee") ||
				 ext.equals(".m") || ext.equals(".jsx") || ext.equals(".ld") || ext.equals(".pas") || ext.equals(".pp") || ext.equals(".scala") || ext.equals(".dart") || ext.equals(".md") || ext.equals(".markdown") ||
				 ext.equals(".json") || ext.equals(".jsonc") || ext.equals(".bat") || ext.equals(".cmd") || ext.equals(".sh") || ext.equals(".conf") || ext.equals(".html") || ext.equals(".htm") || ext.equals(".xml") ||
				 ext.equals(".ini") || ext.equals(".ejs"))) {
			
			if (!(ext.equals(".html") || ext.equals(".htm") || ext.equals(".xml") || ext.equals(".ejs"))) {
				indxs = findWord(new String(chars), "(");
				
				for (Integer i : indxs) {
					int c = i;
					len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ' &&
							chars[c] != '[' &&
							chars[c] != ']' &&
							chars[c] != ',' &&
							chars[c] != ';' &&
							chars[c] != '.' &&
							chars[c] != '-' &&
							chars[c] != '+' &&
							chars[c] != '*' &&
							chars[c] != '/' &&
							chars[c] != '<' &&
							chars[c] != '>' &&
							chars[c] != '?' &&
							chars[c] != ':') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.methodsNormal, FONT_SIZE), fs);
				}
				
				/*indxs = findWord(new String(chars), "=");
				
				for (Integer i : indxs) {
					int c = i;
					int len = 0;
					
					while (c < chars.length && 
							c + len < chars.length &&
							c > 0 &&
							chars[c] != ' ') {
						c--;
						len++;
					}
					
					fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
				}*/ // n deu :(
			}
	
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		// gens = genéricos
		String[] syms = { " ", "(", ")", "[", "]", "{", "}", ",", ".", "<", ">", ";", ":", "?", "/", "|", "+", "-", "*", "=", "&", "%", "$", "#", "!", "@" };
			
		for (String s : syms) {
			indxs = findWord(new String(chars), s);
	
			for (Integer i : indxs)
				fs = color(i, i + 1, new IDEFont(Fonts.symbolsNormal, FONT_SIZE), fs);
		}
		
		//////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), new Character((char) 34).toString()); // colorir strings
		
		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), fs);

		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////

		indxs = findWord(new String(chars), new Character((char) 39).toString()); // colorir chars

		for (int i = 0; i < indxs.size() - 1; i += 2)
			fs = color(indxs.get(i), indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), fs);
		
		/////////////////////////////////////////////////////////////////////////////////////////////////////////////////
		
		for (IDEFont i : fs) {
			i.setSize(FONT_SIZE);
		}
		
		// extras que precisam ser coloridos depois disso
		
		if (ext.equals(".json") || ext.equals(".jsonc")) {
			indxs = findWord(new String(chars), ":");
			
			for (Integer i : indxs) {
				int c = i;
				len = 0;
				
				while (c < chars.length && 
						c + len < chars.length &&
						c > 0 &&
						chars[c] != ' ' &&
						chars[c] != '[' &&
						chars[c] != ']' &&
						chars[c] != ',' &&
						chars[c] != ';') {
					c--;
					len++;
				}
				
				fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs);
			}
		}
		}
		
		if ((!foundExt && editing != null) || (extType.equals("") || extType == null)) { // TODO o culpado do gitignore estar assim é esse ARRUMAR DEPOIS 
			for (FileType f : ListableFile.types) {
				if (f.getExtension().equalsIgnoreCase(editing.getRegent().getRegent().getName())) { // tenta ver se tem algum especial
					String st = capitalizeFirstLetter(f.getExtension());
					extType = st;
					
					indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
					
					if (fs.size() == 0) break;
					
					if (indxs.size() != 0)
						fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
					
					switch (st.toLowerCase()) {
					case "dockerfile":
						String[] dkKeys = { "FROM", "RUN", "VOLUME", "WORKDIR", "ADD", "CMD", "ENTRYPOINT", "ENV", "EXPOSE", "MAINTAINER", "USER",
								"from", "run", "volume", "workdir", "add", "cmd", "entrypoint", "env", "expose", "maintainer", "user" };
						
						for (String s : dkKeys) { // colorir keywords
							indxs = findWord(new String(chars), s);
							
							for (Integer i : indxs)
								fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
						}
						
						break;
						
					case "makefile":
						indxs = findWord(new String(chars), ":");
						
						for (Integer i : indxs) {
							int c = i;
							len = 0;
							
							while (c < chars.length && 
									c + len < chars.length &&
									c > 0 &&
									chars[c] != ' ' &&
									chars[c] != '[' &&
									chars[c] != ']' &&
									chars[c] != ',' &&
									chars[c] != ';' &&
									chars[c] != '.') {
								c--;
								len++;
							}
							
							fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), fs);
						}
						
						break;
						
					case "license":
						if (!foundExt) {
							extType = "Arquivo de Licença";
							foundExt = true;
						}
						break;
						
					case "gitignore":
						if (!foundExt) {
							extType = "Git Ignore";
							foundExt = true;
						}
						break;
					}
				}
			}
			
			if (extType.equals("") || extType == null) {
				String extn = "";
				
				try {
					extn = ListableFile.getFileExtension(editing.getRegent().getRegent()).substring(1); // tenta retornar o nome da extensão
				} catch (Exception e) {
					extn = "Sem Extensão"; // se não der mesmo assim, coloque "Sem Extensão".
				}
				
				extType = extn;
				foundExt = true;
			}
		}
		
		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cs":
		case ".js":
		case ".vue":
		case ".jsx":
		case ".h":
		case ".hpp":
		case ".hxx":
		case ".swift":
		case ".rs":
		case ".kt":
		case ".ino":
		case ".ts":
		case ".go":
		case ".fs":
		case ".m":
		case ".pp":
		case ".pas":
		case ".scala":
			indxs = findWord(new String(chars), "//"); // colorir comentários de uma linha
			
//			int lineindex = 0;
//			int count = 0;
//			
//			boolean canColor = true;
//			
//			for (IDELine l : lines) {
//				if (toCharArray(l.getChars()) == chars) {
//					lineindex = count;
//					break;
//				}
//					
//				count++;
//			}
//			
//			for (Integer i : indxs) {
//				if (i > 1 && lines.get(lineindex).getFonts().get(i - 1).getFont().equals(Fonts.stringsNormal)) // TODO não colorir comentários em strings
//					canColor = false;
//			}
//			
//			if (canColor) {
				if (fs.size() == 0) break;
				
				if (indxs.size() != 0)
					fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".ps1":
		case ".com":
		case ".bat":
		case ".cmd":
			indxs = findWord(new String(chars), "REM"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".s":
		case ".asm":
			indxs = findWord(new String(chars), ";"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".lua":
		case ".sql":
		case ".has":
		case ".hs":
			indxs = findWord(new String(chars), "--"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".py":
		case ".pyd":
		case ".php":
		case ".rb":
		case ".r":
		case ".jl":
		case ".pl":
		case ".coffee":
		case ".make":
		case ".sh":
		case ".gitignore":
		case ".dockerfile":
		case ".config":
		case ".cfg":
		case ".ini":
			indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
		
		case ".markdown":
		case ".md":
			indxs = findWord(new String(chars), "[//]: #"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			indxs = findWord(new String(chars), "[]: #"); // colorir comentários de uma linha
			
			if (fs.size() == 0 || indxs.size() == 0) break;
			
			fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
		}
		
		/*for (FileType f : ListableFile.types) {
			String ex = f.getExtension();
			
			if (ex.toLowerCase().equals("makefile") || ex.toLowerCase().equals("dockerfile") || ex.toLowerCase().equals("gitignore")) {
				indxs = findWord(new String(chars), "#"); // colorir comentários de uma linha
				
				if (fs.size() == 0 || indxs.size() == 0) break;
				
				fs = color(indxs.get(0), fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			}
		}*/
		
		switch (ext.toLowerCase()) {
		case ".java":
		case ".c":
		case ".cpp":
		case ".cxx":
		case ".cs":
		case ".js":
		case ".h":
		case ".hpp":
		case ".hxx":
		case ".sql":
		case ".swift":
		case ".rs":
		case ".php":
		case ".kt":
		case ".vue":
		case ".jsx":
		case ".ino":
		case ".ts":
		case ".go":
		case ".m":
		case ".scala":
		case ".css":
		case ".json":
		case ".jsonc":
			indxs = findWord(new String(chars), "/*");						// colorir comentários multi-linha - caracteres diferentes
			List<Integer> finals = findWord(new String(chars), "*/");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".lua": // Lua
			indxs = findWord(new String(chars), "--[[");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "]]--");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".rb": // Ruby
			indxs = findWord(new String(chars), "=begin");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=end");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".jl": // Julia
			indxs = findWord(new String(chars), "#=");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "=#");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".has": // Haskell
		case ".hs":
			indxs = findWord(new String(chars), "{-");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-}");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".fs": // F#
			indxs = findWord(new String(chars), "(*");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "*)");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".pas":
		case ".pp":
			indxs = findWord(new String(chars), "(*");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "*)");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
			
		case ".py":
		case ".pyd":
			indxs = findWord(new String(chars), "\'\'\'");						// colorir comentários multi-linha - caracteres iguais
			
			if (indxs.size() > 0 && !isMultilineCommenting) { // provavelmente esse é o abrimento
				fs = color(indxs.get(0), indxs.size() > 1 ? indxs.get(1) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
				
				isAnotherIteration = false;
			}
			
			if (indxs.size() > 0 && isMultilineCommenting && isAnotherIteration) { // provavelmente esse é o fechamento
				fs = color(0, indxs.get(0) + 2, new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			isAnotherIteration = true;
			
			/*if (indxs.size() > 1) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, indxs.get(1), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}*/
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			break;
			
		case ".ejs":
		case ".xml":
		case ".htm":
		case ".html":
		case ".svg":
			indxs = findWord(new String(chars), "<!--");						// colorir comentários multi-linha - caracteres diferentes
			finals = findWord(new String(chars), "-->");
			
			if (indxs.size() > 0) {
				fs = color(indxs.get(0), finals.size() > 0 ? finals.get(0) : fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = true;
			}
			
			if (finals.size() > 0) {
				fs = color(indxs.size() > 0 ? indxs.get(indxs.size() - 1) : 0, finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
				isMultilineCommenting = false;
			}
			
			if (isMultilineCommenting)
				fs = color(0, fs.size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), fs);
			
			//if (indxs.size() == 0  && finals.size() == 0)
				//isMultilineCommenting = false;
			break;
		}
		
		if (linesWithErrors != null && syntaxErrorsOn)
			for (Integer i : linesWithErrors) {
				if (toCharArray(lines.get(i).getChars()) == chars) return fs;
				
				fs = color(0, fs.size(), new IDEFont(Fonts.errorNormal, FONT_SIZE), fs);
			}
		
		return fs;
	}
	
	public static String capitalizeFirstLetter(String s) {
		char f = Character.toUpperCase(s.charAt(0));
		String c = f + s.substring(1);
		
		return c;
	}
	
	/**
	 * Conta quantos caracteres {@code c} tem na String {@code str}. 
	 * Adicional: conta desde o começo até não ter mais daquele char
	 * 
	 * @param str - A String que vai contar
	 * @param c - O caractere que vai ser contado
	 * @return O número de vezes que o caractere {@code c} aparece na String {@code str}.
	 */
	public int countChar(String str, char c) { // Fonte: StackOverflow, de novo :/
	    int count = 0;

	    for (int i = 0; i < str.length(); i++) {
	    	if (str.charAt(i) != c) break;
	    	
	    	if (str.charAt(i) == c)
	            count++;
	    }

	    return count;
	}
	
	public static char[] toCharArray(List<Character> list) {
		if (list.size() == 0)
			return new char[0];
		
		StringBuilder sb = new StringBuilder();
		
	    for (Character ch : list) {
	        sb.append(ch);
	    }
		
        String str = sb.toString();
        
        return str.toCharArray();
	}
	
	public List<Character> toCharList(char[] array) {
		List<Character> cl = new ArrayList<>();
		
		for (char c : array)
			cl.add(c);
		
		return cl;
	}
	
	public IDEFont[] toArray(List<IDEFont> list) {
		IDEFont[] a = new IDEFont[list.size()];
		
		for (int i = 0; i < list.size(); i++)
			a[i] = list.get(i);
		
		return a;
	}
	
	
	/**
	 * Esse método faz a função que muitos editores de código fazem: adicionar caracteres inteligentes. Se você digita '{', ele
	 * completa com '}', o mesmo vale para '[', '<', '"' (aspas duplas) e ' ' ' (aspas simples).
	 * 
	 * @param pre - O {@code StringBuilder} anterior, a base.
	 * @return O {@code StringBuilder} anterior com as modificações.
	 */
	private StringBuilder addCodeHints(StringBuilder pre) {
		switch (KeyInput.getCharPressed()) {
		case '{':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('}');
			else pre.insert(cursorX + 1, '}');
			break;
			
		case '(':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append(')');
			else pre.insert(cursorX + 1, ')');
			break;
			
		case '[':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append(']');
			else pre.insert(cursorX + 1, ']');
			break;
			
		case '<':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('>');
			else pre.insert(cursorX + 1, '>');
			break;
			
		case '"':
			if (pre.length() == 0 || cursorX == pre.length()) pre.append('"'); // arrumar uns bug ae
			else pre.insert(cursorX + 1, '"');
			break;
			
		case 39: // -> ( ' ) Aspas Simples
			if (pre.length() == 0 || cursorX == pre.length()) pre.append((char) 39);
			else pre.insert(cursorX + 1, (char) 39);
			break;
		}
		
		return pre;
	}
	
	public static void setCursorWithinBounds() { // o cursorY deve ser feito primeiro
		if (editing == null) return;
		
		try {
			if (cursorY < 1) cursorY = 1;
			if (cursorY + 1 > lines.size()) cursorY = lines.size();
			
			if (cursorX < 0) cursorX = 0;
			if (cursorX > lines.get(cursorY - 1).getChars().size()) cursorX = lines.get(cursorY - 1).getChars().size();
		} catch (Exception e) {}
	}
	
	private int setWithinBounds(int x, int y, boolean isX) {
		try {
			if (isX) {
				if (y < 1) y = 1;
				if (y + 1 > lines.size()) y = lines.size();
				
				if (x < 0) x = 0;
				if (x > lines.get(y - 1).getChars().size()) x = lines.get(y - 1).getChars().size();
				
				return x;
			}
			else {
				if (y < 1) y = 1;
				if (y + 1 > lines.size()) y = lines.size();
				
				return y;
			}
		} catch (Exception e) {
			return x;
		}
	}
	
	private StringBuilder write(StringBuilder cY, char c) {
		if (c < 32 || c > 1000) {
			cursorX--; // esse é o método gambiarrento, mas depois pode arrumar (ou não kkkkk)
			
			return cY;
		}
		
		if (cY.length() == 0) cY.append(c);
		else if (cursorX <= cY.length()) cY.insert(cursorX, c); // use <= pq se digitar no último n digita pq n bate
															   // com a condição mas mesmo assim aumenta o cursorX e quando dá
															  // o backspace excede o tamanho da linha e dá no que dá né
		return cY;
	}
	
	public void register(StringBuilder cY, int y) { // cY = cursorY
		String gs = cY.toString(); // gen string
		char[] ca = gs.toCharArray(); // char array
		
		List<Character> lc = toCharList(ca); // list char	(Esses comentários são para especificar os nomes das variáveis)
		
		lines.get(y).getChars().clear();
		lines.get(y).getFonts().clear();
		
		for (Character c : lc) {
			lines.get(y).getChars().add(c);
			lines.get(y).getFonts().add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
		}
	}
	
	public char addAccents(int keyCode, char ch) {
		if (!pressedAccent) {
			if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown()) { // ^ Circunflexo
				prAcc = PressedAccent.CIRCUMFLEX;
				pressedAccent = true;
				
				return ch;
			}
			else if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown()) { // ` Crase
				prAcc = PressedAccent.BACK_QUOTE;
				pressedAccent = true;
				
				return ch;
			}
			else if (keyCode == KeyEvent.VK_DEAD_ACUTE) { // ´ Acento Agudo
				prAcc = PressedAccent.ACUTE;
				pressedAccent = true;
				
				return ch;
			}
			else if (keyCode == KeyEvent.VK_DEAD_TILDE) { // ~ Til
				prAcc = PressedAccent.TILDE;
				pressedAccent = true;
				
				return ch;
			}
		}
		
		if (pressedAccent && !(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL)) {
			pressedAccent = false;
			
			switch (prAcc) {
			case ACUTE:
				if (ch == 'A') return 'Á';
				 if (ch == 'E') return 'É';
				 if (ch == 'I') return 'Í';
				 if (ch == 'O') return 'Ó';
				 if (ch == 'U') return 'Ú';
				 if (ch == 'Y') return 'Ý';
				
				 if (ch == 'a') return 'á';
				 if (ch == 'e') return 'é';
				 if (ch == 'i') return 'í';
				 if (ch == 'o') return 'ó';
				 if (ch == 'u') return 'ú';
				 if (ch == 'y') return 'ý';
				 
				 if (keyCode == KeyEvent.VK_DEAD_ACUTE) return '´';
				break;
			case BACK_QUOTE:
				if (ch == 'A') return 'À';
				 if (ch == 'E') return 'È';
				 if (ch == 'I') return 'Ì';
				 if (ch == 'O') return 'Ò';
				 if (ch == 'U') return 'Ù';
				
				 if (ch == 'a') return 'à';
				 if (ch == 'e') return 'è';
				 if (ch == 'i') return 'ì';
				 if (ch == 'o') return 'ò';
				 if (ch == 'u') return 'ù';
				 
				 if (keyCode == KeyEvent.VK_DEAD_ACUTE && KeyInput.isShiftDown()) return '`';
				break;
			case CIRCUMFLEX:
				if (ch == 'A') return 'Â';
				 if (ch == 'E') return 'Ê';
				 if (ch == 'I') return 'Î';
				 if (ch == 'O') return 'Ô';
				 if (ch == 'U') return 'Û';
				
				 if (ch == 'a') return 'â';
				 if (ch == 'e') return 'ê';
				 if (ch == 'i') return 'î';
				 if (ch == 'o') return 'ô';
				 if (ch == 'u') return 'û';
				 
				 if (keyCode == KeyEvent.VK_DEAD_TILDE && KeyInput.isShiftDown()) return '^';
				break;
			case TILDE:
				if (ch == 'a') return 'ã';
				else if (ch == 'A') return 'Ã';
				
				if (ch == 'O') return 'Õ';
				else if (ch == 'N') return 'Ñ';
				
				if (ch == 'o') return 'õ';
				else if (ch == 'n') return 'ñ';
				
				if (keyCode == KeyEvent.VK_DEAD_TILDE) return '~';
				break;
			}
		}
		
		return ch;
	}
	
	public static List<Character> delete(int start, int end, List<Character> list) {
		List<Character> result = new ArrayList<>();
		
		result.addAll(list.subList(0, start));
		result.addAll(list.subList(end, list.size()));
		
		return result;
	}
	
	public void paste() {		// terminar o paste com mais de uma linha
		if (editing == null) return;
		
		String[] sp = clipboard.split("\n");
		
		int index = 0;
		
		if (sp.length == 1) { // se é só uma linha
			for (String s : sp) {
				StringBuilder b = new StringBuilder(new String(toCharArray(lines.get((cursorY - 1)).getChars())));
				
				b.insert(cursorX, s);
				
				register(b, (cursorY - 1) + index);
				
				cursorX += s.length();
			}
		}
		else {
			for (String s : sp) {
				if (s != sp[0])
					lines.add((cursorY - 1) + index, new IDELine(new ArrayList<>(), new ArrayList<>()));
				
				StringBuilder b = new StringBuilder(new String(toCharArray(lines.get((cursorY - 1) + index).getChars())));
				
				int x = cursorX > lines.get((cursorY - 1) + index).getChars().size() ? lines.get((cursorY - 1) + index).getChars().size() : cursorX; // não pode exceder o index
				
				b.insert(x, s);
				
				register(b, (cursorY - 1) + index);
				
				if (s == sp[sp.length - 1]) {
					cursorX += s.length();
					cursorY += sp.length - 1;
				}
				
				index++;
			}
		}
		
		editing.setSaved(false);
	}
	
	public static void execTerminal() {
		CommandTerminal term = new CommandTerminal(Screen.WIDTH / 2 - 250, 25, 500, 30);
		
		if (CommandTerminal.active)
			return;
		
		CommandTerminal.active = true;
		IDEComponent.toAdd.add(term);
	}
	
	public void execute(String arg) {
		switch (arg) {
		case "cmd":
			try {
				boolean isWindows = System.getProperty("os.name").toLowerCase().startsWith("windows");
				
				ProcessBuilder pb = null;
				
				if (isWindows)
					pb = new ProcessBuilder("cmd", "/c", "start");
				else
					pb = new ProcessBuilder("sh", "-c", "start");
				
				File dir = Explorer.scope != null ? Explorer.scope.getRegent() : Main.baseFolder; // eu tava fazendo o equivalente a isso: null.regent != null
				
				pb.directory(dir);
				
				pb.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "paste":
			paste();
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
						Main.desktop.open(editing.getRegent().getRegent());
					} catch (Exception e) {
						JOptionPane.showMessageDialog(null, "O sistema não encontrou um programa padrão para abrir esse arquivo.", "Não encontrou nada!", JOptionPane.OK_OPTION);
					}
				}
			}.start();
			break;
			
		case "save":
			if (editing == null) return;
			
			editing.save();
			break;
			
		case "clr":
			if (editing == null) return;
			
			lines.get(cursorY - 1).getChars().clear();
			lines.get(cursorY - 1).getFonts().clear();
			
			editing.setSaved(false);
			
			setCursorWithinBounds();
			break;
			
		case "sysexp":
			try {
				if (Main.baseFolder == null) return;
				
				String path = null;
				
				try {
					path = editing == null ? Explorer.files.get(0).getRegent().getPath() : editing.getRegent().getRegent().getPath();
				} catch (Exception e) { // caiu aqui mt provavelmente é pq não tem itens no explorer
					if (editing != null)
						path = editing.getRegent().getRegent().getPath();
					else
						return;
				}
				
				
				Main.desktop.open(new File(path).getParentFile());
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;
			
		case "term":
			execTerminal();
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
	
	public static void verifyDuplicateTabs() { // continuar segundo o TODO
		try {
			if (tabs == null || tabs.size() == 0) return;
			
			for (int i = 0; i < tabs.size(); i++)
				for (int j = 0; j < tabs.size(); j++) {
					Tab tabi = tabs.get(i);
					Tab tabj = tabs.get(j);
					
					if (tabi.getRegent().getRegent().getAbsolutePath().equals(tabj.getRegent().getRegent().getAbsolutePath()) && tabi != tabj) {
						tabi.close();
						
						return;
					}
				}
		} catch (Exception e) {}
	}
	
	public static <T> List<T> removeAllDuplicates(List<T> list) {
		Set<T> linkedSet = new LinkedHashSet<>();
		
		linkedSet.addAll(list);
		list.clear();
		list.addAll(linkedSet);
		
		return list;
	}
	
	public static int ruleOf3(int a, int b, int c) {
		return (b * c) / a;
	}
	
	public static String arrayToStr(String[] array) {
		StringBuilder result = new StringBuilder();
		
		for (String s : array)
			result.append(s);
		
		return result.toString();
	}
	
	public void tick() {
		if (SetFileName.added || CommandTerminal.active || RenameFile.added) return;
		
		if (tabs == null) tabs = new ArrayList<>();
		verifyDuplicateTabs();
		
		if (!selecting) {
			index1 = cursorX;
			line1 = cursorY;
			
			index2 = cursorX;
			line2 = cursorY;
		}
		
		if (lines.size() > 0 && scrY + (FONT_SIZE + (FONT_SIZE / 4)) * 3 > (lines.size() + 2) * (FONT_SIZE + (FONT_SIZE / 4))) {
			scrY = (lines.size() * FONT_SIZE) - (FONT_SIZE * 3);
			
			cursorX = lines.get(lines.size() - 1).getChars().size();
			cursorY = lines.size();
			
			setCursorWithinBounds();
		}
		
		realcx = ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX;
		realcy = MIN_Y + cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2;
		
		//int speed = 10;
		
		/*if (drawcx < realcx) drawcx += speed; // talvez quando for adicionar animação tá aqui pronto
		if (drawcx > realcx) drawcx -= speed;
		
		if (drawcy < realcy) drawcy += speed;
		if (drawcy > realcy) drawcy -= speed;*/
		
		if ((KeyInput.isKeyPressed() && !KeyInput.isControlDown() && !KeyInput.isShiftDown()) || ((cursorX != index1 && cursorY != line1) && (cursorX != index2 && cursorY != line2)))
			selecting = false;
		
		drawcx = realcx;
		drawcy = realcy;
		
		if (FONT_SIZE < 1)
			FONT_SIZE = 16;
		
		if (MouseInput.isMouseDragged()) {
			selecting = true;
			
			index1 = cursorX;
			line1 = cursorY;
			
			index2 = mx;
			line2 = my;
			
			if (index1 == index2 && line1 == line2)
				selecting = false;
			
			if (line2 < line1) {
				int tempindex1 = index1;
				int templine1 = line1;
				
				index1 = index2;
				index2 = tempindex1;
				
				line1 = line2;
				line2 = templine1;
				
			}
			else if (line2 == line1) {
				if (index2 < index1) {
					int tempindex1 = index1;
					
					index1 = index2;
					index2 = tempindex1;
				}
			}
		}
		
		if (editing != null)
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " • " + editing.getRegent().getRegent().getName() + " - Boot IDE");
		else if (Main.baseFolder != null)
			Main.screen.frame.setTitle(Main.baseFolder.getName() + " - Boot IDE");
		
		showCursorData = false;
		
		if (KeyInput.isAltDown() && editing != null && hovered()) {
			KeyInput.updateKeys();
			
			showCursorData = true;
		}
		
		/*if (KeyInput.isKeyPressed() && hovered() && editing != null) {
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_Z && KeyInput.isControlDown()) // Ctrl + Z
				selectMode = true;
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ESCAPE) {
				selectMode = false;
				isSelectingFirst = true;
				
				CommandTerminal.runCommand("deselect");
			}
		}*/
		
		if (selectMode && leftClicked()) {
			selecting = true;
			
			MouseInput.updateMouse();
			
			/*my = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
			mx = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
			
			double offset = mx * 0.7; // com esse padrão fica quase perfeito
			offset = Math.ceil(offset);
			offset = mx - offset;
			
			mx += (int) offset;
			mx++;
			
			if (FONT_SIZE < 11)
				my--;
			
			mx = setWithinBounds(mx, my, true);
			my = setWithinBounds(mx, my, false);*/
			
			/*while (((x + 40) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
				mx++;
			
			while (MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
				my--;*/
			
			/*if (index2 < index1 && line2 >= line1) {
				int temp = index2;
				
				index2 = index1;
				index1 = temp;
			}
			
			if (line2 < line1) {
				int temp = line2;
				
				line2 = line1;
				line1 = temp;
			}*/
			
			if (isSelectingFirst) {
				line1 = my;
				index1 = mx;
				
				isSelectingFirst = false;
			}
			else {
				line2 = my;
				index2 = mx;
				
				selectMode = false;
				isSelectingFirst = true;
			}
		}
		
		try {
			clipboard = (String) Main.toolkit.getSystemClipboard().getData(DataFlavor.stringFlavor);
		} catch (HeadlessException | UnsupportedFlavorException | IOException | IllegalStateException e) {
			System.err.println("Não é string. Resetando!");
			clipboard = "";
		}
		
		if (MouseInput.hovered(x, 0, Main.screen.getWidth(), Tab.HEIGHT) && tabs != null && tabs.size() > 0) {
			if (MouseInput.isMouseRolling()) {
				if (MouseInput.wheelUp() && tabScr < 0)
					tabScr += 203;						// 3 é a compensação para as tab n se distanciar
				else if (MouseInput.wheelDown() && (tabs.get(tabs.size() - 1).getX() + tabScr) - 200 > (CommandTerminal.expOff ? 0 : 280)) { // 280
					tabScr -= 203;
				}
				
				for (IDEComponent i : components) {
					if (i instanceof RightClickOption)
						IDEComponent.toRemove.add(i);
				}
			}
		}
		
		if (hovered() && editing != null) {
			Main.screen.setCursor(new Cursor(Cursor.TEXT_CURSOR));
			
			if (MouseInput.isMouseRolling()) {
				new Thread() {
					public void run() {
					if (KeyInput.isShiftDown()) {
						if (MouseInput.wheelUp() && scrX > 0)
							scrX -= FONT_SIZE * 3;
						else if (MouseInput.wheelDown())
							scrX += FONT_SIZE * 3;
					}
					else {
						if (MouseInput.wheelUp() && scrY > 0)
							scrY -= (FONT_SIZE + (FONT_SIZE / 4)) * 3;
						else if (MouseInput.wheelDown() && scrY + (FONT_SIZE + (FONT_SIZE / 4)) * 3 < lines.size() * (FONT_SIZE + (FONT_SIZE / 4)))
							scrY += (FONT_SIZE + (FONT_SIZE / 4)) * 3;
					}
					
					return;
					}
				}.start();
			}
			
			if (leftClicked() && !RightClickOption.isRightClickActive() && !selectMode) {
//				cursorY = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4))); // resolver seta do terminal de comando
//				cursorX = (((MouseInput.getMouseX() - (x + 40)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
//				
//				/*double offset = cursorX * 0.75; // 0.7: 16, 0.75: 15 e 14
//				offset = Math.ceil(offset);
//				offset = cursorX - offset; // arrumar isso
//				
//				cursorX += (int) offset;
//				cursorX += cursorX < 2 ? 0 : 2;*/
//				
//				//cursorX = setWithinBounds(cursorX, cursorY, true);
//				//cursorY = setWithinBounds(cursorX, cursorY, false);
//				
//				while (((x + 40) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < MouseInput.getMouseX()) // detecta se a posição real do cursor for menor do que a do cursor e fica adicionando enquanto for menor
//					cursorX++;
//				
//				while (MIN_Y + cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2 > MouseInput.getMouseY()) // o mesmo para aqui, só que com o y
//					cursorY--;
				
				cursorX = mx;
				cursorY = my;
				
				setCursorWithinBounds();
			}
		}
		else
			Main.screen.setCursor(Cursor.getDefaultCursor()); // 24/04/2021 - 09:18
		
		if (rightClicked()) {
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), 550, "Abrir Prompt de Comando", (s) -> execute(s), "cmd");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, 550, "Abrir Terminal de Comando", (s) -> execute(s), "term");
			
			if (Main.baseFolder != null)
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, 550, "Abrir no Explorador de Arquivos", (s) -> execute(s), "sysexp");
			IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (editing != null ? (selecting ? 330 : 210) : 90), 550, "Definir pasta atual como Pasta Base", (s) -> execute(s), "setbase");
			
			if (editing != null) {
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 240 : 150), 550, "Selecionar Linha", (s) -> CommandTerminal.runCommand(s), "selectline");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, 550, "Salvar", (s) -> execute(s), "save");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 150 : 120), 550, "Colar", (s) -> execute(s), "paste");
				
				//IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 300 : 180), 550, "Modo de Seleção", (s) -> CommandTerminal.runCommand(s), "selectmode");
			
			if (selecting) {
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, 550, "Copiar", (s) -> CommandTerminal.runCommand(s), "copy");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 180, 550, "Cortar", (s) -> CommandTerminal.runCommand(s), "cut");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 210, 550, "Deletar", (s) -> CommandTerminal.runCommand(s), "del");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 300, 550, "Desselecionar", (s) -> CommandTerminal.runCommand(s), "deselect");
			}
			
			if (Main.baseFolder != null && editing != null) {
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 270 : 180), 550, "Selecionar Tudo", (s) -> CommandTerminal.runCommand(s), "selectall");
				IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 360 : 240), 550, "Abrir arquivo com o programa padrão", (s) -> execute(s), "opendef");
			}
			}
		}
		
		if (KeyInput.isKeyPressed() && !SetFileName.added && !CommandTerminal.active && !selectMode) {
			setCursorWithinBounds();
			
			new Thread() {
				public void run() {
					try {
						if (editing == null) return;
						
						for (IDELine l : lines) {
							l.setFonts(
									automaticColor(
											toCharArray(
													l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
						
						}
					} catch (ConcurrentModificationException e) {}
				}
			}.start();
			
			// Detectar atalhos
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) { // Ctrl + Home - Começo do Documento
				KeyInput.updateKeys();
				
				scrX = 0;
				scrY = 0;
				
				cursorX = 0;
				cursorY = 0;
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_END) { // Ctrl + End - Fim do Documento
				KeyInput.updateKeys();
				
				//scrX = (lines.get(lines.size() - 1).getChars().size() * FONT_SIZE) - FONT_SIZE * 10; // esse - FONT_SIZE * 5 é pra dar um offset para trás e ficar no meio da tela.
				scrY = (lines.size() * FONT_SIZE) - (FONT_SIZE * 3);
				
				cursorX = lines.get(lines.size() - 1).getChars().size();
				cursorY = lines.size();
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_HOME) { // Home - Começo da Linha
				KeyInput.updateKeys();
				
				scrX = 0;
				cursorX = 0;
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_END) { // End - Fim da Linha
				KeyInput.updateKeys();
				
				//scrX = (lines.get(cursorY - 1).getChars().size() * FONT_SIZE) - FONT_SIZE * 10;
				cursorX = lines.get(cursorY - 1).getChars().size();
				
				setCursorWithinBounds();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_D) { // Ctrl + D (Desselecionar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("deselect");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_M) { // Ctrl + M (Go To Cursor)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("gotocursor");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_X) { // Ctrl + X (Cortar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("cut");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_K) { // Ctrl + K (Alternar Explorador)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("toggleexplorer");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.isAltDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + Alt + T (Fechar Todas as Abas)
				KeyInput.updateKeys();
				
				tabs.clear();
				editing = null;
				
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + Shift + T (Fechar Aba)
				KeyInput.updateKeys();
				
				editing.close();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_T) { // Ctrl + T (Terminal)
				KeyInput.updateKeys();
					
				execute("term");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_B || KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_WINDOWS) { // Ctrl + B OU Ctrl + Win (Cmd)
				KeyInput.updateKeys();
					
				execute("cmd");
					
				return;
			}
			
			if (editing == null) return;
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_S) { // Ctrl + S (Salvar)
				KeyInput.updateKeys();
					
				editing.save(); // 08/05/2021 - 16:12
					
				return;
			}
			
			if (KeyInput.isControlDown() &&  KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A) { // Ctrl + Shift + A (Selecionar Tudo)
				KeyInput.updateKeys();
					
				cursorX = 0;
				cursorY = 1;
				
				CommandTerminal.runCommand("selectall");
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_A) { // Ctrl + A (Selecionar Linha)
				KeyInput.updateKeys();
				
				cursorX = 0;
					
				CommandTerminal.runCommand("selectline");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_C) { // Ctrl + C (Copiar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("copy");
					
				return;
			}
				
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_V) { // Ctrl + V (Colar)
				KeyInput.updateKeys();
					
				paste();
					
				return;
			}
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE || (selecting && KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE)) { // Ctrl + Delete (Deletar)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("del");
					
				return;
			}
			
			/*if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_O) { // Ctrl + O (Toggle Syntax Errors)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("togglesyntaxerrors");
					
				return;
			}*/
			
			if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == KeyEvent.VK_P) { // Ctrl + P (Toggle Code Hints)
				KeyInput.updateKeys();
				
				CommandTerminal.runCommand("togglecodehints");
					
				return;
			}
			
			if (!(KeyInput.isAltDown()|| KeyInput.isControlDown())) { // se ctrl, alt NÃO estão pressionados
			
				if (!KeyInput.isShiftDown()) {
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
						KeyInput.updateKeys();
						
						cursorY--;
						
						setCursorWithinBounds(); // detectar teclas que usam o alt gr TODO
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
						KeyInput.updateKeys();
						
						cursorY++;
						
						setCursorWithinBounds();
						
						return;
					}
					
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
						KeyInput.updateKeys();
						
						cursorX--;
						
						setCursorWithinBounds();
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
						KeyInput.updateKeys();
						
						cursorX++;
						
						setCursorWithinBounds();
						
						return;
					}
				}
				else {
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) { // aperfeiçoar
						KeyInput.updateKeys();
						
						line1--;
						
						selecting = true;
						
						line1 = setWithinBounds(index1, line1, false);
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DOWN) {
						KeyInput.updateKeys();
						
						line2++;
						
						selecting = true;
						
						line2 = setWithinBounds(index2, line2, false);
						
						return;
					}
					
					if (KeyInput.getKeyCodePressed() == KeyEvent.VK_LEFT) {
						KeyInput.updateKeys();
						
						index1--;
						
						selecting = true;
						
						index1 = setWithinBounds(index1, line1, true);
						
						return;
					}
					
					else if (KeyInput.getKeyCodePressed() == KeyEvent.VK_RIGHT) {
						KeyInput.updateKeys();
						
						index2++;
						
						selecting = true;
						
						index2 = setWithinBounds(index2, line2, true);
						
						return;
					}
					
					/*
					index1 = setWithinBounds(index1, line1, true);
					line1 = setWithinBounds(index1, line1, false);
					
					index2 = setWithinBounds(index2, line2, true);
					line2 = setWithinBounds(index2, line2, false);
					*/
				}
			
			KeyInput.updateKeys();
			
			StringBuilder cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
				KeyInput.updateKeys();
				
				if (!selecting) {
					if (cursorX > 0) {
						cY.deleteCharAt(cursorX - 1);
					
						cursorX--;
					
						setCursorWithinBounds();
	
						editing.setSaved(false);
					
						register(cY, cursorY - 1);
					}
					else if (cursorY > 1) {
						String s = cY.toString();
						
						cursorX = lines.get(cursorY - 2).getChars().size();
						
						lines.remove(cursorY - 1);
						cursorY--;
						
						cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
						
						cY.append(s);
	
						editing.setSaved(false);
						
						register(cY, cursorY - 1);
					}
					
					return;
				}
				
				System.out.println("a");
				
				CommandTerminal.runCommand("del");
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) {
				KeyInput.updateKeys();
				
				if (cursorX < cY.length()) {
					cY.deleteCharAt(cursorX);
				
					setCursorWithinBounds();

					editing.setSaved(false);
				
					register(cY, cursorY - 1);
				}
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB) {
				KeyInput.updateKeys();
				
				cY.insert(cursorX, "    ");
				
				cursorX += 4;
				editing.setSaved(false);
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_ENTER) {
				KeyInput.updateKeys();
				
				StringBuilder spaces = new StringBuilder();
				String s = cY.substring(cursorX);
				
				for (int i = 0; i < countChar(cY.toString(), ' '); i++)
					spaces.append(' ');
				
				for (int i = 0; i < countChar(cY.toString(), (char) 9); i++) // char 9 é o tab
					spaces.append(' ');
				
				int nSpaces = spaces.length();
				
				spaces.append(s);
				s = spaces.toString();
				
				cY.delete(cursorX, cY.length());
				
				List<IDEFont> fs = new ArrayList<>();
				
				for (int i = 0; i < s.length(); i++)
					fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
				
				lines.add(cursorY, new IDELine(toCharList(s.toCharArray()), fs));
				
				register(cY, cursorY - 1);
				
				editing.setSaved(false);
				
				cursorX = nSpaces;
				cursorY++;
				
				/*if (cursorX < lines.get(cursorY - 1).getChars().size())
				if (lines.get(cursorY - 1).getChars().size() != 0 &&
					lines.get(cursorY - 1).getChars().get(cursorX) == '}') {
					spaces = new StringBuilder();
					s = cY.substring(cursorX);
					
					for (int i = 0; i < countChar(cY.toString(), ' '); i++)
						spaces.append(' ');
					
					for (int i = 0; i < countChar(cY.toString(), (char) 9); i++) // char 9 é o tab
						spaces.append(' ');
					
					nSpaces = spaces.length();
					
					spaces.append(s);
					s = spaces.toString();
					
					cY.delete(cursorX, cY.length());
					
					fs = new ArrayList<>();
					
					for (int i = 0; i < s.length(); i++)
						fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
					
					lines.add(cursorY, new IDELine(toCharList(s.toCharArray()), fs));
					
					register(cY, cursorY - 1);
					
					editing.setSaved(false);
					
					StringBuilder setBrackets = new StringBuilder("}");
					register(setBrackets, cursorY);
					
					StringBuilder setSpaces = new StringBuilder("    ");
					register(setSpaces, cursorY - 1);
					
					cursorX += 4;
					
					setCursorWithinBounds();
					
					new Thread() {
						public void run() {
							try {
								if (editing == null) return;
								
								for (IDELine l : lines) {
									l.setFonts(
											automaticColor(
													toCharArray(
															l.getChars()), ListableFile.getFileExtension(editing.getRegent().getRegent())));
								
								}
							} catch (ConcurrentModificationException e) {}
						}
					}.start();
				}*/
				
				return;
			}
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SHIFT) return;
			
			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = addAccents(keyCode, c);
			
			cY = write(cY, c);
			
			if (codeHintsOn)
				cY = addCodeHints(cY);
			
			register(cY, cursorY - 1);
			
			cursorX++;
			
			setCursorWithinBounds();
			
			if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
		
			editing.setSaved(false);
		} // <-
		} // não ligue pra isso :)
		
		if (tabs != null) {
			for (Tab t : tabs)
				t.tick();
		
			tabs.addAll(toAdd);
			toAdd.clear();
			
			tabs.removeAll(toRemove);
			toRemove.clear();
			
			lines.removeAll(linesToRemove);
			linesToRemove.clear();
		}
		
		if (index1 < 0) index1 = 0;
		if (line1 < 1) line1 = 1;
		
		if (index2 < 0) index2 = 0;
		if (line2 < 1) line2 = 1;
		
		if (scrX < 0) scrX = 0;
		if (scrY < 0) scrY = 0;
	}
	
	public void render(Graphics g) {
		Graphics2D g2 = (Graphics2D) g;
		
		g.setColor(Colors.explorerLight);
		g2.setStroke(new BasicStroke(8f));
		
		//g2.drawLine(x, y, x, height);
		g2.drawLine(x, 30, width, 30);
		
		g.setColor(Colors.background);
		g.fillRect(x, y, width, height);
		
		if (tabs == null || tabs.size() == 0) return;
		
		if (editing != null) {
			g.setColor(Colors.explorer);
			g.fillRect(x, MIN_Y, Main.screen.getWidth(), height);
		}
		
//		if (editing != null &&																	// não vamos mostrar imagens aqui
//			(ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".png") || // se for uma imagem
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpg") ||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".jpeg")||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".gif") ||
//			 ListableFile.getFileExtension(editing.getRegent().getRegent()).equals(".bmp"))) {
//			try {
//				BufferedImage get = ImageIO.read(getClass().getResource(editing.getRegent().getRegent().getAbsolutePath())); // esse get tá null
//				
//				g.drawImage(get, (x + (width / 2)) - get.getWidth(), (y + (height / 2)) - get.getHeight(), get.getWidth() * 2, get.getHeight() * 2, null);
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			return; // pra n renderizar texto, aquele monte de coisa estranha
//		}
		
		try {
			for (int i = 0; i < lines.size(); i++) {
				if (selecting) {
					g.setColor(Colors.select);
					
					if (i == line1 - 1) { // - 1 porque a line1 é base 1
						if (i == line2 - 1) {
							g.fillRect(((x + 48) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do index1 até o index2
								(line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								(((x + 48) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) - (((x + 48) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
						else {
							g.fillRect(((x + 48) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o fim da linha
								(line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								(((x + 48) + lines.get(i).getChars().size() * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) - (((x + 48) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
					}
					/*if (i == line2 - 1) {
						if (i != line1 - 1) {
							g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								(line2 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 15 ? 5 : 0),
								(((x + 38) + (index2 - 29) * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) + 5,
								FONT_SIZE + 4);
						}
					}*/
					if (i > line1 && i < line2) {
						g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								(i + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								(lines.get(i - 1).getChars().size()) * (FONT_SIZE - (FONT_SIZE / 4)) - scrX, // n é x + 38, é a width, não o x2!
								FONT_SIZE + 4);
					}
				}
			}
			
			for (int i = 0; i < lines.size(); i++) {
				int yr = MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY;
				
				if (yr < 0 || yr > Screen.HEIGHT) continue;
				
				char[] cs = toCharArray(lines.get(i).getChars());
				IDEFont[] fs = toArray(lines.get(i).getFonts());
				
				if (lines.get(i) == null) break;
				
				if (MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY < MIN_Y - 15) continue;
				
				if (i == cursorY - 1) {
					g.setColor(Colors.backgroundLight);
					g.fillRect(x, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY - 1, Main.screen.getWidth(), FONT_SIZE + (FONT_SIZE / 4) + 1);
				} // não mais x + 50
				
				if (selecting) {
					g.setColor(Colors.select);
					
					if (i == line1 - 1) { // - 1 porque a line1 é base 1
						if (i == line2 - 1) {
							g.fillRect(((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher do index1 até o index2
								(line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								(((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) - (((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
						else {
							g.fillRect(((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o fim da linha
								(line1 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 14 ? 5 : 0),
								(((x + 50) + lines.get(i).getChars().size() * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) - (((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX),
								FONT_SIZE + 4);
						}
					}
					if (i == line2 - 1) {
						if (i != line1 - 1) {
							g.fillRect(((x + 38) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								(line2 + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - (FONT_SIZE > 15 ? 5 : 0),
								(((x + 38) + (index2 - (FONT_SIZE == 15 ? 28 : 29)) * (FONT_SIZE - (FONT_SIZE / 4))) - scrX) + (Math.abs(14 - FONT_SIZE) * FONT_SIZE) + 2,// + FONT_SIZE == 15 ? FONT_SIZE : 0,
								FONT_SIZE + 4);
						}
					}
					/*if (i > line1 && i < line2) {
						g.fillRect(((x + 50) + (FONT_SIZE - (FONT_SIZE / 4))) - scrX, // preencher até o index2
								(i + 1) * (FONT_SIZE + (FONT_SIZE / 4)) - scrY - 5,
								(lines.get(i - 1).getChars().size() - 1) * (FONT_SIZE - (FONT_SIZE / 4)) - scrX, // n é x + 50, é a width, não o x2!
								FONT_SIZE + 4);
					}*/
				}
				
				//g.setColor(Colors.select);
				//g.fillRect((x + 50) - scrX, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY, FONT_SIZE, FONT_SIZE + 4);
				
				IDEFont font = i == cursorY - 1 ? new IDEFont(Fonts.selectedLineNumberNormal, FONT_SIZE) : new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
				
				//font = i == linesWithErrors.get(i) ? new IDEFont(Fonts.select1Normal, FONT_SIZE) : save;
				Fonts.drawString(String.valueOf(i + 1), x + 1, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY, font, g);
				Fonts.drawChars(cs, (x + 50) - scrX, MIN_Y + (i * (FONT_SIZE + (FONT_SIZE / 4))) - scrY, fs, x + (FONT_SIZE * 2), g);
			}
		} catch (Exception e) { }
		
		if (showCursorData) {
			KeyInput.updateKeys();
			
			g.setColor(new Color(0, 0, 0, 0.3f));
			g.fillRect(0, 0, Main.screen.getWidth(), Main.screen.getHeight());
			
			Fonts.drawString("Posição do Cursor:", MouseInput.getMouseX() + 10, MouseInput.getMouseY() - 16 - 5, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			
			Fonts.drawString("Coluna: " + (cursorX + 1), MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, 16), g);
			Fonts.drawString(" Linha: " + cursorY, MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 16 + 3, new IDEFont(Fonts.lighterGrayNormal, 16), g);
		}
		
		/*if (selecting) {
			if (line1 * (FONT_SIZE + (FONT_SIZE / 4)) - scrY > 0 && ((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX > x + 50) { 
				g.setColor(Colors.select1);
				g.fillRect(((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, MIN_Y + line1 * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
			
				Fonts.drawString("1", ((x + 50) + index1 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX - 5, MIN_Y + line1 * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY + 15, new IDEFont(Fonts.select1Bold, FONT_SIZE), g);
			}
			
			if (line2 * (FONT_SIZE + (FONT_SIZE / 4)) - scrY > 0 && ((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX > x + 50) {
				g.setColor(Colors.select2);
				g.fillRect(((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, MIN_Y + line2 * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
				
				Fonts.drawString("2", ((x + 50) + index2 * (FONT_SIZE - (FONT_SIZE / 4))) - scrX - 5, MIN_Y + line2 * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY + 15, new IDEFont(Fonts.select2Bold, FONT_SIZE), g);
			}
		}*/
		
		/*my = (MouseInput.getMouseY() / (FONT_SIZE + (FONT_SIZE / 4)) - 1) + (scrY / (FONT_SIZE + (FONT_SIZE / 4)));
		mx = (((MouseInput.getMouseX() - (x + 50)) / FONT_SIZE) + (scrX / FONT_SIZE)); // é * 0.7
		
		double offset = mx * 0.7;
		offset = Math.ceil(offset);
		offset = mx - offset;
		
		mx += (int) offset;
		mx++;
		
		if (FONT_SIZE < 11)
			my--;
		
		mx = setWithinBounds(mx, my, true);
		my = setWithinBounds(mx, my, false);
		
		if (selectMode) {
			g.drawImage(gradient, x - 1, 0, Main.screen.getWidth() - x, 130, null);
			
			g.setColor(Colors.selectCursor);
			g.fillRect(((x + 50) + mx * (FONT_SIZE - (FONT_SIZE / 4))) - scrX, MIN_Y + my * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY - 2, 2, FONT_SIZE);
			
			Fonts.drawString("[Esc] Cancelar", MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 30, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			Fonts.drawString("[Clique Direito] Selecionar", MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 55, new IDEFont(Fonts.lighterGrayNormal, 16), g);
			
			if (isSelectingFirst) {
				Fonts.drawString("Selecione a primeira posição", MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, 16), g);
				
				Fonts.drawString("1", MouseInput.getMouseX() + 25, MouseInput.getMouseY() - 50, new IDEFont(Fonts.lighterGrayNormal, 16 * 3), g);
			}
			
			else {
				Fonts.drawString("Selecione a segunda posição", MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, 16), g);
			
				Fonts.drawString("2", MouseInput.getMouseX() + 25, MouseInput.getMouseY() - 50, new IDEFont(Fonts.lighterGrayNormal, 16 * 3), g);
			}
		}*/
		
		// Desenhar cursor
		if (showCursor && !((cursorY * (FONT_SIZE + (FONT_SIZE / 4)) - FONT_SIZE - scrY < MIN_Y - 40 || ((x + 50) + cursorX * (FONT_SIZE - (FONT_SIZE / 4))) - scrX < x + (FONT_SIZE * 2))) && !WindowInput.isDeactivated()) {
			g.setColor(Colors.cursor);
			g.fillRect(drawcx, drawcy, 2, FONT_SIZE); // * 14
		}
		
		// desenhar barra inferior
		if (editing != null) {
			g.setColor(Colors.backgroundLight);
			g.fillRect(x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22);
			
			Fonts.drawString(codeType + " - " + extType, x + 10, Main.screen.getHeight() - 20, new IDEFont(Fonts.otherNormal, 16), g);
		}
		
		g.setColor(Colors.background);
		g.fillRect(x, 0, width, 35);
		
		for (Tab t : CodeEditor.tabs)
			t.render(g);
		
		/*if (editing != null) {
			int y = this.y;
			int height = 50;
			
			g.setColor(Colors.background);
			g.fillRect(Main.screen.getWidth() - 4, y, 4, height); // 131 linhas = 70 height
		}*/
	}
}
