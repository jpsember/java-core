
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
 */
public class P140WordBreakII {

  public static void main(String[] args) {
    new P140WordBreakII().run();
  }

  private void run() {
    x("catsanddog", "cat cats and sand dog");
    x("pineapplepenapple", "apple pen applepen pine pineapple");
  }

  private void x(String s, String dict) {
    var wordDict = split(dict, ' ');
    var result = wordBreak(s, wordDict);
    pr(result);
  }

  public List<String> wordBreak(String s, List<String> wordDict) {
    memo.clear();
    wordDictSet.addAll(wordDict);
    var result = auxWordBreak(s);
    if (result == null)
      result = new ArrayList<>();
    return result;
  }

  private List<String> auxWordBreak(String s) {
    var result = memo.get(s);
    if (result != null)
      return result;

    for (int i = 0; i < s.length(); i++) {
      var suffixWord = s.substring(i);
      if (!wordDictSet.contains(suffixWord))
        continue;
      if (i == 0) {
        result = new ArrayList<>();
        result.add(suffixWord);
        continue;
      }

      var prefix = s.substring(0, i);
      var prefixResult = auxWordBreak(prefix);
      if (prefixResult == null)
        continue;

      if (result == null)
        result = new ArrayList<>();
      for (var prefixSentence : prefixResult)
        result.add(prefixSentence + " " + suffixWord);
    }

    if (result != null)
      memo.put(s, result);
    return result;
  }

  private Map<String, List<String>> memo = new HashMap<>();
  private Set<String> wordDictSet = new HashSet<>();

}
