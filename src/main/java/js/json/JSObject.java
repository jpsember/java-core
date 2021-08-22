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
package js.json;

import java.io.IOException;
import java.util.function.BiConsumer;

import js.base.BasePrinter;

/**
 * Common interface supported by both JSMap and JSList
 */
public abstract class JSObject {

  /**
   * Print to string
   *
   * @param destination
   *          where to print to
   * @param optionalIndent
   *          if >= 0, performs pretty printing, with number of spaces to indent
   *          any lines following current one
   */
  public abstract void printTo(StringBuilder destination, int optionalIndent);

  public abstract boolean isMap();

  public abstract int size();

  public abstract boolean isEmpty();

  public final boolean nonEmpty() {
    return !isEmpty();
  }

  public JSMap asMap() {
    throw new UnsupportedOperationException();
  }

  public JSList asList() {
    throw new UnsupportedOperationException();
  }

  public abstract JSObject deepCopy();

  public abstract JSObject lock();

  public abstract boolean isLocked();

  public abstract JSObject clear();

  public abstract String prettyPrint();

  public final void printTo(BasePrinter destination) {
    destination.append(prettyPrint());
  }

  abstract void constructFrom(JSParser s);

  private void writeObject(java.io.ObjectOutputStream stream) throws IOException {
    stream.writeUTF(this.toString());
  }

  // Register BasePrinter handlers for json-related classes
  //
  static {
    BiConsumer<Object, BasePrinter> h = (x, p) -> ((JSObject) x).printTo(p);
    BasePrinter.registerClassHandler(JSList.class, h);
    BasePrinter.registerClassHandler(JSMap.class, h);
  }

}
