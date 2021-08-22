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

import js.json.*;

public final class FPoint3 {

  public static final FPoint3 ZERO = new FPoint3();

  private FPoint3() {
    x = 0;
    y = 0;
    z = 0;
  }

  public FPoint3(float x, float y, float z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  public FPoint3(double x, double y, double z) {
    this.x = (float) x;
    this.y = (float) y;
    this.z = (float) z;
  }

  public FPoint3(float[] floats) {
    this(floats[0], floats[1], floats[2]);
    if (floats.length != 3)
      throw die("unexpected length");
  }

  public FPoint3(int[] integers) {
    this(integers[0], integers[1], integers[2]);
    if (integers.length != 3)
      throw die("unexpected length");
  }

  public FPoint3 normalize() {
    float dist = magnitude();
    if (dist < 1e-3)
      throw die("vector is near zero");
    return scaledBy(1 / dist);
  }

  public float squaredMagnitude() {
    return x * x + y * y + z * z;
  }

  public float magnitude() {
    return MyMath.sqrtf(squaredMagnitude());
  }

  public FPoint3 scaledBy(float scaleFactor) {
    return new FPoint3(x * scaleFactor, y * scaleFactor, z * scaleFactor);
  }

  public static FPoint3 sum(FPoint3 a, FPoint3 b) {
    return new FPoint3(a.x + b.x, a.y + b.y, a.z + b.z);
  }

  public static FPoint3 difference(FPoint3 a, FPoint3 b) {
    return new FPoint3(a.x - b.x, a.y - b.y, a.z - b.z);
  }

  /**
   * Calculate cross product of two 3-vectors
   */
  public static FPoint3 crossProduct(FPoint3 a, FPoint3 b) {
    float x = a.y * b.z - a.z * b.x;
    float y = a.z * b.x - a.x * b.z;
    float z = a.x * b.y - a.y * b.x;
    return new FPoint3(x, y, z);
  }

  /**
   * Calculate squared distance between points
   */
  public static float squaredDistance(FPoint3 pt0, FPoint3 pt1) {
    return FPoint3.difference(pt0, pt1).squaredMagnitude();
  }

  /**
   * Calculate squared distance of point from a segment of unit length
   */
  public static float squaredDistance(FPoint3 pt0, FPoint3 pt1, FPoint3 query) {
    FPoint3 cp = crossProduct(FPoint3.difference(pt1, pt0), FPoint3.difference(pt0, query));
    return cp.squaredMagnitude();
  }

  /**
   * Calculate squared distance of point c from segment a..b
   */
  public static float squaredDistanceFromSeg(FPoint3 a, FPoint3 b, FPoint3 c) {

    // Determine parameter t of projection of query point to line

    FPoint3 bMa = FPoint3.difference(b, a);
    FPoint3 aMc = FPoint3.difference(a, c);

    float t = -aMc.x * bMa.x - aMc.y * bMa.y - aMc.z * bMa.z;
    if (t < 0)
      return aMc.squaredMagnitude();

    float denom = bMa.squaredMagnitude();
    if (t > denom)
      return FPoint3.difference(c, b).squaredMagnitude();

    FPoint3 cp = FPoint3.crossProduct(bMa, aMc);
    float numer = cp.squaredMagnitude();
    return numer / denom;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(toStr(x));
    sb.append(' ');
    sb.append(toStr(y));
    sb.append(' ');
    sb.append(toStr(z));
    return sb.toString();
  }

  public String toStringAsInts() {
    StringBuilder sb = new StringBuilder();
    sb.append(toStr((int) x, 4));
    sb.append(' ');
    sb.append(toStr((int) y, 4));
    sb.append(' ');
    sb.append(toStr((int) z, 4));
    return sb.toString();
  }

  public JSList toJson() {
    JSList a = list();
    a.add(x);
    a.add(y);
    a.add(z);
    return a;
  }

  private static JSList toJson(List<FPoint3> points) {
    JSList a = list();
    for (FPoint3 pt : points) {
      a.add(pt.x);
      a.add(pt.y);
      a.add(pt.z);
    }
    return a;
  }

  public void put(JSMap map, String key) {
    map.put(key, toJson());
  }

  /**
   * Encode a list of points to a JSON map
   */
  public static void put(List<FPoint3> points, JSMap map, String key) {
    map.put(key, toJson(points));
  }

  public int ix() {
    return Math.round(x);
  }

  public int iy() {
    return Math.round(y);
  }

  public int iz() {
    return Math.round(z);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    FPoint3 FPoint3 = (FPoint3) o;
    return Float.compare(FPoint3.x, x) == 0 && Float.compare(FPoint3.y, y) == 0
        && Float.compare(FPoint3.z, z) == 0;
  }

  @Override
  public int hashCode() {
    return Objects.hash(x, y, z);
  }

  public final float x;
  public final float y;
  public final float z;

  public static FPoint3 interpolate(FPoint3 p1, FPoint3 p2, float t) {
    return new FPoint3(p1.x + t * (p2.x - p1.x), p1.y + t * (p2.y - p1.y), p1.z + t * (p2.z - p1.z));
  }

  public static FPoint3 midPoint(FPoint3 p1, FPoint3 p2) {
    return interpolate(p1, p2, .5f);
  }

  private static final FPoint3[] ZERO_LENGTH_ARRAY = new FPoint3[0];

  public static FPoint3[] toArray(Collection<FPoint3> collection) {
    return collection.toArray(ZERO_LENGTH_ARRAY);
  }

}
