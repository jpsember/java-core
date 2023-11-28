
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * First attempt will be a recursive algorithm that given a substring, returns
 * all valid sentences.
 * 
 * It will use memoization and will try to 'peel off' single words to the left.
 * 
 * Works! Added optimizations to use a StringBuilder and 'null list' to 
 * avoid unnecessary object construction.
 */
public class P140WordBreakII {

  public static void main(String[] args) {
    new P140WordBreakII().run();
  }

  private void run() {
    x("aaaa", "aa a aaa");

    //    x("catsanddog", "cat cats and sand dog");
    //    x("pineapplepenapple", "apple pen applepen pine pineapple");
    //    x("aaaaaaa", "aaaa aa a" );
  }

  private void x(String s, String dict) {
    var wordDict = split(dict, ' ');
    var result = wordBreak(s, wordDict);
    pr(result);
  }

  public List<String> wordBreak(String s, List<String> wordDict) {
    memo.clear();
    wordDictSet.addAll(wordDict);
    return auxWordBreak(s);
  }

  private List<String> auxWordBreak(String s) {
    var result = memo.get(s);
    if (result != null)
      return result;

    for (int i = 1; i <= s.length(); i++) {
      var prefixWord = s.substring(0, i);
      if (!wordDictSet.contains(prefixWord))
        continue;
      if (i == s.length()) {
        if (result == null)
          result = new ArrayList<>();
        result.add(s);
        continue;
      }

      var suffix = s.substring(i);
      var suffixResult = auxWordBreak(suffix);
      if (suffixResult == null)
        continue;

      if (result == null)
        result = new ArrayList<>();

      var sb = this.sb;
      sb.setLength(0);
      sb.append(prefixWord);
      sb.append(' ');
      int saveLength = sb.length();
      for (var suffixSentence : suffixResult) {
        sb.append(suffixSentence);
        result.add(sb.toString());
        sb.setLength(saveLength);
      }
    }

    if (result == null)
      return nullList;
    memo.put(s, result);
    return result;
  }

  private List<String> nullList = new ArrayList<>();
  private Map<String, List<String>> memo = new HashMap<>();
  private Set<String> wordDictSet = new HashSet<>();
  private StringBuilder sb = new StringBuilder();
}
