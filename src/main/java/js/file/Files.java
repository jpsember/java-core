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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.base.BasePrinter;
import js.file.DirWalk;
import js.data.AbstractData;
import js.json.JSMap;
import js.json.JSObject;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.NullOutputStream;

/**
 * Working with files. A lot of these methods exist only as replacements for the
 * standard Java File methods, to avoid having to deal with checked exceptions.
 * 
 * Methods that don't modify the filesystem are class methods.
 * 
 * Methods that *modify* the filesystem are instance methods of a 'Files' worker
 * instance, for easier support of an application's 'dryrun' option. There is a
 * default instance of the Files class, for convenience.
 */
public final class Files extends BaseObject {

  // ------------------------------------------------------------------
  // Construction of Files worker instances
  // ------------------------------------------------------------------

  public Files withVerbose(boolean flag) {
    setVerbose(flag);
    return this;
  }

  public Files withDryRun(boolean dryRun) {
    mDryRun = dryRun;
    return this;
  }

  public boolean dryRun() {
    return mDryRun;
  }

  private boolean mDryRun;

  // ------------------------------------------------------------------
  // Assertions, querying File objects
  // ------------------------------------------------------------------

  public static boolean nonEmpty(File file) {
    return !empty(file);
  }

  public static boolean empty(File file) {
    return file == null || file.getPath().isEmpty();
  }

  public static File ifEmpty(File file, File defValue) {
    if (empty(file))
      file = defValue;
    return file;
  }

  public static File ifEmpty(File file, String defValue) {
    return ifEmpty(file, new File(defValue));
  }

  public static File assertNonEmpty(File file) {
    return assertNonEmpty(file, null);
  }

  public static File assertNonEmpty(File file, String contextOrNull) {
    if (Files.empty(file))
      badArg("File is null or empty!", contextExpression(contextOrNull));
    return file;
  }

  public static File mustExist(File file) {
    assertNonEmpty(file);
    if (!file.exists())
      badArg("No such file:", file);
    return file;
  }

  public static File mustNotExist(File file) {
    assertNonEmpty(file);
    if (file.exists())
      badArg("File or directory already exists:", file);
    return file;
  }

  public static File assertDirectoryExists(File directory, String contextOrNull) {
    assertNonEmpty(directory, contextOrNull);
    if (!directory.isDirectory())
      badArg("Directory not found:", directory, contextExpression(contextOrNull));
    return directory;
  }

  public static File assertDirectoryExists(File directory) {
    return assertDirectoryExists(directory, null);
  }

  public static File assertFileLength(File file, long expected, String commentOrNull) {
    long actualLength = file.length();
    if (actualLength == expected)
      return file;
    throw die("File", file, "has unexpected length;", CR, "Description:",
        nullTo(commentOrNull, "(none given)"), CR, "Expected   :", expected, CR, "Actual     :",
        actualLength);
  }

  private static String contextExpression(String context) {
    return "context: " + ifNullOrEmpty(context, "(none)");
  }

  // ------------------------------------------------------------------
  // Reading & writing strings.  All strings use UTF-8
  // ------------------------------------------------------------------

