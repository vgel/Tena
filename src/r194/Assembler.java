package r194;

import java.util.*;
import java.io.*;

import r194.generation.*;
import r194.parsing.*;

public class Assembler {

	public static void main(String[] sargs) throws Exception {
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			public void uncaughtException(Thread t, Throwable e) {
				if (e instanceof Exception){
					Logging.error((Exception)e);
				}
				else {
					Logging.error(e);
				}
			}
		});
		Map<String, String> args = parseArgs(sargs);
		//System.out.println(args);
		String filename = args.get("-in");
		String outname = args.get("-out");
		String type = args.get("-f");
		Logging.setupLogging(args.containsKey("-debug"));
		Logging.debug(args);
		if (filename == null || outname == null || args.isEmpty() || args.containsKey("-help") || args.containsKey("-h")
				|| args.containsKey("--help")) {
			Logging.message("0x10^c Assembler (Tena for short) by Jonathon \"Rotten194\" Vogel");
			Logging.message("-----------------------------------------------------------------");
			Logging.message("This assembler compiles a custom dialect of                      ");
			Logging.message("DCPU-16 assembly to standard DCPU-16 bytecode.                   ");
			Logging.message("                                                                 ");
			Logging.message("You can see the README for the syntax breakdown.                 ");
			Logging.message("                                                                 ");
			Logging.message("Command line arguments:                                          ");
			Logging.message("-in: the file to parse                                           ");
			Logging.message("-out: the file to write to (WILL OVERWRITE!)                     ");
			Logging.message("-f: the output type (bin, asm, or both)                          ");
			Logging.message("-debug: get tons of debugging output (including AST dumps)       ");
			Logging.message("-help/--help: display this message                               ");
			System.exit(0);
		}

		String asm = loadFile(filename);

		Lexer lexer = new Lexer();
		Parser parser = new Parser(lexer.lex(asm));
		AbstractSyntaxNode program = parser.program();
		Logging.debug(program.toString());
		Generate g;
		if (type.equalsIgnoreCase("asm")){
			g = new GenerateAsm();
			g.generate(program, new FileOutputStream(outname));
		}
		else if (type.equalsIgnoreCase("bin")){
			g = new GenerateMachine();
			g.generate(program, new FileOutputStream(outname));
		}
		else if (type.equalsIgnoreCase("both")){
			Logging.debug("Why can't we have both?");
			g = new GenerateAsm();
			g.generate(program, new FileOutputStream(outname + ".asm"));
			g = new GenerateMachine();
			g.generate(program, new FileOutputStream(outname + ".bin"));
		}
		else {
			Logging.error("Unrecognized format " + type);
		}
		Logging.message("Done!");
	}

	private static String loadFile(String filename) {
		try {
			BufferedReader r = new BufferedReader(new FileReader(filename));
			String line, ret = "";
			while ((line = r.readLine()) != null)
				ret += line + "\n";
			return ret;
		} catch (IOException e) {
			Logging.error("Could not load " + filename + ": " + e);
			return null; // eclipse = dumb
		}
	}

	/**
	 * This is a technique I have started to use in parsing arguments since it's
	 * lightweight and doesn't pull in another library. Use it if you want, but
	 * I'd appreciate a link back to this repository in a comment or something
	 * :) No warranties, etc. Don't be stupid.
	 * 
	 * You can probably figure out how this works.
	 */
	private static Map<String, String> parseArgs(String[] args) {
		String argName = null;
		Map<String, String> ret = new HashMap<>();

		for (int i = 0; i < args.length; i++) { // when will java get for-each with indexes :(
			if (args[i].startsWith("-")) {
				if (argName == null) {
					argName = args[i];
				} else {
					ret.put(argName, null); // so we can have `program -flag1 -flag2`
					argName = args[i];
				}
			} else {
				if (argName == null) {
					ret.put("" + i, args[i]); // so we can have `program out.file` or things like that.
				} else {
					ret.put(argName, args[i]);
					argName = null;
				}
			}
		}
		if (argName != null){
			ret.put(argName, null); //clean up if we left a flagg
		}

		return ret;
	}

}
