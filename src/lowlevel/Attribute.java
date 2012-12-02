package lowlevel;

public class Attribute {

  private String name;
  private String value;
  private Attribute next;

  public Attribute(String newName, String newVal) {
    this (newName, newVal, null);
  }

  public Attribute(String newName, String newVal, Attribute newNext) {
    name = newName;
    value = newVal;
    next = newNext;
  }

/********************************************************************/
    // Accessor methods

  public String getName() {
    return name;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String val) {
    value = val;
  }

  public Attribute getNext() {
    return next;
  }

  public void setNext(Attribute attr) {
    next = attr;
  }

/**************************************************************************/
}