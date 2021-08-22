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

/**
 * Unpack sequences of bits from an array of integers; see BitWriter
 */
public final class BitReader {

  public BitReader(int[] source) {
    mSourceArray = source;
  }

  private static final boolean ASSERT = false && alert("ASSERT is true");

  public int read(int bitCount) {

    // Invariants:
    //
    // 1) mBitsAvail is always 0..31
    // 2) if mBitsAvail != 0, mBitBuffer contains the packed word that contains the next bit
    //
    if (ASSERT) {
      if (mBitsAvail < 0 || mBitsAvail >= Integer.SIZE)
        throw badState("Assertion failure: mBitsAvail", mBitsAvail);
    }

    validateBitCount(bitCount);

    int bitsAvail = mBitsAvail;

    if (bitsAvail == 0) {
      mBitBuffer = mSourceArray[mSourceCursor];
      mSourceCursor++;
      bitsAvail = Integer.SIZE;
    }

    int overflow = bitCount - bitsAvail;
    if (overflow <= 0) {
      // All the bits we need are available within the buffer
      bitsAvail -= bitCount;
      mBitsAvail = bitsAvail;
      return maskUpperBits(bitCount, mBitBuffer >>> bitsAvail);
    } else {
      // Some bits are outside of the buffer
      int partial = maskUpperBits(bitsAvail, mBitBuffer) << overflow;
      mBitsAvail = 0;
      // Call recursively to read the missing bits
      int remainder = read(overflow);
      return partial | remainder;
    }
  }

  public int readTruncated(int maxValue) {

    int k = truncatedValueBitCount(maxValue);
    int u = (2 << k) - maxValue;

    // "To decode, read the first k bits. If they encode a value less than u, 
    //  decoding is complete. Otherwise, read an additional bit and subtract u from the result."

    // ...when reading the additional bit, it is tacked on as the new lowest bit.

    int base = read(k);
    if (base < u)
      return base;
    else
      return ((base << 1) | read(1)) - u;
  }

  public int readUnary() {
    int value = 0;
    while (read(1) != 0)
      value++;
    return value;
  }

  public int readGolomb(int m) {
    if (m < 2)
      throw badArg("illegal m", m);
    int q = readUnary();
    int remainder = readTruncated(m);
    return q * m + remainder;
  }

  private int[] mSourceArray;
  private int mSourceCursor;
  private int mBitsAvail;
  private int mBitBuffer;
}
