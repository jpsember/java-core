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
import static js.data.BitUtil.*;

import js.base.BaseObject;

/**
 * <pre>
 * 
 * Pack sequences of bits into an array of integers.
 * 
 * Bit sequences are written to occupy the most significant available bits of the destination buffer.
 * 
 * For example, if these two sequences are written
 * in order to a fresh buffer:
 *
 *  write(5, 17);    // bit pattern "10001"
 *  write(3, 5);     // bit pattern "101"
 *  
 * Then the resulting integer array will contain the (single) value:
 *  
 *  -1929379840  // Bit pattern "10001101000000000000000000000000"
 *
 * </pre>
 */
public final class BitWriter extends BaseObject {

  private static final boolean ASSERT = false && alert("ASSERT is true");

  public void write(int bitCount, int bits) {
    //
    // Invariants:
    //
    //  1) mBitsAvail is always 1..32 inclusive
    //  2) mBitBuffer contains the next int to be written, the lower mBitsAvail bits are zero, and the others
    //   have already been filled in appropriately
    //

    if (ASSERT) {

      if (mBitsAvail < 1 || mBitsAvail > 32)
        throw badState("Invariant failed: mBitsAvail", mBitsAvail);

      if (maskUpperBits(mBitsAvail, mBitBuffer) != 0)
        throw badState("Invariant failed: mBitsAvail", mBitsAvail, INDENT, "bit buffer",
            DataUtil.bitString(mBitBuffer));
    }

    // Validate inputs
    //
    validateBitCount(bitCount);
    if (mResult != null)
      throw badState("Writer already closed");
    bits = maskUpperBits(bitCount, bits);

    if (verbose())
      log("write " + bitCount, "bits:", bits, DataUtil.bitString(bitCount, bits));

    int bitsAvail = mBitsAvail;
    int overflowBitCount = bitCount - bitsAvail;

    if (overflowBitCount <= 0) {
      // The buffer has enough free space to fit the entire input bits
      bitsAvail -= bitCount;
      mBitBuffer |= bits << bitsAvail;
      if (bitsAvail == 0) {
        mOutputBuffer.add(mBitBuffer);
        bitsAvail = Integer.SIZE;
        mBitBuffer = 0;
      }
      mBitsAvail = bitsAvail;
    } else {
      // Write those input bits that will fit, then call recursively to write the remainder
      mOutputBuffer.add(mBitBuffer | (bits >>> overflowBitCount));
      mBitsAvail = Integer.SIZE;
      mBitBuffer = 0;
      write(overflowBitCount, bits);
    }
  }

  /**
   * Write a boolean as a single bit
   */
  public void write(boolean bit) {
    write(1, bit ? 1 : 0);
  }

  /**
   * Store a nonnegative integer using truncated binary encoding; see
   * https://en.wikipedia.org/wiki/Truncated_binary_encoding
   * 
   * @param maxValue
   *          upper bound on value; 2 <= maxValue <= MAX_TRUNCATED_BINARY_VALUE
   * @param value
   *          value to encode, 0 <= value < maxValue
   */
  public void writeTruncated(int maxValue, int value) {
    if (value < 0 || value >= maxValue)
      throw badArg("illegal value", value, "; max", maxValue);

    int k = truncatedValueBitCount(maxValue);
    int u = (2 << k) - maxValue;
    if (value < u)
      write(k, value);
    else
      write(k + 1, value + u);
  }

  public void writeUnary(int value) {
    if (value < 0 || value > 500)
      throw badArg("illegal unary value", value);
    while (value >= 4) {
      write(4, (1 << 4) - 1);
      value -= 4;
    }
    while (value != 0) {
      value--;
      write(1, 1);
    }
    write(1, 0);
  }

  public void writeGolomb(int m, int value) {
    if (m < 2 || value < 0)
      throw badArg("illegal: value", value, "m", m);

    int q = value / m;
    int remainder = value - m * q;
    writeUnary(q);
    writeTruncated(m, remainder);
  }

  /**
   * Close writer (if not already closed); return packed bits
   */
  public int[] result() {
    close();
    return mResult;
  }

  public void close() {
    log("closing");
    if (mResult == null) {
      if (mBitsAvail != Integer.SIZE) {
        if (verbose())
          log("...output:", DataUtil.bitString(mBitBuffer));
        mOutputBuffer.add(mBitBuffer);
      }
      mResult = mOutputBuffer.array();
      mOutputBuffer = null;
    }
  }

  private IntArray.Builder mOutputBuffer = IntArray.newBuilder();
  private int[] mResult;
  // The number of bits available in the work buffer 
  private int mBitsAvail = Integer.SIZE;
  private int mBitBuffer;

}
