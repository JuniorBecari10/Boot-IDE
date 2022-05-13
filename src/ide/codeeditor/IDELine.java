package ide.codeeditor;

import java.io.Serializable;
import java.util.List;

import ide.fonts.IDEFont;

public class IDELine implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private List<Character> chars;
	private List<IDEFont> fonts;
	
	public IDELine(List<Character> chars, List<IDEFont> fonts) {
		this.chars = chars;
		this.fonts = fonts;
	}

	public List<Character> getChars() {
		return chars;
	}

	public void setChars(List<Character> chars) {
		this.chars = chars;
	}

	public List<IDEFont> getFonts() {
		return fonts;
	}

	public void setFonts(List<IDEFont> fonts) {
		this.fonts = fonts;
	}
}
