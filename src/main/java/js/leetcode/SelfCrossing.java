package js.leetcode;

import static js.base.Tools.*;

/**
 * I think this is easy? I guess I must be wrong.
 * 
 * Actually, it is easy, once I dealt with a degenerate case.
 */
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

  private String dumpSegs(int[] segs, int nseg) {
    var js = list();
    for (int i = 0; i < nseg; i++)
      js.add(new Pt(segs[i * 2], segs[i * 2 + 1]).toString());
    return js.prettyPrint();
  }

  public boolean isSelfCrossing(int[] distance) {
    final int MAX_SEGS = 6;
    // One additional point for each segment, including initial starting point
    int[] pts = new int[(MAX_SEGS + 1) * 2];

    // Add an origin 0,0
    int npts = addSeg(pts, 0, 0, 0);

    // To deal with the special case of a segment intersecting its (parallel) segment
    // 4 segments back (with no 5th segment), add a zero-length segment immediately
    // to act as this missing 5th segment
    npts = addSeg(pts, npts, 0, 0);

    for (int d : distance) {
      db(VERT_SP, "distance:", d, INDENT, dumpSegs(pts, npts));
      var nx = 0;
      var ny = d;

      npts = addSeg(pts, npts, nx, ny);
      db("after adding segment:", dumpSegs(pts, npts));

      if (npts >= 5) {
        db("...checking for intersection with -5");
        if (segsIntersect(pts, npts - 2, npts - 5))
          return true;
        if (npts >= 7) {
          db("...checking for intersection with -7");
          if (segsIntersect(pts, npts - 2, npts - 7))
            return true;
        }
      }

      int tx = -nx;
      int ty = -ny;

      for (int i = 0; i < npts; i++) {
        int c = i * 2;
        int x = pts[c];
        int y = pts[c + 1];
        x += tx;
        y += ty;

        pts[c] = y;
        pts[c + 1] = -x;
      }
      int removePtsCount = npts - MAX_SEGS;
      if (removePtsCount > 0) {
        System.arraycopy(pts, removePtsCount * 2, pts, 0, pts.length - removePtsCount * 2);
        npts = MAX_SEGS;
      }
    }
    return false;
  }

  private boolean segsIntersect(int[] pts, int segA, int segB) {
    // We can assume s is horizontal and we are vertical

    db("segsIntersect a:", segA, "b:", segB, pts);

    int p0x = pts[segA * 2];
    int p0y = pts[segA * 2 + 1];
    int p1y = pts[(segA+1) * 2 + 1];

    db("...A:",p0x,p0y,"...",p0x,p1y);
    
    int sx0 = pts[segB * 2] - p0x;
    int sx1 = pts[(segB + 1) * 2] - p0y;
    int sy0 = pts[segB * 2 + 1] - p0y;
    db("...B:",sx0,sy0,"...",sx1,sy0);
    
    if (Math.max(sx0, sx1) < 0 || Math.min(sx0, sx1) > 0)
      return false;
    if (sy0 < 0 || sy0 > p1y)
      return false;
    return true;
  }

  private int addSeg(int[] segs, int nseg, int x1, int y1) {
    segs[nseg * 2] = x1;
    segs[nseg * 2 + 1] = y1;
    return nseg + 1;
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
      // We can assume s is horizontal and we are vertical
      checkState(p0.x == p1.x);
      checkState(p1.y >= p0.y);
      int sx0 = s.p0.x - p0.x;
      int sx1 = s.p1.x - p0.x;
      checkState(s.p0.y == s.p1.y);
      int sy0 = s.p0.y - p0.y;
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
