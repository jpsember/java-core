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

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.AbstractData;
import js.data.Accessor;
import js.data.DataUtil;
import js.file.Files;

public abstract class AppOper extends BaseObject {

  public abstract String userCommand();

  public abstract void perform();

  // ------------------------------------------------------------------
  // Ownership 
  // ------------------------------------------------------------------

  public final void setApp(App app) {
    mApp = app;
  }

  @SuppressWarnings("unchecked")
  public final <T extends App> T app() {
    return (T) mApp;
  }

  private App mApp;

  // ------------------------------------------------------------------
  // Help
  // ------------------------------------------------------------------

  private final Object SEP = TAB(24);

  protected final void getHelp(BasePrinter b) {
    if (app().hasMultipleOperations())
      b.prNoCr(userCommand(), SEP);
    b.prNoCr(getHelpDescription(), INDENT);
    List<Object> add = getAdditionalArgs();
    if (add != null)
      b.pr(add.toArray());
  }

  protected String getHelpDescription() {
    return "no help description defined yet!";
  }

  protected List<Object> getAdditionalArgs() {
    return null;
  }

  // ------------------------------------------------------------------
  // Command line (or json) arguments
  // ------------------------------------------------------------------

  /**
   * If json arguments are supported, subclass should return the default
   * arguments here
   */
  public AbstractData defaultArgs() {
    return null;
  }

  /**
   * Determine if a json args file *must* exist. Otherwise, uses default args
   */
  protected boolean argsFileMustExist() {
    return false;
  }

  final void processArgs() {
    CmdLineArgs a = app().cmdLineArgs();
    while (a.handlingArgs()) {
      processAdditionalArgs();
    }

    if (app().hasMultipleOperations() && nullOrEmpty(userCommand())) {
      throw badArg("No userCommand defined");
    }

    if (app().genArgsFlag()) {
      AbstractData data = defaultArgs();
      if (data == null) {
        pr("*** json arguments aren't supported for:", userCommand());
      } else
        pr(config());
      throw new ExitOperImmediately();
    }

    if (argsSupported()) {
      mJsonArgs = defaultArgs();

      // Start with the args file that the user supplied as the command line argument (if any)
      File argsFile = app().argsFile();

      // If no args file was specified, and there are multiple user operations, choose an appropriate default
      if (app().hasMultipleOperations()) {
        argsFile = Files.ifEmpty(argsFile, userCommand() + "-args.json");
      }

      if (!argsFile.exists()) {
        if (argsFileMustExist())
          throw die("No args file specified, and no default found at:", argsFile);
      } else {
        mJsonArgs = Files.parseAbstractData(mJsonArgs, argsFile);
      }

      AbstractData argsBuilder = mJsonArgs.toBuilder();

      // While a next arg exists, and matches one of the keys in the args map,
      // parse a key/value pair as an override
      //
      while (a.hasNextArg()) {
        String key = a.peekNextArg();
        Object parsedValue = null;
        Accessor accessor = null;
        try {
          // Attempt to construct a data accessor for a field with this name
          accessor = Accessor.dataAccessor(argsBuilder, key);
          a.nextArg();
        } catch (IllegalArgumentException e) {
          log("no accessor built for arg:", key, e.getMessage());
          break;
        }
        Object value = accessor.get();
        if (value == null)
          throw badArg("Accessor for", quote(key), "returned null; is it optional? They aren't supported");

        Class valueClass = accessor.get().getClass();
        String valueAsString = null;
        if (a.hasNextArg())
          valueAsString = a.peekNextArg();

        // Special handling for boolean args: if no value given or 
        // fails parsing (i.e. next arg is some other key/value pair), assume 'true'
        //
        if (valueClass == Boolean.class) {
          if (valueAsString == null)
            parsedValue = true;
          else {
            parsedValue = tryParseAsBoolean(valueAsString);
            if (parsedValue != null)
              a.nextArg();
            else
              parsedValue = true;
          }
        }

        if (parsedValue == null) {
          if (!a.hasNextArg())
            throw badArg("Missing value for command line argument:", key);
          valueAsString = a.nextArg();
          try {
            parsedValue = DataUtil.parseValueFromString(valueAsString, valueClass);
          } catch (Throwable t) {
            throw badArgWithCause(t, "Failed to parse", quote(key), ":", valueAsString);
          }
        }
        accessor.set(parsedValue);
      }
      mJsonArgs = argsBuilder.build();
    }
  }

  private static Boolean tryParseAsBoolean(String expr) {
    expr = expr.toLowerCase();
    if (expr.equals("true") || expr.equals("t"))
      return true;
    if (expr.equals("false") || expr.equals("f"))
      return false;
    return null;
  }

  /**
   * Add 'dash-style' command line arguments; only called if json args not
   * supported
   */
  protected void addCommandLineArgs(CmdLineArgs ca) {
  }

  /**
   * Hook for operation-specific additional argument handling within the
   * handlingArgs() loop
   */
  protected void processAdditionalArgs() {
  }

  /**
   * Get the json arguments that were provided by the user (which may be the
   * default ones)
   */
  @SuppressWarnings("unchecked")
  public <T extends AbstractData> T config() {
    argsSupported();
    checkNotNull(mJsonArgs, "No default args supported");
    return (T) mJsonArgs;
  }

  private boolean argsSupported() {
    if (mArgsSupported == null) {
      mJsonArgs = defaultArgs();
      mArgsSupported = (mJsonArgs != null);
    }
    return mArgsSupported;
  }

  /**
   * An exception of this type is thrown if GEN-ARGS option was supplied, to
   * leave the operation without doing further work
   */
  static class ExitOperImmediately extends RuntimeException {
  }

  private Boolean mArgsSupported;
  private AbstractData mJsonArgs;

  // ------------------------------------------------------------------
  // README file generation
  // ------------------------------------------------------------------

  public final BasePrinter readme() {
    if (mReadMe == null)
      mReadMe = new BasePrinter();
    return mReadMe;
  }

  public final void writeReadme(File directory) {
    String content = readme().toString().trim();
    checkState(!content.isEmpty(), "No README content was generated");
    Files.S.writeString(new File(directory, "README.md"), content + "\n");
  }

  private BasePrinter mReadMe;

  // ------------------------------------------------------------------
  // Convenience methods accessing parent app
  // ------------------------------------------------------------------

  public final boolean dryRun() {
    return app().dryRun();
  }

  public final Files files() {
    return app().files();
  }

  public final CmdLineArgs cmdLineArgs() {
    return app().cmdLineArgs();
  }

  public final RuntimeException setError(Object... messages) {
    return app().setErrorWithContext(null, messages);
  }
}
