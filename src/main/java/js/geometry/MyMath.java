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

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static js.base.Tools.*;

public final class MyMath {

  public static int myMod(int value, int divisor) {
    if (divisor <= 0)
      throw new IllegalArgumentException();
    int k = value % divisor;
    if (value < 0) {
      if (k != 0)
        k+=divisor;
    }
    return k;
  }

  public static float myMod(float value, float divisor) {
    if (divisor <= 0)
      throw new IllegalArgumentException();
    float scaledValue = value / divisor;
    scaledValue -= Math.floor(scaledValue);
    return scaledValue * divisor;
  }

  public static <T> T getMod(List<T> list, int index) {
    return list.get(myMod(index, list.size()));
  }

  public static void testForZero(float value) {
    testForZero(value, 1e-8f);
  }

  public static void testForZero(float value, float epsilon) {
    if (Math.abs(value) <= epsilon)
      throw badArg("Value is very near zero:", value, "Epsilon:", epsilon);
  }

  public static float sqrtf(float f) {
    return (float) Math.sqrt(f);
  }

  public static float magnitudeOfRay(float x, float y) {
    return sqrtf(squaredMagnitudeOfRay(x, y));
  }

  public static float squaredMagnitudeOfRay(float x, float y) {
    return (x * x) + (y * y);
  }

  public static int clamp(int value, int min, int max) {
    if (value < min)
      value = min;
    else if (value > max)
      value = max;
    return value;
  }

  public static float clamp(float value, float min, float max) {
    if (value < min)
      value = min;
    else if (value > max)
      value = max;
    return value;
  }

  public static double clamp(double value, double min, double max) {
    if (value < min)
      value = min;
    else if (value > max)
      value = max;
    return value;
  }

  public static float squaredDistanceBetween(FPoint s1, FPoint s2) {
    return squaredMagnitudeOfRay(s2.x - s1.x, s2.y - s1.y);
  }

  public static float distanceBetween(FPoint s1, FPoint s2) {
    return sqrtf(squaredMagnitudeOfRay(s2.x - s1.x, s2.y - s1.y));
  }

  public static float distanceBetween(IPoint s1, IPoint s2) {
    return sqrtf(squaredDistanceBetween(s1, s2));
  }

  public static float squaredDistanceBetween(IPoint s1, IPoint s2) {
    return squaredMagnitudeOfRay(s2.x - s1.x, s2.y - s1.y);
  }

  public static float pointUnitLineSignedDistance(FPoint pt, FPoint s1, FPoint s2) {
    float sx = s2.x - s1.x;
    float sy = s2.y - s1.y;
    float pt_x = pt.x - s1.x;
    float pt_y = pt.y - s1.y;
    return -sy * pt_x + sx * pt_y;
  }

  /**
   * Determine distance of a point from a line
   * 
   * @param pt
   *          FPoint2
   * @param e0
   *          one point on line
   * @param e1
   *          second point on line
   * @param closestPtOrNull
   *          if not null, closest point on line is stored here; can be the same
   *          as one of the input points
   * @return distance
   */
  public static float ptDistanceToLine(FPoint pt, FPoint e0, FPoint e1, FPoint[] closestPtOrNull) {

    /*
     * Let A = pt - l0 B = l1 - l0
     * 
     * then
     * 
     * |A x B| = |A||B| sin t
     * 
     * and the distance is |AxB| / |B|
     * 
     * 
     * The closest point is
     * 
     * l0 + (|A| cos t) / |B|
     */
    float bLength = distanceBetween(e0, e1);
    float dist;
    if (bLength == 0) {
      dist = distanceBetween(pt, e0);
      if (closestPtOrNull != null)
        closestPtOrNull[0] = e0;
    } else {
      float ax = pt.x - e0.x;
      float ay = pt.y - e0.y;
      float bx = e1.x - e0.x;
      float by = e1.y - e0.y;

      float crossProd = bx * ay - by * ax;

      dist = Math.abs(crossProd / bLength);

      if (closestPtOrNull != null) {
        float scalarProd = ax * bx + ay * by;
        float t = scalarProd / (bLength * bLength);
        closestPtOrNull[0] = new FPoint(e0.x + t * bx, e0.y + t * by);
      }
    }
    return dist;
  }

