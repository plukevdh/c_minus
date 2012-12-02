package lowlevel;

/**
 * This is a general-purpose exception for use by low-level code methods.
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *
 * Description:  Classes which support low-level code such as Function,
 * BasicBlock, Operation, and Operand use this exception as a general-
 * purpose exception when they encounter an unexpected event.
 */

public class LowLevelException extends RuntimeException {

  public LowLevelException(String msg) {
    super(msg);
  }
}