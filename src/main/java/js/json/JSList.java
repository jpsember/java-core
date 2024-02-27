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
package js.json;

import static js.base.Tools.*;

import static js.json.JSUtils.*;

import java.util.*;

import js.data.AbstractData;
import js.data.ByteArray;
import js.data.DataUtil;
import js.data.DoubleArray;
import js.data.FloatArray;
import js.data.IntArray;
import js.data.LongArray;
import js.data.ShortArray;

public final class JSList extends JSObject implements Iterable<Object> {

  // ------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------

  public static final JSList DEFAULT_INSTANCE = new JSList().lock();

  public JSList() {
    mList = new ArrayList<>();
  }

  public JSList(CharSequence charSequence) {
    mList = new ArrayList<>();
    JSParser p = new JSParser(charSequence);
    constructFrom(p);
    p.assertCompleted();
  }

  protected JSList(List unsafeList) {
    mList = unsafeList;
  }

  // ------------------------------------------------------------------
  // Factory methods
  // ------------------------------------------------------------------

  /**
   * Construct a JSList containing a sequence of doubles
   */
  public static JSList with(double... doubles) {
    return DoubleArray.with(doubles).toJson();
  }

  /**
   * Construct a JSList containing a sequence of floats
   */
  public static JSList with(float... floats) {
    return FloatArray.with(floats).toJson();
  }

  /**
   * Construct a JSList containing a sequence of integers
   */
  public static JSList with(int... integers) {
    return IntArray.with(integers).toJson();
  }

  /**
   * Construct a JSList containing a sequence of longs
   */
  public static JSList with(long... longs) {
    return LongArray.with(longs).toJson();
  }

  /**
   * Construct a JSList containing a sequence of shorts
   */
  public static JSList with(short... shorts) {
    return ShortArray.with(shorts).toJson();
  }

  /**
   * Construct a JSList containing a sequence of bytes
   */
  public static JSList with(byte... bytes) {
    return ByteArray.with(bytes).toJson();
  }

  /**
   * Construct a JSList containing a sequence of strings
   */
  public static JSList with(String... strings) {
    JSList list = new JSList();
    for (String s : strings)
      list.add(s);
    return list;
  }

  /**
   * Construct a JSList containing a sequence of JSMap representations of
   * AbstractData objects
   */
  public static JSList withAbstractData(Iterable<? extends AbstractData> dataObjects) {
    JSList list = new JSList();
    for (AbstractData d : dataObjects)
      list.add(d.toJson());
    return list;
  }

  /**
   * Construct a JSList containing a sequence of Strings
   */
  public static JSList with(Iterable<String> strings) {
    JSList list = new JSList();
    for (String value : strings)
      list.mList.add(value);
    return list;
  }

  public static JSList withUnsafeList(List<?> unsafeList) {
    checkNotNull(unsafeList);
    JSList result = new JSList();
    result.mList = (List<Object>) unsafeList;
    return result;
  }

  public static JSList withStringRepresentationsOf(Iterable<?> iterable) {
    JSList list = new JSList();
    List<Object> wrappedArrayList = list.mList;
    for (Object value : iterable)
      wrappedArrayList.add(value == null ? null : value.toString());
    return list;
  }

  public static JSList with(Throwable throwable) {
    ArrayList<String> strings = new ArrayList<>();
    StackTraceElement[] stackTraceElementList = throwable.getStackTrace();
    StringBuilder sb = new StringBuilder();
    for (StackTraceElement e : stackTraceElementList) {
      if (strings.size() == 15)
        break;
      strings.add(stackTraceEntryToString(e, sb));
    }
    return new JSList(strings);
  }

  private static String stackTraceEntryToString(StackTraceElement stackTraceElement,
      StringBuilder destination) {
    String className = stackTraceElement.getClassName();
    String shortClassName = className.substring(className.lastIndexOf('.') + 1);
    destination.setLength(0);
    destination.append(shortClassName);
    destination.append(".");
    destination.append(stackTraceElement.getMethodName());
    destination.append(":");
    destination.append(stackTraceElement.getLineNumber());
    return destination.toString();
  }

  // ------------------------------------------------------------------

