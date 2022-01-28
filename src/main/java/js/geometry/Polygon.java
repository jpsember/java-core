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

import java.awt.geom.Path2D;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import js.data.AbstractData;
import js.data.DataUtil;
import js.json.JSList;
import js.json.JSMap;
import static js.base.Tools.*;

public final class Polygon implements AbstractData {

  private static final String KEY_POINTS = "pt";

  // The key 'p' conflicts with the key used in ElementProperties, so I've renamed it to "pt"
  //
  private static final String KEY_OLDPOINTS = "p";

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final Polygon DEFAULT_INSTANCE = new Polygon(0, false);

  @Override
  public JSMap toJson() {
    JSMap m = map();
    JSList lst = list();
    for (IPoint pt : vertices()) {
      lst.add(pt.x);
      lst.add(pt.y);
    }
    m.put(KEY_POINTS, lst);
    if (isOpen())
      m.put("open", true);
    return m;
  }

  @Override
  public String toString() {
    return toJson().toString();
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Polygon other = (Polygon) o;
    return other.toString().equals(toString());
  }

  @Override
  public Polygon parse(Object object) {
    JSMap m = (JSMap) object;

    // Upgrade from old key to new if necessary
    //
    if (!m.containsKey(KEY_POINTS)) {
      Object oldKeyValue = m.optUnsafe(KEY_OLDPOINTS);
      if (oldKeyValue != null && oldKeyValue instanceof JSList) {
        m.putUnsafe(KEY_POINTS, oldKeyValue);
        m.remove(KEY_OLDPOINTS);
      }
    }

    JSList lst = m.getList(KEY_POINTS);
    int[] coordinates = DataUtil.intArray(lst.wrappedList());
    return new Polygon(constructVertices(coordinates), m.opt("open"));
  }

  @Override
  public Polygon build() {
    return this;
  }

  // ------------------------------------------------------------------

  public boolean isWellDefined() {
    return numVertices() >= (isOpen() ? 2 : 3);
  }

  public int numVertices() {
    return mVerts.length;
  }

  @Deprecated
  public static List<IPoint> extractVertices(List<IPoint> targetOrNull, Iterable<Polygon> polygons) {
    List<IPoint> target = targetOrNull;
    if (target == null)
      target = arrayList();
    for (Polygon p : polygons)
      p.extractVertices(target);
    return target;
  }

  public void extractVertices(List<IPoint> target) {
    for (IPoint v : mVerts)
      target.add(v);
  }

  public Polygon withVertices(Collection<IPoint> v) {
    IPoint[] verts = new IPoint[v.size()];
    int i = -1;
    for (IPoint vert : v) {
      i++;
      verts[i] = vert;
    }
    return new Polygon(verts, mOpen);
  }

  public static Polygon with(IRect rect) {
    List<IPoint> vertices = arrayList();
    for (int i = 0; i < 4; i++)
      vertices.add(rect.corner(i));
    return new Polygon(vertices, false);
  }

  private Polygon(int numVertices, boolean isOpen) {
    this(new IPoint[numVertices], isOpen);
  }

  public Polygon(int[] vertices) {
    this(constructVertices(vertices), false);
  }

  public Path2D toPath() {
    checkState(isWellDefined());
    Path2D.Float path = new Path2D.Float();
    IPoint prevPt = null;
    for (IPoint pt : mVerts) {
      if (prevPt == null)
        path.moveTo(pt.x, pt.y);
      else
        path.lineTo(pt.x, pt.y);
      prevPt = pt;
    }
    if (isClosed())
      path.closePath();
    return path;
  }

  private static IPoint[] constructVertices(int[] vertices) {
    checkArgument((vertices.length & 1) == 0);
    IPoint[] mVerts = new IPoint[vertices.length / 2];

    for (int i = 0; i < vertices.length; i += 2)
      mVerts[i / 2] = new IPoint(vertices[i + 0], vertices[i + 1]);
    return mVerts;
  }

  public Polygon(Polygon template, Polygon vertices) {
    this(vertices.mVerts, template.mOpen);
  }

