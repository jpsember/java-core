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

import java.util.List;
import java.util.Objects;

import js.base.BasePrinter;
import js.data.AbstractData;
import js.json.*;

public final class FRect implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final FRect DEFAULT_INSTANCE = new FRect(0, 0, 0, 0);

  @Override
  public JSList toJson() {
    JSList a = new JSList();
    a.add(x);
    a.add(y);
    a.add(width);
    a.add(height);
    return a;
  }

  @Override
  public FRect parse(Object object) {
    JSList array = (JSList) object;
    int c = 0;
    float x = (float) array.getDouble(c++);
    float y = (float) array.getDouble(c++);
    float w = (float) array.getDouble(c++);
    float h = (float) array.getDouble(c++);
    return new FRect(x, y, w, h);
  }

  @Override
  public FRect build() {
    return this;
  }

  // ------------------------------------------------------------------

  public FRect(float x, float y, float w, float h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  /**
   * Constructor for rectangle at origin
   */
  public FRect(FPoint size) {
    this(0, 0, size.x, size.y);
  }

  public FRect(IRect r) {
    this(r.x, r.y, r.width, r.height);
  }

  public IRect toIRect() {
    return new IRect(this);
  }

  public FPoint location() {
    return new FPoint(x, y);
  }

  public float midX() {
    return (x + width * .5f);
  }

  public float midY() {
    return (y + height * .5f);
  }

  public float maxDim() {
    return Math.max(width, height);
  }

  public float minDim() {
    return Math.min(width, height);
  }

  public boolean equals(FRect r) {
    return r != null && r.x == x && r.y == y && r.width == width && r.height == height;
  }

  public String toString(boolean digitsOnly) {
    StringBuilder sb = new StringBuilder();
    FPoint size = new FPoint(width, height);
    if (!digitsOnly)
      sb.append("(pos=");
    sb.append(location());
    if (!digitsOnly)
      sb.append(" size=");
    sb.append(size);
    if (!digitsOnly)
      sb.append(")");
    return sb.toString();
  }

  public String toString() {
    return toString(false);
  }

  /**
   * Construct smallest rectangle containing two points
   *
   * @param pt1
   * @param pt2
   */
  public FRect(FPoint pt1, FPoint pt2) {
    x = Math.min(pt1.x, pt2.x);
    y = Math.min(pt1.y, pt2.y);
    width = Math.max(pt1.x, pt2.x) - x;
    height = Math.max(pt1.y, pt2.y) - y;
  }

  public FRect(FRect r) {
    this(r.x, r.y, r.width, r.height);
  }

  public FPoint bottomRight() {
    return new FPoint(endX(), y);
  }

  public FRect withInset(float dist) {
    return withInset(dist, dist);
  }

  public FRect withInset(float dx, float dy) {
    return new FRect(x + dx, y + dy, width - 2 * dx, height - 2 * dy);
  }

  public FPoint topLeft() {
    return new FPoint(x, endY());
  }

  public FPoint bottomLeft() {
    return location();
  }

  public FPoint topRight() {
    return new FPoint(endX(), endY());
  }

  public float endX() {
    return x + width;
  }

  public float endY() {
    return y + height;
  }

  public boolean contains(FRect r) {
    return x <= r.x && y <= r.y && endX() >= r.endX() && endY() >= r.endY();
  }

  public FRect including(FRect r) {
    return including(r.topLeft()).including(r.bottomRight());
  }

  public FRect including(FPoint pt) {
    float ex = endX(), ey = endY();
    if (contains(pt))
      return this;

    float nx = Math.min(x, pt.x);
    float ny = Math.min(y, pt.y);
    ex = Math.max(ex, pt.x);
    ey = Math.max(ey, pt.y);
    return new FRect(nx, ny, ex - nx, ey - ny);
  }

  public float distanceFrom(FPoint pt) {
    return MyMath.distanceBetween(pt, nearestPointTo(pt));
  }

  /**
   * Find the nearest point within the rectangle to a query point
   *
   * @param queryPoint
   */
  public FPoint nearestPointTo(FPoint queryPoint) {
    return new FPoint(MyMath.clamp(queryPoint.x, x, endX()), MyMath.clamp(queryPoint.y, y, endY()));
  }

  public FRect translatedBy(float dx, float dy) {
    return new FRect(x + dx, y + dy, width, height);
  }

  /**
   * Transform rectangle, by transforming opposite corners and returning the
   * bounding box of the result. May have unexpected result given that a
   * rectangle is not an arbitrary polygon
   */
  public FRect applyTransform(Matrix m) {
    FPoint c0 = m.apply(bottomLeft());
    FPoint c2 = m.apply(topRight());
    return rectContainingPoints(c0, c2);
  }

  public FPoint midPoint() {
    return new FPoint(midX(), midY());
  }

  public boolean contains(FPoint pt) {
    return x <= pt.x && y <= pt.y && endX() >= pt.x && endY() >= pt.y;
  }

  public FRect translatedBy(FPoint tr) {
    return translatedBy(tr.x, tr.y);
  }

  /**
   * Get point for corner of rectangle
   *
   * @param i
   *          corner number (0..3), bottomleft ccw to topleft
   * @return corner
   */
  public FPoint corner(int i) {
    FPoint ret = null;

    switch (i) {
    default:
      throw new IllegalArgumentException();
    case 0:
      ret = bottomLeft();
      break;
    case 1:
      ret = bottomRight();
      break;
    case 2:
      ret = topRight();
      break;
    case 3:
      ret = topLeft();
      break;
    }

    return ret;
  }

  public static FRect rectContainingPoints(List<FPoint> a) {
    if (a.isEmpty())
      throw new IllegalArgumentException();
    FRect r = null;
    for (FPoint pt : a) {
      if (r == null)
        r = new FRect(pt, pt);
      else
        r = r.including(pt);
    }
    return r;
  }

  public static FRect rectContainingPoints(FPoint s1, FPoint s2) {
    FPoint m1 = new FPoint(Math.min(s1.x, s2.x), Math.min(s1.y, s2.y));
    FPoint m2 = new FPoint(Math.max(s1.x, s2.x), Math.max(s1.y, s2.y));
    return new FRect(m1.x, m1.y, m2.x - m1.x, m2.y - m1.y);
  }

  public boolean intersects(FRect t) {
    return (x < t.endX() && endX() > t.x && y < t.endY() && endY() > t.y);
  }

  public FPoint size() {
    return new FPoint(width, height);
  }

  private void applyTo(BasePrinter p) {
    p.appendString("(");
    p.append(x);
    p.append(y);
    p.append(width);
    p.append(height);
    p.appendString(")");
  }

  static {
    loadTools();
    BasePrinter.registerClassHandler(FRect.class, (x, p) -> ((FRect) x).applyTo(p));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FRect rect = (FRect) o;
    return Float.compare(rect.x, x) == 0 && Float.compare(rect.y, y) == 0
        && Float.compare(rect.width, width) == 0 && Float.compare(rect.height, height) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, width, height);
  }

  public float aspectRatio() {
    return height / width;
  }

  public final float x, y, width, height;

}
