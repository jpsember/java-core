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
package js.base;

import java.time.Instant;
import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import js.data.DataUtil;
import js.json.JSList;

import static js.base.Tools.*;

/**
 * Constructs formatted strings, using a fluent interface
 */
public final class BasePrinter {

  /**
   * Add a handler for a particular class.
   */
  public static void registerClassHandler(Class klass, BiConsumer<Object, BasePrinter> printHandler) {
    checkNotNull(printHandler);
    sClassStringConverterMap.put(klass, printHandler);
  }

  /**
   * Interface that objects can implement to format themselves to a BasePrinter
   */
  public static abstract class Printer {
    abstract void printTo(BasePrinter destination);
  }

  /**
   * Use a BasePrinter to format a string from an array of objects
   */
  public static String toString(Object... messageObjects) {
    BasePrinter prBuffer = new BasePrinter();
    prBuffer.pr(messageObjects);
    return prBuffer.toString();
  }

  // ------------------------------------------------------------------
  // Reading content
  // ------------------------------------------------------------------

  /**
   * Get content as a string
   */
  @Override
  public String toString() {
    return mContentBuffer.toString();
  }

  /**
   * Clear any existing contents
   */
  public void clear() {
    mContentBuffer.setLength(0);
  }

  // ------------------------------------------------------------------
  // Spaces, linefeeds, paragraph breaks
  // ------------------------------------------------------------------

  private static final int BRK_COLUMN = 1;
  private static final int BRK_LINE = 2;
  private static final int BRK_PARAGRAPH = 3;

  private void appendSpace() {
    if (mColumn != 0)
      mPendingBreak = Math.max(mPendingBreak, BRK_COLUMN);
  }

  private void appendLineBreak() {
    mPendingBreak = Math.max(mPendingBreak, BRK_LINE);
  }

  private void appendParagraphBreak() {
    mPendingBreak = BRK_PARAGRAPH;
  }

  /**
   * Append a number of spaces
   */
  public void appendSpaces(int count) {
    int cursor = 0;
    while (cursor < count) {
      int maxSpacesPerRow = 42;
      int netSpaces = Math.min(count - cursor, maxSpacesPerRow);
      appendCharacters(spaces(netSpaces));
      cursor += netSpaces;
    }
  }

  /**
   * Insert a line break between current content and any subsequent content
   */
  public BasePrinter cr() {
    appendLineBreak();
    return this;
  }

  /**
   * Insert a pargraph break between current content and subsequent content.
   * 
   * A paragraph is marked by two consecutive linefeeds.
   */
  public BasePrinter br() {
    appendParagraphBreak();
    return this;
  }

  /**
   * Ensure there is at least a space between next content and current if it
   * appears on the same row
   */
  public BasePrinter sp() {
    appendSpace();
    return this;
  }

  // ------------------------------------------------------------------
  // Indentation
  // ------------------------------------------------------------------

  private static final int DEFAULT_INDENTATION_COL = 4;

  /**
   * Increase the indent amount to the next tab stop (4 spaces), and generate a
   * linefeed
   */
  public BasePrinter indent() {
    mIndentColumn += DEFAULT_INDENTATION_COL;
    cr();
    return this;
  }

  /**
   * Move the indent amount to the previous tab stop, and generate a linefeed
   */
  public BasePrinter outdent() {
    checkState(mIndentColumn != 0);
    mIndentColumn -= DEFAULT_INDENTATION_COL;
    cr();
    return this;
  }

  /**
   * Clear variables concerning indentation, pending line breaks
   */
  public BasePrinter resetIndentation() {
    mPendingBreak = 0;
    mIndentColumn = 0;
    mColumn = 0;
    return this;
  }

  // ------------------------------------------------------------------
  // Appending content
  // ------------------------------------------------------------------

  /**
   * Append string representations of objects, separated by spaces; then
   * generate a linefeed
   */
  public BasePrinter pr(Object... messages) {
    prNoCr(messages);
    append('\n');
    return this;
  }

  /**
   * Append string representations of objects, separated by spaces, without a
   * linefeed
   */
  public BasePrinter prNoCr(Object... messages) {
    for (Object obj : messages) {
      sp();
      append(obj);
    }
    return this;
  }

