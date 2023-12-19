package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

public class SerializeAndDeserializeBinaryTree extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new SerializeAndDeserializeBinaryTree().run();
  }

  public void run() {

    x("[3,5,7,null,2,-1000,null,null,null,4,-6]");

    if (false) {
      var tree = t(3, t(5, null, t(2)), t(7, t(-1000, t(4), t(-6))));

      if (false) {
        var cc = new CodecLC();
        var s = cc.serialize(tree);
        pr("serialized:", s);
        var result = cc.deserialize(s);
        pr("deserialized:", result);
        var s2 = cc.serialize(result);
        verify(s, s2);
      }

      var c = new Codec();
      var s = c.serialize(tree);
      pr(s);
      TreeNode tout = c.deserialize(s);
      pr("tree in :", tree);
      pr("tree out:", tout);

      verify(tree.toString(), tout.toString());
    }
  }

  private void x(String s) {

    if (s.startsWith("[")) {
      var c2 = new CodecLC();
      var tree = c2.deserialize(s);
      s = new Codec().serialize(tree);
      pr("Serialized to our form:", s);
    }
    Codec c = new Codec();

    var tree = c.deserialize(s);
    var s2 = c.serialize(tree);
    pr("Serialized:", INDENT, s);
    pr("Deserialized:", INDENT, tree);
    verify(s2, s);
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
      auxStr(sb, this, 0);
      return sb.toString();
    }

    private void auxStr(StringBuilder sb, TreeNode node, int indent) {
      boolean compact = true;
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

  /**
   * LeetCode tree serializer
   */
  public class CodecLC extends Codec {

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
  }

  // ------------------------------------------------------------------

  public class Codec {

    public String serialize(TreeNode root) {
      List<TreeNode> nodeQueue = new ArrayList<>();
      nodeQueue.add(root);
      StringBuilder sb = new StringBuilder();
      int cursor = 0;
      while (cursor < nodeQueue.size()) {
        var n = nodeQueue.get(cursor++);
        if (n == null) {
          sb.append(".");
        } else {
          {
            var x = n.val;
            if (x < 0) {
              sb.append('-');
              x = -x;
            } else
              sb.append('+');
            char dig1 = (char) ((x & 0x1f) + ENCODE_DIGIT_VALUE);
            var upper = x >> 5;
            if (upper != 0)
              sb.append((char) (upper + ENCODE_DIGIT_VALUE));
            sb.append(dig1);
          }
          nodeQueue.add(n.left);
          nodeQueue.add(n.right);
        }
      }
      trimSuffix(sb, ".");
      return sb.toString();
    }

    public TreeNode deserialize(String data) {
      cursor = 0;
      text = data;

      List<Integer> values = new ArrayList<>();
      while (hasMore()) {
        Integer value = null;
        if (!readIf('.')) {
          boolean neg = readIf('-');
          if (!neg)
            read('+');
          int slot2 = read() - ENCODE_DIGIT_VALUE;
          char p = peek();
          int slot1 = p - ENCODE_DIGIT_VALUE;
          if (slot1 >= 0)
            read();
          else {
            slot1 = slot2;
            slot2 = 0;
          }
          var val = (slot2 << 5) | slot1;
          if (neg)
            val = -val;
          value = val;
        }
        values.add(value);
      }
      return constructTreeFromValues(values);
    }

    // We encode numbers as  ( '+' | '-' )  d [d]
    //
    // where d is a base 32 digit plus ASCII value '^'
    //
    private static final int ENCODE_DIGIT_VALUE = '^';

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

}
