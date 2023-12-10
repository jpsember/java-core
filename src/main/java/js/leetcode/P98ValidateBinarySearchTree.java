package js.leetcode;

public class P98ValidateBinarySearchTree {

  // ------------------------------------------------------------------

  private static class TreeNode {
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

  public boolean isValidBST(TreeNode root) {
    return aux(root, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  private boolean aux(TreeNode root, long minVal, long maxVal) {
    if (root == null)
      return true;
    if (root.val < minVal || root.val > maxVal)
      return false;
    return aux(root.left, minVal, root.val - 1L) && aux(root.right, root.val + 1L, maxVal);
  }

}
