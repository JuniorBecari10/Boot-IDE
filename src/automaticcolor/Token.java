package automaticcolor;

public class Token {
	public String value;
	public int pos;
	public TokenType type;
	
	public Token(String value, int pos, TokenType type) {
		this.value = value;
		this.pos = pos;
		this.type = type;
	}
}
