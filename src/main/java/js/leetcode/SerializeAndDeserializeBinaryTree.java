package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

/**
 * The running time is in the bottom 20%.
 * 
 * Now using an approach where we add values to a bucket, unsorted, until a
 * median request happens. At that point, if there are multiple buckets, we walk
 * from the left and right sides of the bucket list, "chopping" off buckets
 * since a 'low' bucket will be balanced by a 'high' bucket of the same size.
 * When both cursors reach the same bucket, then we either split that bucket (if
 * its size is large) or we sort the bucket and extract the median (with special
 * care for the cursor positions with which we arrived at that bucket). A
 * special case is made for the cursors pointing to values that are adjacent but
 * are split between two buckets.
 *
 *
 * To calculate a median, we can discard equal-sized "chunks" of numbers that
 * lie on opposite sides of the median, and that these chunks do not need to be
 * sorted.
 * 
 * The underlying data structure is an array of buckets, where each bucket
 * contains an array of unsorted numbers. The array of buckets is sorted so that
 * B(i)'s min and max numbers are strictly less than that of B(i+1)'s.
 * 
 * With each AddNum(x) call, we determine which of the existing buckets should
 * receive x. We choose the bucket whose min and max range already contain x, or
 * if no such bucket exists, the bucket whose distance from this range is
 * minimized.
 * 
 * For FindMedian(), we set up a pair of cursors to walk from the first and last
 * buckets towards the center. Each cursor has a pointer to a bucket and a count
 * of the numbers processed by the procedure. At each step, we add the smaller
 * bucket's population to each count, and move the bucket pointers inward when
 * all the bucket's numbers are consumed. We do this until either the two
 * cursors point at the same bucket. At this point, if the bucket is small
 * enough, we sort that bucket's contents, and use the cursor counts to
 * determine the median. A special case exists when one cursor is pointing to
 * the max value in one bucket, and the next cursor is pointing to the minimum
 * value in the adjacent bucket. The median is then the average of one bucket's
 * min() and the other bucket's max().
 * 
 * If the above procedure reaches a bucket whose size is large enough, then
 * instead of sorting the bucket, we split the bucket by partitioning its
 * numbers based on the average of the bucket's min and max values (if min =
 * max, then all the numbers in the bucket are identical, and no splitting or
 * sorting is required). We replace the original bucket with the two smaller
 * buckets, and repeat the FindMedian() procedure.
 * 
 * The expensive sorting step in FindMedian() can be optimized by keeping track
 * of how many of a bucket's numbers are already sorted. When adding new numbers
 * to an already-sorted bucket, the numbers are appended to the end of the
 * array. When sorting is next required, we sort only the range of new numbers
 * (at the end of the array), then perform a linear, in-space merge of the two
 * subarrays.
 * 
 * 
 * $$O(n log n)$$ in the worst case, due to the bucket sorting. (The linear scan
 * of buckets in AddNum() might imply a running time of $$O(n^2)$$, but this
 * could only happen if the bucket count was a significant fraction of $$n$$,
 * which is not the case: buckets are only created when a bucket gets large,
 * which implies the bucket count is bounded by $$O(log n)$$).
 * 
 */
public class SerializeAndDeserializeBinaryTree extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new SerializeAndDeserializeBinaryTree().run();
  }

  public void run() {

    // x("3[50[-3000[|]|5[|]]|7[|]]");
     var tree = t(3, t(5, null, t(2)), t(7, t(-3000, t(4), t(-6))));
   // var tree = t(1, null, t(2, t(3), null));
    var c = new Codec();
    var s = c.serialize(tree);
    pr(s);
    TreeNode tout = c.deserialize(s);
    verify(tree.toString(), tout.toString());
  }

  private static TreeNode t(int val) {
    return new TreeNode(val, null, null);
  }

  private static TreeNode t(int val, TreeNode left) {
    return new TreeNode(val, left, null);
  }

  private static TreeNode t(int val, TreeNode left, TreeNode right) {
    return new TreeNode(val, left, right);
  }

  private void x(String treeStr) {

  }

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

  public class Codec {

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {

      List<TreeNode> frontier = new ArrayList<>();
      frontier.add(root);
      StringBuilder sb = new StringBuilder("[");
      int cursor = 0;
      while (cursor < frontier.size()) {
        var n = frontier.get(cursor++);
        pr("popped tree:");
        if (n != null) {
          pr("....val:", n.val, "left:", n.left != null, "right:", n.right != null);
        }
        if (n == null) {
          sb.append("null,");
        } else {
          sb.append(n.val);
          sb.append(',');
          frontier.add(n.left);
          frontier.add(n.right);
        }
      }
      if (true) {
        int i = sb.length() - 5;
        while (i >= 0 && sb.subSequence(i, i + 5).equals("null,")) {
          sb.setLength(i);
          i -= 5;
        }
        if (sb.charAt(sb.length() - 1) == ',')
          sb.setLength(sb.length() - 1);
      }
      sb.append(']');
      return sb.toString();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
      return new TreeNode(0, null, null);
    }
  }
}
