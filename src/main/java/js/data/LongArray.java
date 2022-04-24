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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.json.JSList;

public class LongArray implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData implementation
  // ------------------------------------------------------------------

  // The abstract data classes will attempt to use a class variable to parse 
  // such items, so supply one that can act as a parser
  //
  public static final LongArray DEFAULT_INSTANCE = new LongArray();

  @Override
  public Builder toBuilder() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JSList toJson() {
    List<Long> wrapped = arrayList();
    for (long x : mArray)
      wrapped.add(x);
    JSList output = JSList.withUnsafeList(wrapped);
    return output;
  }

  @Override
  public final String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public LongArray parse(Object object) {
    JSList source = (JSList) object;
    
    List<Number> sourceList = (List<Number>) source.wrappedList();
    long[] w = new long[sourceList.size()];
    for (int i = 0; i < w.length; i++)
      w[i] = sourceList.get(i).longValue();
    LongArray result = new LongArray();
    result.mArray = w;
    return result;
  }

  @Override
  public LongArray build() {
    return this;
  }

  @Override
  public boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof LongArray))
      return false;
    LongArray other = (LongArray) object;
    if (other.hashCode() != hashCode())
      return false;
    return Arrays.equals(mArray, other.mArray);
  }

  @Override
  public int hashCode() {
    int r = m__hashCode;
    if (r == 0) {
      r = Arrays.hashCode(mArray) | 1;
      m__hashCode = r;
    }
    return r;
  }

  public static Builder newBuilder() {
    return new Builder(DEFAULT_INSTANCE);
  }

  public static LongArray with(long... longs) {
    LongArray r = new LongArray();
    r.mArray = longs;
    return r;
  }

  /**
   * Get contents as a primitive array
   */
  public long[] array() {
    return mArray;
  }

  public int size() {
    return mArray.length;
  }

  public final boolean contains(long value) {
    int size = size();
    for (int i = 0; i < size; i++)
      if (mArray[i] == value)
        return true;
    return false;
  }

  public final boolean isEmpty() {
    return size() == 0;
  }

  public final boolean nonEmpty() {
    return !isEmpty();
  }

  public long get(int position) {
    return mArray[position];
  }

  public static final class Builder extends LongArray {

    private Builder(LongArray source) {
      mUsed = source.mArray.length;
      mArray = Arrays.copyOf(source.mArray, mUsed);
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public JSList toJson() {
      return build().toJson();
    }

    @Override
    public int hashCode() {
      m__hashCode = 0;
      return super.hashCode();
    }

    @Override
    public LongArray build() {
      LongArray r = new LongArray();
      r.mArray = trimmedArray();
      return r;
    }

    @Override
    public long[] array() {
      // Trim array to its current size so we return its expected contents
      if (mArray.length != mUsed)
        mArray = trimmedArray();
      return mArray;
    }

    public Builder add(long value) {
      ensureCapacity(1 + size());
      mArray[mUsed++] = value;
      return this;
    }

    public Builder add(int position, int value) {
      if (position > size())
        throw badArg("attempt to insert at position:", position, "> size", size());
      ensureCapacity(1 + size());
      for (int z = mUsed; z > position; z--)
        mArray[z] = mArray[z - 1];
      mArray[position] = value;
      mUsed++;
      return this;
    }

    public int size() {
      return mUsed;
    }

    public long get(int position) {
      if (position >= size())
        throw badArg("attempt to get at position:", position, ">= size", size());
      return mArray[position];
    }

    private long[] trimmedArray() {
      return Arrays.copyOf(mArray, mUsed);
    }

    private void ensureCapacity(int capacity) {
      if (mArray.length >= capacity)
        return;
      mArray = Arrays.copyOf(mArray, 1 + capacity * 2);
    }

    private int mUsed;
  }

  /**
   * Construct a list of boxed Long elements
   */
  public List<Long> toList() {
    int size = size();
    long[] array = array();
    List<Long> result = new ArrayList<Long>(size);
    for (int i = 0; i < size; i++)
      result.add(array[i]);
    return result;
  }

  public static LongArray from(JSList jsonList) {
    List<? extends Object> list = jsonList.wrappedList();
    int size = list.size();
    long[] array = new long[size];
    for (int i = 0; i < size; i++)
      array[i] = ((Number) list.get(i)).longValue();
    LongArray result = new LongArray();
    result.mArray = array;
    return result;
  }

  // ------------------------------------------------------------------

  private LongArray() {
    mArray = DataUtil.EMPTY_LONG_ARRAY;
  }

  protected long[] mArray;
  protected int m__hashCode;

}
