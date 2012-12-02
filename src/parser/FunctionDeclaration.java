package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import lowlevel.BasicBlock;
import lowlevel.CodeItem;
import lowlevel.Data;
import lowlevel.FuncParam;
import lowlevel.Function;

public class FunctionDeclaration extends Declaration {
	private ArrayList<Variable> parameters = new ArrayList<Variable>();
	private CompoundStatement statement;
	
	public FunctionDeclaration() {}
	
	public FunctionDeclaration(String id, VariableType type) {
		super(id, type);
	}
	
	public FunctionDeclaration(ArrayList<Variable> p, CompoundStatement s) {
		parameters = p;
		statement = s;
	}
	
	public void setParameters(ArrayList<Variable> list) {
		parameters = list;
	}
	
	public void setStatement(CompoundStatement s) {
		statement = s;
	}
	
	public CodeItem genLLCode() {
		Function f = new Function(typeConvert(), getID());
		f.createBlock0();
	
		FuncParam funcParam = null;
		for(Variable v : parameters) {
			if(funcParam == null) {
				funcParam = new FuncParam(Data.TYPE_INT, v.id, v.isArray);
				f.setFirstParam(funcParam);
			} else {
				FuncParam temp = new FuncParam(Data.TYPE_INT, v.id, v.isArray); 
				funcParam.setNextParam(temp);
				funcParam = temp;
			}
			f.getTable().put(v.id, f.getNewRegNum());
		}
		
		//f.setFirstUnconnectedBlock(new BasicBlock(f));
		
		BasicBlock bb = new BasicBlock(f);
		BasicBlock retBlock = f.genReturnBlock();
		f.setCurrBlock(bb);
		statement.genLLCode(f);

		f.appendBlock(bb);
		f.appendBlock(retBlock);
		if(f.getFirstUnconnectedBlock() != null) {
			f.appendBlock(f.getFirstUnconnectedBlock());
		}
		
		//f.printLLCode(null);
		
		return f;
	}
	
	public int typeConvert() {
		if(getType() == VariableType.INT)
			return Data.TYPE_INT;
		else
			return Data.TYPE_VOID;
	}
	
	@Override
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write("FunctionDeclaration: " + getType() + " " + getID() + " ( \n");
		for(Variable v : parameters) {
			v.print(tab, out);
		}
		out.write(")\n");
		statement.print(tab, out);
		out.write("\n");
	}
}
