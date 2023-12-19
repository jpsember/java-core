package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.json.JSList;

/**
 * First, slow version, failed since apparently the same word can appear multiple times in the words array.
 */
public class SubstringwithConcatenationofAllWords extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new SubstringwithConcatenationofAllWords().run();
  }

  public void run() {

    x("barfoothefoobarman", "[\"foo\",\"bar\"]");

  }

  private void x(String s, String wordsString) {
    var words = new JSList(wordsString).asStringArray();
    var result = findSubstring(s, words);
    result.sort(null);
    var expected = result;
    pr("String:", s, "Words:", words, "Result:", result);
    verify(result, expected);
  }

  public List<Integer> findSubstring(String s, String[] words) {
    List<Integer> result = new ArrayList<>();

    var wordLength = words[0].length();
    int concatWordLength = words.length * wordLength;

    Map<String, Integer> wordIndexMap = new HashMap<>(words.length);
    for (int i = 0; i < words.length; i++)
      wordIndexMap.put(words[i], i);

    int cursorStop = s.length() - concatWordLength + 1;
    outer: for (int cursor = 0; cursor < cursorStop; cursor++) {
      var ss = s.substring(cursor, cursor + concatWordLength);
      int wordsFoundBitmap = 0;
      for (int i = 0; i < words.length; i++) {
        var word = ss.substring(i * wordLength, i * wordLength + wordLength);
        var wordNumber = wordIndexMap.getOrDefault(word, -1);
        if (wordNumber < 0)
          continue outer;
        var bitFlag = 1 << wordNumber;
        if ((wordsFoundBitmap & bitFlag) != 0)
          continue outer;
        wordsFoundBitmap |= bitFlag;
      }
      result.add(cursor);
    }
    return result;
  }
}
