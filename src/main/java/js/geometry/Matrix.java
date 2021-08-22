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

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.List;

import js.base.BasePrinter;
import js.data.AbstractData;
import js.json.JSList;

/**
 * Matrix for 2D affine transformations; modelled after iOS CGAffineTransform
 *
 * <pre>
 *
 * It represents the following matrix M:
 *
 *   | a  c  tx |
 *   | b  d  ty |
 *   | 0  0  1  |
 *
 * If (x,y) is a point, we will use the notation (x,y)' to refer to the column vector:
 *
 *   | x |
 *   | y |
 *
 * </pre>
 */
public final class Matrix implements AbstractData {

  // ------------------------------------------------------------------
  // AbstractData interface
  // ------------------------------------------------------------------

  public static final Matrix DEFAULT_INSTANCE = new Matrix(1, 0, 0, 1, 0, 0);

  @Override
  public JSList toJson() {
    return list().add(a).add(b).add(c).add(d).add(tx).add(ty);
  }

  @Override
  public Matrix parse(Object object) {
    JSList coeff = (JSList) object;
    return new Matrix(coeff.getFloat(0), coeff.getFloat(1), coeff.getFloat(2), coeff.getFloat(3),
        coeff.getFloat(4), coeff.getFloat(5));
  }

  @Override
  public Matrix build() {
    return this;
  }

  // ------------------------------------------------------------------

  public static final Matrix IDENTITY = DEFAULT_INSTANCE;

  public final float a, b, c, d, tx, ty;

