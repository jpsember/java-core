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
import js.base.SystemCall;
import js.data.AbstractData;
import js.data.DataUtil;
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

  public static File assertExists(File file) {
    return assertExists(file, null);
  }

  public static File assertExists(File file, String contextOrNull) {
    assertNonEmpty(file, contextOrNull);
    if (!file.exists())
      badArg("No such file:", file, contextExpression(contextOrNull));
    return file;
  }

  public static File assertDoesNotExist(File file, String contextOrNull) {
    assertNonEmpty(file);
    if (file.exists())
      badArg("File or directory already exists:", file, contextExpression(contextOrNull));
    return file;
  }

  public static File assertDirectoryIfExists(File directory, String contextOrNull) {
    Files.assertNonEmpty(directory, contextOrNull);
    String ext = Files.getExtension(directory);
    if (!ext.isEmpty())
      badArg("unexpected extension for directory:", directory, Files.contextExpression(contextOrNull));
    if (directory.exists()) {
      if (!directory.isDirectory())
        badArg("Not a directory:", directory, Files.contextExpression(contextOrNull));
    }
    return directory;
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

  public static String contextExpression(String context) {
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
   * Read a byte array from a resource
   */
  public static byte[] toByteArray(Class theClass, String resourceName) {
    return Files.toByteArray(openResource(theClass, resourceName), resourceName);
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

  //------------------------------------------------------------------
  // Pretty-print an AbstractData object 
  // ------------------------------------------------------------------

  public void writePretty(File file, AbstractData abstractData) {
    writePretty(file, abstractData.toJson());
  }

  //------------------------------------------------------------------
  // Write AbstractData object to file, without pretty printing
  // ------------------------------------------------------------------

  public void write(File file, AbstractData abstractData) {
    writeString(file, abstractData.toJson().toString());
  }

  public void writeWithPrettyIf(File file, AbstractData abstractData, boolean prettyFlag) {
    if (prettyFlag)
      writePretty(file, abstractData);
    else
      write(file, abstractData);
  }

  public boolean writeIfChanged(File target, AbstractData abstractData) {
    String content = abstractData.toJson().toString();
    return writeIfChanged(target, content);
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

  public static File addExpectedExtension(File file, String ext) {
    checkArgument(!checkNonEmpty(ext).startsWith("."));
    String currentExt = Files.getExtension(file);
    if (currentExt.isEmpty())
      currentExt = ext;
    checkArgumentsEqual(ext, currentExt, "Expected extension", ext, "for file:", file);
    return Files.addExtension(file, ext);
  }

  public static File addTempSuffix(File file) {
    return new File(file.toString() + "." + EXT_TMP);
  }

  public static File removeTempSuffix(File file) {
    String x = file.toString();
    String y = chomp(x, "." + EXT_TMP);
    checkArgument(y.length() < x.length(), "missing temp suffix");
    return new File(y);
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

  public static File join(File parent, String child) {
    return join(parent, new File(child));
  }

  public static File join(File parent, File child) {
    assertRelative(child);
    return new File(assertNonEmpty(parent), child.toString());
  }

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

  public static String assertRelative(String path, Object... errorMessages) {
    assertRelative(new File(path), errorMessages);
    return path;
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
   * If file is not absolute, and a parent directory is specified, return new
   * File(parent, file); else, return file
   */
  public static File fileWithinOptionalDirectory(File file, File parentDirectoryOrNull) {
    if (parentDirectoryOrNull != null)
      return fileWithinDirectory(file, parentDirectoryOrNull);
    return assertNonEmpty(file, "fileWithinOptionalDirectory");
  }

  /**
   * If file is not absolute, express it relative to a directory
   */
  public static File fileWithinDirectory(File file, File parentDirectory) {
    assertNonEmpty(file, "fileWithinDirectory");
    if (!file.isAbsolute()) {
      return join(parentDirectory, file);
    }
    return file;
  }

  /**
   * Given a file and a directory containing the file, return file relative to
   * the directory. Throw exception if file is not (strictly) within the
   * directory.
   */
  public static File relativeToContainingDirectory(File file, File container) {

    container = getCanonicalFile(container);
    File absFile = getCanonicalFile(file);

    // Note: calling toString() on a File is a very efficient operation (from what I can tell)
    //
    String canonicalPath = absFile.toString();
    String containerPath = container.toString();

    // Include the trailing '/' in the prefix
    int prefixLength = containerPath.length() + 1;
    if (!canonicalPath.startsWith(containerPath) || canonicalPath.length() <= prefixLength)
      badArg("file is not strictly within container directory:", file, INDENT, container);

    return new File(canonicalPath.substring(prefixLength));
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
      return assertNonEmpty(file).getCanonicalFile();
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

  // ------------------------------------------------------------------
  // Locating executable files (when running within Eclipse, it has
  // a lot of trouble finding certain executables; I guess the PATH
  // is missing some stuff).
  // ------------------------------------------------------------------

  /**
   * Attempt to locate a program file. If eclipse is false, just returns the
   * name.
   */
  public static File programPath(String name) {
    File f = null;
    {
      f = sProgramPathMap.get(name);
      if (f == null) {
        f = findProgramPath(name);
        sProgramPathMap.put(name, f);
      }
    }
    //pr("programPath for:",name,"is:",Files.infoMap(f));
    return f;
  }

  private static File findProgramPath(String progName) {
    String[] dirs = { "/usr/local/bin", "/opt/homebrew/bin", };
    File progPath = null;
    for (var d : dirs) {
      var c = new File(d, progName);
      if (c.exists()) {
        progPath = c;
        break;
      }
    }
    if (progPath == null) {
      progPath = new File(progName);
    }
    return progPath;
  }

  private static Map<String, File> sProgramPathMap = hashMap();

  public static File binDirectory() {
    if (sBinDir == null) {
      var d = new File(Files.homeDirectory(), "bin");
      Files.assertDirectoryExists(d, "please create a 'bin' subdirectory in the home directory");
      sBinDir = d;
    }
    return sBinDir;
  }

  public static File fileRelativeToCurrent(File file) {
    return fileRelativeToDirectory(file, currentDirectory());
  }

  private static File sCurrentDir;
  private static File sHomeDir;
  private static File sBinDir;

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
    return join(getDesktopDirectory(), relativePath);
  }

  private static Supplier<File> sDesktopDirectory = singleton(() -> {
    File candidate = join(FileUtils.getUserDirectory(), "Desktop");
    if (!candidate.exists()) {
      alert("Desktop directory not found, creating one:", INDENT, candidate);
      mkdirHelper(candidate);
    }
    return assertDirectoryExists(candidate);
  });

  public static File repoDir() {
    return sRepoDir.get();
  }

  private static Supplier<File> sRepoDir = singleton(() -> {
    File currentDirectory = currentDirectory();
    return parent(
        getFileWithinParents(currentDirectory, ".git", "git repository containing", currentDirectory));
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
    if (verbose() && !dir.isDirectory())
      log("mkdirs:", dir);
    if (!dryRun())
      mkdirHelper(dir);
    return dir;
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
        final int MIN_PATH_LENGTH = 16;
        // As a safety precaution, we will fail if the length of the absolute path is too short
        String str = dir.getPath();
        if (str.length() < MIN_PATH_LENGTH)
          str = dir.getAbsolutePath();
        if (str.length() < MIN_PATH_LENGTH && !str.endsWith("/gen"))
          die("Can't delete directory whose length is too short;", dir);
      }
      log("Delete directory:", dir);
      if (!dryRun())
        FileUtils.deleteDirectory(dir);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Delete a directory, but for safety, ensure its basename includes a
   * particular substring
   */
  public void deleteDirectory(File dir, String requiredSubstring) {
    assertNonEmpty(dir, "deleteDirectory");
    var s = dir.toString();
    checkArgument(requiredSubstring.length() > 3, "required substring is too short");
    checkArgument(s.contains(requiredSubstring), "attempt to delete directory", dir,
        "that doesn't contain the required substring", quote(requiredSubstring));
    deleteDirectory(dir);
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
  @Deprecated // Add some context
  public static byte[] toByteArray(File path) {
    try {
      return toByteArray(new FileInputStream(path), null);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Read file to byte array, with context (for tracking down memory leaks)
   */
  public static byte[] toByteArray(File path, String contextOrNull) {
    try {
      return toByteArray(new FileInputStream(path), contextOrNull);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * Read InputStream to byte array
   */
  public static byte[] toByteArray(InputStream stream, String descriptionOrNull) {
    try {
      String description = ifNullOrEmpty(descriptionOrNull, "toByteArray");
      mem().register(stream, description);
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

  public void writeIntsLittleEndian(int[] ints, OutputStream s) {
    byte[] bytes = DataUtil.intsToBytesLittleEndian(ints);
    write(bytes, s);
  }

  /**
   * Write a number of floats to output stream, little-endian
   */
  public void writeFloatsLittleEndian(float[] floats, OutputStream outputStream) {
    byte[] bytes = DataUtil.floatsToBytesLittleEndian(floats);
    write(bytes, outputStream);
  }

  /**
   * Read a number of floats from input stream, little-endian
   */
  public static float[] readFloatsLittleEndian(InputStream inputStream, int length) {
    byte[] bytes = readBytes(inputStream, length * Float.BYTES);
    return DataUtil.bytesToFloatsLittleEndian(bytes);
  }

  /**
   * Read a number of floats from input stream, big-endian
   */
  public static float[] readFloatsBigEndian(InputStream inputStream, int length) {
    byte[] bytes = readBytes(inputStream, length * Float.BYTES);
    return DataUtil.bytesToFloatsBigEndian(bytes);
  }

  /**
   * Read floats from file, little-endian
   */
  public static float[] readFloatsLittleEndian(File file, String context) {
    byte[] bytes = toByteArray(file, context);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] dest = new float[bytes.length / Float.BYTES];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  /**
   * Read floats from file, big-endian
   */
  public static float[] readFloatsBigEndian(File file, String context) {
    byte[] bytes = toByteArray(file, context);
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
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
   * Make a backup of a directory, and create a new one
   */
  public File backupAndRemake(File directory) {
    if (dryRun())
      throw notSupported("Should we have a null output stream?");
    return backupManager().backupAndDelete(directory);
  }

  @Deprecated // Call backupAndRemake instead
  public File backupAndRebuild(File directory) {
    return backupAndRemake(directory);
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

  // ------------------------------------------------------------------
  // Project directory structure
  // ------------------------------------------------------------------

  public static final String PROJECT_CONFIG_DIR_NAME = "project_config";

  /**
   * Get project directory. If not yet defined, looks at the current directory
   * or one of its parents for one containing a subdirectory named
   * PROJECT_CONFIG_DIR_NAME
   */
  public File projectDirectory() {
    if (mProjectDirectory == null)
      setProjectDirectory(parent(getFileWithinParents(null, PROJECT_CONFIG_DIR_NAME,
          "directory containing '" + PROJECT_CONFIG_DIR_NAME + "'")));
    return mProjectDirectory;
  }

  /**
   * Set directory for project; must not already be defined
   */
  public void setProjectDirectory(File directory) {
    checkState(mProjectDirectory == null, "project directory already set to:", mProjectDirectory);
    mProjectDirectory = getCanonicalFile(directory);
  }

  /**
   * Get the project config directory. If not yet set, sets it to
   * <project_directory>/project_config
   */
  public File projectConfigDirectory() {
    if (mProjectConfigDirectory == null) {
      setProjectConfigDirectory(fileWithinProject(PROJECT_CONFIG_DIR_NAME));
    }
    return mProjectConfigDirectory;
  }

  /**
   * Get the project config directory. If not yet set, sets it to
   * <project_directory>/project_config if directory exists, or create a
   * temporary directory
   */
  public File optProjectConfigDirectory() {
    if (mProjectConfigDirectory == null) {
      try {
        projectDirectory();
      } catch (Throwable t) {
        System.out.println(
            "No project directory found; creating temporary one.  Call Files.setProjectDirectory() if desired.");
        setProjectDirectory(createTempDir("_project_directory_"));
      }
      var f = new File(projectDirectory(), PROJECT_CONFIG_DIR_NAME);
      if (!f.exists())
        f.mkdirs();
      setProjectConfigDirectory(f);
    }
    return projectConfigDirectory();
  }

  /**
   * Set project config directory; must not already be defined
   */
  public void setProjectConfigDirectory(File directory) {
    checkState(mProjectConfigDirectory == null, "project config directory already set to:",
        mProjectConfigDirectory);
    mProjectConfigDirectory = getCanonicalFile(assertDirectoryExists(directory));
  }

  /**
   * Get file within project configuration directory
   */
  public File fileWithinProjectConfigDirectory(String path) {
    return new File(projectConfigDirectory(), Files.assertRelative(path));
  }

  /**
   * Get the project secrets directory: <project_directory>/secrets
   */
  public File projectSecretsDirectory() {
    if (mProjectSecretsDirectory == null)
      mProjectSecretsDirectory = assertDirectoryExists(fileWithinProject("secrets"));
    return mProjectSecretsDirectory;
  }

  /**
   * Gets a file within the project directory; makes sure it exists
   */
  public File fileWithinProject(String relativePath) {
    return assertExists(optFileWithinProject(relativePath));
  }

  /**
   * Get a file within the project directory, which might not exist
   */
  public File optFileWithinProject(String relativePath) {
    return join(projectDirectory(), relativePath);
  }

  /**
   * Gets a file within the secrets directory; makes sure it exists
   */
  public File fileWithinSecrets(String relativePath) {
    return assertExists(optFileWithinSecrets(relativePath));
  }

  /**
   * Get file within the secrets directory, which may not exist
   */
  public File optFileWithinSecrets(String relativePath) {
    return join(projectSecretsDirectory(), relativePath);
  }

  public final JSMap entityInfo() {
    if (mEntityInfo == null) {
      mEntityInfo = JSMap.from(fileWithinSecrets(SECRETS_FILE_ENTITY_INFO));
    }
    return mEntityInfo;
  }

  private static JSMap mEntityInfo;

  public static final String SECRETS_FILE_ENTITY_INFO = "entity_info.json";

  private File mProjectDirectory;
  private File mProjectSecretsDirectory;
  private File mProjectConfigDirectory;

  // ------------------------------------------------------------------
  // Subprojects support
  // ------------------------------------------------------------------

  /**
   * If a subproject.txt file exists, read its suffix. If a variant of the
   * supplied file exists with this suffix, return that file instead
   */
  public static File subprojectVariant(File file) {
    final boolean db = false && alert("debug logging is in effect");
    File output = assertNonEmpty(file, "find subproject variant");
    File subprojectFile = new File("subproject.txt");
    if (db) {
      pr("looking for subproject variant for file:", INDENT, Files.infoMap(file));
      pr("subprojectFile:", INDENT, Files.infoMap(subprojectFile));
    }

    if (subprojectFile.exists()) {
      String suffix = Files.readString(subprojectFile).trim();
      if (db)
        pr("Subproject suffix:", quote(suffix));
      checkArgument(!suffix.isEmpty(), "No subproject found in:", subprojectFile);
      output = Files.setExtension(new File(Files.removeExtension(file.toString()) + "-" + suffix),
          Files.getExtension(file));
      if (db)
        pr("found variant:", INDENT, Files.infoMap(output));
    }
    return output;
  }

  // ------------------------------------------------------------------
  // Wrappers for File methods that throw checked exceptions
  // ------------------------------------------------------------------

  public static File createTempFile(String prefix, String suffix) {
    try {
      return File.createTempFile(prefix, suffix);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static File createTempDir(String prefix) {
    try {
      return java.nio.file.Files.createTempDirectory(prefix).toFile();
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  /**
   * A static version of mkdirs, for internal use
   */
  private static File mkdirHelper(File dir) {
    try {
      FileUtils.forceMkdir(dir);
      return dir;
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  // ------------------------------------------------------------------
  // Miscellaneous
  // ------------------------------------------------------------------

  /**
   * Call chmod on a file
   * 
   * @param file
   * @param flags
   *          argument for chmod command; e.g. "u+x" or 750
   */
  public void chmod(File file, Object flags) {
    if (dryRun())
      return;
    SystemCall sc = new SystemCall();
    sc.setVerbose(verbose());
    sc.arg("chmod", flags, file);
    sc.assertSuccess();
  }

  // ------------------------------------------------------------------
  // Streams
  // ------------------------------------------------------------------

  public static FileInputStream openInputStream(File file) {
    try {
      FileInputStream stream = FileUtils.openInputStream(file);
      mem().register(stream, file);
      return stream;
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public FileOutputStream outputStream(File file) {
    return outputStream(file, false);
  }

  public FileOutputStream outputStream(File file, boolean append) {
    if (verbose())
      log("opening output stream for:", file, "append:", append);
    if (dryRun())
      throw notSupported("Not supported in 'dryrun' mode");
    try {
      return new FileOutputStream(file, append);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static DataInputStream dataInputStream(File file) {
    return new DataInputStream(openInputStream(file));
  }

  /**
   * Get an input stream to a resource, which is stored in the class folder (or
   * one of its subfolders).
   * 
   * If not found, in case program is not running from a jar file, looks in
   * "~/src/main/resources/..."
   */
  public static BufferedInputStream openResource(Class theClass, String resourceName) {
    try {
      InputStream is = theClass.getResourceAsStream(resourceName);
      if (is == null) {
        var packageName = theClass.getPackageName();
        // Look for the resource in a src directory
        var alt = "src/main/resources/" + packageName.replace('.', '/') + "/" + resourceName;
        is = Files.openInputStream(new File(alt));
      }
      if (is == null)
        throw die(); // caught immediately below, so no message required
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
    return parseAbstractDataOpt(prototype, assertExists(file));
  }

  public static <T extends AbstractData> T parseAbstractDataOpt(T prototype, File file) {
    try {
      JSMap json = JSMap.fromFileIfExists(file);
      return parseAbstractDataOpt(prototype, json);
    } catch (Throwable t) {
      throw FileException.withCause(t, "Problem parsing AbstractData from file:", file);
    }
  }

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

  public static <T extends AbstractData> T parseAbstractData(T prototype, JSMap json) {
    if (json == null)
      throw badArg("no JSMap provided");
    return parseAbstractDataOpt(prototype, json);
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
   * Version of info() for string parameter
   */
  public static JSMap infoMap(String file) {
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

  public static ZipFile zipFile(File file) {
    try {
      return new ZipFile(file);
    } catch (IOException e) {
      throw asFileException(e);
    }
  }

  public static void unzip(File zipFile, File targetDirectoryOrNull, Predicate<File> filterOrNull) {
    File targetDirectory = targetDirectoryOrNull;
    if (targetDirectoryOrNull == null)
      targetDirectory = zipFile.getParentFile();

    ZipFile zipFileObj = null;
    try {
      zipFileObj = zipFile(zipFile);
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

  public static byte[] readZipEntryContents(ZipFile zipFile, ZipEntry entry) {
    try {
      InputStream in = zipFile.getInputStream(entry);
      return toByteArray(in, "readZipEntryContents");
    } catch (IOException e) {
      throw asFileException(e);
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

  public static final File[] EMPTY_FILE_LIST = new File[0];

  // ------------------------------------------------------------------
  // Backups
  // ------------------------------------------------------------------

  public BackupManager backupManager() {
    if (mBackupManager == null) {
      File backupDir = new File(projectDirectory(), "_SKIP_files_backups");
      mBackupManager = new BackupManager(this, projectDirectory()).withBackupRootDirectory(backupDir);
    }
    return mBackupManager;
  }

  private BackupManager mBackupManager;
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

  public static final String EXT_TEXT = "txt";
  public static final String EXT_BIN = "bin";
  public static final String EXT_JSON = "json";
  public static final String EXT_ZIP = "zip";
  public static final String EXT_TMP = "tmp";

}