  public Polygon(Polygon template, Collection<IPoint> vertices) {
    this(IPoint.toArray(vertices), template.mOpen);
  }

  public Polygon(IPoint[] vertices, boolean open) {
    mVerts = Arrays.copyOf(vertices, vertices.length);
    this.mOpen = open;
  }

  public Polygon(Collection<IPoint> vertices) {
    this(vertices, false);
  }

  public static Polygon intArrayToPolygon(int[] v) {
    if (v.length <= 3 * 2)
      return Polygon.DEFAULT_INSTANCE;
    List<IPoint> verts = arrayList();
    for (int i = 0; i < v.length; i += 2)
      verts.add(new IPoint(v[i], v[i + 1]));
    return new Polygon(verts);
  }

  public int[] toIntArray() {
    if (!isWellDefined())
      return DataUtil.EMPTY_INT_ARRAY;

    int[] v = new int[numVertices() * 2];
    for (int i = 0; i < numVertices(); i++) {
      IPoint vert = vertex(i);
      v[i * 2 + 0] = vert.x;
      v[i * 2 + 1] = vert.y;
    }
    return v;
  }

  private Polygon(Collection<IPoint> vertices, boolean isOpen) {
    this(IPoint.toArray(vertices), isOpen);
  }

  /**
   * Get location of a particular point
   */
  public IPoint vertex(int ptIndex) {
    return mVerts[ptIndex];
  }

  /**
   * Get location of a particular point, where index is taken modulo the number
   * of points (useful for walking around a polygon's vertices, for instance)
   * 
   * @param ptIndex
   *          index of point; it is converted to modulo(nPoints())
   * @return location, or null if that point doesn't exist
   */
  public IPoint vertexMod(int ptIndex) {
    return mVerts[MyMath.myMod(ptIndex, numVertices())];
  }

  public IPoint lastVertex() {
    return vertex(numVertices() - 1);
  }

  public boolean hasVertex(int index) {
    return index >= 0 && index < numVertices();
  }

  public IPoint[] vertices() {
    return mVerts;
  }

  public IRect bounds() {
    if (mBoundingRect == null) {
      checkState(isWellDefined());
      mBoundingRect = IRect.rectContainingPoints(arrayList(mVerts));
    }
    return mBoundingRect;
  }

  public boolean isOpen() {
    return mOpen;
  }

  public boolean isClosed() {
    return !mOpen;
  }

  /**
   * Normalize polygon (if necessary) so lowest vertex is first; returns
   * unchanged if open
   */
  public Polygon normalize() {
    checkState(isWellDefined());
    if (isOpen())
      return this;
    int lowest = 0;
    IPoint lowestVert = vertex(0);
    for (int i = 1; i < numVertices(); i++) {
      IPoint vert = vertex(i);
      int result = Integer.compare(vert.y, lowestVert.y);
      if (result == 0)
        result = Integer.compare(vert.x, lowestVert.x);
      if (result < 0) {
        lowestVert = vert;
        lowest = i;
      }
    }
    if (lowest == 0)
      return this;

    List<IPoint> v = arrayList();
    for (int i = 0; i < numVertices(); i++)
      v.add(vertexMod(i + lowest));
    return new Polygon(this, v);
  }

  public boolean isConvex() {
    if (mConvexFlag == null) {
      checkState(isClosed());
      mConvexFlag = winding() == 1 ? this.auxConvex() : reverse().auxConvex();
    }
    return mConvexFlag;
  }

  private boolean auxConvex() {
    IPoint p0 = vertexMod(-2);
    IPoint p1 = lastVertex();
    boolean convex = true;
    for (int i = 0; i < numVertices(); i++) {
      IPoint p2 = vertexMod(i);
      if (MyMath.sideOfLine(p0, p2, p1) >= 0) {
        convex = false;
        break;
      }
      p0 = p1;
      p1 = p2;
    }
    return convex;
  }

  public boolean contains(IPoint pt) {
    checkState(isClosed());
    return closedContains(pt);
  }

