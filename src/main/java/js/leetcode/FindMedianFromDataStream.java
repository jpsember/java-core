package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import js.json.JSList;

public class FindMedianFromDataStream extends LeetCode {

  public static void main(String[] args) {
    new FindMedianFromDataStream().run();
  }

  public void run() {
    xx("[\"MedianFinder\", \"addNum\", \"addNum\", \"findMedian\", \"addNum\", \"findMedian\"]\n",
        "[[], [1], [2], [], [3], []]");

    x("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[12],[],[10],[],[13],[],[11],[],[5],[],[15],[],[1],[],[11],[],[6],[],[17],[],[14],[],[8],[],[17],[],[6],[],[4],[],[16],[],[8],[],[10],[],[2],[],[12],[],[0],[]]");
  }

  private void x(String cmdStr, String argStr) {
    var cmdList = new JSList(cmdStr);
    var argsList = new JSList(argStr);

    var resultList = list();

    var expList = list();

    MedianFinder m = null;
    MedianFinder s = null;
    int i = INIT_INDEX;
    for (var cmd : cmdList.asStringArray()) {
      i++;
      Object output = null;
      Object output2 = null;
      switch (cmd) {
      default:
        throw die(cmd);
      case "MedianFinder":
        m = new MedianFinder();
        s = new SlowMedianFinder();
        break;
      case "addNum": {
        var num = argsList.getList(i).getInt(0);
        m.addNum(num);
        s.addNum(num);
      }
        break;
      case "findMedian":
        output = m.findMedian();
        output2 = s.findMedian();
        break;
      }
      resultList.addUnsafe(output);
      expList.addUnsafe(output2);
    }
    pr(resultList);
    verify(resultList, expList);
  }

  class SlowMedianFinder extends MedianFinder {

    public SlowMedianFinder() {

    }

    public void addNum(int num) {
      vals.add(num);
    }

    public double findMedian() {
      vals.sort(null);
      int i = vals.size() / 2;
      int j = i;
      if (vals.size() % 2 == 0)
        j--;
      return (vals.get(i) + vals.get(j)) / 2.0;
    }

    private List<Integer> vals = new ArrayList<>();
  }

  private static //

  // ----------------------------------------------

  class MedianFinder {

    public MedianFinder() {
      mMap = new TreeMap<>();
      var minEnt = new Ent(-100001);
      var maxEnt = new Ent(100001);
      join(minEnt, maxEnt);
      mMap.put(minEnt.value, minEnt);
      mMap.put(maxEnt.value, maxEnt);
    }

    public void addNum(int num) {
      db(VERT_SP, "addNum:", num);

      // Locate map entry that contains this number, or the insertion position
      // if it is not in the map.

      // This is probably the slowest step... as a heuristic, search from the median entry
      // for n steps before giving up and querying the tree

      var ent = medianEnt;
      if (ent != null) {
        final int maxSteps = 12;
        for (int i = 0; i < maxSteps; i++) {
          if (ent.value == num)
            break;
          if (ent.value > num) {
            if (ent.prev.value < num) {
              ent = null;
              break;
            }
            ent = ent.prev;
          } else {
            if (ent.next.value > num) {
              ent = null;
              break;
            }
            ent = ent.next;
          }
        }
      }

      if (ent == null) {
        var tail = mMap.tailMap(num);
        ent = tail.get(tail.firstKey());
        if (ent.value != num) {
          var newEnt = new Ent(num);
          join(ent.prev, newEnt);
          join(newEnt, ent);
          ent = newEnt;
          mMap.put(num, ent);
        }
      }
      ent.frequency++;
      if (medianEnt == null) {
        medianEnt = ent;
        medianOrd = 0;
      }
      population++;

      // Adjust median
      if (num < medianEnt.value)
        medianOrd++;

      // Get order of the left median value (which is equal to the right value if the population is odd)
      int mi = (population - 1) >> 1;
      if (mi < medianOrd) {
        db("...moving median entry LEFT, since pop is now:", population, "and old median order is:",
            medianOrd);
        var n = medianEnt.prev;
        medianOrd -= n.frequency;
        medianEnt = n;
      } else if (mi >= medianOrd + medianEnt.frequency) {
        db("...moving median entry RIGHT, since pop is now:", population, "and old median order is:",
            medianOrd);
        medianOrd += medianEnt.frequency;
        var n = medianEnt.next;
        medianEnt = n;
      }
      db(this);
    }

    public double findMedian() {
      var leftValue = medianEnt.value;
      // Get order of the left median value (which is equal to the right value if the population is odd)
      var leftOrder = (population - 1) >> 1;

      boolean odd = (population & 1) != 0;
      var rightValue = leftValue;

      int rightOrder = leftOrder + (odd ? 0 : 1);
      if (rightOrder >= medianOrd + medianEnt.frequency)
        rightValue = medianEnt.next.value;
      return (leftValue + rightValue) / 2.0;
    }

    @Override
    public String toString() {
      var m = map();
      m.put("", "MedianFinder");
      m.put("pop", population);
      var ls = list();
      if (medianEnt != null) {
        m.put("median entry", medianEnt.toString());
        var e = medianEnt;
        while (e.prev != null)
          e = e.prev;
        while (true) {
          if (!(e.prev == null || e.next == null))
            ls.add(e.toString());
          if (e.next == null)
            break;
          checkState(e.next.prev == e);
          e = e.next;
        }
      }
      if (population != 0) {
        m.put("median", findMedian());
        m.put("median ord", medianOrd);
      }
      m.put("entries", ls);
      return m.prettyPrint();
    }

    private static class Ent {
      Ent(int value) {
        this.value = value;
      }

      int value;
      int frequency;
      Ent prev;
      Ent next;

      @Override
      public String toString() {
        var s = "" + value;
        if (frequency > 1)
          s += " (x" + frequency + ")";
        return s;
      }
    }

    private Ent join(Ent a, Ent b) {
      a.next = b;
      b.prev = a;
      return b;
    }

    private TreeMap<Integer, Ent> mMap;
    // Number of numbers processed from data stream
    private int population;
    // Ent containing the median value
    private Ent medianEnt;
    // The (zero-based) index of the first occurrence of the median value in a sorted list
    // of all the values
    private int medianOrd;
  }

}
