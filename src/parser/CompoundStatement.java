package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import lowlevel.Function;

public class CompoundStatement extends Statement {
	private ArrayList<VariableDeclaration> varDeclarations = new ArrayList<VariableDeclaration>();
	private ArrayList<Statement> statements = new ArrayList<Statement>();
	
	public void addVariableDeclaration(VariableDeclaration d) {
		varDeclarations.add(d);
	}
	
	public void addStatement(Statement s) {
		statements.add(s);
	}
	
	public void genLLCode(Function f) {
		for(VariableDeclaration varDecl : varDeclarations) {
			f.getTable().put(varDecl.getID(), f.getNewRegNum());
		}
		for(Statement s : statements) {
			s.genLLCode(f);
		}
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "CompoundStatement: {\n");
		for(VariableDeclaration vd : varDeclarations) {
			vd.print(tab + "\t", out);
		}
		
		for(Statement s : statements) {
			s.print(tab + "\t", out);
		}
		out.write(tab + "}\n");
	}
}
