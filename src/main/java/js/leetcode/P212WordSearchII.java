
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashSet;
import java.util.List;
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
    for (var w : split(expected,' '))
      exp.add(w);
    exp.sort(null);
    checkState(exp.equals(result), "expected:", exp);
  }

  public List<String> findWords(char[][] board, String[] words) {

    mBoard = new Board(board);
    //List<String> result = new ArrayList<>();
    mResult.clear();

    mTree = new WordTree();
    mTree.addWords(words);
    if (words.length < 10)
      pr(mTree);

    pr(mBoard);

    mSb.setLength(0);
    for (int y = 0; y < mBoard.height; y++) {
      for (int x = 0; x < mBoard.width; x++) {
        //        mSb.setLength(0);
        auxFind(x, y);
      }
    }
    return new ArrayList<String>(mResult);
  }

  private void auxFind(int x, int y) {
    pr(VERT_SP, "auxFind, sb:", quote(mSb.toString()), "x:", x, "y:", y);
    pr(mBoard);

    if (!mBoard.tryVisit(x, y))
      return;

    var len = mSb.length();
    mSb.append(mBoard.cell(x, y));
    var wd = mSb.toString();
    if (mTree.containsPrefix(wd)) {
      pr("prefix is in tree:", quote(wd));
      if (mTree.contains(wd)) {
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
    mBoard.unvisit();
  }

  private static class Node {
    Node[] children = new Node[26];
    boolean isWord;
  }

  /**
   * Tree data structure that can quickly determine if a word or a word prefix
   * is within the dictionary.
   */
  private static class WordTree {
    Node rootNode = new Node();

    @Override
    public String toString() {
      var sb = new StringBuilder();
      auxStr("", rootNode, sb, 0);
      return sb.toString();
    }

    private void auxStr(String wordWork, Node n, StringBuilder sb, int depth) {
      sb.append(spaces(depth));

      if (n.isWord) {
        sb.append("*");
        sb.append(wordWork);
        sb.append(' ');
      }
      sb.append('[');
      for (int i = 0; i < n.children.length; i++) {
        if (n.children[i] != null)
          sb.append((char) ('a' + i));
      }
      sb.append("]\n");
      for (int i = 0; i < n.children.length; i++) {
        if (n.children[i] != null)
          auxStr(wordWork + Character.toString((char) ('a' + i)), n.children[i], sb, depth + 2);
      }
    }

    public boolean contains(String word) {
      var n = containsHelper(word);
      return n != null && n.isWord;
    }

    private Node containsHelper(String prefix) {
      var n = rootNode;
      for (int i = 0; i < prefix.length(); i++) {
        int c = prefix.charAt(i) - 'a';
        var n2 = n.children[c];
        if (n2 == null)
          return null;
        n = n2;
      }
      return n;
    }

    public boolean containsPrefix(String prefix) {
      return containsHelper(prefix) != null;
    }

    public void addWords(String[] words) {
      for (var w : words) {
        var node = rootNode;
        for (int i = 0; i < w.length(); i++) {
          int childNum = w.charAt(i) - 'a';
          var node2 = node.children[childNum];
          if (node2 == null) {
            node2 = new Node();
            node.children[childNum] = node2;
          }
          node = node2;
        }
        checkState(!node.isWord);
        node.isWord = true;
      }
    }
  }

  private Board mBoard;
  private WordTree mTree;
  private Set<String> mResult = new HashSet<>();
  private StringBuilder mSb = new StringBuilder();

  private static class Board {
    int width;
    int height;
    char[] cells;
    BitSet visited;

    Board(char[][] c) {
      width = c[0].length;
      height = c.length;
      cells = new char[width * height];
      visited = new BitSet(cells.length);
      int i = 0;
      for (var row : c) {
        for (var ch : row) {
          cells[i++] = ch;
        }
      }
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      for (int x = 0; x < width; x++)
        sb.append("---");
      sb.append('\n');
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          var i = cellIndex(x, y);
          int index = visitedList.indexOf(i);
          sb.append(index >= 0 ? " " + index : "  ");
          sb.append(cells[i]);
        }
        sb.append('\n');
      }
      for (int x = 0; x < width; x++)
        sb.append("---");
      sb.append('\n');
      return sb.toString();
    }

    public char cell(int x, int y) {
      return cells[cellIndex(x, y)];
    }

    private int cellIndex(int x, int y) {
      return y * width + x;
    }

    /**
     * Determine if a cell of the board can be moved to. It must be within the
     * board, and cannot be marked as having been visited.
     * 
     * If so, it is marked as having been visited
     */
    public boolean tryVisit(int x, int y) {
      pr("try visit:", x, y);
      if (x < 0 || x >= width || y < 0 || y >= height)
        return false;
      var i = cellIndex(x, y);
      if (visited.get(i)) {
        pr("...already visited");
        return false;
      }
      visitedList.add(i);
      visited.set(i);
      return true;
    }

    private List<Integer> visitedList = new ArrayList<>();

    public void unvisit() {
      var index = visitedList.remove(visitedList.size() - 1);
      visited.clear(index);
    }

  }

}
