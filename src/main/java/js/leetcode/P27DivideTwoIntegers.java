
package js.leetcode;

// 29. Divide Two Integers
//
// Created a sort-of data type 'pair' representing 32 bit unsigned integers as the low 16 bits of a pair of ints.
//
// A lot of trouble to avoid dealing with 32 bit overflow.
//
// It runs fast though!

import static js.base.Tools.*;

public class P27DivideTwoIntegers {

  public static void main(String[] args) {
    new P27DivideTwoIntegers().run();
  }

  private void run() {
        x(10, 3);
         x(7, -3);
        x(Integer.MIN_VALUE, Integer.MIN_VALUE);
        x(Integer.MIN_VALUE, 1);
       x(Integer.MIN_VALUE, -1);
    x(2147483647, 1);
  }

  private void x(int dividend, int divisor) {
    pr(dividend, "/", divisor, "=", divide(dividend, divisor), "expected:",
        ((long) dividend) / (long) divisor);
  }

//  private static String str(int[] a) {
//    return hex16(a[0]) + "_" + hex16(a[1]);
//  }

  public int divide(int dividend, int divisor) {
    // We will work with absolute values, and negate the result if necessary afterward.
    boolean resultIsNeg = (dividend < 0 != divisor < 0);
    
    var a = split(dividend);
    var b = split(divisor);

    int[] sum = buildPair(0, 0);

    // Multiply the divisor by 2 so it is maximal without exceeding the dividend.
    int power = 0;
    while (power < 31) {
      var t2 = shiftLeft(b);
      if (!greaterOrEqual(a, t2)) {
        break;
      }
      b = t2;
      power++;
    }

    // Subtract this scaled divisor from the dividend if it is not greater, and 
    // set the appropriate bit in the result
    
    while (true) {
      if (greaterOrEqual(a, b)) {
        sum[1] |= 1;
        a = subtract(a, b);
      }

      // Shift the quotient bits left, and divide the divisor by two;
      // stop doing this if we've reached the unscaled divisor
    
      if (power == 0)
        break;

      sum = shiftLeft(sum);
      b = shiftRight(b);
      power--;
    }
    return join(sum, resultIsNeg);
  }

  private int[] split(int value) {
    var x = Math.abs(value);
    if (x < 0) {
      return buildPair(0x8000, 0);
    } else {
      return buildPair(x >> 16, x & 0xffff);
    }
  }

  private static int[] buildPair(int high, int low) {
    var r = new int[2];
    r[0] = high;
    r[1] = low;
    return r;
  }

  private int[] shiftLeft(int[] pair) {
    return buildPair((pair[0] << 1) | (pair[1] >> 15), (pair[1] << 1) & 0xffff);
  }

  private int[] shiftRight(int[] pair) {
    int high = pair[0] >> 1;
    int low = pair[1] >> 1;
    if ((pair[0] & 1) != 0)
      low |= 0x8000;
    return buildPair(high, low);
  }

  private boolean greaterOrEqual(int[] a, int[] b) {
    if (a[0] != b[0])
      return a[0] > b[0];
    return a[1] >= b[1];
  }

  private int[] subtract(int[] a, int[] b) {
    int carry = 0;
    int low = a[1] - b[1];
    if (low < 0) {
      carry = 1;
      low &= 0xffff;
    }
    int high = a[0] - carry - b[0];
    return buildPair(high, low);
  }

  private int join(int[] a, boolean negate) {
    if (a[0] == 0x8000) {
      if (!negate) {
        return Integer.MAX_VALUE;
      }
      return Integer.MIN_VALUE;
    }
    int val = (a[0] << 16) | a[1];
    if (negate)
      val = -val;
    return val;
  }

}
