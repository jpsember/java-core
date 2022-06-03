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
import java.util.Set;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.AbstractData;
import js.data.Accessor;
import js.data.DataUtil;
import js.file.Files;
import js.json.JSMap;

public abstract class AppOper extends BaseObject {

  public abstract String userCommand();

  public abstract void perform();

  // ------------------------------------------------------------------
  // Ownership 
  // ------------------------------------------------------------------

  public final void setApp(App app) {
    mApp = app;
  }

  public final <T extends App> T app() {
    return (T) mApp;
  }

  private App mApp;

  // ------------------------------------------------------------------
  // Help
  // ------------------------------------------------------------------

  private final Object SEP = TAB(24);

  protected final void getHelp(BasePrinter b) {
    if (app().hasMultipleOperations()) {
      b.prNoCr(userCommand(), SEP);
      b.prNoCr(getHelpDescription(), INDENT);
    }
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

  @Deprecated
  public static File findSubprojectVariant(File file) {
    todo("move this to Files");
    File output = Files.assertNonEmpty(file);
    File subprojectFile = new File("subproject.txt");
    pr("looking for subproject variant for file:",INDENT,Files.infoMap(file));
    pr("subprojectFile:",INDENT,Files.infoMap(subprojectFile));
    if (subprojectFile.exists()) {
      String suffix = Files.readString(subprojectFile).trim();
      pr("suffix:",suffix);
      checkArgument(nonEmpty(suffix), "No subproject found in:", subprojectFile);
      output = Files.setExtension(new File(Files.removeExtension(file.toString()) + "-" + suffix),
          Files.getExtension(file));
      pr("output:",INDENT,Files.infoMap(output));
    }
    return output;
  }

  private String defaultArgsFilename() {
    String base = userCommand();
    todo("delete this unused stuff... hook into 'subproject.txt' if it exists");
    if (false) {
      File subprojectFile = new File("subproject.txt");
      if (subprojectFile.exists()) {
        String suffix = Files.readString(subprojectFile).trim();
        checkArgument(nonEmpty(suffix), "No subproject found in:", subprojectFile);
        base = base + "-" + suffix;
      }
    }
    return base + "-args.json";
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
      } else {
        pr(config());
      }
      throw new ExitOperImmediately();
    }

    if (argsSupported()) {
      mJsonArgs = defaultArgs();

      // Start with the args file that the user supplied as the command line argument (if any)
      File argsFile = app().argsFile();
      argsFile = Files.ifEmpty(argsFile, defaultArgsFilename());
      argsFile = findSubprojectVariant(argsFile);
      log("...looking for arguments in:", argsFile);
      if (!argsFile.exists()) {
        // If there is a version of the args file with underscores instead, raise hell
        {
          String name = argsFile.getName();
          String fixed = name.replace('_', '-');
          if (!fixed.equals(name)) {
            File fixedFile = new File(Files.parent(argsFile), fixed);
            if (fixedFile.exists())
              setError("Could not find arguments file:", argsFile,
                  "but did find one with different spelling:", fixedFile, "(assuming this is a mistake)");
          }
        }

        if (argsFileMustExist())
          throw setError("No args file specified, and no default found at:", argsFile);
      } else {
        mJsonArgs = Files.parseAbstractData(mJsonArgs, argsFile);
        if (a.get(App.CLARG_VALIDATE_KEYS)) {
          //
          // Generate a JSMap A from the parsed arguments
          // Re-parse the args file to a JSMap B.
          // See if B.keys - A.keys is nonempty... if so, that's a problem.
          //
          // NOTE: this will only check the top-level JSMap, not any nested maps.
          //
          Set<String> keysA = mJsonArgs.toJson().asMap().keySet();
          JSMap json = JSMap.fromFileIfExists(argsFile);
          Set<String> keysB = json.keySet();
          keysB.removeAll(keysA);
          if (!keysB.isEmpty())
            throw setError("Unexpected keys in", argsFile, INDENT, keysB);
        }
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
