package parser;

import java.io.BufferedWriter;
import java.io.IOException;

public abstract class Statement implements VoidCodeGen {
	public abstract void print(String tab, BufferedWriter out) throws IOException;
}
