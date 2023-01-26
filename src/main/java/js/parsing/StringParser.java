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
package js.parsing;

import static js.base.Tools.*;

import js.base.BaseObject;
import js.json.JSMap;

/**
 * Utility class to parse text from a string
 */
public final class StringParser extends BaseObject {

  public StringParser(String content) {
    mContent = content;
  }

  public int cursor() {return mCursor;}
  
  /**
   * Read next character; throws exception if no characters remain
   */
  public char readChar() {
    char ch = peekChar();
    if (ch == 0)
      badState("no more characters", INDENT, toJson());
    mCursor++;
    return ch;
  }

  /**
   * Read a string; throws exception if insufficient characters remain
   * 
   * @param length
   *          length of string to read
   */
  public String readChars(int length) {
    int cursor2 = mCursor + length;
    checkState(cursor2 <= mContent.length(), "no more characters", INDENT, toJson());
    String substring = mContent.substring(mCursor, cursor2);
    mCursor = cursor2;
    return substring;
  }

  @Override
  public JSMap toJson() {
    JSMap m = map();
    m.put("content", mContent);
    m.put("cursor", mCursor);
    m.put("remaining", peekRemaining());
    return m;
  }

  public String readSpace() {
    return read(" ");
  }

  /**
   * Read an expected string; throws exception if not present
   */
  public String read(String expected) {
    if (!peekIs(expected))
      throw badArg("Expected", quote(expected), INDENT, toJson());
    mCursor += expected.length();
    return expected;
  }

  public char peekChar() {
    if (mCursor < mContent.length())
      return mContent.charAt(mCursor);
    return (char) 0;
  }

  public boolean peekIs(String expected) {
    if (mCursor + expected.length() <= mContent.length()) {
      if (mContent.substring(mCursor, mCursor + expected.length()).equals(expected))
        return true;
    }
    return false;
  }

  public boolean readIf(String expected) {
    boolean result = peekIs(expected);
    if (result)
      mCursor += expected.length();
    return result;
  }

  /**
   * Read an integer
   */
  public int readInteger() {
    int startCursor = mCursor;
    int i = startCursor;
    if (peekChar() == '-')
      i++;
    while (i < mContent.length()) {
      char c = mContent.charAt(i);
      if (c < '0' || c > '9')
        break;
      i++;
    }
    String substring = mContent.substring(startCursor, i);
    Integer result =  Integer.parseInt(substring);
    mCursor = i;
    return result;
  }

  public String peekRemaining() {
    return mContent.substring(mCursor);
  }

  public String readRemaining() {
    String result = peekRemaining();
    mCursor = mContent.length();
    return result;
  }

  public String readPath() {
    StringBuilder x = new StringBuilder();
    final String quote = "\"";
    boolean quoted = readIf(quote);
    if (quoted) {
      while (!readIf(quote)) {
        readIf("\\");
        x.append(readChar());
      }
    } else {
      while (true) {
        char ch = peekChar();
        if (ch == 0 || ch == ' ')
          break;
        x.append(ch);
        readChar();
      }
    }
    return x.toString();
  }

  public void assertDone() {
    if (!done())
      badState("Unexpected characters in content:", quote(mContent),
          quote("..." + mContent.substring(mCursor)));
  }

  public boolean done() {
    return mCursor == mContent.length();
  }

  private final String mContent;
  private int mCursor;

}
