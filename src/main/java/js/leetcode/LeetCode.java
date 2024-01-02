package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import js.base.BasePrinter;
import js.data.DataUtil;
import js.geometry.MyMath;
import js.json.JSList;
import js.json.JSObject;

public abstract class LeetCode {

  public abstract void run();

  public void verify(Object result, Object expected) {
    if (result == null)
      result = "<null>";
    if (expected == null)
      expected = "<null>";

    checkState(result.equals(expected), "Got result:", INDENT, result, CR, "but expected:", CR, expected);
  }

  public void verify(JSObject result, JSObject expected) {
    verify(result.prettyPrint(), expected.prettyPrint());
  }

  public void verify(int[] result, int[] expected) {
    verify(JSList.with(result), JSList.with(expected));
  }

  public static String bitStr(long val) {
    var s = "";
    for (int i = 0; i <= 63; i++) {
      long bit = 1L << i;
      s = (((val & bit) != 0) ? "1" : "0") + s;
      if (val < (bit << 1))
        break;
    }
    return s;
  }

  public static String bitStrFull(long val) {
    var s = "";
    for (int i = 0; i <= 63; i++) {
      long bit = 1L << i;
      s = (((val & bit) != 0) ? "1" : "0") + s;
    }
    return s;
  }

  public static int[] extractNums(String s) {
    var cs = "[],";
    for (int i = 0; i < cs.length(); i++) {
      s = s.replace(cs.charAt(i), ' ');
    }
    var res = new ArrayList<Integer>();
    for (var q : split(s, ' ')) {
      if (!q.isEmpty())
        res.add(Integer.parseInt(q));
    }
    return DataUtil.intArray(res);
  }

  public static int[][] extractMatrix(String s, int width) {
    var ints = extractNums(s);
    checkArgument(ints.length % width == 0, "unexpected count:", ints.length, "not multiple of width", width);
    int[][] result = new int[ints.length / width][width];
    int i = 0;
    for (var row : result) {
      for (int j = 0; j < width; j++, i++) {
        row[j] = ints[i];
      }
    }
    return result;
  }

  public static int[] subints(int[] array, int min, int max) {
    return Arrays.copyOfRange(array, min, max);
  }

  public static void checkInf(int max) {
    if (sInifiniteLoop++ >= max) {
      badState("infinite loop!");
    }
  }

  public static void checkInf() {
    checkInf(120);
  }

  public static void resetInf() {
    sInifiniteLoop = 0;
  }

  public static void swap(int[] nums, int a, int b) {
    var tmp = nums[a];
    nums[a] = nums[b];
    nums[b] = tmp;
  }

  public Random rand() {
    if (mRandom == null)
      rand(1965);
    return mRandom;
  }

  public Random rand(int seed) {
    mRandom = new Random(seed);
    return rand();
  }

  private Random mRandom;

  private static int sInifiniteLoop;

  private static int dbIndent;

  @Deprecated
  public static void adjustIndent(int amt) {
    if (!db)
      return;
    if (indStack.size() > 100)
      badState("indent stack overflow");
    push(indStack, dbIndent);
    dbIndent += amt;
  }

  public static void pushIndent() {
    if (!db)
      return;
    pushIndent(4);
  }

  public static void pushIndent(int amt) {
    if (!db)
      return;
    if (indStack.size() > 100)
      badState("indent stack overflow");
    push(indStack, dbIndent);
    dbIndent += amt;
  }

  public static void popIndent() {
    if (!db)
      return;
    dbIndent = pop(indStack);
  }

  @Deprecated
  public static void resetIndent() {
    dbIndent = 0;
  }

  public static void db(Object... messages) {
    if (!db)
      return;
    if (dbIndent == 0)
      pr(messages);
    else {
      var s = new BasePrinter();
      s.mIndentColumn = dbIndent;
      s.pr(messages);
      s.resetIndentation();
      logger().println(s.toString());
    }
  }

  public void xx(Object... messages) {
  }

  public static Object darray(int[] array, int size) {
    if (!db)
      return null;
    size = Math.min(size, array.length);
    int maxSize = 30;
    if (size <= maxSize) {
      return JSList.with(Arrays.copyOfRange(array, 0, size)).toString();
    }
    return JSList.with(Arrays.copyOfRange(array, 0, maxSize)).toString() + "... size:" + size;
  }

  public static Object darray(int[] array) {
    return darray(array, array.length);
  }

  public static void pushDb(boolean state) {
    if (dbStack.size() > 100)
      badState("db stack overflow");
    dbStack.add(db);
    db = state;
  }

  public static void popDb() {
    db = pop(dbStack);
  }

  public static String strTable(int[][] m) {
    if (!db)
      return "";

    int ht = m.length;
    int wd = m[0].length;

    var sb = new StringBuilder();
    var dash = "---------------------------------";
    int fieldWidth = 5;
    int lblWidth = 6;
    var dl = wd * fieldWidth + fieldWidth;
    while (dash.length() < dl)
      dash = dash + dash;
    dash = dash.substring(0, dl) + "\n";
    sb.append(dash);
    for (int y = ht - 1; y >= 0; y--) {
      var c = sb.length();
      sb.append(y + " |");
      var row = m[y];
      int x = -1;
      for (var val : row) {
        x++;
        tab(sb, c + lblWidth + x * fieldWidth);
        if (val == 0)
          sb.append("_");
        else
          sb.append(val);
      }
      sb.append('\n');
    }
    sb.append(dash);

    {
      var c = sb.length();
      for (int x = 0; x < wd; x++) {
        tab(sb, c + lblWidth + x * fieldWidth);
        sb.append(x);
      }
      sb.append('\n');
    }

    return sb.toString();
  }

  public StringBuilder sb() {
    return new StringBuilder();
  }

  public String randWord(String alphabet, int avgLength) {
    if (alphabet == null)
      alphabet = "abcdefghjkmnpqrstuvwxyz";
    int len = MyMath.clamp((int) ((rand().nextGaussian() + 0.5) * avgLength), 1, avgLength * 3);
    var sb = sb();
    for (int i = 0; i < len; i++)
      sb.append(alphabet.charAt(rand().nextInt(alphabet.length())));
    return sb.toString();
  }

  public List<String> randWords(int count) {
    var words = new HashSet();
    while (words.size() < count) {
      words.add(randWord("abcdefgh", 3));
    }
    return new ArrayList<>(words);
  }

  private static List<Boolean> dbStack = arrayList();
  private static List<Integer> indStack = arrayList();

  public static boolean db;

}
