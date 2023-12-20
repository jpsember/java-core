package js.leetcode;

import static js.base.Tools.*;

public class BinaryTreeMaximumPathSum extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new BinaryTreeMaximumPathSum().run();
  }

  public void run() {
    x("[1,-2,3]", 4);
    x("[5,4,8,11,null,13,4,7,2,null,null,null,1]", 48);
    x("[1,2,3]", 6);
    x("[1]", 1);
    x("[-10,9,20,null,null,15,7]", 42);
  }

  private void x(String s, int expected) {
    var tree = deserializeTree(s);
    pr(VERT_SP, s, CR, tree);
    var answer = maxPathSum(tree);
    pr(INDENT, "answer:", answer);
    verify(answer, expected);
  }

  private static TreeNode deserializeTree(String s) {
    return new TreeNodeCodec().deserialize(s);
  }

  static class TreeNode {
    int val;
    TreeNode left;
    TreeNode right;

    TreeNode(int val, TreeNode left, TreeNode right) {
      this.val = val;
      this.left = left;
      this.right = right;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      auxStr(sb, this, 0);
      return sb.toString();
    }

    private void auxStr(StringBuilder sb, TreeNode node, int indent) {
      boolean compact = false;
      String sp;
      if (compact)
        sp = " ";
      else
        sp = spaces(indent);
      if (node == null) {
        sb.append(sp);
        sb.append("null");
      } else {
        if (node.left == null && node.right == null) {
          sb.append(sp);
          sb.append(node.val);
        } else {
          sb.append(sp);
          sb.append('(');
          sb.append(node.val);
          if (!compact)
            sb.append('\n');
          auxStr(sb, node.left, indent + 2);
          if (!compact)
            sb.append('\n');
          auxStr(sb, node.right, indent + 2);
          sb.append(')');
        }
      }
    }

  }

  // ------------------------------------------------------------------

  public int maxPathSum(TreeNode root) {
    maxSum = Integer.MIN_VALUE;
    rewrite(root);
    return maxSum;
  }

  private TreeNode rewrite(TreeNode t) {
    if (t == null)
      return t;
    rewrite(t.left);
    rewrite(t.right);

    // Get our value, and the values of the children, or zero if there are none or are negative
    var vParent = t.val;
    var vLeft = (t.left != null) ? t.left.val : 0;
    var vRight = (t.right != null) ? t.right.val : 0;
    if (vLeft < 0)
      vLeft = 0;
    if (vRight < 0)
      vRight = 0;

    // Summing these three values gives maximum value of any path contained by Left->Parent->Right.
    var x = vParent + vLeft + vRight;
    maxSum = x > maxSum ? x : maxSum;

    // Replace the parent node with a leaf node that includes the value of the maximum child (if positive)
    t.left = null;
    t.right = null;
    t.val = vParent + (vLeft > vRight ? vLeft : vRight);
    return t;
  }

  private int maxSum;
}
