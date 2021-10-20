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

public final class BackupManager extends BaseObject {

  public BackupManager(Files filesObject, File sourceRootDirectory) {
    mFiles = nullTo(filesObject, Files.S);
    mSourceRootDirectory = Files.assertNonEmpty(sourceRootDirectory);
  }

  public BackupManager withBackupRootDirectory(File backupRootDirectory) {
    assertMutable();
    mBackupRootDirectory = Files.fileWithinDirectory(backupRootDirectory, sourceRootDirectory());
    return this;
  }

  public File makeBackup(String fileOrDirectory) {
    return makeBackup(new File(fileOrDirectory));
  }

  /**
   * Make a backup of a file or directory
   */
  public File makeBackup(File fileOrDirectory) {
    log("make backup:", fileOrDirectory);
    Files.assertNonEmpty(fileOrDirectory);
    prepare();

    fileOrDirectory = Files.relativeToContainingDirectory(fileOrDirectory, sourceRootDirectory());
    String relativePath = fileOrDirectory.toString();
    log("relative to backup directory:", INDENT, relativePath);
    fileOrDirectory = new File(sourceRootDirectory(), relativePath);
    mSourceFileOrDirectory = fileOrDirectory;
    File backupFile = new File(backupDirectory(), relativePath);

    long timestamp = determineTimestampOfFileOrDir(fileOrDirectory);
    todo("!don't backup the file or dir if an existing backup matches the target name");
    File target;

    List<File> existingBackups = existingBackups(backupFile);
    {
      String backupRelativePath = relativePath + "." + timestamp;
      target = new File(backupDirectory(), backupRelativePath);
      log("...backup file       :", target);
      if (target.exists()) {
        log("...already exists, no backup being made");
        return fileOrDirectory;
      }
      // If the most recent existing backup is the same age, do nothing.
      // If it's newer, fail
      if (!existingBackups.isEmpty()) {
        File mostRecentBackup = last(existingBackups);
        int compareResult = Files.COMPARATOR.compare(target, mostRecentBackup);
        log("...most recent backup:", mostRecentBackup);
        log("...compare result:", compareResult);
        if (compareResult == 0) {
          log("...file/directory timestamp is same as most recent backup, skipping.");
          return fileOrDirectory;
        }
        if (compareResult < 0) {
          throw badState("There is a newer backup file already!", INDENT, mostRecentBackup, CR, target);
        }
      }
    }

    if (fileOrDirectory.isDirectory()) {
      mFiles.copyDirectory(fileOrDirectory, target);
      mTargetBackupOrDirectory = target;
    } else
      mFiles.copyFile(fileOrDirectory, target);

    existingBackups.add(target);
    trimBackups(existingBackups, backupFile);

    return fileOrDirectory;
  }

  private long determineTimestampOfFileOrDir(File fileOrDirectory) {
    log("finding timestamp for:", fileOrDirectory);
    Long newestTimestamp = null;
    if (fileOrDirectory.isDirectory()) {
      for (File childFile : new DirWalk(fileOrDirectory).files()) {
        long mtime = childFile.lastModified();
        if (newestTimestamp == null || newestTimestamp < mtime) {
          newestTimestamp = mtime;
          log("......newest timestamp now:", mtime, "for:", INDENT, childFile);
        }
      }
      if (newestTimestamp == null) {
        newestTimestamp = fileOrDirectory.lastModified();
        log("...directory was empty");
      }
    } else {
      newestTimestamp = fileOrDirectory.lastModified();
    }
    log("...returning timestamp:", newestTimestamp);
    return newestTimestamp;
  }

  /**
   * Make a backup of a directory, and create a new one, optionally preserving a
   * set of files
   */
  public File backupAndDelete(File directory, String... preserveRelativeFiles) {
    log("backupAndDelete:", directory, "preserving:", preserveRelativeFiles);
    makeBackup(directory);
    Files.assertDirectoryExists(mSourceFileOrDirectory);
    mFiles.deleteDirectory(mSourceFileOrDirectory);
    mFiles.mkdirs(mSourceFileOrDirectory);
    for (String relFile : preserveRelativeFiles) {
      File source = new File(mTargetBackupOrDirectory, relFile);
      File dest = new File(mSourceFileOrDirectory, relFile);
      if (source.exists()) {
        log("...copying preserved file:", dest);
        mFiles.copyFile(source, dest, true);
      }
    }
    return directory;
  }

  public File getSourceRootDirectory() {
    testOnlyAssert();
    return sourceRootDirectory();
  }

  public File getBackupDirectory() {
    testOnlyAssert();
    return backupDirectory();
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

  private File sourceRootDirectory() {
    return mSourceRootDirectory;
  }

  private void assertMutable() {
    if (mPrepared)
      throw badState("BackupManager has already been prepared");
  }

  private void prepare() {
    if (mPrepared)
      return;
    if (mBackupRootDirectory == null)
      withBackupRootDirectory(new File(sourceRootDirectory(), "_SKIP_backups"));
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
  private final File mSourceRootDirectory;
  private File mBackupRootDirectory;
  private boolean mPrepared;
  private int mMaxBackupsCount = 5;
  private File mSourceFileOrDirectory;
  private File mTargetBackupOrDirectory;

}
