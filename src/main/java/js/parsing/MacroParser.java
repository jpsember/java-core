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

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.json.JSMap;

import static js.base.Tools.*;

public final class MacroParser extends BaseObject {

  public interface Mapper {
    String textForKey(String key);
  }

  public MacroParser withMapper(Mapper mapper) {
    ensureMutable();
    mMapper = mapper;
    return this;
  }

  public MacroParser withMapper(JSMap jsonMap) {
    ensureMutable();
    mMapper = buildMapper(jsonMap);
    return this;
  }

  public MacroParser withTemplate(String template) {
    ensureMutable();
    extractMacros(template);
    return this;
  }

  public MacroParser withDefault(String defaultValue) {
    ensureMutable();
    mDefaultValue = defaultValue;
    return this;
  }

  public MacroParser withPattern(Pattern pattern) {
    ensureMutable();
    mPattern = pattern;
    log("pattern set to:", mPattern, ST);
    return this;
  }

  public MacroParser lock() {
    if (!mLocked) {
      checkNotNull(mFragments, "no template given");
      mLocked = true;
    }
    return this;
  }

  /**
   * Thread safe.
   */
  public String content(JSMap map) {
    return content(buildMapper(map));
  }

  /**
   * Thread safe.
   */
  public String content(Mapper mapper) {
    log("...generating content");
    checkNotNull(mapper, "no mapper given");
    lock();
    List<String> fragments = mFragments;

    if (false && verbose()) {
      for (int i = 0; i < fragments.size(); i++)
        log("...fragment", i, ":", INDENT, fragments.get(i));
    }

    StringBuilder output = new StringBuilder();
    int index = -1;
    for (String fragment : fragments) {
      index++;
      if ((index & 1) == 1) {
        mCurrentKeyIndex = index;
        String key = fragments.get(index);
        String text = mapper.textForKey(key);
        if (text == null) {
          text = mDefaultValue;
          if (text == null)
            throw new IllegalArgumentException("no value returned for key: " + key);

          if (text.endsWith("@")) {
            text = text.substring(0, text.length() - 1) + "'" + key + "'";
          }
        }
        if (verbose())
          log("...macro:", key, "=>", INDENT, text);
        output.append(text);
      } else {
        if (verbose())
          log("...fragment:", INDENT, fragment);
        output.append(fragment);
      }
    }
    String result = output.toString();
    log("...result:", INDENT, result);
    return result;
  }

  public String content() {
    return content(mMapper);
  }

  public int keyCursor() {
    return mFragmentStartLocations.get(mCurrentKeyIndex);
  }

  // Given a source string and a regular expression defining a macro, construct a list of fragments,
  // where each (2n+1)th fragment is the name of the macro, and the remaining fragments are the
  // portions of text between the macros.
  //
  private void extractMacros(String source_text) {
    List<String> fragments = arrayList();
    List<Integer> fragmentLocations = arrayList();
    int cursor = 0;

    for (Entry entry : occurrencesOf(mPattern, source_text)) {
      fragmentLocations.add(cursor);
      fragments.add(source_text.substring(cursor, entry.start));
      fragmentLocations.add(entry.start);
      fragments.add(entry.content);
      cursor = entry.start + entry.length;
    }

    fragmentLocations.add(cursor);
    fragments.add(source_text.substring(cursor));

    mFragments = fragments;
    mFragmentStartLocations = fragmentLocations;
  }

  private static Mapper buildMapper(JSMap jsonMap) {
    return (key) -> jsonMap.opt(key, (String) null);
  }

  private void ensureMutable() {
    checkState(!mLocked, "already locked");
  }

  public static String parse(String template, Map<String, String> map) {
    MacroParser parser = new MacroParser();
    parser.withMapper((key) -> map.get(key));
    parser.withTemplate(template);
    return parser.content();
  }

  public static String parse(String template, JSMap map) {
    MacroParser parser = new MacroParser();
    parser.withMapper((key) -> map.opt(key, (String) null));
    parser.withTemplate(template);
    return parser.content();
  }

  public static class Entry {
    public Entry(int start, int length, String content) {
      this.start = start;
      this.length = length;
      this.content = content;
    }

    public final int start;
    public final int length;
    public final String content;
  }

  public static List<Entry> occurrencesOf(Pattern pattern, String text) {
    return auxOccurrences(pattern, text, 0);
  }

  public static List<Entry> occurrencesOfPatternSubexpression(Pattern pattern, String text) {
    return auxOccurrences(pattern, text, 1);
  }

  private static List<Entry> auxOccurrences(Pattern pattern, String text, int contextGroupIndex) {
    List<Entry> results = arrayList();
    Matcher m = pattern.matcher(text);
    while (m.find()) {
      int contextStart = m.start(contextGroupIndex);
      int contextLength = m.end(contextGroupIndex) - contextStart;
      results.add(new Entry(contextStart, contextLength, m.group(1)));
    }
    return results;
  }

  private static final Pattern MACRO_EXPR = Pattern.compile("\\[!([^\\n\\]]+)\\]");

  private String mDefaultValue;
  private Pattern mPattern = MACRO_EXPR;
  private Mapper mMapper;
  private List<String> mFragments;
  private List<Integer> mFragmentStartLocations;
  private int mCurrentKeyIndex;
  private boolean mLocked;
}
