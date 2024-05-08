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

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static js.base.Tools.*;

/**
 * Reports problems that are sporadic, and can be reported in various ways
 */
public class ProblemReporter extends BaseObject {

  // ------------------------------------------------------------------
  // Construction
  // ------------------------------------------------------------------

  /**
   * Construct a problem reporter that occasionally sends a message to the
   * system log
   */
  public static ProblemReporter infrequentLogger(float periodSeconds, int maxReports) {
    ProblemReporter p = new ProblemReporter();
    p.withReporter(new InfrequentLoggerReporter(periodSeconds, maxReports));
    return p;
  }

  public static ProblemReporter infrequentLogger(float periodSeconds) {
    return infrequentLogger(periodSeconds, 100);
  }

  public static ProblemReporter infrequentLogger() {
    return infrequentLogger(5f, 100);
  }

  public static ProblemReporter growthReporter() {
    return infrequentLogger(0, -1);
  }

  @Override
  protected String supplyName() {
    if (mName != null)
      return mName;
    mName = super.supplyName();
    return mName;
  }

  private void illegalState(String message) {
    log(message);
    throw new IllegalStateException(name() + ": " + message);
  }

  /**
   * Put reporter into operation state
   */
  private void prepare() {
    if (!mPrepared) {
      if (mReporters.isEmpty())
        illegalState("No reporters registered");
      mPrepared = true;
    }
  }

  /**
   * Ensure reporter is still in preparation stage
   */
  private void mutable() {
    if (mPrepared)
      illegalState("ProblemReporter already prepared");
  }

  // ------------------------------------------------------------------
  // Preparation stage
  // ------------------------------------------------------------------

  public ProblemReporter withName(Object... messages) {
    mutable();
    if (mName != null)
      illegalState("name already defined");
    mName = "{" + BasePrinter.toString(messages) + "}";
    return this;
  }

  public ProblemReporter withReporter(Consumer<ProblemReporter> reporter) {
    mutable();
    mReporters.add(reporter);
    return this;
  }

  public ProblemReporter withThreadSafe() {
    mutable();
    mThreadSafe = true;
    return this;
  }

  public ProblemReporter withOptionalStackTraces() {
    mutable();
    mStackTracesOptional = true;
    return this;
  }

  // ------------------------------------------------------------------
  // Operation stage
  // ------------------------------------------------------------------

  /**
   * Set throwable argument for next update
   */
  public ProblemReporter throwable(Throwable throwable) {
    prepare();
    mThrowable = throwable;
    return this;
  }

  /**
   * Issue a report
   */
  public void report(Object... messages) {
    prepare();
    if (mThreadSafe) {
      synchronized (this) {
        mArguments = messages;
        reportProblem();
      }
    } else {
      mArguments = messages;
      reportProblem();
    }
  }

  /**
   * Get description of last problem
   */
  public String getProblemDescription() {
    prepare();
    if (nullOrEmpty(mProblemMessage))
      return "<no description given>";
    return mProblemMessage;
  }

  /**
   * Get stack trace of last problem, if there was one
   */
  public Throwable optStackTrace() {
    prepare();
    return mProblemStackTrace;
  }

  /**
   * Get stack trace of last problem, creating one if necessary
   */
  public Throwable getProblemStackTrace() {
    prepare();
    if (mProblemStackTrace == null)
      mProblemStackTrace = new Throwable();
    return optStackTrace();
  }

  public boolean requireStackTrace() {
    return !mStackTracesOptional;
  }

  private void auxReportGrowth(String message, int currentValue, int maxStepSize) {
    if (mPreviousMaxReportedValue == 0) {
      mPreviousMaxReportedValue = currentValue;
    }
    if (currentValue >= mPreviousMaxReportedValue + maxStepSize) {
      report("Count has grown from", mPreviousMaxReportedValue, "to", currentValue, ";", message);
      mPreviousMaxReportedValue = currentValue;
    }
  }

  private int mPreviousMaxReportedValue;

  private static final Object[] DEFAULT_MESSAGES = new Object[0];

  private void reportProblem() {
    prepare();

    Object[] messages = mArguments;
    if (messages == null)
      messages = DEFAULT_MESSAGES;

    if (mThrowable != null) {
      String message = mThrowable.getMessage();
      message = ifNullOrEmpty(message, "<none>");
      List<Object> args = arrayList();
      for (Object x : messages)
        args.add(x);
      args.add(INDENT);
      args.add("(" + mThrowable.getClass().getSimpleName() + ",");
      args.add(message + ")");
      messages = args.toArray();
    }

    mProblemMessage = BasePrinter.toString(messages);
    mProblemStackTrace = mThrowable;

    // Discard arguments now that they've been used
    mArguments = null;
    mThrowable = null;

    for (Consumer<ProblemReporter> reporter : mReporters) {
      try {
        reporter.accept(this);
      } catch (Throwable t) {
        pr(this, "trouble reporting problem! Reporter:", reporter);
        pr(t);
      }
    }
  }

  private static Map<String, ProblemReporter> sGrowthReporters = hashMap();

  public static void reportGrowth(String message, int currentValue, int maxStepSize) {
    ProblemReporter rep = sGrowthReporters.get(message);
    if (rep == null) {
      rep = ProblemReporter.growthReporter();
      sGrowthReporters.put(message, rep);
    }
    rep.auxReportGrowth(message, currentValue, maxStepSize);
  }

  public static void sharedReporter(Object owner, Object... messages) {
    Object key;
    if (owner instanceof String)
      key = owner;
    else
      key = owner.getClass();
    ProblemReporter reporter = sSharedReporterMap.get(key);
    if (reporter == null) {
      reporter = infrequentLogger();
      reporter.withThreadSafe();
      sSharedReporterMap.put(key, reporter);
    }
    reporter.report(messages);
  }

  private static Map<Object, ProblemReporter> sSharedReporterMap = concurrentHashMap();

  private final List<Consumer<ProblemReporter>> mReporters = arrayList();
  private String mName;
  private boolean mPrepared;
  private boolean mThreadSafe;
  private boolean mStackTracesOptional;

  // Arguments for issuing report
  //
  private Throwable mThrowable;
  private Object[] mArguments;

  // Details of last reported problem
  //
  private String mProblemMessage;
  private Throwable mProblemStackTrace;
}
