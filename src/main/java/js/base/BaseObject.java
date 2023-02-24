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

import java.io.File;

import js.file.Files;
import js.json.JSMap;

/**
 * An Object subclass that supports conditional logging, naming, JSON conversion
 * and pretty printing
 */
public class BaseObject {

  private static final Boolean ISSUE19 = true;

  // ------------------------------------------------------------------
  // Naming
  // ------------------------------------------------------------------

  public final String name() {
    if (!hasName())
      setName(nullToEmpty(supplyName()));
    return mName;
  }

  /**
   * Assign a name to the object if it doesn't already have one. Default
   * implementation uses class name. Called at most once per object instance
   * (excepting multithreading race conditions)
   */
  protected String supplyName() {
    return getClass().getSimpleName();
  }

  public final <T extends BaseObject> T setName(String name) {
    mName = checkNotNull(name);
    return (T) this;
  }

  protected boolean hasName() {
    return mName != null;
  }

  private String mName;

  //------------------------------------------------------------------
  // Verbosity
  // ------------------------------------------------------------------

  public final boolean verbose() {
    return mVerbose;
  }

  public final void setVerbose() {
    setVerbose(true);
  }

  public final void alertVerbose() {
    setVerbose(true);
    alertWithSkip(1, name() + ": verbose is set");
  }

  public final void setVerbose(boolean state) {
    mVerbose = state;
  }

  /**
   * Read verbosity from configuration file
   */
  public final void updateVerbose() {
    boolean flag = REGISTER.readVerbose(getClass(), 1);
    if (mVerbose != flag) {
      if (flag)
        alertWithSkip(1, name() + ": verbose is set in config file");
      setVerbose(flag);
    }
  }

  /**
   * Log messages if verbosity is in effect
   */
  public final void log(Object... messageObjects) {
    if (verbose()) {
      String name = name();
      if (!name.isEmpty())
        pr(insertStringToFront("(" + name() + ":)", messageObjects));
      else
        pr(messageObjects);
    }
  }

  private boolean mVerbose;

  // ------------------------------------------------------------------
  // JSON, String conversion
  // ------------------------------------------------------------------

  @Override
  public final String toString() {
    return toJson().prettyPrint();
  }

  /**
   * Get JSON description. Default implementation returns a map with a single
   * key "" mapped to the class name
   */
  public JSMap toJson() {
    JSMap m = new JSMap();
    if (!name().isEmpty())
      m.put("", name());
    return m;
  }

  /**
   * Reread the verbosity register from the filesystem if its modification time
   * is more recent than the last time we read it
   */
  public static void readVerbosityRegister() {
    REGISTER.refreshFromFileSystem();
  }

  private static class VerbosityRegister {

    public boolean readVerbose(Object keyOwner, int skipFactor) {
      String key = getKey(keyOwner);
      Boolean flag = register().opt(key, (Boolean) null);
      if (flag == null) {
        flag = false;
        register().put(key, flag);
        mChangesMadeTime = System.currentTimeMillis();
        if (ISSUE19)
          pr("flag was missing, storing:",flag,"set changes made time:",mChangesMadeTime);
        //  mLastReadTime = 0;
      }
      todo("add an explicit false key to the map if it is missing");
      return flag;
    }

    public synchronized void refreshFromFileSystem() {
      File file = registerFile();
      long lastModified = file.lastModified();
      if (false && ISSUE19)
        pr("refreshFromFileSystem, lastModified:", lastModified, "lastRead",mLastReadTime,"changes made",mChangesMadeTime);
  
      if (lastModified > mLastReadTime) {
        if (ISSUE19)
          pr("register last modified", lastModified, "> last read", mLastReadTime);
        rereadRegister();
        if (ISSUE19)
          pr("read new:", INDENT, mRegister);
        todo("populate missing keys with FALSE");
      } else {
        // If we have made changes to the registry since it was last read, write new contents
        if (mChangesMadeTime > mLastReadTime) {
          if (ISSUE19)
            pr("register changed time", mChangesMadeTime, "> last read", mLastReadTime,
                "writing new content:", INDENT, register());
          Files.S.writePretty(file, register());
          mChangesMadeTime = file.lastModified();
        }
      }
    }

    private String getKey(Object keyOwner) {
      if (keyOwner instanceof BaseObject) {
        return ((BaseObject) keyOwner).name();
      }
      if (keyOwner instanceof Class)
        return ((Class) keyOwner).getSimpleName();
      return keyOwner.toString();
    }

    private void rereadRegister() {
      // Don't update the register if the file has invalid content (e.g. in case it has
      //  been written to within an editor while still editing)
      //
      JSMap m = null;
      try {
        m = JSMap.fromFileIfExists(registerFile());
        mRegister = m;
        mLastReadTime = registerFile().lastModified();
      } catch (Throwable t) {
        alert("problem reading register:", registerFile(), "cause:", t.getMessage());
      }
    }

    private JSMap register() {
      if (mRegister == null) {
        synchronized (this) {
          if (mRegister == null)
            rereadRegister();
        }
      }
      return mRegister;
    }

    private File registerFile() {
      if (mRegisterFile == null)
        mRegisterFile = Files.S.fileWithinProjectConfigDirectory("verbosity_register.json");
      return mRegisterFile;
    }

    //
    //    private synchronized void updateFile() {
    //      Files.S.writePretty(registerFile(), register());
    //      if (ISSUE19)
    //        pr("just wrote:", registerFile(), INDENT, register());
    //    }
    //
    private File mRegisterFile;
    private JSMap mRegister;
    private long mLastReadTime;
    private long mChangesMadeTime;
  }

  private static final VerbosityRegister REGISTER = new VerbosityRegister();

}
