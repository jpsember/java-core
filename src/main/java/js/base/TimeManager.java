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


import java.time.Duration;
import java.util.List;
import java.util.TreeSet;

import js.json.JSMap;

import static js.base.Tools.*;

public final class TimeManager extends BaseObject {

  public TimeManager() {
  }

  public TimeManager(ExecutionContext context) {
    mContext = context;
  }

  public static final int MODE_NORMAL = 0;
  public static final int MODE_SCRIPTED = 1;
  public static final int MODE_FAST = 2;

  public int mode() {
    return mMode;
  }

  private boolean setMode(int mode) {
    if (mode() == mode)
      return false;
    checkArgument(mode >= 0 && mode <= MODE_FAST);
    checkState(mode() == 0, "attempt to set mode to " + mode + ", but already " + mode());
    mMode = mode;
    return true;
  }

  private int mMode;

  // ------------------------------------------------------------------------------------
  // Sleeping

  public void sleep(long millis) {
    sleep(millis, null);
  }

  public void sleep(long millis, String reason) {
    switch (mode()) {
    default: //MODE_NORMAL
      try {
        Thread.sleep(millis);
      } catch (InterruptedException e) {
        throw asRuntimeException(e);
      }
      break;
    case MODE_SCRIPTED:
      sleepForAccelerated(millis, reason);
      break;
    case MODE_FAST:
      try {
        Thread.sleep(millis / FAST_SCALE_FACTOR);
      } catch (InterruptedException e) {
        throw asRuntimeException(e);
      }
      break;
    }
  }

  private static final int FAST_SCALE_FACTOR = 15;

  public Duration scaleDuration(Duration duration) {
    switch (mode()) {
    default: //MODE_NORMAL
    case MODE_SCRIPTED:
      return duration;
    case MODE_FAST:
      return duration.dividedBy(FAST_SCALE_FACTOR);
    }
  }

  public long scaleDuration(long duration) {
    switch (mode()) {
    default: //MODE_NORMAL
    case MODE_SCRIPTED:
      return duration;
    case MODE_FAST:
      return duration / FAST_SCALE_FACTOR;
    }
  }

  // ------------------------------------------------------------------
  // Getting time
  // ------------------------------------------------------------------

  // ------------------------------------------------------------------
  // Installing a separate clock
  // ------------------------------------------------------------------

  public long nowMs() {
    long timeMs;
    switch (mode()) {
    default:// MODE_NORMAL:
      timeMs = getRealMs();
      break;
    case MODE_SCRIPTED:
      timeMs = currentAcceleratedTime();
      break;
    case MODE_FAST: {
      long trueMs = getRealMs();
      timeMs = (trueMs - mFastTimeRealStart) * FAST_SCALE_FACTOR + mFastTimeReportedStart;
    }
      break;
    }
    return timeMs;
  }

  /**
   * Get system time, bypassing any clocks
   */
  public static long getRealMs() {
    return System.currentTimeMillis();
  }

  /**
   * Have current thread sleep, bypassing any clocks
   */
  public static long sleepForRealMs(long millis) {
    try {
      if (millis > 0)
        Thread.sleep(millis);
      return millis;
    } catch (InterruptedException e) {
      throw asRuntimeException(e);
    }
  }

  // ------------------------------------------------------------------
  // Fast time
  // ------------------------------------------------------------------
  public void activateFastTime() {
    if (!setMode(MODE_FAST))
      return;
    log("activateFastTime");
    resetFastTime(0);
  }

  public void resetFastTime(long startTime) {
    checkState(mode() == MODE_FAST);
    mFastTimeRealStart = getRealMs();
    if (startTime == 0)
      startTime = mFastTimeRealStart;
    mFastTimeReportedStart = startTime;
  }

  // ------------------------------------------------------------------------------------
  // Accelerated time   (TODO: rename to scripted time)

  /**
   * Activate accelerated time. Causes system to discretely advance effective
   * current time according to sleep requests
   */
  public void activateAcceleratedTime() {
    ensureUnitTestPrepared();
    if (!setMode(MODE_SCRIPTED))
      return;
    log("activateAcceleratedTime");
    mTimeTargets = treeSet();
    mCurrentAcceleratedTime = DateTimeTools.getRealMs();
  }

  /**
   * For test purposes: supply an array to store the DateTime values generated
   * during accelerated sleeping
   */
  public void generateSleepTimeLog() {
    ensureUnitTestPrepared();
    mSleepTimeLog = arrayList();
  }

  public List<TimeTarget> getSleepTimeLog() {
    checkState(mSleepTimeLog != null, "no log available");
    return mSleepTimeLog;
  }

  private void ensureUnitTestPrepared() {
    checkState(mUnitTestPrepared);
  }

  /**
   * This should only be called by the testing framework... once accelerated
   * time has been activated by the client code, it cannot be deactivated until
   * the test is complete
   */
  public void prepareUnitTest() {
    checkState(mContext != null, "no unit test context given");
    checkState(!mUnitTestPrepared);
    mUnitTestPrepared = true;
  }

