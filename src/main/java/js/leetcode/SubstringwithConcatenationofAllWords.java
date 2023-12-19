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
    //
    //    //    long q = 
    //    //        2l *3l* 5l* 7l* 11l* 13l* 17l* 19l* 23l* 29l* 31l* 37l* 41l* 43l* 47l* 53l* 59l* 61l* 67l* 71l* 73l* 79l* 83l* 89l* 97l;
    //    //    
    //    pr(q);
    //    halt();

    x("barfoothefoobarman", "[\"foo\",\"bar\"]");

    x("wordgoodgoodgoodbestword", "[\"word\",\"good\",\"best\",\"good\"]");

  }

  private void x(String s, String wordsString) {
    var words = new JSList(wordsString).asStringArray();
    var result = findSubstring(s, words);
    result.sort(null);
    var expected = SLOWfindSubstring(s, words);
    pr("String:", s, "Words:", words, "Result:", result);
    verify(result, expected);
  }

  public List<Integer> SLOWfindSubstring(String s, String[] words) {
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

  // ------------------------------------------------------------------

  public List<Integer> findSubstring(String s, String[] words) {
    List<Integer> result = new ArrayList<>();
    var wordLength = words[0].length();

    // Get some information about the words we're looking for
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

    // Construct an array of shorts corresponding to the index of the word starting at that character,
    // or -1 if none
    short[] wordAtIndex = new short[s.length() - (wordLength - 1)];
    Arrays.fill(wordAtIndex, (short) -1);
    for (int i = 0; i < wordAtIndex.length; i++) {
      var info = wordIndexMap.get(s.substring(i, i + wordLength));
      if (info != null)
        wordAtIndex[i] = (short) info.uniqueIndex;
    }

    int substringLength = wordLength * words.length;
    int scanStop = (s.length() - (substringLength - 1));

    int[] found = new int[uniqueWordsCount];
    int[] target = new int[uniqueWordsCount];

    for (var ent : wordIndexMap.values())
      target[ent.uniqueIndex] = ent.multiplicity;

    // Scan through our wordAtIndex array, looking for sets of starting words that match 
    // our target
    outer: for (int i = 0; i < scanStop; i++) {
      Arrays.fill(found, 0);
      for (int j = 0; j < substringLength; j += wordLength) {
        var word = wordAtIndex[i + j];
        if (word < 0)
          continue outer;
        found[word]++;
      }
      if (Arrays.equals(found, target))
        result.add(i);
    }
    return result;
  }

}
