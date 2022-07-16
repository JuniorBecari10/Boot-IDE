package ide.fonts;

import java.awt.Color;
import java.awt.Graphics;
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

    public static BufferedImage[] normal;
    public static BufferedImage[] bold;
    
    public static BufferedImage[] lightGrayNormal;
    public static BufferedImage[] lightGrayBold;

    public static BufferedImage[] lighterGrayNormal;
    public static BufferedImage[] lighterGrayBold;

    public static BufferedImage[] keywordsNormal;
    public static BufferedImage[] keywordsBold;
    
    public static BufferedImage[] numbersNormal;
    public static BufferedImage[] numbersBold;

    public static BufferedImage[] methodsNormal;
    public static BufferedImage[] methodsBold;

    public static BufferedImage[] objectsNormal;
    public static BufferedImage[] objectsBold;

    public static BufferedImage[] commentsNormal;
    public static BufferedImage[] commentsBold;

    public static BufferedImage[] stringsNormal;
    public static BufferedImage[] stringsBold;

    public static BufferedImage[] variablesNormal;
    public static BufferedImage[] variablesBold;
    
    public static BufferedImage[] symbolsNormal;
    public static BufferedImage[] symbolsBold;
    
    /*public static BufferedImage[] select1Normal;
    public static BufferedImage[] select1Bold;
    
    public static BufferedImage[] select2Normal;
    public static BufferedImage[] select2Bold;*/
    
    public static BufferedImage[] otherNormal;
    public static BufferedImage[] otherBold;
    
    public static BufferedImage[] errorNormal;
    public static BufferedImage[] errorBold;
    
    public static BufferedImage[] lineNumberNormal;
    public static BufferedImage[] lineNumberBold;
    
    public static BufferedImage[] selectedLineNumberNormal;
    public static BufferedImage[] selectedLineNumberBold;
    
    public static BufferedImage unknown;
    
    public static void initFonts(String font1, String font2) {
    	 normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
    	 lightGrayNormal = Fonts.initFont(normal, Color.white, Colors.textLight);
         lightGrayBold = Fonts.initFont(bold, Color.white, Colors.textLight);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         lighterGrayNormal = Fonts.initFont(normal, Color.white, Colors.textLighter);
         lighterGrayBold = Fonts.initFont(bold, Color.white, Colors.textLighter);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         keywordsNormal = Fonts.initFont(normal, Color.white, Colors.keywords); // original: (204, 108, 29)
         keywordsBold = Fonts.initFont(bold, Color.white, Colors.keywords);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         numbersNormal = Fonts.initFont(normal, Color.white, Colors.numbers);
         numbersBold = Fonts.initFont(bold, Color.white, Colors.numbers);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         methodsNormal = Fonts.initFont(normal, Color.white, Colors.methods);
         methodsBold = Fonts.initFont(bold, Color.white, Colors.methods);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         objectsNormal = Fonts.initFont(normal, Color.white, Colors.objects);
         objectsBold = Fonts.initFont(bold, Color.white, Colors.objects);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         commentsNormal = Fonts.initFont(normal, Color.white, Colors.comments);
         commentsBold = Fonts.initFont(bold, Color.white, Colors.comments);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         stringsNormal = Fonts.initFont(normal, Color.white, Colors.strings);
         stringsBold = Fonts.initFont(bold, Color.white, Colors.strings);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         variablesNormal = Fonts.initFont(normal, Color.white, Colors.variables);
         variablesBold = Fonts.initFont(bold, Color.white, Colors.variables);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         symbolsNormal = Fonts.initFont(normal, Color.white, Colors.symbols);
         symbolsBold = Fonts.initFont(bold, Color.white, Colors.symbols);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
        
         /*select1Normal = Fonts.initFont(normal, Color.white, Colors.select1);
         select1Bold = Fonts.initFont(bold, Color.white, Colors.select1);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         select2Normal = Fonts.initFont(normal, Color.white, Colors.select2);
         select2Bold = Fonts.initFont(bold, Color.white, Colors.select2);*/
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         otherNormal = Fonts.initFont(normal, Color.white, Colors.other);
         otherBold = Fonts.initFont(bold, Color.white, Colors.other);
         
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         errorNormal = Fonts.initFont(normal, Color.white, Colors.error);
         errorBold = Fonts.initFont(bold, Color.white, Colors.error);
         
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         lineNumberNormal = Fonts.initFont(normal, Color.white, Colors.lineNumber);
         lineNumberBold = Fonts.initFont(bold, Color.white, Colors.lineNumber);
         
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         selectedLineNumberNormal = Fonts.initFont(normal, Color.white, Colors.selectedLineNumber);
         selectedLineNumberBold = Fonts.initFont(bold, Color.white, Colors.selectedLineNumber);
         
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         unknown = normal[127];
    }
    
    /**
     * A gambiarra n�o foi totalmente resolvida. � s� uma forma de "escond�"-la.
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
    
    /**
     * Retorna true ou false se o char em c pertence � tabela Ascii.
     * Note que no return deveria ser 128, mas essa � a Ascii estendida.
     * 
     * @param c - O char
     * @return true ou false se o char em c pertence � tabela Ascii.
     *
    public static boolean isAscii(char c) {
    	return c < 200;
    }*/
    
    /**
     * Desenha um texto na tela de acordo com a posi��o, o tamanho e a fonte especificados.
     * 
     * <pre>Como foi feito?</pre>
     * 
     * O m�todo dividiu a {@code String} em um array de {@code char}, depois criou outro array de {@code BufferedImage}
     * e associa cada posi��o ao valor da tabela ASCII definido pelo array, pois um {@code int} pode ser representado por um
     * {@code char} no Java, depois roda um loop e desenha. Simples, ou n�o?
     * 
     * @param s - O texto.
     * @param x - A posi��o x.
     * @param y - A posi��o y.
     * @param font - A fonte.
     * @param g -  O objeto {@code Graphics} (use o g do render).
     * 
     * @author Boot
     */
	public static void drawString(String s, int x, int y, IDEFont font, Graphics g) {
		if (s == null) throw new NullPointerException("A String n�o pode ser nula!");
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		
    		if (ind >= font.getFont().length)
    			continue;
    				
    		text[i] = font.getFont()[ind];
    	}
    	
    	// acento - maiusculas
    	
    	for (int i = 0; i < ca.length; i++) {
    		char cah = ca[i];
    		
    		if (cah == 193) {
    			text[i] = font.getFont()[190+2];
    			
    			continue;
    		}
    		else if (cah == 201) {
    			text[i] = font.getFont()[197+2];
    			
    			continue;
    		}
    		else if (cah == 205) {
    			text[i] = font.getFont()[201+2];
    			
    			continue;
    		}
    		else if (cah == 211) {
    			text[i] = font.getFont()[207+2];
    			
    			continue;
    		}
    		else if (cah == 218) {
    			text[i] = font.getFont()[214+2];
    			
    			continue;
    		}
    		else if (cah == 221) {
    			text[i] = font.getFont()[218+2];
    			
    			continue;
    		}
    		
    		// acento - minusculas
    		
    		else if (cah == 225) {
    			text[i] = font.getFont()[222+2];
    			
    			continue;
    		}
    		else if (cah == 233) {
    			text[i] = font.getFont()[230+2];
    			
    			continue;
    		}
    		else if (cah == 237) {
    			text[i] = font.getFont()[234+2];
    			
    			continue;
    		}
    		else if (cah == 243) {
    			text[i] = font.getFont()[240+2];
    			
    			continue;
    		}
    		else if (cah == 250) {
    			text[i] = font.getFont()[247+2];
    			
    			continue;
    		}
    		else if (cah == 253) {
    			text[i] = font.getFont()[250+2];
    			
    			continue;
    		}
    		
    		// --- crase - maiusculas ---
    		
    		else if (cah == 192) {
    			text[i] = font.getFont()[189+1];
    			
    			continue;
    		}
    		else if (cah == 200) {
    			text[i] = font.getFont()[196+2];
    			
    			continue;
    		}
    		else if (cah == 204) {
    			text[i] = font.getFont()[200+2];
    			
    			continue;
    		}
    		else if (cah == 210) {
    			text[i] = font.getFont()[206+2];
    			
    			continue;
    		}
    		else if (cah == 217) {
    			text[i] = font.getFont()[213+2];
    			
    			continue;
    		}
    		
    		// crase - minusculas
    		
    		else if (cah == 224) {
    			text[i] = font.getFont()[221+2];
    			
    			continue;
    		}
    		else if (cah == 232) {
    			text[i] = font.getFont()[229+2];
    			
    			continue;
    		}
    		else if (cah == 236) {
    			text[i] = font.getFont()[233+2];
    			
    			continue;
    		}
    		else if (cah == 242) {
    			text[i] = font.getFont()[239+2];
    			
    			continue;
    		}
    		else if (cah == 249) {
    			text[i] = font.getFont()[246+2];
    			
    			continue;
    		}
    		
    		// --- circunflexo - maiusculas
    		
    		if (cah == 194) {
    			text[i] = font.getFont()[191+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[198+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[202+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[208+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[216+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[223+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[231+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[235+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[241+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[248+2];
    			
    			continue;
    		}
    		//////
    		else if (cah == '�') {
    			text[i] = font.getFont()[209+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[205+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[224+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[192+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[242+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[238+2];
    			
    			continue;
    		}
    		
    		else if (cah == '�') {
        		text[i] = font.getFont()[228+2];
        			
        		continue;
    		}
    		
    		else if (cah == '�') {
        		text[i] = font.getFont()[195+2];
        			
        		continue;
    		}
    		
    		else if (cah == '�') {
        		text[i] = font.getFont()[180];
        			
        		continue;
    		}
    		
    		int ind = ca[i]; 						// pega o valor na tabela ASCII
    		
    		if (ind > 225) continue;
    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray();
    		char ch = cha[i];
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == '�' || ch == 'j' || ch == ',' || ch == ';' || ch == '�' || ch == '�' ? y + 2 : y;
    		
    		g.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    	}
    }
	
	public static void drawString(String s, int x, int y, IDEFont font, int maxPos, Graphics g) {
		if (s == null) throw new NullPointerException("A String n�o pode ser nula!");
		
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		if (ind >= font.getFont().length) continue;    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < ca.length; i++) {
    		char cah = ca[i];
    		
    		if (cah == '�') {
    			text[i] = font.getFont()[190+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[197+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[201+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[207+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[214+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[218+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[222+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[230+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[234+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[240+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[247+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[250+2];
    			
    			continue;
    		}
    		/////////
    		else if (cah == '�') {
    			text[i] = font.getFont()[189+1];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[196+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[200+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[206+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[213+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[221+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[229+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[233+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[239+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[246+2];
    			
    			continue;
    		}
    		//////
    		if (cah == '�') {
    			text[i] = font.getFont()[191+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[198+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[202+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[208+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[216+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[223+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[231+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[235+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[241+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[248+2];
    			
    			continue;
    		}
    		//////
    		else if (cah == '�') {
    			text[i] = font.getFont()[209+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[205+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[224+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[192+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[242+2];
    			
    			continue;
    		}
    		else if (cah == '�') {
    			text[i] = font.getFont()[238+2];
    			
    			continue;
    		}
    		
    		else if (cah == '�') {
        		text[i] = font.getFont()[228+2];
        			
        		continue;
    		}
    		
    		else if (cah == '�') {
        		text[i] = font.getFont()[195+2];
        			
        		continue;
    		}
    		
    		
    		int ind = ca[i]; 						// pega o valor na tabela ASCII
    		
    		if (ind > 225) continue;
    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		char[] cha = s.toCharArray(); // fazer arruma��o de � e acentos
    		char ch = cha[i];
    		
    		if ((x + ((font.getSize() - (font.getSize() / 4)) * i)) > maxPos - font.getSize()) break;
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g' || ch == 'y' || ch == '�' || ch == 'j' || ch == ',' || ch == ';' || ch == '�' || ch == '�' ? y + 2 : y;
    		
    		g.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), ydraw, font.getSize(), font.getSize(), null);
    	}
    }
	
	/**
	 * Desenha um array de {@code char} na tela.
	 * 
	 * @param c - O array
	 * @param x - A posi��o x inicial.
	 * @param y - A posi��o y inicial.
	 * @param fonts - O array das fontes
	 * @param g - O par�metro {@code Graphics}.
	 */
	public static void drawChars(char[] c, int x, int y, IDEFont[] fonts, int minX, int maxX, Graphics g) {
		if (c == null) throw new NullPointerException("O array de chars n�o pode ser nulo!");
		
    	BufferedImage[] text = new BufferedImage[c.length];
    	
    	for (int i = 0; i < c.length; i++) {
    		char ch = c[i];
    		
    		if (ch == '�') {
    			text[i] = fonts[i].getFont()[190+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[197+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[201+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[207+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[214+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[218+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[222+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[230+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[234+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[240+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[247+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[250+2];
    			
    			continue;
    		}
    		/////////
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[189+1];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[196+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[200+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[206+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[213+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[221+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[229+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[233+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[239+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[246+2];
    			
    			continue;
    		}
    		//////
    		if (ch == '�') {
    			text[i] = fonts[i].getFont()[191+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[198+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[202+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[208+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[216+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[223+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[231+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[235+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[241+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[248+2];
    			
    			continue;
    		}
    		//////
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[209+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[205+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[224+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[192+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[242+2];
    			
    			continue;
    		}
    		else if (ch == '�') {
    			text[i] = fonts[i].getFont()[238+2];
    			
    			continue;
    		}
    		
    		else if (ch == '�') {
        		text[i] = fonts[i].getFont()[228+2];
        			
        		continue;
    		}
    		
    		else if (ch == '�') {
        		text[i] = fonts[i].getFont()[195+2];
        			
        		continue;
    		}
    		
    		else if (ch == '�') {
        		text[i] = fonts[i].getFont()[39];
        			
        		continue;
    		}
    		
    		else if (ch == '�') {
        		text[i] = fonts[i].getFont()[180];
        			
        		continue;
    		}
    		
    		else if (ch == 8721) {
        		text[i] = fonts[i].getFont()[255];
        			
        		continue;
    		}
    		
    		else if (ch == 8226) {
    			text[i] = fonts[i].getFont()[128];
    			
    			continue;
    		}
    		
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
    		
    		int ind = c[i]; 						// pega o valor na tabela ASCII
    		
    		if (ind > 225) continue;
    		
    		text[i] = fonts[i].getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++) {
    		char ch = c[i];
    		
    		if ((x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) < minX) continue; // TODO talvez aumentar ou diminuir o espa�amento entre letras por parte do usu�rio, ou n�o sla
    		if ((x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) > maxX) break;
    		
    		int ydraw = ch == 'p' || ch == 'q' || ch == 'g'  || ch == 'y' || ch == '�' || ch == 'j' || ch == ',' || ch == ';' || ch == '�' || ch == '�' ? y + (CodeEditor.FONT_SIZE < 14 ? 1 : 2) : y;
    		
    		BufferedImage chr = text[i];
    		//BufferedImage shadow = Fonts.otherNormal[i];
    		//if ((i < 33 || (i > 126 && i < 161) || i > 255) && i != 8721) chr = unknown;
    		
    		//g.drawImage(shadow, (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) + (int) (Math.ceil(CodeEditor.FONT_SIZE / 16)), ydraw + (int) (Math.ceil(CodeEditor.FONT_SIZE / 16)), fonts[i].getSize() + ((ch == 'i' || ch == ',' || ch == ';') && (CodeEditor.FONT_SIZE == 14 || CodeEditor.FONT_SIZE == 13) ? 1 : 0), fonts[i].getSize(), null);
    		g.drawImage(chr, (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)), ydraw, fonts[i].getSize() + ((ch == 'i' || ch == ',' || ch == ';' || ch == '|') && (CodeEditor.FONT_SIZE == 14 || CodeEditor.FONT_SIZE == 13) ? 1 : 0), fonts[i].getSize(), null);
    			
    	}
    }
}