  private boolean isAccelerated() {
    return mode() == MODE_SCRIPTED;
  }

  public void setAcceleratedTime(long time, String reason) {
    ensureUnitTestPrepared();
    checkNotNull(time);
    activateAcceleratedTime();

    mCurrentAcceleratedTime = time;

    if (mSleepTimeLog != null) {
      synchronized (mSleepTimeLog) {
        mSleepTimeLog.add(new TimeTarget(time, reason));
      }
    }
  }

  /**
   * Add a time target to the accelerated time. Used by the UnitTestScript to
   * ensure the scripted events occur in their appropriate sequence relative to
   * other threads
   */
  public void addAcceleratedTimeTarget(TimeTarget target) {
    checkState(isAccelerated());
    synchronized (mTimeTargets) {
      mTimeTargets.add(target);
      // Note whether this is the initial UnitTestScript time target 
      if (mFirstTarget == null)
        mFirstTarget = target;
    }
  }

  private Long currentAcceleratedTime() {
    return mCurrentAcceleratedTime;
  }

  private void sleepForAccelerated(long millis, String reason) {
    if (millis < 0)
      return;
    checkArgument(millis < Integer.MAX_VALUE);

    long target;
    synchronized (mTimeTargets) {
      long now = currentAcceleratedTime();
      target = now + millis;

      // Add target, unless one with this time already exists; in that case, add 2 ms and repeat
      // Heuristic:
      // Add an additional 'guard' target for this thread that is 1ms later.
      // This ensures that no other thread can advance the clock an unusual amount before
      // this thread has an opportunity to add its next (real, non-guard) sleep target.
      while (true) {
        TimeTarget t1 = new TimeTarget(target, reason);
        TimeTarget t2 = new TimeTarget(target + 1000, "guard for:" + reason);
        if (mTimeTargets.contains(t1) || mTimeTargets.contains(t2)) {
          target = target + 2000;
          continue;
        }

        mTimeTargets.add(t1);
        mTimeTargets.add(t2);
        break;
      }
    }

    boolean sleepFinished = false;
    while (mContext.stillActive()) {
      synchronized (mTimeTargets) {

        while (true) {

          // Let T be the first target in the list.
          // Cases:

          // 1) T belongs to a thread that is no longer alive: remove it.
          //    Special case: it may be the UnitTestScript initial target, and its thread may not have started yet.

          // 2) T belongs to some other thread: yield.

          // 3) T belongs to our thread, and is our target time: remove it, advance time, and exit.

          // 4) T belongs to our thread, and is before our target time: remove it.

          TimeTarget head = mTimeTargets.first();
          if (head != mFirstTarget && !head.thread().isAlive()) {
            popFirstTimeTarget();
          } else if (!head.belongsToCurrentThread()) {
            break;
          } else if (head.time() == target) {
            popFirstTimeTarget();
            setAcceleratedTime(head.time(), reason);
            sleepFinished = true;
            break;
          } else {
            checkState(head.time() < target);
            popFirstTimeTarget();
          }
        }
      }
      if (sleepFinished)
        break;

      // The current thread should continue simulated sleeping, since some other
      // thread is to service the next time target.
      // Allow those other threads a chance to run.

      // For some reason, calling Thread.yield() leads to failed unit tests!
      // I think it's because a single thread can immediately execute another iteration,
      // and advance the clock ahead of where other threads reasonably could expect it
      // to be, causing one thread to monopolize the clock.
      DateTimeTools.sleepForRealMs(5);
    }
  }

  public TimeTarget popFirstTimeTarget() {
    synchronized (mTimeTargets) {
      return mTimeTargets.pollFirst();
    }
  }

  public static final class TimeTarget implements Comparable<TimeTarget> {

    public TimeTarget(long time, String reason, Thread thread) {
      if (thread == null)
        thread = Thread.currentThread();
      mTime = time;
      mThread = thread;
      mReason = nullTo(reason, "(unknown)");
    }

    public TimeTarget(long time, String reason) {
      this(time, reason, null);
    }

    @Override
    public int compareTo(TimeTarget other) {
      return Long.compare(time(), other.time());
    }

    public JSMap toJson() {
      JSMap map = map();
      map.put("time", DateTimeTools.toString(time()));
      map.put("reason", reason());
      map.put("thread", thread().toString());
      return map;
    }

    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    public boolean belongsToCurrentThread() {
      return thread() == Thread.currentThread();
    }

    public long time() {
      return mTime;
    }

    public Thread thread() {
      return mThread;
    }

    public String reason() {
      return mReason;
    }

    private final long mTime;
    private final Thread mThread;
    private final String mReason;
  }

  private TreeSet<TimeTarget> mTimeTargets;
  private volatile Long mCurrentAcceleratedTime;
  private List<TimeTarget> mSleepTimeLog;
  private ExecutionContext mContext;
  private boolean mUnitTestPrepared;
  private TimeTarget mFirstTarget;
  private long mFastTimeRealStart;
  private long mFastTimeReportedStart;

}