  /**
   * Determine if polygon contains a point.
   * 
   * If it's open, treats as closed
   * 
   * @param pt
   * @return
   */
  public boolean closedContains(IPoint pt) {
    // http://geomalgorithms.com/a03-_inclusion.html
    // Also, O'Rourke, p. 240
    checkState(isWellDefined());

    int crossCount = 0;
    IPoint prev = lastVertex();
    for (IPoint curr : vertices()) {
      int side = 0;
      if (curr.y > prev.y) { // up edge
        if (pt.y >= prev.y && pt.y < curr.y) {
          side = MyMath.sideOfLine(prev, curr, pt);
        }
      } else if (curr.y < prev.y) { // down edge
        if (pt.y >= curr.y && pt.y < prev.y) {
          side = MyMath.sideOfLine(curr, prev, pt);
        }
      }
      if (side < 0)
        crossCount++;
      prev = curr;
    }
    return (crossCount & 1) == 1;
  }

  /**
   * Determine winding of polygon.
   * 
   * @return 1 if ccw, -1 if cw; undefined result if polygon is nonsimple
   */
  public int winding() {
    area();
    return MyMath.signum((int) Math.signum(mSignedArea));
  }

  public Polygon centerAtOrigin() {
    IPoint center = bounds().midPoint();
    return applyTransform(Matrix.getTranslate(-center.x, -center.y));
  }

  public Polygon simplify(float maxDistanceFromLine) {
    checkState(isWellDefined());
    List<IPoint> verts = arrayList(mVerts);
    List<IPoint> result = arrayList();
    if (isClosed()) {
      int[] diameterVerts = new int[2];
      farthestVertices(mVerts, diameterVerts);
      List<IPoint> pline1 = simplifyPolyline(verts, diameterVerts[0], diameterVerts[1], maxDistanceFromLine);
      List<IPoint> pline2 = simplifyPolyline(verts, diameterVerts[1], diameterVerts[0], maxDistanceFromLine);
      result.addAll(pline1);
      result.addAll(pline2.subList(1, pline2.size() - 1));
    } else {
      result = simplifyPolyline(verts, 0, numVertices() - 1, maxDistanceFromLine);
    }
    return new Polygon(this, result);
  }

  /**
   * Compute the convex hull of this polygon
   */
  public Polygon convexHull() {
    checkArgument(isWellDefined());
    List<IPoint> verts = arrayList(mVerts);
    List<IPoint> hull = convexHull(verts);
    return new Polygon(hull, false);
  }

  public float perimeter() {
    if (mPerimeter == null) {
      checkState(isWellDefined());
      IPoint prevVertex = lastVertex();
      float perimeter = 0;
      for (IPoint vertex : mVerts) {
        perimeter += MyMath.distanceBetween(prevVertex, vertex);
        prevVertex = vertex;
      }
      mPerimeter = perimeter;
    }
    return mPerimeter;
  }

  public float area() {
    if (mSignedArea == null) {
      checkState(isWellDefined());
      checkState(isClosed());
      IPoint prev = lastVertex();
      float twiceArea = 0;
      for (IPoint curr : mVerts) {
        twiceArea += prev.x * curr.y - curr.x * prev.y;
        prev = curr;
      }
      mSignedArea = twiceArea * 0.5f;
    }
    return Math.abs(mSignedArea);
  }

  /**
   * If polygon does not have a ccw winding, return one that does
   */
  public Polygon ccw() {
    if (winding() != 1) {
      Polygon result = reverse();
      result.mSignedArea = -mSignedArea;
      return result;
    }
    return this;
  }

  public Polygon reverse() {
    List<IPoint> v = arrayList();
    for (int j = numVertices() - 1; j >= 0; j--)
      v.add(mVerts[j]);
    Polygon reversed = new Polygon(this, v);
    reversed.mConvexFlag = mConvexFlag;
    reversed.mBoundingRect = mBoundingRect;
    reversed.mPerimeter = mPerimeter;
    if (mSignedArea != null) {
      reversed.mSignedArea = -mSignedArea;
    }
    return reversed;
  }

