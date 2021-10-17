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

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

import js.file.Files;
import js.geometry.MyMath;
import js.json.JSList;
import js.json.JSMap;

public final class Tools {

  /**
   * A do-nothing method that we can call to avoid 'unused import' warnings in
   * Eclipse
   */
  public static void loadTools() {
  }

  // ------------------------------------------------------------------
  // Development mode
  // ------------------------------------------------------------------

  /**
   * Determine if we're in development mode. If so, it will generate alert and
   * todo messages
   */
  public static boolean devMode() {
    if (sDevModeFlag == null)
      setDevMode(true);
    return sDevModeFlag;
  }

  /**
   * Set development mode. This can only be set once. If it hasn't been
   * explicitly set, it will be set true when devMode() is first called
   */
  public static void setDevMode(boolean flag) {
    checkState(sDevModeFlag == null || sDevModeFlag == flag, "dev mode flag already set");
    sDevModeFlag = flag;
  }

  private static Boolean sDevModeFlag;

  // ------------------------------------------------------------------
  // 'Print once' reports
  // ------------------------------------------------------------------

  /**
   * Print message that code is unimplemented at current line; prints a specific
   * string only once. Thread safe. Not very efficient, since it needs to
   * construct a stack trace
   */
  public static boolean todo(Object... messageObjects) {
    return todoWithSkip(1, messageObjects);
  }

  private static boolean todoWithSkip(int skipCount, Object... messageObjects) {
    String message = reportMaxTimes("TODO", 1, 1 + skipCount, messageObjects);
    if (message != null)
      pr(message);
    return true;
  }

  /**
   * Print message that code is unimplemented at current line; prints a specific
   * string only once. Thread safe. Always returns true
   */
  public static boolean alert(Object... messageObjects) {
    return alertWithSkip(1, messageObjects);
  }

  /**
   * Generate todo, but with skip count indicating how far back in stack to look
   * for the caller entry to display. Always returns true
   */
  public static boolean alertWithSkip(int stackFrameSkipCount, Object... messageObjects) {
    String message = reportMaxTimes(null, 1, 1 + stackFrameSkipCount, messageObjects);
    if (message != null)
      pr(message);
    return true;
  }

  /**
   * If not yet called from the client's location, logs a message and runs some
   * client code. Like 'alert' and 'todo', it generates a stack trace so it's
   * not particularly quick
   *
   * @param oper
   *          code to run if this is the first time called from the location
   * @return true
   */
  public static boolean once(Runnable oper) {
    String message = reportMaxTimes("ONCEONLY", 1, 1);
    if (message != null) {
      pr(message);
      oper.run();
    }
    return true;
  }

  private static String reportMaxTimes(String type, int limit, int stackFrameSkipCount,
      Object... messageObjects) {
    if (!devMode())
      return null;
    StringBuilder sb = new StringBuilder();
    sb.append("*** ");

    sb.append(ifNullOrEmpty(type, "WARNING"));
    int fileModeSentinalPosition = sb.length() + 2;

    if (messageObjects.length > 0) {
      sb.append(":");
      for (Object object : messageObjects) {
        sb.append(' ');
        sb.append(object);
      }
    }
    sb.append(" (");
    String st = getStackTraceElement(1 + stackFrameSkipCount, null);
    sb.append(st);
    sb.append(")");
    String reportText = sb.toString();

    Integer reportCount;
    synchronized (sReportCountMap) {
      reportCount = sReportCountMap.get(reportText);
      if (reportCount == null)
        reportCount = 0;
      sReportCountMap.put(reportText, reportCount + 1);
    }
    if (reportCount >= limit)
      return null;

    if (fileModeSentinalPosition < reportText.length()
        && reportText.charAt(fileModeSentinalPosition) == '!') {
      reportText = reportText.substring(0, fileModeSentinalPosition)
          + reportText.substring(fileModeSentinalPosition + 1);
      synchronized (sReportCountMap) {
        JSMap flagsMap = sPersistedAlertFlagsMap;
        File flagsFile = sPersistedAlertFlagsFile;
        if (flagsMap == null) {
          flagsFile = sPersistedAlertFlagsFile = Files.getDesktopFile("_alerts_.json");
          flagsMap = sPersistedAlertFlagsMap = JSMap.fromFileIfExists(sPersistedAlertFlagsFile);
        }
        if (flagsMap.containsKey(reportText))
          return null;
        flagsMap.put(reportText, true);
        Files.S.writePretty(flagsFile, flagsMap);
      }
    }

    return reportText;
  }

  private static JSMap sPersistedAlertFlagsMap;
  private static File sPersistedAlertFlagsFile;

