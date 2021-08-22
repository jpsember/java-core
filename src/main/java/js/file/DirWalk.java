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

import static js.base.Tools.*;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.file.Files;
import js.parsing.RegExp;

public final class DirWalk extends BaseObject {

  public DirWalk(File directory) {
    mRootDirectory = directory.getAbsoluteFile();
    Files.assertDirectoryExists(mRootDirectory, "DirWalk");
    withRecurse(true);
    omitPrefixes(sDefaultOmitPrefixes);
  }

  /**
   * Determine whether to include directories within the list of files, or to
   * only report the non-directory files
   */
  public DirWalk includeDirectories() {
    assertMutable();
    mIncludeDirectoriesFlag = true;
    return this;
  }

  /**
   * Add a list of valid extensions. If list is nonempty, only files with such
   * extensions are included
   */
  public DirWalk withExtensions(Iterable<String> ext) {
    assertMutable();
    // TODO: these could be combined into a single regex
    for (String x : ext)
      mPatternsToInclude.add(Pattern.compile(".*\\." + RegExp.escapedForPattern(x)));
    return this;
  }

  /**
   * Add a list of valid extensions. If list is nonempty, only files with such
   * extensions are included
   */
  public DirWalk withExtensions(String... ext) {
    return withExtensions(arrayList(ext));
  }

  /**
   * Add a list of filenames to exclude
   */
  public DirWalk omitNames(Iterable<String> n) {
    assertMutable();
    for (String x : n)
      mPatternsToOmit.add(Pattern.compile(RegExp.escapedForPattern(x)));
    return this;
  }

  /**
   * Add a list of filenames to exclude
   */
  public DirWalk omitNames(String... n) {
    return omitNames(arrayList(n));
  }

  /**
   * Add a list of prefixes of files that should be omitted
   */
  public DirWalk omitPrefixes(Iterable<String> p) {
    assertMutable();
    for (String x : p) {
      Pattern pat = Pattern.compile(RegExp.escapedForPattern(x) + ".*");
      mPatternsToOmit.add(pat);
    }
    return this;
  }

  /**
   * Add a list of prefixes of files that should be omitted
   */
  public DirWalk omitPrefixes(String... p) {
    return omitPrefixes(arrayList(p));
  }

  /**
   * Get the root directory of this walker
   */
  public File directory() {
    return mRootDirectory;
  }

  /**
   * Determine whether to recurse into subdirectories; default is false
   */
  public DirWalk withRecurse(boolean f) {
    assertMutable();
    mRecurseIntoSubdirs = f;
    return this;
  }

  /**
   * Perform walk (if not already done), and return a list of filtered, absolute
   * File objects
   */
  public List<File> files() {
    if (mAbsoluteFilesList == null) {
      if (mPatternsToOmit.size() > sDefaultOmitPrefixes.length && !mPatternsToInclude.isEmpty())
        badState("include vs omit are exclusive options");

      mAbsoluteFilesList = arrayList();
      List<File> stack = arrayList();
      stack.add(mRootDirectory);
      while (!stack.isEmpty()) {
        File dir = pop(stack);
        boolean wasStart = (dir == mRootDirectory);
        log("...processing directory:", dir);

        if (mIncludeDirectoriesFlag && !wasStart)
          mAbsoluteFilesList.add(dir);
        if (!mRecurseIntoSubdirs && !wasStart)
          continue;

        outer: for (File f : Files.files(dir)) {
          String name = f.getName();

          if (!mPatternsToInclude.isEmpty()) {
            boolean include = false;
            if (f.isDirectory()) {
              // We always include directories -- only apply the pattern matching to files
              include = true;
            } else
              for (Pattern p : mPatternsToInclude) {
                if (p.matcher(name).matches()) {
                  include = true;
                  break;
                }
              }
            if (!include) {
              if (verbose())
                log("no matching pattern, omitting:", name);
              continue outer;
            }
          } else {
            for (Pattern p : mPatternsToOmit) {
              if (p.matcher(name).matches()) {
                if (verbose())
                  log("omitting:", name, "matches pattern:", p.pattern());
                continue outer;
              }
            }
          }

          if (verbose())
            log("including:", name);
          if (f.isDirectory()) {
            stack.add(f);
          } else {
            mAbsoluteFilesList.add(f);
          }
        }
      }
      mAbsoluteFilesList.sort(Files.COMPARATOR);
    }
    return mAbsoluteFilesList;
  }

  /**
   * Perform walk (if not already done), and return a list of filtered File
   * objects, relative to walk root directory
   */
  public List<File> filesRelative() {
    if (mRelativeFilesList == null) {
      mRelativeFilesList = arrayList();
      for (File f : files())
        mRelativeFilesList.add(Files.fileRelativeToDirectory(f, mRootDirectory));
    }
    return mRelativeFilesList;
  }

  /**
   * Convert a path relative to the walk directory to an absolute one
   */
  public File abs(File f) {
    String path = f.getPath();
    checkArgument(!path.startsWith("/"), "already absolute");
    return new File(mRootDirectory, path);
  }

  /**
   * Convert an absolute path to one relative to (and within) the walk directory
   */
  public File rel(File f) {
    Files.assertAbsolute(f);
    String path = f.getPath();
    String rootPath = mRootDirectory.getPath();
    if (!path.startsWith(rootPath))
      throw badArg("Path", path, "is not within tree", rootPath);
    return new File(path.substring(rootPath.length() + 1));
  }

  private void assertMutable() {
    if (locked())
      throw badState("DirWalk has already started");
  }

  private boolean locked() {
    return mAbsoluteFilesList != null;
  }

  private static final String[] sDefaultOmitPrefixes = { "_SKIP_", "_OLD_" };

  private final File mRootDirectory;
  private boolean mRecurseIntoSubdirs;
  private List<File> mRelativeFilesList;
  private List<File> mAbsoluteFilesList;
  private boolean mIncludeDirectoriesFlag;
  private Set<Pattern> mPatternsToOmit = hashSet();
  private Set<Pattern> mPatternsToInclude = hashSet();
}
