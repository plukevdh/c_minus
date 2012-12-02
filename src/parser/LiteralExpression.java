package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.Function;

public class LiteralExpression extends Expression {
	private Object value;
	
	public LiteralExpression(Object v) {
		value = v;
	}

	public void genLLCode(Function f) {}
	
	public Object getValue() {
		return Integer.valueOf((String) value);
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "LiteralExpression: " + value.toString() + " \n");
	}
}