  public Polygon rotateVertices(int steps) {
    checkState(isClosed());
    steps = MyMath.myMod(steps, numVertices());
    if (steps == 0)
      return this;
    List<IPoint> v = arrayList();
    for (int i = 0; i < numVertices(); i++)
      v.add(vertexMod(i + steps));
    return new Polygon(v, false);
  }

  public IPoint randomPointWithin(Random randomOrNull, int maxAttempts) {
    checkState(isClosed());
    Random random = MyMath.orDefault(randomOrNull);
    IRect bounds = bounds();
    GeometryException.checkState(bounds.width > 0 && bounds.height > 0, "degenerate bounds");
    for (int i = 0; i < maxAttempts; i++) {
      int x = bounds.x + random.nextInt(bounds.width);
      int y = bounds.y + random.nextInt(bounds.height);
      IPoint pt = new IPoint(x, y);
      if (contains(pt))
        return pt;
    }
    throw new GeometryException("failed to find point within:", toJson());
  }

  public float boundaryDistanceFrom(IPoint pt) {

    // Our 'distance to segment' method expects FPoint arguments
    FPoint fp = pt.toFPoint();

    int j = isClosed() ? 0 : 1;
    FPoint p0 = vertexMod(j - 1).toFPoint();

    float bestDist = -1;
    while (j < numVertices()) {
      FPoint p1 = vertex(j).toFPoint();
      float dist = MyMath.ptDistanceToSegment(fp, p0, p1, null);
      if (bestDist < 0 || dist < bestDist)
        bestDist = dist;
      p0 = p1;
      j++;
    }
    return bestDist;
  }

  private static List<IPoint> simplifyPolyline(List<IPoint> vertices, int startVertex, int endVertex,
      float maxDistanceFromLine) {

    IPoint p0 = vertices.get(startVertex);
    IPoint p1 = vertices.get(endVertex);

    List<IPoint> polyline = arrayList();
    polyline.add(p0);

    // Find furthest vertex from line
    int farthest = -1;
    float maxDist = -1;
    int j = startVertex;

    FPoint f0 = p0.toFPoint();
    FPoint f1 = p1.toFPoint();
    // TODO: precalculate the unit distance of the line from f0..f1, avoid
    // embedding this in ptDistanceToLine
    while (true) {
      j = (j + 1) % vertices.size();
      if (j == endVertex)
        break;
      IPoint vj = vertices.get(j);
      polyline.add(vj);
      float dist = MyMath.ptDistanceToLine(vj.toFPoint(), f0, f1, null);
      if (maxDist < dist) {
        maxDist = dist;
        farthest = j;
      }
    }
    if (maxDist <= maxDistanceFromLine) {
      polyline.clear();
      polyline.add(p0);
      polyline.add(p1);
      return polyline;
    }
    {
      polyline.clear();
      polyline.addAll(simplifyPolyline(vertices, startVertex, farthest, maxDistanceFromLine));
      pop(polyline); // to avoid storing vertex 'farthest' twice
      polyline.addAll(simplifyPolyline(vertices, farthest, endVertex, maxDistanceFromLine));
      return polyline;
    }
  }

  private static void farthestVertices(IPoint[] polygon, int[] output) {
    // There are more efficient algorithms, an O(n) caliper method, but for now...   
    float maxDist = -1;
    for (int i = 1; i < polygon.length; i++) {
      IPoint pi = polygon[i];
      for (int j = 0; j < i; j++) {
        IPoint pj = polygon[j];
        float ds = MyMath.squaredDistanceBetween(pi, pj);
        if (ds > maxDist) {
          maxDist = ds;
          output[0] = j;
          output[1] = i;
        }
      }
    }
  }

  public Polygon applyTransform(Matrix m) {
    if (numVertices() == 0)
      return this;
    Polygon newPoly = new Polygon(numVertices(), isOpen());
    for (int i = 0; i < mVerts.length; i++)
      newPoly.mVerts[i] = m.apply(mVerts[i]);
    return newPoly;
  }

