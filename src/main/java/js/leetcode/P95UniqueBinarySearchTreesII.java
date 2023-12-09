package js.leetcode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

  private static int depth;

  public static void main(String[] args) {
    new P95UniqueBinarySearchTreesII().run();
  }

  private void run() {
    x(3);
  }

  private void x(int n) {
    var s = generateTrees(n);
    pn(s);
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
    return auxGenerateTrees(1, n);
  }

  private List<TreeNode> auxGenerateTrees(int offset, int n) {

    depth++;
    pn("genTrees, n:", n, "offset:", offset);
    List<TreeNode> res = new ArrayList<>();

    if (n == 0) {
      res.add(null);
    } else

      for (int leftCount = 0; leftCount < n; leftCount++) {
        var rightCount = n - 1 - leftCount;
        pn("...left:", leftCount, "right:", rightCount);
        var leftOffset = offset;
        var rootOffset = leftOffset + leftCount;
        var rightOffset = rootOffset + 1;

        var chLeft = auxGenerateTrees(leftOffset, leftCount);
        pn("...left trees:", chLeft);
        var chRight = auxGenerateTrees(rightOffset, n - 1 - leftCount);
        pn("...right trees:", chRight);

        for (var left : chLeft) {
          //int i = 0; i <  chLeft.size(); i++) {
          //        TreeNode left = (i < chLeft.size() ? chLeft.get(i) : null);
          for (var right : chRight) {
            //          int j = 0; j <= chRight.size(); j++) {
            //          TreeNode right = (j < chRight.size() ? chRight.get(j) : null);
            var root = new TreeNode(rootOffset, left, right);
            res.add(root);
            if (depth == 1)
              pn("...generated:", root);
          }
        }
      }
    depth--;
    return res;
  }

  private static void pn(Object... messages) {
    //    if (depth <= 1)
    //      pr(insertStringToFront(spaces(depth * 2), messages));
  }
}
