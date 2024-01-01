package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import js.json.JSList;

/**
 * I think I need to use a trie data structure
 */
public class PalindromePairs extends LeetCode {

  public static void main(String[] args) {
    new PalindromePairs().run();
  }

  private static String rep(String s, int count) {
    var sb = new StringBuilder();
    while (count-- > 0)
      sb.append(s);
    return sb.toString();
  }

  public void run() {
    x("[\"a\",\"\"]", "[[0,1],[1,0]]");
    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]", "[[0,1],[1,0],[3,2],[2,4]]");
    x("[\"z\",\"a\",\"bcd\",\"cb\"]", "[[2,3]]");
    x("[\"zy\",\"abx\",\"ba\",\"bb\"]", "[[1,2]]");
    x("[\"abc\",\"ba\",\"cba\",\"dcba\"]", "[[0,1],[0,2],[0,3],[2,0]]");

    {
      var wd = list();
      var as = rep("a", 300);
      int run = 150;
      run = 8;
      wd.add(as.substring(0, run - 1) + "b" + as.substring(0, run));
      wd.add("a");
      wd.add(as.substring(0, run) + "b" + as.substring(0, run - 1));
      pr(wd);
      var exp = new JSList("[ [ 0,2 ],[ 1,0 ],[ 2,0 ],[ 2,1 ] ]");
      y(wd, exp);
    }

    todo("But why does this work?");
    todo("And how can we be sure it's O(n)?");
  }

  private void x(String s, String sExp) {
    db = true;
    var words = new JSList(s);
    var exp = new JSList(sExp);
    y(words, exp);

  }

  private void y(JSList words, JSList exp) {
    var wlist = words.asStringArray();
    var result = palindromePairs(wlist);
    var res = list();
    for (var p : result)
      res.add(list().add(p.get(0)).add(p.get(1)));
    db("palindromePairs", words, INDENT, res);
    sort(res);
    sort(exp);
    verify(res, exp);
  }

  private void sort(JSList res) {
    Map<String, Object> sorted = new TreeMap<>();
    for (JSList x : res.asLists()) {
      sorted.put(x.toString(), x);
    }
    var s = list();
    for (var ent : sorted.entrySet())
      s.addUnsafe(ent.getValue());
    res.clear();
    res.append(s);
  }

  // ------------------------------------------------------------------

  public List<List<Integer>> palindromePairs(String[] wordsw) {
    result.clear();

    byte[][] wordsAsBytes = new byte[wordsw.length][];
    for (int i = 0; i < wordsw.length; i++) {
      wordsAsBytes[i] = stringToBytes(wordsw[i]);
      pr("cvt:", wordsw[i], "to:", JSList.with(wordsAsBytes[i]));
    }

    // Construct a trie.
    // Add all the forward (normal) words to it.
    // Also add all the backward (reversed) words to it.
    // Store appropriate indices in the leaf nodes.
    var trie = new Trie();
    for (int i = 0; i < wordsAsBytes.length; i++) {
      trie.add(wordsAsBytes[i], i, true);
      trie.add(wordsAsBytes[i], i, false);
    }
    db("Trie:", INDENT, trie);

    lookForPrefixWord(trie, trie);
    return result;
  }

  private void lookForPrefixWord(Trie root, Trie node) {
    if (node == null)
      return;

    // Case 1: Is this w'.end?   (' means bwd)
    if (node.bwdIndex >= 0)
      lookForMatchingWordAsSuffix(node.bwdIndex, node, root);

    // Case 2: Is this v.end?
    if (node.fwdIndex >= 0)
      lookForMatchingWordAsPrefix(node.fwdIndex, node, root);

    for (var child : node.children)
      lookForPrefixWord(root, child);
  }

  private void addResult(int v, int w) {
    if (v == w)
      return;
    var r = new ArrayList<Integer>(2);
    r.add(v);
    r.add(w);
    result.add(r);
  }

  private void lookForMatchingWordAsSuffix(int bwdIndex, Trie t1, Trie t2) {
    if (t1 == null || t2 == null)
      return;
    if (t1.fwdIndex >= 0)
      addResult(t1.fwdIndex, bwdIndex);
    for (int i = 0; i < 26; i++)
      lookForMatchingWordAsSuffix(bwdIndex, t1.children[i], t2.children[i]);
  }

  private void lookForMatchingWordAsPrefix(int fwdIndex, Trie t1, Trie t2) {
    if (t1 == null || t2 == null)
      return;
    if (t1.bwdIndex >= 0)
      addResult(fwdIndex, t1.bwdIndex);
    for (int i = 0; i < 26; i++)
      lookForMatchingWordAsPrefix(fwdIndex, t1.children[i], t2.children[i]);
  }

  private ArrayList<List<Integer>> result = new ArrayList<>();

  private class Trie {

    public void add(byte[] word, int index, boolean fwd) {
      var node = this;
      for (int i = 0; i < word.length; i++) {
        var c = word[fwd ? i : word.length - i - 1];
        var child = node.children[c];
        if (child == null) {
          child = new Trie();
          node.children[c] = child;
        }
        node = child;
      }
      if (fwd)
        node.fwdIndex = index;
      else
        node.bwdIndex = index;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      aux(sb, 2);
      return sb.toString();
    }

    private void aux(StringBuilder sb, int indent) {
      if (fwdIndex >= 0 || bwdIndex >= 0) {
        sb.append(" [");
        if (fwdIndex >= 0) {
          sb.append(fwdIndex);
        } else {
          sb.append('-');
        }
        sb.append('|');
        if (bwdIndex >= 0) {
          sb.append(bwdIndex);
        } else {
          sb.append('-');
        }
        sb.append("] ");
      }
      boolean anyChild = false;
      for (int i = 0; i < 26; i++) {
        var child = children[i];
        if (child == null)
          continue;
        if (!anyChild) {
          sb.append("{");
          anyChild = true;
        }
        sb.append('\n');
        sb.append(spaces(indent));
        sb.append((char) (i + 'a'));
        child.aux(sb, indent + 2);
      }
      if (anyChild)
        sb.append("}");
    }

    private Trie[] children = new Trie[26];
    private int fwdIndex = -1;
    private int bwdIndex = -1;
  }

  private static byte[] work = new byte[300];

  private static byte[] stringToBytes(String s) {
    if (true) {
      var res = work;
      int cursor = 0;
      int prevVal = -1;
      int count = 0;
      for (int i = 0; i <= s.length(); i++) {
        int val = -1;
        if (i != s.length()) {
          val = s.charAt(i) - 'a';
          //    pr("i:", i, s.charAt(i), "val:", val);
        }
        if (val != prevVal) {
          if (count != 0) {
            if (count <= 5) {
              for (int j = 0; j < count; j++)
                res[cursor++] = (byte) prevVal;
            } else {
              // We need to store a count up to 300; use base 32
              int c1 = (count >> 5) + 26;
              int c2 = (count & (0x1f)) + 26;
              res[cursor + 0] = (byte) c1;
              res[cursor + 1] = (byte) c2;
              res[cursor + 3] = (byte) prevVal;
              res[cursor + 4] = (byte) c2;
              res[cursor + 5] = (byte) c1;
              //  pr("stored special as c1:", c1, "c2:", c2);
            }
          }
          if (val < 0)
            break;
          prevVal = val;
          count = 0;
        }
        count++;
      }
      return Arrays.copyOf(res, cursor);
    } else {
      var res = work;
      for (int i = 0; i < s.length(); i++)
        res[i] = (byte) (s.charAt(i) - 'a');
      return Arrays.copyOf(res, s.length());
    }
  }

}
