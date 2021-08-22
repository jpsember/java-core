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
 * Execution context (unit test vs production).
 */
public final class ExecutionContext extends BaseObject {

  public ExecutionContext() {
    mTimeManager = new TimeManager(this);
  }

  public TimeManager getTimeManager() {
    return mTimeManager;
  }

  /**
   * Set name (for unit test only)
   */
  public void setName(String unitTestName) {
    testOnlyAssert();
    mUnitTestName = unitTestName;
  }

  /**
   * If in a unit test, and the context is no longer valid, throws an
   * EndTestException; otherwise, returns true
   */
  public boolean stillActive() {
    if (mUnitTestNoLongerValid)
      throw new EndTestException();
    return true;
  }

  private boolean withinUnitTest() {
    return mUnitTestName != null;
  }

  @Override
  protected String supplyName() {
    return ifNullOrEmpty(mUnitTestName, "PRODUCTION");
  }

  // ------------------------------------------------------------------
  // Recording unit test exceptions for later reporting
  // ------------------------------------------------------------------

  /**
   * Store an exception for later reporting, if in unit test; otherwise, throw
   * immediately as RuntimeException
   */
  public void processException(Throwable t) {
    if (withinUnitTest()) {
      if (mException == null)
        mException = t;
    } else
      throw asRuntimeException(t);
  }

  public synchronized void shutdown() {
    if (mUnitTestNoLongerValid)
      return;
    mUnitTestNoLongerValid = true;
  }

  /**
   * Exception thrown if a unit test has ended
   */
  public static class EndTestException extends RuntimeException {
  }

  private final TimeManager mTimeManager;
  private Throwable mException;
  private boolean mUnitTestNoLongerValid;
  private String mUnitTestName;

}
