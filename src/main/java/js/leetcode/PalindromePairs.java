package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
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

  public void run() {
    // x("[\"a\",\"\"]", "[[0,1],[1,0]]");
    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]", "[[0,1],[1,0],[3,2],[2,4]]");
  }

  private void x(String s, String sExp) {
    db = true;
    var words = new JSList(s);
    var exp = new JSList(sExp);
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
    for (int i = 0; i < wordsw.length; i++)
      wordsAsBytes[i] = stringToBytes(wordsw[i]);

    todo("I think I do need a 'reversed' trie, and walk leftward and rightward resp.");
    todo("But does that still ensure a linear run time?");

    var trie = new Trie();
    for (int i = 0; i < wordsAsBytes.length; i++) {
      trie.add(wordsAsBytes[i], i, true);
      trie.add(wordsAsBytes[i], i, false);
    }
    pr("fwd:", INDENT, trie);

    var node = trie;
    aux(trie, node);

    //    int wordNumber = -1;
    //    for (var w : wordsAsBytes) {
    //      wordNumber++;
    //      int i = w.length;
    //      var t = fwdTrie;
    //      while (t != null) {
    //        if (t.index >= 0 && t.index != wordNumber) {
    //          if (isPal(w, i)) {
    //            var res = new ArrayList<Integer>(2);
    //            res.add(t.index);
    //            res.add(wordNumber);
    //            result.add(res);
    //          }
    //        }
    //        if (i == 0)
    //          break;
    //        i--;
    //        t = t.child(w[i]);
    //      }
    //    }
    return result;
  }

  private ArrayList<List<Integer>> result = new ArrayList<>();

  private void aux(Trie root, Trie node) {
    if (node == null)
      return;

    // Case 1: Is this w'.end?   (' means bwd)
    if (node.bwdIndex >= 0) {
      aux2(node.bwdIndex, node, root);
    }

    //    // Case 2: Is this v.end?
    //    if (node.fwdIndex >= 0) {
    //    }

    for (var child : node.children)
      aux(root, child);
  }

  private void addResult(int v, int w) {
    if (v == w)
      return;
    var r = new ArrayList<Integer>(2);
    r.add(v);
    r.add(w);
    result.add(r);
  }

  private void aux2(int bwdIndex, Trie t1, Trie t2) {
    if (t1 == null || t2 == null)
      return;
    if (t1.fwdIndex >= 0) {
      addResult(t1.fwdIndex, bwdIndex);
    }
    for (int i = 0; i < 26; i++)
      aux2(bwdIndex, t1.children[i], t2.children[i]);
  }
  //
  //  private static boolean isPal(byte[] bytes, int i) {
  //    int j = 0;
  //    while (i > j) {
  //      if (bytes[i - 1] != bytes[j])
  //        return false;
  //      i--;
  //      j++;
  //    }
  //    return true;
  //  }

  private class Trie {

    //    public Trie child(int i) {
    //      return children[i];
    //    }

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

  private static byte[] stringToBytes(String s) {
    var res = new byte[s.length()];
    for (int i = 0; i < s.length(); i++)
      res[i] = (byte) (s.charAt(i) - 'a');
    return res;
  }

//  private static byte[] reversed(byte[] b) {
//    var res = new byte[b.length];
//    for (int i = 0; i < b.length; i++)
//      res[i] = b[b.length - 1 - i];
//    pr("reverse:", JSList.with(b), JSList.with(res));
//    return res;
//  }
}
