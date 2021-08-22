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

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

import static js.base.Tools.*;

import js.base.BasePrinter;
import js.data.AbstractData;
import js.json.JSList;
import js.json.JSMap;

public class IPoint implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final IPoint DEFAULT_INSTANCE = new IPoint();

  @Override
  public JSList toJson() {
    JSList a = list();
    a.add(x);
    a.add(y);
    return a;
  }

  @Override
  public IPoint parse(Object object) {
    JSList array = (JSList) object;
    int x = array.getInt(0);
    int y = array.getInt(1);
    return new IPoint(x, y);
  }

  @Override
  public IPoint build() {
    return this;
  }

  // ------------------------------------------------------------------

  public static final IPoint ZERO = DEFAULT_INSTANCE;

  public static final IPoint[] EMPTY_ARRAY = new IPoint[0];

  public static IPoint[] toArray(Collection<IPoint> points) {
    return points.toArray(EMPTY_ARRAY);
  }

  public static List<IPoint> toArrayList(int... coords) {
    int numPoints = coords.length / 2;
    checkArgument(coords.length == numPoints * 2);
    List<IPoint> output = new ArrayList<>(numPoints);
    for (int i = 0; i < coords.length; i += 2)
      output.add(new IPoint(coords[i], coords[i + 1]));
    return output;
  }

  private IPoint() {
    x = 0;
    y = 0;
  }

  public IPoint(FPoint src) {
    this(src.x, src.y);
  }

  public IPoint(float x, float y) {
    this.x = Math.round(x);
    this.y = Math.round(y);
  }

  public IPoint(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public IPoint(Dimension dimension) {
    this.x = dimension.width;
    this.y = dimension.height;
  }

  public static IPoint sum(IPoint a, IPoint b) {
    return new IPoint(a.x + b.x, a.y + b.y);
  }

  public static IPoint difference(IPoint a, IPoint b) {
    return new IPoint(a.x - b.x, a.y - b.y);
  }

  public IPoint negate() {
    return new IPoint(-x, -y);
  }

  public IPoint sumWith(IPoint other) {
    return IPoint.sum(this, other);
  }

  public IPoint sumWith(FPoint other) {
    return sumWith(other.ix(), other.iy());
  }

  public IPoint sumWith(int x, int y) {
    return new IPoint(this.x + x, this.y + y);
  }

  public IPoint withX(int x) {
    return new IPoint(x, this.y);
  }

  public IPoint withY(int y) {
    return new IPoint(this.x, y);
  }

  public IPoint withX(float x) {
    return new IPoint((int) x, this.y);
  }

  public IPoint withY(float y) {
    return new IPoint(this.x, (int) y);
  }

  public IPoint scaledBy(float scaleFactor) {
    return new IPoint(x * scaleFactor, y * scaleFactor);
  }

  /**
   * Returns true if 0 <= pt.x <= x and 0 <= pt.y <= y
   */
  public boolean contains(IPoint pt) {
    return pt.x >= 0 && pt.y >= 0 && pt.x <= x && pt.y <= y;
  }

  public static IPoint interp(IPoint p0, IPoint p1, float t) {
    return new IPoint((p0.x * (1 - t)) + p1.x * t, (p0.y * (1 - t)) + p1.y * t);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(toStr(x));
    sb.append(' ');
    sb.append(toStr(y));
    return sb.toString();
  }

  private void applyTo(BasePrinter p) {
    p.appendString("(");
    p.append(x);
    p.append(y);
    p.appendString(")");
  }

  static {
    BasePrinter.registerClassHandler(IPoint.class, (x, p) -> ((IPoint) x).applyTo(p));
  }

  public FPoint toFPoint() {
    return new FPoint(x, y);
  }

  public Dimension toDimension() {
    return new Dimension(x, y);
  }

  public Point toPoint() {
    return new Point(x, y);
  }

  public final int x;
  public final int y;

  public boolean isZero() {
    return x == 0 && y == 0;
  }

  /**
   * Determine if either coordinate is nonzero
   */
  public boolean nonZero() {
    return !isZero();
  }

  public int product() {
    return x * y;
  }

  public boolean positive() {
    return x > 0 && y > 0;
  }

  public IPoint clampTo(IRect bounds) {
    int x = MyMath.clamp(this.x, bounds.x, bounds.endX());
    int y = MyMath.clamp(this.y, bounds.y, bounds.endY());
    if (x != this.x || y != this.y)
      return new IPoint(x, y);
    return this;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    IPoint iPoint = (IPoint) o;
    return x == iPoint.x && y == iPoint.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y);
  }

  public float magnitude() {
    return MyMath.magnitudeOfRay(x, y);
  }

  public static List<IPoint> convert(List<FPoint> fpoints) {
    List<IPoint> f = arrayList();
    for (FPoint p : fpoints)
      f.add(p.toIPoint());
    return f;
  }

  public static List<IPoint> convexHull(List<IPoint> vertices) {
    return IPoint.convert(FPoint.convexHull(FPoint.convert(vertices)));
  }

  /**
   * Parse an IPoint from a JSMap
   */
  public static IPoint get(JSMap jsonMap, String key) {
    JSMap m = jsonMap.optJSMap(key);
    if (m == null)
      throw badArg("No IPoint found for key:", key, INDENT, jsonMap);
    return DEFAULT_INSTANCE.parse(m);
  }
}
