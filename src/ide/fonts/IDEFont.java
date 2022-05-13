package ide.fonts;

import java.awt.Color;
import java.awt.image.BufferedImage;

import ide.util.Colors;

/**
 * A classe das fontes de texto da IDE.
 * 
 * @author Juninho
 *
 */
public class IDEFont {

	private BufferedImage[] font;
	private int size;
	
	public IDEFont(BufferedImage[] font, int size) {
		this.font = font;
		this.size = size;
	}
	
	public Color getColor() {
		if (font.equals(Fonts.keywordsNormal)) return Colors.keywords;
		else if (font.equals(Fonts.objectsNormal)) return Colors.objects;
		else if (font.equals(Fonts.methodsNormal)) return Colors.methods;
		else if (font.equals(Fonts.numbersNormal)) return Colors.numbers;
		else if (font.equals(Fonts.variablesNormal)) return Colors.variables;
		
		else if (font.equals(Fonts.commentsNormal)) return Colors.comments;
		else if (font.equals(Fonts.stringsNormal)) return Colors.strings;
		else if (font.equals(Fonts.symbolsNormal)) return Colors.symbols;
		
		return Colors.other;
	}

	public BufferedImage[] getFont() {
		return font;
	}

	public void setFont(BufferedImage[] font) {
		this.font = font;
	}
	
	public int getSize() {
		return size;
	}
	
	public void setSize(int size) {
		this.size = size;
	}
	
	public String toString() {
		String s = "IDEFont | size: " + size + ", font: [";
		int[] pix = Colors.getColors(font[0]);
		
		for (int i : pix)
			s += i + ", ";
		
		s = s.substring(0, s.length() - 2);
		
		s += "]";
		
		return s;
	}
}
