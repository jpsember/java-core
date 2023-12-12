package js.leetcode;

import static js.base.Tools.*;

public class P201BitwiseANDNumbersRange {

  public int SLOWrangeBitwiseAnd(int left, int right) {
    int mask = right;
    boolean agree = true;
    int bit = 1 << 30;
    while (bit != 0) {
      if (agree && (left & bit) != (right & bit))
        agree = false;
      if (!agree)
        mask &= ~bit;
      bit >>= 1;
    }
    return mask;
  }

  private static String binaryStr(long val) {
    var s = "";
    for (int i = 0; i <= 31; i++) {
      long bit = 1L << i;
      s = (((val & bit) != 0) ? "1" : "0") + s;
      if (val < (bit << 1))
        break;
    }
    return s;
  }

  public static void main(String[] args) {
    new P201BitwiseANDNumbersRange().run();
  }

  private void run() {
    x(5, 7);
    for (int a = 0; a < 18; a++) {
      for (int b = 0; b <= a; b++) {
        x(a, b);
      }
    }
    x(0, Integer.MAX_VALUE);
    x(Integer.MAX_VALUE / 2, Integer.MAX_VALUE);
    x(0, 0);

  }

  private void x(int left, int right) {
    var exp = SLOWrangeBitwiseAnd(left, right);
    var result = rangeBitwiseAnd(left, right);

    pr(left, right, result, binaryStr(result));
    checkState(result == exp, "expected", exp, binaryStr(exp));
  }

  public int rangeBitwiseAnd(int left, int right) {
    int mask = ~0;
    while (left != right) {
      mask <<= 1;
      left &= mask;
      right &= mask;
    }
    return right;
  }

}
