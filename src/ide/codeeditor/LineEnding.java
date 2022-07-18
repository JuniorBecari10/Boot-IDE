package ide.codeeditor;

public enum LineEnding {
	CR("\r"),
	LF("\n"),
	CRLF("\r\n");
	
	private String ch;
	
	LineEnding(String ch) {
		this.ch = ch;
	}
	
	public String getCh() {
		return ch;
	}
}
