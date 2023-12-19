package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.json.JSList;

/**
 * First, slow version, failed since apparently the same word can appear
 * multiple times in the words array.
 * 
 * Fixed it, and the slow version beats 72.46% runtime, 10% memory
 */
public class SubstringwithConcatenationofAllWords extends LeetCode {

  public static void main(String[] args) {
    loadTools();
    new SubstringwithConcatenationofAllWords().run();
  }

  public void run() {

    x("barfoothefoobarman", "[\"foo\",\"bar\"]");

    x("wordgoodgoodgoodbestword", "[\"word\",\"good\",\"best\",\"good\"]");

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

    Map<String, WordInfo> wordIndexMap = new HashMap<>(words.length);
    int unique = 0;
    for (int i = 0; i < words.length; i++) {
      var info = wordIndexMap.get(words[i]);
      if (info == null) {
        info = new WordInfo();
        wordIndexMap.put(words[i], info);
        info.uniqueIndex = unique++;
      }
      info.multiplicity++;
    }

    int uniqueWordsCount = wordIndexMap.size();

    int[] foundCount = new int[uniqueWordsCount];
    int[] targetCount = new int[uniqueWordsCount];

    for (var ent : wordIndexMap.entrySet()) {
      var info = ent.getValue();
      targetCount[info.uniqueIndex] = info.multiplicity;
    }

    int cursorStop = s.length() - concatWordLength + 1;
    outer: for (int cursor = 0; cursor < cursorStop; cursor++) {
      Arrays.fill(foundCount, 0);
      var ws = cursor;
      for (int i = 0; i < words.length; i++, ws += wordLength) {
        var word = s.substring(ws, ws + wordLength);
        var info = wordIndexMap.get(word);
        if (info == null)
          continue outer;
        int j = info.uniqueIndex;
        if (foundCount[j] == targetCount[j])
          continue outer;
        foundCount[j]++;
      }
      result.add(cursor);
    }
    return result;
  }

  private class WordInfo {
    int uniqueIndex;
    int multiplicity;
  }

}
