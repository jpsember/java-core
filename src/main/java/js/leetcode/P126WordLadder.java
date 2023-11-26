
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.json.JSList;
import js.json.JSMap;

public class P126WordLadder {

  public static void main(String[] args) {
    new P126WordLadder().run();
  }

  private void run() {
    x("hit cog hot dot dog lot log cog", 2);

  }

  private void x(String s, int expectedCount) {
    var ws = split(s, ' ');
    int i = 0;
    var beginWord = ws.get(i++);
    var endWord = ws.get(i++);
    var wordList = ws.subList(i, ws.size());
    var result = findLadders(beginWord, endWord, wordList);
    var ls = list();
    for (var x : result) {
      ls.add(JSList.withStringRepresentationsOf(x));
    }
    pr(ls);
    checkState(ls.size() == expectedCount, "expected:", expectedCount, "but got:", ls.size());
  }

  private void dumpGraph(String prompt) {
    JSMap m = map();
    for (var n : mGraph.values()) {
      var lst = list();
      m.put((n == beginNode ? "*" : "") + n.word, lst);
      for (var s : n.links) {
        lst.add("" + s.depth + ":" + s.word);
      }
    }
    pr(prompt, INDENT, m);
  }

  public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
    List<List<String>> results = new ArrayList<>();
    wordLength = beginWord.length();
    wordList.add(beginWord);
    // wordList.add(endWord);
    constructGraph(wordList);
    wordList.remove(wordList.size() - 1);

    beginNode = mGraph.get(beginWord);
    endNode = mGraph.get(endWord);
    if (endNode != null) {
      doBFS();
      dumpGraph("after BFS");
      filterEdges();
      dumpGraph("after filter");
    }
    return results;
  }

  private void constructGraph(List<String> words) {
    mGraph = new HashMap<>();
    for (var w : words) {
      if (mGraph.containsKey(w))
        continue;
      mGraph.put(w, new Node(w));
    }

    // Create links
    Map<String, List<String>> edgeMap = new HashMap<>();
    for (int i = 0; i < wordLength; i++) {
      edgeMap.clear();
      for (var w : mGraph.keySet()) {
        var w2 = w.substring(0, i) + w.substring(i + 1);
        List<String> set = edgeMap.get(w2);
        if (set == null) {
          set = new ArrayList<>();
          edgeMap.put(w2, set);
        }
        set.add(w);
      }
      for (List<String> set : edgeMap.values()) {
        for (int k = 0; k < set.size(); k++) {
          var nk = mGraph.get(set.get(k));
          for (int j = k + 1; j < set.size(); j++) {
            var nj = mGraph.get(set.get(j));
            nk.links.add(nj);
            nj.links.add(nk);
          }
        }
      }
    }
  }

  private void doBFS() {
    List<Node> frontier = new ArrayList<>();
    beginNode.depth = 0;
    frontier.add(beginNode);
    var sptr = 0;
    while (sptr < frontier.size()) {
      var node = frontier.get(sptr++);
      for (var sib : node.links) {
        if (sib.depth >= 0)
          continue;
        sib.depth = node.depth + 1;
        pr(node.word, "->", sib.depth, sib.word);
        frontier.add(sib);
      }
    }
  }

  private void filterEdges() {
    for (var n : mGraph.values()) {
      List<Node> filtered = new ArrayList<>();
      for (var s : n.links) {
        if (s.depth == n.depth + 1) {
          filtered.add(s);
        }
      }
      n.links = filtered;
    }
  }

  private static class Node {
    String word;
    List<Node> links = new ArrayList<>();
    int depth = -1;

    Node(String w) {
      this.word = w;
    }

    @Override
    public String toString() {
      return "<" + word + " d:" + depth + " #links:" + links.size() + ">";
    }

  }

  private int wordLength;
  private Map<String, Node> mGraph;
  private Node beginNode;
  private Node endNode;

}
