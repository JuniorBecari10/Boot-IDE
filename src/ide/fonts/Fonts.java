package ide.fonts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import ide.codeeditor.CodeEditor;
import ide.util.Colors;
import ide.util.Spritesheet;

/**
 * Tem tudo relacionado a fontes.
 * 
 * @author Juninho
 *
 */
public class Fonts {
	
	/*public static InputStream emojiStream;
	public static Font emojiFont;*/
	
	public static boolean useAntiAliasing = false;
	
    public static BufferedImage[] normal;
    public static BufferedImage[] editor;
    
    public static BufferedImage[] lightGrayNormal;
    public static BufferedImage[] lightGrayEditor;

    public static BufferedImage[] lighterGrayNormal;
    public static BufferedImage[] lighterGrayEditor;

    public static BufferedImage[] keywordsNormal;
    public static BufferedImage[] keywordsEditor;
    
    public static BufferedImage[] numbersNormal;
    public static BufferedImage[] numbersEditor;

    public static BufferedImage[] methodsNormal;
    public static BufferedImage[] methodsEditor;

    public static BufferedImage[] objectsNormal;
    public static BufferedImage[] objectsEditor;

    public static BufferedImage[] commentsNormal;
    public static BufferedImage[] commentsEditor;

    public static BufferedImage[] stringsNormal;
    public static BufferedImage[] stringsEditor;

    public static BufferedImage[] variablesNormal;
    public static BufferedImage[] variablesEditor;
    
    public static BufferedImage[] symbolsNormal;
    public static BufferedImage[] symbolsEditor;
    
    /*public static BufferedImage[] select1Normal;
    public static BufferedImage[] select1Editor;
    
    public static BufferedImage[] select2Normal;
    public static BufferedImage[] select2Editor;*/
    
    public static BufferedImage[] otherNormal;
    public static BufferedImage[] otherEditor;
    
    public static BufferedImage[] errorNormal;
    public static BufferedImage[] errorEditor;
    
    public static BufferedImage[] lineNumberNormal;
    public static BufferedImage[] lineNumberEditor;
    
    public static BufferedImage[] selectedLineNumberNormal;
    public static BufferedImage[] selectedLineNumberEditor;
    
    public static void initFonts(String font1, String font2) {
    	 normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
    	 lightGrayNormal = Fonts.initFont(normal, Color.white, Colors.textLight);
         lightGrayEditor = Fonts.initFont(editor, Color.white, Colors.textLight);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         lighterGrayNormal = Fonts.initFont(normal, Color.white, Colors.textLighter);
         lighterGrayEditor = Fonts.initFont(editor, Color.white, Colors.textLighter);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         keywordsNormal = Fonts.initFont(normal, Color.white, Colors.keywords); // original: (204, 108, 29)
         keywordsEditor = Fonts.initFont(editor, Color.white, Colors.keywords);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         numbersNormal = Fonts.initFont(normal, Color.white, Colors.numbers);
         numbersEditor = Fonts.initFont(editor, Color.white, Colors.numbers);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         methodsNormal = Fonts.initFont(normal, Color.white, Colors.methods);
         methodsEditor = Fonts.initFont(editor, Color.white, Colors.methods);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         objectsNormal = Fonts.initFont(normal, Color.white, Colors.objects);
         objectsEditor = Fonts.initFont(editor, Color.white, Colors.objects);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         commentsNormal = Fonts.initFont(normal, Color.white, Colors.comments);
         commentsEditor = Fonts.initFont(editor, Color.white, Colors.comments);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         stringsNormal = Fonts.initFont(normal, Color.white, Colors.strings);
         stringsEditor = Fonts.initFont(editor, Color.white, Colors.strings);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         variablesNormal = Fonts.initFont(normal, Color.white, Colors.variables);
         variablesEditor = Fonts.initFont(editor, Color.white, Colors.variables);
         
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         symbolsNormal = Fonts.initFont(normal, Color.white, Colors.symbols);
         symbolsEditor = Fonts.initFont(editor, Color.white, Colors.symbols);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
        
         /*select1Normal = Fonts.initFont(normal, Color.white, Colors.select1);
         select1Editor = Fonts.initFont(editor, Color.white, Colors.select1);
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         select2Normal = Fonts.initFont(normal, Color.white, Colors.select2);
         select2Editor = Fonts.initFont(editor, Color.white, Colors.select2);*/
        
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         otherNormal = Fonts.initFont(normal, Color.white, Colors.other);
         otherEditor = Fonts.initFont(editor, Color.white, Colors.other);
         
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         errorNormal = Fonts.initFont(normal, Color.white, Colors.error);
         errorEditor = Fonts.initFont(editor, Color.white, Colors.error);
         
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         lineNumberNormal = Fonts.initFont(normal, Color.white, Colors.lineNumber);
         lineNumberEditor = Fonts.initFont(editor, Color.white, Colors.lineNumber);
         
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
         
         selectedLineNumberNormal = Fonts.initFont(normal, Color.white, Colors.selectedLineNumber);
         selectedLineNumberEditor = Fonts.initFont(editor, Color.white, Colors.selectedLineNumber);
         
         normal = Fonts.initFont(font1);
         editor = Fonts.initFont(font2);
    }
    
