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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static js.base.Tools.*;

public final class RegExp {

  /**
   * Compile a regular expression to a Pattern; use an internal map to cache any
   * results
   *
   * <pre>
   *
   * Java regular expression gotchas
   * ----------------------------------
   * + The dot '.' doesn't match newlines.  If desired, use [\s\S] instead of .
   *
   * </pre>
   * 
   * This is useful:
   * 
   * https://www.freeformatter.com/java-regex-tester.html
   */
  public static Pattern pattern(String regularExpressionString) {
    Pattern pattern = sCachedPatternMap.get(regularExpressionString);
    if (pattern == null) {
      pattern = Pattern.compile(regularExpressionString);
      sCachedPatternMap.put(regularExpressionString, pattern);
    }
    return pattern;
  }

  public static Matcher matcher(String regularExpressionString, String text) {
    Pattern pattern = pattern(regularExpressionString);
    return pattern.matcher(text);
  }

  /**
   * Given a regex defining a group, see if it matches a string; if so, return
   * the matched group; else, null
   */
  public static String matcherGroup(String regularExpressionString, String text) {
    Matcher matcher = matcher(regularExpressionString, text);
    if (matcher.matches())
      return matcher.group(1);
    return null;
  }

  public static boolean patternMatchesString(Pattern pattern, String text) {
    return pattern.matcher(text).matches();
  }

  public static boolean patternMatchesString(String patternString, String text) {
    return patternMatchesString(pattern(patternString), text);
  }

  /**
   * Escape a string for subsequent pattern matching
   */
  public static String escapedForPattern(String string) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < string.length(); i++) {
      char ch = string.charAt(i);
      final String charsRequiringEscaping = "<([{\\^-=$!|]})?*+.>";
      int index = charsRequiringEscaping.indexOf(ch);
      if (index >= 0)
        sb.append('\\');
      sb.append(ch);
    }
    return sb.toString();
  }

  private static Map<String, Pattern> sCachedPatternMap = concurrentHashMap();

}
