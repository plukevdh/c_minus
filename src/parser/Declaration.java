package parser;

public abstract class Declaration implements ParseNode, CodeGen {
	private VariableType type;
	private String id;
	
	public Declaration() {}
	
	public Declaration(String id, VariableType type) {
		this.id = id;
		this.type = type;
	}
	
	public VariableType getType() {
		return type;
	}
	
	public String getID() {
		return id;
	}
	
}
