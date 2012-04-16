package r194.parsing;

import java.util.*;

import r194.Logging;


public class Lexer {
	public List<Lexeme> lex(String input) {
		List<Lexeme> ret = new ArrayList<>();
		int start = 0;
		int end = 0;
		int line = 1;
		boolean commentState = false;
		
		while (end < input.length()){
			if (!isMatchable(input.substring(start, end + 1))){
				String token = input.substring(start, end);
				if (!token.equals("") && (isMatchable(token) || commentState)){
					Token type = Token.getMatching(token);
					
					if (token.contains("\n")){
						line++;
					}
					// Checks for open multi-line comment
					if (type == Token.OPEN_MULTILINE_COMMENT) {
						commentState = true;
					}
					
					if (!(commentState || type == Token.COMMENT)) {
						ret.add(new Lexeme(token, type, line));
					}
					
					// Checks for end of multi-line comment
					if (commentState) {
						commentState = type != Token.CLOSE_MULTILINE_COMMENT;
					}
					
					start = end;
					
					if (commentState) {
						end++;
					}
				}
				else {
					end++;
				}
			}
			else {
				end++;
			}
		}
		
		//clean up
		String token = input.substring(start, end);
		if (isMatchable(token)){
			if (token.contains("\n"))
				line++;
			Token type = Token.getMatching(token);
			ret.add(new Lexeme(token, type, line));
			start = end;
		}
		else Logging.error("Unexpected token " + token);
		
		return ret;
	}
	
	private boolean isMatchable(String s){
		return Token.getMatching(s) != Token.NONE;
	}

}
