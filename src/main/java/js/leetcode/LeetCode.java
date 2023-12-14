package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import js.data.DataUtil;
import js.json.JSList;

public abstract class LeetCode {

  public abstract void run();

  public void verify(Object result, Object expected) {
    if (result == null)
      result = "<null>";
    if (expected == null)
      expected = "<null>";

    checkState(result.equals(expected), "Got result:", INDENT, result, CR, "but expected:", CR, expected);
  }

  public void verify(int[] result, int[] expected) {
    verify(JSList.with(result), JSList.with(expected));
  }

  public static String bitStr(long val) {
    var s = "";
    for (int i = 0; i <= 31; i++) {
      long bit = 1L << i;
      s = (((val & bit) != 0) ? "1" : "0") + s;
      if (val < (bit << 1))
        break;
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

  public void checkInf(int max) {
    if (mInfiniteLoop++ >= max) {
      badState("infinite loop!");
    }
  }

  public void checkInf() {
    checkInf(120);
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

  private int mInfiniteLoop;

  public static void db(Object... messages) {
    pr(messages);
  }
}
