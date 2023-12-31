package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

import js.json.JSList;

/**
 * I think I need to use a trie data structure
 */
public class PalindromePairs extends LeetCode {

  public static void main(String[] args) {
    new PalindromePairs().run();
  }

  public void run() {
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
    pr(res);
    pr("palindromePairs", words, res);
    verify(res, exp);
  }

  // ------------------------------------------------------------------

  public List<List<Integer>> palindromePairs(String[] words) {
    var result = new ArrayList<List<Integer>>();

    var fwdTrie = new Trie();
    for (int i = 0; i < words.length; i++) {
      fwdTrie.add(words[i], i);
    }

    pr(fwdTrie);

    int wordNumber = -1;
    for (var w : words) {
      wordNumber++;
      int i = w.length();
      var t = fwdTrie;
      int lastWord = -1;
      while (i >= 0) {
        if (t.index >= 0) {
          lastWord = t.index;
          var prefix = w.substring(0, i);
          pr("found prefix at i:", i, quote(prefix));
          if (isPal(prefix)) {
            var res = new ArrayList<Integer>(2);
            res.add(lastWord);
            res.add(wordNumber);
            result.add(res);
          }
        }
        if (i == 0)
          break;
        i--;
        var x = w.charAt(i);
        t = t.child(x);
        if (t == null)
          break;
      }

    }

    return result;
  }

  private static boolean isPal(String s) {
    int i = s.length();
    int j = 0;
    while (i > j) {
      if (s.charAt(i - 1) != s.charAt(j))
        return false;
      i--;
      j++;
    }
    return true;
  }

  private class Trie {

    public Trie child(char c) {
      int i = c - 'a';
      return children[i];
    }

    public void add(String word, int index) {
      var node = this;
      for (int i = 0; i < word.length(); i++) {
        int c = word.charAt(i) - 'a';
        var child = node.children[c];
        if (child == null) {
          child = new Trie();
          node.children[c] = child;
        }
        node = child;
      }
      node.index = index;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      aux(sb, 2);
      return sb.toString();
    }

    private void aux(StringBuilder sb, int indent) {
      if (index >= 0) {
        sb.append('*');
        sb.append(index);
      }
      boolean anyChild = false;
      for (int i = 0; i < 26; i++) {
        var child = children[i];
        if (child == null)
          continue;
        if (!anyChild) {
          sb.append("[");
          anyChild = true;
        }
        sb.append('\n');
        sb.append(spaces(indent));
        sb.append((char) (i + 'a'));
        child.aux(sb, indent + 2);
      }
      if (anyChild)
        sb.append("]");
    }

    private Trie[] children = new Trie[26];
    private int index = -1;
  }

}
