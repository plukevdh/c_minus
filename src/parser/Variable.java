package parser;

import java.io.BufferedWriter;
import java.io.IOException;

public class Variable {
	protected String id;
	protected boolean isArray = false;
	
	public Variable(String i) {
		id = i;
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "Variable: " + id + " \n");
	}
}
