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
 **/
package js.base;

import java.io.File;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.apache.commons.io.FileUtils;

import js.file.Files;
import js.geometry.MyMath;
import js.json.JSList;
import js.json.JSMap;
import js.system.MemoryMonitor;

public final class Tools {

  /**
   * A do-nothing method that we can call to avoid 'unused import' warnings in
   * Eclipse
   */
  public static void loadTools() {
  }

  // ------------------------------------------------------------------
  // Supporting idiomatic expressions
  // ------------------------------------------------------------------

  /**
   * Initial value for counters used in for-each loops where the item index is
   * required as well, e.g.
   *
   * <pre>
   *
   * int i = INIT_INDEX;
   * for (String s : stringCollection) {
   *   i++;
   *
   *   if (i == ...) ...
   *   ...
   * }
   *
   * I haven't found a cleaner way to accomplish this.
   *
   * </pre>
   *
   * Using this symbolic constant is a good way to indicate this technique is
   * being used.
   */
  public static final int INIT_INDEX = -1;

  // ------------------------------------------------------------------
  // 'Print once' reports
  // ------------------------------------------------------------------

  /**
   * Print alert message only once. Thread safe. Always returns true
   */
  public static boolean alert(String key, Object... message) {
    auxAlert(1, key, "ALERT", message);
    return true;
  }

  public static boolean todo(String key, Object... message) {
    auxAlert(1, key, "TODO", message);
    return true;
  }

  private static Map<String, Integer> sAlertCounterMap = concurrentHashMap();

  private static class AlertInfo {
    String key;
    long delayMs;
    int maxPerSession;
    int skipCount;
  }

  private static int[] extractInt(String s, int cursor) {
    var newCursor = cursor;
    var value = 0;
    while (newCursor < s.length()) {
      var ch = s.charAt(newCursor);
      if (ch < '0' || ch > '9')
        break;
      value = value * 10 + (int) (ch - '0');
      newCursor++;
    }
    var result = new int[2];
    result[0] = newCursor;
    result[1] = value;
    return result;
  }

  // Parse an alert key into an alertInfo structure.
  // Can contain zero or more prefixes of the form:
  //
  //-              Never print
  //!          Print about once per day
  //?              Print about once per month
  //#[0-9]+              Print n times, every time program is run
  //<[0-9]+              Skip first n entries in stack trace
  //
  private static AlertInfo extractAlertInfo(String key) {

    final long minute = 60 * 1000;
    final long hour = minute * 60;

    var info = new AlertInfo();
    info.maxPerSession = 1;
    var cursor = 0;
    while (cursor < key.length()) {
      var ch = key.charAt(cursor);
      cursor++;
      if (ch == '-') {
        info.maxPerSession = 0;
        break;
      }
      if (ch == '!') {
        info.delayMs = hour * 24;
      } else if (ch == '?') {
        info.delayMs = hour * 24 * 31;
      } else if (ch == '#') {
        var x = extractInt(key, cursor);
        cursor = x[0];
        info.maxPerSession = x[1];
      } else if (ch == '<') {
        var x = extractInt(key, cursor);
        cursor = x[0];
        info.skipCount += x[1];
      } else if (ch == ' ') {
        // ignore leading spaces
      } else {
        cursor--;
        break;
      }
    }

    info.key = key.substring(cursor);

    if (DEBUG_ALERTS) {
      var m = map();
      m.put("", key);
      m.put("key", info.key).put("delayMS", info.delayMs).put("maxPerSession", info.maxPerSession)
          .put("skipCount", info.skipCount);
      System.out.println("Extracted alert info from: " + key + "\n" + m.prettyPrint());
    }
    return info;
  }

  private static void auxAlert(int skipCount, String key, String prompt, Object... args) {
    var info = extractAlertInfo(key);
    var cachedInfo = sAlertCounterMap.getOrDefault(info.key, 0) + 1;
    sAlertCounterMap.put(info.key, cachedInfo);

    // If we are never to print this alert, exit now
    if (info.maxPerSession == 0) {
      return;
    }

    // If there's a multi-session priority value, process it
    //
    if (info.delayMs > 0) {
      synchronized (sReportCountMap) {
        JSMap flagsMap = sPersistedAlertFlagsMap;
        File flagsFile = sPersistedAlertFlagsFile;
        if (flagsMap == null) {
          flagsFile = sPersistedAlertFlagsFile = new File(Files.S.optProjectConfigDirectory(), "alerts.json");
          if (DEBUG_ALERTS)
            pr("...reading flags map from file:", INDENT, Files.infoMap(flagsFile));
          flagsMap = sPersistedAlertFlagsMap = JSMap.fromFileIfExists(flagsFile);
        }
        final int expectedVersion = 1;
        final String VERSION_KEY = "~~version~~";
        if (flagsMap.opt(VERSION_KEY, 0) != expectedVersion)
          flagsMap.clear().put(VERSION_KEY, expectedVersion);

        var m = flagsMap.optJSMapOrEmpty(info.key);
        var currTime = System.currentTimeMillis();
        var lastReport = m.opt("r", 0L);
        var elapsed = currTime - lastReport;
        checkArgument(elapsed >= 0);
        if (DEBUG_ALERTS)
          pr("elapsed time (ms):", elapsed);

        if (elapsed < info.delayMs)
          return;
        m.put("r", currTime);
        flagsMap.put(info.key, m);
        if (DEBUG_ALERTS)
          pr("writing flags map to:", sPersistedAlertFlagsFile, INDENT, flagsMap);
        Files.S.writeString(sPersistedAlertFlagsFile, flagsMap.toString());
      }
    } else {
      // If we've exceeded the max per session count, exit now
      if (cachedInfo > info.maxPerSession)
        return;
    }

    var sb = prepareAlertBuffer(prompt);

    getStackTraceElement(sb, 1 + skipCount + info.skipCount);

    if (args.length > 0) {
      sb.append(info.key + " " + BasePrinter.toString(args));
    } else {
      sb.append(info.key);
    }

    // I think we want to be calling our logger instead of System.out!
    pr(sb);
  }

