package automaticcolor;

import java.util.ArrayList;
import java.util.List;

import ide.codeeditor.CodeEditor;
import ide.explorer.ListableFile;
import ide.main.Main;

public class Lexer {
	public String line;
	
	public int cursor = 0;
	public int peekCursor = cursor + 1;
	
	public char ch;
	
	public Lexer(String line) {
		this.line = line;
		this.ch = line.charAt(cursor);
	}
	
	public Token nextToken() {
		int pos = cursor;
		
		if (ch == '\0')
			return new Token("", cursor, TokenType.END);
		
		else if (CodeEditor.isSymbol(ch)) {				
			char ch = this.ch;
			readChar();
			
			return new Token(String.valueOf(ch), cursor, TokenType.SYMBOL);
		}
		
		else if (Character.isWhitespace(ch)) {
			char ch = this.ch;
			readChar();
			
			return new Token(String.valueOf(ch), cursor, TokenType.SPACE);
		}
		
		else if (Character.isDigit(ch)) { // n vai verificar ponto aqui
			return new Token(readNumber(), pos, TokenType.NUMBER);
		}
		
		else if (ch == '"') { // n vai verificar ponto aqui
			return new Token(readString(), pos, TokenType.STRING);
		}
		
		else if (CodeEditor.isLetter(ch)) {
			String value = readLetters();
			TokenType type = TokenType.VARIABLE;
			
			String[] keywords = ListableFile.fileHasExtension(Main.editor.editing.getRegent().getRegent())
					? CodeEditor.getKeywords(ListableFile.getFileExtension(Main.editor.editing.getRegent().getRegent()))
					: CodeEditor.getKeywordsSpecial(Main.editor.editing.getRegent().getRegent().getName());
			
			if (Character.isUpperCase(value.charAt(0)))
				type = TokenType.OBJECT;
			
			for (String s : keywords)
				if (value.equals(s)) {
					type = TokenType.KEYWORD;
					break;
				}
			
			if (ch == '(')
				type = TokenType.FUNCTION;
			
			return new Token(value, pos, type);
		}
		
		return new Token("", cursor, TokenType.END);
	}
	
	public void readChar() {
		if (peekCursor >= line.length()) {
			ch = '\0';
			return;
		}
		
		ch = line.charAt(peekCursor);
		
		cursor = peekCursor;
		peekCursor++;
	}
	
	public char peekChar() {
		if (peekCursor >= line.length())
			return 0;
		
		return line.charAt(peekCursor);
	}
	
	public String readNumber() {
		int pos = cursor;
		
		while (Character.isDigit(ch) || ch == '.' || CodeEditor.isLetter(ch))
			readChar();
		
		return line.substring(pos, cursor);
	}
	
	public String readLetters() {
		int pos = cursor;
		
		while (CodeEditor.isLetter(ch) || Character.isDigit(ch) || ch == '.') {
			readChar();
		}
		
		return line.substring(pos, cursor);
	}
	
	public String readString() {
		int pos = cursor;
		
		readChar();
		
		while (ch != '"') {
			readChar();
		}
		
		return line.substring(pos, cursor);
	}
	
	public static List<Token> Lex(String line) {
		if (line.equals("")) return new ArrayList<>();
		
		line = line + '\0';
		
		Lexer l = new Lexer(line);
		List<Token> tokens = new ArrayList<>();
		
		for (Token t = l.nextToken(); t.type != TokenType.END; t = l.nextToken()) {
			tokens.add(t);
		}
		
		return tokens;
	}
}
