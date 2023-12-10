package js.leetcode;

import static js.base.Tools.*;

public class P105ConstructBinaryTreeFromTraversals {

  public static void main(String[] args) {
    new P105ConstructBinaryTreeFromTraversals().run();
  }

  private void run() {
    x("3 9 5 20 15 7", "5 9 3 15 20 7");
  }

  private void x(String a, String b) {
    var preorder = parse(a);
    var inorder = parse(b);
    var built = buildTree(preorder, inorder);
    pr(built);
  }

  private static int[] parse(String s) {
    var wd = s.split(" ");
    int[] res = new int[wd.length];
    for (int i = 0; i < res.length; i++)
      res[i] = Integer.parseInt(wd[i]);
    return res;
  }

  // ------------------------------------------------------------------

  private static class TreeNode {
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

  public TreeNode buildTree(int[] pre, int[] in) {
    inorder = in;
    preorder = pre;
    return auxBuildTree(0, 0, in.length);
  }

  private static int[] inorder;
  private static int[] preorder;

  private TreeNode auxBuildTree(int preCursor, int inCursor, int treeSize) {

    if (treeSize == 0)
      return null;

    // inorder:  [left] node [right]
    // preorder: node [left] [right]

    int nodeValue = preorder[preCursor];

    // Determine population of [left]
    int leftPop = 0;
    while (inorder[inCursor + leftPop] != nodeValue)
      leftPop++;

    int rightPop = treeSize - (1 + leftPop);
    TreeNode left = auxBuildTree(preCursor + 1, inCursor, leftPop);
    TreeNode right = auxBuildTree(preCursor + 1 + leftPop, inCursor + 1 + leftPop, rightPop);
    return new TreeNode(nodeValue, left, right);
  }

}
