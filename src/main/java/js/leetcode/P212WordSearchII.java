
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
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

  public List<String> findWords(char[][] board, String[] words) {
    List<String> result = new ArrayList<>();
    return result;
  }

}
