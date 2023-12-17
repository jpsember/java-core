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
        "[[], [1], [2], [], [3], []]", "[null, null, null, 1.5, null, 2.0]");

    x("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[12],[],[10],[],[13],[],[11],[],[5],[],[15],[],[1],[],[11],[],[6],[],[17],[],[14],[],[8],[],[17],[],[6],[],[4],[],[16],[],[8],[],[10],[],[2],[],[12],[],[0],[]]",
        "[null,null,12.00000,null,11.00000,null,12.00000,null,11.50000,null,11.00000,null,11.50000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,11.00000,null,10.50000,null,10.00000,null,10.50000,null,10.00000]");
  }

  private void x(String cmdStr, String argStr, String expStr) {
    var cmdList = new JSList(cmdStr);
    var argsList = new JSList(argStr);
    var expectedList = new JSList(expStr);

    var resultList = list();

    var expList = list();
    
    MedianFinder m = null;
    MedianFinder s  = null;
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
      case "addNum":
      {
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
    pr(expList);
    verify(resultList, expList);
  }

  private static //

  // ----------------------------------------------

  class MedianFinder {

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
          ls.add(e.toString());
          if (e.next == null)
            break;
          checkState(e.next.prev == e);
          e = e.next;
        }
      }
      if (population != 0)
        m.put("median", findMedian());
      m.put("entries", ls);
      return m.prettyPrint();
    }

    public MedianFinder() {
      mMap = new TreeMap<>();
      var minEnt = new Ent(-100001);
      var maxEnt = new Ent(100001);
      join(minEnt, maxEnt);
      mMap.put(minEnt.value, minEnt);
      mMap.put(maxEnt.value, maxEnt);
      pr("constructed", this);
    }

    public void addNum(int num) {

      pr(VERT_SP, "addNum:", num);

      var tail = mMap.tailMap(num);

      var ent = tail.get(tail.firstKey());

      if (ent.value != num) {
        var newEnt = new Ent(num);
        join(ent.prev, newEnt);
        join(newEnt, ent);
        ent = newEnt;
        mMap.put(num, ent);
      }
      ent.frequency++;
      if (medianEnt == null) {
        medianEnt = ent;
        medianOrd = 0;
      }
      population++;

      // Adjust median
      if (num < medianEnt.value) {
        medianOrd++;
      }

      // Get order of the left median value (which is equal to the right value if the population is odd)
      int mi = (population - 1) >> 1;
      if (mi < medianOrd) {
        var n = medianEnt.prev;
        medianOrd -= n.frequency;
        medianEnt = n;
      } else if (mi >= medianOrd + medianEnt.frequency) {
        var n = medianEnt.next;
        medianOrd += medianEnt.frequency;
        medianEnt = n;
      }

      pr(VERT_SP, "added:", num, "pop:", population, "median entry val:", medianEnt.value, "freq:",
          medianEnt.frequency, "medianOrd:", medianOrd);
      pr(this.toString());
    }

    public double findMedian() {
      var leftValue = medianEnt.value;
      // Get order of the left median value (which is equal to the right value if the population is odd)
      int mi = (population - 1) >> 1;
      var medord = medianOrd + mi;

      boolean odd = (population & 1) != 0;
      var rightValue = leftValue;

      int rightorder = medord + (odd ? 0 : 1);
      if (rightorder >= medord + medianEnt.frequency)
        rightValue = medianEnt.next.value;
      pr("leftValue:", leftValue, "odd:", odd, "rightValue:", rightValue);

      return (leftValue + rightValue) / 2.0;
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
}
