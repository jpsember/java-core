package js.app;

import static js.base.Tools.*;

import java.io.File;
import java.util.List;

import js.base.DateTimeTools;
import js.base.SystemCall;
import js.file.DirWalk;
import js.file.Files;

public class ScreenCaptureOper {

  public static void main(String[] args) {
    new ScreenCaptureOper().perform(args);
  }

  private void perform(String[] args) {

    File errFile = null;
    if (mSimulateError) {
      errFile = Files.getDesktopFile("_err_sentinel.txt_");
    }

    if (alert("short interval"))
      mSecondsBetweenShots = 5;

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
          s.setVerbose();
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
    while (imageFiles().size() > mMaxScreenshots) {
      File x = imageFiles().get(0);
      Files.S.deletePeacefully(x);
      imageFiles().remove(0);
    }
  }

  private List<File> imageFiles() {
    if (mImageFiles == null) {
      List<File> lst = arrayList();
      DirWalk w = new DirWalk(imageDir());
      w.withRecurse(false);
      w.withExtensions("jpg");
      for (File f : w.files()) {
        String name = f.getName();
        if (!name.startsWith("_screenshot_"))
          continue;
        lst.add(f);
      }
      mImageFiles = lst;
    }
    return mImageFiles;
  }

  private File imageDir() {
    if (mImageDir == null) {
      File c = Files.S.mkdirs(new File(Files.homeDirectory(), "Downloads/screenshots"));
      checkState(c.isDirectory(), "can't find directory:", c);
      mImageDir = c;
    }
    return mImageDir;
  }

  private File mImageDir;

  private File getNextOutputFile(long timestamp, int deviceNumber) {
    File f = new File(imageDir(), "_screenshot_" + timestamp + "_d" + deviceNumber + ".jpg");
    return f;
  }

  private boolean mSimulateError = true;
  private List<File> mImageFiles;
  private int mSecondsBetweenShots = 60;
  private int mMaxScreenshots = 1500;
  private long mStartTime;
  private final boolean mDisableActualScreenshots = alert("skipping shots");

}