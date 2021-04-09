package ide.fonts;

import java.awt.image.BufferedImage;

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
