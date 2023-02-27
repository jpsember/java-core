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

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.*;
import java.util.function.Consumer;

/**
 * Wrapper class for Executor to process tasks
 * 
 * Relevant: https://stackoverflow.com/questions/2248131
 */
public class TaskProcessor<T> extends BaseObject implements AutoCloseable {

  /**
   * Specifies an optional method to call with each completed task
   */
  public TaskProcessor<T> withConsumer(Consumer<T> consumer) {
    checkState(mConsumer == null);
    mConsumer = consumer;
    return this;
  }

  public TaskProcessor<T> withMaxBuffered(int count) {
    assertState(STATE_START);
    mMaxBuffered = count;
    return this;
  }

  public Future<T> submit(Callable<T> task) {
    log("submit", mSubmitCount);
    begin();
    mSubmitCount++;
    if (mMaxBuffered > 0) {
      int reducedSize = mMaxBuffered - 1;
      if (reducedSize < size()) {
        log("...fetching results until size is", reducedSize);
        fetchResults(reducedSize);
      }
    }

    Future<T> future = executor().submit(task);
    mFutureResults.add(future);
    mMaxQueueUsed = Math.max(mMaxQueueUsed, mFutureResults.size());
    return future;
  }

  /**
   * Submit a task that will repeatedly call a particular method at a particular
   * rate
   */
  public void startBackgroundTask(long intervalMs, Runnable userMethod) {
    checkState(!mBackgroundTaskStarted, "background task already started");
    mBackgroundTaskStarted = true;

    submit(new Callable<T>() {
      @Override
      public T call() throws Exception {
        while (mBackgroundTaskStarted) {
          try {
            if (verbose())
              log("running background task at:", DateTimeTools.humanTimeString());
            userMethod.run();
          } catch (Throwable t) {
            pr("*** background task caught:", t);
          }
          DateTimeTools.sleepForRealMs(intervalMs);
        }
        return null;
      }
    });

  }

  public void stopBackgroundTask() {
    mBackgroundTaskStarted = false;
  }

  private volatile boolean mBackgroundTaskStarted;

  /**
   * For test purposes only
   */
  public int maxQueueUsed() {
    return mMaxQueueUsed;
  }

  public static Throwable getRootCause(Throwable t) {
    while (t != null) {
      if (t instanceof ExecutionException) {
        return t.getCause();
      }
      t = t.getCause();
    }
    return null;
  }

  private static final int STATE_START = 0;
  private static final int STATE_READY = 1;
  private static final int STATE_SHUTDOWN = 2;

  /**
   * Puts processor into SHUTDOWN state, if not already, and fetches any
   * remaining results
   */
  @Override
  public void close() {
    begin();
    if (mState != STATE_SHUTDOWN) {
      log("shutdown");
      executor().shutdown();
      mExecutor = null;
      mState = STATE_SHUTDOWN;
      fetchResults(0);
      if (mConsumeCount != mSubmitCount) {
        pr("*** submitted", mSubmitCount, "!= consumed", mConsumeCount);
      }
    }
  }

  /**
   * Puts processor into SHUTDOWN state, if not already, and fetches any
   * remaining results
   */
  public void shutdown() {
    alert("consider using a try(...){ ... } block instead");
    close();
  }

  @Override
  @Deprecated // This method has been deprecated in Java 9, so mark it as such to avoid maven warnings
  protected void finalize() throws Throwable {
    if (mState == STATE_READY) {
      pr("TaskProcessor was finalized without being shut down");
      shutdown();
    }
    super.finalize();
  }

  public boolean isEmpty() {
    return size() == 0;
  }

  public boolean nonEmpty() {
    return !isEmpty();
  }

  public int size() {
    begin();
    return mFutureResults.size();
  }

  /**
   * If result for the future at the head of the queue is available, removes it
   * from the head of the queue and forwards the result to the consumer
   * 
   * Returns true if the queue is empty
   */
  public boolean fetchResultsIfAvail() {
    // TODO: we might want to be able to examine any element, not just the first; 
    //  but for the download task this is good enough 
    while (!isEmpty()) {
      Future<T> future = mFutureResults.peek();
      if (!future.isDone())
        break;
      fetchResults(size() - 1);
    }
    return isEmpty();
  }

  public void fetchAllResults() {
    fetchAllResults(null);
  }

  public void fetchAllResults(Runnable idleTask) {
    while (!fetchResultsIfAvail()) {
      if (verbose())
        log("...fetchAllResults not done");
      long sleepTime = 100;
      if (idleTask != null) {
        long time = System.currentTimeMillis();
        // TODO: not sure this is required or sufficient
        try {
          idleTask.run();
        } catch (Throwable t) {
          pr("*** Caught exception in idle task:", INDENT, t);
        }
        long time2 = System.currentTimeMillis();
        sleepTime -= (time2 - time);
      }
      if (sleepTime > 0)
        DateTimeTools.sleepForRealMs(sleepTime);
    }
  }

  private void fetchResults(int targetSize) {
    try {
      while (size() > targetSize) {
        Future<T> future = mFutureResults.remove();
        T result = future.get();
        if (mConsumer != null) {
          if (verbose())
            log("sending result", mConsumeCount, "to consumer");
          mConsumer.accept(result);
        }
        mConsumeCount++;
      }
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  private void begin() {
    if (mState != STATE_START)
      return;
    assertState(STATE_START);

    int numProcessors = Runtime.getRuntime().availableProcessors();
    log("number of processors:", numProcessors);
    if (numProcessors > 4) {
      log("...but Macbook seems to not do better with more than 4; see stackoverflow");
      numProcessors = 4;
    }
    mExecutor = Executors.newFixedThreadPool(numProcessors);
    mFutureResults = new ArrayDeque<>();
    mState = STATE_READY;
  }

  private ExecutorService executor() {
    begin();
    return mExecutor;
  }

  private void assertState(int expected) {
    if (mState == expected)
      return;
    throw new IllegalStateException("Expected state: " + expected + " but was: " + mState);
  }

  private int mState = STATE_START;
  private ExecutorService mExecutor;
  private Deque<Future<T>> mFutureResults;
  private Consumer<T> mConsumer;
  private int mMaxBuffered = 20;
  private int mSubmitCount;
  private int mConsumeCount;
  private int mMaxQueueUsed;

}
