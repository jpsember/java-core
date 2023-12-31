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

  public void run() {
    x("[\"a\",\"\"]","[[0,1],[1,0]]");
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
    var result = new ArrayList<List<Integer>>();

    byte[][] wordsAsBytes = new byte[wordsw.length][];
    for (int i = 0; i < wordsw.length; i++)
      wordsAsBytes[i] = stringToBytes(wordsw[i]);

    var trie = new Trie();
    for (int i = 0; i < wordsAsBytes.length; i++)
      trie.add(wordsAsBytes[i], i);

    int wordNumber = -1;
    for (var w : wordsAsBytes) {
      wordNumber++;
      int i = w.length;
      var t = trie;
      while (t != null) {
        if (t.index >= 0 && t.index != wordNumber) {
          if (isPal(w, i)) {
            var res = new ArrayList<Integer>(2);
            res.add(t.index);
            res.add(wordNumber);
            result.add(res);
          }
        }
        if (i == 0)
          break;
        i--;
        t = t.child(w[i]);
      }
    }
    return result;
  }

  private static boolean isPal(byte[] bytes, int i) {
    int j = 0;
    while (i > j) {
      if (bytes[i - 1] != bytes[j])
        return false;
      i--;
      j++;
    }
    return true;
  }

  private class Trie {

    public Trie child(int i) {
      return children[i];
    }

    public void add(byte[] word, int index) {
      var node = this;
      for (int i = 0; i < word.length; i++) {
        var c = word[i];
        var child = node.children[c];
        if (child == null) {
          child = new Trie();
          node.children[c] = child;
        }
        node = child;
      }
      node.index = index;
    }

    //    @Override
    //    public String toString() {
    //      StringBuilder sb = new StringBuilder();
    //      aux(sb, 2);
    //      return sb.toString();
    //    }

//    private void aux(StringBuilder sb, int indent) {
//      if (index >= 0) {
//        sb.append('*');
//        sb.append(index);
//      }
//      boolean anyChild = false;
//      for (int i = 0; i < 26; i++) {
//        var child = children[i];
//        if (child == null)
//          continue;
//        if (!anyChild) {
//          sb.append("[");
//          anyChild = true;
//        }
//        sb.append('\n');
//        sb.append(spaces(indent));
//        sb.append((char) (i + 'a'));
//        child.aux(sb, indent + 2);
//      }
//      if (anyChild)
//        sb.append("]");
//    }

    private Trie[] children = new Trie[26];
    private int index = -1;
  }

  private static byte[] stringToBytes(String s) {
    var res = new byte[s.length()];
    for (int i = 0; i < s.length(); i++)
      res[i] = (byte) (s.charAt(i) - 'a');
    return res;
  }
}
