package js.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static js.base.Tools.*;

/**
 * First attempt: generate binary trees based on permutations. Generates
 * duplicate structures... not sure whether n! is >> number of binary trees. If
 * not, still a valid approach.
 * 
 * As I suspected, n! >> |binary trees|.
 * 
 */
public class P95UniqueBinarySearchTreesII {

  public static void main(String[] args) {
    new P95UniqueBinarySearchTreesII().run();
  }

  private void run() {
    x(3);
  }

  private void x(int n) {
    var s = generateTrees(n);
    pr(s);
  }

  public class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode() {
    }

    TreeNode(int val) {
      this.val = val;
    }

    TreeNode(int val, TreeNode left, TreeNode right) {
      this.val = val;
      this.left = left;
      this.right = right;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      auxStr(sb, this);
      return sb.toString();
    }

    private void auxStr(StringBuilder sb, TreeNode node) {
      if (node == null)
        return;
      sb.append(node.val);
      sb.append('[');
      auxStr(sb, node.left);
      sb.append('|');
      auxStr(sb, node.right);
      sb.append("]");
    }
  }

  public List<TreeNode> generateTrees(int n) {
    memo.clear();
    return auxGenerateTrees(1, n);
  }

  private List<TreeNode> auxGenerateTrees(int offset, int n) {
    var key = (offset << 8) | n;
    var res = memo.get(key);
    if (res != null)
      return res;

    res = new ArrayList<>();

    if (n == 0) {
      res.add(null);
    } else {
      for (int leftCount = 0; leftCount < n; leftCount++) {
        var rightCount = n - 1 - leftCount;
        var leftOffset = offset;
        var rootOffset = leftOffset + leftCount;
        var rightOffset = rootOffset + 1;
        var chLeft = auxGenerateTrees(leftOffset, leftCount);
        var chRight = auxGenerateTrees(rightOffset, rightCount);
        for (var left : chLeft) {
          for (var right : chRight) {
            res.add(new TreeNode(rootOffset, left, right));
          }
        }
      }
    }
    memo.put(key, res);
    return res;
  }

  private Map<Integer, List<TreeNode>> memo = new HashMap<>();
}
