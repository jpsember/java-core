package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.json.JSList;

/**
 * I think this is easy? I guess I must be wrong.
 * 
 * Actually, it is easy, once I dealt with a degenerate case.
 */
public class PalindromePairs extends LeetCode {

  public static void main(String[] args) {
    new PalindromePairs().run();
  }

  public void run() {

    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]", "[[0,1],[1,0],[3,2],[2,4]]");

  }

  private void x(String s, String sExp) {
    db = true;

    var words = new JSList(s);
    var exp = new JSList(sExp);

    var wlist = words.asStringArray();
    var result = palindromePairs(wlist);
    var res = list();
    for (var p : result)
      res.add(list().add(p.get(0)).add(p.get(1)));
    pr(res);
    pr("palindromePairs", words, res);
    verify(res, exp);
  }

  // ------------------------------------------------------------------

  public List<List<Integer>> palindromePairs(String[] words) {
    var a = new ArrayList<List<Integer>>();
    var b = new ArrayList<Integer>();
    b.add(7);
    b.add(8);
    a.add(b);
    return a;
  }
}
