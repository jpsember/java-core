/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.data;

import static js.base.Tools.*;

public final class BitUtil {

  /**
   * Extract a word from the lower bits of an integer
   * 
   * @param wordSize
   *          number of bits in word
   * @param wordValue
   *          integer containing the word in its lower bits
   */
  public static int maskUpperBits(int wordSize, int wordValue) {
    if (wordSize < Integer.SIZE)
      return wordValue & ((1 << wordSize) - 1);
    return wordValue;
  }

  static int validateBitCount(int bitCount) {
    if (bitCount < 1 || bitCount > Integer.SIZE)
      throw badArg("Illegal bit count:", bitCount);
    return bitCount;
  }

  static int truncatedValueBitCount(int maxValue) {
    if (maxValue < 2 || maxValue > MAX_TRUNCATED_BINARY_VALUE)
      throw badArg("illegal maximum value", maxValue);

    int k;
    {
      int bits = maxValue;
      k = 0;
      if ((bits & 0xffff0000) != 0) {
        bits >>>= 16;
        k = 16;
      }
      if (bits >= 256) {
        bits >>>= 8;
        k += 8;
      }
      if (bits >= 16) {
        bits >>>= 4;
        k += 4;
      }
      if (bits >= 4) {
        bits >>>= 2;
        k += 2;
      }
      k += (bits >>> 1);
    }
    return k;
  }

  /**
   * Maximum value for the 'maxValue' argument for calls to writeTruncated.
   * 
   * For efficient implementation (i.e. to allow working with ints), this is
   * less than the full range of an Integer
   */
  public static final int MAX_TRUNCATED_BINARY_VALUE = 1 << 29;

}
