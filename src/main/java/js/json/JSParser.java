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
package js.json;

/**
 * Parse a json value (as defined in https://www.json.org/json-en.html) from a
 * character sequence
 */
final class JSParser {

  public JSParser(CharSequence source) {
    mSourceChars = source;
    skipWhitespace();
  }

  public Object readValue() {
    switch (peek()) {
    case '[': {
      JSList list = new JSList();
      list.constructFrom(this);
      return list;
    }
    case '{': {
      JSMap map = new JSMap();
      map.constructFrom(this);
      return map;
    }
    case '"':
      return readString();
    case 't':
    case 'f':
      return readBoolean();
    case 'n':
      readExpected("null");
      return null;
    default:
      return readNumber();
    }
  }

  /**
   * Read an expected character and any following whitespace
   */
  public void read(char expected) {
    if (read() != expected)
      fail();
    skipWhitespace();
  }

  /**
   * If next character matches a value, read it and any following whitespace
   */
  public boolean readIf(char expected) {
    if (peek() == expected) {
      mCursor++;
      skipWhitespace();
      return true;
    }
    return false;
  }

  /**
   * Read a quoted, escaped string and any following whitespace
   */
  public String readString() {

    StringBuilder w = mReadStringBuffer;
    w.setLength(0);

    if (read() != '"')
      fail();
    while (true) {
      char c = read();
      if (c == '"')
        break;
      if (c != '\\') {
        w.append(c);
        continue;
      }
      c = read();
      switch (c) {
      case '\\':
      case '"':
      case '/':
        w.append(c);
        break;
      case 'b':
        w.append('\b');
        break;
      case 'f':
        w.append('\f');
        break;
      case 'n':
        w.append('\n');
        break;
      case 'r':
        w.append('\r');
        break;
      case 't':
        w.append('\t');
        break;
      case 'u':
        w.append((char) ((readHex() << 12) | (readHex() << 8) | (readHex() << 4) | readHex()));
        break;
      default:
        fail();
      }
    }
    skipWhitespace();
    return w.toString();
  }

  public void assertCompleted() {
    if (mCursor != mSourceChars.length())
      fail();
  }

  private char peek() {
    return mSourceChars.charAt(mCursor);
  }

  private char read() {
    return mSourceChars.charAt(mCursor++);
  }

  private int readHex() {
    int result;
    char c = read();
    if (c >= 'a')
      c -= ('a' - 'A');
    if (c >= 'A') {
      result = (c - 'A') + 10;
      if (result >= 16)
        fail();
      return result;
    } else {
      result = c - '0';
      if (result < 0 || result >= 10)
        fail();
    }
    return result;
  }

  private boolean skipWhitespace() {
    int j = mCursor;
    while (j < mSourceChars.length()) {
      char c = mSourceChars.charAt(j);
      if (c == '/') {
        j++;
        if (j == mSourceChars.length() || mSourceChars.charAt(j) != '/')
          fail();
        j++;
        while (j < mSourceChars.length() && mSourceChars.charAt(j) != '\n')
          j++;
        continue;
      }
      if (c > ' ') {
        mCursor = j;
        return true;
      }
      j++;
    }
    mCursor = j;
    return false;
  }

  private Number readNumber() {
    int start = mCursor;
    boolean isFloat = false;
    while (mCursor < mSourceChars.length()) {
      char c = peek();
      if (c <= ' ' || c == ',' || c == ']' || c == '}')
        break;
      if (c == 'e' || c == 'E' || c == '.')
        isFloat = true;
      mCursor++;
    }
    String expr = mSourceChars.subSequence(start, mCursor).toString();
    skipWhitespace();
    Number value;
    if (isFloat)
      value = Double.parseDouble(expr);
    else
      value = Long.parseLong(expr);
    return value;
  }

  private void fail() {
    throw new IllegalArgumentException("Failing; source was: '" + mSourceChars + "'");
  }

  private void readExpected(String s) {
    for (int i = 0; i < s.length(); i++) {
      char c = s.charAt(i);
      if (c != mSourceChars.charAt(mCursor + i))
        fail();
    }
    mCursor += s.length();
    skipWhitespace();
  }

  private Boolean readBoolean() {
    switch (read()) {
    case 't':
      readExpected("rue");
      return true;
    case 'f':
      readExpected("alse");
      return false;
    default:
      fail();
      return false;
    }
  }

  private final StringBuilder mReadStringBuffer = new StringBuilder();
  private final CharSequence mSourceChars;
  private int mCursor;
}
