package optimizer;

/**
 * This is a general-purpose exception for use by optimizer methods.
 *
 * @author Dr. Gallagher
 * @version 1.0
 * Created: 22 Apr 03
 * Summary of Modifications:
 *
 * Description:  Optimization code uses this exception as a general-
 * purpose exception when it encounter an unexpected event.
 */


public class CodeOptimizationException extends RuntimeException {

  public CodeOptimizationException(String msg) {
    super(msg);
  }
}