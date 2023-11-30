
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class P212WordSearchII {

  public static void main(String[] args) {
    new P212WordSearchII().run();
  }

  private void run() {
    x(4, 4, "oaanetaeihkriflv", "oath pea eat rain", "eat oath");
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

  public List<String> findWords(char[][] board, String[] words) {
    mBoard = new Board(board);
    mResult.clear();
    mPrefixMap.clear();

    for (var w : words) {
      for (int i = 1; i <= w.length(); i++) {
        var pref = w.substring(0, i);
        Integer c = mPrefixMap.getOrDefault(pref, 0);
        int newVal = (i == w.length()) ? 2 : 1;
        if (newVal > c)
          mPrefixMap.put(pref, newVal);
      }
    }

    mSb.setLength(0);
    for (int y = 0; y < mBoard.height; y++)
      for (int x = 0; x < mBoard.width; x++)
        auxFind(x, y);

    return new ArrayList<String>(mResult);
  }

  private void auxFind(int x, int y) {
    var posCode = mBoard.tryVisit(x, y);
    if (posCode < 0)
      return;

    var len = mSb.length();
    mSb.append(mBoard.c[y][x]);
    var wd = mSb.toString();
    var code = mPrefixMap.getOrDefault(wd, 0);
    pr("code for", quote(wd), "is", code);
    if (code != 0) {
      pr("prefix is in tree:", quote(wd));
      if (code == 2) {
        pr("word is in tree:", quote(wd));
        mResult.add(wd);
      }
      auxFind(x - 1, y);
      auxFind(x + 1, y);
      auxFind(x, y - 1);
      auxFind(x, y + 1);
    } else {
      pr("prefix not in tree:", quote(wd));
    }
    mSb.setLength(len);
    mBoard.visitedCursor--;
  }

  private static class Board {
    int width;
    int height;
    char[][] c;

    Board(char[][] c) {
      this.c = c;
      width = c[0].length;
      height = c.length;
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      for (int x = 0; x < width; x++)
        sb.append("---");
      sb.append('\n');
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          var posCode = cellPos(x, y);
          //var i = cellIndex(x, y);
          int index = indexWithinVisitedList(posCode);
          sb.append(index >= 0 ? " " + index : "  ");
          sb.append(c[y][x]);
        }
        sb.append('\n');
      }
      for (int x = 0; x < width; x++)
        sb.append("---");
      sb.append('\n');
      return sb.toString();
    }

    private int indexWithinVisitedList(int pos) {
      for (int i = 0; i < visitedCursor; i++)
        if (visitedList[i] == pos)
          return i;
      return -1;
    }

    private int cellPos(int x, int y) {
      return x | (y * width);
    }

    /**
     * Determine if a cell of the board can be moved to. It must be within the
     * board, and cannot be marked as having been visited.
     * 
     * If so, it is marked as having been visited
     */
    public int tryVisit(int x, int y) {
      pr("try visit:", x, y);
      if (x < 0 || x >= width || y < 0 || y >= height)
        return -1;
      var posCode = cellPos(x, y);
      if (indexWithinVisitedList(posCode) >= 0) {
        pr("...already visited");
        return -1;
      }
      visitedList[visitedCursor++] = posCode;
      return posCode;
    }

    private int[] visitedList = new int[10];
    private int visitedCursor;

  }

  private Board mBoard;
  private Set<String> mResult = new HashSet<>();
  private StringBuilder mSb = new StringBuilder();
  private Map<String, Integer> mPrefixMap = new HashMap<>();

}
