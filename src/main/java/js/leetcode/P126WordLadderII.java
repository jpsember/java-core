
package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import js.json.JSList;
import js.json.JSMap;

/**
 * The tricky part was figuring out that a step was required to prune edges that
 * lead to nodes that don't have a path to the target.
 *
 */
public class P126WordLadderII {

  public static void main(String[] args) {
    new P126WordLadderII().run();
  }

  private void run() {
    //    x("hit cog hot dot dog lot log cog", 2);
    //    x("hit cog hot dot dog lot log", 0);
    //    x("a b c d e f b g h", 1);

    // y("abc", "def",2, "abd", "abe", "abf", "abg", "aec", "aef", "def");

    y("aaaaa", "ggggg", 1, //
        "aaaaa", "caaaa", "cbaaa", "daaaa", "dbaaa", "eaaaa", "ebaaa", "faaaa", "fbaaa", "gaaaa", "gbaaa",
        "haaaa", "hbaaa", "iaaaa", "ibaaa", "jaaaa", "jbaaa", "kaaaa", "kbaaa", "laaaa", "lbaaa", "maaaa",
        "mbaaa", "naaaa", "nbaaa", "oaaaa", "obaaa", "paaaa", "pbaaa", "bbaaa", "bbcaa", "bbcba", "bbdaa",
        "bbdba", "bbeaa", "bbeba", "bbfaa", "bbfba", "bbgaa", "bbgba", "bbhaa", "bbhba", "bbiaa", "bbiba",
        "bbjaa", "bbjba", "bbkaa", "bbkba", "bblaa", "bblba", "bbmaa", "bbmba", "bbnaa", "bbnba", "bboaa",
        "bboba", "bbpaa", "bbpba", "bbbba", "abbba", "acbba", "dbbba", "dcbba", "ebbba", "ecbba", "fbbba",
        "fcbba", "gbbba", "gcbba", "hbbba", "hcbba", "ibbba", "icbba", "jbbba", "jcbba", "kbbba", "kcbba",
        "lbbba", "lcbba", "mbbba", "mcbba", "nbbba", "ncbba", "obbba", "ocbba", "pbbba", "pcbba", "ccbba",
        "ccaba", "ccaca", "ccdba", "ccdca", "cceba", "cceca", "ccfba", "ccfca", "ccgba", "ccgca", "cchba",
        "cchca", "cciba", "ccica", "ccjba", "ccjca", "cckba", "cckca", "cclba", "cclca", "ccmba", "ccmca",
        "ccnba", "ccnca", "ccoba", "ccoca", "ccpba", "ccpca", "cccca", "accca", "adcca", "bccca", "bdcca",
        "eccca", "edcca", "fccca", "fdcca", "gccca", "gdcca", "hccca", "hdcca", "iccca", "idcca", "jccca",
        "jdcca", "kccca", "kdcca", "lccca", "ldcca", "mccca", "mdcca", "nccca", "ndcca", "occca", "odcca",
        "pccca", "pdcca", "ddcca", "ddaca", "ddada", "ddbca", "ddbda", "ddeca", "ddeda", "ddfca", "ddfda",
        "ddgca", "ddgda", "ddhca", "ddhda", "ddica", "ddida", "ddjca", "ddjda", "ddkca", "ddkda", "ddlca",
        "ddlda", "ddmca", "ddmda", "ddnca", "ddnda", "ddoca", "ddoda", "ddpca", "ddpda", "dddda", "addda",
        "aedda", "bddda", "bedda", "cddda", "cedda", "fddda", "fedda", "gddda", "gedda", "hddda", "hedda",
        "iddda", "iedda", "jddda", "jedda", "kddda", "kedda", "lddda", "ledda", "mddda", "medda", "nddda",
        "nedda", "oddda", "oedda", "pddda", "pedda", "eedda", "eeada", "eeaea", "eebda", "eebea", "eecda",
        "eecea", "eefda", "eefea", "eegda", "eegea", "eehda", "eehea", "eeida", "eeiea", "eejda", "eejea",
        "eekda", "eekea", "eelda", "eelea", "eemda", "eemea", "eenda", "eenea", "eeoda", "eeoea", "eepda",
        "eepea", "eeeea", "ggggg", "agggg", "ahggg", "bgggg", "bhggg", "cgggg", "chggg", "dgggg", "dhggg",
        "egggg", "ehggg", "fgggg", "fhggg", "igggg", "ihggg", "jgggg", "jhggg", "kgggg", "khggg", "lgggg",
        "lhggg", "mgggg", "mhggg", "ngggg", "nhggg", "ogggg", "ohggg", "pgggg", "phggg", "hhggg", "hhagg",
        "hhahg", "hhbgg", "hhbhg", "hhcgg", "hhchg", "hhdgg", "hhdhg", "hhegg", "hhehg", "hhfgg", "hhfhg",
        "hhigg", "hhihg", "hhjgg", "hhjhg", "hhkgg", "hhkhg", "hhlgg", "hhlhg", "hhmgg", "hhmhg", "hhngg",
        "hhnhg", "hhogg", "hhohg", "hhpgg", "hhphg", "hhhhg", "ahhhg", "aihhg", "bhhhg", "bihhg", "chhhg",
        "cihhg", "dhhhg", "dihhg", "ehhhg", "eihhg", "fhhhg", "fihhg", "ghhhg", "gihhg", "jhhhg", "jihhg",
        "khhhg", "kihhg", "lhhhg", "lihhg", "mhhhg", "mihhg", "nhhhg", "nihhg", "ohhhg", "oihhg", "phhhg",
        "pihhg", "iihhg", "iiahg", "iiaig", "iibhg", "iibig", "iichg", "iicig", "iidhg", "iidig", "iiehg",
        "iieig", "iifhg", "iifig", "iighg", "iigig", "iijhg", "iijig", "iikhg", "iikig", "iilhg", "iilig",
        "iimhg", "iimig", "iinhg", "iinig", "iiohg", "iioig", "iiphg", "iipig", "iiiig", "aiiig", "ajiig",
        "biiig", "bjiig", "ciiig", "cjiig", "diiig", "djiig", "eiiig", "ejiig", "fiiig", "fjiig", "giiig",
        "gjiig", "hiiig", "hjiig", "kiiig", "kjiig", "liiig", "ljiig", "miiig", "mjiig", "niiig", "njiig",
        "oiiig", "ojiig", "piiig", "pjiig", "jjiig", "jjaig", "jjajg", "jjbig", "jjbjg", "jjcig", "jjcjg",
        "jjdig", "jjdjg", "jjeig", "jjejg", "jjfig", "jjfjg", "jjgig", "jjgjg", "jjhig", "jjhjg", "jjkig",
        "jjkjg", "jjlig", "jjljg", "jjmig", "jjmjg", "jjnig", "jjnjg", "jjoig", "jjojg", "jjpig", "jjpjg",
        "jjjjg", "ajjjg", "akjjg", "bjjjg", "bkjjg", "cjjjg", "ckjjg", "djjjg", "dkjjg", "ejjjg", "ekjjg",
        "fjjjg", "fkjjg", "gjjjg", "gkjjg", "hjjjg", "hkjjg", "ijjjg", "ikjjg", "ljjjg", "lkjjg", "mjjjg",
        "mkjjg", "njjjg", "nkjjg", "ojjjg", "okjjg", "pjjjg", "pkjjg", "kkjjg", "kkajg", "kkakg", "kkbjg",
        "kkbkg", "kkcjg", "kkckg", "kkdjg", "kkdkg", "kkejg", "kkekg", "kkfjg", "kkfkg", "kkgjg", "kkgkg",
        "kkhjg", "kkhkg", "kkijg", "kkikg", "kkljg", "kklkg", "kkmjg", "kkmkg", "kknjg", "kknkg", "kkojg",
        "kkokg", "kkpjg", "kkpkg", "kkkkg", "ggggx", "gggxx", "ggxxx", "gxxxx", "xxxxx", "xxxxy", "xxxyy",
        "xxyyy", "xyyyy", "yyyyy", "yyyyw", "yyyww", "yywww", "ywwww", "wwwww", "wwvww", "wvvww", "vvvww",
        "vvvwz", "avvwz", "aavwz", "aaawz", "aaaaz");
  }

