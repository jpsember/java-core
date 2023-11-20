package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import js.json.JSList;

// There is a trick... look at sorted lists of start intervals and sorted lists of end intervals, and
// and do merging with that instead...
//
public class P56MergeIntervals {

  public static void main(String[] args) {
    new P56MergeIntervals().run();
  }

  private void run() {
    int[][] intervals = { { 1, 3 }, { 2, 6 }, { 8, 10 }, { 15, 18 } };

    go(intervals);

    int[][] intervals2 = { { 1, 4 }, { 4, 5 } };
    go(intervals2);
  }

  private static void dump(String prompt, int[][] intervals) {
    JSList x = list();
    for (var a : intervals) {
      x.add(JSList.with(a));
    }

    pr(prompt, INDENT, x);
  }

  private void go(int[][] intervals) {
    dump("intervals:", intervals);
    var res = merge(intervals);
    dump("merged:", res);
  }

  public int[][] merge(int[][] intervals) {
    Arrays.sort(intervals, new Comparator<int[]>() {
      @Override
      public int compare(int[] o1, int[] o2) {
        return Integer.compare(o1[0], o2[0]);
      }
    });

    List<int[]> result = new ArrayList<>(intervals.length);
    int[] work = null;
    for (var s : intervals) {
      if (work == null) {
        work = s;
        continue;
      }
      if (work[1] < s[0]) {
        result.add(work);
        work = s;
      }
      if (work[1] >= s[1]) {
        continue;
      }
      var work2 = new int[2];
      work2[0] = work[0];
      work2[1] = s[1];
      work = work2;
    }
    if (work != null)
      result.add(work);

    return result.toArray(new int[0][]);
  }

}
