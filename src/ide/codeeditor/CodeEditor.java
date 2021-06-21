package ide.codeeditor;
import ide.components.CommandTerminal;
import ide.components.IDEComponent;
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
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;

public class CodeEditor extends IDEComponent {
	
  public static volatile int FONT_SIZE = 16;
  
  public static Tab editing;
  
  private boolean showCursorData = false;
  
  public static boolean selectMode;
  
  public static boolean isSelectingFirst = true;
  
  public static boolean isMultilineCommenting = false;
  
  public static boolean selecting;
  
  public static int line1;
  
  public static int line2;
  
  public static int index1;
  
  public static int index2;
  
  public static boolean isCssPart;
  
  public static boolean isJSPart;
  
  public static boolean codeHintsOn = true;
  
  public static String codeType = "";
  
  public static String extType = "";
  
  public static boolean syntaxErrorsOn = true;
  
  public static boolean isAnotherIteration = false;
  
  public static boolean foundExt = false;
  
  private int realcx;
  
  private int realcy;
  
  private int drawcx = this.x + 50 + cursorX * (FONT_SIZE - FONT_SIZE / 4) - scrX;
  
  private int drawcy = 35 + cursorY * (FONT_SIZE + FONT_SIZE / 4) - FONT_SIZE - scrY - 2;
  
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
  
  private static int mx;
  
  private static int my;
  
  public static List<Integer> linesWithErrors = new ArrayList<>();
  
  private Thread syntaxErrors;
  
  private static List<Integer> numopenbrackets = new ArrayList<>();
  
  private static List<Integer> numclosebrackets = new ArrayList<>();
  
  public CodeEditor(final int x, int y, int width, int height) {
    super(x, y, width, height, null);
    
    tabs = new ArrayList<>();
    toAdd = new ArrayList<>();
    toRemove = new ArrayList<>();
    cursor = new Animation(2, true) {
        private boolean flip = false;
        
        public void play() {
          CodeEditor.this.showCursor = !this.flip;
          this.flip = !this.flip;
          super.play();
        }
      };
    (new Thread() {
        public void run() {
          CodeEditor.cursor.play();
        }
      }).start();
    (new Thread() {
        public void run() {
          int offset = CommandTerminal.expOff ? 280 : 0;
          int lcx = !CommandTerminal.expOff ? 0 : 280;
          int lcmx = CodeEditor.mx;
          int lcmy = CodeEditor.my;
          while (true) {
            lcmy = MouseInput.getMouseY() / (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - 1 + CodeEditor.scrY / (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4);
            lcmx = (MouseInput.getMouseX() - x + 40) / CodeEditor.FONT_SIZE + CodeEditor.scrX / CodeEditor.FONT_SIZE;
            while (lcx + 40 + lcmx * (CodeEditor.FONT_SIZE - CodeEditor.FONT_SIZE / 4) - CodeEditor.scrX + offset < MouseInput.getMouseX())
              lcmx++; 
            while (lcx + 40 + lcmx * (CodeEditor.FONT_SIZE - CodeEditor.FONT_SIZE / 4) - CodeEditor.scrX + offset > MouseInput.getMouseX())
              lcmx--; 
            while (35 + lcmy * (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - CodeEditor.FONT_SIZE - CodeEditor.scrY - 2 < MouseInput.getMouseY())
              lcmy++; 
            while (35 + lcmy * (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - CodeEditor.FONT_SIZE - CodeEditor.scrY - 2 > MouseInput.getMouseY())
              lcmy--; 
            if (CodeEditor.FONT_SIZE < 13)
              lcmx--; 
            lcmx = CodeEditor.this.setWithinBounds(lcmx, lcmy, true);
            lcmy = CodeEditor.this.setWithinBounds(lcmx, lcmy, false);
            CodeEditor.my = MouseInput.getMouseY() / (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - 1 + CodeEditor.scrY / (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4);
            CodeEditor.mx = (MouseInput.getMouseX() - x + 40) / CodeEditor.FONT_SIZE + CodeEditor.scrX / CodeEditor.FONT_SIZE;
            while (x + 40 + CodeEditor.mx * (CodeEditor.FONT_SIZE - CodeEditor.FONT_SIZE / 4) - CodeEditor.scrX < MouseInput.getMouseX())
              CodeEditor.mx = CodeEditor.mx + 1; 
            while (x + 40 + CodeEditor.mx * (CodeEditor.FONT_SIZE - CodeEditor.FONT_SIZE / 4) - CodeEditor.scrX > MouseInput.getMouseX())
              CodeEditor.mx = CodeEditor.mx - 1; 
            while (35 + CodeEditor.my * (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - CodeEditor.FONT_SIZE - CodeEditor.scrY - 2 < MouseInput.getMouseY())
              CodeEditor.my = CodeEditor.my + 1; 
            while (35 + CodeEditor.my * (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) - CodeEditor.FONT_SIZE - CodeEditor.scrY - 2 > MouseInput.getMouseY())
              CodeEditor.my = CodeEditor.my - 1; 
            if (CodeEditor.FONT_SIZE < 13)
              CodeEditor.mx = CodeEditor.mx - 1; 
            CodeEditor.mx = CodeEditor.this.setWithinBounds(CodeEditor.mx, CodeEditor.my, true);
            CodeEditor.my = CodeEditor.this.setWithinBounds(CodeEditor.mx, CodeEditor.my, false);
            if (CommandTerminal.expOff) {
              CodeEditor.mx = lcmx;
              CodeEditor.my = lcmy;
            } 
            try {
              Thread.sleep(8L);
            } catch (InterruptedException e) {
              e.printStackTrace();
            } 
          } 
        }
      }).start();
    this.syntaxErrors = new Thread() {
        public void run() {
          while (true) {
            try {
              while (true)
                CodeEditor.linesWithErrors = CodeEditor.syntaxErrors(CodeEditor.lines); 
            } catch (Exception e) {
              e.printStackTrace();
            } 
          } 
        }
      };
    this.syntaxErrors.start();
    try {
      gradient = ImageIO.read(getClass().getResource("/gradient.png"));
    } catch (IOException e) {
      e.printStackTrace();
    } 
  }
  
  public boolean hovered() {
    Rectangle mouse = new Rectangle(MouseInput.getMouseX(), MouseInput.getMouseY(), 1, 1);
    Rectangle comp = new Rectangle(this.x, 35, this.width, this.height);
    return mouse.intersects(comp);
  }
  
  public static <T> List<T> removeDuplicates(List<T> list) {
    return new ArrayList<>(new LinkedHashSet<>(list));
  }
  
  public static List<Integer> syntaxErrors(List<IDELine> lines) {
    if (lines.size() == 0)
      return new ArrayList<>(); 
    numopenbrackets.clear();
    numclosebrackets.clear();
    List<IDELine> getlines = Collections.synchronizedList(CodeEditor.lines);
    List<Integer> linescounted = new ArrayList<>();
    List<Integer> linesfound = new ArrayList<>();
    boolean bracketsHasPair = true;
    if (getlines.size() == 0)
      return linesfound; 
    for (int i = 0; i < getlines.size(); i++) {
      IDELine l = getlines.get(i);
      if (l == null)
        break; 
      Object[] arr = l.getChars().toArray();
      char[] sa = new char[arr.length];
      for (int j = 0; j < arr.length; j++) {
        if (arr[j] != null)
          sa[j] = ((Character)arr[j]).charValue(); 
      } 
      String s = (new String(sa)).toLowerCase();
      if (s.contains("{"))
        numopenbrackets.add(Integer.valueOf(i)); 
      if (s.contains("}"))
        numclosebrackets.add(Integer.valueOf(i)); 
      byte b;
      int k;
      char[] arrayOfChar1;
      for (k = (arrayOfChar1 = s.toCharArray()).length, b = 0; b < k; ) {
        char c = arrayOfChar1[b];
        if (c == '{') {
          if (!bracketsHasPair && numopenbrackets.size() != numclosebrackets.size())
            linescounted.add(Integer.valueOf(i)); 
          bracketsHasPair = false;
        } 
        if (c == '}') {
          if (bracketsHasPair && numopenbrackets.size() != numclosebrackets.size())
            linescounted.add(Integer.valueOf(i)); 
          bracketsHasPair = true;
        } 
        b++;
      } 
    } 
    if (numopenbrackets.size() != numclosebrackets.size() && linesfound.size() > 0) {
      linesfound = linescounted;
      linesfound = removeDuplicates(linesfound);
      Integer get0 = linesfound.get(0);
      linesfound.clear();
      linesfound.add(get0);
    } 
    return linesfound;
  }
  
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
  
  public static List<Integer> findWord(String textString, String word) {
    List<Integer> indexes = new ArrayList<>();
    String lowerCaseTextString = textString;
    String lowerCaseWord = word;
    int index = 0;
    while (index != -1) {
      index = lowerCaseTextString.indexOf(lowerCaseWord, index);
      if (index != -1) {
        indexes.add(Integer.valueOf(index));
        index++;
      } 
    } 
    return indexes;
  }
  
  public static List<IDEFont> color(int s, int e, IDEFont color, List<IDEFont> fs) {
    if (e < s)
      return fs; 
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

      for(int i = 0; i < chars.length; ++i) {
         fs.add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
      }
      
      int len;
      Integer i;
      Iterator<?> var12;
      List<?> indxs;
      String[] tagsss;
      if (ext.equals(".java") || ext.equals(".c") || ext.equals(".cs") || ext.equals(".cpp") || ext.equals(".cxx") || ext.equals(".js") || ext.equals(".h") || ext.equals(".hpp") || ext.equals(".hxx") || ext.equals(".lua") || ext.equals(".rs") || ext.equals(".asm") || ext.equals(".php") || ext.equals(".kt") || ext.equals(".vue") || ext.equals(".py") || ext.equals(".pyd") || ext.equals(".rb") || ext.equals(".ino") || ext.equals(".ts") || ext.equals(".swift") || ext.equals(".go") || ext.equals(".r") || ext.equals(".jl") || ext.equals(".pl") || ext.equals(".has") || ext.equals(".hs") || ext.equals(".fs") || ext.equals(".coffee") || ext.equals(".m") || ext.equals(".jsx") || ext.equals(".ld") || ext.equals(".pas") || ext.equals(".pp") || ext.equals(".scala") || ext.equals(".dart") || ext.equals(".md")) {
         indxs = findWord(new String(chars), ")");
         
         Iterator<?> var5;
         for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs)) {
            i = (Integer)var5.next();
            len = i;

            for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != '('; ++len) {
               --len;
            }
         }

         indxs = findWord(new String(chars), "]");

         for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), fs)) {
            i = (Integer)var5.next();
            len = i;

            for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != '[' && chars[len] != ':'; ++len) {
               --len;
            }
         }

