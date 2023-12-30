package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.List;

public class SelfCrossing extends LeetCode {

  public static void main(String[] args) {
    new SelfCrossing().run();
  }

  public void run() {

    x("[2,1,1,2]", true);

    x("[1,2,3,4]", false);

  }

  private void x(String s, boolean expected) {
    db = true;

    var distance = extractNums(s);

    var result = isSelfCrossing(distance);
    pr("distance:", distance, result);
    verify(result, expected);

  }

  public boolean isSelfCrossing(int[] distance) {
    List<Seg> s = new ArrayList<>();
    var origin = new Pt(0, 0);
    for (int d : distance) {
      var seg = new Seg(origin, new Pt(origin.x, origin.y + d));
      db("constructed:", seg);
      s.add(seg);

      int sz = s.size();
      if (sz > 3) {
        if (seg.intersects(s.get(sz - 1 - 3)))
          return true;
        if (sz > 5) {
          if (seg.intersects(s.get(sz - 1 - 5)))
            return true;
        }
      }

      var newOrigin = seg.p1;
      for (int i = 0; i < s.size(); i++) {
        var seg2 = s.get(i);
        seg2 = seg2.translate(-newOrigin.x, -newOrigin.y);
        seg2 = seg2.rotate(-1);
        s.set(i, seg2);
      }
      if (s.size() > 5)
        s.remove(0);

      db("rotated and translated:", INDENT, s);
    }
    return false;
  }

  private class Pt {
    final int x;
    final int y;

    Pt(int x, int y) {
      this.x = x;
      this.y = y;
    }

    Pt rot(int dir) {
      // We actually only need to rotate by -90 degrees
      checkState(dir == -1);
      dir &= 3;
      switch (dir) {
      default:
        return this;
      case 1:
        return new Pt(-y, x);
      case 2:
        return new Pt(-x, -y);
      case 3:
        return new Pt(y, -x);
      }
    }

    Pt translate(int dx, int dy) {
      return new Pt(x + dx, y + dy);
    }

    @Override
    public String toString() {
      return "(" + x + " " + y + ")";
    }
  }

  private class Seg {
    public final Pt p0, p1;

    Seg(Pt p0, Pt p1) {
      this.p0 = p0;
      this.p1 = p1;
    }

    Seg rotate(int dir) {
      return new Seg(p0.rot(dir), p1.rot(dir));
    }

    Seg translate(int dx, int dy) {
      return new Seg(p0.translate(dx, dy), p1.translate(dx, dy));
    }

    boolean intersects(Seg s) {
      pr("seg", this, "intersects", s, "?");
      // We can assume s is horizontal and we are vertical
      checkState(p0.x == p1.x);
      checkState(p1.y >= p0.y);
      int sx0 = s.p0.x - p0.x;
      int sx1 = s.p1.x - p0.x;
      checkState(s.p0.y == s.p1.y);
      int sy0 = s.p0.y - p0.y;
      //  int sy1 = s.p1.y - p0.y;
      if (Math.max(sx0, sx1) < 0 || Math.min(sx0, sx1) > 0)
        return false;
      if (sy0 < 0 || sy0 > p1.y)
        return false;
      return true;
    }

    @Override
    public String toString() {
      return "[" + p0 + "..." + p1 + "]";
    }

  }

}
