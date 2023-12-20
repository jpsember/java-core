package js.leetcode;

import static js.base.Tools.*;

public class BinaryTreeMaximumPathSum extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new BinaryTreeMaximumPathSum().run();
  }

  public void run() {
    x("[-10,9,20,null,null,15,7]", 42);
  }

  private void x(String s, int expected) {
    var tree = deserializeTree(s);
    var answer = maxPathSum(tree);
    pr(s, INDENT, tree, "answer:", answer);
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
    return 7;
  }
}