  /**
   * Get element at a particular depth within a Throwable's stack trace,
   * converted to a string that allows clicking on within (an
   * intelligent-enough) IDE. If Throwable is null, it constructs one
   */
  private static String getStackTraceElement(int stackDepth, Throwable exceptionOrNull) {
    if (exceptionOrNull == null) {
      exceptionOrNull = new Throwable();
      stackDepth++;
    }
    List<StackTraceElement> elist = Arrays.asList(exceptionOrNull.getStackTrace());
    if (elist.size() <= stackDepth)
      return "(no StackTraceElement available)";
    StackTraceElement element = elist.get(stackDepth);
    return element.getFileName() + ":" + element.getLineNumber();
  }

  private static final Map<String, Integer> sReportCountMap = new ConcurrentHashMap<>();

  public static List<String> stackTraceToArray(Throwable t) {
    return split(BasePrinter.toString(t), '\n');
  }

  public static JSList stackTraceToList(Throwable t) {
    JSList list = new JSList();
    for (String s : stackTraceToArray(t))
      list.add(s);
    return list;
  }

  // ------------------------------------------------------------------
  // Conditional compilation flags
  // ------------------------------------------------------------------

  private static Map<Integer, String> sConditionalFlags = concurrentHashMap();

  public static boolean condFlag(boolean flag, int issueNumber, String description) {
    if (flag) {
      if (!testMode())
        sConditionalFlags.put(issueNumber, description);
    }
    return flag;
  }

  public static void displayConditionalFlags() {
    if (sConditionalFlags.isEmpty())
      return;
    pr("\n\n");
    List<Integer> keys = arrayList();
    keys.addAll(sConditionalFlags.keySet());
    Collections.sort(keys);
    for (Integer issueNum : keys)
      alert("Dev flag is TRUE, Issue #" + issueNum + " (" + sConditionalFlags.get(issueNum) + ")");
    pr("\n\n");
  }

  // ------------------------------------------------------------------
  // Logging
  // ------------------------------------------------------------------

  /**
   * Print string representations of an array of objects, with spaces between
   * them
   */
  public static synchronized void pr(Object... messages) {
    sBasePrinter.prNoCr(messages);
    String result = sBasePrinter.toString();
    sBasePrinter.clear();
    sBasePrinter.resetIndentation();
    sLogger.println(result);
  }

  public static LoggerInterface logger() {
    return sLogger;
  }

  public static void logger(LoggerInterface logger) {
    sLogger = logger;
  }

  private static final BasePrinter sBasePrinter = new BasePrinter();
  private static LoggerInterface sLogger = (x) -> System.out.println(x);

  // ------------------------------------------------------------------
  // Text manipulation
  // ------------------------------------------------------------------

  private static final String SPACES = repeatText(" ", 256);