  /**
   * Print alert message only n times. Thread safe. Always returns true
   */
  public static boolean alertMaxTimes(int maxTimes, Object... messageObjects) {
    String message = reportMaxTimes(null, maxTimes, 1, messageObjects);
    if (message != null)
      pr(message);
    return true;
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
   * @param oper code to run if this is the first time called from the location
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

  private static StringBuilder prepareAlertBuffer(String type) {
    StringBuilder sb = new StringBuilder();
    sb.append("*** ");
    sb.append(ifNullOrEmpty(type, "WARNING"));
    sb.append(" .");  // Add a period so IntelliJ makes a clickable link
    return sb;
  }

  private static String reportMaxTimes(String type, int limit, int stackFrameSkipCount,
                                       Object... messageObjects) {
    var sb = prepareAlertBuffer(type);
    int fileModeSentinalPosition = sb.length() + 2;

    int locationPosition = sb.length();

    if (messageObjects.length > 0) {
      sb.append(": ");
      sb.append(BasePrinter.toString(messageObjects));
    }

    String reportKey = sb.toString();
    Integer reportCount;
    synchronized (sReportCountMap) {
      reportCount = sReportCountMap.get(reportKey);
      if (reportCount == null) {
        reportCount = 0;
        mem().monitorSize("base reports", sReportCountMap);
      }
      sReportCountMap.put(reportKey, reportCount + 1);
    }
    if (reportCount >= limit)
      return null;

    StringBuilder s2 = getStackTraceElement(null, 1 + stackFrameSkipCount);
    sb.insert(locationPosition, spaces(12 - locationPosition) + s2);
    String reportText = sb.toString();

    if (fileModeSentinalPosition < reportText.length()
        && reportText.charAt(fileModeSentinalPosition) == '!') {
      reportText = reportText.substring(0, fileModeSentinalPosition)
          + reportText.substring(fileModeSentinalPosition + 1);
      synchronized (sReportCountMap) {
        JSMap flagsMap = sPersistedAlertFlagsMap;
        File flagsFile = sPersistedAlertFlagsFile;
        if (flagsMap == null) {
          flagsFile = sPersistedAlertFlagsFile = Files.getDesktopFile("_alerts_.json");
          flagsMap = sPersistedAlertFlagsMap = JSMap.fromFileIfExists(flagsFile);
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

  public static StringBuilder constructStringBuilderIfNec(StringBuilder sbOrNull) {
    if (sbOrNull == null)
      sbOrNull = new StringBuilder();
    return sbOrNull;
  }

  /**
   * Get stack trace element at a particular depth within the current thread,
   * converted to a string that allows clicking on within (an
   * intelligent-enough) IDE.
   */
  private static StringBuilder getStackTraceElement(StringBuilder sbOrNull, int stackDepth) {
    StringBuilder sb = constructStringBuilderIfNec(sbOrNull);
    stackDepth += 2;
    StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
    if (stackTrace.length <= stackDepth) {
      sb.append("(no StackTraceElement available)");
    } else {
      StackTraceElement element = stackTrace[stackDepth];

      // The returned string should contain (filename:linenumber) so the links are clickable within Eclipse;
      // see https://stackoverflow.com/questions/6469445/
      //
      sb.append('(');
      sb.append(element.getFileName());
      sb.append(':');
      sb.append(element.getLineNumber());
      sb.append(") ");
      if (false) {
        // The method name maybe just adds unnecessary clutter.
        sb.append(element.getMethodName());
      }
    }
    return sb;
  }

  private static final boolean DEBUG_ALERTS = false;

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

  /**
   * Convert an from underscores to 'camel case' string
   */
  public static String convertUnderscoreToCamel(String underscoreString) {
    String mod = underscoreString.toLowerCase();
    StringBuilder sb = new StringBuilder();
    for (String s : split(mod, '_')) {
      if (nonEmpty(s)) {
        sb.append(Character.toUpperCase(s.charAt(0)));
        sb.append(s.substring(1));
      }
    }
    return sb.toString();
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
   * Insert '|' characters at the start of each line (to prevent the pr() method
   * from messing with linefeeds)
   */
  public static String insertLeftMargin(Object object) {
    return auxInsertMargins(object, "|", "\n");
  }

  /**
   * Insert left and right margins at the start and end of each line
   */
  public static String insertLeftRightMargins(Object object) {
    return auxInsertMargins(object, "\u21e8 ", "\u21e6\n");
  }

  private static String auxInsertMargins(Object object, String leftMargin, String rightMargin) {
    StringBuilder sb = new StringBuilder();
    for (String s : split(object.toString(), '\n')) {
      sb.append(leftMargin);
      sb.append(s);
      sb.append(rightMargin);
    }
    return sb.toString();
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
    while (cursor > 0 && string.charAt(cursor - 1) <= ' ')
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

  public static String checkNonEmpty(String string, Object... messages) {
    if (nullOrEmpty(string))
      throw badArg("Null or empty string!", BasePrinter.toString(messages));
    return string;
  }

  // ------------------------------------------------------------------
  // Timing utilities
  // ------------------------------------------------------------------

  public static void checkpoint(Object... messages) {
    long currentCheckpoint = System.currentTimeMillis();
    long last = sPreviousCheckpoint;
    if (last == 0)
      last = currentCheckpoint;
    long elapsed = currentCheckpoint - last;
    sPreviousCheckpoint = currentCheckpoint;
    String timestamp = String.format("%5.3f |", elapsed / 1000f);
    pr(insertStringToFront(timestamp, messages));
  }

  private static long sPreviousCheckpoint;

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
   * Determine if a collection is null or is empty
   */
  public static boolean nullOrEmpty(Collection collection) {
    return collection == null || collection.isEmpty();
  }

  /**
   * Determine if a collection is non-null and nonempty
   */
  public static boolean nonEmpty(Collection collectionOrNull) {
    return !nullOrEmpty(collectionOrNull);
  }

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
      K key = (K) keysAndValuesList[i];
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

  /**
   * Remove all elements at or beyond a particular position; adjust arguments
   * into range, and do nothing if appropriate
   */
  public static <T> void removeAllButFirstN(List<T> list, int start) {
    remove(list, start, list.size());
  }

  /**
   * Remove all elements except the last n, doing nothing if there are <= n
   * elements
   */
  public static <T> void removeAllButLastN(List<T> list, int n) {
    remove(list, 0, list.size() - n);
  }

  @Deprecated // Rename to removeAllButFirstN
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

  @Deprecated
  public static boolean testMode() {
    return false;
  }

  /**
   * Throw exception if not in test mode
   */
  @Deprecated
  public static void testOnlyAssert() {
    if (!testMode())
      badState("test-only");
  }

  /**
   * Throw exception if in test mode
   */
  @Deprecated
  public static void nonTestOnlyAssert() {
    if (testMode())
      badState("non-test-only");
  }

  @Deprecated
  public static void testOnlyAlert() {
    if (!testMode()) {
      alert("<1SHOULD ONLY BE CALLED IN TEST MODE");
    }
  }

  public static JSMap mapWithClassName(Object object) {
    JSMap map = map();
    Class c = object.getClass();
    if (object instanceof Class)
      c = (Class) object;
    map.put("", c.getSimpleName());
    return map;
  }

  public static MemoryMonitor mem() {
    return MemoryMonitor.sharedInstance();
  }

  // ------------------------------------------------------------------
  // Calls a client method with a logging message, in a thread safe way
  // ------------------------------------------------------------------

  public static void setWtfHandler(BiConsumer<Integer, Object[]> handler) {
    synchronized (Tools.class) {
      checkState(sWtfCallback == null, "already a handler");
      sWtfCallback = handler;
    }
  }

  public static void wtf(Object... msgs) {
    synchronized (Tools.class) {
      if (sWtfCallback == null)
        return;
      pr(insertStringToFront("" + sWtfCounter, msgs));
      sWtfCallback.accept(sWtfCounter, msgs);
      sWtfCounter++;
    }
  }

  public static void pw(Object... msgs) {
    int skipCount = 0;
    synchronized (Tools.class) {
      String msg = BasePrinter.toString(msgs);
      StringBuilder sb = getStackTraceElement(null, 1 + skipCount);
      sb.append(' ');
      sb.append(msg);
      pr(sb);
    }
  }

  @Deprecated
  public static boolean mark(Object... msgs) {
    alertWithSkip(1, msgs);
    return true;
  }

  private static BiConsumer<Integer, Object[]> sWtfCallback;
  private static int sWtfCounter;

  /**
   * A main() method for quick tests within the IDE
   */
  public static void main(String[] args) {
    pr("hello");
    todo("!this has an exclamation mark");
    FileUtils.getUserDirectory();
  }
}
