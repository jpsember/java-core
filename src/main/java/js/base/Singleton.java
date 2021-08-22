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

import java.io.Closeable;
import java.util.function.Supplier;

import js.file.Files;

/**
 * Thread safe, lazy initialized singleton
 */
public class Singleton<T> implements Supplier<T>, Closeable {

  public Singleton(Supplier<T> supplier) {
    mSupplier = supplier;
  }

  @Override
  public T get() {
    T obj = mSingleton;
    if (obj == null) {
      synchronized (this) {
        obj = mSingleton;
        if (obj == null) {
          obj = mSingleton = mSupplier.get();
          mSupplier = null;
        }
      }
    }
    return obj;
  }

  @Override
  public void close() {
    if (mSingleton != null) {
      if (mSingleton instanceof Closeable)
        Files.close((Closeable) mSingleton);
      mSingleton = null;
    }
  }

  private volatile T mSingleton;
  private Supplier<T> mSupplier;

}
