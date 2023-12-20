package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.leetcode.BinaryTreeMaximumPathSum.TreeNode;

/**
 * LeetCode tree serializer
 */
public class TreeNodeCodec {

  public String serialize(TreeNode root) {
    StringBuilder sb = new StringBuilder();
    if (root == null) {
      sb.append("null");
    } else {
      sb.append('[');
      List<TreeNode> frontier = new ArrayList<>();
      frontier.add(root);
      int cursor = 0;
      while (cursor < frontier.size()) {
        //db("...cursor:", cursor, "string:", quote(sb.toString()));
        var n = frontier.get(cursor++);
        if (sb.length() > 1)
          sb.append(',');
        if (n == null)
          sb.append("null");
        else {
          sb.append(n.val);
          frontier.add(n.left);
          frontier.add(n.right);
        }
      }
      trimSuffix(sb, ",null");
      sb.append(']');
    }
    return sb.toString();
  }

  public TreeNode deserialize(String data) {
    cursor = 0;
    text = data;
    TreeNode result = null;
    if (!readIf("null")) {
      read('[');
      List<Integer> values = new ArrayList<>();
      while (!readIf(']')) {
        //db("...remaining text:", quote(text.substring(cursor)));
        Integer value = null;
        if (!readIf("null")) {
          int number = 0;
          boolean neg = readIf('-');
          int oldCursor = cursor;
          while (true) {
            int digit = peek() - '0';
            if (digit < 0 || digit > 9)
              break;
            read();
            number = number * 10 + digit;
          }
          checkState(cursor > oldCursor);
          if (neg)
            number = -number;
          value = number;
        }
        readIf(',');
        values.add(value);
      }
      result = constructTreeFromValues(values);
    }
    checkState(cursor == text.length(), "extra characters");
    return result;
  }

  protected final boolean hasMore() {
    return cursor < text.length();
  }

  protected final char peek() {
    if (hasMore())
      return text.charAt(cursor);
    return 0;
  }

  protected final char read() {
    char c = peek();
    cursor++;
    return c;
  }

  protected final boolean readIf(char c2) {
    if (peek() == c2) {
      cursor++;
      return true;
    }
    return false;
  }

  protected final char read(char c2) {
    checkState(peek() == c2);
    return read();
  }

  protected final boolean readIf(String s) {
    var cnew = cursor + s.length();
    if (cnew <= text.length() && text.substring(cursor, cnew).equals(s)) {
      cursor = cnew;
      return true;
    }
    return false;
  }

  protected TreeNode constructTreeFromValues(List<Integer> values) {
    if (values.isEmpty())
      return null;
    var treeQueue = new ArrayList<TreeNode>(values.size());
    int treeCursor = 0;
    int cursor = 0;
    var result = new TreeNode(values.get(cursor++), null, null);
    treeQueue.add(result);
    while (treeCursor < treeQueue.size()) {
      var tree = treeQueue.get(treeCursor++);
      Integer val = cursor == values.size() ? null : values.get(cursor++);
      if (val != null) {
        tree.left = new TreeNode(val, null, null);
        treeQueue.add(tree.left);
      }
      val = cursor == values.size() ? null : values.get(cursor++);
      if (val != null) {
        tree.right = new TreeNode(val, null, null);
        treeQueue.add(tree.right);
      }
    }
    return result;
  }

  protected void trimSuffix(StringBuilder sb, String suffix) {
    while (true) {
      int x = sb.length() - suffix.length();
      if (x < 0)
        break;
      if (!sb.substring(x).equals(suffix))
        break;
      sb.setLength(x);
    }
  }

  protected int cursor;
  protected String text;
}