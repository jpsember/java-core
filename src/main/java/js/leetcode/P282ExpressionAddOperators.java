package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class P282ExpressionAddOperators extends LeetCode {

  public static void main(String[] args) {
    new P282ExpressionAddOperators().run();
  }

  public void run() {
    x(123, 6, "1*2*3", "1+2+3");
  }

  private void x(int num, int target, String... results) {

    var res = addOperators(Integer.toString(num), target);
    var exp = new HashSet<String>(arrayList(results));
    var got = new HashSet<String>(res);

    pr("Number:", num, "Target:", target, INDENT, got);
    verify(got, exp);
  }

  public List<String> addOperators(String num, int target) {
    var res = new ArrayList<String>();
    return res;
  }
}
