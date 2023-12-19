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

    var tree = t(3, t(5, null, t(2)), t(7, t(-1000, t(4), t(-6))));
    var c = new CodecLC();
    var s = c.serialize(tree);
    pr(s);
    TreeNode tout = c.deserialize(s);
    pr("tree in :", tree);
    pr("tree out:", tout);

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

    private static final String dig = "012345abcdefghijklmnopqrstuvwxyz";

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
      db("serialize:", root);
      List<TreeNode> frontier = new ArrayList<>();
      frontier.add(root);
      StringBuilder sb = new StringBuilder();
      int cursor = 0;
      while (cursor < frontier.size()) {
        db("...cursor:", cursor, "string:", quote(sb.toString()));
        var n = frontier.get(cursor++);
        db("...cursor:", cursor, "tree:", n);
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

            char dig1 = dig.charAt(x & 0x1f);
            char dig2 = dig.charAt(x >> 5);
            if (dig2 != '0')
              sb.append(dig2);
            sb.append(dig1);
          }
          frontier.add(n.left);
          frontier.add(n.right);
        }
      }
      trimSuffix(sb, ".");
      //      if (true) {
      //        int newLen = sb.length();
      //        int i = newLen - 1;
      //        while (i >= 0 && sb.charAt(i) == '.') {
      //          newLen = i;
      //          i--;
      //        }
      //        sb.setLength(newLen);
      //      }
      return sb.toString();
    }

    private int cursor;
    private String text;

    private boolean hasMore() {
      return cursor < text.length();
    }

    private char peek() {
      if (hasMore())
        return text.charAt(cursor);
      return 0;
    }

    private char read() {
      char c = peek();
      cursor++;
      return c;
    }

    private boolean readIf(char c2) {
      if (peek() == c2) {
        cursor++;
        return true;
      }
      return false;
    }

    private char read(char c2) {
      if (peek() != c2) {
        throw new IllegalArgumentException();
      }
      return read();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
      db("deserialize:", quote(data));

      List<Integer> values = new ArrayList<>();
      cursor = 0;
      text = data;
      while (hasMore()) {
        db("...peek:", Character.toString(peek()));
        Integer value = null;

        if (!readIf('.')) {
          boolean neg = readIf('-');
          if (!neg)
            read('+');

          char d = read();
          int slot2 = dig.indexOf(d);
          checkState(slot2 >= 0);
          char p = peek();
          int slot1 = dig.indexOf(p);
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
        db("...adding value:", value);
        values.add(value);
      }

      TreeNode result = parse(values, 0);
      return result;
    }

    private TreeNode parse(List<Integer> values, int cursor) {
      if (cursor >= values.size())
        return null;
      Integer val = values.get(cursor++);
      if (val == null)
        return null;
      return new TreeNode(val, parse(values, cursor + 1), parse(values, cursor + 1));
    }
  }

  private static void trimSuffix(StringBuilder sb, String suffix) {
    while (true) {
      int x = sb.length() - suffix.length();
      if (x < 0)
        break;
      if (!sb.substring(x).equals(suffix))
        break;
      sb.setLength(x);
    }
  }

  public class CodecLC extends Codec {

    public String serialize(TreeNode root) {
      db("serialize:", root);
      StringBuilder sb = new StringBuilder();
      if (root == null) {
        sb.append("null");
      } else {
        sb.append('[');
        List<TreeNode> frontier = new ArrayList<>();
        frontier.add(root);
        int cursor = 0;
        while (cursor < frontier.size()) {
          db("...cursor:", cursor, "string:", quote(sb.toString()));
          var n = frontier.get(cursor++);
          db("...cursor:", cursor, "tree:", n);
          if (sb.length() > 1) // && sb.charAt(sb.length() - 1) != '[')
            sb.append(',');
          if (n == null) {
            sb.append("null");
          } else {
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

    private int cursor;
    private String text;

    private boolean hasMore() {
      return cursor < text.length();
    }

    private char peek() {
      if (hasMore())
        return text.charAt(cursor);
      return 0;
    }

    private char read() {
      char c = peek();
      cursor++;
      return c;
    }

    private boolean readIf(char c2) {
      if (peek() == c2) {
        cursor++;
        return true;
      }
      return false;
    }

    private boolean readIf(String s) {
      var cnew = cursor + s.length();
      if (cnew <= text.length() && text.substring(cursor, cnew).equals(s)) {
        cursor = cnew;
        return true;
      }
      return false;
    }

    private char read(char c2) {
      if (peek() != c2) {
        throw new IllegalArgumentException();
      }
      return read();
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
      db("deserialize:", quote(data));
      TreeNode result = null;
      if (!readIf("null")) {
        read('[');

        List<Integer> values = new ArrayList<>();
        cursor = 0;
        text = data;
        while (hasMore()) {
          db("...peek:", Character.toString(peek()));
          Integer value = null;
          if (!readIf("null")) {

            boolean neg = readIf('-');
            int i = 0;
            int oldCursor = cursor;
            while (true) {
              int digit = peek() - '0';
              if (digit < 0 || digit > 9)
                break;
              read();
              i = i * 10 + digit;
            }
            if (cursor == oldCursor)
              throw new IllegalArgumentException();
            value = i;
          }
          db("...adding value:", value);
          values.add(value);
        }

        result = parse(values, 0);
        read(']');
      }
      checkState(cursor == text.length(), "extra characters");
      return result;
    }

    private TreeNode parse(List<Integer> values, int cursor) {
      if (cursor >= values.size())
        return null;
      Integer val = values.get(cursor++);
      if (val == null)
        return null;
      return new TreeNode(val, parse(values, cursor + 1), parse(values, cursor + 1));
    }
  }

}