  /**
   * Calculate the parameter for a point on a line
   * 
   * @param pt
   *          FPoint2, assumed to be on line
   * @param s0
   *          start point of line segment (t = 0.0)
   * @param s1
   *          end point of line segment (t = 1.0)
   * @return t value associated with pt
   */
  public static float positionOnSegment(FPoint pt, FPoint s0, FPoint s1) {

    float sx = s1.x - s0.x;
    float sy = s1.y - s0.y;

    float t = 0;

    float dotProd = (pt.x - s0.x) * sx + (pt.y - s0.y) * sy;
    if (dotProd != 0)
      t = dotProd / (sx * sx + sy * sy);

    return t;
  }

  /**
   * Determine distance of point from segment
   * 
   * @param pt
   *          FPoint2
   * @param l0
   *          FPoint2
   * @param l1
   *          FPoint2
   * @param ptOnSeg
   *          if not null, closest point on segment to point is stored here
   * @return float
   */
  public static float ptDistanceToSegment(FPoint pt, FPoint l0, FPoint l1, FPoint[] ptOnSegOrNull) {

    float dist = 0;
    // calculate parameter for position on segment
    float t = positionOnSegment(pt, l0, l1);

    FPoint cpt = null;
    if (t < 0) {
      cpt = l0;
      dist = distanceBetween(pt, cpt);
      if (ptOnSegOrNull != null)
        ptOnSegOrNull[0] = cpt;
    } else if (t > 1) {
      cpt = l1;
      dist = distanceBetween(pt, cpt);
      if (ptOnSegOrNull != null)
        ptOnSegOrNull[0] = cpt;
    } else {
      dist = ptDistanceToLine(pt, l0, l1, ptOnSegOrNull);
    }
    return dist;
  }

  public static FPoint pointOnCircle(FPoint origin, float angle, float radius) {
    return pointOnCircle(origin.x, origin.y, angle, radius);
  }

  public static FPoint pointOnCircle(float originX, float originY, float angle, float radius) {
    return new FPoint(originX + radius * (float) Math.cos(angle), originY + radius * (float) Math.sin(angle));
  }

