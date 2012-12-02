package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.HashMap;

import compiler.CMinusCompiler;

import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class VariableExpression extends Expression {
	private Expression arrayExpression = null;
	
	public VariableExpression(String i) {
		super(i);
	}
	
	public VariableExpression(String i, Expression inner) {
		this(i);
		arrayExpression = inner;
	}
	
	public void genLLCode(Function f) {
		/** TODO: what do we do with this? */
		//arrayExpression.genLLCode(f);
		
		HashMap local = f.getTable();
		HashMap global = CMinusCompiler.globalHash;
		if(local.containsKey(getID())) {
			regNum = (Integer) local.get(getID());
		} else if(global.containsKey(getID())) {
			regNum = f.getNewRegNum();
			
			Operation oper = new Operation(Operation.OPER_LOAD_I, f.getCurrBlock());
			f.getCurrBlock().appendOper(oper);
			oper.setDestOperand(0, new Operand(Operand.OPERAND_REG, regNum));
			oper.setSrcOperand(0, new Operand(Operand.OPERAND_STRING, getID()));
		} else {
			throw new ParseErrorException("No variable named " + getID());
		}
	}
	
	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "VariableExpression: " + getID());
		if(arrayExpression != null) {
			out.write("[ \n");
			arrayExpression.print(tab + "\t", out);
			out.write("\n" + tab + "]\n");
		}
	}
}
