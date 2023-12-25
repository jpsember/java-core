package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import js.base.BasePrinter;
import js.data.DataUtil;
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


  public void xx(Object... messages) {
  }


  private static List<Boolean> dbStack = arrayList();
  private static List<Integer> indStack = arrayList();


}
