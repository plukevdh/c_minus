package parser;

import java.io.BufferedWriter;
import java.io.IOException;

import compiler.CMinusCompiler;

import lowlevel.CodeItem;
import lowlevel.Data;

public class VariableDeclaration extends Declaration {
	protected int arraySize = -1;
	
	public VariableDeclaration(String id, VariableType type) {
		super(id, type);
	}
	
	public VariableDeclaration(int offset) {
		arraySize = offset;
	}
	
	public VariableDeclaration() {}
	
	public CodeItem genLLCode() {
		CMinusCompiler.globalHash.put(getID(), null);
		return new Data(typeConvert(), getID(), arraySize != -1, arraySize);
	}
	
	public int typeConvert() {
		if(getType() == VariableType.INT)
			return Data.TYPE_INT;
		else
			return Data.TYPE_VOID;
	}

	@Override
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "VariableDeclaration: " + getID());
		if(arraySize != -1) {
			out.write("[ " + arraySize + " ]");
		}
		
		out.write("\n");
	}
}
