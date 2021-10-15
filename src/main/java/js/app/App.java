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
package js.app;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import js.app.AppOper.ExitOperImmediately;
import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.file.Files;
import js.geometry.MyMath;

public abstract class App extends BaseObject {

  // ------------------------------------------------------------------
  // Launching app
  // ------------------------------------------------------------------

  /**
   * Subclass should call this method from its main(String[]) method
   */
  public final void startApplication(String[] cmdLineArguments) {
    mOperMap = hashMap();
    registerOperations();

    cmdLineArgs().parse(cmdLineArguments);
    if (cmdLineArgs().helpShown())
      return;

    if (cmdLineArgs().get(CLARG_VERSION)) {
      pr(getVersion());
      return;
    }

    setVerbose(cmdLineArgs().get(CLARG_VERBOSE));
    mDryRun = cmdLineArgs().get(CLARG_DRYRUN);
    if (supportArgsFile()) {
      mGenArgs = cmdLineArgs().get(CLARG_GEN_ARGS);
      mArgsFile = new File(cmdLineArgs().getString(CLARG_ARGS_FILE));
    } else
      mArgsFile = Files.DEFAULT;

    try {
      runApplication(cmdLineArgs());
    } catch (AppErrorException e) {
    }
  }

  protected abstract void registerOperations();

  protected void registerOper(AppOper oper) {
    mOperMap.put(oper.userCommand(), oper);
    oper.setApp(this);
  }

  public final boolean hasMultipleOperations() {
    return mOperMap.size() > 1;
  }

  private Map<String, AppOper> mOperMap;

  private void runApplication(CmdLineArgs args) {
    if (!hasMultipleOperations()) {
      checkArgument(!mOperMap.isEmpty(), "no AppOper defined");
      AppOper oper = mOperMap.values().iterator().next();
      auxRunOper(oper);
    } else {
      while (args.hasNextArg()) {
        String operation = args.nextArg();
        AppOper oper = mOperMap.get(operation);
        checkArgument(oper != null, "No such operation:", operation);
        auxRunOper(oper);
      }
    }
  }

  private void auxRunOper(AppOper oper) {
    try {
      oper.setVerbose(verbose());
      oper.processArgs();
      oper.perform();
    } catch (ExitOperImmediately e) {
    }
  }

  public final boolean catchingErrors() {
    return !cmdLineArgs().get(CLARG_SHOW_EXCEPTIONS);
  }

  @Override
  protected final String supplyName() {
    String className = getClass().getName();
    List<String> sections = split(className, '.');
    checkArgument(sections.size() >= 2, "trouble parsing name from package", className);
    return DataUtil.capitalizeFirst(MyMath.getMod(sections, -2));
  }

  private File mArgsFile;

  // ------------------------------------------------------------------
  // Command line arguments
  // ------------------------------------------------------------------

  private static final String CLARG_VERBOSE = "verbose";
  private static final String CLARG_VERSION = "version";
  private static final String CLARG_DRYRUN = "dryrun";
  private static final String CLARG_GEN_ARGS = "gen-args";
  private static final String CLARG_ARGS_FILE = "args";
  private static final String CLARG_SHOW_EXCEPTIONS = "exceptions";

  public final CmdLineArgs cmdLineArgs() {
    if (mCmdLineArgs == null) {
      CmdLineArgs ca = mCmdLineArgs = new CmdLineArgs();
      if (supportArgsFile()) {
        ca.add(CLARG_ARGS_FILE).def("").desc("Specify file containing arguments").shortName("a");
        ca.add(CLARG_GEN_ARGS).desc("Generate default operation arguments").shortName("g");
      } 
      
      {
        addAppCommandLineArgs(ca);
        for (AppOper oper : mOperMap.values())
          oper.addCommandLineArgs(ca);
      }
      ca.banner(getClass().getSimpleName() + " banner (!!! Please add one)");
      ca.add(CLARG_DRYRUN).desc("Dry run");
      ca.add(CLARG_SHOW_EXCEPTIONS).desc("Show exception stack traces").shortName("e");
      ca.add(CLARG_VERBOSE).desc("Verbose messages").shortName("v");
      ca.add(CLARG_VERSION).desc("Display version number").shortName("n");
      defineCommandLineArgs(ca);
    }
    return mCmdLineArgs;
  }

