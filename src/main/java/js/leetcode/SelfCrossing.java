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

  // ------------------------------------------------------------------

  public boolean isSelfCrossing(int[] distance) {
    final int MAX_SEGS = 6;
    // One additional point for each segment, including initial starting point
    int[] pts = new int[(distance.length + 1) * 2];

    // Add an origin 0,0
    int npts = addPoint(pts, 0, 0, 0);

    // To deal with the special case of a segment intersecting its (parallel) segment
    // 4 segments back (with no 5th segment), add a zero-length segment immediately
    // to act as this missing 5th segment
    npts = addPoint(pts, npts, 0, 0);

    for (int d : distance) {
      var nx = 0;
      var ny = d;
      npts = addPoint(pts, npts, nx, ny);
      if (npts >= 5) {
        if (segsIntersect(pts, npts - 2, npts - 5))
          return true;
        if (npts >= 7) {
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
    // We can assume A is vertical and B is horizontal 

    int p0x = pts[segA * 2];
    int p0y = pts[segA * 2 + 1];
    int p1y = pts[segA * 2 + 3];

    int sx0 = pts[segB * 2] - p0x;
    int sy0 = pts[segB * 2 + 1] - p0y;
    int sx1 = pts[segB * 2 + 2] - p0y;

    if (Math.max(sx0, sx1) < 0 || Math.min(sx0, sx1) > 0)
      return false;
    return !(sy0 < 0 || sy0 > p1y);
  }

  private int addPoint(int[] segs, int nseg, int x1, int y1) {
    segs[nseg * 2] = x1;
    segs[nseg * 2 + 1] = y1;
    return nseg + 1;
  }

}
