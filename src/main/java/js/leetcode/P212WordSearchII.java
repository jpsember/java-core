
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

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
    for (var w : words)
      exp.add(w);
    exp.sort(null);
    checkState(exp.equals(result), "expected:", exp);
  }

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

    void clearVisited() {
      visited.clear();
    }

    @Override
    public String toString() {
      var sb = new StringBuilder();
      for (int y = 0; y < height; y++) {
        for (int x = 0; x < width; x++) {
          sb.append(cell(x, y));
        }
        sb.append('\n');
      }
      return sb.toString();
    }

    public char cell(int x, int y) {
      return cells[cellIndex(x, y)];
    }

    private int cellIndex(int x, int y) {
      return y * width + x;
    }

    public boolean tryVisit(int x, int y) {
      pr("try visit:", x, y);
      if (x < 0 || x >= width || y < 0 || y >= height)
        return false;
      var i = cellIndex(x, y);
      if (visited.get(i)) {
        pr("...already visited");
        return false;
      }
      visitedWork.add(i);
      visited.set(i);
      pr("...ok, visited:",visitedWork);
      return true;
    }

    private List<Integer> visitedWork = new ArrayList<>();

    public void setVisWorkLength(int vw) {
      while (visitedWork.size() > vw) {
        int j = visitedWork.size() - 1;
        int ind = visitedWork.get(j);
        visited.clear(ind);
        visitedWork.remove(j);
      }
    }

  }

  private WordTree tree;

  private List<String> result = new ArrayList<>();

  public List<String> findWords(char[][] board, String[] words) {

    var b = new Board(board);
    //List<String> result = new ArrayList<>();
    result.clear();

    tree = buildTree(words);
    if (words.length < 10)
      pr(tree);

    pr(b);

    for (int y = 0; y < b.height; y++) {
      for (int x = 0; x < b.width; x++) {
        findWordsAt(b, x, y);
      }
    }
    return result;
  }

  private void findWordsAt(Board b, int x, int y) {
    b.clearVisited();

    var sb = new StringBuilder();
    auxFind(sb, b, x, y);

  }

  private void auxFind(StringBuilder sb, Board b, int x, int y) {
    pr(VERT_SP, "auxFind, sb:", quote(sb.toString()), "x:", x, "y:", y);

    var vw = b.visitedWork.size();
    if (!b.tryVisit(x, y))
      return;

    var len = sb.length();
    sb.append(b.cell(x, y));
    var wd = sb.toString();
    if (tree.containsPrefix(wd)) {
      pr("word now:", wd, "in tree:", tree.contains(wd));
      if (tree.contains(wd)) {
        result.add(wd);
      }
      auxFind(sb, b, x - 1, y);
      auxFind(sb, b, x + 1, y);
      auxFind(sb, b, x, y - 1);
      auxFind(sb, b, x, y + 1);
    }
    sb.setLength(len);
    b.setVisWorkLength(vw);
  }

  //  private BitSet visited;

  private static class Node {
    Node[] children = new Node[26];
    boolean isWord;
  }

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
  }

  private WordTree buildTree(String[] words) {
    var t = new WordTree();
    for (var w : words) {
      var node = t.rootNode;
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
    return t;
  }

}
