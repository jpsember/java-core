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
package js.file;

import java.io.File;
import java.util.List;

import js.base.BaseObject;
import js.base.DateTimeTools;

import static js.base.Tools.*;

/**
 * Asynchronous communication service. Default implementation uses filesystem
 * and atomic renamining to enforce thread safety
 */
public class AsyncChannel extends BaseObject {

  public interface Handler {
    void processFile(File file);
  }

  /**
   * Specify directory to contain the message files
   */
  public final AsyncChannel setDirectory(File directory, boolean createIfMissing) {
    assertConfigurable();
    mDirectory = directory;
    if (createIfMissing)
      Files.S.mkdirs(mDirectory);
    Files.assertDirectoryExists(mDirectory);
    if (mName == null)
      withName(directory.getName());
    return this;
  }

  /**
   * Set the file extension of the messages; default is "bin"
   */
  public final AsyncChannel setFileExtension(String extension) {
    assertConfigurable();
    checkArgument(!extension.startsWith("."));
    checkArgument(!extension.equals(RENAME_EXTENSION));
    mExtension = extension;
    return this;
  }

  @Override
  protected String supplyName() {
    if (mName != null)
      return mName;
    return super.supplyName();
  }

  public final AsyncChannel withName(String name) {
    assertConfigurable();
    if (mName != null)
      throw badState("already given a name");
    mName = name;
    return this;
  }

  public final AsyncChannel withHandler(Handler handler) {
    todo(
        "if strangely large number of files appear in input, discard them all with a log message instead of attempting to process them");
    assertConfigurable();
    mHandler = handler;
    return this;
  }

  public final AsyncChannel withVerbose(boolean flag) {
    setVerbose(flag);
    return this;
  }

  public final AsyncChannel withMinInterval(float seconds) {
    mMinAvgIntervalSec = seconds;
    return this;
  }

  /**
   * Specify a prefix to use for the filenames. Defaults to empty string. This
   * allows multiple clients to write to a channel without risk of filename
   * collisions
   */
  public final AsyncChannel withFilenamePrefix(String prefix) {
    mFilenamePrefix = prefix;
    return this;
  }

  /**
   * Put channel into locked state, for communication to begin. Any further
   * configuration attempts will fail
   */
  public final void lock() {
    if (!mLocked) {
      checkNotNull(directory(), "no directory defined");
      mLocked = true;
    }
  }

  public static final String RENAME_EXTENSION = "temp";

  /**
   * Delete any (presumably stale) files from the channel that match the
   * extension (or temp)
   */
  public final AsyncChannel clean() {
    assertConfigurable();
    List<File> files = new DirWalk(directory()).withExtensions(mExtension, RENAME_EXTENSION).files();
    for (File file : files) {
      if (verbose())
        log("...(cleaning:", file, ")");
      Files.S.deletePeacefully(file);
    }
    return this;
  }

  /**
   * Process messages within directory
   */
  public final void update() {
    lock();
    checkNotNull(mHandler, "please provide a Handler", name());
    updateMaintenanceTime();
    if (verbose())
      log("update, examining files in:", directory(), "with extension:", mExtension);

    DirWalk dirWalk = new DirWalk(directory()).withExtensions(mExtension);
    mem().monitorSize(getClass(), dirWalk.files());

    for (File file : dirWalk.files()) {
      updateIntervalCalculation();

      try {
        if (verbose())
          log("processFile", file.getName());
        mHandler.processFile(file);
      } catch (Throwable t) {
        processException(file, t);
      } finally {
        Files.S.deletePeacefully(file);
      }
    }
  }

  /**
   * Send a message to the channel. Returns the message's number
   */
  public final int send(String message) {
    return send(message.getBytes());
  }

  /**
   * Send a message to the channel. Returns the message's number
   */
  public final int send(byte[] messageBytes) {
    lock();

    verifyMaintenanceCalled();

    int messageNumber = mMessageNumber;
    mMessageNumber++;
    String messageName = nullToEmpty(mFilenamePrefix) + messageNumber + "." + RENAME_EXTENSION;
    File tempFile = new File(directory(), messageName);
    File targetFile = Files.setExtension(tempFile, mExtension);
    if (tempFile.exists()) {
      alert("file already existed:", tempFile);
      Files.S.deletePeacefully(tempFile);
    }
    if (targetFile.exists()) {
      alert("file already existed:", targetFile);
      Files.S.deletePeacefully(targetFile);
    }
    Files.S.write(messageBytes, tempFile);

    if (verbose())
      log("sending", targetFile.getName());

    Files.S.moveFile(tempFile, targetFile);

    return messageNumber;
  }

  public void processException(File file, Throwable exception) {
    pr("trouble processing:", file, INDENT, exception);
  }

  public final int messageCount() {
    return mMessageNumber;
  }

  public final File directory() {
    return mDirectory;
  }

  private void assertConfigurable() {
    checkState(!mLocked, "AsyncChannel already locked");
  }

  private void updateIntervalCalculation() {
    if (mMinAvgIntervalSec == 0)
      return;
    if (mHistory == null)
      mHistory = arrayList();

    final int minEvents = 20;
    final int maxEvents = 50;
    long time = System.currentTimeMillis();

    mHistory.add(time);
    int len = mHistory.size();
    if (len >= minEvents) {
      float sum = 0;
      Long prev = null;
      for (Long val : mHistory) {
        if (prev != null) {
          float elapsed = (val - prev) / 1000.0f;
          sum += elapsed;
        }
        prev = val;
      }
      float avgDelay = sum / len;
      if (len > maxEvents)
        mHistory.remove(0);

      if (avgDelay < mMinAvgIntervalSec) {
        pr("*** average delay between channel events", name(), "is", avgDelay, "sec");
      }
    }
  }

  // I now suspect that update() only needs to be called by some channels (e.g. input channels)
  private static final boolean VERIFY_MAINTENANCE = true && alert("verifying .update() called");

  private void updateMaintenanceTime() {
    if (!VERIFY_MAINTENANCE)
      return;
    mLastMaintenanceTime = System.currentTimeMillis();
  }

  private void verifyMaintenanceCalled() {
    if (!VERIFY_MAINTENANCE)
      return;
    if (mHandler == null)
      return;
    long current = System.currentTimeMillis();
    if (mLastMaintenanceTime == 0)
      updateMaintenanceTime();
    if (current - mLastMaintenanceTime > DateTimeTools.SECONDS(10))
      die("AsyncChannel update is never called!", name());
  }

  private boolean mLocked;
  private String mExtension = "bin";
  private File mDirectory;
  private int mMessageNumber;
  private String mFilenamePrefix;
  private String mName;
  private Handler mHandler;
  private float mMinAvgIntervalSec;
  private List<Long> mHistory;
  private long mLastMaintenanceTime;

}
