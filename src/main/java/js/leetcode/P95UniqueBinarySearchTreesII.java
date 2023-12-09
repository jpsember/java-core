package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.base.Tools;

/**
 * First attempt: generate binary trees based on permutations. Generates
 * duplicate structures... not sure whether n! is >> number of binary trees. If
 * not, still a valid approach.
 * 
 */
public class P95UniqueBinarySearchTreesII {

  private static int depth;

  private static void pr(Object... messages) {
    Tools.pr(insertStringToFront(spaces(depth * 2), messages));
  }

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
    int[] ord = new int[n];
    for (int i = 0; i < n; i++)
      ord[i] = i + 1;

    List<TreeNode> res = new ArrayList<>();

    for (var order : permuteUnique(ord)) {
      TreeNode parent = null;
      for (var val : order) {
        parent = insert(parent, val);
      }
      res.add(parent);

    }

    return res;
  }

  private TreeNode insert(TreeNode root, int val) {
    if (root == null)
      return new TreeNode(val);

    TreeNode node = root;
    while (true) {
      if (val < node.val) {
        if (node.left != null) {
          node = node.left;
        } else {
          node.left = new TreeNode(val);
          break;
        }
      } else {
        if (node.right != null) {
          node = node.right;
        } else {
          node.right = new TreeNode(val);
          break;
        }
      }
    }
    return root;

  }

  public List<int[]> permuteUnique(int[] nums) {
    results = new ArrayList<>();
    auxPermute(nums, 0);
    return results;
  }

  private static void swap(int[] nums, int a, int b) {
    int tmp = nums[a];
    nums[a] = nums[b];
    nums[b] = tmp;
  }

  private void auxPermute(int[] nums, int index) {

    if (index == nums.length - 1) {
      pr(nums);
      results.add(Arrays.copyOf(nums, nums.length));
      return;
    }

    for (int i = index; i < nums.length; i++) {
      swap(nums, index, i);
      auxPermute(nums, index + 1);
      swap(nums, index, i);
    }
  }

  private List<int[]> results;

}
