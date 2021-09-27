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

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import js.json.JSMap;

/**
 * Calls the Java APIs to make a system call, capturing System.out, System.err
 */
public final class SystemCall extends BaseObject {

  /**
   * Specify what the current directory should be for the call
   */
  public SystemCall directory(File directory) {
    assertMutable();
    processBuilder().directory(directory);
    return this;
  }

  public SystemCall withVerbose(boolean verbose) {
    setVerbose(verbose);
    return this;
  }

  /**
   * Add arguments
   */
  public SystemCall arg(Object... argumentObjects) {
    assertMutable();
    for (Object arg : argumentObjects) {
      String argStr = arg.toString();
      mArgList.add(argStr);
      if (argStr.charAt(0) == '"' || argStr.charAt(0) == '\'')
        throw badArg("Unexpected quoting in SystemCall argument?", argStr);
    }
    return this;
  }

  /**
   * Perform the system call (if not already performed)
   */
  public SystemCall call() {
    if (mExitCode != null)
      return this;
    try {
      if (verbose())
        log("Making call:", INDENT, sysCallAsString());
      processBuilder().command(mArgList);
      Process process = processBuilder().start();
      mSystemOut = captureStreamToString(process.getInputStream(), verbose() ? "" : null);
      mSystemErr = captureStreamToString(process.getErrorStream(), verbose() ? "ERR>>" : null);
      mExitCode = process.waitFor();
      process.destroy();
    } catch (Throwable t) {
      // Replace uninitialized values with reasonable defaults
      if (mExitCode == null)
        mExitCode = 99;
      if (mSystemOut == null)
        mSystemOut = "";
      if (mSystemErr == null)
        mSystemErr = ifNullOrEmpty(t.getMessage(), "<no details>");
      throw asRuntimeException(t);
    }
    return this;
  }

  /**
   * Get System.out contents (after making call, if not already made)
   */
  public String systemOut() {
    call();
    return mSystemOut;
  }

  /**
   * Get System.err contents (after making call, if not already made)
   */
  public String systemErr() {
    call();
    return mSystemErr;
  }

  /**
   * Make call (if necessary), and throw exception if not successful
   */
  public SystemCall assertSuccess() {
    call();
    if (mExitCode != 0)
      badState("SystemCall failed:", INDENT, toJson());
    return this;
  }

  /**
   * Make call (if necessary), and get exit code
   */
  public int exitCode() {
    call();
    return mExitCode;
  }

  private String sysCallAsString() {
    return String.join(" ", mArgList);
  }

  @Override
  public JSMap toJson() {
    JSMap map = new JSMap();
    map.put("args", sysCallAsString());
    if (mExitCode != null) {
      map.put("system_out", systemOut());
      map.put("system_err", systemErr());
      map.put("exit_code", exitCode());
    }
    return map;
  }

  private String captureStreamToString(InputStream stream, String optionalLinePrefix) {
    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    StringBuilder stringBuilder = new StringBuilder();
    reader.lines().iterator().forEachRemaining((line) -> {
      stringBuilder.append(line);
      stringBuilder.append('\n');
      if (optionalLinePrefix != null)
        pr(optionalLinePrefix, line);
    });
    return stringBuilder.toString();
  }

  private void assertMutable() {
    checkState(mExitCode == null, "call already made");
  }

  private ProcessBuilder processBuilder() {
    return mProcess;
  }

  private final List<String> mArgList = new ArrayList<>();
  private final ProcessBuilder mProcess = new ProcessBuilder();
  private Integer mExitCode;
  private String mSystemOut;
  private String mSystemErr;
}