    /**
     * A gambiarra nao foi totalmente resolvida. é só uma forma de "esconda"-la.
     * 
     * @param arg - A {@code String} para iniciar
     * @return - A fonte como um array de {@code BufferedImage}.
     */
    /*public static BufferedImage[] refreshFont(String arg) {
    	BufferedImage[] font = new BufferedImage[256];
    	
    	font = initFont(arg);
    	
    	return font;
    }*/
    
    /***/
    public static BufferedImage[] initFont(String path) {
    	BufferedImage[] array = new BufferedImage[256];
    	
        Spritesheet spr = new Spritesheet(path);
        int index = 0;

        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                array[index] = spr.getSprite(x * 8, y * 8, 8, 8);
                
                index++;
            }
        }
        
        return array;
    }
    
    public static BufferedImage[] initFont(BufferedImage[] base, Color target, Color out) {
    	for (int i = 0; i < base.length; i++)
        	base[i] = Colors.swapColor(base[i], target, out);
    	
    	return base;
    }
    
    public static BufferedImage accents(char c, IDEFont font) {
    	BufferedImage text = null;
    	
    	if (c == 'Á') {
			text = font.getFont()[190+2]; // 190 + 2
		}
		else if (c == 'É') {
			text = font.getFont()[197+2];
		}
		else if (c == 'Í') {
			text = font.getFont()[201+2];
		}
		else if (c == 'Ó') {
			text = font.getFont()[207+2];
		}
		else if (c == 'Ú') {
			text = font.getFont()[214+2];
			
			
		}
		else if (c == 'Ý') {
			text = font.getFont()[218+2];
			
			
		}
		else if (c == 'á') {
			text = font.getFont()[222+2];
			
			
		}
		else if (c == 'é') {
			text = font.getFont()[230+2];
			
			
		}
		else if (c == 'í') {
			text = font.getFont()[234+2];
			
			
		}
		else if (c == 'ó') {
			text = font.getFont()[240+2];
			
			
		}
		else if (c == 'ú') {
			text = font.getFont()[247+2];
			
			
		}
		else if (c == 'ý') {
			text = font.getFont()[250+2];
			
			
		}
		/////////
		else if (c == 'À') {
			text = font.getFont()[189+1];
			
			
		}
		else if (c == 'È') {
			text = font.getFont()[196+2];
			
			
		}
		else if (c == 'Ì') {
			text = font.getFont()[200+2];
			
			
		}
		else if (c == 'Ò') {
			text = font.getFont()[206+2];
			
			
		}
		else if (c == 'Ù') {
			text = font.getFont()[213+2];
			
			
		}
		else if (c == 'à') {
			text = font.getFont()[221+2];
			
			
		}
		else if (c == 'è') {
			text = font.getFont()[229+2];
			
			
		}
		else if (c == 'ì') {
			text = font.getFont()[233+2];
			
			
		}
		else if (c == 'ò') {
			text = font.getFont()[239+2];
			
			
		}
		else if (c == 'ù') {
			text = font.getFont()[246+2];
			
			
		}
		//////
		if (c == 'Â') {
			text = font.getFont()[191+2];
			
			
		}
		else if (c == 'Ê') {
			text = font.getFont()[198+2];
			
			
		}
		else if (c == 'Î') {
			text = font.getFont()[202+2];
			
			
		}
		else if (c == 'Ô') {
			text = font.getFont()[208+2];
			
			
		}
		else if (c == 'Û') {
			text = font.getFont()[216+2];
			
			
		}
		else if (c == 'â') {
			text = font.getFont()[223+2];
			
			
		}
		else if (c == 'ê') {
			text = font.getFont()[231+2];
			
			
		}
		else if (c == 'î') {
			text = font.getFont()[235+2];
			
			
		}
		else if (c == 'ô') {
			text = font.getFont()[241+2];
			
			
		}
		else if (c == 'û') {
			text = font.getFont()[248+2];
			
			
		}
		//////
		else if (c == 'Õ') {
			text = font.getFont()[209+2];
			
			
		}
		else if (c == 'Ñ') {
			text = font.getFont()[205+2];
			
			
		}
		else if (c == 'ã') {
			text = font.getFont()[224+2];
			
			
		}
		else if (c == 'Ã') {
			text = font.getFont()[192+2];
			
			
		}
		else if (c == 'õ') {
			text = font.getFont()[242+2];
			
			
		}
		else if (c == 'ñ') {
			text = font.getFont()[238+2];
			
			
		}
		
		else if (c == 'ç') {
    		text = font.getFont()[228+2];
    			
    		
		}
		
		else if (c == 'Ç') {
    		text = font.getFont()[195+2];
    			
    		
		}
		
		else if (c == 'µ') {
    		text = font.getFont()[180];
    			
    		
		}
		
		return text;
    }
    
    public static boolean isAccent(char c) {
    	return c == 'Á' || c == 'É' || c == 'Í' || c == 'Ó' || c == 'Ú' || c == 'Ý'
    		|| c == 'á' || c == 'é' || c == 'í' || c == 'ó' || c == 'ú' || c == 'ý'
    		|| c == 'À' || c == 'È' || c == 'Ì' || c == 'Ò' || c == 'Ù'
    		|| c == 'à' || c == 'è' || c == 'ì' || c == 'ò' || c == 'ù'
    		|| c == 'Â' || c == 'Ê' || c == 'Î' || c == 'Ô' || c == 'Û'
    		|| c == 'â' || c == 'ê' || c == 'î' || c == 'ô' || c == 'û'
    		|| c == 'Ã' || c == 'Õ' || c == 'Ñ'
    		|| c == 'ã' || c == 'õ' || c == 'ñ'
    		|| c == 'ç' || c == 'Ç';
    }
    
    /**
     * Retorna true ou false se o char em c pertence a tabela Ascii.
     * Note que no return deveria ser 128, mas essa a a Ascii estendida.
     * 
     * @param c - O char
     * @return true ou false se o char em c pertence a tabela Ascii.
     *
    public static boolean isAscii(char c) {
    	return c < 200;
    }*/
    
    /**
     * Desenha um texto na tela de acordo com a posiaao, o tamanho e a fonte especificados.
     * 
     * <pre>Como foi feito?</pre>
     * 
     * O matodo dividiu a {@code String} em um array de {@code char}, depois criou outro array de {@code BufferedImage}
     * e associa cada posiaao ao valor da tabela ASCII definido pelo array, pois um {@code int} pode ser representado por um
     * {@code char} no Java, depois roda um loop e desenha. Simples, ou nao?
     * 
     * @param s - O texto.
     * @param x - A posição x.
     * @param y - A posição y.
     * @param font - A fonte.
     * @param g -  O objeto {@code Graphics} (use o g do render).
     * 
     * @author Boot
     */
	public static void drawString(String s, int x, int y, IDEFont font, Graphics g) {
		if (s == null) throw new NullPointerException("A String não pode ser nula!");
		Graphics2D g2 = (Graphics2D) g;
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		
    		if (ind >= font.getFont().length)
    			continue;
    				
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < ca.length; i++) {
    		char ch = ca[i];
    		
    		if (ch > 255) continue;
    		
    		if (isAccent(ch))
    			text[i] = accents(ch, font);
    		else
    			text[i] = font.getFont()[ch];
    		
    		// sigma
    		if (ch == 8721) {
        		text[i] = font.getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == CodeEditor.BLACK_CIRCLE) {
    			text[i] = font.getFont()[128];
    			
    			continue;
    		}
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray();
    		char ch = cha[i];
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == 'ý' || ch == 'j' || ch == ',' || ch == ';' || ch == 'ç' || ch == 'Ç' ? y + 2 : y;
    		
    		if (font.getSize() % 8 != 0 && useAntiAliasing) {
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    		g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
    		}
    		else {
    			g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    		}
    	}
    }
	
	public static void drawString(String s, int x, int y, IDEFont font, boolean useAntiAliasing, Graphics g) {
		if (s == null) throw new NullPointerException("A String não pode ser nula!");
		Graphics2D g2 = (Graphics2D) g;
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		
    		if (ind >= font.getFont().length)
    			continue;
    				
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < ca.length; i++) {
    		char ch = ca[i];
    		
    		if (ch > 255) continue;
    		
    		if (isAccent(ch))
    			text[i] = accents(ch, font);
    		else
    			text[i] = font.getFont()[ch];
    		
    		// sigma
    		if (ch == 8721) {
        		text[i] = font.getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == CodeEditor.BLACK_CIRCLE) {
    			text[i] = font.getFont()[128];
    			
    			continue;
    		}
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray();
    		char ch = cha[i];
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == 'ý' || ch == 'j' || ch == ',' || ch == ';' || ch == 'ç' || ch == 'Ç' ? y + 2 : y;
    		
    		if (font.getSize() % 8 != 0 && useAntiAliasing) {
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    		g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
    		}
    		else {
    			g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    		}
    	}
    }
	
	public static void drawString(String s, int x, int y, IDEFont font, int maxPos, Graphics g) {
		if (s == null) throw new NullPointerException("A String nao pode ser nula!");
		Graphics2D g2 = (Graphics2D) g;
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		if (ind >= font.getFont().length) continue;    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < ca.length; i++) {
    		char ch = ca[i];
    		
    		if (ch > 255) continue;
    		
    		if (isAccent(ch))
    			text[i] = accents(ch, font);
    		else
    			text[i] = font.getFont()[ch];
    		
    		// sigma
    		if (ch == 8721) {
        		text[i] = font.getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == CodeEditor.BLACK_CIRCLE) {
    			text[i] = font.getFont()[128];
    			
    			continue;
    		}
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray();
    		char ch = cha[i];
    		
    		if ((x + ((font.getSize() - (font.getSize() / 4)) * i)) > maxPos - font.getSize()) break;
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == 'ý' || ch == 'j' || ch == ',' || ch == ';' || ch == 'ç' || ch == 'Ç' ? y + 2 : y;
    		
    		if (font.getSize() % 8 != 0 && useAntiAliasing) {
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    		g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
    		}
    		else {
    			g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    		}
    	}
    }
	
	public static void drawString(String s, int x, int y, IDEFont font, int minPos, int maxPos, Graphics g) {
		if (s == null) throw new NullPointerException("A String nao pode ser nula!");
		Graphics2D g2 = (Graphics2D) g;
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		if (ind >= font.getFont().length) continue;    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < ca.length; i++) {
    		char ch = ca[i];
    		
    		if (ch > 255) continue;
    		
    		if (isAccent(ch))
    			text[i] = accents(ch, font);
    		else
    			text[i] = font.getFont()[ch];
    		
    		// sigma
    		if (ch == 8721) {
        		text[i] = font.getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == CodeEditor.BLACK_CIRCLE) {
    			text[i] = font.getFont()[128];
    			
    			continue;
    		}
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray();
    		char ch = cha[i];
    		
    		if ((x + ((font.getSize() - (font.getSize() / 4)) * i)) < minPos) continue;
    		if ((x + ((font.getSize() - (font.getSize() / 4)) * i)) > maxPos - font.getSize()) break;
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == 'ý' || ch == 'j' || ch == ',' || ch == ';' || ch == 'ç' || ch == 'Ç' ? y + 2 : y;
    		
    		if (font.getSize() % 8 != 0 && useAntiAliasing) {
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    		g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
    		}
    		else {
    			g2.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    		}
    	}
    }
	
	/**
	 * Desenha um array de {@code char} na tela.
	 * 
	 * @param c - O array
	 * @param x - A posiaao x inicial.
	 * @param y - A posiaao y inicial.
	 * @param fonts - O array das fontes
	 * @param g - O parametro {@code Graphics}.
	 */
	public static void drawChars(char[] c, int x, int y, IDEFont[] fonts, int minX, int maxX, Graphics g) {
		if (c == null) throw new NullPointerException("O array de chars não pode ser nulo!");
		Graphics2D g2 = (Graphics2D) g;
		
    	BufferedImage[] text = new BufferedImage[c.length];
    	
    	// Set images in array text
    	for (int i = 0; i < c.length; i++) {
    		char ch = c[i];
    		
    		if (ch > 255) continue;
    		
    		if (isAccent(ch))
    			text[i] = accents(ch, fonts[i]);
    		else
    			text[i] = fonts[i].getFont()[ch];
    		
    		// sigma
    		if (ch == 8721) {
        		text[i] = fonts[i].getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == CodeEditor.BLACK_CIRCLE) {
    			text[i] = fonts[i].getFont()[128];
    			
    			continue;
    		}
    		
    		//System.out.println(CodeEditor.findIndex(fonts[i].getFont(), text[i]));
    		
    		if (CodeEditor.showWhitespace) {
    			if (ch == ' ') {
    				text[i] = fonts[i].getFont()[129];
        			
        			continue;
    			}
    			else if (ch == '\t') {
    				text[i] = fonts[i].getFont()[130];
        			
        			continue;
    			}
    		}
    	}
    	
    	// Render images
    	for (int i = 0; i < text.length; i++) {
    		char ch = c[i];
    		
    		if ((x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) < minX) continue; // TODO talvez aumentar ou diminuir o espaaamento entre letras por parte do usuario, ou nao sla
    		if ((x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) > maxX) break;
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g'  || ch == 'y' || ch == 'ý' || ch == 'j' || ch == ',' || ch == ';' || ch == 'ç' || ch == 'Ç' ? y + CodeEditor.FONT_SIZE / 8 : y;
    		
    		BufferedImage chr = text[i];
    		
    		/*if (chr == null) {
    			g.setColor(fonts[i].getColor());
    			emojiFont = emojiFont.deriveFont((float) CodeEditor.FONT_SIZE);
    			g.setFont(emojiFont);
    			g.drawString(Character.toString(ch), (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)), ydraw);
    		}*/
    		if (chr == null) {
    			chr = fonts[i].getFont()[127];
    		}
    		
    		if (CodeEditor.FONT_SIZE % 8 != 0 && useAntiAliasing) {
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY));
	    		g2.drawImage(chr, (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)), ydraw, fonts[i].getSize() + ((ch == 'i' || ch == ',' || ch == ';' || ch == '|') && (CodeEditor.FONT_SIZE == 14 || CodeEditor.FONT_SIZE == 13) ? 1 : 0), fonts[i].getSize(), null);
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF));
	    		g2.setRenderingHints(new RenderingHints(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_DEFAULT));
    		}
    		else {
    			g2.drawImage(chr, (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)), ydraw, fonts[i].getSize() + ((ch == 'i' || ch == ',' || ch == ';' || ch == '|') && (CodeEditor.FONT_SIZE == 14 || CodeEditor.FONT_SIZE == 13) ? 1 : 0), fonts[i].getSize(), null);
    		}
    	}
    }
}
