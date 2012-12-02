package parser;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class Expression implements ParseNode, VoidCodeGen {
	private String id;
	protected int regNum;
	
	public Expression() {}
	public Expression(String id) {
		this.id = id;
	}
	
	public String getID() {
		return id;
	}
	
	public abstract void print(String tab, BufferedWriter out) throws IOException;
}