  /**
   * Append string, without examining linefeeds
   */
  public void appendString(String string) {
    flushLineBreak();
    if (string.length() > 0) {
      if (mPendingBreak == BRK_COLUMN) {
        if (mColumn > 0) {
          int len = mContentBuffer.length();
          if (len > 0 && mContentBuffer.charAt(len - 1) != ' ')
            appendCharacters(" ");
        }
        mPendingBreak = 0;
      }
      if (mColumn == 0)
        appendSpaces(mIndentColumn);
      appendCharacters(string);
    }
  }

  /**
   * Append an object's string representation, by looking up its converter
   */
  private void append(Object value) {
    if (value == null) {
      appendString("<null>");
      return;
    }
    getClassHandler(value).accept(value, this);
  }

  /**
   * Append string; process embedded linefeeds
   */
  public void append(String string) {
    if (!string.contains("\n")) {
      appendString(string);
      return;
    }
    int c = 0;
    while (c < string.length()) {
      int cNext = string.indexOf('\n', c);
      if (cNext < 0)
        cNext = string.length();
      if (c < cNext)
        appendString(string.substring(c, cNext));
      appendLineBreak();
      c = cNext + 1;
    }
  }

  public void append(Boolean b) {
    appendString(b ? "T" : "F");
  }

  /**
   * Append a BitSet
   */
  public void append(BitSet bitSet) {
    flushLineBreak();
    StringBuilder target = mContentBuffer;
    target.append('[');
    int charsToPrint = Math.min(bitSet.length(), 50);
    int omittedChars = bitSet.length() - charsToPrint;
    for (int i = 0; i < charsToPrint; i++)
      target.append(bitSet.get(i) ? '*' : '.');
    if (omittedChars > 0)
      target.append(" + " + omittedChars + " more");
    target.append(']');
  }

  public void append(double dblVal) {
    String formattedValue = String.format("%10.4f ", dblVal);
    String allZerosSuffix = ".0000 ";
    if (formattedValue.endsWith(allZerosSuffix))
      formattedValue = chomp(formattedValue, allZerosSuffix) + "      ";
    appendString(formattedValue);
  }

  public void append(int intVal) {
    formatInt(intVal, 6);
  }

  public void append(long longVal) {
    formatInt(longVal, 8);
  }

  public void append(char charVal) {
    append(Character.toString(charVal));
  }

  /**
   * Adjust tab stop to a particular column
   */
  public BasePrinter tab(int column) {
    flushLineBreak();
    int spacesToNextTabStop = column - mColumn;
    if (spacesToNextTabStop > 0)
      appendSpaces(spacesToNextTabStop);
    return this;
  }

  /**
   * Append a collection of objects
   */
  public void append(AbstractCollection<?> value) {
    if (value == null) {
      appendString("<null>");
      return;
    }
    appendString("[");
    boolean first = true;
    for (Object item : value) {
      if (!first)
        appendString(",");
      append(item);
      first = false;
    }
    appendString("]");
  }

  private void appendCharacters(String characters) {
    mContentBuffer.append(characters);
    mColumn += characters.length();
    mMaxColumn = Math.max(mMaxColumn, mColumn);
  }

  private void flushLineBreak() {
    if (mPendingBreak < BRK_LINE)
      return;
    mContentBuffer.append('\n');
    if (mPendingBreak >= BRK_PARAGRAPH)
      mContentBuffer.append('\n');
    mPendingBreak = 0;
    mColumn = 0;
  }

  /**
   * Append the description of a Throwable and its stack trace
   */
  public void append(Throwable throwable) {
    while (true) {
      appendString(throwable.getClass().getSimpleName());
      appendString(",");
      sp();
      appendString(ifNullOrEmpty(throwable.getMessage(), "(unknown cause)"));
      appendString(";");
      cr();
      appendThrowableStackTrace(throwable);
      throwable = throwable.getCause();
      if (throwable == null)
        break;
      appendString(" ...caused by...");
      cr();
    }
  }

  /**
   * Append long integer, padding to particular fixed width
   */
  private void formatInt(long longVal, int fixedWith) {
    appendSpace();
    appendString(""+longVal);
    //    I'm not always wanting this excessive indentation
//    String stringOfAbsVal = Long.toString(Math.abs(longVal));
//    int paddingChars = fixedWith - stringOfAbsVal.length();
//    appendString(spaces(Math.max(1, paddingChars)));
//    appendString(longVal < 0 ? "-" : " ");
//    appendString(stringOfAbsVal);
  }

