package ide.fonts;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.Serializable;

import ide.util.Colors;

/**
 * A classe das fontes de texto da IDE.
 * 
 * @author Juninho
 *
 */
public class IDEFont implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private transient BufferedImage[] font;
	private int size;
	
	public IDEFont(BufferedImage[] font, int size) {
		this.font = font;
		this.size = size;
	}
	
	public Color getColor() {
		if (font.equals(Fonts.keywordsEditor)) return Colors.keywords;
		else if (font.equals(Fonts.objectsEditor)) return Colors.objects;
		else if (font.equals(Fonts.methodsEditor)) return Colors.methods;
		else if (font.equals(Fonts.numbersEditor)) return Colors.numbers;
		else if (font.equals(Fonts.variablesEditor)) return Colors.variables;
		
		else if (font.equals(Fonts.commentsEditor)) return Colors.comments;
		else if (font.equals(Fonts.stringsEditor)) return Colors.strings;
		else if (font.equals(Fonts.symbolsEditor)) return Colors.symbols;
		
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
}
