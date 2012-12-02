package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import lowlevel.Attribute;
import lowlevel.Function;
import lowlevel.Operand;
import lowlevel.Operation;

public class CallExpression extends Expression {
	private String id;
	private ArrayList<Expression> args = new ArrayList<Expression>();
	
	public CallExpression(String id, ArrayList<Expression> args) {
		this.id = id;
		this.args = args;
	}
	
	public void genLLCode(Function f) {
		for(int i = args.size()-1; i >=0; i--) {
			Operation oper = new Operation(Operation.OPER_PASS, f.getCurrBlock());
			args.get(i).genLLCode(f);
			
			Operand op = null;
			if(args.get(i) instanceof LiteralExpression) {
				op = new Operand(Operand.OPERAND_INT, ((LiteralExpression) args.get(i)).getValue());
			} else {
				op = new Operand(Operand.OPERAND_REG, args.get(i).regNum);
			}
			
			oper.setSrcOperand(0, op);
			f.getCurrBlock().appendOper(oper);
		}
		
		Operation oper = new Operation(Operation.OPER_CALL, f.getCurrBlock());
		oper.addAttribute(new Attribute("numParams", String.valueOf(args.size())));
		Operand op = new Operand(Operand.OPERAND_STRING, id);
		oper.setSrcOperand(0, op);
		f.getCurrBlock().appendOper(oper);
		
		oper = new Operation(Operation.OPER_ASSIGN, f.getCurrBlock());
		op = new Operand(Operand.OPERAND_MACRO, "RetReg");
		oper.setSrcOperand(0, op);
		
		regNum = f.getNewRegNum();
		op = new Operand(Operand.OPERAND_REG, regNum);
		oper.setDestOperand(0, op);
		f.getCurrBlock().appendOper(oper);
	}

	public void print(String tab, BufferedWriter out) throws IOException {
		out.write(tab + "CallExpression: " + id + " ( \n");
		for(Expression exp : args) {
			exp.print(tab + "\t", out);
			out.write("\n");
		}
		out.write(tab + " )\n");
	}
}