         boolean hasSpace;
         if (!ext.equals(".md")) {
            indxs = findWord(new String(chars), ":");
            var5 = indxs.iterator();

            label4981:
            while(true) {
               if (!var5.hasNext()) {
                  indxs = findWord(new String(chars), ".");

                  for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                     i = (Integer)var5.next();
                     len = i;

                     for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != '[' && chars[len] != ']' && chars[len] != ',' && chars[len] != ':'; ++len) {
                        --len;
                     }
                  }

                  indxs = findWord(new String(chars), ";");

                  for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                     i = (Integer)var5.next();
                     len = i;

                     for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != '[' && chars[len] != ']' && chars[len] != ',' && chars[len] != '.' && chars[len] != ':'; ++len) {
                        --len;
                     }
                  }

                  indxs = findWord(new String(chars), ".");

                  for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                     i = (Integer)var5.next();
                     len = i;

                     for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != '[' && chars[len] != ']' && chars[len] != ',' && chars[len] != ':'; ++len) {
                        --len;
                     }
                  }

                  indxs = findWord(new String(chars), "[");

                  for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                     i = (Integer)var5.next();
                     len = i;

                     for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != ']' && chars[len] != ',' && chars[len] != '.' && chars[len] != ':'; ++len) {
                        --len;
                     }
                  }

                  indxs = findWord(new String(chars), "->");
                  var5 = indxs.iterator();

                  while(true) {
                     if (!var5.hasNext()) {
                        break label4981;
                     }

                     i = (Integer)var5.next();
                     len = i;

                     for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != ']' && chars[len] != ',' && chars[len] != '.' && chars[len] != ':'; ++len) {
                        --len;
                     }

                     fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                  }
               }

               i = (Integer)var5.next();
               len = i;
               len = 0;
               hasSpace = false;

               while(len < chars.length && len + len < chars.length && len > 0 && chars[len] != '(') {
                  --len;
                  ++len;
                  if (chars[len] == ' ') {
                     if (hasSpace) {
                        break;
                     }

                     if (!hasSpace) {
                        hasSpace = true;
                     }
                  }
               }

               fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
            }
         }

         if (!ext.equals(".html") && !ext.equals(".htm") && !ext.equals(".md")) {
            String[] cll = new String[]{"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z"};
            tagsss = cll;
            len = cll.length;

            label4836:
            for(len = 0; len < len; ++len) {
               String s = tagsss[len];
               indxs = findWord(new String(chars), s);
               len = 0;
               String str = new String(chars);
               var12 = indxs.iterator();

               while(true) {
                  do {
                     if (!var12.hasNext()) {
                        continue label4836;
                     }

                     i = (Integer)var12.next();
                  } while(i - 1 > 0 && (str.charAt(i - 1) == 'a' || str.charAt(i - 1) == 'b' || str.charAt(i - 1) == 'c' || str.charAt(i - 1) == 'd' || str.charAt(i - 1) == 'e' || str.charAt(i - 1) == 'f' || str.charAt(i - 1) == 'g' || str.charAt(i - 1) == 'h' || str.charAt(i - 1) == 'i' || str.charAt(i - 1) == 'j' || str.charAt(i - 1) == 'k' || str.charAt(i - 1) == 'l' || str.charAt(i - 1) == 'm' || str.charAt(i - 1) == 'n' || str.charAt(i - 1) == 'o' || str.charAt(i - 1) == 'p' || str.charAt(i - 1) == 'q' || str.charAt(i - 1) == 'r' || str.charAt(i - 1) == 's' || str.charAt(i - 1) == 't' || str.charAt(i - 1) == 'u' || str.charAt(i - 1) == 'v' || str.charAt(i - 1) == 'w' || str.charAt(i - 1) == 'x' || str.charAt(i - 1) == 'y' || str.charAt(i - 1) == 'z'));

                  while(i + len < chars.length && !isCharsEqual(chars[i + len], ' ') && !isCharsEqual(chars[i + len], '[') && !isCharsEqual(chars[i + len], ']') && !isCharsEqual(chars[i + len], '(') && !isCharsEqual(chars[i + len], ')') && !isCharsEqual(chars[i + len], ',') && !isCharsEqual(chars[i + len], ';') && !isCharsEqual(chars[i + len], '.') && !isCharsEqual(chars[i + len], ':') && !isCharsEqual(chars[i + len], '=') && !isCharsEqual(chars[i + len], '"') && !isCharsEqual(chars[i + len], '\'')) {
                     ++len;
                  }

                  if (i + len < chars.length) {
                     fs = color(i, i + len, new IDEFont(Fonts.objectsNormal, FONT_SIZE), (List)fs);
                  }
               }
            }
         }

         if (ext.equals(".java")) {
            indxs = findWord(new String(chars), "@");
            len = 0;
            Iterator var70 = indxs.iterator();

            while(var70.hasNext()) {
               for(i = (Integer)var70.next(); i + len < chars.length && !isCharsEqual(chars[i + len], ' ') && !isCharsEqual(chars[i + len], '[') && !isCharsEqual(chars[i + len], ']') && !isCharsEqual(chars[i + len], '(') && !isCharsEqual(chars[i + len], ')') && !isCharsEqual(chars[i + len], ',') && !isCharsEqual(chars[i + len], ';') && !isCharsEqual(chars[i + len], '.') && !isCharsEqual(chars[i + len], ':') && !isCharsEqual(chars[i + len], '=') && !isCharsEqual(chars[i + len], '"') && !isCharsEqual(chars[i + len], '\''); ++len) {
               }

               if (i + len < chars.length) {
                  fs = color(i, i + len, new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               }
            }
         }

         if (!ext.equals(".md")) {
            indxs = findWord(new String(chars), "=");

            for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
               i = (Integer)var5.next();
               len = i;
               len = 0;
               hasSpace = false;

               while(len < chars.length && len + len < chars.length && len > 0 && chars[len] != '(' && chars[len] != ':') {
                  --len;
                  ++len;
                  if (chars[len] == ' ') {
                     if (hasSpace) {
                        break;
                     }

                     if (!hasSpace) {
                        hasSpace = true;
                     }
                  }
               }
            }

            indxs = findWord(new String(chars), "<");

            for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
               i = (Integer)var5.next();
               len = i;
               len = 0;
               hasSpace = false;

               while(len < chars.length && len + len < chars.length && len > 0 && chars[len] != '(' && chars[len] != ':') {
                  --len;
                  ++len;
                  if (chars[len] == ' ') {
                     if (hasSpace) {
                        break;
                     }

                     if (!hasSpace) {
                        hasSpace = true;
                     }
                  }
               }
            }

            indxs = findWord(new String(chars), ">");

            for(var5 = indxs.iterator(); var5.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
               i = (Integer)var5.next();
               len = i;
               len = 0;
               hasSpace = false;

               while(len < chars.length && len + len < chars.length && len > 0 && chars[len] != '(' && chars[len] != ':') {
                  --len;
                  ++len;
                  if (chars[len] == ' ') {
                     if (hasSpace) {
                        break;
                     }

                     if (!hasSpace) {
                        hasSpace = true;
                     }
                  }
               }
            }
         }
      }

      int var15;
      String[] javaKeys;
      String[] props;
      Iterator var61;
      Iterator var63;
      int c;
      String s1;
      Iterator var69;
      Iterator var73;
      String[] csKeys;
      String[] dkKeys;
      String extn;
      Iterator var106;
      String[] pyKeys;
      label5450: {
         label5451: {
            label5452: {
               String[] cppKeys;
               int var107;
               int var109;
               String[] jsKeys;
               label5453: {
                  label5454: {
                     String[] hasKeys;
                     int var152;
                     int var162;
                     String[] rsKeys;
                     label5455: {
                        String[] pasKeys;
                        int var89;
                        String[] rKeys;
                        label5456: {
                           int var38;
                           String[] jsonKeys;
                           int var190;
                           String[] goKeys;
                           label5457: {
                              int var14;
                              String[] cKeys;
                              label5458: {
                                 int var26;
                                 String[] asmRegs;
                                 String[] asmKeys;
                                 Iterator var143;
                                 int var149;
                                 String[] fsKeys;
                                 label5459: {
                                    String[] tagss;
                                    int var119;
                                    Iterator var129;
                                    label5460: {
                                       String[] batCom;
                                       int var21;
                                       String[] sqlKeys;
                                       label5461: {
                                          Iterator var210;
                                          label5288: {
                                             String[] units;
                                             String[] dartKeys;
                                             int var71;
                                             Iterator var83;
                                             label5462: {
                                                label5463: {
                                                   label4539: {
                                                      label5464: {
                                                         label4533: {
                                                            label4532: {
                                                               label4531: {
                                                                  String[] luaKeys;
                                                                  int var25;
                                                                  String[] plKeys;
                                                                  String[] cfKeys;
                                                                  String[] swKeys;
                                                                  int var33;
                                                                  int var34;
                                                                  String[] tsKeys;
                                                                  String[] scaKeys;
                                                                  int var41;
                                                                  String var55;
                                                                  int var114;
                                                                  String[] jlKeys;
                                                                  int var150;
                                                                  int var160;
                                                                  Iterator var165;
                                                                  String[] shKeys;
                                                                  String[] phpKeys;
                                                                  int var177;
                                                                  int var182;
                                                                  int var185;
                                                                  String[] ktKeys;
                                                                  String[] rbKeys;
                                                                  int var200;
                                                                  int var203;
                                                                  String[] objKeys;
                                                                  int var211;
                                                                  switch((var55 = ext.toLowerCase()).hashCode()) {
                                                                  case -1268345773:
                                                                     if (var55.equals(".license") && !foundExt) {
                                                                        extType = "Arquivo de Licença";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case -646628426:
                                                                     if (var55.equals(".gitignore") && !foundExt) {
                                                                        extType = "Git Ignore";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1525:
                                                                     if (var55.equals(".c")) {
                                                                        if (!foundExt) {
                                                                           extType = "C";
                                                                           foundExt = true;
                                                                        }

                                                                        cKeys = new String[]{"auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "true", "false", "null", "include", "bool", "duint", "uint16_t"};
                                                                        batCom = cKeys;
                                                                        var107 = cKeys.length;

                                                                        for(var89 = 0; var89 < var107; ++var89) {
                                                                           s1 = batCom[var89];
                                                                           indxs = findWord(new String(chars), s1);
                                                                           
                                                                           for(Iterator var122 = indxs.iterator(); var122.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var122.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1530:
                                                                     if (!var55.equals(".h")) {
                                                                        break label5450;
                                                                     }

                                                                     if (!foundExt) {
                                                                        extType = "C/C++ Header";
                                                                        foundExt = true;
                                                                     }
                                                                     break label4532;
                                                                  case 1535:
                                                                     if (var55.equals(".m")) {
                                                                        if (!foundExt) {
                                                                           extType = "Objective-C";
                                                                           foundExt = true;
                                                                        }

                                                                        objKeys = new String[]{"auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "inline", "int", "long", "register", "restrict", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "_Bool", "_Complex", "_Imaginary", "BOOL", "Class", "bycopy", "byref", "id", "IMP", "in", "inout", "nil", "NO", "NULL", "oneway", "out", "Protocol", "SEL", "self", "super", "YES", "@interface", "@end", "@implementation", "@protocol", "@class", "@public", "@protected", "@private", "@property", "@try", "@throw", "@catch", "@finally", "@synthesize", "@dynamic", "@selector", "atomic", "nonatomic", "retain"};
                                                                        String[] var217 = objKeys;
                                                                        c = objKeys.length;

                                                                        for(var211 = 0; var211 < c; ++var211) {
                                                                           s1 = var217[var211];
                                                                           indxs = findWord(new String(chars), s1);
                                                                           
                                                                           for(Iterator var221 = indxs.iterator(); var221.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var221.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1540:
                                                                     if (var55.equals(".r")) {
                                                                        if (!foundExt) {
                                                                           extType = "R";
                                                                           foundExt = true;
                                                                        }

                                                                        rKeys = new String[]{"if", "else", "repeat", "while", "function", "for", "in", "next", "break", "TRUE", "FALSE", "NULL", "Inf", "NaN", "NA", "NA_integer", "NA_real", "NA_complex", "NA_character"};
                                                                        luaKeys = rKeys;
                                                                        var21 = rKeys.length;

                                                                        for(var114 = 0; var114 < var21; ++var114) {
                                                                           s1 = luaKeys[var114];
                                                                           indxs = findWord(new String(chars), s1);
                                                                           
                                                                           for(Iterator var139 = indxs.iterator(); var139.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var139.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1541:
                                                                     if (!var55.equals(".s")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5459;
                                                                  case 46033:
                                                                     if (!var55.equals(".7z")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5452;
                                                                  case 47318:
                                                                     if (var55.equals(".ai") && !foundExt) {
                                                                        extType = "Arquivo do Illustrator";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 47390:
                                                                     if (var55.equals(".cs")) {
                                                                        if (!foundExt) {
                                                                           extType = "C#";
                                                                           foundExt = true;
                                                                        }

                                                                        csKeys = new String[]{"abstract", "async", "const", "event", "extern", "new", "override", "partial", "readonly", "sealed", "static", "unsafe", "virtual", "volatile", "public", "private", "internal", "protected", "if", "else", "switch", "case", "do", "for", "foreach", "in", "while", "break", "continue", "default", "goto", "return", "yield", "throw", "try", "catch", "finally", "checked", "unchecked", "fixed", "lock", "params", "ref", "out", "using", "alias", "await", "sizeof", "typeof", "stackalloc", "is", "base", "this", "null", "false", "true", "value", "void", "bool", "byte", "char", "class", "decimal", "double", "enum", "float", "int", "long", "sbyte", "short", "string", "struct", "uint", "ulong", "ushort", "add", "var", "dynamic", "global", "set", "namespace", "object", "as", "get"};
                                                                        tagss = csKeys;
                                                                        var114 = csKeys.length;

                                                                        for(var109 = 0; var109 < var114; ++var109) {
                                                                           s1 = tagss[var109];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var129 = indxs.iterator(); var129.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var129.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47483:
                                                                     if (var55.equals(".fs")) {
                                                                        if (!foundExt) {
                                                                           extType = "F#";
                                                                           foundExt = true;
                                                                        }

                                                                        fsKeys = new String[]{"abstract", "and", "as", "assert", "base", "begin", "class", "default", "delegate", "do", "done", "downcast", "downto", "elif", "else", "end", "exception", "extern", "false", "finally", "fixed", "for", "fun", "function", "global", "if", "in", "inherit", "inline", "interface", "internal", "lazy", "let", "match", "member", "module", "mutable", "namespace", "new", "not", "null", "of", "open", "or", "override", "private", "public", "rec", "return", "select", "static", "struct", "then", "to", "true", "try", "type", "upcast", "use", "val", "void", "when", "while", "with", "yield", "const", "asr", "land", "lor", "lsl", "lsr", "lxor", "mod", "sig", "atomic", "break", "checked", "component", "const", "constraint", "constructor", "continue", "eager", "event", "external", "functor", "include", "method", "mixin", "object", "parallel", "process", "protected", "pure", "sealed", "tailcall", "trait", "virtual", "volatile"};
                                                                        shKeys = fsKeys;
                                                                        var160 = fsKeys.length;

                                                                        for(var162 = 0; var162 < var160; ++var162) {
                                                                           String s = shKeys[var162];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var183 = indxs.iterator(); var183.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var183.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47510:
                                                                     if (var55.equals(".go")) {
                                                                        if (!foundExt) {
                                                                           extType = "Go";
                                                                           foundExt = true;
                                                                        }

                                                                        goKeys = new String[]{"break", "default", "func", "interface", "select", "case", "defer", "go", "map", "struct", "chan", "else", "goto", "package", "switch", "const", "fallthrough", "if", "range", "type", "continue", "for", "import", "return", "var"};
                                                                        String[] var218 = goKeys;
                                                                        var211 = goKeys.length;

                                                                        for(var203 = 0; var203 < var211; ++var203) {
                                                                           String s = var218[var203];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var219 = indxs.iterator(); var219.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var219.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47545:
                                                                     if (!var55.equals(".hs")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5455;
                                                                  case 47600:
                                                                     if (var55.equals(".jl")) {
                                                                        extType = "Julia";
                                                                        foundExt = true;
                                                                        jlKeys = new String[]{"baremodule", "begin", "break", "catch", "const", "continue", "do", "else", "elseif", "end", "export", "false", "finally", "for", "function", "global", "if", "import", "let", "local", "macro", "module", "quote", "return", "struct", "true", "try", "using", "while"};
                                                                        cfKeys = jlKeys;
                                                                        var150 = jlKeys.length;

                                                                        for(var149 = 0; var149 < var150; ++var149) {
                                                                           String s = cfKeys[var149];
                                                                           indxs = findWord(new String(chars), s);

                                                                           for(var165 = indxs.iterator(); var165.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var165.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47607:
                                                                     if (!var55.equals(".js")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5460;
                                                                  case 47639:
                                                                     if (var55.equals(".kt")) {
                                                                        if (!foundExt) {
                                                                           extType = "Kotlin";
                                                                           foundExt = true;
                                                                        }

                                                                        ktKeys = new String[]{"as", "as?", "break", "class", "continue", "do", "else", "false", "for", "fun", "if", "in", "!in", "interface", "is", "!is", "null", "object", "package", "return", "super", "this", "throw", "true", "try", "typealias", "typeof", "val", "var", "when", "while", "by", "catch", "constructor", "delegate", "dynamic", "field", "file", "finally", "get", "import", "init", "param", "property", "receiver", "set", "setparam", "value", "class", "where", "actual", "abstract", "annotation", "companion", "const", "crossinline", "data", "enum", "expect", "external", "final", "infix", "inline", "inner", "internal", "lateinit", "noinline", "open", "operator", "out", "override", "private", "protected", "public", "reified", "sealed", "suspend", "tailrec", "vararg", "field", "it"};
                                                                        objKeys = ktKeys;
                                                                        var200 = ktKeys.length;

                                                                        for(var190 = 0; var190 < var200; ++var190) {
                                                                           String s = objKeys[var190];
                                                                           indxs = findWord(new String(chars), s);

                                                                           for(var210 = indxs.iterator(); var210.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var210.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47654:
                                                                     if (var55.equals(".ld")) {
                                                                        if (!foundExt) {
                                                                           extType = "LinkerScript";
                                                                           foundExt = true;
                                                                        }

                                                                        dkKeys = new String[]{"ENTRY", "OUTPUT_FORMAT", "STARTUP", "SEARCH_DIR", "INPUT", "OUTPUT", "MEMORY", "SECTIONS", "KEEP"};
                                                                        csKeys = dkKeys;
                                                                        c = dkKeys.length;

                                                                        for(var15 = 0; var15 < c; ++var15) {
                                                                           s1 = csKeys[var15];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var106 = indxs.iterator(); var106.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var106.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47685:
                                                                     if (var55.equals(".md")) {
                                                                        if (!foundExt) {
                                                                           extType = "Markdown";
                                                                           foundExt = true;
                                                                        }

                                                                        indxs = findWord(new String(chars), "#");

                                                                        for(var165 = indxs.iterator(); var165.hasNext(); fs = color(i, ((List)fs).size(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                           i = (Integer)var165.next();
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47692:
                                                                     if (!var55.equals(".mk")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5288;
                                                                  case 47786:
                                                                     if (var55.equals(".pl")) {
                                                                        if (!foundExt) {
                                                                           extType = "Perl";
                                                                           foundExt = true;
                                                                        }

                                                                        plKeys = new String[]{"-A", "END", "length", "setpgrp", "-B", "endgrent", "link", "setpriority", "-b", "endhostnet", "listen", "setprotoent", "-C", "endnetent", "local", "setpwent", "-c", "endprotoent", "localtime", "setservent", "-d", "endpwent", "log", "setsockopt", "-e", "endservent", "lstat", "shift", "-f", "eof", "map", "shmctl", "-g", "eval", "mkdir", "shmget", "-k", "exec", "msgctl", "shmread", "-l", "exists", "msgget", "shmwrite", "-M", "exit", "msgrcv", "shutdown", "-O", "fcntl", "msgsnd", "sin", "-o", "fileno", "my", "sleep", "-p", "flock", "next", "socket", "-r", "fork", "not", "socketpair", "-R", "format", "oct", "sort", "-S", "formline", "open", "splice", "-s", "getc", "opendir", "split", "-T", "getgrent", "ord", "sprintf", "-t", "getgrgid", "our", "sqrt", "-u", "getgrnam", "pack", "srand", "-w", "gethostbyaddr", "pipe", "stat", "-W", "gethostbyname", "pop", "state", "-X", "gethostent", "pos", "study", "-x", "getlogin", "print", "substr", "-z", "getnetbyaddr", "printf", "symlink", "abs", "getnetbyname", "prototype", "syscall", "accept", "getnetent", "push", "sysopen", "alarm", "getpeername", "quotemeta", "sysread", "atan2", "getpgrp", "rand", "sysseek", "AUTOLOAD", "getppid", "read", "system", "BEGIN", "getpriority", "readdir", "syswrite", "bind", "getprotobyname", "readline", "tell", "binmode", "getprotobynumber", "readlink", "telldir", "bless", "getprotoent", "readpipe", "tie", "break", "getpwent", "recv", "tied", "caller", "getpwnam", "redo", "time", "chdir", "getpwuid", "ref", "times", "CHECK", "getservbyname", "rename", "truncate", "chmod", "getservbyport", "rename", "umask", "chown", "getsockopt", "reverse", "undef", "chr", "glob", "rewinddir", "UNITCHECK", "chroot", "gmtime", "rindex", "unlink", "close", "goto", "rmdir", "unpack", "closedir", "grep", "say", "unshift", "connect", "hex", "scalar", "untie", "cos", "index", "seek", "use", "crypt", "INIT", "seekdir", "utime", "dbmclose", "int", "select", "values", "dbmopen", "ioctl", "semctl", "vec", "defined", "join", "semget", "wait", "delete", "keys", "semop", "waitpid", "DESTROY", "kill", "send", "wantarray", "die", "last", "setgrent", "warn", "dump", "lc", "sethostent", "write", "each", "lcfirst", "setnetent", "__DATA__", "else", "lock", "qw", "__END__", "elsif", "lt", "qx", "__FILE__", "eq", "m", "s", "__LINE__", "exp", "ne", "sub", "__PACKAGE__", "for", "no", "tr", "and", "foreach", "or", "unless", "cmp", "ge", "package", "until", "continue", "gt", "q", "while", "CORE", "if", "qq", "xor", "do", "le", "qr", "y"};
                                                                        swKeys = plKeys;
                                                                        var152 = plKeys.length;

                                                                        for(var150 = 0; var150 < var152; ++var150) {
                                                                           String s = swKeys[var150];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var170 = indxs.iterator(); var170.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var170.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47790:
                                                                     if (!var55.equals(".pp")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5456;
                                                                  case 47799:
                                                                     if (!var55.equals(".py")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5458;
                                                                  case 47838:
                                                                     if (var55.equals(".rb")) {
                                                                        if (!foundExt) {
                                                                           extType = "Ruby";
                                                                           foundExt = true;
                                                                        }

                                                                        rbKeys = new String[]{"_ENCODING_", "_LINE_", "_FILE_", "BEGIN", "END", "alias", "and", "begin", "break", "case", "class", "def", "defined?", "do", "else", "elsif", "end", "ensure", "false", "for", "if", "in", "module", "next", "nil", "not", "or", "redo", "rescue", "retry", "return", "self", "super", "then", "true", "undef", "unless", "until", "when", "while", "yield"};
                                                                        dkKeys = rbKeys;
                                                                        var41 = rbKeys.length;

                                                                        for(var200 = 0; var200 < var41; ++var200) {
                                                                           String s = dkKeys[var200];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var214 = indxs.iterator(); var214.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var214.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47855:
                                                                     if (var55.equals(".rs")) {
                                                                        if (!foundExt) {
                                                                           extType = "Rust";
                                                                           foundExt = true;
                                                                        }

                                                                        rsKeys = new String[]{"as", "break", "const", "continue", "crate", "else", "enum", "extern", "false", "fn", "for", "if", "impl", "in", "let", "loop", "match", "mod", "move", "mut", "pub", "ref", "return", "self", "Self", "static", "struct", "super", "trait", "true", "type", "unsafe", "use", "where", "while", "async", "await", "dyn", "abstract", "become", "box", "do", "final", "macro", "override", "priv", "typeof", "unsized", "virtual", "yield", "try", "union", "'static", "dyn"};
                                                                        jsonKeys = rsKeys;
                                                                        var177 = rsKeys.length;

                                                                        for(var34 = 0; var34 < var177; ++var34) {
                                                                           String s = jsonKeys[var34];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var192 = indxs.iterator(); var192.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var192.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47875:
                                                                     if (var55.equals(".sh")) {
                                                                        if (!foundExt) {
                                                                           extType = "Bash";
                                                                           foundExt = true;
                                                                        }

                                                                        shKeys = new String[]{"pwd", "cd", "ls", "cat", "cp", "mv", "mkdir", "rmdir", "rm", "touch", "locate", "find", "grep", "sudo", "df", "du", "head", "tail", "diff", "tar", "chmod", "chown", "jobs", "kill", "ping", "wget", "uname", "top", "history", "man", "echo", "zip", "unzip", "hostname", "useradd", "userdel", "clear"};
                                                                        ktKeys = shKeys;
                                                                        var182 = shKeys.length;

                                                                        for(var177 = 0; var177 < var182; ++var177) {
                                                                           String s = ktKeys[var177];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var196 = indxs.iterator(); var196.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var196.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 47917:
                                                                     if (var55.equals(".ts")) {
                                                                        if (!foundExt) {
                                                                           extType = "TypeScript";
                                                                           foundExt = true;
                                                                        }

                                                                        tsKeys = new String[]{"break", "as", "any", "switch", "case", "if", "throw", "else", "var", "number", "string", "get", "module", "type", "instanceof", "typeof", "public", "private", "enum", "export", "finally", "for", "while", "void", "null", "super", "this", "new", "in", "return", "true", "false", "extends", "static", "let", "package", "implements", "interface", "function", "new", "try", "yield", "const", "continue", "do", "catch"};
                                                                        scaKeys = tsKeys;
                                                                        var38 = tsKeys.length;

                                                                        for(var185 = 0; var185 < var38; ++var185) {
                                                                           String s = scaKeys[var185];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var202 = indxs.iterator(); var202.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var202.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1466834:
                                                                     if (var55.equals(".aed") && !foundExt) {
                                                                        extType = "Arquivo do After Effects";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1467277:
                                                                     if (!var55.equals(".asm")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5459;
                                                                  case 1467687:
                                                                     if (!var55.equals(".bat")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5461;
                                                                  case 1467929:
                                                                     if (var55.equals(".bin") && !foundExt) {
                                                                        extType = "Arquivo Binário";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1468790:
                                                                     if (!var55.equals(".cfg")) {
                                                                        break label5450;
                                                                     }
                                                                     break;
                                                                  case 1469075:
                                                                     if (!var55.equals(".com")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5461;
                                                                  case 1469109:
                                                                     if (!var55.equals(".cpp")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5453;
                                                                  case 1469205:
                                                                     if (var55.equals(".css")) {
                                                                        if (!foundExt) {
                                                                           extType = "Cascading Style Sheets - CSS";
                                                                           foundExt = true;
                                                                        }

                                                                        tagsss = new String[]{"a", "abbr", "acronym", "address", "applet", "area", "article", "aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button", "canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del", "details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure", "font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main", "map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "important", "from", "to"};
                                                                        props = new String[]{"align-content", "align-items", "all", "animation", "animation-direction", "animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name", "animation-play-state", "animation-timing-function", "backface-visibility", "background", "background-attachment", "background-blend-mode", "background-clip", "background-color", "background-image", "background-origin", "background-position", "background-repeat", "background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius", "border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse", "border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice", "border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color", "border-right-style", "border-right-width", "border-spacing", "border-style", "border-top", "border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style", "border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing", "break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear", "clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color", "column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content", "counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter", "flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap", "float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values", "font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch", "font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps", "font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position", "font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows", "grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template", "grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation", "height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes", "left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image", "list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left", "margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media", "min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity", "order", "orphans", "outline", "outline-color", "outline-offset", "outline-style", "outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding", "padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after", "page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events", "position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout", "text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color", "text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation", "text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform", "transform-origin", "transform-style", "transition", "transition-delay", "transition-duration", "transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align", "visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap", "writing-mode", "z-index", "screen", "and"};
                                                                        units = new String[]{"px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax"};
                                                                        pasKeys = tagsss;
                                                                        c = tagsss.length;

                                                                        for(var71 = 0; var71 < c; ++var71) {
                                                                           s1 = pasKeys[var71];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var83.next();
                                                                           }
                                                                        }

                                                                        pasKeys = props;
                                                                        c = props.length;

                                                                        for(var71 = 0; var71 < c; ++var71) {
                                                                           s1 = pasKeys[var71];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var83.next();
                                                                           }
                                                                        }

                                                                        pasKeys = units;
                                                                        c = units.length;

                                                                        for(var71 = 0; var71 < c; ++var71) {
                                                                           s1 = pasKeys[var71];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var83.next();
                                                                           }
                                                                        }

                                                                        indxs = findWord(new String(chars), ".");
                                                                        len = 0;
                                                                        var12 = indxs.iterator();

                                                                        while(var12.hasNext()) {
                                                                           for(i = (Integer)var12.next(); i + len < chars.length && chars[i + len] != ' '; ++len) {
                                                                           }

                                                                           if (i + len < chars.length) {
                                                                              fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                                                           }
                                                                        }

                                                                        indxs = findWord(new String(chars), "#");
                                                                        len = 0;
                                                                        var12 = indxs.iterator();

                                                                        while(var12.hasNext()) {
                                                                           for(i = (Integer)var12.next(); i + len < chars.length && chars[i + len] != ' '; ++len) {
                                                                           }

                                                                           if (i + len < chars.length) {
                                                                              fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                                                           }
                                                                        }

                                                                        indxs = findWord(new String(chars), ";");

                                                                        for(var12 = indxs.iterator(); var12.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                           i = (Integer)var12.next();
                                                                           c = i;

                                                                           for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']' && chars[c] != '.' && chars[c] != '#' && chars[c] != ':'; ++len) {
                                                                              --c;
                                                                           }
                                                                        }

                                                                        indxs = findWord(new String(chars), "]");

                                                                        for(var12 = indxs.iterator(); var12.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                                           i = (Integer)var12.next();
                                                                           c = i;

                                                                           for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                                              --c;
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1469365:
                                                                     if (!var55.equals(".cxx")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5453;
                                                                  case 1470848:
                                                                     if (!var55.equals(".ejs")) {
                                                                        break label5450;
                                                                     }

                                                                     if (!foundExt) {
                                                                        extType = "Embedded JavaScript - EJS";
                                                                        foundExt = true;
                                                                     }
                                                                     break label4531;
                                                                  case 1471268:
                                                                     if (var55.equals(".exe")) {
                                                                        extType = "Executável do Windows - EXE";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1473452:
                                                                     if (!var55.equals(".has")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5455;
                                                                  case 1473914:
                                                                     if (!var55.equals(".hpp")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5463;
                                                                  case 1474035:
                                                                     if (!var55.equals(".htm")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5462;
                                                                  case 1474170:
                                                                     if (!var55.equals(".hxx")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5463;
                                                                  case 1474812:
                                                                     if (!var55.equals(".ino")) {
                                                                        break label5450;
                                                                     }
                                                                     break label4532;
                                                                  case 1475373:
                                                                     if (var55.equals(".jar") && !foundExt) {
                                                                        extType = "Arquivo Jar";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1475937:
                                                                     if (!var55.equals(".jsx")) {
                                                                        break label5450;
                                                                     }
                                                                     break label4531;
                                                                  case 1477718:
                                                                     if (!var55.equals(".log")) {
                                                                        break label5450;
                                                                     }

                                                                     if (!foundExt) {
                                                                        extType = "Arquivo de Log";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5454;
                                                                  case 1477898:
                                                                     if (var55.equals(".lua")) {
                                                                        if (!foundExt) {
                                                                           extType = "Lua";
                                                                           foundExt = true;
                                                                        }

                                                                        luaKeys = new String[]{"and", "break", "do", "else", "elseif", "end", "false", "for", "function", "if", "in", "local", "nil", "not", "or", "repeat", "return", "then", "true", "until", "while"};
                                                                        jlKeys = luaKeys;
                                                                        var25 = luaKeys.length;

                                                                        for(c = 0; c < var25; ++c) {
                                                                           String s = jlKeys[c];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var145 = indxs.iterator(); var145.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var145.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1480568:
                                                                     if (var55.equals(".one") && !foundExt) {
                                                                        extType = "Arquivo do Microsoft OneNote";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1480755:
                                                                     if (!var55.equals(".otf")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5451;
                                                                  case 1481140:
                                                                     if (!var55.equals(".pas")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5456;
                                                                  case 1481220:
                                                                     if (var55.equals(".pdf") && !foundExt) {
                                                                        extType = "Portable Document Format - PDF";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1481354:
                                                                     if (var55.equals(".php")) {
                                                                        if (!foundExt) {
                                                                           extType = "Hypertext Preprocessor - PHP";
                                                                           foundExt = true;
                                                                        }

                                                                        phpKeys = new String[]{"abstract", "and", "as", "break", "callable", "case", "catch", "class", "clone", "const", "continue", "declare", "default", "do", "echo", "else", "elseif", "enddeclare", "endfor", "endforeach", "endif", "endswitch", "endwhile", "extends", "final", "finally", "fn", "for", "foreach", "function", "global", "goto", "if", "implements", "include", "include_once", "instanceof", "insteadof", "interface", "match", "namespace", "new", "or", "print", "private", "protected", "public", "require", "require_once", "return", "static", "switch", "throw", "trait", "try", "use", "var", "while", "yield", "yield from", "__CLASS__", "__DIR__", "__FILE__", "__FUNCTION__", "__LINE__", "__METHOD__", "__NAMESPACE__", "__TRAIT__"};
                                                                        rbKeys = phpKeys;
                                                                        var185 = phpKeys.length;

                                                                        for(var182 = 0; var182 < var185; ++var182) {
                                                                           String s = rbKeys[var182];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var198 = indxs.iterator(); var198.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var198.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1481632:
                                                                     if (!var55.equals(".ps1")) {
                                                                        break label5450;
                                                                     }

                                                                     if (!foundExt) {
                                                                        extType = "Arquivo PowerShell";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5461;
                                                                  case 1481683:
                                                                     if (var55.equals(".psd") && !foundExt) {
                                                                        extType = "Arquivo do Photoshop";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1481869:
                                                                     if (!var55.equals(".pyd")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5458;
                                                                  case 1483061:
                                                                     if (!var55.equals(".rar")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5452;
                                                                  case 1484512:
                                                                     if (var55.equals(".sql")) {
                                                                        if (!foundExt) {
                                                                           extType = "Structured Query Language - SQL";
                                                                           foundExt = true;
                                                                        }

                                                                        sqlKeys = new String[]{"ADD", "ADD CONSTRAINT", "ALTER", "ALTER COLUMN", "ALTER TABLE", "ALL", "AND", "ANY", "AS", "ASC", "BACKUP DATABASE", "BETWEEN", "CASE", "CHECK", "COLUMN", "CONSTRAINT", "CREATE", "CREATE DATABASE", "CREATE INDEX", "CREATE OR REPLACE VIEW", "CREATE TABLE", "CREATE PROCEDURE", "CREATE UNIQUE INDEX", "CREATE VIEW", "DATABASE", "DEFAULT", "DELETE", "DESC", "DISTINCT", "DROP", "DROP COLUMN", "DROP CONSTRAINT", "DROP DATABASE", "DROP DEFAULT", "DROP INDEX", "DROP TABLE", "DROP VIEW", "EXEC", "EXISTS", "FOREIGN KEY", "FROM", "FULL OUTER JOIN", "GROUP BY", "HAVING", "IN", "INDEX", "INNER JOIN", "INSERT INTO", "INSERT INTO SELECT", "IS NULL", "IS NOT NULL", "JOIN", "LEFT JOIN", "LIKE", "LIMIT", "NOT", "NOT NULL", "OR", "ORDER BY", "OUTER JOIN", "PRIMARY KEY", "PROCEDURE", "RIGHT JOIN", "ROWNUM", "SELECT", "SELECT DISTINCT", "SELECT INTO", "SELECT TOP", "SET", "TABLE", "TOP", "TRUNCATE TABLE", "UNION", "UNION ALL", "UNIQUE", "UPDATE", "VALUES", "VIEW", "WHERE", "add", "add constraint", "alter", "alter column", "alter table", "all", "and", "any", "as", "asc", "backup database", "between", "case", "check", "column", "constraint", "create", "create database", "create index", "create or replace view", "create table", "create procedure", "create unique index", "create view", "database", "default", "delete", "desc", "distinct", "drop", "drop column", "drop constraint", "drop database", "drop default", "drop index", "drop table", "drop view", "exec", "exists", "foreign key", "from", "full outer join", "group by", "having", "in", "index", "inner join", "insert into", "insert into select", "is null", "is not null", "join", "left join", "like", "limit", "not", "not null", "or", "order by", "outer join", "primary key", "procedure", "right join", "rownum", "select", "select distinct", "select into", "select top", "set", "table", "top", "truncate table", "union", "union all", "unique", "update", "values", "view", "where"};
                                                                        plKeys = sqlKeys;
                                                                        var26 = sqlKeys.length;

                                                                        for(var25 = 0; var25 < var26; ++var25) {
                                                                           String s = plKeys[var25];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var29 = indxs.iterator(); var29.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var29.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1484662:
                                                                     if (!var55.equals(".svg")) {
                                                                        break label5450;
                                                                     }
                                                                     break label4539;
                                                                  case 1485560:
                                                                     if (!var55.equals(".ttf")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5451;
                                                                  case 1485698:
                                                                     if (!var55.equals(".txt")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5454;
                                                                  case 1487512:
                                                                     if (!var55.equals(".vue")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5464;
                                                                  case 1489193:
                                                                     if (!var55.equals(".xml")) {
                                                                        break label5450;
                                                                     }
                                                                     break label4533;
                                                                  case 1490995:
                                                                     if (!var55.equals(".zip")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5452;
                                                                  case 45541458:
                                                                     if (var55.equals(".conf") && !foundExt) {
                                                                        extType = "Arquivo de Configurações da Boot IDE";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 45557933:
                                                                     if (var55.equals(".dart")) {
                                                                        if (!foundExt) {
                                                                           extType = "Dart";
                                                                           foundExt = true;
                                                                        }

                                                                        dartKeys = new String[]{"abstract", "else", "import", "super", "as", "enum", "in", "switch", "assert", "export", "interface", "sync", "async", "extends", "is", "this", "await", "extension", "library", "throw", "break", "external", "mixin", "true", "case", "factory", "new", "try", "class", "final", "catch", "false", "null", "typedef", "on", "var", "const", "finally", "operator", "void", "continue", "for", "part", "while", "covariant", "Function", "rethrow", "with", "default", "get", "return", "yield", "deferred", "hide", "set", "do", "if", "show", "dynamic", "implements", "static"};
                                                                        cppKeys = dartKeys;
                                                                        var15 = dartKeys.length;

                                                                        for(var14 = 0; var14 < var15; ++var14) {
                                                                           String s = cppKeys[var14];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var18 = indxs.iterator(); var18.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var18.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 45570926:
                                                                     if (var55.equals(".docx") && !foundExt) {
                                                                        extType = "Documento do Microsoft Word";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 45695193:
                                                                     if (!var55.equals(".html")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5462;
                                                                  case 45718931:
                                                                     if (var55.equals(".indd") && !foundExt) {
                                                                        extType = "Arquivo do InDesign";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 45736784:
                                                                     if (var55.equals(".java")) {
                                                                        if (!foundExt) {
                                                                           extType = "Java";
                                                                           foundExt = true;
                                                                        }

                                                                        javaKeys = new String[]{"abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "true", "false", "null", "@interface"};
                                                                        props = javaKeys;
                                                                        i = javaKeys.length;

                                                                        for(len = 0; len < i; ++len) {
                                                                           s1 = props[len];
                                                                           indxs = findWord(new String(chars), s1);

                                                                           for(var63 = indxs.iterator(); var63.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var63.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 45753878:
                                                                     if (!var55.equals(".json")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5457;
                                                                  case 45809241:
                                                                     if (var55.equals(".lock") && !foundExt) {
                                                                        extType = "Lock";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 45825820:
                                                                     if (!var55.equals(".make")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5288;
                                                                  case 45929906:
                                                                     if (var55.equals(".pptx") && !foundExt) {
                                                                        extType = "ApresentaÃ§Ã£o do Microsoft PowerPoint";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 46004907:
                                                                     if (var55.equals(".save") && !foundExt) {
                                                                        extType = "Jogo salvo do World's Hardest Game Maker 2";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 46080574:
                                                                     if (var55.equals(".urna") && !foundExt) {
                                                                        extType = "Urna salva do Criador de Urnas";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 46137030:
                                                                     if (!var55.equals(".woff")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5451;
                                                                  case 46164359:
                                                                     if (var55.equals(".xlsx") && !foundExt) {
                                                                        extType = "Planilha do Microsoft Excel";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 815433082:
                                                                     if (var55.equals(".coffee")) {
                                                                        if (!foundExt) {
                                                                           extType = "CoffeeScript";
                                                                           foundExt = true;
                                                                        }

                                                                        cfKeys = new String[]{"for", "while", "loop", "by", "in", "of", "break", "continue", "if", "then", "else", "unless", "switch", "when", "default", "return", "do", "is", "isnt", "and", "or", "not", "true", "yes", "on", "false", "no", "off", "throw", "try", "catch", "finally", "new", "delete", "class", "extends", "super", "typeof", "instanceof", "this", "arguments", "await", "defer", "yield", "null", "undefined", "Infinity", "NaN", "export", "import", "package", "let", "case", "debugger", "function", "var", "with", "private", "protected", "public", "native", "static", "const", "implements", "interface", "void", "enum"};
                                                                        phpKeys = cfKeys;
                                                                        var33 = cfKeys.length;

                                                                        for(var160 = 0; var160 < var33; ++var160) {
                                                                           String s = phpKeys[var160];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var178 = indxs.iterator(); var178.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var178.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 815671536:
                                                                     if (!var55.equals(".config")) {
                                                                        break label5450;
                                                                     }
                                                                     break;
                                                                  case 1411683850:
                                                                     if (var55.equals(".class") && !foundExt) {
                                                                        extType = "Arquivo do Java Compilado";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5450;
                                                                  case 1418370317:
                                                                     if (!var55.equals(".jsonc")) {
                                                                        break label5450;
                                                                     }

                                                                     if (!foundExt) {
                                                                        extType = "JavaScript Object Notation with Comments - JSONC";
                                                                        foundExt = true;
                                                                     }
                                                                     break label5457;
                                                                  case 1426191832:
                                                                     if (var55.equals(".scala")) {
                                                                        if (!foundExt) {
                                                                           extType = "Scala";
                                                                           foundExt = true;
                                                                        }

                                                                        scaKeys = new String[]{"abstract", "finally", "object", "trait", "catch", "forSome", "package", "try", "class", "if", "private", "type", "def", "implicit", "protected", "val", "else", "lazy", "sealed", "while", "false", "new", "this", "yield", "final", "null", "throw"};
                                                                        String[] var207 = scaKeys;
                                                                        var203 = scaKeys.length;

                                                                        for(var41 = 0; var41 < var203; ++var41) {
                                                                           String s = var207[var41];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var215 = indxs.iterator(); var215.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var215.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1426795173:
                                                                     if (var55.equals(".swift")) {
                                                                        if (!foundExt) {
                                                                           extType = "Swift";
                                                                           foundExt = true;
                                                                        }

                                                                        swKeys = new String[]{"associatedtype", "class", "deinit", "enum", "extension", "fileprivate", "func", "import", "init", "inout", "internal", "let", "open", "operator", "private", "protocol", "public", "rethrows", "static", "struct", "subscript", "typealias", "var", "break", "case", "continue", "default", "defer", "do", "else", "fallthrough", "for", "guard", "if", "in", "repeat", "return", "switch", "where", "while", "as", "Any", "catch", "false", "is", "nil", "super", "self", "self", "throw", "throws", "true", "try", "_", "#available", "#colorLiteral", "#column", "#else", "#elseif", "#endif", "#error", "#file", "#fileID", "#fileLiteral", "#filePath", "#function", "#if", "#imageLiteral", "#line", "#selector", "#sourceLocation", "#warning", "associativity", "convenience", "dynamic", "didset", "final", "get", "infix", "indirect", "lazy", "left", "mutating", "none", "nonmutating", "optional", "override", "postfix", "precendence", "prefix", "Protocol", "required", "right", "set", "Type", "unowned", "weak", "willSet"};
                                                                        tsKeys = swKeys;
                                                                        var34 = swKeys.length;

                                                                        for(var33 = 0; var33 < var34; ++var33) {
                                                                           String s = tsKeys[var33];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var37 = indxs.iterator(); var37.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var37.next();
                                                                           }
                                                                        }
                                                                     }
                                                                     break label5450;
                                                                  case 1430247980:
                                                                     if (!var55.equals(".woff2")) {
                                                                        break label5450;
                                                                     }
                                                                     break label5451;
                                                                  case 1922265674:
                                                                     if (var55.equals(".dockerfile")) {
                                                                        if (!foundExt) {
                                                                           extType = "Dockerfile";
                                                                           foundExt = true;
                                                                        }

                                                                        dkKeys = new String[]{"FROM", "RUN", "VOLUME", "WORKDIR", "from", "run", "volume", "workdir"};
                                                                        String[] var46 = dkKeys;
                                                                        int var45 = dkKeys.length;

                                                                        for(c = 0; c < var45; ++c) {
                                                                           String s = var46[c];
                                                                           indxs = findWord(new String(chars), s);
                                                                           
                                                                           for(Iterator var48 = indxs.iterator(); var48.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                                              i = (Integer)var48.next();
                                                                           }
                                                                        }
                                                                     }
                                                                  default:
                                                                     break label5450;
                                                                  }

                                                                  if (!foundExt) {
                                                                     extType = "Arquivo de Configurações";
                                                                     foundExt = true;
                                                                  }
                                                                  break label4533;
                                                               }

                                                               if (!foundExt) {
                                                                  extType = "React";
                                                                  foundExt = true;
                                                               }
                                                               break label5464;
                                                            }

                                                            if (!foundExt) {
                                                               extType = "Arduino";
                                                               foundExt = true;
                                                            }
                                                            break label5463;
                                                         }

                                                         if (!foundExt) {
                                                            extType = "Extensible Markup Language - XML";
                                                            foundExt = true;
                                                         }
                                                         break label4539;
                                                      }

                                                      if (!foundExt) {
                                                         extType = "Vue.js";
                                                         foundExt = true;
                                                      }
                                                      break label5460;
                                                   }

                                                   if (!foundExt) {
                                                      extType = "Scalable Vector Graphics - SVG";
                                                      foundExt = true;
                                                   }
                                                   break label5462;
                                                }

                                                if (!foundExt) {
                                                   extType = "C++ Header";
                                                   foundExt = true;
                                                }
                                                break label5453;
                                             }

                                             if (!foundExt) {
                                                extType = "Hyper Text Markup Language - HTML";
                                                foundExt = true;
                                             }

                                             String[] tags = new String[]{"<!--", "<!doctype", "<!DOCTYPE", "<a", "<abbr", "<acronym", "<address", "<applet", "<area", "<article", "<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo", "<big", "<blockquote", "<body", "<br", "<button", "<canvas", "<caption", "<center", "<cite", "<code", "<col", "<colgroup", "<data", "<datalist", "<dd", "<del", "<details", "<dfn", "<dialog", "<dir", "<div", "<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure", "<font", "<footer", "<form", "<frame", "<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header", "<hr", "<html", "<i", "<iframe", "<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main", "<map", "<mark", "<meta", "<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option", "<output", "<p", "<param", "<picture", "<pre", "<progress", "<q", "<rp", "<rt", "<ruby", "<s", "<samp", "<script", "<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg", "<table", "<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr", "<track", "<tt", "<u", "<ul", "<var", "<video", "<wbr", "</a", "</abbr", "</acronym", "</address", "</applet", "</area", "</article", "</aside", "</audio", "</b", "</base", "</basefont", "</bdi", "</bdo", "</big", "</blockquote", "</body", "</br", "</button", "</canvas", "</caption", "</center", "</cite", "</code", "</col", "</colgroup", "</data", "</datalist", "</dd", "</del", "</details", "</dfn", "</dialog", "</dir", "</div", "</dl", "</dt", "</em", "</embed", "</fieldset", "</figcaption", "</figure", "</font", "</footer", "</form", "</frame", "</frameset", "</h1", "</h2", "</h3", "</h4", "</h5", "</h6", "</head", "</header", "</hr", "</html", "</i", "</iframe", "</img", "</input", "</ins", "</kbd", "</label", "</legend", "</li", "</link", "</main", "</map", "</mark", "</meta", "</meter", "</nav", "</noframes", "</noscript", "</object", "</ol", "</optgroup", "</option", "</output", "</p", "</param", "</picture", "</pre", "</progress", "</q", "</rp", "</rt", "</ruby", "</s", "</samp", "</script", "</section", "</select", "</small", "</source", "</span", "</strike", "</strong", "</style", "</sup", "</svg", "</table", "</tbody", "</td", "</template", "</textarea", "</tfoot", "</th", "</thead", "</time", "</title", "</tr", "</track", "</tt", "</u", "</ul", "</var", "</video", "</wbr"};
                                             units = tags;
                                             len = tags.length;

                                             for(i = 0; i < len; ++i) {
                                                extn = units[i];
                                                indxs = findWord(new String(chars), extn);

                                                for(var12 = indxs.iterator(); var12.hasNext(); fs = color(i, i + extn.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                   i = (Integer)var12.next();
                                                }
                                             }

                                             indxs = findWord(new String(chars), ">"); // TODO

                                             for(var69 = indxs.iterator(); var69.hasNext(); fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                i = (Integer)var69.next();
                                             }

                                             indxs = findWord(new String(chars), "<");
                                             len = 0;
                                             var61 = indxs.iterator();
                                             
                                             while(var61.hasNext()) {
                                                for(i = (Integer)var61.next(); i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']' && chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':' && chars[i + len] != '>'; ++len) {
                                                }

                                                if (i + len < chars.length) {
                                                   fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                                                }
                                             }

                                             indxs = findWord(new String(chars), "</");
                                             len = 0;
                                             var61 = indxs.iterator();

                                             while(var61.hasNext()) {
                                                for(i = (Integer)var61.next(); i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']' && chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':'; ++len) {
                                                }

                                                if (i + len < chars.length) {
                                                   fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                                                }
                                             }

                                             indxs = findWord(new String(chars), "=");

                                             for(var61 = indxs.iterator(); var61.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                i = (Integer)var61.next();
                                                c = i;

                                                for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                   --c;
                                                }
                                             }

                                             indxs = findWord(new String(chars), "<style");
                                             if (indxs.size() > 0) {
                                                isCssPart = true;
                                             }

                                             indxs = findWord(new String(chars), "</style");
                                             if (indxs.size() > 0) {
                                                isCssPart = false;
                                             }

                                             indxs = findWord(new String(chars), "<script");
                                             if (indxs.size() > 0) {
                                                isJSPart = true;
                                             }

                                             indxs = findWord(new String(chars), "</script");
                                             if (indxs.size() > 0) {
                                                isJSPart = false;
                                             }

                                             if (isJSPart) {
                                                tagsss = new String[]{"abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield", "undefined", "of"};
                                                dartKeys = tagsss;
                                                c = tagsss.length;
                                                c = 0;

                                                label4109:
                                                while(true) {
                                                   if (c >= c) {
                                                      indxs = findWord(new String(chars), ")");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != '('; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), "]");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), ":");

                                                      boolean hasSpace;
                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;
                                                         len = 0;
                                                         hasSpace = false;

                                                         while(c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(') {
                                                            --c;
                                                            ++len;
                                                            if (chars[c] == ' ') {
                                                               if (hasSpace) {
                                                                  break;
                                                               }

                                                               if (!hasSpace) {
                                                                  hasSpace = true;
                                                               }
                                                            }
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), ".");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), ";");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), ".");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), "[");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != ']' && chars[c] != ',' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), "->");

                                                      for(var73 = indxs.iterator(); var73.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                         i = (Integer)var73.next();
                                                         c = i;

                                                         for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != ']' && chars[c] != ',' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                            --c;
                                                         }
                                                      }

                                                      indxs = findWord(new String(chars), "=");
                                                      var73 = indxs.iterator();

                                                      while(true) {
                                                         if (!var73.hasNext()) {
                                                            break label4109;
                                                         }

                                                         i = (Integer)var73.next();
                                                         c = i;
                                                         len = 0;
                                                         hasSpace = false;

                                                         while(c < chars.length && c + len < chars.length && c > 0 && chars[c] != '(' && chars[c] != ':') {
                                                            --c;
                                                            ++len;
                                                            if (chars[c] == ' ') {
                                                               if (hasSpace) {
                                                                  break;
                                                               }

                                                               if (!hasSpace) {
                                                                  hasSpace = true;
                                                               }
                                                            }
                                                         }

                                                         fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                                      }
                                                   }

                                                   String s = dartKeys[c];
                                                   indxs = findWord(new String(chars), s);
                                                   
                                                   for(Iterator var81 = indxs.iterator(); var81.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                      i = (Integer)var81.next();
                                                   }

                                                   ++c;
                                                }
                                             }

                                             if (isCssPart) {
                                                tagsss = new String[]{"a", "abbr", "acronym", "address", "applet", "area", "article", "aside", "audio", "b", "base", "basefont", "bdi", "bdo", "big", "blockquote", "body", "br", "button", "canvas", "caption", "center", "cite", "code", "col", "colgroup", "data", "datalist", "dd", "del", "details", "dfn", "dialog", "dir", "div", "dl", "dt", "em", "embed", "fieldset", "figcaption", "figure", "font", "footer", "form", "frame", "frameset", "h1", "h2", "h3", "h4", "h5", "h6", "head", "header", "hr", "html", "i", "iframe", "img", "input", "ins", "kbd", "label", "legend", "li", "link", "main", "map", "mark", "meta", "meter", "nav", "noframes", "noscript", "object", "ol", "optgroup", "option", "output", "p", "param", "picture", "pre", "progress", "q", "rp", "rt", "ruby", "s", "samp", "script", "section", "select", "small", "source", "span", "strike", "strong", "style", "sup", "svg", "table", "tbody", "td", "template", "textarea", "tfoot", "th", "thead", "time", "title", "tr", "track", "tt", "u", "ul", "var", "video", "wbr", "important"};
                                                props = new String[]{"align-content", "align-items", "all", "animation", "animation-direction", "animation-duration", "animation-fill-mode", "animation-iteration-count", "animation-name", "animation-play-state", "animation-timing-function", "backface-visibility", "background", "background-attachment", "background-blend-mode", "background-clip", "background-color", "background-image", "background-origin", "background-position", "background-repeat", "background-size", "border", "border-bottom", "border-bottom-color", "border-bottom-left-radius", "border-bottom-right-radius", "border-bottom-style", "border-bottom-width", "border-collapse", "border-color", "border-image", "border-image-outset", "border-image-repeat", "border-image-slice", "border-image-source", "border-image-width", "border-radius", "border-right", "border-right-color", "border-right-style", "border-right-width", "border-spacing", "border-style", "border-top", "border-top-color", "border-top-left-radius", "border-top-right-radius", "border-top-style", "border-top-width", "border-width", "bottom", "box-decoration-break", "box-shadow", "box-sizing", "break-after", "break-before", "break-inside", "caption-side", "caret-color", "@charset", "clear", "clip", "color", "column-count", "column-fill", "column-gap", "column-rule", "column-rule-color", "column-rule-style", "column-rule-width", "column-span", "column-width", "columns", "content", "counter-increment", "counter-reset", "cursor", "direction", "display", "empty-cells", "filter", "flex", "flex-basis", "flex-direction", "flex-flow", "flex-grow", "flex-shrink", "flex-wrap", "float", "font", "@font-face", "font-family", "font-feature-settings", "@font-feature-values", "font-kerning", "font-language-override", "font-size", "font-size-adjust", "font-stretch", "font-style", "font-synthesis", "font-variant", "font-variant-alternates", "font-variant-caps", "font-variant-east-asian", "font-variant-ligatures", "font-variant-numeric", "font-variant-position", "font-weight", "gap", "grid", "grid-area", "grid-auto-columns", "grid-auto-flow", "grid-auto-rows", "grid-column", "grid-column-end", "grid-column-gap", "grid-column-start", "grid-template", "grid-template-areas", "grid-template-columns", "grid-template-rows", "hanging-ponctuation", "height", "hyphens", "image-rendering", "@import", "isolation", "justify-content", "@keyframes", "left", "letter-spacing", "line-break", "line-height", "list-style", "list-style-image", "list-style-position", "list-style-type", "margin", "margin-bottom", "margin-left", "margin-right", "margin-top", "mask", "mask-type", "max-height", "max-width", "@media", "min-height", "min-width", "mix-blend-mode", "object-fit", "object-position", "opacity", "order", "orphans", "outline", "outline-color", "outline-offset", "outline-style", "outline-width", "overflow", "overflow-wrap", "overflow-x", "overflow-y", "padding", "padding-bottom", "padding-left", "padding-right", "padding-top", "page-break-after", "page-break-before", "page-break-inside", "perspective", "perspective-origin", "pointer-events", "position", "quotes", "resize", "right", "row-gap", "scroll-behavior", "tab-size", "table-layout", "text-align", "text-align-last", "text-combine-upright", "text-decoration", "text-decoration-color", "text-decoration-line", "text-decoration-style", "text-indent", "text-justify", "text-orientation", "text-overflow", "text-shadow", "text-transform", "text-underline-position", "top", "transform", "transform-origin", "transform-style", "transition", "transition-delay", "transition-duration", "transition-property", "transition-timing-function", "unicode-bidi", "user-select", "vertical-align", "visibility", "white-space", "widows", "width", "word-break", "word-spacing", "word-wrap", "writing-mode", "z-index"};
                                                units = new String[]{"px", "em", "rem", "cm", "mm", "in", "pt", "pc", "ex", "ch", "vw", "vh", "vmin", "vmax"};
                                                pasKeys = tagsss;
                                                c = tagsss.length;

                                                for(var71 = 0; var71 < c; ++var71) {
                                                   s1 = pasKeys[var71];
                                                   indxs = findWord(new String(chars), s1);

                                                   for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                      i = (Integer)var83.next();
                                                   }
                                                }

                                                pasKeys = props;
                                                c = props.length;

                                                for(var71 = 0; var71 < c; ++var71) {
                                                   s1 = pasKeys[var71];
                                                   indxs = findWord(new String(chars), s1);

                                                   for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                      i = (Integer)var83.next();
                                                   }
                                                }

                                                pasKeys = units;
                                                c = units.length;

                                                for(var71 = 0; var71 < c; ++var71) {
                                                   s1 = pasKeys[var71];
                                                   indxs = findWord(new String(chars), s1);

                                                   for(var83 = indxs.iterator(); var83.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                      i = (Integer)var83.next();
                                                   }
                                                }

                                                indxs = findWord(new String(chars), ".");
                                                len = 0;
                                                var12 = indxs.iterator();

                                                while(var12.hasNext()) {
                                                   for(i = (Integer)var12.next(); i + len < chars.length && chars[i + len] != ' '; ++len) {
                                                   }

                                                   if (i + len < chars.length) {
                                                      fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                                   }
                                                }

                                                indxs = findWord(new String(chars), "#");
                                                len = 0;
                                                var12 = indxs.iterator();

                                                while(var12.hasNext()) {
                                                   for(i = (Integer)var12.next(); i + len < chars.length && chars[i + len] != ' '; ++len) {
                                                   }

                                                   if (i + len < chars.length) {
                                                      fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                                   }
                                                }

                                                indxs = findWord(new String(chars), ";");

                                                for(var12 = indxs.iterator(); var12.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                                   i = (Integer)var12.next();
                                                   c = i;

                                                   for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != '[' && chars[c] != ']' && chars[c] != '.' && chars[c] != '#' && chars[c] != ':'; ++len) {
                                                      --c;
                                                   }
                                                }

                                                indxs = findWord(new String(chars), "]");

                                                for(var12 = indxs.iterator(); var12.hasNext(); fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                                   i = (Integer)var12.next();
                                                   c = i;

                                                   for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                                      --c;
                                                   }
                                                }
                                             }
                                             break label5450;
                                          }

                                          if (!foundExt) {
                                             extType = "Makefile";
                                             foundExt = true;
                                          }

                                          indxs = findWord(new String(chars), ":");
                                          var210 = indxs.iterator();

                                          while(true) {
                                             if (!var210.hasNext()) {
                                                break label5450;
                                             }

                                             i = (Integer)var210.next();
                                             c = i;

                                             for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.'; ++len) {
                                                --c;
                                             }

                                             fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                                          }
                                       }

                                       if (!foundExt) {
                                          extType = "Batch";
                                          foundExt = true;
                                       }

                                       batCom = new String[]{"ver", "assoc", "cd", "cls", "copy", "del", "dir", "date", "echo", "@echo", "exit", "md", "move", "path", "pause", "prompt", "rd", "rem", "start", "time", "type", "vol", "attrib", "chkdsk", "choice", "cmd", "comp", "convert", "driverquery", "expand", "find", "format", "help", "ipconfig", "label", "more", "net", "ping", "shutdown", "sort", "subst", "subst", "systeminfo", "taskkill", "xcopy", "tree", "fc", "title", "set", "bash", "node", "off", "goto", "rmdir", "icacls", "takeown", "VER", "ASSOC", "CD", "CLS", "COPY", "DEL", "DIR", "DATE", "ECHO", "@ECHO", "EXIT", "MD", "MOVE", "PATH", "PAUSE", "PROMPT", "RD", "REM", "START", "TIME", "TYPE", "VOL", "ATTRIB", "CHKDSK", "CHOICE", "CMD", "COMP", "CONVERT", "DRIVERQUERY", "EXPAND", "FIND", "FORMAT", "HELP", "IPCONFIG", "LABEL", "MORE", "NET", "PING", "SHUTDOWN", "SORT", "SUBST", "SUBST", "SYSTEMINFO", "TASKKILL", "XCOPY", "TREE", "FC", "TITLE", "SET", "BASH", "NODE", "OFF", "GOTO", "RMDIR", "ICACLS", "TAKEOWN"};
                                       sqlKeys = batCom;
                                       var119 = batCom.length;
                                       var21 = 0;

                                       while(true) {
                                          if (var21 >= var119) {
                                             break label5450;
                                          }

                                          String s = sqlKeys[var21];
                                          indxs = findWord(new String(chars), s);
                                          
                                          for(Iterator var133 = indxs.iterator(); var133.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                             i = (Integer)var133.next();
                                          }

                                          ++var21;
                                       }
                                    }

                                    if (!foundExt) {
                                       extType = "JavaScript";
                                       foundExt = true;
                                    }

                                    jsKeys = new String[]{"abstract", "arguments", "await", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue", "debugger", "default", "delete", "do", "double", "else", "enum", "eval", "export", "extends", "false", "final", "finally", "float", "for", "function", "goto", "if", "implements", "import", "in", "instanceof", "int", "interface", "let", "long", "native", "new", "null", "package", "private", "protected", "public", "return", "short", "static", "super", "switch", "synchronized", "this", "throw", "throws", "transient", "true", "try", "typeof", "var", "void", "volatile", "while", "with", "yield", "undefined", "of", "async"};
                                    asmRegs = jsKeys;
                                    int var127 = jsKeys.length;

                                    for(var119 = 0; var119 < var127; ++var119) {
                                       String s = asmRegs[var119];
                                       indxs = findWord(new String(chars), s);
                                       
                                       for(Iterator var137 = indxs.iterator(); var137.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                          i = (Integer)var137.next();
                                       }
                                    }

                                    tagss = new String[]{"<!--", "<!doctype", "<!DOCTYPE", "<a", "<abbr", "<acronym", "<address", "<applet", "<area", "<article", "<aside", "<audio", "<b", "<base", "<basefont", "<bdi", "<bdo", "<big", "<blockquote", "<body", "<br", "<button", "<canvas", "<caption", "<center", "<cite", "<code", "<col", "<colgroup", "<data", "<datalist", "<dd", "<del", "<details", "<dfn", "<dialog", "<dir", "<div", "<dl", "<dt", "<em", "<embed", "<fieldset", "<figcaption", "<figure", "<font", "<footer", "<form", "<frame", "<frameset", "<h1", "<h2", "<h3", "<h4", "<h5", "<h6", "<head", "<header", "<hr", "<html", "<i", "<iframe", "<img", "<input", "<ins", "<kbd", "<label", "<legend", "<li", "<link", "<main", "<map", "<mark", "<meta", "<meter", "<nav", "<noframes", "<noscript", "<object", "<ol", "<optgroup", "<option", "<output", "<p", "<param", "<picture", "<pre", "<progress", "<q", "<rp", "<rt", "<ruby", "<s", "<samp", "<script", "<section", "<select", "<small", "<source", "<span", "<strike", "<strong", "<style", "<sup", "<svg", "<table", "<tbody", "<td", "<template", "<textarea", "<tfoot", "<th", "<thead", "<time", "<title", "<tr", "<track", "<tt", "<u", "<ul", "<var", "<video", "<wbr", "</a", "</abbr", "</acronym", "</address", "</applet", "</area", "</article", "</aside", "</audio", "</b", "</base", "</basefont", "</bdi", "</bdo", "</big", "</blockquote", "</body", "</br", "</button", "</canvas", "</caption", "</center", "</cite", "</code", "</col", "</colgroup", "</data", "</datalist", "</dd", "</del", "</details", "</dfn", "</dialog", "</dir", "</div", "</dl", "</dt", "</em", "</embed", "</fieldset", "</figcaption", "</figure", "</font", "</footer", "</form", "</frame", "</frameset", "</h1", "</h2", "</h3", "</h4", "</h5", "</h6", "</head", "</header", "</hr", "</html", "</i", "</iframe", "</img", "</input", "</ins", "</kbd", "</label", "</legend", "</li", "</link", "</main", "</map", "</mark", "</meta", "</meter", "</nav", "</noframes", "</noscript", "</object", "</ol", "</optgroup", "</option", "</output", "</p", "</param", "</picture", "</pre", "</progress", "</q", "</rp", "</rt", "</ruby", "</s", "</samp", "</script", "</section", "</select", "</small", "</source", "</span", "</strike", "</strong", "</style", "</sup", "</svg", "</table", "</tbody", "</td", "</template", "</textarea", "</tfoot", "</th", "</thead", "</time", "</title", "</tr", "</track", "</tt", "</u", "</ul", "</var", "</video", "</wbr"};
                                    asmKeys = tagss;
                                    c = tagss.length;

                                    for(var127 = 0; var127 < c; ++var127) {
                                       String s = asmKeys[var127];
                                       indxs = findWord(new String(chars), s);

                                       for(var143 = indxs.iterator(); var143.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                          i = (Integer)var143.next();
                                       }
                                    }

                                    indxs = findWord(new String(chars), ">");

                                    for(var129 = indxs.iterator(); var129.hasNext(); fs = color(i, i + 1, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                       i = (Integer)var129.next();
                                    }

                                    indxs = findWord(new String(chars), "<");
                                    len = 0;
                                    var129 = indxs.iterator();

                                    while(var129.hasNext()) {
                                       for(i = (Integer)var129.next(); i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']' && chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':' && chars[i + len] != '>'; ++len) {
                                       }

                                       if (i + len < chars.length) {
                                          fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                                       }
                                    }

                                    indxs = findWord(new String(chars), "</");
                                    len = 0;
                                    var129 = indxs.iterator();

                                    while(var129.hasNext()) {
                                       for(i = (Integer)var129.next(); i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']' && chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':'; ++len) {
                                       }

                                       if (i + len < chars.length) {
                                          fs = color(i, i + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                                       }
                                    }

                                    indxs = findWord(new String(chars), "=");
                                    var129 = indxs.iterator();

                                    while(true) {
                                       if (!var129.hasNext()) {
                                          break label5450;
                                       }

                                       i = (Integer)var129.next();
                                       c = i;

                                       for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.' && chars[c] != ':'; ++len) {
                                          --c;
                                       }

                                       fs = color(c, c + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                    }
                                 }

                                 if (!foundExt) {
                                    extType = "Assembly";
                                    foundExt = true;
                                 }

                                 asmRegs = new String[]{"rax", "rbx", "rcx", "rdx", "rsi", "rdi", "rbp", "rsp", "r8", "r9", "r10", "r11", "r12", "r13", "r14", "r15", "eax", "ebx", "ecx", "esi", "edi", "ebp", "esp", "r8d", "r9d", "r10d", "r11d", "r12d", "r13d", "r14d", "r15d", "ax", "bx", "cx", "dx", "si", "di", "bp", "sp", "r8w", "r9w", "r10w", "r11w", "r12w", "r13w", "r14w", "r15w", "al", "bl", "cl", "dl", "sil", "dil", "bpl", "spl", "r8b", "r9b", "r10b", "r11b", "r12b", "r13b", "r14b", "r15b", "ah", "bh", "ch", "dh", "edx"};
                                 hasKeys = asmRegs;
                                 int var147 = asmRegs.length;

                                 for(var26 = 0; var26 < var147; ++var26) {
                                    String s = hasKeys[var26];
                                    indxs = findWord(new String(chars), s);
                                    
                                    for(Iterator var158 = indxs.iterator(); var158.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs)) {
                                       i = (Integer)var158.next();
                                    }
                                 }

                                 asmKeys = new String[]{"global", "db", "dw", "equ", "extern", "org", "syscall", "aaa", "aad", "aam", "aas", "adc", "add", "addpd", "addps", "addressing", "addsd", "addss", "align", "and", "andnpd", "andnps", "andpd", "andps", "arpl", "as", "commandline", "ELFobjectfile", "macroprocessing", "syntaxUNIXversusIntel", "ascii", "assemblerSeeasB", "bcd", "binaryarithmeticinstructions", "bitinstructions", "bound", "bsf", "bsr", "bss", "bswap", "bt", "btc", "btr", "bts", "byte", "byte", "byte", "byte", "byteinstructionsC", "call", "cbtw", "clc", "cld", "clflush", "cli", "cltd", "cltq", "clts", "cmc", "cmova", "cmova", "cmovae", "cmovae", "cmovb", "cmovb", "cmovbe", "cmovbe", "cmovc", "cmovc", "cmove", "cmove", "cmovg", "cmovg", "cmovge", "cmovge", "cmovl", "cmovl", "cmovle", "cmovle", "cmovna", "cmovna", "cmovnae", "cmovnae", "cmovnb", "cmovnb", "cmovnbe", "cmovnbe", "cmovnc", "cmovnc", "cmovne", "cmovne", "cmovng", "cmovng", "cmovnge", "cmovnge", "cmovnl", "cmovnl", "cmovnle", "cmovnle", "cmovno", "cmovno", "cmovnp", "cmovnp", "cmovns", "cmovns", "cmovnz", "cmovnz", "cmovo", "cmovo", "cmovp", "cmovp", "cmovpe", "cmovpo", "cmovs", "cmovz", "cmp", "cmppd", "cmpps", "cmps", "cmpsb", "cmpsd", "cmpsl", "cmpss", "cmpsw", "cmpxchg", "cmpxchgb", "comisd", "comiss", "comm", "comment", "controltransferinstructions", "cpp", "cpuid", "cqtd", "cqto", "cvtdqpd", "cvtdqps", "cvtpddq", "cvtpdpi", "cvtpdps", "cvtpipd", "cvtpips", "cvtpsdq", "cvtpspd", "cvtpspi", "cvtsdsi", "cvtsdss", "cvtsisd", "cvtsiss", "cvtsssd", "cvtsssi", "cvttpddq", "cvttpdpi", "cvttpsdq", "cvttpspi", "cvttsdsi", "cvttsssi", "cwtd", "cwtlD", "daa", "das", "data", "datatransferinstructions", "dec", "decimalarithmeticinstructions", "directives", "div", "divpd", "divps", "divsd", "divss", "doubleE", "ELFobjectfile", "emms", "enter", "even", "extF", "fxm", "fabs", "fadd", "faddp", "fbe", "Seeas", "fbld", "fbstp", "fchs", "fclex", "fcmovb", "fcmovbe", "fcmove", "fcmovnb", "fcmovnbe", "fcmovne", "fcmovnu", "fcmovu", "fcom", "fcomi", "fcomip", "fcomp", "fcompp", "fcos", "fdecstp", "fdiv", "fdivp", "fdivr", "fdivrp", "ffree", "fiadd", "ficom", "ficomp", "fidiv", "fidivr", "fild", "file", "fimul", "fincstp", "finit", "fist", "fistp", "fisub", "fisubr", "flagcontrolinstructions", "fld", "fld", "fldcw", "fldenv", "fldle", "fldlt", "fldlg", "fldln", "fldpi", "fldz", "float", "floating-pointinstructions", "basicarithmetic", "comparison", "control", "datatransfer", "loadconstants", "logarithmic", "Seetranscendental", "transcendental", "trigonometric", "Seetranscendental", "fmul", "fmulp", "fnclex", "fninit", "fnop", "fnsave", "fnstcw", "fnstenv", "fnstsw", "fpatan", "fprem", "fprem", "fptan", "frndint", "frstor", "fsave", "fscale", "fsin", "fsincos", "fsqrt", "fst", "fstcw", "fstenv", "fstp", "fstsw", "fsub", "fsubp", "fsubr", "fsubrp", "ftst", "fucom", "fucomi", "fucomip", "fucomp", "fucompp", "fwait", "fxam", "fxch", "fxrstor", "fxsave", "fxtract", "fylx", "fylxp", "G", "gas", "globl", "group", "H", "hidden", "hlt", "ident", "identifier", "idiv", "imul", "in", "inc", "ins", "insb", "insl", "instruction", "format", "suffixes", "instructions", "binaryarithmetic", "bit", "byte", "controltransfer", "datatransfer", "decimalarithmetic", "flagcontrol", "floating-point-", "logical", "miscellaneous", "MMX-", "operatingsystemsupport-", "Opteron", "rotate", "segmentregister", "shift", "SIMDstatemanagement", "SSE-", "SSE-", "string", "insw", "int", "into", "invd", "invlpg", "iretJ", "ja", "jae", "jb", "jbe", "jc", "jcxz", "je", "jecxz", "jg", "jge", "jl", "jle", "jmp", "jnae", "jnb", "jnbe", "jnc", "jne", "jng", "jnge", "jnl", "jnle", "jno", "jnp", "jns", "jnz", "jo", "jp", "jpe", "jpo", "js", "jzK", "keywordL", "label", "numeric", "symbolic", "lahf", "lar", "lcall", "lcomm", "ldmxcsr", "lds", "lea", "leave", "les", "lfence", "lfs", "lgdt", "lgs", "lidt", "lldt", "lmsw", "local", "lock", "lods", "lodsb", "lodsl", "lodsw", "logicalinstructions", "long", "loop", "loope", "loopne", "loopnz", "loopz", "lret", "lsl", "lss", "ltr", "m", "maskmovdqu", "maskmovq", "maxpd", "maxps", "maxsd", "maxss", "mfence", "minpd", "minps", "minsd", "minss", "miscellaneousinstructions", "MMXinstructions", "comparison", "conversion", "datatransfer", "logical", "packedarithmetic", "rotate", "shift", "statemanagement", "mov", "movabs", "movabsA", "movapd", "movaps", "movd", "movdqq", "movdqa", "movdqu", "movhlps", "movhpd", "movhps", "movlhps", "movlpd", "movlps", "movmskpd", "movmskps", "movntdq", "movnti", "movntpd", "movntps", "movntq", "movq", "movqdq", "movs", "movsb", "movsd", "movsl", "movss", "movsw", "movupd", "movups", "movzb", "movzw", "mul", "mulpd", "mulps", "mulsd", "mulss", "N", "neg", "nop", "not", "numbers", "floatingpoint", "integers", "binary", "decimal", "hexadecimal", "octal", "operands", "immediate", "indirect", "memory", "addressing", "ordering", "register", "operatingsystemsupportinstructions", "Opteroninstructions", "or", "orpd", "orps", "out", "outs", "outsb", "outsl", "outswP", "packssdw", "packsswb", "packuswb", "paddb", "paddd", "paddq", "paddsb", "paddsw", "paddusb", "paddusw", "paddw", "pand", "pandn", "pause", "pavgb", "pavgw", "pcmpeqb", "pcmpeqd", "pcmpeqw", "pcmpgtb", "pcmpgtd", "pcmpgtw", "pextrw", "pinsrw", "pmaddwd", "pmaxsw", "pmaxub", "pminsw", "pminub", "pmovmskb", "pmulhuw", "pmulhw", "pmullw", "pmuludq", "pop", "popa", "popal", "popaw", "popf", "popfw", "popsection", "por", "prefetchnta", "prefetcht", "prefetcht", "prefetcht", "previous", "psadbw", "pshufd", "pshufhw", "pshuflw", "pshufw", "pslld", "pslldq", "psllq", "psllw", "psrad", "psraw", "psrld", "psrldq", "psrlq", "psrlw", "psubb", "psubd", "psubq", "psubsb", "psubsw", "psubusb", "psubusw", "psubw", "punpckhbw", "punpckhdq", "punpckhqdq", "punpckhwd", "punpcklbw", "punpckldq", "punpcklqdq", "punpcklwd", "push", "pusha", "pushal", "pushaw", "pushf", "pushfw", "pushsection", "pxor", "quad", "rcl", "rcpps", "rcpss", "rcr", "rdmsr", "rdpmc", "rdtsc", "rel", "rep", "repnz", "repz", "ret", "rol", "ror", "rotateinstructions", "rsm", "rsqrtps", "rsqrtss", "sahf", "sal", "sar", "sbb", "scas", "scasb", "scasl", "scasw", "section", "segmentregisterinstructions", "set", "seta", "setae", "setb", "setbe", "setc", "sete", "setg", "setge", "setl", "setle", "setna", "setnae", "setnb", "setnbe", "setnc", "setne", "setng", "setnge", "setnl", "setnle", "setno", "setnp", "setns", "setnz", "seto", "setp", "setpe", "setpo", "sets", "setz", "sfence", "sgdt", "shiftinstructions", "shl", "shld", "shr", "shrd", "shufpd", "shufps", "sidt", "SIMDstatemanagementinstructions", "skip", "sldt", "sleb", "smovl", "smsw", "sqrtpd", "sqrtps", "sqrtsd", "sqrtss", "SSEinstructions", "compare", "conversion", "datatransfer", "integer", "logical", "miscellaneous", "MXCSRstatemanagement", "packedarithmetic", "shuffle", "unpack", "SSEinstructions", "compare", "conversion", "datamovement", "logical", "miscellaneous", "packedarithmetic", "packedsingle-precisionfloating-point", "shuffle", "SIMDintegerinstructions", "unpack", "statement", "empty", "stc", "std", "sti", "stmxcsr", "stos", "stosb", "stosl", "stosw", "str", "string", "string", "stringinstructions", "sub", "subpd", "subps", "subsd", "subss", "symbolic", "sysenter", "sysexit", "tbss", "tcomm", "tdata", "test", "text", "ucomisd", "ucomiss", "ud", "uleb", "unpckhpd", "unpckhps", "unpcklpd", "unpcklps", "value", "verr", "verw", "wait", "wbinvd", "weak", "whitespace", "wrmsr", "xadd", "xchg", "xchgA", "xlat", "xlatb", "xor", "xorpd", "xorps", "zero"};
                                 fsKeys = asmKeys;
                                 var149 = asmKeys.length;

                                 for(var147 = 0; var147 < var149; ++var147) {
                                    String s = fsKeys[var147];
                                    indxs = findWord(new String(chars), s);
                                    
                                    for(Iterator var166 = indxs.iterator(); var166.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                       i = (Integer)var166.next();
                                    }
                                 }

                                 indxs = findWord(new String(chars), ".");
                                 len = 0;
                                 var143 = indxs.iterator();

                                 while(true) {
                                    if (!var143.hasNext()) {
                                       break label5450;
                                    }

                                    for(i = (Integer)var143.next(); i + len < chars.length && chars[i + len] != ' '; ++len) {
                                    }

                                    if (i + len < chars.length) {
                                       fs = color(i, i + len, new IDEFont(Fonts.variablesNormal, FONT_SIZE), (List)fs);
                                    }
                                 }
                              }

                              if (!foundExt) {
                                 extType = "Python";
                                 foundExt = true;
                              }

                              pyKeys = new String[]{"and", "as", "assert", "break", "class", "continue", "def", "del", "elif", "else", "except", "False", "finally", "for", "from", "global", "if", "import", "in", "is", "lambda", "None", "nonlocal", "not", "or", "pass", "raise", "return", "True", "try", "while", "with", "yield", "self"};
                              cKeys = pyKeys;
                              var14 = pyKeys.length;
                              c = 0;

                              while(true) {
                                 if (c >= var14) {
                                    break label5450;
                                 }

                                 s1 = cKeys[c];
                                 indxs = findWord(new String(chars), s1);
                                 
                                 for(Iterator var88 = indxs.iterator(); var88.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                    i = (Integer)var88.next();
                                 }

                                 ++c;
                              }
                           }

                           if (!foundExt) {
                              extType = "JavaScript Object Notation - JSON";
                              foundExt = true;
                           }

                           jsonKeys = new String[]{"true", "false"};
                           goKeys = jsonKeys;
                           var190 = jsonKeys.length;
                           var38 = 0;

                           while(true) {
                              if (var38 >= var190) {
                                 break label5450;
                              }

                              String s = goKeys[var38];
                              indxs = findWord(new String(chars), s);
                              
                              for(Iterator var205 = indxs.iterator(); var205.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                                 i = (Integer)var205.next();
                              }

                              ++var38;
                           }
                        }

                        if (!foundExt) {
                           extType = "Pascal";
                           foundExt = true;
                        }

                        pasKeys = new String[]{"and", "begin", "boolean", "break", "byte", "continue", "div", "do", "double", "else", "end", "false", "if", "integer", "longint", "mod", "not", "or", "repeat", "shl", "shortint", "shr", "single", "then", "true", "until", "while", "word", "xor", "function"};
                        rKeys = pasKeys;
                        var89 = pasKeys.length;
                        c = 0;

                        while(true) {
                           if (c >= var89) {
                              break label5450;
                           }

                           String s = rKeys[c];
                           indxs = findWord(new String(chars), s);
                           
                           for(Iterator var111 = indxs.iterator(); var111.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                              i = (Integer)var111.next();
                           }

                           ++c;
                        }
                     }

                     if (!foundExt) {
                        extType = "Haskell";
                        foundExt = true;
                     }

                     hasKeys = new String[]{"as", "case", "of", "class", "data", "family", "data", "instance", "default", "deriving", "do", "forall", "foreign", "hiding", "if", "then", "else", "import", "infix", "infixl", "infixr", "let", "in", "mdo", "module", "newtype", "proc", "qualified", "rec", "type", "where"};
                     rsKeys = hasKeys;
                     var162 = hasKeys.length;
                     var152 = 0;

                     while(true) {
                        if (var152 >= var162) {
                           break label5450;
                        }

                        String s = rsKeys[var152];
                        indxs = findWord(new String(chars), s);
                        
                        for(Iterator var175 = indxs.iterator(); var175.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                           i = (Integer)var175.next();
                        }

                        ++var152;
                     }
                  }

                  if (!foundExt) {
                     extType = "Arquivo de Texto";
                     foundExt = true;
                  }
                  break label5450;
               }

               if (!foundExt) {
                  extType = "C++";
                  foundExt = true;
               }

               cppKeys = new String[]{"auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while", "asm", "dynamic_cast", "namespace", "reinterpret_cast", "bool", "explicit", "new", "static_cast", "false", "catch", "operator", "template", "friend", "private", "class", "this", "inline", "public", "throw", "const_cast", "delete", "mutable", "protected", "true", "try", "typeid", "typename", "using", "virtual", "wchar_t", "include", "define", "string", "ifdef", "ifndef", "error", "pragma", "endif", "override"};
               jsKeys = cppKeys;
               var109 = cppKeys.length;
               var107 = 0;

               while(true) {
                  if (var107 >= var109) {
                     break label5450;
                  }

                  String s = jsKeys[var107];
                  indxs = findWord(new String(chars), s);
                  
                  for(Iterator var132 = indxs.iterator(); var132.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                     i = (Integer)var132.next();
                  }

                  ++var107;
               }
            }

            if (!foundExt) {
               extType = "Arquivo Compactado";
               foundExt = true;
            }
            break label5450;
         }

         if (!foundExt) {
            extType = "Arquivo de Fonte";
            foundExt = true;
         }
      }

      javaKeys = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "0", "1a", "2a", "3a", "4a", "5a", "6a", "7a", "8a", "9a", "0a", "1b", "2b", "3b", "4b", "5b", "6b", "7b", "8b", "9b", "0b", "1c", "2c", "3c", "4c", "5c", "6c", "7c", "8c", "9c", "0c", "1d", "2d", "3d", "4d", "5d", "6d", "7d", "8d", "9d", "0d", "1e", "2e", "3e", "4e", "5e", "6e", "7e", "8e", "9e", "0e", "1f", "2f", "3f", "4f", "5f", "6f", "7f", "8f", "9f", "0f", "1l", "2l", "3l", "4l", "5l", "6l", "7l", "8l", "9l", "0l", "1A", "2A", "3A", "4A", "5A", "6A", "7A", "8A", "9A", "0A", "1B", "2B", "3B", "4B", "5B", "6B", "7B", "8B", "9B", "0B", "1C", "2C", "3C", "4C", "5C", "6C", "7C", "8C", "9C", "0C", "1D", "2D", "3D", "4D", "5D", "6D", "7D", "8D", "9D", "0D", "1E", "2E", "3E", "4E", "5E", "6E", "7E", "8E", "9E", "0E", "1F", "2F", "3F", "4F", "5F", "6F", "7F", "8F", "9F", "0F", "1L", "2L", "3L", "4L", "5L", "6L", "7L", "8L", "9L", "0L", "0x", "0X"};
      props = javaKeys;
      i = javaKeys.length;

      for(len = 0; len < i; ++len) {
         s1 = props[len];
         indxs = findWord(new String(chars), s1);

         for(var63 = indxs.iterator(); var63.hasNext(); fs = color(i, i + s1.length(), new IDEFont(Fonts.numbersNormal, FONT_SIZE), (List)fs)) {
            i = (Integer)var63.next();
         }
      }

      indxs = findWord(new String(chars), "0x");
      len = 0;
      var69 = indxs.iterator();

      while(var69.hasNext()) {
         for(i = (Integer)var69.next(); i + len < chars.length && chars[i + len] != ' ' && chars[i + len] != '[' && chars[i + len] != ']' && chars[i + len] != '(' && chars[i + len] != ')' && chars[i + len] != ',' && chars[i + len] != ';' && chars[i + len] != '.' && chars[i + len] != ':'; ++len) {
         }

         if (i + len < chars.length) {
            fs = color(i, i + len, new IDEFont(Fonts.numbersNormal, FONT_SIZE), (List)fs);
         }
      }

      String s;
      if (ext.equals(".java") || ext.equals(".c") || ext.equals(".cs") || ext.equals(".css") || ext.equals(".cpp") || ext.equals(".cxx") || ext.equals(".js") || ext.equals(".h") || ext.equals(".hpp") || ext.equals(".hxx") || ext.equals(".lua") || ext.equals(".rs") || ext.equals(".asm") || ext.equals(".php") || ext.equals(".kt") || ext.equals(".vue") || ext.equals(".py") || ext.equals(".pyd") || ext.equals(".rb") || ext.equals(".ino") || ext.equals(".ts") || ext.equals(".swift") || ext.equals(".html") || ext.equals(".htm") || ext.equals(".go") || ext.equals(".r") || ext.equals(".jl") || ext.equals(".pl") || ext.equals(".has") || ext.equals(".hs") || ext.equals(".fs") || ext.equals(".coffee") || ext.equals(".m") || ext.equals(".jsx") || ext.equals(".ld") || ext.equals(".pas") || ext.equals(".pp") || ext.equals(".scala") || ext.equals(".dart") || ext.equals(".md") || ext.equals(".json") || ext.equals(".jsonc")) {
         indxs = findWord(new String(chars), "(");

         for(var69 = indxs.iterator(); var69.hasNext(); fs = color(len, len + len, new IDEFont(Fonts.methodsNormal, FONT_SIZE), (List)fs)) {
            i = (Integer)var69.next();
            len = i;

            for(len = 0; len < chars.length && len + len < chars.length && len > 0 && chars[len] != ' ' && chars[len] != '[' && chars[len] != ']' && chars[len] != ',' && chars[len] != ';' && chars[len] != '.' && chars[len] != '-' && chars[len] != '+' && chars[len] != '*' && chars[len] != '/' && chars[len] != '<' && chars[len] != '>' && chars[len] != '?' && chars[len] != ':'; ++len) {
               --len;
            }
         }

         String[] syms = new String[]{" ", "(", ")", "[", "]", "{", "}", ",", ".", "<", ">", ";", ":", "?", "/", "|", "+", "-", "*", "=", "&", "%", "$", "#", "!", "@"};
         pyKeys = syms;
         c = syms.length;

         for(len = 0; len < c; ++len) {
            s = pyKeys[len];
            indxs = findWord(new String(chars), s);
            
            for(Iterator var105 = indxs.iterator(); var105.hasNext(); fs = color(i, i + 1, new IDEFont(Fonts.symbolsNormal, FONT_SIZE), (List)fs)) {
               i = (Integer)var105.next();
            }
         }

         indxs = findWord(new String(chars), (new Character('"')).toString());

         for(i = 0; i < indxs.size() - 1; i += 2) {
            fs = color((Integer)indxs.get(i), (Integer)indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), (List)fs);
         }

         indxs = findWord(new String(chars), (new Character('\'')).toString());

         for(i = 0; i < indxs.size() - 1; i += 2) {
            fs = color((Integer)indxs.get(i), (Integer)indxs.get(i + 1) + 1, new IDEFont(Fonts.stringsNormal, FONT_SIZE), (List)fs);
         }

         var61 = ((List)fs).iterator();

         while(var61.hasNext()) {
            IDEFont id = (IDEFont)var61.next();
            id.setSize(FONT_SIZE);
         }
      }

      if (!foundExt && editing != null) {
         FileType[] var84;
         len = (var84 = ListableFile.types).length;

         label2991:
         for(i = 0; i < len; ++i) {
            FileType f = var84[i];
            if (f.getExtension().equalsIgnoreCase(editing.getRegent().getRegent().getName())) {
               s = capitalizeFirstLetter(f.getExtension());
               extType = s;
               indxs = findWord(new String(chars), "#");
               if (((List)fs).size() == 0) {
                  break;
               }

               if (indxs.size() != 0) {
                  fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               }

               switch((s = s.toLowerCase()).hashCode()) {
               case -1317317732:
                  if (!s.equals("dockerfile")) {
                     break;
                  }

                  dkKeys = new String[]{"FROM", "RUN", "VOLUME", "WORKDIR", "from", "run", "volume", "workdir"};
                  csKeys = dkKeys;
                  c = dkKeys.length;

                  for(var15 = 0; var15 < c; ++var15) {
                     s = csKeys[var15];
                     indxs = findWord(new String(chars), s);

                     for(var106 = indxs.iterator(); var106.hasNext(); fs = color(i, i + s.length(), new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs)) {
                        i = (Integer)var106.next();
                     }
                  }
                  break;
               case -196941788:
                  if (s.equals("gitignore")) {
                     extType = "Git Ignore";
                     foundExt = true;
                  }
                  break;
               case 41047146:
                  if (!s.equals("makefile")) {
                     break;
                  }

                  indxs = findWord(new String(chars), ":");
                  Iterator var102 = indxs.iterator();

                  while(true) {
                     if (!var102.hasNext()) {
                        continue label2991;
                     }

                     i = (Integer)var102.next();
                     c = i;

                     for(len = 0; c < chars.length && c + len < chars.length && c > 0 && chars[c] != ' ' && chars[c] != '[' && chars[c] != ']' && chars[c] != ',' && chars[c] != ';' && chars[c] != '.'; ++len) {
                        --c;
                     }

                     fs = color(c, c + len, new IDEFont(Fonts.keywordsNormal, FONT_SIZE), (List)fs);
                  }
               case 166757441:
                  if (s.equals("license") && !foundExt) {
                     extType = "Arquivo de Licença";
                     foundExt = true;
                  }
               }
            }
         }

         if (extType.equals("") || extType == null) {
            extn = "";

            try {
               extn = ListableFile.getFileExtension(editing.getRegent().getRegent()).substring(1);
            } catch (Exception var49) {
               extn = "Sem Extensão";
            }

            extType = extn;
            foundExt = true;
         }
      }

      label2932: {
         label2931: {
            label2930: {
               label2929: {
                  label2928: {
                     switch((extn = ext.toLowerCase()).hashCode()) {
                     case -646628426:
                        if (!extn.equals(".gitignore")) {
                           break label2932;
                        }
                        break label2931;
                     case 1525:
                        if (!extn.equals(".c")) {
                           break label2932;
                        }
                        break;
                     case 1530:
                        if (!extn.equals(".h")) {
                           break label2932;
                        }
                        break;
                     case 1535:
                        if (!extn.equals(".m")) {
                           break label2932;
                        }
                        break;
                     case 1540:
                        if (!extn.equals(".r")) {
                           break label2932;
                        }
                        break label2931;
                     case 1541:
                        if (!extn.equals(".s")) {
                           break label2932;
                        }
                        break label2929;
                     case 47390:
                        if (!extn.equals(".cs")) {
                           break label2932;
                        }
                        break;
                     case 47483:
                        if (!extn.equals(".fs")) {
                           break label2932;
                        }
                        break;
                     case 47510:
                        if (!extn.equals(".go")) {
                           break label2932;
                        }
                        break;
                     case 47545:
                        if (!extn.equals(".hs")) {
                           break label2932;
                        }
                        break label2930;
                     case 47600:
                        if (!extn.equals(".jl")) {
                           break label2932;
                        }
                        break label2931;
                     case 47607:
                        if (!extn.equals(".js")) {
                           break label2932;
                        }
                        break;
                     case 47639:
                        if (!extn.equals(".kt")) {
                           break label2932;
                        }
                        break;
                     case 47685:
                        if (extn.equals(".md")) {
                           indxs = findWord(new String(chars), "[//]: #");
                           if (((List)fs).size() != 0 && indxs.size() != 0) {
                              fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              indxs = findWord(new String(chars), "[]: #");
                              if (((List)fs).size() != 0 && indxs.size() != 0) {
                                 fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              }
                           }
                        }
                        break label2932;
                     case 47786:
                        if (!extn.equals(".pl")) {
                           break label2932;
                        }
                        break label2931;
                     case 47790:
                        if (!extn.equals(".pp")) {
                           break label2932;
                        }
                        break;
                     case 47799:
                        if (!extn.equals(".py")) {
                           break label2932;
                        }
                        break label2931;
                     case 47838:
                        if (!extn.equals(".rb")) {
                           break label2932;
                        }
                        break label2931;
                     case 47855:
                        if (!extn.equals(".rs")) {
                           break label2932;
                        }
                        break;
                     case 47875:
                        if (!extn.equals(".sh")) {
                           break label2932;
                        }
                        break label2931;
                     case 47917:
                        if (!extn.equals(".ts")) {
                           break label2932;
                        }
                        break;
                     case 1467277:
                        if (!extn.equals(".asm")) {
                           break label2932;
                        }
                        break label2929;
                     case 1467687:
                        if (!extn.equals(".bat")) {
                           break label2932;
                        }
                        break label2928;
                     case 1468790:
                        if (!extn.equals(".cfg")) {
                           break label2932;
                        }
                        break label2931;
                     case 1469075:
                        if (!extn.equals(".com")) {
                           break label2932;
                        }
                        break label2928;
                     case 1469109:
                        if (!extn.equals(".cpp")) {
                           break label2932;
                        }
                        break;
                     case 1473452:
                        if (!extn.equals(".has")) {
                           break label2932;
                        }
                        break label2930;
                     case 1473914:
                        if (!extn.equals(".hpp")) {
                           break label2932;
                        }
                        break;
                     case 1474170:
                        if (!extn.equals(".hxx")) {
                           break label2932;
                        }
                        break;
                     case 1474812:
                        if (!extn.equals(".ino")) {
                           break label2932;
                        }
                        break;
                     case 1475937:
                        if (!extn.equals(".jsx")) {
                           break label2932;
                        }
                        break;
                     case 1477898:
                        if (!extn.equals(".lua")) {
                           break label2932;
                        }
                        break label2930;
                     case 1481140:
                        if (!extn.equals(".pas")) {
                           break label2932;
                        }
                        break;
                     case 1481354:
                        if (!extn.equals(".php")) {
                           break label2932;
                        }
                        break label2931;
                     case 1481632:
                        if (!extn.equals(".ps1")) {
                           break label2932;
                        }
                        break label2928;
                     case 1481869:
                        if (!extn.equals(".pyd")) {
                           break label2932;
                        }
                        break label2931;
                     case 1484512:
                        if (!extn.equals(".sql")) {
                           break label2932;
                        }
                        break label2930;
                     case 1487512:
                        if (!extn.equals(".vue")) {
                           break label2932;
                        }
                        break;
                     case 45736784:
                        if (!extn.equals(".java")) {
                           break label2932;
                        }
                        break;
                     case 45825820:
                        if (!extn.equals(".make")) {
                           break label2932;
                        }
                        break label2931;
                     case 815433082:
                        if (!extn.equals(".coffee")) {
                           break label2932;
                        }
                        break label2931;
                     case 815671536:
                        if (!extn.equals(".config")) {
                           break label2932;
                        }
                        break label2931;
                     case 1426191832:
                        if (!extn.equals(".scala")) {
                           break label2932;
                        }
                        break;
                     case 1426795173:
                        if (!extn.equals(".swift")) {
                           break label2932;
                        }
                        break;
                     case 1922265674:
                        if (!extn.equals(".dockerfile")) {
                           break label2932;
                        }
                        break label2931;
                     default:
                        break label2932;
                     }

                     indxs = findWord(new String(chars), "//");
                     if (((List)fs).size() != 0 && indxs.size() != 0) {
                        fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                     }
                     break label2932;
                  }

                  indxs = findWord(new String(chars), "REM");
                  if (((List)fs).size() != 0 && indxs.size() != 0) {
                     fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                  }
                  break label2932;
               }

               indxs = findWord(new String(chars), ";");
               if (((List)fs).size() != 0 && indxs.size() != 0) {
                  fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               }
               break label2932;
            }

            indxs = findWord(new String(chars), "--");
            if (((List)fs).size() != 0 && indxs.size() != 0) {
               fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
            }
            break label2932;
         }

         indxs = findWord(new String(chars), "#");
         if (((List)fs).size() != 0 && indxs.size() != 0) {
            fs = color((Integer)indxs.get(0), ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
         }
      }

      label5245: {
         List finals;
         label5246: {
            label5466: {
               label5248: {
                  label5249: {
                     switch((s = ext.toLowerCase()).hashCode()) {
                     case 1525:
                        if (!s.equals(".c")) {
                           break label5245;
                        }
                        break label5248;
                     case 1530:
                        if (!s.equals(".h")) {
                           break label5245;
                        }
                        break label5248;
                     case 1535:
                        if (!s.equals(".m")) {
                           break label5245;
                        }
                        break label5248;
                     case 47390:
                        if (!s.equals(".cs")) {
                           break label5245;
                        }
                        break label5248;
                     case 47483:
                        if (s.equals(".fs")) {
                           indxs = findWord(new String(chars), "(*");
                           finals = findWord(new String(chars), "*)");
                           if (indxs.size() > 0) {
                              fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = true;
                           }

                           if (finals.size() > 0) {
                              fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = false;
                           }

                           if (isMultilineCommenting) {
                              fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                           }
                        }
                        break label5245;
                     case 47510:
                        if (!s.equals(".go")) {
                           break label5245;
                        }
                        break label5248;
                     case 47545:
                        if (!s.equals(".hs")) {
                           break label5245;
                        }
                        break label5466;
                     case 47600:
                        if (s.equals(".jl")) {
                           indxs = findWord(new String(chars), "#=");
                           finals = findWord(new String(chars), "=#");
                           if (indxs.size() > 0) {
                              fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = true;
                           }

                           if (finals.size() > 0) {
                              fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = false;
                           }

                           if (isMultilineCommenting) {
                              fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                           }
                        }
                        break label5245;
                     case 47607:
                        if (!s.equals(".js")) {
                           break label5245;
                        }
                        break label5248;
                     case 47639:
                        if (!s.equals(".kt")) {
                           break label5245;
                        }
                        break label5248;
                     case 47790:
                        if (!s.equals(".pp")) {
                           break label5245;
                        }
                        break label5246;
                     case 47799:
                        if (!s.equals(".py")) {
                           break label5245;
                        }
                        break label5249;
                     case 47838:
                        if (s.equals(".rb")) {
                           indxs = findWord(new String(chars), "=begin");
                           finals = findWord(new String(chars), "=end");
                           if (indxs.size() > 0) {
                              fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = true;
                           }

                           if (finals.size() > 0) {
                              fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = false;
                           }

                           if (isMultilineCommenting) {
                              fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                           }
                        }
                        break label5245;
                     case 47855:
                        if (!s.equals(".rs")) {
                           break label5245;
                        }
                        break label5248;
                     case 47917:
                        if (!s.equals(".ts")) {
                           break label5245;
                        }
                        break label5248;
                     case 1469109:
                        if (!s.equals(".cpp")) {
                           break label5245;
                        }
                        break label5248;
                     case 1469205:
                        if (!s.equals(".css")) {
                           break label5245;
                        }
                        break label5248;
                     case 1469365:
                        if (!s.equals(".cxx")) {
                           break label5245;
                        }
                        break label5248;
                     case 1470848:
                        if (!s.equals(".ejs")) {
                           break label5245;
                        }
                        break;
                     case 1473452:
                        if (!s.equals(".has")) {
                           break label5245;
                        }
                        break label5466;
                     case 1473914:
                        if (!s.equals(".hpp")) {
                           break label5245;
                        }
                        break label5248;
                     case 1474035:
                        if (!s.equals(".htm")) {
                           break label5245;
                        }
                        break;
                     case 1474170:
                        if (!s.equals(".hxx")) {
                           break label5245;
                        }
                        break label5248;
                     case 1474812:
                        if (!s.equals(".ino")) {
                           break label5245;
                        }
                        break label5248;
                     case 1475937:
                        if (!s.equals(".jsx")) {
                           break label5245;
                        }
                        break label5248;
                     case 1477898:
                        if (s.equals(".lua")) {
                           indxs = findWord(new String(chars), "--[[");
                           finals = findWord(new String(chars), "]]--");
                           if (indxs.size() > 0) {
                              fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = true;
                           }

                           if (finals.size() > 0) {
                              fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                              isMultilineCommenting = false;
                           }

                           if (isMultilineCommenting) {
                              fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                           }
                        }
                        break label5245;
                     case 1481140:
                        if (!s.equals(".pas")) {
                           break label5245;
                        }
                        break label5246;
                     case 1481354:
                        if (!s.equals(".php")) {
                           break label5245;
                        }
                        break label5248;
                     case 1481869:
                        if (!s.equals(".pyd")) {
                           break label5245;
                        }
                        break label5249;
                     case 1484512:
                        if (!s.equals(".sql")) {
                           break label5245;
                        }
                        break label5248;
                     case 1484662:
                        if (!s.equals(".svg")) {
                           break label5245;
                        }
                        break;
                     case 1487512:
                        if (!s.equals(".vue")) {
                           break label5245;
                        }
                        break label5248;
                     case 1489193:
                        if (!s.equals(".xml")) {
                           break label5245;
                        }
                        break;
                     case 45695193:
                        if (!s.equals(".html")) {
                           break label5245;
                        }
                        break;
                     case 45736784:
                        if (!s.equals(".java")) {
                           break label5245;
                        }
                        break label5248;
                     case 45753878:
                        if (!s.equals(".json")) {
                           break label5245;
                        }
                        break label5248;
                     case 1418370317:
                        if (!s.equals(".jsonc")) {
                           break label5245;
                        }
                        break label5248;
                     case 1426191832:
                        if (!s.equals(".scala")) {
                           break label5245;
                        }
                        break label5248;
                     case 1426795173:
                        if (!s.equals(".swift")) {
                           break label5245;
                        }
                        break label5248;
                     default:
                        break label5245;
                     }

                     indxs = findWord(new String(chars), "<!--");
                     finals = findWord(new String(chars), "-->");
                     if (indxs.size() > 0) {
                        fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                        isMultilineCommenting = true;
                     }

                     if (finals.size() > 0) {
                        fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                        isMultilineCommenting = false;
                     }

                     if (isMultilineCommenting) {
                        fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                     }
                     break label5245;
                  }

                  indxs = findWord(new String(chars), "'''");
                  if (indxs.size() > 0 && !isMultilineCommenting) {
                     fs = color((Integer)indxs.get(0), indxs.size() <= 1 ? ((List)fs).size() : (Integer)indxs.get(1), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                     isMultilineCommenting = true;
                     isAnotherIteration = false;
                  }

                  if (indxs.size() > 0 && isMultilineCommenting && isAnotherIteration) {
                     fs = color(0, (Integer)indxs.get(0) + 2, new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                     isMultilineCommenting = false;
                  }

                  isAnotherIteration = true;
                  if (isMultilineCommenting) {
                     fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                  }
                  break label5245;
               }

               indxs = findWord(new String(chars), "/*");
               finals = findWord(new String(chars), "*/");
               if (indxs.size() > 0) {
                  fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                  isMultilineCommenting = true;
               }

               if (finals.size() > 0) {
                  fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
                  isMultilineCommenting = false;
               }

               if (isMultilineCommenting) {
                  fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               }
               break label5245;
            }

            indxs = findWord(new String(chars), "{-");
            finals = findWord(new String(chars), "-}");
            if (indxs.size() > 0) {
               fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               isMultilineCommenting = true;
            }

            if (finals.size() > 0) {
               fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
               isMultilineCommenting = false;
            }

            if (isMultilineCommenting) {
               fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
            }
            break label5245;
         }

         indxs = findWord(new String(chars), "(*");
         finals = findWord(new String(chars), "*)");
         if (indxs.size() > 0) {
            fs = color((Integer)indxs.get(0), finals.size() <= 0 ? ((List)fs).size() : (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
            isMultilineCommenting = true;
         }

         if (finals.size() > 0) {
            fs = color(indxs.size() <= 0 ? 0 : (Integer)indxs.get(indxs.size() - 1), (Integer)finals.get(0), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
            isMultilineCommenting = false;
         }

         if (isMultilineCommenting) {
            fs = color(0, ((List)fs).size(), new IDEFont(Fonts.commentsNormal, FONT_SIZE), (List)fs);
         }
      }

      if (linesWithErrors != null && syntaxErrorsOn) {
         for(var73 = linesWithErrors.iterator(); var73.hasNext(); fs = color(0, ((List)fs).size(), new IDEFont(Fonts.errorNormal, FONT_SIZE), (List)fs)) {
            i = (Integer)var73.next();
            if (toCharArray(((IDELine)lines.get(i)).getChars()) == chars) {
               return (List)fs;
            }
         }
      }

      return (List)fs;

  }
  
  public static String capitalizeFirstLetter(String s) {
    char f = Character.toUpperCase(s.charAt(0));
    String c = String.valueOf(f) + s.substring(1);
    return c;
  }
  
  public int countChar(String str, char c) {
    int count = 0;
    for (int i = 0; i < str.length() && 
      str.charAt(i) == c; i++) {
      if (str.charAt(i) == c)
        count++; 
    } 
    return count;
  }
  
  public static char[] toCharArray(List<Character> list) {
    if (list.size() == 0)
      return new char[0]; 
    StringBuilder sb = new StringBuilder();
    for (Character ch : list)
      sb.append(ch); 
    String str = sb.toString();
    return str.toCharArray();
  }
  
  public List<Character> toCharList(char[] array) {
    List<Character> cl = new ArrayList<>();
    byte b;
    int i;
    char[] arrayOfChar;
    for (i = (arrayOfChar = array).length, b = 0; b < i; ) {
      char c = arrayOfChar[b];
      cl.add(Character.valueOf(c));
      b++;
    } 
    return cl;
  }
  
  public IDEFont[] toArray(List<IDEFont> list) {
    IDEFont[] a = new IDEFont[list.size()];
    for (int i = 0; i < list.size(); i++)
      a[i] = list.get(i); 
    return a;
  }
  
  private StringBuilder addCodeHints(StringBuilder pre) {
    switch (KeyInput.getCharPressed()) {
      case '{':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append('}');
          break;
        } 
        pre.insert(cursorX + 1, '}');
        break;
      case '(':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append(')');
          break;
        } 
        pre.insert(cursorX + 1, ')');
        break;
      case '[':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append(']');
          break;
        } 
        pre.insert(cursorX + 1, ']');
        break;
      case '<':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append('>');
          break;
        } 
        pre.insert(cursorX + 1, '>');
        break;
      case '"':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append('"');
          break;
        } 
        pre.insert(cursorX + 1, '"');
        break;
      case '\'':
        if (pre.length() == 0 || cursorX == pre.length()) {
          pre.append('\'');
          break;
        } 
        pre.insert(cursorX + 1, '\'');
        break;
    } 
    return pre;
  }
  
  public static void setCursorWithinBounds() {
    if (editing == null)
      return; 
    try {
      if (cursorY < 1)
        cursorY = 1; 
      if (cursorY + 1 > lines.size())
        cursorY = lines.size(); 
      if (cursorX < 0)
        cursorX = 0; 
      if (cursorX > ((IDELine)lines.get(cursorY - 1)).getChars().size())
        cursorX = ((IDELine)lines.get(cursorY - 1)).getChars().size(); 
    } catch (Exception exception) {}
  }
  
  private int setWithinBounds(int x, int y, boolean isX) {
    try {
      if (isX) {
        if (y < 1)
          y = 1; 
        if (y + 1 > lines.size())
          y = lines.size(); 
        if (x < 0)
          x = 0; 
        if (x > ((IDELine)lines.get(y - 1)).getChars().size())
          x = ((IDELine)lines.get(y - 1)).getChars().size(); 
        return x;
      } 
      if (y < 1)
        y = 1; 
      if (y + 1 > lines.size())
        y = lines.size(); 
      return y;
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
  
  public void register(StringBuilder cY, int y) {
    String gs = cY.toString();
    char[] ca = gs.toCharArray();
    List<Character> lc = toCharList(ca);
    ((IDELine)lines.get(y)).getChars().clear();
    ((IDELine)lines.get(y)).getFonts().clear();
    for (Character c : lc) {
      ((IDELine)lines.get(y)).getChars().add(c);
      ((IDELine)lines.get(y)).getFonts().add(new IDEFont(Fonts.otherNormal, FONT_SIZE));
    } 
  }
  
  public char addAccents(int keyCode, char ch) {
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
		
		if (pressedAccent && !(keyCode == KeyEvent.VK_SHIFT || keyCode == KeyEvent.VK_CONTROL)) { // acabar os acentos e adicionar coloração automática
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
				break;
			case TILDE:
				if (ch == 'a') return 'ã';
				else if (ch == 'A') return 'Ã';
				
				if (ch == 'O') return 'Õ';
				else if (ch == 'N') return 'Ñ';
				
				if (ch == 'o') return 'õ';
				else if (ch == 'n') return 'ñ';
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
  
  public void paste() {
    if (editing == null)
      return; 
    String[] sp = clipboard.split("\n");
    int index = 0;
    if (sp.length == 1) {
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = sp).length, b = 0; b < i; ) {
        String s = arrayOfString[b];
        StringBuilder stringBuilder = new StringBuilder(new String(toCharArray(((IDELine)lines.get(cursorY - 1)).getChars())));
        stringBuilder.insert(cursorX, s);
        register(stringBuilder, cursorY - 1 + index);
        cursorX += s.length();
        b++;
      } 
    } else {
      byte b;
      int i;
      String[] arrayOfString;
      for (i = (arrayOfString = sp).length, b = 0; b < i; ) {
        String s = arrayOfString[b];
        if (s != sp[0])
          lines.add(cursorY - 1 + index, new IDELine(new ArrayList<>(), new ArrayList<>())); 
        StringBuilder stringBuilder = new StringBuilder(new String(toCharArray(((IDELine)lines.get(cursorY - 1 + index)).getChars())));
        int x = (cursorX > ((IDELine)lines.get(cursorY - 1 + index)).getChars().size()) ? ((IDELine)lines.get(cursorY - 1 + index)).getChars().size() : cursorX;
        stringBuilder.insert(x, s);
        register(stringBuilder, cursorY - 1 + index);
        if (s == sp[sp.length - 1]) {
          cursorX += s.length();
          cursorY += sp.length - 1;
        } 
        index++;
        b++;
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
    String str;
    switch ((str = arg).hashCode()) {
      case -1263189637:
        if (!str.equals("opendef"))
          break; 
        (new Thread() {
            public void run() {
              try {
                Main.desktop.open(CodeEditor.editing.getRegent().getRegent());
              } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "O sistema nencontrou um programa padrpara abrir esse arquivo.", "Nencontrou nada!", 0);
              } 
            }
          }).start();
        break;
      case -887342032:
        if (!str.equals("sysexp"))
          break; 
        try {
          if (Main.baseFolder == null)
            return; 
          String str1 = null;
          try {
            String str2 = (editing == null) ? ((ListableFile)Explorer.files.get(0)).getRegent().getPath() : editing.getRegent().getRegent().getPath();
          } catch (Exception e) {
            ProcessBuilder pb;
            if (editing != null) {
              str1 = editing.getRegent().getRegent().getPath();
            } else {
              return;
            } 
          } 
          Main.desktop.open((new File(str1)).getParentFile());
        } catch (IOException e) {
          e.printStackTrace();
        } 
        break;
      case 98601:
        if (!str.equals("clr"))
          break; 
        if (editing == null)
          return; 
        ((IDELine)lines.get(cursorY - 1)).getChars().clear();
        ((IDELine)lines.get(cursorY - 1)).getFonts().clear();
        editing.setSaved(false);
        setCursorWithinBounds();
        break;
      case 98618:
        if (!str.equals("cmd"))
          break; 
        try {
          boolean bool = System.getProperty("os.name").toLowerCase().startsWith("windows");
          ProcessBuilder processBuilder = null;
          if (bool) {
            processBuilder = new ProcessBuilder(new String[] { "cmd", "/c", "start" });
          } else {
            processBuilder = new ProcessBuilder(new String[] { "sh", "-c", "start" });
          } 
          File dir = (Explorer.scope != null) ? Explorer.scope.getRegent() : Main.baseFolder;
          processBuilder.directory(dir);
          processBuilder.start();
        } catch (IOException iOException) {
          iOException.printStackTrace();
        } 
        break;
      case 3522941:
        if (!str.equals("save"))
          break; 
        if (editing == null)
          return; 
        editing.save();
        break;
      case 3556460:
        if (!str.equals("term"))
          break; 
        execTerminal();
        break;
      case 106438291:
        if (!str.equals("paste"))
          break; 
        paste();
        break;
      case 1985397299:
        if (!str.equals("setbase"))
          break; 
        Main.baseFolder = new File(Explorer.getScopePath());
        Explorer.folderPath = "";
        ListableFile.files = ListableFile.loadFolder(null);
        break;
    } 
  }
  
  public static void verifyDuplicateTabs() {
    if (tabs == null || tabs.size() == 0)
      return; 
    for (int i = 0; i < tabs.size(); i++) {
      for (int j = 0; j < tabs.size(); j++) {
        Tab tabi = tabs.get(i);
        Tab tabj = tabs.get(j);
        if (tabi.getRegent().getRegent().getAbsolutePath().equals(tabj.getRegent().getRegent().getAbsolutePath()) && tabi != tabj) {
          tabi.close();
          return;
        } 
      } 
    } 
  }
  
  public static <T> List<T> removeAllDuplicates(List<T> list) {
    Set<T> linkedSet = new LinkedHashSet<>();
    linkedSet.addAll(list);
    list.clear();
    list.addAll(linkedSet);
    return list;
  }
  
  public static int ruleOf3(int a, int b, int c) {
    return b * c / a;
  }
  
  public void tick() {
    if (tabs == null)
      tabs = new ArrayList<>(); 
    verifyDuplicateTabs();
    if (!selecting) {
      index1 = cursorX;
      line1 = cursorY;
      index2 = cursorX;
      line2 = cursorY;
    } 
    if (lines.size() > 0 && scrY + (FONT_SIZE + FONT_SIZE / 4) * 3 > (lines.size() + 2) * (FONT_SIZE + FONT_SIZE / 4)) {
      scrY = lines.size() * FONT_SIZE - FONT_SIZE * 3;
      cursorX = ((IDELine)lines.get(lines.size() - 1)).getChars().size();
      cursorY = lines.size();
      setCursorWithinBounds();
    } 
    this.realcx = this.x + 50 + cursorX * (FONT_SIZE - FONT_SIZE / 4) - scrX;
    this.realcy = 35 + cursorY * (FONT_SIZE + FONT_SIZE / 4) - FONT_SIZE - scrY - 2;
    if ((KeyInput.isKeyPressed() && !KeyInput.isControlDown() && !KeyInput.isShiftDown()) || (cursorX != index1 && cursorY != line1 && cursorX != index2 && cursorY != line2))
    	KeyInput.updateKeys();
    	selecting = false; 
    this.drawcx = this.realcx;
    this.drawcy = this.realcy;
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
      } else if (line2 == line1 && 
        index2 < index1) {
        int tempindex1 = index1;
        index1 = index2;
        index2 = tempindex1;
      } 
    } 
    if (editing != null) {
      Main.screen.frame.setTitle(String.valueOf(Main.baseFolder.getName()) + " " + editing.getRegent().getRegent().getName() + " - Boot IDE");
    } else if (Main.baseFolder != null) {
      Main.screen.frame.setTitle(String.valueOf(Main.baseFolder.getName()) + " - Boot IDE");
    } 
    this.showCursorData = false;
    if (KeyInput.isAltDown() && editing != null && hovered()) {
      KeyInput.updateKeys();
      this.showCursorData = true;
    } 
   /* if (KeyInput.isKeyPressed() && hovered() && editing != null) {
      if (KeyInput.getKeyCodePressed() == 90 && KeyInput.isControlDown())
        selectMode = true; 
      if (KeyInput.getKeyCodePressed() == 27) {
        selectMode = false;
        isSelectingFirst = true;
        CommandTerminal.runCommand("deselect");
      } 
    } 
    if (selectMode && leftClicked()) {
      selecting = true;
      MouseInput.updateMouse();
      if (isSelectingFirst) {
        line1 = my;
        index1 = mx;
        isSelectingFirst = false;
      } else {
        line2 = my;
        index2 = mx;
        selectMode = false;
        isSelectingFirst = true;
      } 
    } */
    try {
      clipboard = (String)Main.toolkit.getSystemClipboard().getData(DataFlavor.stringFlavor);
    } catch (HeadlessException|java.awt.datatransfer.UnsupportedFlavorException|IOException|IllegalStateException e) {
      System.err.println("Não é string. Resetando!");
      clipboard = "";
    } 
    if (MouseInput.hovered(this.x, 0, Main.screen.getWidth(), 30) && tabs != null && tabs.size() > 0 && 
      MouseInput.isMouseRolling())
      if (MouseInput.wheelUp() && tabScr < 0) {
        tabScr += 203;
      } else if (MouseInput.wheelDown() && ((Tab)tabs.get(tabs.size() - 1)).getX() + tabScr - 200 > (CommandTerminal.expOff ? 0 : 280)) {
        tabScr -= 203;
      }  
    if (hovered() && editing != null) {
      Main.screen.setCursor(new Cursor(2));
      if (MouseInput.isMouseRolling())
        (new Thread() {
            public void run() {
              if (KeyInput.isShiftDown()) {
            	  KeyInput.updateKeys();
                if (MouseInput.wheelUp() && CodeEditor.scrX > 0) {
                  CodeEditor.scrX -= CodeEditor.FONT_SIZE * 3;
                } else if (MouseInput.wheelDown()) {
                  CodeEditor.scrX += CodeEditor.FONT_SIZE * 3;
                } 
              } else if (MouseInput.wheelUp() && CodeEditor.scrY > 0) {
                CodeEditor.scrY -= (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) * 3;
              } else if (MouseInput.wheelDown() && CodeEditor.scrY + (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) * 3 < CodeEditor.lines.size() * (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4)) {
                CodeEditor.scrY += (CodeEditor.FONT_SIZE + CodeEditor.FONT_SIZE / 4) * 3;
              } 
            }
          }).start(); 
      if (leftClicked() && !RightClickOption.isRightClickActive() && !selectMode) {
        cursorX = mx;
        cursorY = my;
        setCursorWithinBounds();
      } 
    } else {
      Main.screen.setCursor(Cursor.getDefaultCursor());
    } 
    if (rightClicked()) {
      IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY(), 550, "Abrir Prompt de Comando", s -> execute(s), "cmd");
      IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 30, 550, "Abrir Terminal de Comando", s -> execute(s), "term");
      if (Main.baseFolder != null)
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 60, 550, "Abrir no Explorador de Arquivos", s -> execute(s), "sysexp"); 
      if (editing != null) {
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 240 : 150), 550, "Selecionar Linha", s -> CommandTerminal.runCommand(s), "selectline");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 90, 550, "Salvar", s -> execute(s), "save");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 150 : 120), 550, "Colar", s -> execute(s), "paste");
      } 
      if (selecting) {
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 120, 550, "Copiar", s -> CommandTerminal.runCommand(s), "copy");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 180, 550, "Cortar", s -> CommandTerminal.runCommand(s), "cut");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 210, 550, "Deletar", s -> CommandTerminal.runCommand(s), "del");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + 300, 550, "Desselecionar", s -> CommandTerminal.runCommand(s), "deselect");
      } 
      if (Main.baseFolder != null && editing != null) {
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 270 : 180), 550, "Selecionar Tudo", s -> CommandTerminal.runCommand(s), "selectall");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 330 : 210), 550, "Definir pasta atual como Pasta Base", s -> execute(s), "setbase");
        IDEComponent.addRightClickOption(MouseInput.getMouseX(), MouseInput.getMouseY() + (selecting ? 360 : 240), 550, "Abrir arquivo com o programa padrão", s -> execute(s), "opendef");
      }
    }
    if (KeyInput.isKeyPressed() && !SetFileName.added && !CommandTerminal.active && !selectMode) {
    	KeyInput.updateKeys();
      setCursorWithinBounds();
      
      /*
       * new Thread() {
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
    }
       */
    }
      
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 36) {
        KeyInput.updateKeys();
        scrX = 0;
        scrY = 0;
        cursorX = 0;
        cursorY = 0;
        setCursorWithinBounds();
        return;
      }
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 35) {
        KeyInput.updateKeys();
        scrY = lines.size() * FONT_SIZE - FONT_SIZE * 3;
        cursorX = ((IDELine)lines.get(lines.size() - 1)).getChars().size();
        cursorY = lines.size();
        setCursorWithinBounds();
        return;
      } 
      if (KeyInput.getKeyCodePressed() == 36) {
        KeyInput.updateKeys();
        scrX = 0;
        cursorX = 0;
        setCursorWithinBounds();
        return;
      } 
      if (KeyInput.getKeyCodePressed() == 35) {
        KeyInput.updateKeys();
        cursorX = ((IDELine)lines.get(cursorY - 1)).getChars().size();
        setCursorWithinBounds();
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 68) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("deselect");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 77) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("gotocursor");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 88) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("cut");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 75) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("toggleexplorer");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.isAltDown() && KeyInput.getKeyCodePressed() == 84) {
        KeyInput.updateKeys();
        tabs.clear();
        editing = null;
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == 84) {
        KeyInput.updateKeys();
        editing.close();
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 84) {
        KeyInput.updateKeys();
        execute("term");
        return;
      } 
      if ((KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 66) || (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 524)) {
        KeyInput.updateKeys();
        execute("cmd");
        return;
      } 
      if (editing == null)
        return; 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 83) {
        KeyInput.updateKeys();
        editing.save();
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.isShiftDown() && KeyInput.getKeyCodePressed() == 65) {
        KeyInput.updateKeys();
        cursorX = 0;
        cursorY = 1;
        CommandTerminal.runCommand("selectall");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 65) {
        KeyInput.updateKeys();
        cursorX = 0;
        CommandTerminal.runCommand("selectline");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 67) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("copy");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 86) {
        KeyInput.updateKeys();
        paste();
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 127) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("del");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 79) {
        KeyInput.updateKeys();
        CommandTerminal.runCommand("togglesyntaxerrors");
        return;
      } 
      if (KeyInput.isControlDown() && KeyInput.getKeyCodePressed() == 80) {
    	  if (KeyInput.getKeyCodePressed() == KeyEvent.VK_UP) {
				KeyInput.updateKeys();
				
				cursorY--;
				
				setCursorWithinBounds();
				
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
			
			KeyInput.updateKeys();
			
			StringBuilder cY = new StringBuilder(new String(toCharArray( lines.get(cursorY - 1).getChars() )));
			
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_BACK_SPACE) {
				KeyInput.updateKeys();
				
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
				
				return;
			}

			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SHIFT || KeyInput.getKeyCodePressed() == KeyEvent.VK_TAB) return;
			if (KeyInput.getKeyCodePressed() == KeyEvent.VK_SHIFT) return;

			int keyCode = KeyInput.getKeyCodePressed();
			char c = KeyInput.getCharPressed();
			
			c = addAccents(keyCode, c);
			
			cY = write(cY, c);
			cY = addCodeHints(cY);
			
			register(cY, cursorY - 1);
			
			cursorX++;
			
			setCursorWithinBounds();
			
			if (KeyInput.getCharPressed() < 31 || KeyInput.getCharPressed() > 256 || KeyInput.getKeyCodePressed() == KeyEvent.VK_DELETE) return;
		
			editing.setSaved(false);
      }
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
    if (index1 < 0)
      index1 = 0; 
    if (line1 < 1)
      line1 = 1; 
    if (index2 < 0)
      index2 = 0; 
    if (line2 < 1)
      line2 = 1; 
    if (scrX < 0)
      scrX = 0; 
    if (scrY < 0)
      scrY = 0; 
  }
  
  public void render(Graphics g) {
    Graphics2D g2 = (Graphics2D) g;
    
    g.setColor(Colors.explorerLight);
    g2.setStroke(new BasicStroke(8.0F));
    g2.drawLine(this.x, 30, this.width, 30);
    
    g.setColor(Colors.background);
    g.fillRect(this.x, this.y, this.width, this.height);
    
    if (tabs == null || tabs.size() == 0)
      return; 
    
    if (editing != null) {
      g.setColor(Colors.explorer);
      g.fillRect(this.x, 35, Main.screen.getWidth(), this.height);
    } 
    
    try {
      int i;
      for (i = 0; i < lines.size(); i++) {
        if (selecting) {
          g.setColor(Colors.select);
          if (i == line1 - 1)
            if (i == line2 - 1) {
              g.fillRect(this.x + 48 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, (
                  line1 + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 14) ? 5 : 0), 
                  this.x + 48 + index2 * (FONT_SIZE - FONT_SIZE / 4) - scrX - this.x + 48 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, 
                  FONT_SIZE + 4);
            } else {
              g.fillRect(this.x + 48 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, (
                  line1 + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 14) ? 5 : 0), 
                  this.x + 48 + ((IDELine)lines.get(i)).getChars().size() * (FONT_SIZE - FONT_SIZE / 4) - scrX - this.x + 48 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, 
                  FONT_SIZE + 4);
            }  
          if (i > line1 && i < line2)
            g.fillRect(this.x + 38 + FONT_SIZE - FONT_SIZE / 4 - scrX, (
                i + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 14) ? 5 : 0), (
                (IDELine)lines.get(i - 1)).getChars().size() * (FONT_SIZE - FONT_SIZE / 4) - scrX, 
                FONT_SIZE + 4); 
        } 
      } 
      for (i = 0; i < lines.size(); i++) {
        int yr = 35 + i * (FONT_SIZE + FONT_SIZE / 4) - scrY;
        if (yr >= 0 && yr <= Screen.HEIGHT) {
          char[] cs = toCharArray(((IDELine)lines.get(i)).getChars());
          IDEFont[] fs = toArray(((IDELine)lines.get(i)).getFonts());
          if (lines.get(i) == null)
            break; 
          if (35 + i * (FONT_SIZE + FONT_SIZE / 4) - scrY >= 20) {
            if (i == cursorY - 1) {
              g.setColor(Colors.backgroundLight);
              g.fillRect(this.x, 35 + i * (FONT_SIZE + FONT_SIZE / 4) - scrY - 1, Main.screen.getWidth(), FONT_SIZE + FONT_SIZE / 4 + 1);
            } 
            if (selecting) {
              g.setColor(Colors.select);
              if (i == line1 - 1)
                if (i == line2 - 1) {
                  g.fillRect(this.x + 50 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, (
                      line1 + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 14) ? 5 : 0), 
                      this.x + 50 + index2 * (FONT_SIZE - FONT_SIZE / 4) - scrX - this.x + 50 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, 
                      FONT_SIZE + 4);
                } else {
                  g.fillRect(this.x + 50 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, (
                      line1 + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 14) ? 5 : 0), 
                      this.x + 50 + ((IDELine)lines.get(i)).getChars().size() * (FONT_SIZE - FONT_SIZE / 4) - scrX - this.x + 50 + index1 * (FONT_SIZE - FONT_SIZE / 4) - scrX, 
                      FONT_SIZE + 4);
                }  
              if (i == line2 - 1 && 
                i != line1 - 1)
                g.fillRect(this.x + 38 + FONT_SIZE - FONT_SIZE / 4 - scrX, (
                    line2 + 1) * (FONT_SIZE + FONT_SIZE / 4) - scrY - ((FONT_SIZE > 15) ? 5 : 0), 
                    this.x + 38 + (index2 - 29) * (FONT_SIZE - FONT_SIZE / 4) - scrX + Math.abs(14 - FONT_SIZE) * FONT_SIZE + 2, 
                    FONT_SIZE + 4); 
            } 
            IDEFont font = (i == cursorY - 1) ? new IDEFont(Fonts.selectedLineNumberNormal, FONT_SIZE) : new IDEFont(Fonts.lineNumberNormal, FONT_SIZE);
            Fonts.drawString(String.valueOf(i + 1), this.x + 1, 35 + i * (FONT_SIZE + FONT_SIZE / 4) - scrY, font, g);
            Fonts.drawChars(cs, this.x + 50 - scrX, 35 + i * (FONT_SIZE + FONT_SIZE / 4) - scrY, fs, this.x + FONT_SIZE * 2, g);
          } 
        } 
      } 
    } catch (Exception exception) {}
    if (this.showCursorData) {
      KeyInput.updateKeys();
      g.setColor(new Color(0.0F, 0.0F, 0.0F, 0.3F));
      g.fillRect(0, 0, Main.screen.getWidth(), Main.screen.getHeight());
      Fonts.drawString("Posido Cursor:", MouseInput.getMouseX() + 10, MouseInput.getMouseY() - 16 - 5, new IDEFont(Fonts.lighterGrayNormal, 16), g);
      Fonts.drawString("Coluna: " + (cursorX + 1), MouseInput.getMouseX() + 10, MouseInput.getMouseY(), new IDEFont(Fonts.lighterGrayNormal, 16), g);
      Fonts.drawString(" Linha: " + cursorY, MouseInput.getMouseX() + 10, MouseInput.getMouseY() + 16 + 3, new IDEFont(Fonts.lighterGrayNormal, 16), g);
    } 
    if (this.showCursor && cursorY * (FONT_SIZE + FONT_SIZE / 4) - FONT_SIZE - scrY >= -5 && this.x + 50 + cursorX * (FONT_SIZE - FONT_SIZE / 4) - scrX >= this.x + FONT_SIZE * 2 && !WindowInput.isDeactivated()) {
      g.setColor(Colors.cursor);
      g.fillRect(this.drawcx, this.drawcy, 2, FONT_SIZE);
    } 
    if (editing != null) {
      g.setColor(Colors.backgroundLight);
      g.fillRect(this.x, Main.screen.getHeight() - 22, Main.screen.getWidth(), 22);
      Fonts.drawString(String.valueOf(codeType) + " - " + extType, this.x + 10, Main.screen.getHeight() - 20, new IDEFont(Fonts.otherNormal, 16), g);
    } 
    g.setColor(Colors.background);
    g.fillRect(this.x, 0, this.width, 35);
    for (Tab t : tabs)
      t.render(g); 
  }
}