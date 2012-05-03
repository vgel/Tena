package r194.generation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

import r194.Logging;
import r194.parsing.*;

public class GenerateMachine extends Generate {
	static final String[] basic = 
		{  null, "set", "add", "sub", "mul", "mli", "div", "dvi", "mod", "and", "bor", "xor", "shr", "asr", "shl", "sti",
		  "ifb", "ifc", "ife", "ifn", "ifg", "ifa", "ifl", "ifu",  null,  null, "adx", "sbx",  null,  null,  null,  null };
	static final String[] extended = 
		{  null, "jsr",  null,  null,  null,  null,  null, "hcf", "int", "iag", "ias",  null,  null,  null,  null,  null,
		  "hwn", "hwq", "hwi",  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null,  null };
	
	static final String[] other = 
		{ "dat" };
	
	IntList code;
	Map<Integer, String> labelFixupAddresses;
	Map<String, Integer> labelAddresses;
	
	@Override
	public void generate(AbstractSyntaxNode root, OutputStream out) throws IOException {
		root = super.preprocess(root);
		labelFixupAddresses = new HashMap<>();
		labelAddresses = new HashMap<>();
		code = new IntList();
		for (AbstractSyntaxNode node : root){
			if (node.type == ASTType.INSTRUCTION){
				handleInstruction(node);
			}
			else if (node.type == ASTType.LABEL){
				labelAddresses.put((String)node.child(0).content, code.length);
			}
			else {
				Logging.error("Unknown top-level node type " + node.type);
			}
		}
		for (int i : labelFixupAddresses.keySet()){
			Logging.debug("Fixing up " + i + "(" + labelFixupAddresses.get(i) + ") to " + labelAddresses.get(labelFixupAddresses.get(i)));
			code.set(i, labelAddresses.get(labelFixupAddresses.get(i)));
		}
		
		for (int s : code.contents){
			out.write((byte)((s & 0xff00) >> 8));
			out.write((byte)(s & 0x00ff));
		}
	}
	
	int getRegCode(String register){
		switch(register.toLowerCase()){
		case "a": return 0x00;
		case "b": return 0x01;
		case "c": return 0x02;
		case "x": return 0x03;
		case "y": return 0x04;
		case "z": return 0x05;
		case "i": return 0x06;
		case "j": return 0x07;
		default: Logging.error("No such register " + register);
		}
		return -1;
	}
	
	int basicInstructionCode(String ins){
		ins = ins.toLowerCase();
		for (int i = 1; i < basic.length; i++){
			if (basic[i] != null && basic[i].equals(ins)) return i;
		}
		return 0;
	}
	
	int extendedInstructionCode(String ins){
		ins = ins.toLowerCase();
		for (int i = 1; i < extended.length; i++){
			if (extended[i] != null && extended[i].equals(ins)) return i;
		}
		return 0;
	}
	
	boolean isOther(String ins){
		ins = ins.toLowerCase();
		for (String s : other){
			if (s.equals(ins)){
				return true;
			}
		}
		return false;
	}
	
	void handleInstruction(AbstractSyntaxNode instruction){
		String ins = (String)instruction.child(0).content;
		Logging.debug(ins + " instruction");
		int oldLen = code.length;
		int s = basicInstructionCode(ins);
		if (s > 0){
			if (instruction.child(1).numChildren() > 2)
				Logging.error("Too many arguments for instruction " + ins);
			ArgResult a = getArg(instruction.child(1).child(0), 1, false);
			ArgResult b = getArg(instruction.child(1).child(1), 2, a.useb);
			handleBasicInstruction(ins, a, b);
		}
		else if (s == 0){
			Logging.debug("GIMME MY DAMN JSR");
			s = extendedInstructionCode(ins);
			if (s > 0){
				if (instruction.child(1).numChildren() > 1)
					Logging.error("Too many arguments for instruction " + ins);
				handleExtendedInstruction(ins, getArg(instruction.child(1).child(0), 1, false));
			}
			else if (s == 0){
				if (isOther(ins)){
					handleOther(ins, getArg(instruction.child(1).child(0), 1, false));
				}
			}
		}
		Logging.debug("Wrote " + (code.length - oldLen) + " bytes");
	}
	
	void handleBasicInstruction(String ins, ArgResult a, ArgResult b){
		int opcode = basicInstructionCode(ins);
		if (opcode <= 0){
			Logging.error("Invalid instruction " + ins);
		}
		
		final int sixBitMask = 0x3f;
		final int fourBitMask = 0xf;
		int result = opcode & fourBitMask;
		result |= ((a.a & sixBitMask) << 4);
		result |= ((b.a & sixBitMask) << 10);
		code.add(result);
		if (a.useb) code.add(a.b);
		if (b.useb) code.add(b.b);
	}
	
	void handleExtendedInstruction(String ins, ArgResult a){
		int opcode = extendedInstructionCode(ins);
		Logging.debug("Found " + opcode + " for " + ins);
		if (opcode <= 0){
			Logging.error("Invalid instruction " + ins);
		}	
		final int sixBitMask = 0x3f;
		
		int result = 0;
		result |= ((opcode & sixBitMask) << 4);
		result |= ((a.a & sixBitMask) << 10);
		code.add(result);
		if(a.useb) code.add(a.b);
	}
	
