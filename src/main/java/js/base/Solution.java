// 22. Generate Parenthesis

// Insight: what is a grammar for such parenthesis?

//

package js.base;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Solution {

  public static void main(String[] args) {
    new Solution().run();
  }

  private void run() {
    pr(generateParenthesis(3));
  }

  public List<String> generateParenthesis(int n) {
    List<Set<String>> depthLists = new ArrayList<>();
    for (int j = 0; j < n; j++) {
      auxGen(depthLists, j);
    }
    var last = depthLists.get(n - 1);
    return new ArrayList<>(last);
  }

  private void auxGen(List<Set<String>> depthLists, int depth) {
    Set<String> result = new HashSet<>();
    if (depth == 0) {
      result.add("()");
    } else {
      Set<String> b = depthLists.get(depth - 1);
      for (var x : b) {
        var z = "(" + x + ")";
        result.add(z);
      }

      for (int a = 0; a < depth; a++) {
        b = depthLists.get(a);
        var c = depthLists.get(depth - 1 - a);
        for (String x : b) {
          for (String y : c) {
            var z = x+y;
            result.add(z);
          }
        }
      }
    }
    depthLists.add(result);
  }


}
