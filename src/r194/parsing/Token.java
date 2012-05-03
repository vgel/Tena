package r194.parsing;

import java.util.regex.*;

enum Token {
	NONE(null),
	EOF(null),
	COMMENT(";[^\n]*"),
	OPEN_MULTILINE_COMMENT("/\\*"),
	CLOSE_MULTILINE_COMMENT("\\*/"),
	WHITESPACE("\\s"),
	DIRECTIVE("#"),
	NUMBER("[0-9]+"),
	HEXNUMBER("0x[a-fA-F0-9]*"),
	OCTNUMBER("o[0-7]+"),
	LITERALCHAR("'[a-zA-Z]'"),
	LSQUAREBRACKET("\\["),
	RSQUAREBRACKET("\\]"),
	LCURLYBRACKET("\\{"),
	RCURLYBRACKET("\\}"),
	LPAREN("\\("),
	RPAREN("\\)"),
	COMMA("\\,"),
	COLON(":"),
	PLUS("\\+"),
	MINUS("\\-"),
	IDENT("[a-zA-Z\\_][a-zA-Z\\_0-9]*");
	
	Pattern pattern;
	
	Token(String spattern){
		if (spattern != null){
			pattern = Pattern.compile(spattern);
		}
		else {
			pattern = null;
		}
	}
	
	public static Token getMatching(String match){
		for (Token t : values()){
			if (t.pattern != null && t.pattern.matcher(match).matches()){
				return t;
			}
		}
		return NONE;
	}
}