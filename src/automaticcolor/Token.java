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
	
	@Override
	public String toString() {
		return String.format("Token(value: %s, pos: %d, type: %s)", value, pos, String.valueOf(type));
	}
}