  private void y(String begin, String end, int expectedCount, String... words) {
    var ls = yAux(begin, end, new ArrayList<>(Arrays.asList(words)));
    pr(ls);
    checkState(ls.size() == expectedCount, "expected:", expectedCount, "but got:", ls.size());
  }

  private JSList yAux(String begin, String end, List<String> wd) {
    var result = findLadders(begin, end, wd);
    var ls = list();
    for (var x : result) {
      ls.add(JSList.withStringRepresentationsOf(x));
    }
    return ls;
  }

  /* private */ void x(String s, int expectedCount) {
    var ws = split(s, ' ');
    int i = 0;
    var beginWord = ws.get(i++);
    var endWord = ws.get(i++);
    var wordList = ws.subList(i, ws.size());
    var result = yAux(beginWord, endWord, wordList);
    pr(result);
    checkState(result.size() == expectedCount, "expected:", expectedCount, "but got:", result.size());
  }

  /* private */ void dumpGraph(String prompt) {
    JSMap m = map();
    for (var n : mGraph.values()) {
      var lst = list();
      m.put((n == beginNode ? "*" : "") + n.word, lst);
      for (var s : n.edges) {
        lst.add("" + s.depth + ":" + s.word);
      }
    }
    pr(prompt, INDENT, m);
  }

