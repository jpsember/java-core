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
package js.base;

import static js.base.Tools.*;

/**
 * Class for (immutable) pairs of objects
 */
public final class Pair<T1, T2> {

  // Use the Tools.pair() method to construct pairs.
  Pair(T1 first, T2 second) {
    this.first = first;
    this.second = second;
  }

  @Override
  public String toString() {
    BasePrinter sb = new BasePrinter();
    sb.append("Pair(");
    String s1 = first.toString();
    String s2 = second.toString();
    if (s1.contains("\n") || s2.contains("\n")) {
      sb.pr(INDENT, s1, OUTDENT, ",", INDENT, s2, OUTDENT, ")", CR);
    } else {
      sb.pr(first, ",", second, ")");
    }
    return sb.toString();
  }

  @Override
  public int hashCode() {
    return first.hashCode() ^ second.hashCode();
  }

  @Override
  public boolean equals(Object object) {
    if (object == null || !(object instanceof Pair))
      return false;
    Pair other = (Pair) object;
    return this.first.equals(other.first) && this.second.equals(other.second);
  }

  public final T1 first;
  public final T2 second;

}
