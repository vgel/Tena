package r194.generation;

import java.io.IOException;
import java.io.OutputStream;

import r194.parsing.AbstractSyntaxNode;

public abstract class Generate {
	
	public AbstractSyntaxNode preprocess(AbstractSyntaxNode root){
		return new MacroProcessor().expandMacros(root);
	}
	
	public abstract void generate(AbstractSyntaxNode root, OutputStream out) throws IOException;
}
