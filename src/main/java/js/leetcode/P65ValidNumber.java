
package js.leetcode;

import static js.base.Tools.*;

public class P65ValidNumber {

  public static void main(String[] args) {
    new P65ValidNumber().run();
  }

  private void run() {
    x("005047e+6", true);
    x("0", true);
    x("e", false);
    x(".", false);
    x("0..", false);
  }

  private void x(String s, boolean expected) {
    boolean result = isNumber(s);
    pr(s, result);
    checkState(result == expected, "expected:", expected);
  }

  public boolean isNumber(String s) {
    mCursor = 0;
    mExpr = s;
    mValid = true;
    parseDecOrInt();
    if (parseOptE())
      parseDec();
    checkValid(mCursor == s.length());
    return mValid;
  }

  private void parseDec() {
    readAOrB('+', '-');
    readDigits();
  }

  private void parseDecOrInt() {
    readAOrB('+', '-');
    if (readIf('.')) {
      readDigits();
    } else {
      readDigits();
      readIf('.');
      if (peekDigit())
        readDigits();
    }
  }

  private boolean checkValid(boolean flag) {
    if (!flag)
      mValid = false;
    return mValid;
  }

  private void readDigits() {
    checkValid(peekDigit());
    while (peekDigit())
      read();
  }

  private boolean peekDigit() {
    char c = peek();
    return c >= '0' && c <= '9';
  }

  private boolean parseOptE() {
    return readAOrB('e', 'E');
  }

  private boolean readAOrB(char a, char b) {
    return readIf(a) || readIf(b);
  }

  private boolean readIf(char c) {
    if (peek() != c)
      return false;
    read();
    return true;
  }

  private char peek() {
    char result = 0;
    if (mCursor != mExpr.length())
      result = mExpr.charAt(mCursor);
    return result;
  }

  private char read() {
    char result = peek();
    if (checkValid(result != 0))
      mCursor++;
    //    pr("[" + mExpr.substring(0, mCursor) + ">" + mExpr.substring(mCursor) + "] ",
    //        mValid ? "" : "*** invalid ***");
    return result;
  }

  private int mCursor;
  private String mExpr;
  private boolean mValid;

}
