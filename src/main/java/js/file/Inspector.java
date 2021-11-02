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
import java.util.Random;

import js.base.BaseObject;

public abstract class Inspector<T> extends BaseObject {

  public final void setDirectory(File directory) {
    checkState(mPreparedDirectory == null, "directory has already been prepared:", INDENT,
        mPreparedDirectory);
    mDirectory = directory;
  }

  public final void add(T item) {
    mSampleCount++;
    int rval = random().nextInt(sampleCount());
    if (rval >= maxSamples())
      return;
    String baseName = String.format("%04", rval);
    File filename = new File(directory(), baseName);
    writeSample(item, filename);
  }

  public final int sampleCount() {
    return mSampleCount;
  }

  public abstract void writeSample(T item, File target);

  private int maxSamples() {
    return 20;
  }

  private File directory() {
    if (mPreparedDirectory == null)
      prepareDirectory();
    return mPreparedDirectory;
  }

  private void prepareDirectory() {
    File dir = mDirectory;
    if (dir == null)
      dir = Files.getDesktopFile("_SKIP_inspection");
    checkArgument(dir.getName().endsWith("inspection"),
        "For safetly, inspection directory must end with 'inspection'");
    files().rebuild(dir, "scredit_project.txt");
    mPreparedDirectory = dir;
  }

  private Files files() {
    return Files.S;
  }

  private Random random() {
    if (mRandom == null)
      mRandom = new Random(System.currentTimeMillis());
    return mRandom;
  }

  private File mDirectory;
  private File mPreparedDirectory;
  private int mSampleCount;
  private Random mRandom;
}
