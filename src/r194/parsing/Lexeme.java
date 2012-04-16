package r194.parsing;



public class Lexeme {
	public String matched;
	public Token type;
	int line;
	
	public Lexeme(String matched, Token type, int line) {
		super();
		this.matched = matched;
		this.type = type;
		this.line = line;
	}
	
	public String getMatched() {
		return matched;
	}
	
	public Token getType() {
		return type;
	}
	
	public int getLine(){
		return line;
	}

	@Override
	public String toString() {
		return "Lexeme [matched=" + matched + ", type=" + type + "]";
	}
}