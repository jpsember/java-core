package js.leetcode;

import java.util.ArrayList;
import java.util.List;

import js.base.BasePrinter;
import static js.base.Tools.*;

public class P218TheSkylineProblem extends LeetCode {

  public static void main(String[] args) {
    new P218TheSkylineProblem().run();
  }

  public void run() {
    x("[[2,9,10],[3,7,15],[5,12,12],[15,20,10],[19,24,8]] ",
        "  [[2,10],[3,15],[7,12],[12,0],[15,10],[20,8],[24,0]]");
  }

  private void x(String a, String b) {
    var an = extractNums(a);
    int[][] bu = new int[an.length / 3][3];
    int i = 0;

    for (var row : bu) {
      row[0] = an[i++];
      row[1] = an[i++];
      row[2] = an[i++];
    }

    var bn = extractNums(b);
    var result = getSkyline(bu);
    var ss = BasePrinter.toString(result);
    pr(extractNums(ss));
    verify(extractNums(ss), bn);
  }

  public List<List<Integer>> getSkyline(int[][] buildings) {
    return new ArrayList<List<Integer>>();
  }
}
