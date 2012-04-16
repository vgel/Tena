package r194.generation;

public class IntList {
	public int[] contents;
	public int length;
	
	public IntList(){
		this(16);
	}
	
	public IntList(int[] initial){
		contents = initial;
		length = initial.length;
	}
	
	public IntList(int initialSize){
		contents = new int[initialSize];
		length = 0;
	}
	
	public int[] copyArray(){
		int[] ret = new int[contents.length];
		System.arraycopy(contents, 0, ret, 0, contents.length);
		return ret;
	}
	
	public int[] toSmallestArray(){
		int[] ret = new int[length];
		System.arraycopy(contents, 0, ret, 0, length);
		return ret;
	}
	
	public void add(int s){
		if (length >= contents.length){
			int[] newContents = new int[contents.length * 2];
			System.arraycopy(contents, 0, newContents, 0, contents.length);
			contents = newContents;
		}
		contents[length] = s;
		length++;
	}
	
	public void set(int i, int s){
		if (i >= length || i < 0)
			throw new ArrayIndexOutOfBoundsException(i + ", " + length);
		contents[i] = s;
	}
	
	public int get(int i){
		if (i >= length || i < 0)
			throw new ArrayIndexOutOfBoundsException(i + ", " + length);
		return contents[i];
	}
}
