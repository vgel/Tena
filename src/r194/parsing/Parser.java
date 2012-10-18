package r194.parsing;

import java.util.*;

import r194.Logging;

public class Parser {
	List<Lexeme> tokens;
	int position = 0;
	int line = -1;
	
	public Parser(List<Lexeme> tokens) {
		this.tokens = tokens;
	}
	
	public String errorString(){
		return " Line: " + line;
	}
	
	public void filterTokens(){
		List<Lexeme> newTokens = new ArrayList<>();
		for (int i = 0; i < tokens.size(); i++) {
			Lexeme t = tokens.get(i);
			if (t.type != Token.WHITESPACE){
				newTokens.add(t);
			}
		}
		tokens = newTokens;
	}
	
	public boolean accept(Token token) {
		if (lookahead(0).type == token){
			line = lookahead(0).getLine();
			position++;
			return true;
		}
		return false;
	}
	
	public boolean accept(Lexeme token) {
		return accept(token.type);
	}
	
	public void expect(Lexeme token) {
		expect(token.type);
	}
	
	public void expect(Token token) {
		if (!accept(token)) {
			Logging.error("Syntax error: found " + lookahead(0) + ", expected " + token.toString() + errorString());
		}
	}
	
	public void expect(boolean b, String wanted){
		if (!b) {
			Logging.error("Syntax error: found " + lookahead(0) + ", wanted " + wanted + errorString());
		}
	}
	
	public Lexeme lookahead(int i) {
		if (position + i >= tokens.size()){
			return new Lexeme("", Token.EOF, line);
		}
		return tokens.get(position + i);
	}
	
	public AbstractSyntaxNode program() {
		AbstractSyntaxNode root = new AbstractSyntaxNode(ASTType.PROGRAM, null, null);
		filterTokens();
		while (directive(root) || topLevelThing(root)){}
		if (!accept(Token.EOF)){
			Logging.debug(tokens);
			Logging.debug(lookahead(0));
			Logging.debug(root);
			Logging.error("Unexpected root token " + lookahead(0).type + errorString());
		}
		return root;
	}
	
	public boolean directive(AbstractSyntaxNode node){
		if (accept(Token.DIRECTIVE) && accept(Token.IDENT)){
			AbstractSyntaxNode macro = node.addChild(ASTType.DIRECTIVE, lookahead(-1).getMatched()); //content = "macro"/"incbin"/etc.
			if (accept(Token.IDENT)){
				macro.addChild(ASTType.IDENT, lookahead(-1).getMatched()); //macro name, if there, otherwise it's an incbin or similar
			}
			expect(Token.LPAREN);
			AbstractSyntaxNode arglist = macro.addChild(ASTType.ARGUMENT_LIST, null);
			if (accept(Token.IDENT)){
				arglist.addChild(ASTType.IDENT, lookahead(-1).getMatched());
				while (accept(Token.COMMA)){
					expect(Token.IDENT);
					arglist.addChild(ASTType.IDENT, lookahead(-1).getMatched());
				}
			}
			expect(Token.RPAREN);
			macroBlock(macro);
			return true;
		}
		return false;
	}
	
	public boolean macroBlock(AbstractSyntaxNode node){
		if (accept(Token.LCURLYBRACKET)){
			AbstractSyntaxNode block = node.addChild(ASTType.BLOCK, null);
			while (topLevelThing(block)){}
			expect(Token.RCURLYBRACKET);
			return true;
		}
		return false;
	}
	
	public boolean topLevelThing(AbstractSyntaxNode node){
		if (macroCall(node)){
			return true;
		}
		else if (labelDecl(node)){
			return true;
		}
		else if (instruction(node)){
			return true;
		}
		else 
			return false;
	}
	
	public boolean instruction(AbstractSyntaxNode node){
		if (accept(Token.IDENT)){
			AbstractSyntaxNode ins = node.addChild(ASTType.INSTRUCTION, null);
			ins.addChild(ASTType.IDENT, lookahead(-1).getMatched());
			AbstractSyntaxNode arglist = ins.addChild(ASTType.ARGUMENT_LIST, null);
			Logging.debug("START ARG LIST!");
			if (atom(arglist)){
				Logging.debug("currtoken " + lookahead(0));
				while (accept(Token.COMMA)){
					Logging.debug("Getting atom");
					expect(atom(arglist), "Expected thing after comma!");
					Logging.debug("end atom");
				}
				Logging.debug("endwhile");
			}
			return true;
		}
		return false;
	}
	
	public boolean macroCall(AbstractSyntaxNode node){
		if (lookahead(0).type == Token.IDENT && lookahead(1).type == Token.LPAREN){
			AbstractSyntaxNode macrocall = node.addChild(ASTType.MACRO_CALL, null);
			expect(Token.IDENT);
			macrocall.addChild(ASTType.IDENT, lookahead(-1).getMatched());
			expect(Token.LPAREN);
			AbstractSyntaxNode arglist = macrocall.addChild(ASTType.ARGUMENT_LIST, null);
			if (atom(arglist)){
				while (accept(Token.COMMA)){
					expect(atom(arglist), "Thing after comma");
				}
			}
			expect(Token.RPAREN);
			return true;
		}
		return false;
	}
	
	public boolean labelDecl(AbstractSyntaxNode node){
		if (lookahead(0).type == Token.COLON){
			AbstractSyntaxNode labeldecl = node.addChild(ASTType.LABEL, null);
			expect(Token.COLON);
			if (!accept(Token.IDENT) || !accept(Token.NUMBER))
				Logging.error("Syntax error: found " + lookahead(0) + ", expected NUMBER or IDENT");
			labeldecl.addChild(ASTType.IDENT, lookahead(-1).getMatched());
			return true;
		}
		return false;
	}
	
	public boolean atom(AbstractSyntaxNode node){
		if (accept(Token.HEXNUMBER)){
			String hex = lookahead(-1).getMatched();
			Logging.debug("HEX " + hex);
			if (hex.length() == 2){
				node.addChild(ASTType.NUMBER, "0");
				return true;
			}
			hex = hex.substring(2);
			Logging.debug("HEX2 " + hex + "=" + Integer.parseInt(hex, 16));
			node.addChild(ASTType.NUMBER, "" + Integer.parseInt(hex, 16));
			return true;
		}
		else if (accept(Token.OCTNUMBER)){
			String oct = lookahead(-1).getMatched();
			oct = oct.substring(1);
			node.addChild(ASTType.NUMBER, "" + Integer.parseInt(oct, 8));
			return true;
		}
		else if (accept(Token.NUMBER)){
			node.addChild(ASTType.NUMBER, lookahead(-1).getMatched());
			return true;
		}
		else if (accept(Token.IDENT)){
			node.addChild(ASTType.IDENT, lookahead(-1).getMatched());
			return true;
		}
		else if (accept(Token.LSQUAREBRACKET)){
			AbstractSyntaxNode indir = node.addChild(ASTType.INDIRECT_ADDR, null);
			expect(atom(indir), "Need at least one part to indirect address");
			if (accept(Token.PLUS) || accept(Token.MINUS)){
				indir.addChild(ASTType.INDIR_OP, lookahead(-1).getMatched());
				expect(atom(indir), "Need another atom after plus/minus in indirect address!");
			}
			expect(Token.RSQUAREBRACKET);
			return true;
		}
		else
			return false;
	}
}
