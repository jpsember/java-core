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
import java.util.*;
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.data.DataUtil;
import js.file.Files;
import js.parsing.RegExp;

/**
 * <pre>
 * 
 * Command line argument processor
 * 
 * Inspired by trollop.rb.
 * I looked at 3rd party alternatives, and they all seem fairly heavyweight.
 * 
 * Terminology: 
 * 
 * Options (e.g., 'speed' of type: double) are provided by the client application.
 * Arguments (e.g., '--speed 42.5') are provided by the user from the command line.
 * 
 * Options are analogous to classes, whereas arguments are analogous to objects 
 * or instances.
 * 
 * Option types can be booleans, integers, doubles, or strings; they can be scalars,
 * or (except boolean) can be arrays of zero or more of these.
 * 
 * Usage:
 * 
 * 1) construct a CmdLineArgs instance
 * 
 * CmdLineArgs clArgs = new CmdLineArgs()
 * 
 * 2) define options 
 * 
 * clArgs.banner("Program for compiling graphs or whatnot");   // displayed in help
 * 
 * clArgs.add("speed").def(42.0);      // Double
 * clArgs.add("verbose");              // Boolean; default is false
 * clArgs.add("maxdepth").def(8);      // Integer
 * clArgs.add("name").setString();     // No default value, type set explicitly
 * clArgs.add("heights").setInt().setArray();     // array of (zero or more) ints
 * clArgs.add("dimensions").setInt().setArray(2); // array of exactly two ints
 * 
 * 3) parse command line arguments
 * 
 * try {
 *   clArgs.parse(args);                          // where clArgs is String[]
 * } catch (CmdLineArgs.Exception e) {
 *   System.out.println(e.getMessage());
 * }
 * 
 * 4) process arguments
 * 
 * if (clArgs.get("verbose")) { ... }   
 * 
 * if (clArgs.hasValue("name")) { 
 *    System.out.println("name is "+clArgs.getString("name"));
 * }
 * 
 * clArgs.getInts("heights"); // get array (of ints)
 * 
 * clArgs.getExtras();  // returns array of any arguments not parsed as options
 * 
 * 
 * </pre>
 */
public final class CmdLineArgs extends BaseObject {

  private static final String HELP = "help";

  /**
   * Specify a banner, which describes the app; it's displayed in help messages
   */
  public CmdLineArgs banner(String banner) {
    mBanner = banner;
    return this;
  }

  /**
   * Add an option; makes it the current option. It initially has type boolean,
   * with default value false
   * 
   * @param longName
   */
  public CmdLineArgs add(String longName) {
    checkState(!mLocked);
    mOpt = new Option(longName);
    claimName(longName, mOpt);
    mOptionList.add(mOpt.mLongName);
    return this;
  }

  public CmdLineArgs shortName(String shortName) {
    mOpt.mShortName = shortName;
    claimName(shortName, mOpt);
    return this;
  }

  /**
   * Set type of current option to int
   */
  public CmdLineArgs setInt() {
    mOpt.setType(T_INT);
    return this;
  }

  /**
   * Set type of current option to double
   */
  public CmdLineArgs setDouble() {
    mOpt.setType(T_DOUBLE);
    return this;
  }

  /**
   * Set type of current option to string
   */
  public CmdLineArgs setString() {
    mOpt.setType(T_STRING);
    return this;
  }

  /**
   * Make current option an array type, of variable length
   */
  public CmdLineArgs setArray() {
    return setArray(-1);
  }

  /**
   * Make current option an array type, of fixed length
   */
  public CmdLineArgs setArray(int requiredLength) {
    mOpt.setArray();
    mOpt.mExpectedValueCount = requiredLength;
    return this;
  }

  /**
   * Set default value of current option to a boolean
   */
  public CmdLineArgs def(boolean boolValue) {
    mOpt.setType(T_BOOL);
    mOpt.mDefaultValue = boolValue;
    return this;
  }

  /**
   * Set type of current option to string, with a default value
   */
  public CmdLineArgs def(String stringValue) {
    mOpt.setType(T_STRING);
    mOpt.mDefaultValue = stringValue;
    return this;
  }

  /**
   * Set type of current option to integer, with a default value
   */
  public CmdLineArgs def(int intValue) {
    mOpt.setType(T_INT);
    mOpt.mDefaultValue = intValue;
    return this;
  }

