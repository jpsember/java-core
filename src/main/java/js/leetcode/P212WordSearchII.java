
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This is a more optimized (?) version, but my first version used what is apparently a 'Trie'.
 * 
 *
 */
public class P212WordSearchII {

  public static void main(String[] args) {
    new P212WordSearchII().run();
  }

  private void run() {
    x(4, 4, "oaanetaeihkriflv", "oath pea eat rain", "eat oath");

    x(3, 3, "abcaedafg", "abcdefg gfedcbaaa eaabcdgfa befa dgc ade", "abcdefg befa eaabcdgfa gfedcbaaa");

    x(12, 12,
        "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa",
        "a aa aaa aaaa aaaaa aaaaaa aaaaaaa aaaaaaaa aaaaaaaaa aaaaaaaaaa",
        "a aa aaa aaaa aaaaa aaaaaa aaaaaaa aaaaaaaa aaaaaaaaa aaaaaaaaaa");
  }

  private void x(int width, int height, String cells, String dicts, String expected) {
    char[][] board = new char[height][];
    for (int i = 0; i < height; i++)
      board[i] = cells.substring(i * width, (i + 1) * width).toCharArray();
    var words = dicts.split(" ");
    var result = findWords(board, words);
    result.sort(null);
    pr("result:", result);
    var exp = new ArrayList<String>();
    for (var w : split(expected, ' '))
      exp.add(w);
    exp.sort(null);
    checkState(exp.equals(result), "expected:", exp);
  }

  //  @Override
  //  public String toString() {
  //    var sb = new StringBuilder();
  //    for (int x = 0; x < bWidth; x++)
  //      sb.append("---");
  //    sb.append('\n');
  //    for (int y = 0; y < bHeight; y++) {
  //      for (int x = 0; x < bWidth; x++) {
  //        var posCode = cellPos(x, y);
  //        //var i = cellIndex(x, y);
  //        int index = indexWithinVisitedList(posCode);
  //        sb.append(index >= 0 ? " " + index : "  ");
  //        sb.append(bChars[y][x]);
  //      }
  //      sb.append('\n');
  //    }
  //    for (int x = 0; x < bWidth; x++)
  //      sb.append("---");
  //    sb.append('\n');
  //    return sb.toString();
  //  }

  public List<String> findWords(char[][] board, String[] words) {

    bChars = board;
    bWidth = board[0].length;
    bHeight = board.length;
    mResult.clear();
    mPrefixMap.clear();

    for (var w : words) {
      for (int i = 1; i <= w.length(); i++) {
        var pref = w.substring(0, i);
        Integer c = mPrefixMap.getOrDefault(pref, 0);
        int newVal = (i == w.length()) ? 2 : 1;
        if (newVal > c) {
          mPrefixMap.put(pref, newVal);
        }
      }
    }

    mSb.setLength(0);
    for (int y = bHeight - 1; y >= 0; y--)
      for (int x = bWidth - 1; x >= 0; x--)
        auxFind(x, y);

    return new ArrayList<String>(mResult);
  }

  private void auxFind(int x, int y) {
    var vc = visitedCursor;
    var posCode = tryVisit(x, y);

    if (posCode < 0)
      return;

    var len = mSb.length();
    mSb.append(bChars[y][x]);
    var wd = mSb.toString();
    var code = mPrefixMap.getOrDefault(wd, 0);
    if (code != 0) {
      if (code == 2) {
        mResult.add(wd);
      }
      auxFind(x - 1, y);
      auxFind(x + 1, y);
      auxFind(x, y - 1);
      auxFind(x, y + 1);
    }
    mSb.setLength(len);
    visitedCursor = vc;
  }

  private int indexWithinVisitedList(int pos) {
    for (int i = 0; i < visitedCursor; i++)
      if (visitedList[i] == pos)
        return i;
    return -1;
  }

  private int cellPos(int x, int y) {
    return (x | (y << 8));
  }

  /**
   * Determine if a cell of the board can be moved to. It must be within the
   * board, and cannot be marked as having been visited.
   * 
   * If so, it is marked as having been visited
   */
  public int tryVisit(int x, int y) {
    if (x < 0 || x >= bWidth || y < 0 || y >= bHeight)
      return -1;
    var posCode = cellPos(x, y);
    if (indexWithinVisitedList(posCode) >= 0) {
      return -1;
    }
    visitedList[visitedCursor++] = posCode;
    return posCode;
  }

  private int[] visitedList = new int[10 + 1];
  private int visitedCursor;
  private Set<String> mResult = new HashSet<>();
  private StringBuilder mSb = new StringBuilder();
  private Map<String, Integer> mPrefixMap = new HashMap<>();
  private int bWidth;
  private int bHeight;
  private char[][] bChars;

}
