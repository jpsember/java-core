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

import js.json.JSMap;

/**
 * An Object subclass that supports conditional logging, naming, JSON conversion
 * and pretty printing
 */
public class BaseObject {

  // ------------------------------------------------------------------
  // Naming
  // ------------------------------------------------------------------

  public final String name() {
    if (!hasName())
      setName(nullToEmpty(supplyName()));
    return mName;
  }

  /**
   * Assign a name to the object if it doesn't already have one. Default
   * implementation uses class name. Called at most once per object instance
   * (excepting multithreading race conditions)
   */
  protected String supplyName() {
    return getClass().getSimpleName();
  }

  @SuppressWarnings("unchecked")
  public final <T extends BaseObject> T setName(String name) {
    mName = checkNotNull(name);
    return (T) this;
  }

  protected boolean hasName() {
    return mName != null;
  }

  private String mName;

  //------------------------------------------------------------------
  // Verbosity
  // ------------------------------------------------------------------

  public final boolean verbose() {
    return mVerbose;
  }

  public final void setVerbose() {
    setVerbose(true);
  }

  public final void alertVerbose() {
    setVerbose(true);
    alertWithSkip(1, name() + ": verbose is set");
  }

  public final void setVerbose(boolean state) {
    mVerbose = state;
  }

  /**
   * Log messages if verbosity is in effect
   */
  public final void log(Object... messageObjects) {
    if (verbose()) {
      String name = name();
      if (!name.isEmpty())
        pr(insertStringToFront("(" + name() + ":)", messageObjects));
      else
        pr(messageObjects);
    }
  }

  private boolean mVerbose;

  // ------------------------------------------------------------------
  // JSON, String conversion
  // ------------------------------------------------------------------

  @Override
  public final String toString() {
    return toJson().prettyPrint();
  }

  /**
   * Get JSON description. Default implementation returns a map with a single
   * key "" mapped to the class name
   */
  public JSMap toJson() {
    JSMap m = new JSMap();
    if (!name().isEmpty())
      m.put("", name());
    return m;
  }

}
