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
package js.system;

import static js.base.Tools.*;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.util.function.Supplier;

import js.base.SystemCall;
import js.parsing.RegExp;

public final class SystemUtil {

  /**
   * Get our own process id (pid)
   */
  public static Long processId() {
    return sProcessIdSupplier.get();
  }

  private static Supplier<Long> sProcessIdSupplier = singleton(() -> {
    String vmName = ManagementFactory.getRuntimeMXBean().getName();
    String processIdString = RegExp.matcherGroup("^\\s*(\\d+)@.*$", vmName);
    checkState(processIdString != null, "Cannot determine id of our own process from: " + vmName);
    return Long.parseLong(processIdString);
  });

  /**
   * Kill particular running processes
   * 
   * @param psSubstrings
   *          a list of substrings that must appear in the output of the command
   *          "ps -x". Lines containing all of these substrings will have their
   *          corresponding pid's killed (except for the caller's pid)
   */
  public static void killProcesses(String... psSubstrings) {
    auxKill(processId(), psSubstrings);
  }

  private static void auxKill(Long currentProcessIdOrNull, String... psSubstrings) {
    SystemCall killCmd = new SystemCall();
    killCmd.arg("kill");
    boolean processesFound = false;

    String[] lines = new SystemCall()//
        .arg("ps", "-x")//
        .assertSuccess()//
        .systemOut().split("\\R");
    outer: for (String ln : lines) {
      for (String expr : psSubstrings) {
        if (!ln.contains(expr)) {
          continue outer;
        }
      }
      String process = RegExp.matcherGroup("^\\s*(\\d+)\\s+.*$", ln);
      if (process == null) {
        alert("Could not parse pid from expression:", quote(ln));
        continue;
      }
      long pid = Long.parseLong(process);
      if (currentProcessIdOrNull != null && currentProcessIdOrNull == pid)
        continue;
      killCmd.arg(pid);
      processesFound = true;
    }

    if (!processesFound)
      return;

    killCmd.assertSuccess();
  }

  public static void killAfterDelay(String... psSubstrings) {
    alert("!Setting up delay to kill app after a delay");

    new java.util.Timer().schedule(new java.util.TimerTask() {
      @Override
      public void run() {
        alert("*** Exiting program, timer has expired");
        auxKill(null, psSubstrings);
      }
    }, 1000 * 300);
  }

  public interface RunnableWithException {
    void run() throws Exception;
  }

  public static void runUnchecked(RunnableWithException r) {
    try {
      r.run();
    } catch (Throwable e) {
      pr("*** Rethrowing:", INDENT, e);
      throw asRuntimeException(e);
    }
  }

  // ------------------------------------------------------------------
  // Memory usage
  // ------------------------------------------------------------------

  /**
   * Get number of bytes of memory currently used
   */
  public static long memoryUsed() {
    long before = getGcCount();
    System.gc();
    while (getGcCount() == before)
      ;
    return getCurrentlyUsedMemory();
  }

  private static long getCurrentlyUsedMemory() {
    return ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed()
        + ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed();
  }

  private static long getGcCount() {
    long sum = 0;
    for (GarbageCollectorMXBean b : ManagementFactory.getGarbageCollectorMXBeans()) {
      long count = b.getCollectionCount();
      if (count != -1) {
        sum += count;
      }
    }
    return sum;
  }

  // ------------------------------------------------------------------
  // Setting 'headless' mode for OSX, to avoid strange menu bar 
  // behaviour with command line apps
  // ------------------------------------------------------------------

  @Deprecated
  public static boolean consoleAppFlag() {
    return true;
  }

  @Deprecated
  public static void setConsoleAppFlag(boolean consoleAppFlag) {
  }

  public static void prepareForConsoleOrGUI(boolean consoleMode) {
    if (sConsoleFlag != null && sConsoleFlag != consoleMode) {
      alert("<1prepareForConsoleOrGUI:", consoleMode, "when already set to:", sConsoleFlag);
    }
    sConsoleFlag = consoleMode;
    System.setProperty("java.awt.headless", consoleMode ? "true" : "false");
    System.setProperty("apple.laf.useScreenMenuBar", consoleMode ? "false" : "true");
  }

  private static Boolean sConsoleFlag;

  /**
   * For OSX, set the Dock icon
   * 
   * ...does nothing at present, as the functionality vanished after upgrading
   * my version of OSX.
   */
  @Deprecated
  public static void setDockIcon() {
  }

}