	void handleOther(String ins, ArgResult a){
		if (ins.equalsIgnoreCase("dat")){
			code.add(a.a);
			return;
		}
		else {
			Logging.error("Unknown ins " + ins);
		}
	}
	
	ArgResult getArg(AbstractSyntaxNode arg, int argNum, boolean mayHaveThird){
		if (arg.type == ASTType.IDENT){ //label or register
			String s = (String)arg.content;
			switch(s.toLowerCase()){
			case "a": return new ArgResult(0x00);
			case "b": return new ArgResult(0x01);
			case "c": return new ArgResult(0x02);
			case "x": return new ArgResult(0x03);
			case "y": return new ArgResult(0x04);
			case "z": return new ArgResult(0x05);
			case "i": return new ArgResult(0x06);
			case "j": return new ArgResult(0x07);
			case "pop": return new ArgResult(0x18);
			case "peek": return new ArgResult(0x19);
			case "push": return new ArgResult(0x1a);
			case "sp": return new ArgResult(0x1b);
			case "pc": return new ArgResult(0x1c);
			case "o": return new ArgResult(0x1d);
			}
			//assume it's a label
			int offset = 1;
			if (mayHaveThird) offset++;
			labelFixupAddresses.put(code.length + offset, s);
			return new ArgResult(0x1f, Integer.MIN_VALUE);
		}
		else if (arg.type == ASTType.NUMBER){
			int s = Integer.parseInt((String)arg.content);
			if (s <= 0x1f){
				return new ArgResult(s + 0x20); //int form literal
			}
			else {
				return new ArgResult(0x1f, s);
			}
		}
		else if (arg.type == ASTType.INDIRECT_ADDR){
			if (arg.numChildren() == 1){ //[register], [label] or [number]
				AbstractSyntaxNode node = arg.child(0);
				if (node.type == ASTType.IDENT){ //[register] or [label]
					String s = (String)node.content;
					switch(s.toLowerCase()){
					case "a": return new ArgResult(0x08);
					case "b": return new ArgResult(0x09);
					case "c": return new ArgResult(0x0a);
					case "x": return new ArgResult(0x0b);
					case "y": return new ArgResult(0x0c);
					case "z": return new ArgResult(0x0d);
					case "i": return new ArgResult(0x0e);
					case "j": return new ArgResult(0x0f);
					}
					//assume it's a label
					int offset = 1;
					if (mayHaveThird) offset++;
					labelFixupAddresses.put(code.length + offset, s);
					return new ArgResult(0x1e, Integer.MIN_VALUE);
				}
				else if (node.type == ASTType.NUMBER){
					return new ArgResult(0x1e, Integer.parseInt((String)node.content));
				}
				else {
					Logging.error("Strange thing passed to indirect expression: " + node.type);
					return null;
				}
			}
			else if (arg.numChildren() == 3){ //[something +/- something]
				AbstractSyntaxNode a = arg.child(0);
				AbstractSyntaxNode b = arg.child(2);
				if (a.type == b.type && a.type == ASTType.IDENT){
					Logging.error("Can only add Label +- Number in indirect address expression, not Label +- Label");
				}
				else if (a.type == b.type && a.type == ASTType.NUMBER){
					int shrb = Integer.parseInt((String)b.content);
					if ("-".equals(arg.child(1).content)) 
						shrb = -shrb;
					else if (!"+".equals(arg.child(1).content))
						Logging.error("Unexpected op " + arg.child(1).content);
					
					int shr = Integer.parseInt((String)a.content) + shrb;
					return new ArgResult(0x1e, shr);
				}
				else if (a.type == ASTType.NUMBER && b.type == ASTType.IDENT){ //swap so a is always the label
					AbstractSyntaxNode c = a;
					a = b;
					b = c;
				}
				
				int number = Integer.parseInt((String)b.content);
				if ("-".equals(arg.child(1).content)) 
					number = -number;
				else if (!"+".equals(arg.child(1).content))
					Logging.error("Unexpected op " + arg.child(1).content);
				
				String reg = (String)a.content;
				switch(reg.toLowerCase()){
				case "a": return new ArgResult(0x10, number);
				case "b": return new ArgResult(0x11, number);
				case "c": return new ArgResult(0x12, number);
				case "x": return new ArgResult(0x13, number);
				case "y": return new ArgResult(0x14, number);
				case "z": return new ArgResult(0x15, number);
				case "i": return new ArgResult(0x16, number);
				case "j": return new ArgResult(0x17, number);
				}
				Logging.error("I don't know what to do with " + a + " " + arg.child(1).content + " " + b);
				return null;
			}
			else {
				Logging.error("Bad indirect expression, must be either single thing or something +/- something");
				return null;
			}
		}
		else {
			Logging.error("Something strange was put in an indirect expression and it aint feel right: " + arg.type);
			return null;
		}
	}
	
	public class ArgResult {
		int a, b;
		boolean useb;
		
		public ArgResult(int a, int b) {
			this.a = a;
			this.b = b;
			this.useb = true;
		}
		
		public ArgResult(int a) {
			this.a = a;
			this.useb = false;
		}

		public int getA() {
			return a;
		}

		public int getB() {
			return b;
		}

		public boolean isUseb() {
			return useb;
		}
	}
}