  public static List<Polygon> applyTransform(Iterable<Polygon> polygons, Matrix m) {
    List<Polygon> transformedPolygons = arrayList();
    for (Polygon orig : polygons)
      transformedPolygons.add(orig.applyTransform(m));
    return transformedPolygons;
  }

  public Polygon withOpen(boolean open) {
    if (open == isOpen())
      return this;
    return new Polygon(mVerts, open);
  }

  /**
   * Construct convex hull of a set of points, using the QuickHull algorithm
   */
  public static List<IPoint> convexHull(List<IPoint> points) {
    checkArgument(points.size() > 2);

    // Determine a=rightmost, b=leftmost points in set
    IPoint a = points.get(0);
    IPoint b = a;

    for (IPoint p : points) {
      if (p.x > a.x)
        a = p;
      else if (p.x < b.x)
        b = p;
    }

    // Partition set into points to right, left of line a->b
    List<IPoint> setRight = arrayList();
    List<IPoint> setLeft = arrayList();

    for (IPoint p : points) {
      if (p == a || p == b)
        continue;
      if (MyMath.sideOfLine(a, b, p) > 0)
        setLeft.add(p);
      else
        setRight.add(p);
    }

    List<IPoint> hullPoints = arrayList();
    hullPoints.add(a);
    auxHull(setRight, a, b, hullPoints);
    hullPoints.add(b);
    auxHull(setLeft, b, a, hullPoints);
    return hullPoints;
  }

  /**
   * Find point most to the left of the line a->b
   */
  private static int leftmostPoint(IPoint a, IPoint b, List<IPoint> points) {
    long furthestDist = 0;
    int furthestIndex = -1;
    // Translate so a is at origin.
    // Use long to avoid overflowing integer range
    long sx = b.x - a.x;
    long sy = b.y - a.y;
    for (int i = 0; i < points.size(); i++) {
      IPoint c = points.get(i);
      int tx = c.x - a.x;
      int ty = c.y - a.y;
      long signedDistance = (sy * tx - sx * ty);
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
  private static void auxHull(List<IPoint> points, IPoint a, IPoint b, List<IPoint> result) {
    if (points.isEmpty())
      return;

    int leftmostPoint = leftmostPoint(a, b, points);

    if (leftmostPoint >= 0) {
      IPoint furthest = points.get(leftmostPoint);
      List<IPoint> sublist = arrayList();
      List<IPoint> sublist2 = arrayList();
      for (IPoint c : points) {
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

  /**
   * Remove polygon vertices that lie on the same axis-aligned segment as their
   * neighbors
   */
  @Deprecated // Delete when appropriate
  public static void filterColinearVertices(List<IPoint> path) {
    int len = path.size();
    if (len == 0)
      return;
    IPoint ptPrev = last(path);
    int cursor = 0;
    int destination = 0;
    while (cursor < len) {
      IPoint current = path.get(cursor);
      cursor++;
      IPoint next = (cursor == len) ? path.get(0) : path.get(cursor);
      IPoint prev = ptPrev;
      ptPrev = current;
      if ((prev.x == next.x && prev.x == current.x) || (prev.y == next.y && prev.y == current.y)) {
        continue;
      }

      path.set(destination, current);
      destination++;
    }
    removeAllButFirstN(path, destination);
  }

  private final IPoint[] mVerts;
  private final boolean mOpen;
  private IRect mBoundingRect;
  private Boolean mConvexFlag;
  private Float mPerimeter;
  private Float mSignedArea;

  /**
   * A side-effect free description
   */
  public JSMap debugJson() {
    JSMap m = map();
    JSList lst = list();
    for (IPoint pt : vertices()) {
      lst.add(pt.x);
      lst.add(pt.y);
    }
    m.put("vertices", lst);
    if (mBoundingRect != null)
      m.put("bounding rect", mBoundingRect.toJson());
    if (mConvexFlag != null)
      m.put("convex", mConvexFlag);
    if (mPerimeter != null)
      m.put("perim", mPerimeter);
    if (mSignedArea != null)
      m.put("signed area", mSignedArea);
    if (isOpen())
      m.put("open", true);
    return m;
  }

}