  public List<List<String>> findLadders(String beginWord, String endWord, List<String> wordList) {
    results.clear();
    wordLength = beginWord.length();
    wordList.add(beginWord);
    checkpoint("construct graph");
    constructGraph(wordList);
    wordList.remove(wordList.size() - 1);

    beginNode = mGraph.get(beginWord);
    endNode = mGraph.get(endWord);

    {
      checkpoint("BFS");

      doBFS();
      //dumpGraph("after BFS");
      checkpoint("filter bwd");
      filterBackwardEdges();

      checkpoint("filter edges to dead ends");

      filterEdgesLeadingToDeadEnds(beginNode);
      checkpoint("find results");

      findResults(beginNode);
      checkpoint("done");

    }
    return results;
  }

  private void findResults(Node node) {
    path.add(node);
    if (node == endNode) {
      List<String> solution = new ArrayList<>(path.size());
      for (var n : path)
        solution.add(n.word);
      results.add(solution);
    }
    for (var sib : node.edges)
      findResults(sib);
    path.remove(path.size() - 1);
  }

  private void constructGraph(List<String> words) {
    mGraph = new HashMap<>(words.size() );
    var g = mGraph;
    checkpoint("create nodes");
    for (var w : words) {
      if (g.get(w) == null) {
        g.put(w, new Node(w));
      }
    }
    checkpoint("created nodes");

    // Create edges by creating sets of node names with a particular character removed
    Map<String, List<String>> edgeMap = new HashMap<>();
    for (int i = 0; i < wordLength; i++) {
      checkpoint("edges", i);
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
            nk.edges.add(nj);
            nj.edges.add(nk);
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
      for (var sib : node.edges) {
        if (sib.depth >= 0)
          continue;
        sib.depth = node.depth + 1;
        //pr(node.word, "->", sib.depth, sib.word);
        frontier.add(sib);
      }
    }
  }

  /**
   * Delete any edges that don't make progress towards the target depth
   */
  private void filterBackwardEdges() {
    for (var n : mGraph.values()) {
      List<Node> filtered = new ArrayList<>();
      if (n != endNode) {
        for (var s : n.edges) {
          if (s.depth == n.depth + 1) {
            filtered.add(s);
          }
        }
      }
      n.edges = filtered;
    }
  }

  /**
   * Delete any edges that only lead to dead ends. Return true if this node has
   * a path to the target.
   */
  private boolean filterEdgesLeadingToDeadEnds(Node node) {
    if (node == endNode)
      return true;
    List<Node> filtered = new ArrayList<>();
    for (var sib : node.edges) {
      if (!filterEdgesLeadingToDeadEnds(sib))
        continue;
      filtered.add(sib);
    }
    node.edges = filtered;
    return !filtered.isEmpty();
  }

  private static class Node {
    String word;
    List<Node> edges = new ArrayList<>();
    int depth = -1;

    Node(String w) {
      this.word = w;
    }

    @Override
    public String toString() {
      return "<" + word + " d:" + depth + " #links:" + edges.size() + ">";
    }

  }

  private int wordLength;
  private Map<String, Node> mGraph;
  private Node beginNode;
  private Node endNode;
  private List<Node> path = new ArrayList<>();
  private List<List<String>> results = new ArrayList<>();

}