  public Matrix(float a, float b, float c, float d, float tx, float ty) {
    this.a = a;
    this.b = b;
    this.c = c;
    this.d = d;
    this.tx = tx;
    this.ty = ty;
  }

  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("\n[");
    sb.append(toStr(a));
    sb.append(toStr(c));
    sb.append(toStr(tx));
    sb.append("]\n");
    sb.append("[");
    sb.append(toStr(b));
    sb.append(toStr(d));
    sb.append(toStr(ty));
    sb.append("]\n");
    sb.append("[");
    sb.append(toStr(0.0f));
    sb.append(toStr(0.0f));
    sb.append(toStr(1.0f));
    sb.append("]\n");
    return sb.toString();
  }

  private void applyTo(BasePrinter p) {
    p.appendString("[");
    p.append(a);
    p.append(c);
    p.append(tx);
    p.appendString("]");
    p.cr();
    p.appendString("[");
    p.append(b);
    p.append(d);
    p.append(ty);
    p.appendString("]");
    p.cr();
    p.appendString("[");
    p.append(0.0f);
    p.append(0.0f);
    p.append(1.0f);
    p.appendString("]");
  }

  static {
    BasePrinter.registerClassHandler(Matrix.class, (x, p) -> ((Matrix) x).applyTo(p));
  }

  /**
   * Given a point p = (x,y,1)', calculate r = M * p = (rx,ry,1)'
   */
  public FPoint apply(float x, float y) {
    return new FPoint(a * x + c * y + tx, b * x + d * y + ty);
  }

  public IPoint apply(IPoint integerPt) {
    float x = integerPt.x;
    float y = integerPt.y;
    return new IPoint(a * x + c * y + tx, b * x + d * y + ty);
  }

  /**
   * Return Matrix.multiply(other, this)
   */
  public Matrix pcat(Matrix other) {
    if (other == IDENTITY)
      return this;
    return Matrix.multiply(other, this);
  }

  /**
   * Return Matrix.multiply(this, other)
   */
  public Matrix cat(Matrix other) {
    if (other == IDENTITY)
      return this;
    return Matrix.multiply(this, other);
  }

  public FPoint apply(FPoint source) {
    return apply(source.x, source.y);
  }

  public List<IPoint> applyIPoints(Iterable<IPoint> points) {
    List<IPoint> result = arrayList();
    for (IPoint point : points)
      result.add(apply(point));
    return result;
  }

  public List<FPoint> applyFPoints(Iterable<FPoint> points) {
    List<FPoint> result = arrayList();
    for (FPoint point : points)
      result.add(apply(point));
    return result;
  }

  public static Matrix getTranslate(FPoint translate) {
    return getTranslate(translate.x, translate.y);
  }

  public static Matrix getTranslate(IPoint translate) {
    return getTranslate(translate.x, translate.y);
  }

  public static Matrix getTranslate(float tx, float ty) {
    return new Matrix(1, 0, 0, 1, tx, ty);
  }

  public static Matrix getRotate(float angleInRadians) {
    float c = MyMath.cos(angleInRadians), s = MyMath.sin(angleInRadians);
    return new Matrix(c, s, -s, c, 0, 0);
  }

  public static Matrix getScale(float scaleFactor) {
    return getScale(scaleFactor, scaleFactor);
  }

  public static Matrix getScale(float xScale, float yScale) {
    return new Matrix(xScale, 0, 0, yScale, 0, 0);
  }

  /**
   * Multiply two matrices. Note that if v is a vector, and T1 and T2 are
   * matrices, then to construct a matrix T3 such that T3[v] = T2[T1[v]], then
   * T3 = T2 * T1 (not T1 * T2). In other words, the order matters; matrix
   * multiplication is not commutative.
   *
   * @param m1
   * @param m2
   * @param dest
   *          where to store result; if null, constructs new one; can also be
   *          either m1 or m2
   * @return result
   */
  public static Matrix multiply(Matrix m1, Matrix m2) {

    float na = m1.a * m2.a + m1.c * m2.b + m1.tx * 0;
    float nc = m1.a * m2.c + m1.c * m2.d + m1.tx * 0;
    float ntx = m1.a * m2.tx + m1.c * m2.ty + m1.tx * 1;

    float nb = m1.b * m2.a + m1.d * m2.b + m1.ty * 0;
    float nd = m1.b * m2.c + m1.d * m2.d + m1.ty * 0;
    float nty = m1.b * m2.tx + m1.d * m2.ty + m1.ty * 1;

    return new Matrix(na, nb, nc, nd, ntx, nty);
  }

  public Matrix invert() {

    MyMath.testForZero(d);
    float e = 1 / d;
    float h = c * e;
    float g = b * h;
    float ag = a - g;
    MyMath.testForZero(ag);
    float f = 1 / ag;
    float j = h * ty - tx;

    float na = f;
    float nc = -h * f;
    float ntx = f * j;
    float nb = -b * e * f;
    float nd = e * (1 + g * f);
    float nty = e * (-ty - b * f * j);

    return new Matrix(na, nb, nc, nd, ntx, nty);
  }

  public static Matrix getFlipHorizontally(float x0, float x1) {
    return new Matrix(-1, 0, 0, 1, x0 + x1, 0);
  }

  public static Matrix getFlipVertically(float y0, float y1) {
    return new Matrix(1, 0, 0, -1, 0, y0 + y1);
  }

  public float horizontalScaleFactor() {
    return MyMath.sqrtf(a * a + c * c);
  }

  public float verticalScaleFactor() {
    return MyMath.sqrtf(b * b + d * d);
  }

  public AffineTransform toAffineTransform() {
    return new AffineTransform(a, b, c, d, tx, ty);
  }

  public static Matrix from(AffineTransform t) {
    return new Matrix((float) t.getScaleX(), (float) t.getShearY(), (float) t.getShearX(),
        (float) t.getScaleY(), (float) t.getTranslateX(), (float) t.getTranslateY());
  }

  public static Matrix from(Graphics2D graphics) {
    return from(graphics.getTransform());
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || !(object instanceof Matrix))
      return false;
    Matrix other = (Matrix) object;
    return (a == other.a) && (b == other.b) && (c == other.c) && (d == other.d) && (tx == other.tx)
        && (ty == other.ty);
  }

  @Override
  public int hashCode() {
    if (mHashCode == 0)
      mHashCode = (int) (0//
          + a * 37 //
          + b * 37 * 37 //
          + c * 37 * 37 * 37 //
          + d * 37 * 37 * 37 * 37 //
          + tx * 37 * 37 * 37 * 37 * 37//
          + ty * 37 * 37 * 37 * 37 * 37 * 37);
    return mHashCode;
  }

  private int mHashCode;
}
