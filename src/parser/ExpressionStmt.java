package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import lowlevel.Function;

public class ExpressionStmt extends Statement {
	private Expression expression;
	
	public ExpressionStmt() {}
	
	public ExpressionStmt(Expression e) {
		expression = e;
	}
	
	public void genLLCode(Function f) {
		if(expression != null) {
			expression.genLLCode(f);
		}
	}

	public void print(String tab, BufferedWriter out) throws IOException {
		if(expression != null) {
			out.write(tab + "ExpressionStmt: \n");
			expression.print(tab + "\t", out);
			out.write("\n");
		}
	}
}
