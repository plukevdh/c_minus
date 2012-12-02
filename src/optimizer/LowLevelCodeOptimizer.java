package optimizer;

import lowlevel.*;

public class LowLevelCodeOptimizer {

  CodeItem firstItem;
  int optimizationLevel;
  JumpOptimizer jumpOpti;

  public LowLevelCodeOptimizer(CodeItem first) {
    this(first, 0);
  }

  public LowLevelCodeOptimizer(CodeItem first, int level) {
    firstItem = first;
    optimizationLevel = level;
    jumpOpti = new JumpOptimizer(first, level);
  }

  public void optimize () {
    if (optimizationLevel > 0) {
      jumpOpti.optimize();
      doIterativeOptimization();
    }
  }

  public void doIterativeOptimization () {

  }
}