package parser;

import java.io.BufferedWriter;
import java.io.IOException;

public interface ParseNode {
	public void print(String tab, BufferedWriter out) throws IOException;
}


