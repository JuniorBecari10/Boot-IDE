package automaticcolor;

import java.util.List;

public class Lexer {
	public String line;
	public int cursor = 0;
	public int peekCursor = cursor + 1;
	
	public Lexer(String line) {
		this.line = line;
	}
	
	public void nextChar() {
		cursor++;
		peekCursor++;
	}
	
	public static List<Token> Lex(String line) {
		Lexer l = new Lexer(line);
		
		
	}
}