  /**
   * Set type of current option to double, with a default value
   */
  public CmdLineArgs def(double doubleValue) {
    mOpt.setType(T_DOUBLE);
    mOpt.mDefaultValue = doubleValue;
    return this;
  }

  /**
   * Set description for an option (it is displayed within help messages)
   */
  public CmdLineArgs desc(String description) {
    mOpt.mDescription = description;
    return this;
  }

  /**
   * Determine if user provided a value for a particular option
   * 
   * @param optionName
   *          name of option
   * @return true if user provided a value
   */
  public boolean hasValue(String optionName) {
    return !findOption(optionName).mValues.isEmpty();
  }

  /**
   * Get the boolean value supplied for an option, or its default if none was
   * given. If no default was specified, assume it was false.
   */
  public boolean get(String optionName) {
    Option opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_BOOL, "type mismatch", optionName);
    Object value = opt.mDefaultValue;
    if (value == null)
      value = Boolean.FALSE;
    if (!opt.mValues.isEmpty())
      value = opt.mValues.get(0);
    return (Boolean) value;
  }

  /**
   * Get the single integer supplied for an option, or its default value if none
   * was given
   */
  public int getInt(String optionName) {
    Option opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_INT, "type mismatch", optionName);
    return (Integer) opt.valueOrDefault(optionName);
  }

  /**
   * Get the single double supplied for an option, or its default value if none
   * was given
   */
  public double getDouble(String optionName) {
    Option opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_DOUBLE, "type mismatch", optionName);
    return (Double) opt.valueOrDefault(optionName);
  }

  /**
   * Get the single string supplied for an option, or its default value if none
   * was given
   */
  public String getString(String optionName) {
    Option opt = findOption(optionName);
    validate(!opt.mArray && opt.mType == T_STRING, "type mismatch", optionName);
    return (String) opt.valueOrDefault(optionName);
  }

  /**
   * Get the array of ints supplied for an option
   */
  public Integer[] getInts(String optionName) {
    Option opt = findOption(optionName);
    validate(opt.mArray && opt.mType == T_INT, "type mismatch", optionName);
    return opt.mValues.toArray(new Integer[0]);
  }

  /**
   * Get the array of doubles supplied for an argument
   */
  public Double[] getDoubles(String arg) {
    Option opt = findOption(arg);
    validate(opt.mArray && opt.mType == T_DOUBLE, "type mismatch", arg);
    return opt.mValues.toArray(new Double[0]);
  }

  /**
   * Get the array of strings supplied for an option
   */
  public String[] getStrings(String optionName) {
    Option opt = findOption(optionName);
    validate(opt.mArray && opt.mType == T_STRING, "type mismatch", optionName);
    return DataUtil.toStringArray(opt.mValues);
  }

  public String[] getExtras() {
    return DataUtil.toStringArray(mExtraArguments);
  }

  public void mutuallyExclusive(String... args) {
    int numberUsed = 0;
    for (String arg : args) {
      Option opt = findOption(arg);
      if (opt.hasValue())
        numberUsed++;
    }
    if (numberUsed > 1) {
      validate(false, "At most one of these options can be used", String.join(", ", args));
    }
  }

  /**
   * Parse command line arguments
   */
  public void parse(App app, String[] args) {
    checkState(!mLocked);
    mApp = app;
    lock();
    List<Object> argList = unpackArguments(args);
    readArgumentValues(argList);
  }

  /**
   * Throw a CmdLineArgs.Exception
   * 
   * @param message
   */
  public void fail(String message) {
    throw new CmdLineArgException(message);
  }

  @Deprecated // Does this need to be public?
  public void help() {
    help(null);
  }

  public void help(AppOper optionalOper) {
    checkState(mLocked);
    if (helpShown())
      return;

    StringBuilder sb = new StringBuilder();
    sb.append("\n");

    if (optionalOper != null) {
      BasePrinter b = new BasePrinter();
      optionalOper.getOperSpecificHelp(b);
      sb.append(b.toString());
    } else {
      auxHelp(sb);
    }

    mHelpHasBeenShown = true;
    System.out.println(sb.toString());
  }

  private void auxHelp(StringBuilder sb) {
    if (mBanner != null) {
      sb.append(mBanner);
      sb.append("\n\n");
    }
    int longestPhrase1Length = 0;
    List<String> phrases = arrayList();
    for (String key : mOptionList) {
      Option opt = mNamedOptionMap.get(key);
      StringBuilder sb2 = new StringBuilder();
      sb2.append("--" + opt.mLongName + ", -" + opt.mShortName);

      String typeStr = null;
      switch (opt.mType) {
      case T_INT:
        typeStr = "<n>";
        break;
      case T_DOUBLE:
        typeStr = "<f>";
        break;
      case T_STRING:
        typeStr = "<s>";
        break;
      }
      if (typeStr != null) {
        if (opt.mExpectedValueCount < 0) {
          sb2.append(" " + typeStr);
          sb2.append("...");
        } else {
          if (opt.mExpectedValueCount <= 3) {
            for (int i = 0; i < opt.mExpectedValueCount; i++)
              sb2.append(" " + typeStr);
          } else {
            sb2.append(" " + typeStr);
            sb2.append("..[" + opt.mExpectedValueCount + "]..");
            sb2.append(typeStr);
          }
        }
      }

      String phrase1 = sb2.toString();
      phrases.add(phrase1);
      longestPhrase1Length = Math.max(longestPhrase1Length, phrase1.length());

      String desc = opt.mDescription;

      if (opt.mDefaultValue != null) {
        var s = opt.mDefaultValue.toString();
        if (!s.isEmpty())
          desc += " [" + s + "]";
      }

      phrases.add(desc);
    }

    for (int j = 0; j < phrases.size(); j += 2) {
      String phrase1 = phrases.get(j);
      String phrase2 = phrases.get(j + 1);
      sb.append(spaces(longestPhrase1Length - phrase1.length()));
      sb.append(phrase1);
      sb.append(" :  ");
      sb.append(phrase2);
      sb.append("\n");
    }
  }

  public boolean helpShown() {
    return mHelpHasBeenShown;
  }

  private Option claimName(String name, Option forOwner) {
    if (mNamedOptionMap.containsKey(name))
      throw new IllegalArgumentException(
          "option already exists: " + name + ": " + mNamedOptionMap.get(name).mDescription);
    mNamedOptionMap.put(name, forOwner);
    return forOwner;
  }

  private Option findOption(String optionName) {
    Option opt = mNamedOptionMap.get(optionName);
    validate(opt != null, "unrecognized option", optionName);
    return opt;
  }

  private List<Object> unpackArguments(String[] args) {
    Pattern argumentsPattern = RegExp.pattern("^--?[a-z_A-Z][a-z_A-Z\\-]*$");
    List<Object> argList = arrayList();
    for (int cursor = 0; cursor < args.length; cursor++) {
      String arg = args[cursor];
      if (argumentsPattern.matcher(arg).matches()) {
        if (arg.startsWith("--")) {
          Option opt = findOption(arg.substring(2));
          opt.mInvocation = arg;
          argList.add(opt);
        } else {
          for (int i = 1; i < arg.length(); i++) {
            Option opt = findOption(arg.substring(i, i + 1));
            opt.mInvocation = arg;
            argList.add(opt);
          }
        }
        continue;
      }
      argList.add(arg);
    }
    return argList;
  }

  private void readArgumentValues(List<Object> args) {
    int cursor = 0;
    while (cursor < args.size()) {
      Object arg = args.get(cursor);
      cursor++;
      if (arg instanceof Option) {
        Option opt = (Option) arg;
        if (opt.mType == T_BOOL) {
          opt.addValue(Boolean.TRUE);
          if (opt.mLongName == HELP) {
            AppOper oper = null;

            // If there's a following argument that matches the name of an operation, 
            // generate operation-specific help.
            var app = mApp;
            if (app.hasMultipleOperations()) {
              if (cursor < args.size()) {
                var operArg = args.get(cursor).toString();
                oper = app.findOper(operArg);
                cursor++;
              }
            }
            help(oper);
            break;
          }
          continue;
        }

        while (true) {
          if (cursor == args.size())
            break;
          if (!opt.variableLengthArray() && !opt.isMissingValues())
            break;
          arg = args.get(cursor);
          if (arg instanceof Option)
            break;
          cursor++;
          String value = arg.toString();
          if (opt.mType == T_DOUBLE) {
            try {
              opt.addValue(Double.parseDouble(value));
            } catch (NumberFormatException e) {
              validate(false, "invalid argument " + value, opt.mInvocation);
            }
          } else if (opt.mType == T_INT) {
            try {
              opt.addValue(Integer.parseInt(value));
            } catch (NumberFormatException e) {
              validate(false, "invalid argument " + value, opt.mInvocation);
            }
          } else {
            opt.addValue(value);
          }
        }
        validate(!opt.isMissingValues(), "missing argument", opt.mInvocation);
      } else {
        mExtraArguments.add(arg.toString());
      }
    }
  }

  private static final int T_BOOL = 0;
  private static final int T_INT = 1;
  private static final int T_DOUBLE = 2;
  private static final int T_STRING = 3;

  /**
   * Throw a CmdLineArgs.Exception if a condition is false
   * 
   * @param condition
   * @param message
   *          message to include
   * @param arg
   *          optional argument to append to message
   */
  private static void validate(boolean condition, String message, String arg) {
    if (condition)
      return;
    if (arg != null)
      message += ": " + arg;
    throw new CmdLineArgException(message);
  }

  private void lock() {
    if (mLocked)
      return;
    var msg = "Show help";
    if (mApp.hasMultipleOperations())
      msg += " (\"help <opername>\" for a specific operation)";
    add(HELP).desc(msg);
    // Reserve the 'h' short name for the help option
    shortName("h");

    mLocked = true;
    chooseShortNames();
  }

  private void chooseShortNames() {
    for (String key : mOptionList) {
      Option opt = mNamedOptionMap.get(key);
      int j = 0;
      // If option has prefix "no", it's probably 'noXXX', so avoid
      // deriving short name from 'n' or 'o'
      if (key.startsWith("no"))
        j = 2;
      for (; opt.mShortName == null; j++) {
        if (j >= key.length()) {
          // Choose first unused character
          String poss = DataUtil.CHARS_ALPHA;
          for (int k = 0; k < poss.length(); k++) {
            String candidate = poss.substring(k, k + 1);
            if (!mNamedOptionMap.containsKey(candidate)) {
              claimName(candidate, opt).mShortName = candidate;
              break;
            }
          }
          break;
        }

        String candidate = key.substring(j, j + 1);
        if (!mNamedOptionMap.containsKey(candidate)) {
          claimName(candidate, opt).mShortName = candidate;
          break;
        }
        candidate = candidate.toUpperCase();
        if (!mNamedOptionMap.containsKey(candidate)) {
          claimName(candidate, opt).mShortName = candidate;
          break;
        }
      }
      validate(opt.mShortName != null, "can't find short name for", key);
    }
  }

  public static class CmdLineArgException extends RuntimeException {
    public CmdLineArgException(String msg) {
      super(msg);
    }
  }

  // ------------------------------------------------------------------
  // Additional functionality moved from App class
  // ------------------------------------------------------------------

  private String[] mExtraArgs;
  private int mExtraArgsCursor;
  private Map<String, Object> mArgValueMap = hashMap();
  private boolean mStillHandlingArgs;

  private int extraArgsCursor() {
    return mExtraArgsCursor;
  }

  private String[] extraArgs() {
    if (mExtraArgs == null)
      mExtraArgs = getExtras();
    return mExtraArgs;
  }

  public final boolean hasNextArg() {
    return extraArgsCursor() < extraArgs().length;
  }

  public final void assertArgsDone() {
    if (hasNextArg())
      fail("Unexpected extra argument(s): " + nextArg());
  }

  public final String peekNextArg() {
    if (!hasNextArg())
      fail("missing argument(s)");
    return extraArgs()[mExtraArgsCursor];
  }

  public final String nextArg() {
    String arg = peekNextArg();
    mExtraArgsCursor++;
    return arg;
  }

  public final String nextArg(String defaultValue) {
    String value = defaultValue;
    if (hasNextArg() || value == null)
      value = nextArg();
    return value;
  }

  /**
   * Convert a string argument to match the class of a value
   */
  private Object parseValueToMatchType(String value, Object valueOfType) {
    Object result;
    if (valueOfType instanceof Number) {
      if (valueOfType instanceof Integer)
        result = Integer.parseInt(value);
      else
        result = Float.parseFloat(value);
    } else if (valueOfType instanceof File) {
      result = new File(value);
    } else
      result = value;
    return result;
  }

  /**
   * Condition for argument parsing loop. While this returns true, arguments may
   * remain to be parsed. For example:
   * 
   * <pre>
   * 
   * int maxSpeed;
   * float accel;
   * boolean withTracking;
   * 
   * do {
   * 
   *   maxSpeed = nextArgIf(&quot;maxspeed&quot;, 100);
   *   accel = nextArgIf(&quot;accel&quot;, 9.8f);
   *   withTracking = nextArgIf(&quot;tracking&quot;);
   * 
   * } while (handlingArgs());
   *
   * </pre>
   */
  public final boolean handlingArgs() {
    mStillHandlingArgs = !mStillHandlingArgs;
    if (!mStillHandlingArgs) {
      ensureRequiredArgsProvided();
      if (hasNextArg())
        log("...done handling args; argument(s) remain:", peekNextArg());
    }
    return mStillHandlingArgs;
  }

  /**
   * Return TRUE and read next argument if it matches a string, otherwise FALSE
   */
  public final Boolean nextArgIf(String name) {
    return nextArgIf(name, false);
  }

  /**
   * Parse next value if its name matches a string, or return a default value.
   * Use the default's class to determine how to parse the value from the string
   * following the argument name.
   */
  public final <T extends Object> T nextArgIf(String name, T defaultValue) {
    checkNotNull(defaultValue);
    T effectiveDefault = (T) mArgValueMap.get(name);

    if (effectiveDefault == null) {
      effectiveDefault = defaultValue;
      mArgValueMap.put(name, defaultValue);
    }
    Object value = effectiveDefault;
    if (hasNextArg() && peekNextArg().equals(name)) {
      mStillHandlingArgs = false;
      nextArg();

      if (effectiveDefault != null) {
        if (effectiveDefault.getClass() == Boolean.class) {
          checkArgument(effectiveDefault == Boolean.FALSE, "illegal use of boolean default value");
          value = Boolean.TRUE;
        } else
          value = parseValueToMatchType(nextArg(), effectiveDefault);
        mArgValueMap.put(name, value);
      }
    }
    return (T) value;
  }

  private void ensureRequiredArgsProvided() {
    for (Map.Entry<String, Object> entry : mArgValueMap.entrySet()) {
      Object val = entry.getValue();
      if (isDefaultValue(val))
        fail("Missing argument: '" + entry.getKey() + "'");
    }
  }

  /**
   * Representation of a command line option
   */
  private static class Option {
    public Option(String longName) {
      mLongName = longName;
      // Until changed to something else, we will assume the type is boolean
      mType = T_BOOL;
    }

    public void setType(int type) {
      checkState(!mTypeDefined);
      mTypeDefined = true;
      mType = type;
    }

    public void setArray() {
      checkState(mTypeDefined && !mArray && mDefaultValue == null);
      mArray = true;
    }

    public void addValue(Object value) {
      if (!mArray)
        mValues.clear();
      mValues.add(value);
    }

    /**
     * Return true iff type is an array of variable length
     */
    public boolean variableLengthArray() {
      return mExpectedValueCount < 0;
    }

    /**
     * Return true if some values are missing (i.e. no value for scalar, or less
     * than required number for fixed length array)
     */
    public boolean isMissingValues() {
      return !variableLengthArray() && mValues.size() < mExpectedValueCount;
    }

    public Object valueOrDefault(String optionName) {
      Object value = mDefaultValue;
      if (!mValues.isEmpty())
        value = mValues.get(0);
      validate(value != null, "missing value", optionName);
      return value;
    }

    public boolean hasValue() {
      return !mValues.isEmpty();
    }

    public String mLongName;
    public String mShortName;
    public Object mDefaultValue;
    public String mDescription = "*** No description! ***";
    public int mType;
    public boolean mArray;
    // Number of values expected; -1 if variable-length array
    public int mExpectedValueCount = 1;
    public boolean mTypeDefined;
    public String mInvocation;
    public ArrayList<Object> mValues = arrayList();
  }

  private boolean mLocked;
  private String mBanner;
  private Option mOpt;
  private App mApp;
  private Map<String, Option> mNamedOptionMap = hashMap();
  private List<String> mExtraArguments = arrayList();
  private List<String> mOptionList = arrayList();
  private boolean mHelpHasBeenShown;

  // Optional argument values
  //
  public static final File OPT_FILE = Files.DEFAULT;

  // Required arguments.  These are specified by including one of these constant values.
  // If they are not replaced by subsequent parsing with some other value (which may be equal,
  // but not the same actual object), an error is generated.

  public static final File FILE = new File("");
  public static final String STRING = "~!~!~!~";
  public static final Integer INT = -999999998;
  public static final Float FLOAT = -9999999998.0f;

  public static boolean isDefaultValue(Object val) {
    return val == FILE || val == STRING || val == INT || val == FLOAT;
  }

}