  /**
   * Read string from file
   */
  public static String readString(File file) {
    try {
      return FileUtils.readFileToString(file, UTF8_CHARSET);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static String readString(File file, String defaultContents) {
    if (file.exists())
      return readString(file);
    return defaultContents;
  }

  /**
   * Read string from InputStream, then close the stream
   */
  public static String readString(InputStream inputStream) {
    try {
      return IOUtils.toString(inputStream, UTF8_CHARSET);
    } catch (IOException e) {
      throw asFileException(e);
    } finally {
      close(inputStream);
    }
  }

  /**
   * Read a string from a resource
   */
  public static String readString(Class theClass, String resourceName) {
    return readString(openResource(theClass, resourceName));
  }

  /**
   * Read a number of bytes from input stream
   */
  public static byte[] readBytes(InputStream inputStream, int length) {
    byte[] bytes = new byte[length];
    try {
      int bytesRead = inputStream.read(bytes);
      if (bytesRead != length)
        throw FileException.withMessage("readBytes, attempted to read", length, "but got result", bytesRead);
    } catch (IOException e) {
      throw asFileException(e);
    }
    return bytes;
  }

  /**
   * Write string to file
   */
  public void writeString(File file, String content) {
    if (verbose())
      log("Writing string to file:", file, "<==", debStr(content));
    if (dryRun())
      return;
    try {
      FileUtils.writeStringToFile(file, content, UTF8_CHARSET);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Write a string to a file, unless file exists and content is unchanged
   */
  public boolean writeIfChanged(File target, String content) {
    boolean changedFlag = true;
    if (target.exists()) {
      String existingContent = readString(target);
      changedFlag = !existingContent.equals(content);
    }
    if (changedFlag) {
      if (verbose())
        log("File has changed, writing new content:", target);
      writeString(target, content);
    }
    return changedFlag;
  }

  // ------------------------------------------------------------------
  // Pretty-print a JSON object 
  // ------------------------------------------------------------------

  public void writePretty(File file, JSObject json) {
    writeString(file, json.prettyPrint());
  }

  // ------------------------------------------------------------------
  // Pretty-print an AbstractData object 
  // ------------------------------------------------------------------

  public void write(File file, AbstractData abstractData) {
    writeString(file, abstractData.toJson().toString());
  }

  public boolean writeIfChanged(File target, AbstractData abstractData) {
    String content = abstractData.toJson().toString();
    return writeIfChanged(target, content);
  }

  public void writePretty(File file, AbstractData abstractData) {
    writePretty(file, abstractData.toJson());
  }

  // ------------------------------------------------------------------
  // Symlinks
  // ------------------------------------------------------------------

  public void createLink(File link, File target) {
    if (verbose())
      log("Creating link:", link, "==>", target);
    if (dryRun())
      return;
    Path targetPath = target.toPath().toAbsolutePath();
    try {
      java.nio.file.Files.createSymbolicLink(link.toPath(), targetPath);
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
  }

  // ------------------------------------------------------------------
  // File extensions
  // ------------------------------------------------------------------

  /**
   * Set extension of file (replacing any existing one). If extension is empty,
   * just trims existing extension (if any)
   */
  public static File setExtension(File file, String extension) {
    return new File(setExtension(file.getPath(), extension));
  }

  /**
   * Set extension (string argument)
   */
  public static String setExtension(String pathAsString, String extension) {
    String filePath = removeExtension(pathAsString);
    if (filePath.isEmpty())
      badArg("filename is empty without extension:", pathAsString);
    if (!nullOrEmpty(extension))
      filePath += FilenameUtils.EXTENSION_SEPARATOR_STR + extension;
    return filePath;
  }

  /**
   * Get extension of a file; return empty string if it has none
   */
  public static String getExtension(File file) {
    return getExtension(file.getPath());
  }

  /**
   * Get extension (string argument)
   */
  public static String getExtension(String filename) {
    return FilenameUtils.getExtension(filename);
  }

  /**
   * Add extension to file if it doesn't have one
   */
  public static File addExtension(File file, String extension) {
    if (getExtension(file).isEmpty())
      file = setExtension(file, extension);
    return file;
  }

  /**
   * Add extension to file if it doesn't have one
   */
  public static String addExtension(String filename, String extension) {
    if (getExtension(filename).isEmpty())
      return setExtension(filename, extension);
    return filename;
  }

  /**
   * Remove extension, if any, from path
   */
  public static File removeExtension(File file) {
    return setExtension(file, "");
  }

  /**
   * Remove extension (string argument)
   */
  public static String removeExtension(String filename) {
    return FilenameUtils.removeExtension(filename);
  }

  /**
   * Get the basename of a file, which omits directories and any extension
   */
  public static String basename(File file) {
    return basename(file.getPath());
  }

  /**
   * Get the basename of a file (string argument)
   */
  public static String basename(String path) {
    String result = FilenameUtils.getBaseName(path);
    if (result.isEmpty())
      badArg("basename for:", path);
    return result;
  }

  // ------------------------------------------------------------------
  // Relative and absolute paths
  // ------------------------------------------------------------------

  /**
   * Ensure a file is absolute
   */
  public static File assertAbsolute(File file) {
    assertNonEmpty(file);
    if (!file.isAbsolute())
      badArg("expected absolute file, but was", quote(file));
    return file;
  }

  /**
   * Ensure a file is relative
   */
  public static File assertRelative(File file, Object... errorMessages) {
    if (assertNonEmpty(file).isAbsolute())
      badArg(insertStringToFront(BasePrinter.toString("expected relative file, but was", quote(file)),
          errorMessages));
    return file;
  }

  /**
   * Get absolute form of file
   */
  public static File absolute(File file) {
    assertNonEmpty(file, "absolute");
    return file.getAbsoluteFile();
  }

  /**
   * Get parent of file; throw exception if it has none (instead of returning
   * null)
   */
  public static File parent(File file) {
    File parent = file.getParentFile();
    if (parent == null)
      throw badArg("File has no parent:", file);
    return parent;
  }

  /**
   * If file is not absolute, and a parent directory is specified, join them;
   * else return file
   */
  public static File fileWithinOptionalDirectory(File file, File parentDirectoryOrNull) {
    assertNonEmpty(file, "fileWithinOptionalDirectory");
    if (!file.isAbsolute() && nonEmpty(parentDirectoryOrNull))
      return new File(parentDirectoryOrNull, file.getPath());
    return file;
  }

  /**
   * Get file expressed relative to a directory, if one provided
   */
  public static File fileRelativeToDirectory(File absFile, File absDirectoryOrNull) {
    assertAbsolute(absFile);
    if (absDirectoryOrNull == null)
      return absFile;
    assertAbsolute(absDirectoryOrNull);
    Path dirPath = absDirectoryOrNull.toPath();
    Path filePath = absFile.toPath();
    return dirPath.relativize(filePath).toFile();
  }

  public static File getCanonicalFile(File file) {
    try {
      return file.getCanonicalFile();
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Get alphabetically-sorted list of files within a directory
   */
  public static List<File> files(File directory) {
    List<File> files = arrayList();
    File[] auxFiles = directory.listFiles();
    if (auxFiles != null) {
      for (File name : auxFiles)
        files.add(name);
      files.sort(COMPARATOR);
    }
    return files;
  }

  /**
   * Get all files in a directory that have a particular extension. Does not
   * recurse into subdirectories
   */
  public static List<File> filesWithExtension(File directory, String extension) {
    return new DirWalk(directory).withRecurse(false).withExtensions(extension).files();
  }

  // ------------------------------------------------------------------
  // Exceptions
  // ------------------------------------------------------------------

  /**
   * Wrap a throwable within our (unchecked) FileException, if it isn't one
   * already
   */
  public static FileException asFileException(Throwable t) {
    if (t instanceof FileException)
      return (FileException) t;
    return FileException.withCause(t);
  }

  //------------------------------------------------------------------
  // System properties
  // ------------------------------------------------------------------

  public static File currentDirectory() {
    if (sCurrentDir == null) {
      sCurrentDir = new File(System.getProperty("user.dir"));
    }
    return sCurrentDir;
  }

  public static File homeDirectory() {
    if (sHomeDir == null) {
      sHomeDir = new File(System.getProperty("user.home"));
    }
    return sHomeDir;
  }

  public static File fileRelativeToCurrent(File file) {
    return fileRelativeToDirectory(file, currentDirectory());
  }

  private static File sCurrentDir;
  private static File sHomeDir;

  /**
   * Get the OSX Desktop directory
   */
  public static File getDesktopDirectory() {
    return sDesktopDirectory.get();
  }

  /**
   * Get file within OSX Desktop
   */
  public static File getDesktopFile(String relativePath) {
    return new File(getDesktopDirectory(), relativePath);
  }

  private static Supplier<File> sDesktopDirectory = singleton(() -> {
    return assertDirectoryExists(new File(FileUtils.getUserDirectory(), "Desktop"));
  });

  public static File repoDir() {
    return sRepoDir.get();
  }

  private static Supplier<File> sRepoDir = singleton(() -> {
    String userDir = currentDirectory().getPath();
    String repoHome;
    // If running within Docker container, or something similar, return the home directory
    //
    if (userDir.startsWith("/home/ubuntu")) {
      repoHome = "/home/ubuntu";
    } else {
      // Assume we are running in a subdirectory of the repo's "source" directory
      int i = userDir.indexOf("/source/");
      checkState(i >= 0, "can't find $REPO_HOME, nor find '/source/' within userDir: " + userDir);
      repoHome = userDir.substring(0, i);
    }
    return new File(repoHome).getAbsoluteFile();
  });

  // ------------------------------------------------------------------
  // Closeables and AutoClosables
  // ------------------------------------------------------------------

  /**
   * If a Closeable is not null, attempt to close it; if there's an exception,
   * catch it and report a todo
   */
  public static <T extends AutoCloseable> T closePeacefully(T c) {
    if (c != null) {
      try {
        c.close();
      } catch (Exception e) {
        pr("caught:", e);
      }
    }
    return null;
  }

  /**
   * If a Closeable is not null, attempt to close it; throw unchecked exception
   * if problem
   */
  public static <T extends Closeable> T close(T c) {
    if (c != null) {
      try {
        c.close();
      } catch (IOException e) {
        throw asFileException(e);
      }
    }
    return null;
  }

  /**
   * If a Closeable is not null, attempt to close it; throw unchecked exception
   * if problem
   */
  public static void close(Closeable... closeables) {
    try {
      for (Closeable c : closeables) {
        if (c != null)
          c.close();
      }
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static void flush(Flushable flushable) {
    if (flushable != null) {
      try {
        flushable.flush();
      } catch (IOException e) {
        throw asFileException(e);
      }
    }
  }

  // ------------------------------------------------------------------
  // Creating, deleting files and directories
  // ------------------------------------------------------------------

  public File mkdirs(File dir) {
    try {
      log("mkdirs:", dir);
      if (!dryRun())
        FileUtils.forceMkdir(dir);
      return dir;
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public File remakeDirs(File dir) {
    deleteDirectory(dir);
    return mkdirs(dir);
  }

  public void deleteFile(File file) {
    // TODO: exists() doesn't return true if it's a symbolic link
    if (!file.exists())
      return;
    log("Delete file:", file);
    if (dryRun())
      return;
    boolean deleted = file.delete();
    if (!deleted)
      throw FileException.withMessage("Failed to delete file:", file);

  }

  public File deletePeacefully(File file) {
    if (file != null) {
      try {
        deleteFile(file);
      } catch (Throwable t) {
        pr("caught:", t);
      }
    }
    return null;
  }

  public void deleteDirectory(File dir) {
    try {
      assertNonEmpty(dir, "deleteDirectory");
      {
        // As a safety precaution, we will fail if the length of the absolute path is too short
        String str = dir.getPath();
        if (str.length() < 30)
          str = dir.getAbsolutePath();
        if (str.length() < 30)
          die("Can't delete directory whose length is too short;", dir);
      }
      log("Delete directory:", dir);
      if (!dryRun())
        FileUtils.deleteDirectory(dir);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public void setModifiedTime(File file, long timeMs) {
    Path p = file.toPath();
    try {
      java.nio.file.Files.setAttribute(p, "lastModifiedTime", FileTime.fromMillis(timeMs));
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  // ------------------------------------------------------------------
  // Reading, writing binary files (byte arrays)
  // ------------------------------------------------------------------

  /**
   * Read file to byte array
   */
  public static byte[] toByteArray(File path) {
    try {
      return toByteArray(new FileInputStream(path));
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Read InputStream to byte array
   */
  public static byte[] toByteArray(InputStream stream) {
    try {
      byte[] data = IOUtils.toByteArray(stream);
      stream.close();
      return data;
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Write byte array to file
   */
  public void write(byte[] data, File path) {
    if (verbose())
      log("write bytes, length:", data.length, "to:", path);
    if (dryRun())
      return;
    OutputStream s = null;
    try {
      s = outputStream(path);
      IOUtils.write(data, s);
    } catch (IOException e) {
      throw asFileException(e);
    } finally {
      closePeacefully(s);
    }
  }

  /**
   * Write byte array to stream
   */
  public void write(byte[] data, OutputStream s) {
    if (verbose())
      log("write bytes to stream, length:", data.length);
    if (dryRun())
      return;
    try {
      IOUtils.write(data, s);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Write a number of floats to output stream, little-endian
   */
  public void writeFloatsLittleEndian(float[] floats, OutputStream outputStream) {
    ByteBuffer byteBuffer = ByteBuffer.allocate(floats.length * Float.BYTES).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.asFloatBuffer().put(floats);
    write(byteBuffer.array(), outputStream);
  }

  /**
   * Read a number of floats from input stream, little-endian
   */
  public static float[] readFloatsLittleEndian(InputStream inputStream, int length) {
    byte[] bytes = readBytes(inputStream, length * Float.BYTES);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] dest = new float[length];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  /**
   * Read a number of floats from input stream, big-endian
   */
  public static float[] readFloatsBigEndian(InputStream inputStream, int length) {
    byte[] bytes = readBytes(inputStream, length * Float.BYTES);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
    float[] dest = new float[length];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  /**
   * Read a number of floats from file, little-endian
   */
  public static float[] readFloatsLittleEndian(File file) {
    byte[] bytes = toByteArray(file);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] dest = new float[bytes.length / Float.BYTES];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  public void writeFloatsLittleEndian(float[] floats, File file) {
    OutputStream outs = outputStream(file);
    writeFloatsLittleEndian(floats, outs);
    close(outs);
  }

  // ------------------------------------------------------------------
  // Copying files, directories
  // ------------------------------------------------------------------

  public void copyFile(File sourceFile, File destFile) {
    copyFile(sourceFile, destFile, true);
  }

  public void copyFile(File sourceFile, File destFile, boolean preserveFileDate) {
    if (verbose())
      log("copy file:", sourceFile, "to:", destFile);
    if (dryRun())
      return;
    try {
      FileUtils.copyFile(sourceFile, destFile, preserveFileDate);
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
  }

  public void copyDirectory(File srcDir, File destDir) {
    if (verbose())
      log("Copy directory", srcDir, "==>", destDir);
    if (dryRun())
      return;
    try {
      FileUtils.copyDirectory(srcDir, destDir, true);
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
  }

  public void moveFile(File sourceFile, File destFile) {
    if (verbose())
      log("Move file", sourceFile, "==>", destFile);
    if (dryRun())
      return;
    try {
      FileUtils.moveFile(sourceFile, destFile);
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
  }

  public void moveDirectory(File sourceFile, File destFile) {
    if (verbose())
      log("Move directory", sourceFile, "==>", destFile);
    if (dryRun())
      return;
    try {
      FileUtils.moveDirectory(sourceFile, destFile);
    } catch (IOException e) {
      throw Files.asFileException(e);
    }
  }

  /**
   * Return true if in dry run mode and a file doesn't exist. So we can avoid
   * throwing exceptions when we otherwise would expect the file (or directory)
   * to exist
   */
  public boolean missingWithDryRunActive(File file) {
    return dryRun() && !file.exists();
  }

  /**
   * Make a backup of a directory, and create a new one, optionally preserving a
   * set of files
   */
  public File rebuild(File directory, String... preserveRelativeFiles) {
    if (dryRun())
      throw notSupported("Should we have a null output stream?");
    throw notSupported("do we need to construct a BackupManager?");
    //    return BackupManager.get().rebuild(directory, preserveRelativeFiles);
  }

  /**
   * Find a file within a project directory structure, by ascending to a parent
   * directory until found (or we run out of parents)
   * 
   * @param startParentDirectoryOrNull
   *          directory to start search within, or null for current directory
   * @param filename
   *          name of file (or directory) to look for
   * @param errorContext
   *          if nonempty, and file isn't found, throws exception, displaying a
   *          message with these arguments
   * @return found file, or Files.DEFAULT
   */
  public static File getFileWithinParents(File startParentDirectoryOrNull, String filename,
      Object... errorContext) {
    File startDir;
    if (startParentDirectoryOrNull == null)
      startDir = Files.currentDirectory();
    else
      startDir = Files.absolute(startParentDirectoryOrNull);
    File result = Files.DEFAULT;
    File dir = startDir;
    while (true) {
      File candidate = new File(dir, filename);
      if (candidate.exists()) {
        result = candidate;
        break;
      }
      dir = dir.getParentFile();
      if (dir == null)
        break;
    }
    if (Files.empty(result) && errorContext.length > 0)
      throw badArg("Cannot find file", filename, "within", startDir, "; context:",
          BasePrinter.toString(errorContext));
    return result;
  }

  public File projectConfigDirectory() {
    if (mProjectConfigDirectory == null) {
      mProjectConfigDirectory = getFileWithinParents(null, "project_config", "project_config directory");
    }
    return mProjectConfigDirectory;
  }

  public File projectSecretsDirectory() {
    if (mProjectSecretsDirectory == null) {
      mProjectSecretsDirectory = getFileWithinParents(null, "secrets", "project_config directory");
    }
    return mProjectSecretsDirectory;
  }

  private File mProjectSecretsDirectory;
  private File mProjectConfigDirectory;

  // ------------------------------------------------------------------
  // Streams
  // ------------------------------------------------------------------

  public static FileInputStream openInputStream(File file) {
    try {
      return FileUtils.openInputStream(file);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public FileOutputStream outputStream(File file) {
    if (verbose())
      log("opening output stream for:", file);
    if (dryRun())
      throw notSupported("Should we have a null output stream?");
    try {
      return new FileOutputStream(file);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static DataInputStream dataInputStream(File file) {
    return new DataInputStream(openInputStream(file));
  }

  /**
   * Get an input stream to a resource, which is stored in the class folder (or
   * one of its subfolders)
   */
  public static BufferedInputStream openResource(Class theClass, String resourceName) {
    try {
      InputStream is = theClass.getResourceAsStream(resourceName);
      if (is == null)
        throw die("stream returned null");
      return new BufferedInputStream(is);
    } catch (Throwable e) {
      pr("Failed to open resource for class:", theClass, "name:", resourceName);
      throw asFileException(e);
    }
  }

  // ------------------------------------------------------------------
  // Parsing data 
  // ------------------------------------------------------------------

  public static <T extends AbstractData> T parseAbstractData(T prototype, File file) {
    mustExist(file);
    return parseAbstractDataOpt(prototype, file);
  }

  public static <T extends AbstractData> T parseAbstractDataOpt(T prototype, File file) {
    try {
      JSMap json = JSMap.fromFileIfExists(file);
      return parseAbstractDataOpt(prototype, json);
    } catch (Throwable t) {
      throw FileException.withCause(t, "Problem with file:", file, CR, t);
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractData> T parseAbstractDataOpt(T prototype, JSMap json) {
    try {
      T result = (T) prototype.build();
      if (!json.isEmpty())
        result = (T) result.parse(json);
      return result;
    } catch (Throwable t) {
      throw FileException.withCause(t, "Problem with json map:", INDENT, json, CR, t);
    }
  }
  // ------------------------------------------------------------------
  // Logging information
  // ------------------------------------------------------------------

  /**
   * Get a JSMap describing a file, whether it exists, its content, etc.
   */
  public static JSMap infoMap(File file) {
    JSMap m = map();
    if (file == null)
      return m.put("", "<null>");
    if (empty(file))
      return m.put("", "<empty>");

    String val = "MISSING";
    if (file.exists()) {
      if (file.isDirectory())
        val = "DIRECTORY";
      else
        val = "FILE";
    }
    m.put("1 status", val);

    String absPath;
    if (file.isAbsolute()) {
      m.put("2 name", file.getName());
      m.put("3 parent", file.getParent());
      absPath = file.toString();
    } else {
      String relName = file.toString();
      File curr = Files.currentDirectory();
      m.put("2 rel", relName);
      m.put("3 cdir", curr.toString());
      absPath = new File(curr, relName).toString();
    }
    m.put("4 abs", absPath);
    return m;
  }

  /**
   * Version of infoMap() for string parameter
   */
  public static JSMap info(String file) {
    File f = null;
    if (file != null)
      f = new File(file);
    return infoMap(f);
  }

  // ------------------------------------------------------------------
  // Zip files
  // ------------------------------------------------------------------

  public static List<ZipEntry> getZipEntries(ZipFile zipFileObj) {
    List<ZipEntry> output = arrayList();
    Enumeration<? extends ZipEntry> entries = zipFileObj.entries();
    while (entries.hasMoreElements()) {
      ZipEntry entry = entries.nextElement();
      output.add(entry);
    }
    return output;
  }

  public static void unzip(File zipFile, File targetDirectoryOrNull, Predicate<File> filterOrNull) {
    File targetDirectory = targetDirectoryOrNull;
    if (targetDirectoryOrNull == null)
      targetDirectory = zipFile.getParentFile();

    ZipFile zipFileObj = null;
    try {
      zipFileObj = new ZipFile(zipFile);
      for (ZipEntry entry : getZipEntries(zipFileObj)) {
        File entryDestination = new File(targetDirectory, entry.getName());
        if (filterOrNull != null && !filterOrNull.test(entryDestination))
          continue;
        if (entry.isDirectory()) {
          entryDestination.mkdirs();
        } else {
          entryDestination.getParentFile().mkdirs();
          InputStream in = zipFileObj.getInputStream(entry);
          OutputStream out = new FileOutputStream(entryDestination);
          IOUtils.copy(in, out);
          Files.closePeacefully(in);
          out.close();
        }
      }
    } catch (IOException e) {
      throw asFileException(e);
    } finally {
      Files.close(zipFileObj);
    }
  }

  // ------------------------------------------------------------------
  // Constants
  // ------------------------------------------------------------------

  /**
   * The default worker instance (dryrun should always be false!)
   */
  public static final Files S = new Files();

  // Eclipse has an annoying habit of not displaying 'S', so let's try this:
  public static final Files z = S;

  /**
   * The default File instance, implementing the 'null object' pattern
   */
  public static final File DEFAULT = new File("");

  public static final DataOutputStream NULL_DATA_OUTPUT_STREAM = new DataOutputStream(
      NullOutputStream.NULL_OUTPUT_STREAM);

  public static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

  /**
   * Comparator to sort files alphabetically
   */
  public static final Comparator<File> COMPARATOR = (File x, File y) -> x.getPath().compareTo(y.getPath());

  // ------------------------------------------------------------------
  // Hash functions for files (for test purposes)
  // ------------------------------------------------------------------

  /**
   * Register a file type / function
   */
  public static void registerFiletypeHashFn(String ext, Function<File, Object> fn) {
    sHashFnMap.put(ext, fn);
  }

  /**
   * If there's a registered hash calculator function for a filename, call it
   * and return the result; else return null
   */
  public static Object tryHash(File file) {
    String ext = getExtension(file);
    Function<File, Object> fn = sHashFnMap.get(ext);
    if (fn == null)
      return null;
    return fn.apply(file);
  }

  private static Map<String, Function<File, Object>> sHashFnMap = concurrentHashMap();

  // ------------------------------------------------------------------
  // File extensions
  // ------------------------------------------------------------------

  public static final String EXT_JSON = "json";

}
