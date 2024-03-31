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
import java.util.Set;

import js.file.Files;
import js.json.JSMap;

/**
 * An Object subclass that supports conditional logging, naming, JSON conversion
 * and pretty printing
 */
public class BaseObject {

  private static final Boolean ISSUE19 = false && alert("Extra verbosity issue 19 (verbosity register)");

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

  /**
   * Determine if verbosity is set (ignoring register)
   */
  public final boolean verbose() {
    return mVerbose;
  }

  /**
   * Set verbosity true (ignoring register)
   */
  public final void setVerbose() {
    setVerbose(true);
  }

  /**
   * Set verbosity true with warning (ignoring register)
   */
  @Deprecated // Use the updateVerbose() method instead
  public final void alertVerbose() {
    setVerbose(true);
    alertWithSkip(1, name() + ": verbose is set");
  }

  /**
   * Set verbosity state (ignoring register)
   */
  public final void setVerbose(boolean state) {
    mVerbose = state;
  }

  /**
   * Update verbosity state from register (with a warning if new state is true)
   */
  public final void updateVerbose() {
    boolean flag = REGISTER.readVerbose(getClass(), 1);
    if (mVerbose != flag) {
      if (flag)
        alertWithSkip(1, name() + ": verbose is set in", REGISTER.registerFile().getName());
      setVerbose(flag);
    }
  }

  public static boolean updateVerbose(String key) {
    return REGISTER.readVerbose(key, 1);
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
   * is more recent than the last time we read it; and,
   * 
   * write new contents to filesystem if we have modified it since it was last
   * written.
   * 
   * Ideally, this should be called periodically from a background thread if it
   * is to 'watch' the filesystem copy for live updates. Otherwise, it should be
   * called before the program exits so the registry is updated with new keys
   * that its filesytem version doesn't yet know about.
   */
  public static void syncVerbosityRegister() {
    REGISTER.synchronizeWithFileSystem();
  }

  private static class VerbosityRegister {

    public boolean readVerbose(Object keyOwner, int skipFactor) {
      String key = getKey(keyOwner);
      Boolean flag = register().opt(key, (Boolean) null);
      mKnownKeysSet.add(key);
      if (flag == null) {
        flag = false;
        register().put(key, flag);
        mChangesMadeTime = System.currentTimeMillis();
        if (ISSUE19)
          pr("flag was missing:", key, "storing:", flag, "set changes made time:", mChangesMadeTime);
      }
      return flag;
    }

    public synchronized void synchronizeWithFileSystem() {
      File file = registerFile();
      long lastModified = file.lastModified();

      if (lastModified > mLastReadTime) {
        if (ISSUE19)
          pr("register last modified", lastModified, "> last read", mLastReadTime);
        rereadRegister();
        if (ISSUE19)
          pr("read new:", INDENT, mRegister);
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
      JSMap m = null;
      try {
        // Update the last read time before reading it, in case it has a parse problem
        mLastReadTime = registerFile().lastModified();
        m = JSMap.fromFileIfExists(registerFile());
        mRegister = m;
        boolean modified = false;
        for (String key : mKnownKeysSet) {
          if (!m.containsKey(key)) {
            m.put(key, false);
            modified = true;
          }
        }
        if (modified)
          mChangesMadeTime = System.currentTimeMillis();
        mKnownKeysSet.addAll(m.keySet());
        mLastLegalRegister = mRegister;
      } catch (Throwable t) {
        pr("*** problem parsing:", registerFile());
        mRegister = mLastLegalRegister;
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
      if (mRegisterFile == null) {
        // Attempt to find a project config subdirectory.  If not found, use a temporary file as the register file
        File dir = null;
        try {
          dir = Files.S.fileWithinProjectConfigDirectory("verbosity_register.json");
        } catch (IllegalArgumentException e) {
          System.out.println("caught: " + e);
        }
        if (dir == null) {
          dir = new File("_SKIP_register_file.json");
          if (!dir.exists())
            Files.S.writeString(dir, "{}");
        }
        mRegisterFile = dir;
      }
      return mRegisterFile;
    }

    private File mRegisterFile;
    private JSMap mRegister;
    private JSMap mLastLegalRegister = map();
    private long mLastReadTime;
    private long mChangesMadeTime;
    private Set<String> mKnownKeysSet = hashSet();
  }

  private static final VerbosityRegister REGISTER = new VerbosityRegister();

}
