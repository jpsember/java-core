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

import java.util.function.Consumer;

public abstract class ProblemListener implements Consumer<ProblemReporter> {

  /**
   * Determine if sufficient time has elapsed since previous report
   */
  public boolean sufficientTimeElapsed() {
    boolean displayReport = false;
    long currentTime = DateTimeTools.getRealMs();
    do {
      if (mMaxReports >= 0 && mNetReportCount >= mMaxReports)
        break;
      if (mPeriodMS <= 0) {
        displayReport = true;
        break;
      }
      if (currentTime - mLastReportTime >= mPeriodMS) {
        displayReport = true;
      }

    } while (false);

    if (displayReport) {
      mLastReportTime = currentTime;
      mNetReportCount++;
      mPeriodMS = (long) (mPeriodMS * mPeriodGrowthFactor);
    }
    mGrossReportCount++;
    return displayReport;
  }

  public String getReportNumberSummary() {
    boolean issuedLastReport = (mMaxReports > 0 && mNetReportCount == mMaxReports);
    String result = String.format("(%d/%d%s)", mNetReportCount, mGrossReportCount,
        issuedLastReport ? ", suppressing further" : "");
    return result;
  }

  /**
   * Specify minimum period between reports. It omits reports if this amount of
   * time hasn't elapsed. The interval grows exponentially as the total number
   * of reports increases.
   */
  public ProblemListener withPeriod(float initialPeriodInSeconds) {
    mPeriodMS = (long) (initialPeriodInSeconds * 1000);
    return this;
  }

  public ProblemListener withPeriodGrowthFactor(float factor) {
    mPeriodGrowthFactor = factor;
    return this;
  }

  public ProblemListener withMaxReports(int maxReports) {
    mMaxReports = maxReports;
    return this;
  }

  private long mPeriodMS;
  private long mLastReportTime;
  private int mMaxReports = -1;
  private int mGrossReportCount;
  private int mNetReportCount;
  private float mPeriodGrowthFactor = 1.3f;

  public static final ProblemListener NULL_LISTENER = new ProblemListener() {

    @Override
    public boolean sufficientTimeElapsed() {
      return false;
    }

    @Override
    public void accept(ProblemReporter t) {
    }
  };
}
