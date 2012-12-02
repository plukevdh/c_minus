package parser;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;

import lowlevel.CodeItem;

public class Program implements CodeGen {
	private ArrayList<Declaration> declarations = new ArrayList<Declaration>();
	
	public void addDeclaration(Declaration d) {
		declarations.add(d);
	}

	public void printTree(BufferedWriter out) throws IOException {
		for(Declaration d : declarations) {
			d.print("\t", out);
		}
	}

	public CodeItem genLLCode() {
		CodeItem codeItem = null;
		CodeItem first = null;
		
		for(Declaration d : declarations) {
			if(codeItem == null) {
				codeItem = d.genLLCode();
				first = codeItem;
			} else {
				CodeItem temp = d.genLLCode();
				codeItem.setNextItem(temp);
				codeItem = temp;
			}
		}
		
		return first;
	}
	
}