  /**
   * Allow an app to add its own 'hyphen' style command line arguments; only
   * called if args file is not supported
   */
  public void addAppCommandLineArgs(CmdLineArgs ca) {
  }

  private void defineCommandLineArgs(CmdLineArgs args) {
    StringBuilder sb = new StringBuilder(name().toLowerCase());
    sb.append(" version: ");
    sb.append(getVersion());
    sb.append("\n");

    if (hasMultipleOperations()) {
      sb.append("\nUsage: [--<app arg>]* [<operation> <operation arg>*]*\n\n");
      sb.append("Operations:\n");
    }
    for (AppOper oper : mOperMap.values()) {
      BasePrinter b = new BasePrinter();
      oper.getHelp(b);
      sb.append(b.toString());
      sb.append('\n');
    }
    if (hasMultipleOperations())
      sb.append("\nApp arguments:");

    cmdLineArgs().banner(sb.toString());
  }

  private CmdLineArgs mCmdLineArgs;

  // ------------------------------------------------------------------
  // Dry run support
  // ------------------------------------------------------------------

  public final boolean dryRun() {
    return mDryRun;
  }

  // ------------------------------------------------------------------
  // Injectable Files object (for unit tests)
  // ------------------------------------------------------------------

  public final void setFiles(Files f) {
    checkState(mFilesObject == null,"Files already set");
    mFilesObject = f.withDryRun(dryRun()).withVerbose(verbose());
  }
  
  public final Files files() {
    if (mFilesObject == null)
      setFiles(new Files());
    return mFilesObject;
  }

  private Files mFilesObject;
  private boolean mDryRun;

  // ------------------------------------------------------------------
  // Version numbers
  // ------------------------------------------------------------------

  public abstract String getVersion();

  // ------------------------------------------------------------------
  // JSON arguments
  // ------------------------------------------------------------------

  /**
   * Determine if json arguments are supported
   */
  public boolean supportArgsFile() {
    return true;
  }

  public final File argsFile() {
    return mArgsFile;
  }

  public final boolean genArgsFlag() {
    return mGenArgs;
  }

  private boolean mGenArgs;

  // ------------------------------------------------------------------
  // Application exit code, errors
  // ------------------------------------------------------------------

  public final void exitWithReturnCode() {
    System.exit(returnCode());
  }

  public final int returnCode() {
    return mAppErrorException != null ? 1 : 0;
  }

  /**
   * Set an error, and throw an AppErrorException
   */
  public final AppErrorException setError(Object... messages) {
    return setErrorWithContext(null, messages);
  }

  /**
   * Set an error, and throw an AppErrorException
   */
  public final AppErrorException setErrorWithContext(Object context, Object... messages) {
    if (mAppErrorException == null) {
      String prefix = "*** ";
      if (context != null)
        prefix = prefix + "(" + context + ") ";
      messages = insertStringToFront(prefix, messages);
      String message = BasePrinter.toString(messages);
      mAppErrorException = new AppErrorException(message);
      if (displayExceptionStackTraces())
        mAppErrorException.printStackTrace();
      else
        System.err.println(message);
    }
    throw mAppErrorException;
  }

  public static final class AppErrorException extends RuntimeException {
    public AppErrorException(String message) {
      super(message);
    }
  }

  public final RuntimeException getError() {
    return mAppErrorException;
  }

  private final boolean displayExceptionStackTraces() {
    return cmdLineArgs().get(CLARG_SHOW_EXCEPTIONS);
  }

  private AppErrorException mAppErrorException;

}
