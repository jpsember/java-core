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

import java.util.Arrays;
import java.util.List;

import js.base.BasePrinter;
import js.json.JSList;

public class IntArray implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData implementation
  // ------------------------------------------------------------------

  // The abstract data classes will attempt to use a class variable to parse 
  // such items, so supply one that can act as a parser
  //
  public static final IntArray DEFAULT_INSTANCE = with(DataUtil.EMPTY_INT_ARRAY);

  /**
   * Ensure the wrapped array has no unused elements. Does nothing if not a
   * Builder
   */
  protected void trim() {
  }

  @Override
  public Builder toBuilder() {
    int[] copy = DataUtil.copyOf(mArray);
    return new Builder(copy);
  }

  @Override
  public final JSList toJson() {
    trim();
    List<Integer> wrapped = arrayList();
    for (int x : mArray)
      wrapped.add(x);
    JSList output = JSList.withUnsafeList(wrapped);
    return output;
  }

  @Override
  public final String toString() {
    return toJson().prettyPrint();
  }

  @Override
  public IntArray parse(Object object) {
    JSList source = (JSList) object;
    @SuppressWarnings("unchecked")
    List<Number> sourceList = (List<Number>) source.wrappedList();
    int[] w = new int[sourceList.size()];
    for (int i = 0; i < w.length; i++)
      w[i] = sourceList.get(i).intValue();
    return new IntArray(w);
  }

  @Override
  public IntArray build() {
    return this;
  }

  @Override
  public final boolean equals(Object object) {
    if (this == object)
      return true;
    if (object == null || !(object instanceof IntArray))
      return false;
    IntArray other = (IntArray) object;
    if (other.hashCode() != hashCode())
      return false;
    trim();
    other.trim();
    return Arrays.equals(mArray, other.mArray);
  }

  @Override
  public int hashCode() {
    // We cache the hash code.  Note that special care is taken if this is a builder
    int r = m__hashCode;
    if (r == 0) {
      trim();
      r = Arrays.hashCode(mArray) | 1;
      m__hashCode = r;
    }
    return r;
  }

  /**
   * Get a Builder, initially empty
   */
  public static Builder newBuilder() {
    return new Builder(DataUtil.EMPTY_INT_ARRAY);
  }

  /**
   * Construct an IntArray wrapping a primitive array
   */
  public static IntArray with(int... integers) {
    return new IntArray(integers);
  }

  /**
   * Construct a new IntArray with a copy of this one's array
   */
  public IntArray copy() {
    return with(DataUtil.copyOf(array()));
  }

  /**
   * Get contents as a primitive array
   */
  public final int[] array() {
    // trim array to its current size, in case it's a builder, so there are no extra elements
    trim();
    return mArray;
  }

  public int size() {
    return mArray.length;
  }

  // TODO: replicate ArrayList methods where appropriate, i.e. indexOf(...)
  public final boolean contains(int value) {
    return indexOf(value) >= 0;
  }

  public final int indexOf(int value) {
    int size = size();
    for (int i = 0; i < size; i++)
      if (mArray[i] == value)
        return i;
    return -1;
  }

  public final boolean isEmpty() {
    return size() == 0;
  }

  public final boolean nonEmpty() {
    return !isEmpty();
  }

  public int get(int position) {
    return mArray[position];
  }

  public static IntArray from(JSList jsonList) {
    List<? extends Object> list = jsonList.wrappedList();
    int size = list.size();
    int[] array = new int[size];
    for (int i = 0; i < size; i++)
      array[i] = ((Number) list.get(i)).intValue();
    return new IntArray(array);
  }

  public static final class Builder extends IntArray {

    private Builder(int[] array) {
      super(array);
      mUsed = array.length;
    }

    @Override
    public Builder toBuilder() {
      return this;
    }

    @Override
    public IntArray build() {
      return with(Arrays.copyOf(mArray, mUsed));
    }

    @Override
    public Builder copy() {
      return new Builder(Arrays.copyOf(mArray, mUsed));
    }

    @Override
    public int hashCode() {
      // In case the (mutable) contents have changed, discard any previously cached hash code
      m__hashCode = 0;
      return super.hashCode();
    }

    /**
     * Add value to the end of the array
     */
    public Builder add(int value) {
      return add(size(), value);
    }

    public Builder clear() {
      mUsed = 0;
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

    public Builder remove(int position) {
      if (position < 0 || position >= size())
        throw outOfBounds("attempt to insert at position:", position, "; size", size());
      for (int z = position; z < mUsed; z++)
        mArray[z] = mArray[z + 1];
      mUsed--;
      return this;
    }

    private static ArrayIndexOutOfBoundsException outOfBounds(Object... messages) {
      throw new ArrayIndexOutOfBoundsException(BasePrinter.toString(messages));
    }

    public int size() {
      return mUsed;
    }

    public int get(int position) {
      if (position >= size())
        throw badArg("attempt to get at position:", position, ">= size", size());
      return mArray[position];
    }

    @Override
    protected void trim() {
      if (mUsed != mArray.length) {
        mArray = Arrays.copyOf(mArray, mUsed);
      }
    }

    private void ensureCapacity(int capacity) {
      if (mArray.length >= capacity)
        return;
      mArray = Arrays.copyOf(mArray, 1 + capacity * 2);
    }

    private int mUsed;
  }

  // ------------------------------------------------------------------

  private IntArray(int[] wrappedArray) {
    mArray = wrappedArray;
  }

  protected int[] mArray;
  protected int m__hashCode;

}
