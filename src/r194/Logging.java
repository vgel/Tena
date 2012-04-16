package r194;

import java.io.PrintStream;

public class Logging {
	private static PrintStream out;
	private static PrintStream err;
	private static boolean debug;
	
	static void setupLogging(boolean bdebug){
		debug = bdebug;
		out = System.out;
		err = System.err;
		System.setOut(null); //"discourage" using plain output ;)
		System.setErr(null);
	}
	
	public static void error(byte s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(short s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(int s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(long s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(float s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(double s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(Object s){
		err.println(s);
		System.exit(1);
	}
	
	public static void error(Exception e){
		e.printStackTrace(err);
		System.exit(1);
	}
	
	public static void debug(byte s){
		if (debug) out.println(s);
	}
	
	public static void debug(short s){
		if (debug) out.println(s);
	}
	
	public static void debug(int s){
		if (debug) out.println(s);
	}
	
	public static void debug(long s){
		if (debug) out.println(s);
	}
	
	public static void debug(float s){
		if (debug) out.println(s);
	}
	
	public static void debug(double s){
		if (debug) out.println(s);
	}
	
	public static void debug(Object s){
		if (debug) out.println(s);
	}
	
	public static void message(byte s){
		out.println(s);
	}
	
	public static void message(short s){
		out.println(s);
	}
	
	public static void message(int s){
		out.println(s);
	}
	
	public static void message(long s){
		out.println(s);
	}
	
	public static void message(float s){
		out.println(s);
	}
	
	public static void message(double s){
		out.println(s);
	}
	
	public static void message(Object s){
		out.println(s);
	}
}