  private void appendThrowableStackTrace(Throwable throwable) {
    int displayCount = 15;
    List<StackTraceElement> stackTraceElements = Arrays.asList(throwable.getStackTrace());
    List<String> stringRepresentations = new ArrayList<>();

    StringBuilder work = new StringBuilder();

    for (StackTraceElement elem : stackTraceElements) {
      work.setLength(0);
      String nameOfClass = elem.getClassName();
      String abbreviatedClassName = nameOfClass.substring(nameOfClass.lastIndexOf('.') + 1);
      work.append(abbreviatedClassName);
      work.append(".");
      work.append(elem.getMethodName());
      work.append(":");
      work.append(elem.getLineNumber());
      String row = work.toString();
      stringRepresentations.add(row);
      if (stringRepresentations.size() >= displayCount)
        break;
    }

    for (String stackTraceStringRep : stringRepresentations) {
      appendString(" .. ");
      appendString(stackTraceStringRep);
      cr();
    }
    cr();
  }

  // ------------------------------------------------------------------
  // Registering class handlers
  // ------------------------------------------------------------------

  /**
   * Get handler for an object's class. If none found, looks for its superclass,
   * etc. There is guaranteed to be a handler for the Object class.
   */
  private static BiConsumer<Object, BasePrinter> getClassHandler(Object value) {
    Class objClass = value.getClass();
    while (true) {
      BiConsumer<Object, BasePrinter> converter = sClassStringConverterMap.get(objClass);
      if (converter != null)
        return converter;
      objClass = objClass.getSuperclass();
      if (objClass == null)
        objClass = Object.class;
    }
  }

  private int mIndentColumn;
  private int mColumn;
  private int mMaxColumn;
  private StringBuilder mContentBuffer = new StringBuilder();
  private int mPendingBreak;

  private static Map<Class, BiConsumer<Object, BasePrinter>> sClassStringConverterMap = new ConcurrentHashMap<>();

  // Populate the class->string converter map with some default values.
  //
  // We must not attempt to store interfaces as keys, since no concrete object
  // will have a class that matches an interface.
  //
  static {
    Map<Class, BiConsumer<Object, BasePrinter>> map = sClassStringConverterMap;
    BiConsumer<Object, BasePrinter> handler;

    handler = (x, p) -> p.append(((Number) x).doubleValue());
    map.put(Double.class, handler);
    map.put(Float.class, handler);

    handler = (x, p) -> p.append(((Number) x).longValue());
    map.put(Long.class, handler);

    handler = (x, p) -> p.append(((Number) x).intValue());
    map.put(Integer.class, handler);
    map.put(Short.class, handler);
    map.put(Byte.class, handler);

    handler = (x, p) -> p.append(x.toString());
    map.put(Object.class, handler);
    map.put(String.class, handler);
    map.put(Character.class, handler);
    map.put(Boolean.class, (x, p) -> p.append(((Boolean) x)));
    map.put(Throwable.class, (x, p) -> p.append((Throwable) x));
    map.put(Printer.class, (x, p) -> ((Printer) x).printTo(p));
    map.put(AbstractCollection.class, (x, p) -> p.append((AbstractCollection) x));
    map.put(BitSet.class, (x, p) -> p.append((BitSet) x));
    map.put(Instant.class, (x, p) -> p.append((Instant) x));
    
    // This is generating an ExceptionInInitializerError, so removing for now
    map.put(DataUtil.EMPTY_STRING_ARRAY.getClass(), (x, p) -> p.append(JSList.with((String[]) x)));
    map.put(DataUtil.EMPTY_LONG_ARRAY.getClass(), (x, p) -> p.append(JSList.with((long[]) x)));
    map.put(DataUtil.EMPTY_INT_ARRAY.getClass(), (x, p) -> p.append(JSList.with((int[]) x)));
    map.put(DataUtil.EMPTY_FLOAT_ARRAY.getClass(), (x, p) -> p.append(JSList.with((float[]) x)));
  }
}