  public static FPoint linesIntersection(FPoint p1, FPoint p2, FPoint q1, FPoint q2, float[] iParam) {
    return linesIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q2.x, q2.y, iParam);
  }

  /**
   * @param param
   *          if not null, parameters of intersection point returned here
   * @return FPoint2 if they properly intersect (if parallel or coincident,
   *         returns null)
   */
  public static FPoint linesIntersection(float p1x, float p1y, float p2x, float p2y, float q1x, float q1y,
      float q2x, float q2y, float[] param) {

    final float EPS = 1e-8f;

    float dx = p2x - p1x;
    float dy = p2y - p1y;

    float denom = (q2y - q1y) * dx - (q2x - q1x) * dy;
    float numer1 = (q2x - q1x) * (p1y - q1y) - (q2y - q1y) * (p1x - q1x);
    if (Math.abs(denom) < EPS)
      return null;
    float ua = numer1 / denom;

    float numer2 = dx * (p1y - q1y) - dy * (p1x - q1x);
    float ub = numer2 / denom;

    if (param != null) {
      param[0] = ua;
      param[1] = ub;
    }

    return new FPoint(p1x + ua * dx, p1y + ua * dy);
  }

  public static FPoint lineSegmentIntersection(FPoint p1, FPoint p2, FPoint q1, FPoint q2, float[] iParam) {
    return lineSegmentIntersection(p1.x, p1.y, p2.x, p2.y, q1.x, q1.y, q2.x, q2.y, iParam);
  }

  /**
   * Determine intersection point (if one exists) between two line segments
   * 
   * @param p1x
   * @param p1y
   * @param p2x
   * @param p2y
   *          endpoints of first segment
   * @param q1x
   * @param q1y
   * @param q2x
   * @param q2y
   *          endpoints of second segment
   * @param param
   *          if not null, parameters of intersection point returned here
   * @return FPoint2 if they properly intersect (if parallel or coincident, or
   *         if they intersect outside of either segment range, returns null)
   */
  public static FPoint lineSegmentIntersection(float p1x, float p1y, float p2x, float p2y, float q1x,
      float q1y, float q2x, float q2y, float[] param) {

    final float EPS = 1e-5f;

    FPoint out = null;
    do {

      float denom = (q2y - q1y) * (p2x - p1x) - (q2x - q1x) * (p2y - p1y);
      float numer1 = (q2x - q1x) * (p1y - q1y) - (q2y - q1y) * (p1x - q1x);
      //double numer2 = (p2x - p1x)*(p1y - q1y) - (p2y -p1y)*(p1x-q1x);
      if (Math.abs(denom) < EPS) {
        break;
      }

      float ua = numer1 / denom;

      float numer2 = (p2x - p1x) * (p1y - q1y) - (p2y - p1y) * (p1x - q1x);

      float ub = numer2 / denom;
      if (param != null) {
        param[0] = ua;
        param[1] = ub;
      }
      if (ua < -EPS || ua > 1 + EPS) {
        break;
      }
      if (ub < -EPS || ub > 1 + EPS) {
        break;
      }

      out = new FPoint(p1x + ua * (p2x - p1x), p1y + ua * (p2y - p1y));
    } while (false);

    return out;
  }

  /**
   * Calculate point of intersection of line segment with horizontal line
   * 
   * @param pt1
   * @param pt2
   * @param yLine
   * @param parameter
   *          if not null, and intersection point found, parameter of
   *          intersection returned here
   * @return point of intersection, or null
   */
  public static FPoint segHorzLineIntersection(FPoint pt1, FPoint pt2, float yLine, float[] parameter) {
    FPoint ipt = null;

    float denom = pt2.y - pt1.y;
    testForZero(denom);

    float numer = yLine - pt1.y;
    float t = numer / denom;

    if (!(t < 0 || t > 1)) {
      if (parameter != null)
        parameter[0] = t;

      ipt = new FPoint(pt1.x + (pt2.x - pt1.x) * t, pt1.y + denom * t);
    }
    return ipt;
  }

  public static float interpolateBetweenScalars(float v1, float v2, float parameter) {
    return (v1 * (1 - parameter)) + v2 * parameter;
  }

  public static float normalizeAngle(float angleRadians) {
    final float CIRCLE = 2 * PI;
    float result = myMod(angleRadians, CIRCLE);
    if (result >= PI)
      result -= CIRCLE;
    return result;
  }

  public static float polarAngle(FPoint ray) {
    return polarAngle(ray.x, ray.y);
  }

  public static float polarAngle(FPoint pt0, FPoint pt1) {
    return polarAngle(pt1.x - pt0.x, pt1.y - pt0.y);
  }

  public static float polarAngle(IPoint pt0, IPoint pt1) {
    return polarAngle(pt1.x - pt0.x, pt1.y - pt0.y);
  }

  public static float polarAngle(float x, float y) {
    float max = Math.max(Math.abs(x), Math.abs(y));
    if (max <= 1e-8f)
      throw badArg("Point is too close to origin:", x, y);
    return (float) Math.atan2(y, x);
  }

  public static final float PSEUDO_ANGLE_RANGE = 8;
  public static final float PSEUDO_ANGLE_RANGE_12 = (PSEUDO_ANGLE_RANGE * .5f);
  public static final float PSEUDO_ANGLE_RANGE_14 = (PSEUDO_ANGLE_RANGE * .25f);
  public static final float PSEUDO_ANGLE_RANGE_34 = (PSEUDO_ANGLE_RANGE * .75f);

  public static float pseudoPolarAngle(float x, float y) {
    // For consistency, always insist that y is nonnegative
    boolean negateFlag = (y <= 0);
    if (negateFlag)
      y = -y;

    float ret;
    if (y > Math.abs(x)) {
      float rat = x / y;
      ret = PSEUDO_ANGLE_RANGE_14 - rat;
    } else {
      testForZero(x);
      float rat = y / x;
      if (x < 0) {
        ret = PSEUDO_ANGLE_RANGE_12 + rat;
      } else {
        ret = rat;
      }
    }
    if (negateFlag)
      ret = -ret;

    return ret;
  }

  public static float sin(float angle) {
    return (float) Math.sin(angle);
  }

  public static float cos(float angle) {
    return (float) Math.cos(angle);
  }

  public static float PI = (float) Math.PI;

  /**
   * Conversion factor to convert degrees to radians
   */
  public static final float M_DEG = PI / 180.0f;

  public static Random random() {
    return ThreadLocalRandom.current();
  }

  public static Random orDefault(Random random) {
    if (random == null)
      random = random();
    return random;
  }

  public static float random(Random random, float min, float max) {
    random = orDefault(random);
    return min + random.nextFloat() * (max - min);
  }

  public static int random(Random random, int min, int max) {
    random = orDefault(random);
    return min + random.nextInt(max - min);
  }

  public static float biasedRandomNumber(Random random) {
    return biasedRandomNumber(random, POWER_FACTOR_DEFAULT);
  }

  public static final float POWER_FACTOR_DEFAULT = 1.8f;

  /**
   * Choose a random number 0..1, but biased towards zero (or one)
   * 
   * 
   * See https://gamedev.stackexchange.com/questions/116832
   * 
   * and particularly https://i.stack.imgur.com/10kPZ.png
   * 
   * @param powerFactor
   *          if > 1, bias is towards zero; if < 1, bias is towards one
   */
  public static float biasedRandomNumber(Random random, float powerFactor) {
    float t = (float) Math.pow(random.nextFloat(), powerFactor);
    return t;
  }

  public static <T> T randomElement(Random random, List<T> list) {
    random = orDefault(random);
    checkArgument(list.size() > 0);
    return list.get(random.nextInt(list.size()));
  }

  public static <T> List<T> permute(List<T> list, Random randomOrNull) {
    Random random = orDefault(randomOrNull);
    int size = list.size();
    for (int i = size - 1; i >= 1; i--) {
      int j = random.nextInt(i + 1);
      T tmp = list.get(i);
      list.set(i, list.get(j));
      list.set(j, tmp);
    }
    return list;
  }

  public static int[] permutation(int size, Random randomOrNull) {
    Random random = orDefault(randomOrNull);
    int[] p = new int[size];
    for (int i = 0; i < size; i++)
      p[i] = i;
    for (int i = size - 1; i >= 1; i--) {
      int j = random.nextInt(i + 1);
      int tmp = p[i];
      p[i] = p[j];
      p[j] = tmp;
    }
    return p;
  }

  public static int signum(long value) {
    if (value == 0)
      return 0;
    return (value < 0) ? -1 : 1;
  }

  /**
   * Return 1,0,-1 if pt is left, on, or right of directed line ln0..ln1
   */
  public static float sideOfLine(FPoint ln0, FPoint ln1, FPoint pt) {
    float area = (ln1.x - ln0.x) * (pt.y - ln0.y) - (pt.x - ln0.x) * (ln1.y - ln0.y);
    return Math.signum(area);
  }

  /**
   * Return 1,0,-1 if pt is left, on, or right of directed line ln0..ln1
   */
  public static int sideOfLine(IPoint ln0, IPoint ln1, IPoint pt) {
    long area = (ln1.x - ln0.x) * (long) (pt.y - ln0.y) - (pt.x - ln0.x) * (long) (ln1.y - ln0.y);
    return signum(area);
  }

  private static float overlap(float x1, float w1, float x2, float w2) {
    float left = x1 > x2 ? x1 : x2;
    float e1 = x1 + w1;
    float e2 = x2 + w2;
    float right = e1 > e2 ? e2 : e1;
    return right - left;
  }

  public static float areaOfIntersection(float ax, float ay, float aw, float ah, float bx, float by, float bw,
      float bh) {
    float w = overlap(ax, aw, bx, bw);
    float h = overlap(ay, ah, by, bh);
    if (w < 0 || h < 0)
      return 0;
    return w * h;
  }

  public static float areaOfUnion(float ax, float ay, float aw, float ah, float bx, float by, float bw,
      float bh) {
    float i = areaOfIntersection(ax, ay, aw, ah, bx, by, bw, bh);
    return aw * ah + bw * bh - i;
  }

  public static float intersectionOverUnion(float ax, float ay, float aw, float ah, float bx, float by,
      float bw, float bh) {
    return areaOfIntersection(ax, ay, aw, ah, bx, by, bw, bh) / areaOfUnion(ax, ay, aw, ah, bx, by, bw, bh);
  }

  /**
   * Converts a percentage (0..100) to a parameter (0..1)
   */
  public static float percentToParameter(float percent) {
    if (percent < 0 || percent > 100)
      throw new IllegalArgumentException("bad percent: " + percent);
    return percent / 100f;
  }

  /**
   * Converts a parameter (0...1) to an integer percentage
   */
  public static int parameterToPercentage(float parameter) {
    if (parameter < 0 || parameter > 100)
      throw new IllegalArgumentException("bad parameter: " + parameter);
    return Math.round(parameter * 100);
  }

  /**
   * Calculate a proportion (numer / denom). If both are zero, returns zero
   */
  public static float calculateProportion(float numer, float denom) {
    if (denom < 0 || numer > denom)
      throw new IllegalArgumentException("bad proportion arguments: " + numer + " / " + denom);
    float result = 0;
    if (denom != 0)
      result = numer / denom;
    return result;
  }

  /**
   * Calculate a proportion (numer / denom); fail if denominator is very near
   * zero
   */
  public static float calculateProportionStrict(float numer, float denom) {
    if (denom < 1e-05f)
      throw new IllegalArgumentException("bad proportion arguments: " + numer + " / " + denom);
    return calculateProportion(numer, denom);
  }

  public static int logitToPercentConfidence(float logit) {
    int index = java.util.Arrays.binarySearch(sLogitToPercentBisectors, logit);
    if (index < 0)
      index = -(index + 1);
    return index;
  }

  public static float percentConfidenceToLogit(int percent) {
    return sPercentConfidenceToLogit[percent];
  }

  private static float[] sLogitToPercentBisectors = { -5.2933f, -4.1846f, -3.6636f, -3.3168f, -3.0550f,
      -2.8439f, -2.6662f, -2.5123f, -2.3763f, -2.2541f, -2.1429f, -2.0407f, -1.9459f, -1.8575f, -1.7744f,
      -1.6959f, -1.6215f, -1.5506f, -1.4828f, -1.4178f, -1.3553f, -1.2950f, -1.2368f, -1.1803f, -1.1255f,
      -1.0721f, -1.0201f, -0.9694f, -0.9198f, -0.8712f, -0.8236f, -0.7768f, -0.7309f, -0.6857f, -0.6411f,
      -0.5971f, -0.5537f, -0.5108f, -0.4684f, -0.4263f, -0.3847f, -0.3433f, -0.3023f, -0.2615f, -0.2209f,
      -0.1805f, -0.1402f, -0.1001f, -0.0600f, -0.0200f, 0.0200f, 0.0600f, 0.1001f, 0.1402f, 0.1805f, 0.2209f,
      0.2615f, 0.3023f, 0.3433f, 0.3847f, 0.4263f, 0.4684f, 0.5108f, 0.5537f, 0.5971f, 0.6411f, 0.6857f,
      0.7309f, 0.7768f, 0.8236f, 0.8712f, 0.9198f, 0.9694f, 1.0201f, 1.0721f, 1.1255f, 1.1803f, 1.2368f,
      1.2950f, 1.3553f, 1.4178f, 1.4828f, 1.5506f, 1.6215f, 1.6959f, 1.7744f, 1.8575f, 1.9459f, 2.0407f,
      2.1429f, 2.2541f, 2.3763f, 2.5123f, 2.6662f, 2.8439f, 3.0550f, 3.3168f, 3.6636f, 4.1846f, 5.2933f, };

  private static float[] sPercentConfidenceToLogit = { -5.3000f, -4.5951f, -3.8918f, -3.4761f, -3.1781f,
      -2.9444f, -2.7515f, -2.5867f, -2.4423f, -2.3136f, -2.1972f, -2.0907f, -1.9924f, -1.9010f, -1.8153f,
      -1.7346f, -1.6582f, -1.5856f, -1.5163f, -1.4500f, -1.3863f, -1.3249f, -1.2657f, -1.2083f, -1.1527f,
      -1.0986f, -1.0460f, -0.9946f, -0.9445f, -0.8954f, -0.8473f, -0.8001f, -0.7538f, -0.7082f, -0.6633f,
      -0.6190f, -0.5754f, -0.5322f, -0.4895f, -0.4473f, -0.4055f, -0.3640f, -0.3228f, -0.2819f, -0.2412f,
      -0.2007f, -0.1603f, -0.1201f, -0.0800f, -0.0400f, 0.0000f, 0.0400f, 0.0800f, 0.1201f, 0.1603f, 0.2007f,
      0.2412f, 0.2819f, 0.3228f, 0.3640f, 0.4055f, 0.4473f, 0.4895f, 0.5322f, 0.5754f, 0.6190f, 0.6633f,
      0.7082f, 0.7538f, 0.8001f, 0.8473f, 0.8954f, 0.9445f, 0.9946f, 1.0460f, 1.0986f, 1.1527f, 1.2083f,
      1.2657f, 1.3249f, 1.3863f, 1.4500f, 1.5163f, 1.5856f, 1.6582f, 1.7346f, 1.8153f, 1.9010f, 1.9924f,
      2.0907f, 2.1972f, 2.3136f, 2.4423f, 2.5867f, 2.7515f, 2.9444f, 3.1781f, 3.4761f, 3.8918f, 4.5951f,
      5.3000f, };

}