  // It is possible that some values are Integers and others are Longs, 
  // or Floats and Doubles, and their differing types will cause a 
  // more naive method to fail.  So, for equals and hashCode, we
  // convert the objects to a (non-pretty printed) string
  // and compare those strings.  Slower but robust.

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    return this.toString().equals(((JSList) o).toString());
  }

  @Override
  public int hashCode() {
    return this.toString().hashCode();
  }

  @Override
  public JSList deepCopy() {
    JSList copy = new JSList();
    List<Object> wrappedArrayList = new ArrayList<>();
    copy.mList = wrappedArrayList;
    for (Object value : mList)
      wrappedArrayList.add(JSUtils.deepCopy(value));
    return copy;
  }

  @Override
  void constructFrom(JSParser parser) {
    parser.read('[');
    boolean first = true;
    while (!parser.readIf(']')) {
      if (!first) {
        parser.read(',');
        if (parser.readIf(']'))
          break;
      } else
        first = false;
      mList.add(parser.readValue());
    }
  }

  @Override
  public int size() {
    return mList.size();
  }

  @Override
  public boolean isEmpty() {
    return mList.isEmpty();
  }

  public JSList add(JSObject value) {
    mList.add(value);
    return this;
  }

  public JSList add(String string) {
    mList.add(string);
    return this;
  }

  public JSList add(long longValue) {
    mList.add(longValue);
    return this;
  }

  public JSList add(double doubleValue) {
    mList.add(doubleValue);
    return this;
  }

  public JSList add(Boolean booleanValue) {
    mList.add(booleanValue);
    return this;
  }

  public JSList add(AbstractData abstractData) {
    mList.add(abstractData.toJson());
    return this;
  }

  @Override
  public JSList clear() {
    mList.clear();
    return this;
  }

  public JSList append(JSList jsonList) {
    mList.addAll(jsonList.mList);
    return this;
  }

  /**
   * Add a value of unknown but json-compatible type
   */
  public JSList addUnsafe(Object objectOfUnknownType) {
    mList.add(objectOfUnknownType);
    return this;
  }

  public JSList remove(int index) {
    mList.remove(index);
    return this;
  }

  public Object getUnsafe(int index) {
    return mList.get(index);
  }

  public <T> T get(int index) {
    return (T) mList.get(index);
  }

  public String getString(int index) {
    return (String) mList.get(index);
  }

  public Boolean getBoolean(int index) {
    return (Boolean) mList.get(index);
  }

  public JSMap getMap(int index) {
    return (JSMap) mList.get(index);
  }

  public JSList getList(int index) {
    return (JSList) mList.get(index);
  }

  private Number getNumber(int index) {
    return (Number) mList.get(index);
  }

  public int getInt(int index) {
    return getNumber(index).intValue();
  }

  public long getLong(int index) {
    return getNumber(index).longValue();
  }

  public double getDouble(int index) {
    return getNumber(index).doubleValue();
  }

  public float getFloat(int index) {
    return getNumber(index).floatValue();
  }

  @Override
  public String prettyPrint() {
    StringBuilder stringBuilder = new StringBuilder();
    printTo(stringBuilder, 0);
    return stringBuilder.toString();
  }

  @Override
  public boolean isMap() {
    return false;
  }

  @Override
  public void printTo(StringBuilder stringBuilder, int indentColumns) {
    if (indentColumns >= 0) {
      prettyPrintTo(stringBuilder, indentColumns);
      return;
    }
    stringBuilder.append('[');
    int size = mList.size();
    for (int i = 0; i < size; i++) {
      Object value = mList.get(i);
      printValueTo(value, stringBuilder, indentColumns);
      if (i + 1 < size)
        stringBuilder.append(',');
    }
    stringBuilder.append(']');
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    printTo(sb, -1);
    return sb.toString();
  }

  List<Object> containedList() {
    return mList;
  }

  @Override
  public Iterator<Object> iterator() {
    return mList.iterator();
  }

  @Override
  public JSList asList() {
    return this;
  }

  @Override
  public JSList lock() {
    if (!isLocked())
      mList = Collections.unmodifiableList(mList);
    return this;
  }

  @Override
  public boolean isLocked() {
    return mList.getClass().getSimpleName().equals("UnmodifiableList");
  }

  public List<? extends Object> wrappedList() {
    return mList;
  }

  /**
   * Assumes list contains only strings, and extracts those strings to an array
   */
  public String[] asStringArray() {
    String[] stringArray = new String[size()];
    for (int i = 0; i < stringArray.length; i++)
      stringArray[i] = (String) mList.get(i);
    return stringArray;
  }

  /**
   * Assumes list contains only strings, and extracts those strings to an
   * ArrayList
   */
  public List<String> asStringList() {
    int size = size();
    List<String> stringList = new ArrayList<>();
    for (int i = 0; i < size; i++)
      stringList.add((String) mList.get(i));
    return stringList;
  }

  /**
   * Assumes list contains only Numbers, and extracts those as integers
   */
  public int[] asIntArray() {
    return DataUtil.intArrayFromObjectList(mList);
  }

  /**
   * Returns iterable, assuming list contains only elements of type: JSMap
   */
  public Iterable<JSMap> asMaps() {
    return DataUtil.castingIterable(iterator());
  }

  /**
   * Returns iterable, assuming list contains only elements of type: JSList
   */
  public Iterable<JSList> asLists() {
    return DataUtil.castingIterable(iterator());
  }

  /**
   * Returns iterable, assuming list contains only elements of type: String
   */
  public Iterable<String> asStrings() {
    return DataUtil.castingIterable(iterator());
  }

  /**
   * Return either this list, or its first element if it has exactly one element
   */
  public Object orSingleElement() {
    if (size() == 1)
      return getUnsafe(0);
    return this;
  }

  private void prettyPrintTo(StringBuilder stringBuilder, int indent) {
    int initialAdjustment = 0;
    indent += 2;
    initialAdjustment = -indent;
    stringBuilder.append("[ ");
    int size = mList.size();
    int rowLength = 0;
    for (int i = 0; i < size; i++) {
      Object value = mList.get(i);
      int startCursor = stringBuilder.length();
      printValueTo(value, stringBuilder, indent);
      rowLength += stringBuilder.length() - startCursor;
      initialAdjustment = 0;
      if (i + 1 < size) {
        stringBuilder.append(',');
        if (rowLength > 40) {
          stringBuilder.append("\n");
          rowLength = 0;
          stringBuilder.append(spaces(indent + initialAdjustment));
        }
      }
    }
    indent -= 2;
    stringBuilder.append(' ');
    stringBuilder.append(']');
  }

  private List<Object> mList;

}
