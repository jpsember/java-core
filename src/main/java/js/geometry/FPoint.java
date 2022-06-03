/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.geometry;

import static js.base.Tools.*;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import js.data.AbstractData;
import js.json.*;

public final class FPoint implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final FPoint DEFAULT_INSTANCE = new FPoint();
  public static final FPoint[] EMPTY_ARRAY = new FPoint[0];

  @Override
  public JSList toJson() {
    JSList a = list();
    a.add(x);
    a.add(y);
    return a;
  }

  @Override
  public FPoint parse(Object object) {
    JSList array = (JSList) object;
    return new FPoint(array.getFloat(0), array.getFloat(1));
  }

  @Override
  public FPoint build() {
    return this;
  }

  // ------------------------------------------------------------------

  public static final FPoint ZERO = DEFAULT_INSTANCE;

  private FPoint() {
    x = 0;
    y = 0;
  }

  public FPoint(float x, float y) {
    this.x = x;
    this.y = y;
  }

  public FPoint(double x, double y) {
    this.x = (float) x;
    this.y = (float) y;
  }

  public FPoint(IPoint point) {
    this(point.x, point.y);
  }

  public IPoint toIPoint() {
    return new IPoint(ix(), iy());
  }

  public float squaredMagnitude() {
    return MyMath.squaredMagnitudeOfRay(x, y);
  }

  public float magnitude() {
    return MyMath.magnitudeOfRay(x, y);
  }

  public FPoint withX(float x) {
    return new FPoint(x, this.y);
  }

  public FPoint withY(float y) {
    return new FPoint(this.x, y);
  }

  public FPoint scaledBy(float scaleFactor) {
    return new FPoint(x * scaleFactor, y * scaleFactor);
  }

  public FPoint scaledBy(FPoint scaleFactor) {
    return new FPoint(x * scaleFactor.x, y * scaleFactor.y);
  }

  public FPoint scaledBy(IPoint scaleFactor) {
    return new FPoint(x * scaleFactor.x, y * scaleFactor.y);
  }

  public float product() {
    return x * y;
  }

  public static FPoint sum(FPoint a, FPoint b) {
    return new FPoint(a.x + b.x, a.y + b.y);
  }

  public static FPoint difference(FPoint a, FPoint b) {
    return new FPoint(a.x - b.x, a.y - b.y);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(toStr(x));
    sb.append(' ');
    sb.append(toStr(y));
    return sb.toString();
  }

  public String toStringAsInts() {
    StringBuilder sb = new StringBuilder();
    sb.append(toStr((int) x, 4));
    sb.append(' ');
    sb.append(toStr((int) y, 4));
    return sb.toString();
  }

  public int ix() {
    return Math.round(x);
  }

  public int iy() {
    return Math.round(y);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FPoint fPoint = (FPoint) o;
    return Float.compare(fPoint.x, x) == 0 && Float.compare(fPoint.y, y) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  public final float x;
  public final float y;

  public static FPoint interpolate(FPoint p1, FPoint p2, float t) {
    return new FPoint(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y));
  }

  public static FPoint midPoint(FPoint p1, FPoint p2) {
    return interpolate(p1, p2, .5f);
  }

  private static final FPoint[] ZERO_LENGTH_ARRAY = new FPoint[0];

  public static FPoint[] toArray(Collection<FPoint> collection) {
    return collection.toArray(ZERO_LENGTH_ARRAY);
  }

  public float aspectRatio() {
    return y / x;
  }

  public static List<FPoint> convert(List<IPoint> fpoints) {
    List<FPoint> f = arrayList();
    for (IPoint p : fpoints)
      f.add(p.toFPoint());
    return f;
  }

  public static FPoint onCircle(float originX, float originY, float angle, float radius) {
    return new FPoint(originX + radius * (float) Math.cos(angle), originY + radius * (float) Math.sin(angle));
  }

  public static FPoint onCircle(FPoint origin, float angle, float radius) {
    return onCircle(origin.x, origin.y, angle, radius);
  }

  /**
   * Construct convex hull of a set of points, using the QuickHull algorithm
   */
  public static List<FPoint> convexHull(List<FPoint> points) {
    checkArgument(points.size() > 2, "too few points for hull");

    // Determine a=rightmost, b=leftmost points in set
    FPoint a = points.get(0);
    FPoint b = a;

    for (FPoint p : points) {
      if (p.x > a.x)
        a = p;
      else if (p.x < b.x)
        b = p;
    }

    // Partition set into points to right, left of line a->b
    List<FPoint> setRight = arrayList();
    List<FPoint> setLeft = arrayList();

    for (FPoint p : points) {
      if (p == a || p == b)
        continue;
      if (MyMath.sideOfLine(a, b, p) > 0)
        setLeft.add(p);
      else
        setRight.add(p);
    }

    List<FPoint> hullPoints = arrayList();
    hullPoints.add(a);
    auxHull(setRight, a, b, hullPoints);
    hullPoints.add(b);
    auxHull(setLeft, b, a, hullPoints);
    return hullPoints;
  }

  /**
   * Find point most to the left of the line a->b
   */
  private static int leftmostPoint(FPoint a, FPoint b, List<FPoint> points) {
    float furthestDist = 0;
    int furthestIndex = -1;
    // Translate so a is at origin.
    // Use long to avoid overflowing integer range
    float sx = b.x - a.x;
    float sy = b.y - a.y;
    for (int i = 0; i < points.size(); i++) {
      FPoint c = points.get(i);
      float tx = c.x - a.x;
      float ty = c.y - a.y;
      float signedDistance = (sy * tx - sx * ty);
      if (signedDistance > furthestDist) {
        furthestDist = signedDistance;
        furthestIndex = i;
      }
    }
    return furthestIndex;
  }

  /**
   * Calculate points lying on hull to left of line from a->b
   */
  private static void auxHull(List<FPoint> points, FPoint a, FPoint b, List<FPoint> result) {
    if (points.isEmpty())
      return;

    int leftmostPoint = leftmostPoint(a, b, points);

    if (leftmostPoint >= 0) {
      FPoint furthest = points.get(leftmostPoint);
      List<FPoint> sublist = arrayList();
      List<FPoint> sublist2 = arrayList();
      for (FPoint c : points) {
        if (c == furthest)
          continue;
        if (MyMath.sideOfLine(a, furthest, c) < 0)
          sublist.add(c);
        else if (MyMath.sideOfLine(furthest, b, c) < 0)
          sublist2.add(c);
      }
      auxHull(sublist, a, furthest, result);
      result.add(furthest);
      auxHull(sublist2, furthest, b, result);
    }
  }

}
