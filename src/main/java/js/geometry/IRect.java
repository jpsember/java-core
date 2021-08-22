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

import java.awt.Rectangle;
import java.util.Objects;

import js.base.BasePrinter;
import js.data.AbstractData;
import js.json.*;

/**
 * Rectangle with integer coordinates; modelled after Rect
 */
public final class IRect implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final IRect DEFAULT_INSTANCE = new IRect(0, 0, 0, 0);

  @Override
  public JSList toJson() {
    JSList a = list();
    a.add(x);
    a.add(y);
    a.add(width);
    a.add(height);
    return a;
  }

  @Override
  public IRect parse(Object object) {
    JSList array = (JSList) object;
    return new IRect(array.getInt(0), array.getInt(1), array.getInt(2), array.getInt(3));
  }

  @Override
  public IRect build() {
    return this;
  }

  // ------------------------------------------------------------------

  // Copy constructors

  public IRect(FRect r) {
    this(Math.round(r.x), Math.round(r.y), Math.round(r.width), Math.round(r.height));
  }

  public IRect(Rectangle r) {
    this(r.x, r.y, r.width, r.height);
  }

  public FRect toRect() {
    return new FRect(this);
  }

  public Rectangle toRectangle() {
    return new Rectangle(x, y, width, height);
  }

  public int midX() {
    return (x + width / 2);
  }

  public int midY() {
    return (y + height / 2);
  }

  public int area() {
    return width * height;
  }

  public IPoint center() {
    return new IPoint(midX(), midY());
  }

  public int maxDim() {
    return Math.max(width, height);
  }

  public int minDim() {
    return Math.min(width, height);
  }

  public boolean equals(IRect r) {
    return r != null && r.x == x && r.y == y && r.width == width && r.height == height;
  }

  public IRect(int x, int y, int w, int h) {
    this.x = x;
    this.y = y;
    this.width = w;
    this.height = h;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    IPoint loc = new IPoint(x, y);
    IPoint size = new IPoint(width, height);
    sb.append(loc);
    sb.append(size);
    return sb.toString();
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
    BasePrinter.registerClassHandler(IRect.class, (x, p) -> ((IRect) x).applyTo(p));
  }

  /**
   * Construct smallest rectangle containing two points
   */
  public IRect(IPoint pt1, IPoint pt2) {
    x = Math.min(pt1.x, pt2.x);
    y = Math.min(pt1.y, pt2.y);
    width = Math.max(pt1.x, pt2.x) - x;
    height = Math.max(pt1.y, pt2.y) - y;
  }

  public IRect(IPoint size) {
    this(0, 0, size.x, size.y);
  }

  public IRect withInset(int amount) {
    return withInset(amount, amount);
  }

  public IRect withInset(int dx, int dy) {
    return new IRect(x + dx, y + dy, width - 2 * dx, height - 2 * dy);
  }

  public IPoint location() {
    return new IPoint(x, y);
  }

  public IPoint bottomLeft() {
    return location();
  }

  public IPoint bottomRight() {
    return new IPoint(endX(), y);
  }

  public IPoint topRight() {
    return new IPoint(endX(), endY());
  }

  public IPoint topLeft() {
    return new IPoint(x, endY());
  }

  public int endX() {
    return x + width;
  }

  public int endY() {
    return y + height;
  }

  public boolean contains(IRect r) {
    return x <= r.x && y <= r.y && endX() >= r.endX() && endY() >= r.endY();
  }

  public IRect including(IRect r) {
    return including(r.topLeft()).including(r.bottomRight());
  }

  public IRect including(IPoint pt) {
    if (contains(pt))
      return this;
    int ex = endX(), ey = endY();
    int nx = Math.min(x, pt.x);
    int ny = Math.min(y, pt.y);
    ex = Math.max(ex, pt.x);
    ey = Math.max(ey, pt.y);
    return new IRect(nx, ny, ex - nx, ey - ny);
  }

  public float distanceFrom(IPoint pt) {
    return MyMath.distanceBetween(new FPoint(pt), new FPoint(nearestPointTo(pt)));
  }

  /**
   * Find the nearest point within the rectangle to a query point
   *
   * @param queryPoint
   */
  public IPoint nearestPointTo(IPoint queryPoint) {
    return new IPoint(MyMath.clamp(queryPoint.x, x, endX()), MyMath.clamp(queryPoint.y, y, endY()));
  }

  /**
   * Transform rectangle, by transforming opposite corners and returning the
   * bounding box of the result. May have unexpected result given that a
   * rectangle is not an arbitrary polygon
   */
  public IRect applyTransform(Matrix m) {
    IPoint c0 = m.apply(bottomLeft());
    IPoint c2 = m.apply(topRight());
    return rectContainingPoints(c0, c2);
  }

  public IRect translatedBy(int dx, int dy) {
    return new IRect(x + dx, y + dy, width, height);
  }

  public IPoint midPoint() {
    return new IPoint(midX(), midY());
  }

  public boolean contains(IPoint pt) {
    return x <= pt.x && y <= pt.y && endX() >= pt.x && endY() >= pt.y;
  }

  public IRect translatedBy(IPoint tr) {
    return translatedBy(tr.x, tr.y);
  }

  /**
   * Get point for corner of rectangle
   *
   * @param i
   *          corner number (0..3), bottomleft ccw to topleft
   * @return corner
   */
  public IPoint corner(int i) {
    switch (i) {
    default:
      throw new IllegalArgumentException();
    case 0:
      return bottomLeft();
    case 1:
      return bottomRight();
    case 2:
      return topRight();
    case 3:
      return topLeft();
    }
  }

  public static IRect rectContainingPoints(Iterable<IPoint> a) {
    IRect r = null;
    for (IPoint pt : a) {
      if (r == null)
        r = new IRect(pt, pt);
      else
        r = r.including(pt);
    }
    checkArgument(r != null);
    return r;
  }

  public static IRect rectContainingPoints(IPoint s1, IPoint s2) {
    IPoint m1 = new IPoint(Math.min(s1.x, s2.x), Math.min(s1.y, s2.y));
    IPoint m2 = new IPoint(Math.max(s1.x, s2.x), Math.max(s1.y, s2.y));
    return new IRect(m1.x, m1.y, m2.x - m1.x, m2.y - m1.y);
  }

  public boolean intersects(IRect t) {
    return (x < t.endX() && endX() > t.x && y < t.endY() && endY() > t.y);
  }

  public static IRect intersection(IRect a, IRect b) {
    int x0 = Math.max(a.x, b.x);
    int x1 = Math.min(a.endX(), b.endX());
    if (x0 >= x1)
      return null;

    int y0 = Math.max(a.y, b.y);
    int y1 = Math.min(a.endY(), b.endY());
    if (y0 >= y1)
      return null;
    return new IRect(x0, y0, x1 - x0, y1 - y0);
  }

  public IPoint size() {
    return new IPoint(width, height);
  }

  public boolean isValid() {
    return !isDegenerate();
  }

  public boolean isDegenerate() {
    return width <= 0 || height <= 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    IRect iRect = (IRect) o;
    return x == iRect.x && y == iRect.y && width == iRect.width && height == iRect.height;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, width, height);
  }

  public float aspectRatio() {
    return height / (float) width;
  }

  public final int x, y, width, height;

  public static IRect withLocAndSize(IPoint pos, IPoint size) {
    if (pos == null)
      pos = IPoint.ZERO;
    if (size == null)
      size = IPoint.ZERO;
    return new IRect(pos.x, pos.y, size.x, size.y);
  }

  public IRect withLocation(IPoint loc) {
    return new IRect(loc.x, loc.y, width, height);
  }
}
