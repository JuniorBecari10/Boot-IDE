package ide.fonts;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

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
    
    public static BufferedImage[] select1Normal;
    public static BufferedImage[] select1Bold;
    
    public static BufferedImage[] select2Normal;
    public static BufferedImage[] select2Bold;
    
    public static BufferedImage[] otherNormal;
    public static BufferedImage[] otherBold;
    
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
        
         select1Normal = Fonts.initFont(normal, Color.white, Colors.select1);
         select1Bold = Fonts.initFont(bold, Color.white, Colors.select1);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         select2Normal = Fonts.initFont(normal, Color.white, Colors.select2);
         select2Bold = Fonts.initFont(bold, Color.white, Colors.select2);
        
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
         
         otherNormal = Fonts.initFont(normal, Color.white, Colors.other);
         otherBold = Fonts.initFont(bold, Color.white, Colors.other);
         
         normal = Fonts.initFont(font1);
         bold = Fonts.initFont(font2);
    }
    
    /**
     * A gambiarra não foi totalmente resolvida. É só uma forma de "escondê"-la.
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
     * Desenha um texto na tela de acordo com a posição, o tamanho e a fonte especificados.
     * 
     * <pre>Como foi feito?</pre>
     * 
     * O método dividiu a {@code String} em um array de {@code char}, depois criou outro array de {@code BufferedImage}
     * e associa cada posição ao valor da tabela ASCII definido pelo array, pois um {@code int} pode ser representado por um
     * {@code char} no Java, depois roda um loop e desenha. Simples, ou não?
     * 
     * @param s - O texto.
     * @param x - A posição x.
     * @param y - A posição y.
     * @param font - A fonte.
     * @param g -  O objeto {@code Graphics} (use o g do render).
     * 
     * @author Boot
     */
	public static void drawString(String s, int x, int y, IDEFont font, Graphics g) { ///// continuar arrumando o texto pra branco e arrumar o explorador e adicionar guias.
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++)										// roda um loop para desenhar.
    		g.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), y, font.getSize(), font.getSize(), null);
    }
	
	public static void drawString(String s, int x, int y, IDEFont font, int maxPos, Graphics g) {
    	char[] ca = s.toCharArray(); // ca = char array								   converte a string em um char array
    	
    	BufferedImage[] text = new BufferedImage[ca.length];						// declara o array das imagens
    	
    	for (int i = 0; i < ca.length; i++) {										// roda um loop for para associar as
    		int ind = ca[i] > 126 ? ca[i] - 3 : ca[i];								// imagens ao array
    		
    		text[i] = font.getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++) {										// roda um loop para desenhar.
    		if ((x + ((font.getSize() - (font.getSize() / 4)) * i)) > maxPos - font.getSize()) break;
    		
    		g.drawImage(text[i], (x + ((font.getSize() - (font.getSize() / 4)) * i)), y, font.getSize(), font.getSize(), null);
    	}
    }
	
	/**
	 * Desenha um array de {@code char} na tela.
	 * 
	 * @param c - O array
	 * @param x - A posição x inicial.
	 * @param y - A posição y inicial.
	 * @param fonts - O array das fontes
	 * @param g - O parâmetro {@code Graphics}.
	 */
	public static void drawChars(char[] c, int x, int y, IDEFont[] fonts, int minX, Graphics g) {
    	BufferedImage[] text = new BufferedImage[c.length];
    	
    	for (int i = 0; i < c.length; i++) {
    		char ch = c[i];
    		
    		if (ch == 'Á') {
    			text[i] = fonts[i].getFont()[190];
    			
    			continue;
    		}
    		else if (ch == 'É') {
    			text[i] = fonts[i].getFont()[197];
    			
    			continue;
    		}
    		else if (ch == 'Í') {
    			text[i] = fonts[i].getFont()[201];
    			
    			continue;
    		}
    		else if (ch == 'Ó') {
    			text[i] = fonts[i].getFont()[207];
    			
    			continue;
    		}
    		else if (ch == 'Ú') {
    			text[i] = fonts[i].getFont()[214];
    			
    			continue;
    		}
    		else if (ch == 'Ý') {
    			text[i] = fonts[i].getFont()[218];
    			
    			continue;
    		}
    		else if (ch == 'á') {
    			text[i] = fonts[i].getFont()[222];
    			
    			continue;
    		}
    		else if (ch == 'é') {
    			text[i] = fonts[i].getFont()[230];
    			
    			continue;
    		}
    		else if (ch == 'í') {
    			text[i] = fonts[i].getFont()[234];
    			
    			continue;
    		}
    		else if (ch == 'ó') {
    			text[i] = fonts[i].getFont()[240];
    			
    			continue;
    		}
    		else if (ch == 'ú') {
    			text[i] = fonts[i].getFont()[247];
    			
    			continue;
    		}
    		else if (ch == 'ý') {
    			text[i] = fonts[i].getFont()[250];
    			
    			continue;
    		}
    		/////////
    		else if (ch == 'À') {
    			text[i] = fonts[i].getFont()[189];
    			
    			continue;
    		}
    		else if (ch == 'È') {
    			text[i] = fonts[i].getFont()[196];
    			
    			continue;
    		}
    		else if (ch == 'Ì') {
    			text[i] = fonts[i].getFont()[200];
    			
    			continue;
    		}
    		else if (ch == 'Ò') {
    			text[i] = fonts[i].getFont()[206];
    			
    			continue;
    		}
    		else if (ch == 'Ù') {
    			text[i] = fonts[i].getFont()[213];
    			
    			continue;
    		}
    		else if (ch == 'à') {
    			text[i] = fonts[i].getFont()[221];
    			
    			continue;
    		}
    		else if (ch == 'è') {
    			text[i] = fonts[i].getFont()[229];
    			
    			continue;
    		}
    		else if (ch == 'ì') {
    			text[i] = fonts[i].getFont()[233];
    			
    			continue;
    		}
    		else if (ch == 'ò') {
    			text[i] = fonts[i].getFont()[239];
    			
    			continue;
    		}
    		else if (ch == 'ù') {
    			text[i] = fonts[i].getFont()[246];
    			
    			continue;
    		}
    		//////
    		if (ch == 'Â') {
    			text[i] = fonts[i].getFont()[191];
    			
    			continue;
    		}
    		else if (ch == 'Ê') {
    			text[i] = fonts[i].getFont()[198];
    			
    			continue;
    		}
    		else if (ch == 'Î') {
    			text[i] = fonts[i].getFont()[202];
    			
    			continue;
    		}
    		else if (ch == 'Ô') {
    			text[i] = fonts[i].getFont()[208];
    			
    			continue;
    		}
    		else if (ch == 'Û') {
    			text[i] = fonts[i].getFont()[216];
    			
    			continue;
    		}
    		else if (ch == 'â') {
    			text[i] = fonts[i].getFont()[223];
    			
    			continue;
    		}
    		else if (ch == 'ê') {
    			text[i] = fonts[i].getFont()[231];
    			
    			continue;
    		}
    		else if (ch == 'î') {
    			text[i] = fonts[i].getFont()[235];
    			
    			continue;
    		}
    		else if (ch == 'ô') {
    			text[i] = fonts[i].getFont()[241];
    			
    			continue;
    		}
    		else if (ch == 'û') {
    			text[i] = fonts[i].getFont()[248];
    			
    			continue;
    		}
    		//////
    		else if (ch == 'Õ') {
    			text[i] = fonts[i].getFont()[209];
    			
    			continue;
    		}
    		else if (ch == 'Ñ') {
    			text[i] = fonts[i].getFont()[205];
    			
    			continue;
    		}
    		else if (ch == 'ã') {
    			text[i] = fonts[i].getFont()[224];
    			
    			continue;
    		}
    		else if (ch == 'Ã') {
    			text[i] = fonts[i].getFont()[192];
    			
    			continue;
    		}
    		else if (ch == 'õ') {
    			text[i] = fonts[i].getFont()[242];
    			
    			continue;
    		}
    		else if (ch == 'ñ') {
    			text[i] = fonts[i].getFont()[238];
    			
    			continue;
    		}
    		
    		else if (ch == 'ç') {
        		text[i] = fonts[i].getFont()[228];
        			
        		continue;
    		}
    		
    		else if (ch == 'Ç') {
        		text[i] = fonts[i].getFont()[195];
        			
        		continue;
    		}
    		
    		
    		int ind = c[i]; 						// pega o valor na tabela ASCII
    		
    		if (ind > 225) continue;
    		
    		text[i] = fonts[i].getFont()[ind];
    	}
    	
    	for (int i = 0; i < text.length; i++) {
    		if ((x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)) < minX) continue; // TODO talvez aumentar ou diminuir o espaçamento entre letras por parte do usuário, ou não sla
    		
    		g.drawImage(text[i], (x + ((fonts[i].getSize() - (fonts[i].getSize() / 4)) * i)), y, fonts[i].getSize(), fonts[i].getSize(), null);
    	}
    }
}
