package js.app;

import static js.base.Tools.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import js.base.BaseObject;
import js.base.DateTimeTools;
import js.base.SystemCall;
import js.file.DirWalk;
import js.file.Files;

public class ScreenCaptureOper extends BaseObject {

  public static void main(String[] args) {
    new ScreenCaptureOper().perform(args);
  }

  private void perform(String[] args) {
    alertVerbose();

    File errFile = null;
    if (mSimulateError) {
      errFile = Files.getDesktopFile("_err_sentinel.txt_");
    }

    buildImageFiles();
    buildZipFiles();

    // See https://markholloway.com/2018/11/14/macos-screencapture-terminal/

    while (true) {

      long timestamp = System.currentTimeMillis();
      if (mStartTime == 0)
        mStartTime = timestamp;

      if (mSimulateError) {
        if (timestamp - mStartTime > 20000) {
          if (!errFile.exists()) {
            Files.S.writeString(errFile, "simulating error");
            pr("...simulating an error...");
            die("goodbye");
          } else {
            mSimulateError = false;
            alert(
                "there is already an error sentinel file, so we already restarted; not simulating an error");
          }
        }
      }

      pr("....iteration: " + timestamp / 1000);

      if (!mDisableActualScreenshots)
        for (int devNum = 0; devNum < 2; devNum++) {
          SystemCall s = new SystemCall();
          s.arg("screencapture");
          s.arg("-S"); // Capture the entire screen
          s.arg("-T", 1); // delay in seconds
          //     s.arg("-x");  // Do not play sounds
          s.arg("-r"); // Do not add some metadata to image
          s.arg("-tjpg"); // output image format
          s.arg("-D" + (1 + devNum)); // device number

          File output = getNextOutputFile(timestamp, devNum);
          s.arg(output);
          s.call();

          // If error output is 'no such device', ignore
          if (s.systemErr().contains("Invalid display specified"))
            continue;

          s.assertSuccess();
          imageFiles().add(output);
        }

      DateTimeTools.sleepForRealMs(mSecondsBetweenShots * 1000L);

      if (!mDisableActualScreenshots)
        cullShots();
    }

  }

  private void cullShots() {
    // If there are enough image files for a zip file, create one
    if (imageFiles().size() < mMaxImagesPerZip)
      return;

    File tmpFile = createZipFile(imageFiles());
    File zipFile = new File(imageDir(), System.currentTimeMillis() + "." + Files.EXT_ZIP);

    Files.S.moveFile(tmpFile, zipFile);
    zipFiles().add(zipFile);
    Files.S.deleteFile(tmpFile);

    for (File imgFile : imageFiles())
      Files.S.deleteFile(imgFile);
    imageFiles().clear();

    // Delete old image zip files
    //
    while (zipFiles().size() > mMaxScreenshotZipFiles) {
      File x = zipFiles().get(0);
      Files.S.deleteFile(x);
      zipFiles().remove(0);
    }
  }

  private File createZipFile(List<File> fileList) {
    File tmpFile = new File(imageDir(), "_tmpfile_." + Files.EXT_TMP);
    Files.S.deletePeacefully(tmpFile);

    try {
      ZipOutputStream zipStream = new ZipOutputStream(Files.S.outputStream(tmpFile));
      for (File absFile : fileList) {
        String name = absFile.getName();
        ZipEntry zipEntry = new ZipEntry(name);
        zipStream.putNextEntry(zipEntry);
        zipStream.write(Files.toByteArray(absFile, "ScreenCaptureOper add to zip"));
        zipStream.closeEntry();
      }

      zipStream.close();
    } catch (IOException e) {
      throw Files.asFileException(e);
    }

    return tmpFile;
  }

  private void buildImageFiles() {
    DirWalk w = new DirWalk(imageDir());
    w.withRecurse(false);
    w.withExtensions("jpg");
    mImageFiles = w.files();
  }

  private List<File> imageFiles() {
    return mImageFiles;
  }

  private void buildZipFiles() {
    DirWalk w = new DirWalk(imageDir());
    w.withRecurse(false);
    w.withExtensions("zip");
    mZipFiles = w.files();
  }

  private List<File> zipFiles() {
    return mZipFiles;
  }

  private File imageDir() {
    if (mImageDir == null) {
      // I used to store things in the Downloads directory, but the files don't show up
      // when run as a daemon; see https://developer.apple.com/forums/thread/118508
      //
      // Using our own subdirectory in the home directory seems to work.
      //
      File c = Files.S.mkdirs(new File(Files.homeDirectory(), "_screenshots_"));
      checkState(c.isDirectory(), "can't find directory:", c);
      mImageDir = c;
      log("set image dir:", mImageDir);
    }
    return mImageDir;
  }

  private File mImageDir;

  private File getNextOutputFile(long timestamp, int deviceNumber) {
    File f = new File(imageDir(), timestamp + "_d" + deviceNumber + ".jpg");
    return f;
  }

  private boolean mSimulateError = false; //true;
  private List<File> mImageFiles;
  private List<File> mZipFiles;
  private int mSecondsBetweenShots = 10; // 60;
  private int mMaxImagesPerZip = 10; // 100
  private int mMaxScreenshotZipFiles = 3; // 100
  private long mStartTime;
  private final boolean mDisableActualScreenshots = false && alert("skipping shots");

}
