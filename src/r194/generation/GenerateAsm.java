package r194.generation;

import java.io.*;

import r194.Logging;
import r194.parsing.*;

public class GenerateAsm extends Generate {

	@Override
	public void generate(AbstractSyntaxNode root, OutputStream out) {
		root = super.preprocess(root);
		handleSection(root, new PrintStream(out));
	}
	
	private void handleSection(AbstractSyntaxNode node, PrintStream out){
		for (AbstractSyntaxNode child : node){
			switch(child.type){
			case INSTRUCTION:
				printInstruction(child, out);
				break;
			case LABEL:
				out.print(":" + child.child(0).content);
				break;
			default:
				Logging.error("What? " + node.type);
			}
			out.println();
		}
	}
	
	private void printInstruction(AbstractSyntaxNode ins, PrintStream out){
		String name = (String)ins.child(0).content;
		out.print(name);
		out.print(" ");
		for (AbstractSyntaxNode node : ins.child(1)){
			if (node.pos != 0)
				out.print(", ");
			printArgument(node, out);
		}
	}
	
	private void printArgument(AbstractSyntaxNode arg, PrintStream out){
		switch(arg.type){
		case IDENT:
			out.print(arg.content);
			break;
		case INDIRECT_ADDR:
			printInDirAddr(arg, out);
			break;
		case NUMBER:
			out.print(arg.content);
			break;
		default:
			Logging.error("Not a valid argument!");
		}
	}
	
	private void printInDirAddr(AbstractSyntaxNode indir, PrintStream out){
		out.print("[");
		out.print(indir.child(0).content);
		if (indir.numChildren() > 1){
			out.print(" ");
			out.print(indir.child(1).content);
			out.print(" ");
			out.print(indir.child(2).content);
		}
		out.print("]");
	}
}
