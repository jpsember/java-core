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
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.parsing.RegExp;

/**
 * Maintains sets of rotating backups for files and directories
 * 
 * All files and directories being backed up by a manager must lie within a file
 * hierarchy
 */
public final class BackupManager extends BaseObject {

  // ------------------------------------------------------------------
  // Construction and configuration
  // ------------------------------------------------------------------

  /**
   * Constructor
   * 
   * @param filesObject
   *          Files instance to use, or null to use the default one (Files.S)
   * @param baseDirectory
   *          topmost directory; all files to be backed up must lie somewhere
   *          within its hierarchy
   * 
   */
  public BackupManager(Files filesObject, File baseDirectory) {
    mFiles = nullTo(filesObject, Files.S);
    mBaseDirectory = Files.assertNonEmpty(baseDirectory);
  }

  /**
   * Set the directory that will contain the backups. It need not lie within the
   * baseDirectory hierarchy. Initially set to <baseDirectory>/_SKIP_backups
   */
  public BackupManager withBackupRootDirectory(File backupRootDirectory) {
    assertMutable();
    mBackupRootDirectory = Files.fileWithinDirectory(backupRootDirectory, baseDirectory());
    return this;
  }

  public File makeBackup(String fileOrDirectory) {
    return makeBackup(new File(fileOrDirectory));
  }

  public BackupManager withMaxBackups(int max) {
    mMaxBackupsCount = max;
    return this;
  }

  /**
   * Make a backup of a file or directory
   */
  public File makeBackup(File fileOrDirectory) {
    log("make backup:", fileOrDirectory);
    Files.assertNonEmpty(fileOrDirectory);
    prepare();

    fileOrDirectory = Files.relativeToContainingDirectory(fileOrDirectory, baseDirectory());

    String relativePath = fileOrDirectory.toString();
    log("relative to backup directory:", INDENT, relativePath);
    fileOrDirectory = new File(baseDirectory(), relativePath);
    mSourceFileOrDirectory = fileOrDirectory;
    File backupFile = new File(backupDirectory(), relativePath);

    long timestamp = currentTime();
    File target;

    List<File> existingBackups = existingBackups(backupFile);
    {
      String backupRelativePath = relativePath + "." + timestamp;
      target = new File(backupDirectory(), backupRelativePath);

      // If the most recent existing backup is not older than our new timestamp, problem
      if (!existingBackups.isEmpty()) {
        File mostRecentBackup = last(existingBackups);
        long recentTimestamp = 0;
        try {
          String name = mostRecentBackup.getName();
          recentTimestamp = Long.parseLong(name.substring(1 + name.lastIndexOf('.')));
        } catch (Throwable t) {
          throw FileException.withMessage("failed to parse timestamp from:", mostRecentBackup);
        }
        if (recentTimestamp >= timestamp)
          throw FileException.withMessage("most recent backup is newer than current time:",
              Files.infoMap(mostRecentBackup));
      }
    }

    if (fileOrDirectory.isDirectory())
      mFiles.copyDirectory(fileOrDirectory, target);
    else
      mFiles.copyFile(fileOrDirectory, target);

    existingBackups.add(target);
    trimBackups(existingBackups, backupFile);

    return fileOrDirectory;
  }

  /**
   * Rebuild a directory (if it exists).
   * 
   * If directory already exists, make a backup of a directory, and create a new
   * one
   */
  public File backupAndDelete(File directory) {
    log("backupAndDelete:", directory);
    boolean oldExists = directory.exists();
    if (!oldExists) {
      mFiles.mkdirs(directory);
    } else {
      makeBackup(directory);
      Files.assertDirectoryExists(mSourceFileOrDirectory);
      mFiles.deleteDirectory(mSourceFileOrDirectory);
      mFiles.mkdirs(mSourceFileOrDirectory);
    }
    return directory;
  }

  // ------------------------------------------------------------------
  // Unit tests
  // ------------------------------------------------------------------

  /**
   * Public for tests only
   */
  public File getSourceRootDirectory() {
    testOnlyAssert();
    return baseDirectory();
  }

  // ------------------------------------------------------------------
  // For unit tests only
  // ------------------------------------------------------------------

  public File getBackupDirectory() {
    testOnlyAssert();
    return backupDirectory();
  }

  public void setCurrentTime(long timeMs) {
    testOnlyAssert();
    mCurrentTimeMs = timeMs;
  }

  // ------------------------------------------------------------------

  private long currentTime() {
    if (mCurrentTimeMs != null)
      return mCurrentTimeMs;
    return System.currentTimeMillis();
  }

  private static boolean stringIsTimestampSuffix(String expr) {
    return RegExp.patternMatchesString(BACKUP_PATTERN, expr);
  }

  private static final Pattern BACKUP_PATTERN = RegExp.pattern("\\.\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d\\d");

  private List<File> existingBackups(File backupPath) {
    String backupPathString = backupPath.toString();
    File parent = Files.parent(backupPath);
    List<File> allFiles = Files.files(parent);
    List<File> files = arrayList();
    for (File f : allFiles) {
      String rel = f.toString();
      if (!rel.startsWith(backupPathString)) {
        continue;
      }
      String suffix = rel.substring(backupPathString.length());
      if (!stringIsTimestampSuffix(suffix))
        continue;
      files.add(f);
    }
    return files;
  }

  private File baseDirectory() {
    return mBaseDirectory;
  }

  private void assertMutable() {
    if (mPrepared)
      throw badState("BackupManager has already been prepared");
  }

  private void prepare() {
    if (mPrepared)
      return;
    if (mBackupRootDirectory == null)
      withBackupRootDirectory(new File(baseDirectory(), "_SKIP_backups"));
    mPrepared = true;
  }

  private File backupDirectory() {
    return mBackupRootDirectory;
  }

  private void trimBackups(List<File> existingBackups, File backupFile) {
    int trimCount = Math.max(0, existingBackups.size() - mMaxBackupsCount);
    for (int i = 0; i < trimCount; i++) {
      File oldFile = existingBackups.get(i);
      // This is a 'just in case' test, which we can get rid of later
      {
        String x = oldFile.getPath();
        x = x.substring(x.length() - 14);
        if (!stringIsTimestampSuffix(x))
          throw badArg("file doesn't end with timestamp expression:", oldFile);
      }
      if (oldFile.isDirectory())
        mFiles.deleteDirectory(oldFile);
      else
        mFiles.deleteFile(oldFile);
    }
  }

  private final Files mFiles;
  private final File mBaseDirectory;
  private File mBackupRootDirectory;
  private boolean mPrepared;
  private int mMaxBackupsCount = 10;
  private File mSourceFileOrDirectory;
  private Long mCurrentTimeMs;

}