  public static String repeatText(String text, int numCopies) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < numCopies; i++)
      sb.append(text);
    return sb.toString();
  }

  /**
   * Get string of zero or more spaces; if count < 0, returns empty string
   */
  public static String spaces(int count) {
    count = Math.max(count, 0);
    if (count <= SPACES.length())
      return SPACES.substring(0, count);
    return SPACES + spaces(count - SPACES.length());
  }

  /**
   * Add minimum number of spaces necessary to make StringBuilder length at
   * least targetLength
   */
  public static void tab(StringBuilder stringBuilder, int targetLength) {
    int currentLength = targetLength - stringBuilder.length();
    if (currentLength > 0)
      stringBuilder.append(spaces(currentLength));
  }

  /**
   * Add a linefeed to a StringBuilder if it doesn't already end with one (and
   * is nonempty)
   */
  public static StringBuilder addLF(StringBuilder sb) {
    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '\n')
      sb.append('\n');
    return sb;
  }

  /**
   * Convenience method to insert a string in front of a (primitive array) of
   * objects; return moidifed array
   */
  public static Object[] insertStringToFront(String stringToInsert, Object... objArray) {
    Object[] newObjArray = new Object[1 + objArray.length];
    newObjArray[0] = stringToInsert;
    for (int i = 0; i < objArray.length; i++)
      newObjArray[i + 1] = objArray[i];
    return newObjArray;
  }

  /**
   * Return string with all trailing linefeeds removed
   */
  public static String chomp(String string) {
    int i = string.length();
    while (i > 0 && string.charAt(i - 1) == '\n')
      i--;
    return string.substring(0, i);
  }

  /**
   * Return string with a suffix removed (if found); otherwise, return input
   * string
   */
  public static String chomp(String string, String optionalSuffix) {
    if (string.endsWith(optionalSuffix))
      string = string.substring(0, string.length() - optionalSuffix.length());
    return string;
  }

  /**
   * Return string with a prefix removed (if found); otherwise, return input
   * string
   */
  public static String chompPrefix(String string, String optionalPrefix) {
    if (string.startsWith(optionalPrefix))
      string = string.substring(optionalPrefix.length());
    return string;
  }

  /**
   * Convert a 'camel case' string to one with underscores instead
   */
  public static String convertCamelToUnderscore(String camelCaseString) {
    String regex = "([a-z0-9])([A-Z]+)";
    String replacement = "$1_$2";
    return camelCaseString.replaceAll(regex, replacement).toLowerCase();
  }

  public static boolean nullOrEmpty(CharSequence charSeq) {
    return charSeq == null || charSeq.length() == 0;
  }

  public static boolean nonEmpty(CharSequence charSeqOrNull) {
    return !nullOrEmpty(charSeqOrNull);
  }

  public static String ifNullOrEmpty(String string, String defaultValue) {
    return nullOrEmpty(string) ? defaultValue : string;
  }

  public static String nullToEmpty(String string) {
    return ifNullOrEmpty(string, "");
  }

  // ------------------------------------------------------------------
  // Converting objects to strings
  // ------------------------------------------------------------------

  public static String debStr(String string) {
    if (string == null)
      return "<null>";
    if (string.length() > 20)
      string = string.substring(0, 20).trim() + "...";
    string = string.replace('\n', ';');
    return quote(string);
  }

  public static String toStr(double doubleValue) {
    return String.format("%11.4f", doubleValue);
  }

  public static String toStr(int intValue) {
    return toStr(intValue, 6);
  }

  public static String toStr(int intValue, int formatWidth) {
    return String.format("%" + formatWidth + "d", intValue);
  }

  public static String quote(Object object) {
    return "\"" + object + "\"";
  }

  /**
   * Split a string by a character into a list of strings
   */
  public static List<String> split(String string, char delimeter) {
    List<String> out = new ArrayList<>();
    int prevCursor = 0;
    while (true) {
      int pos = string.indexOf(delimeter, prevCursor);
      if (pos < 0) {
        out.add(string.substring(prevCursor));
        break;
      }
      out.add(string.substring(prevCursor, pos));
      prevCursor = pos + 1;
    }
    return out;
  }

  public static String trimLeft(String string) {
    int cursor = 0;
    while (cursor < string.length() && string.charAt(cursor) <= ' ')
      cursor++;
    return string.substring(cursor);
  }

  public static String trimRight(String string) {
    int cursor = string.length();
    while (cursor - 1 > 0 && string.charAt(cursor - 1) <= ' ')
      cursor--;
    return string.substring(0, cursor);
  }

  // ------------------------------------------------------------------
  // Exception utilities
  // ------------------------------------------------------------------

  public static IllegalArgumentException badArg(Object... messageObjects) {
    throw badArgWithCause(null, messageObjects);
  }

  public static IllegalArgumentException badArgWithCause(Throwable cause, Object... messageObjects) {
    throw new IllegalArgumentException(BasePrinter.toString(messageObjects), cause);
  }

  public static IllegalStateException badState(Object... messageObjects) {
    throw new IllegalStateException(BasePrinter.toString(messageObjects));
  }

  public static RuntimeException die(Object... messageObjects) {
    throw new RuntimeException(BasePrinter.toString(messageObjects));
  }

  public static RuntimeException halt(Object... messageObjects) {
    throw die(insertStringToFront("(halting for development purposes)", messageObjects));
  }

  /**
   * Throw an exception and exit the Java program. Used e.g. for debugging Swing
   * applications
   */
  public static RuntimeException haltProgram(Object... messageObjects) {
    Throwable t = new RuntimeException(BasePrinter.toString(messageObjects));
    System.err.print(t);
    System.exit(1);
    return null;
  }

  public static RuntimeException notFinished(Object... messageObjects) {
    throw notSupported(insertStringToFront("this code isn't finished yet", messageObjects));
  }

  public static RuntimeException asRuntimeException(Throwable throwable) {
    if (throwable instanceof RuntimeException) {
      return (RuntimeException) throwable;
    }
    return new RuntimeException(throwable);
  }

  public static IllegalArgumentException asIllegalArgumentException(Throwable throwable) {
    if (throwable instanceof IllegalArgumentException) {
      return (IllegalArgumentException) throwable;
    }
    return new IllegalArgumentException(throwable);
  }

  public static UnsupportedOperationException notSupported(Object... messageObjects) {
    throw new UnsupportedOperationException(BasePrinter.toString(messageObjects));
  }

  // ------------------------------------------------------------------
  // Assertions
  // ------------------------------------------------------------------

  public static <T> T checkNotNull(T object, Object... messageObjects) {
    if (object == null)
      throw new NullPointerException(BasePrinter.toString(messageObjects));
    return object;
  }

  public static void checkArgumentsEqual(Object firstArg, Object secondArg, Object... messageObjects) {
    if (firstArg == null || !firstArg.equals(secondArg))
      throw die("Arguments didn't match!", firstArg, "!=", secondArg, "\n",
          BasePrinter.toString(messageObjects));
  }

  public static void checkArgument(boolean expression, Object... messageObjects) {
    if (!expression)
      throw new IllegalArgumentException(BasePrinter.toString(messageObjects));
  }

  public static void checkState(boolean booleanValue, Object... messageObjects) {
    if (!booleanValue)
      badState(messageObjects);
  }

  public static float verifyFinite(float value, Object... messages) {
    if (!Float.isFinite(value))
      throw die("Value isn't finite:", value, "\n", BasePrinter.toString(messages));
    return value;
  }

  // ------------------------------------------------------------------
  // BasePrinter sentinel objects
  // ------------------------------------------------------------------

  public static final BasePrinter.Printer CR = new BasePrinter.Printer() {
    public void printTo(BasePrinter sb) {
      sb.cr();
    }
  };

  public static final BasePrinter.Printer SP = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      sb.sp();
    }
  };

  public static final BasePrinter.Printer BR = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      sb.br();
    }
  };

  public static final BasePrinter.Printer ST = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      Throwable t = new Throwable();
      sb.cr().append(t);
      sb.cr();
    }
  };

  public static final BasePrinter.Printer INDENT = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      sb.indent();
    }
  };

  public static final BasePrinter.Printer OUTDENT = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      sb.outdent();
    }
  };

  public static Object TAB(int column) {
    return new BasePrinter.Printer() {
      @Override
      public void printTo(BasePrinter sb) {
        sb.tab(column);
      }
    };
  }

  public static final BasePrinter.Printer VERT_SP = new BasePrinter.Printer() {
    public void printTo(BasePrinter sb) {
      sb.cr();
      sb.resetIndentation();
      sb.appendString("\n\n");
      sb.cr();
    }
  };

  public static final BasePrinter.Printer DASHES = new BasePrinter.Printer() {
    @Override
    public void printTo(BasePrinter sb) {
      sb.cr();
      sb.resetIndentation();
      sb.appendString("\n--------------------------------------------------------------------------");
      sb.cr();
      sb.resetIndentation();
    }
  };

  // ------------------------------------------------------------------
  // Data structure utilities
  // ------------------------------------------------------------------

  /**
   * Check if an optional boolean value is true (versus false or null)
   */
  public static boolean isTrue(Boolean booleanOrNull) {
    return booleanOrNull == Boolean.TRUE;
  }

  /**
   * Check if an optional boolean value is false or null
   */
  public static boolean isFalse(Boolean booleanOrNull) {
    return !isTrue(booleanOrNull);
  }

  public static <T> T nullTo(T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }

  /**
   * Wraps a supplier to produce a thread-safe, lazy supplier of a singleton
   */
  public static <T> Supplier<T> singleton(Supplier<T> supplier) {
    return new Singleton<>(supplier);
  }

  /**
   * Convenience method to return an ArrayList
   */
  public static <T> ArrayList<T> arrayList() {
    return new ArrayList<>();
  }

  /**
   * Convenience method to return an ArrayList with particular elements
   */
  @SafeVarargs
  public static <T> List<T> arrayList(T... array) {
    return Arrays.asList(array);
  }

  /**
   * Convenience method to return a hash map
   */
  public static <KEY, VAL> HashMap<KEY, VAL> hashMap() {
    return new HashMap<>();
  }

  /**
   * Convenience method to return a concurrent hash map
   */
  public static <KEY, VAL> ConcurrentHashMap<KEY, VAL> concurrentHashMap() {
    return new ConcurrentHashMap<>();
  }

  /**
   * Convenience method to return a tree map
   */
  public static <KEY, VAL> TreeMap<KEY, VAL> treeMap() {
    return new TreeMap<>();
  }

  /**
   * Convenience method to return a concurrent hash set.
   */
  public static <T> Set<T> concurrentHashSet() {
    return ConcurrentHashMap.newKeySet();
  }

  /**
   * Convenience method to return a hash set.
   */
  public static <T> Set<T> hashSet() {
    return new HashSet<T>();
  }

  /**
   * Return a hash set constructed with particular elements
   */
  @SafeVarargs
  public static <T> HashSet<T> hashSetWith(T... objs) {
    HashSet<T> set = new HashSet<>();
    Collections.addAll(set, objs);
    return set;
  }

  /**
   * Build a map from a primitive array of keys and values
   */
  public static <K, V> Map<K, V> mapWith(Object... keysAndValuesList) {
    Map<K, V> map = concurrentHashMap();
    int i = 0;
    while (i < keysAndValuesList.length) {
      @SuppressWarnings("unchecked")
      K key = (K) keysAndValuesList[i];
      @SuppressWarnings("unchecked")
      V value = (V) keysAndValuesList[i + 1];
      i += 2;
      if (map.containsKey(key))
        throw new IllegalArgumentException("duplicate key: " + key);
      map.put(key, value);
    }
    return map;
  }

  /**
   * Convenience method to return pair of appropriate type, given arguments
   */
  @SuppressWarnings("unchecked")
  public static <T1, T2> Pair<T1, T2> pair(T1 first, T2 second) {
    return new Pair(first, second);
  }

  /**
   * Convenience method to return a tree set
   */
  public static <T> TreeSet<T> treeSet() {
    return new TreeSet<>();
  }

  /**
   * Remove and return last item in list
   */
  public static <T> T pop(List<T> list) {
    return list.remove(list.size() - 1);
  }

  public static <T> void push(List<T> stack, T itemToAdd) {
    stack.add(itemToAdd);
  }

  /**
   * Look at item in list, relative to end, where 0 is end of list
   */
  public static <T> T peek(List<T> stack, int distanceFromEnd) {
    return stack.get(stack.size() - 1 - distanceFromEnd);
  }

  /**
   * Look at last item in list, without removal
   */
  public static <T> T last(List<T> list) {
    return peek(list, 0);
  }

  /**
   * Get last item in list
   */
  public static <T> T peek(List<T> stack) {
    return peek(stack, 0);
  }

  /**
   * Get first item in list
   */
  public static <T> T first(List<T> list) {
    return list.get(0);
  }

  /**
   * Construct an Iterable from an Interator, e.g. so they can be used with
   * enhanced for loops
   */
  public static <T> Iterable<T> in(final Iterator<T> iterator) {
    checkNotNull(iterator);
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        checkState(!mUsed);
        mUsed = true;
        return iterator;
      }

      private boolean mUsed;
    };
  }

  /**
   * Remove a contiguous sequence of elements from a list; adjust arguments into
   * range, and do nothing if appropriate
   */
  public static <T> void remove(List<T> list, int start, int count) {
    int end = start + count;
    start = MyMath.clamp(start, 0, list.size());
    end = MyMath.clamp(end, start, list.size());
    if (start < end)
      list.subList(start, end).clear();
  }

  public static <T> void remove(List<T> list, int start) {
    remove(list, start, list.size());
  }

  public static <T> T getMod(List<T> list, int index) {
    return list.get(MyMath.myMod(index, list.size()));
  }

  //------------------------------------------------------------------
  // JSON utilities
  // ------------------------------------------------------------------

  /**
   * Convenience method to construct an empty JSMap
   */
  public static JSMap map() {
    return new JSMap();
  }

  /**
   * Convenience method to construct an empty JSList
   */
  public static JSList list() {
    return new JSList();
  }

  // ------------------------------------------------------------------
  // Program context
  // ------------------------------------------------------------------

  public static boolean testMode() {
    return sTestModeFlag;
  }

  /**
   * Throw exception if not in test mode
   */
  public static void testOnlyAssert() {
    if (!testMode())
      badState("test-only");
  }

  /**
   * Throw exception if in test mode
   */
  public static void nonTestOnlyAssert() {
    if (testMode())
      badState("non-test-only");
  }

  public static void testOnlyAlert() {
    if (!testMode()) {
      alertWithSkip(1, "*** SHOULD ONLY BE CALLED IN TEST MODE");
    }
  }

  private static final boolean sTestModeFlag;

  static {
    // Determine if we're in test mode by seeing if there's a MyTestCase class
    boolean testMode = true;
    try {
      Class.forName("js.testutil.MyTestCase");
    } catch (ClassNotFoundException e) {
      testMode = false;
    }
    sTestModeFlag = testMode;
  }

  public static JSMap mapWithClassName(Object object) {
    JSMap map = map();
    Class c = object.getClass();
    if (object instanceof Class)
      c = (Class) object;
    map.put("", c.getSimpleName());
    return map;
  }

}
