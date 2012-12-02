package dataflow;

public class BitArraySet {

  private static int BITS_PER_WORD = 32;
  private int maxVal;
  private int []bits;

  public BitArraySet(int newMaxVal) {
    maxVal = newMaxVal;
      // first element assumed to be zero, so 0-31 take one word, 32-63 take 2
    int numInts = (maxVal / BITS_PER_WORD) + 1;
    bits = new int[numInts];
  }

  public BitArraySet(BitArraySet copy) {
    this(copy.getMaxVal());
    int numInts = (maxVal / BITS_PER_WORD) + 1;
    for (int i=0; i < numInts; i++) {
      bits[i] = copy.bits[i];
    }
  }

/**********************************************************************/
    // Static methods

  private static int getSetBitForOffset (int val) {

    if (val < 0 || val >= BITS_PER_WORD) {
       throw new DataflowException ("BitArraySet: illegal offest for SetBit: " +
                                              val);
    }
      // shift by offset size
    return (1 << val);
  }


  private static int getUnsetBitForOffset(int val) {
    return (getSetBitForOffset(val) ^ 0xFFFFFFFF);
  }


/**********************************************************************/
    // Accessor methods

  public int getMaxVal () {
    return maxVal;
  }

/**********************************************************************/
    // General methods

  public void add (int newVal) {

    if (newVal < 0 || newVal > maxVal) {
      throw new DataflowException ("BitArraySet: illegal value to be added: " +
                                            newVal);
    }
      // which word is it in
    int index = newVal / BITS_PER_WORD;
      // what is the offset into the word
    int offset = newVal % BITS_PER_WORD;
      // return an int with only this single bit set
    int orBit = getSetBitForOffset(offset);
      // update the set
    bits[index] |= orBit;
  }

  public void remove (int newVal) {

    if (newVal < 0 || newVal > maxVal) {
      throw new DataflowException ("BitArraySet: illegal value to be removed: " +
                                            newVal);
    }
      // which word is it in
    int index = newVal / BITS_PER_WORD;
      // what is the offset into the word
    int offset = newVal % BITS_PER_WORD;
      // return an int with only this single bit set
    int andBit = getUnsetBitForOffset(offset);
      // xor with 0xFFFFFFFF will give all 1's except this bit
      // then we AND to reset bit
    bits[index] &= andBit;
  }

  public boolean contains (int val) {
    if (val < 0 || val > maxVal) {
      throw new DataflowException ("BitArraySet: illegal value for contains: " +
                                              val);
    }
      // which word is it in
    int index = val / BITS_PER_WORD;
      // what is the offset into the word
    int offset = val % BITS_PER_WORD;
      // return an int with only this single bit set
    int orBit = getSetBitForOffset(offset);

    return ( (bits[index] & orBit) != 0);
  }

  public BitArraySet union (BitArraySet setB) {
      // makes a new set, which is size of biggest, and returns it
      // union is done by ORing each word of two sets

    BitArraySet returnSet = null;

    if (maxVal > setB.getMaxVal() ) {
      int newSize = maxVal;

      returnSet = new BitArraySet (newSize);

        // for all words in both sets, OR; then copy remaining words from bigger
        // set
      int littleWords = setB.getMaxVal() / BITS_PER_WORD;
      int bigWords = maxVal / BITS_PER_WORD;

      for (int i = 0; i <= littleWords; i++) {
        returnSet.bits[i] = bits[i] | setB.bits[i];
      }
      for (int i = littleWords+1; i <= bigWords; i++) {
        returnSet.bits[i] = bits[i];
      }
    }
      // setB was bigger
    else {
      int newSize = setB.getMaxVal();

      returnSet = new BitArraySet (newSize);

        // for all words in both sets, OR; then copy remaining words from bigger
        // set
      int bigWords = setB.getMaxVal() / BITS_PER_WORD;
      int littleWords = maxVal / BITS_PER_WORD;

      for (int i = 0; i <= littleWords; i++) {
        returnSet.bits[i] = bits[i] | setB.bits[i];
      }
      for (int i = littleWords+1; i <= bigWords; i++) {
        returnSet.bits[i] = setB.bits[i];
      }
    }
    return returnSet;
  }

  public BitArraySet intersect (BitArraySet setB) {
      // makes a new set, which is size of biggest, and returns it
      // intersect is done by ANDing each word of two sets


    BitArraySet returnSet = null;
      // assume setB smaller, then change if necessary
    int littleSize = setB.getMaxVal();
    int newSize = maxVal;

    if (maxVal <= setB.getMaxVal() ) {
      newSize = setB.getMaxVal();
      littleSize = maxVal;
    }

    returnSet = new BitArraySet (newSize);

      // for all words in both sets, AND; ignore remaining words in big set
      // Because new set will be init to all zeros, remaining words will be OK
    int littleWords = littleSize / BITS_PER_WORD;

    for (int i = 0; i <= littleWords; i++) {
      returnSet.bits[i] = bits[i] & setB.bits[i];
    }

    return returnSet;
  }

    // subtract is simply intersect with not of setB
  public BitArraySet subtract (BitArraySet setB) {
      // makes a new set, which is size of biggest, and returns it
      // intersect is done by ANDing each word of two sets

    BitArraySet negation = setB.invert();

    return intersect(negation);
  }

  public BitArraySet invert () {
    int size = maxVal / BITS_PER_WORD + 1;
    BitArraySet returnSet = new BitArraySet (maxVal);
    for (int i=0; i< size; i++) {
        // -1 is all 1's - xor with 1's inverts bits
      returnSet.bits[i] = ~bits[i];
    }
    return returnSet;
  }

  public boolean equals (BitArraySet bas) {
    boolean returnVal = true;
    int size = maxVal / BITS_PER_WORD + 1;
    for (int i=0; i< size; i++) {
      if (bas.bits[i] != bits[i]) {
        returnVal = false;
      }
    }
    return returnVal;
  }

  public boolean isEmpty () {
    boolean returnVal = true;
    int size = maxVal / BITS_PER_WORD + 1;
    for (int i=0; i< size; i++) {
      if (bits[i] != 0) {
        returnVal = false;
      }
    }
    return returnVal;
  }

  public void printSet () {
    int numWords = maxVal / BITS_PER_WORD;
    int printCount = 0;

    for (int i = 0; i <= numWords; i++) {
      for (int j=0; j < BITS_PER_WORD; j++) {
        int andBit = getSetBitForOffset(j);
        if ( (bits[i] & andBit) == 0) {
          continue;
        }
        int num = i * BITS_PER_WORD + j;
        System.out.print(num + " ");
        printCount++;
        if (printCount > 16) {
          printCount = 0;
          System.out.println();
        }
      }
    }
    if (printCount != 0) {
      System.out.println();
    }
  }

  public static void main(String []args) {

    BitArraySet setA = new BitArraySet(100);
    BitArraySet setB = new BitArraySet(100);

    setA.add(12);
    setA.add(13);
    setA.add(12);
    setA.add(17);
    setA.add(35);
    setA.add(0);
    setA.add(15);
    setA.add(100);
    setA.add(90);
    setA.add(50);
    setA.add(37);

    setB.add(11);
    setB.add(5);
    setB.add(0);
    setB.add(37);
    setB.add(36);
    setB.add(21);
    setB.add(32);

    BitArraySet setC = setA.union(setB);
    BitArraySet setD = setA.intersect(setB);

    setC.printSet();
    System.out.println();
    System.out.println();
    setD.printSet();

    if (setA.contains(12)) {
      System.out.println("12 in there");
    }
    if (!setB.contains(2)) {
      System.out.println("2 not in there");
    }
  }
}