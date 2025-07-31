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
 **/
package js.json;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import js.data.AbstractData;
import js.file.Files;
import js.parsing.Lexer;
import js.parsing.Scanner;

import static js.base.Tools.*;
import static js.json.JSUtils.*;

/**
 * A json object (see https://www.json.org/json-en.html)
 */
public final class JSMap extends JSObject {

  // ------------------------------------------------------------------
  // Constructors
  // ------------------------------------------------------------------

  public static final JSMap DEFAULT_INSTANCE = new JSMap().lock();

  public JSMap() {
  }

  public JSMap(CharSequence source) {
    var lexer = new Lexer(JSON_DFA).withText(source);
    parseFrom(lexer, this);
  }

  public static JSMap from(File file) {
    String content = Files.readString(file);
    return new JSMap(content);
  }

  JSMap(Map<String, Object> wrappedMap) {
    mMap = wrappedMap;
  }

  public static JSMap from(Map map) {
    JSMap jsMap = new JSMap();
    Map<Object, Object> map2 = (Map<Object, Object>) map;
    for (Map.Entry<Object, Object> entry : map2.entrySet()) {
      String key = entry.getKey().toString();
      Object jsonValue = JSUtils.asJson(entry.getValue());
      jsMap.putUnsafe(key, jsonValue);
    }
    return jsMap;
  }

  public static JSMap fromFileIfExists(File file) {
    String content = Files.readString(file, "{}");
    return new JSMap(content);
  }

  // ------------------------------------------------------------------
  // Converting to string
  // ------------------------------------------------------------------

  @Override
  public String toString() {
    StringBuilder stringBuilder = new StringBuilder();
    printTo(stringBuilder, -1);
    return stringBuilder.toString();
  }

  @Override
  public void printTo(StringBuilder stringBuilder, int indentColumns) {
    if (indentColumns >= 0) {
      prettyPrintWithIndent(stringBuilder, indentColumns);
      return;
    }
    stringBuilder.append('{');
    if (nonEmpty()) {
      // Print the keys in sorted order, so they are deterministic
      for (String key : sortedKeys()) {
        JSUtils.printAsQuotedJsonString(key, stringBuilder);
        stringBuilder.append(':');
        Object value = mMap.get(key);
        JSUtils.printValueTo(value, stringBuilder, indentColumns);
        stringBuilder.append(',');
      }
      // Remove the extraneous comma added 
      stringBuilder.setLength(stringBuilder.length() - 1);
    }
    stringBuilder.append('}');
  }

  @Override
  public boolean isMap() {
    return true;
  }

  @Override
  public int size() {
    return mMap.size();
  }

  @Override
  public boolean isEmpty() {
    return mMap.isEmpty();
  }

  /**
   * Parse a JSMap from a scanner.  If target isn't null, it must be an empty JSMap
   */
  static JSMap parseFrom(Lexer s, JSMap targetOrNull) {
    var target = targetOrNull == null ? new JSMap() : targetOrNull;
    var mp = target.mMap;
    s.read(J_CBROP);
    while (!s.readIf(J_CBRCL)) {
      var key = JSUtils.parseStringFrom(s.read(J_STRING).text());
      s.read(J_COLON);
      var value = JSUtils.parseValue(s);
      if (value != null)
        mp.put(key, value);
      if (!s.readIf(J_COMMA)) {
        s.read(J_CBRCL);
        break;
      }
    }
    return target;
  }

  @Override
  public JSMap asMap() {
    return this;
  }

  @Override
  public JSMap deepCopy() {
    JSMap copy = new JSMap();
    Map<String, Object> hashMap = new ConcurrentHashMap<>();
    copy.mMap = hashMap;
    for (Entry<String, Object> entry : mMap.entrySet())
      hashMap.put(entry.getKey(), JSUtils.deepCopy(entry.getValue()));
    return copy;
  }

  @Override
  public JSMap lock() {
    if (!isLocked())
      mMap = Collections.unmodifiableMap(mMap);
    return this;
  }

  @Override
  public boolean isLocked() {
    return mMap.getClass().getSimpleName().equals("UnmodifiableMap");
  }

  @Override
  public JSMap clear() {
    mMap.clear();
    return this;
  }

  @Override
  public String prettyPrint() {
    StringBuilder sb = new StringBuilder();
    printTo(sb, 0);
    sb.append('\n');
    return sb.toString();
  }

  public static JSMap nullToEmpty(JSMap mapOrNull) {
    if (mapOrNull == null)
      return new JSMap();
    return mapOrNull;
  }

  // ------------------------------------------------------------------
  // Storing key/value pairs
  // ------------------------------------------------------------------

  public JSMap put(String key, JSObject value) {
    mMap.put(key, value);
    return this;
  }

  public JSMap put(String key, String value) {
    mMap.put(key, value);
    return this;
  }

  public JSMap put(String key, Number number) {
    mMap.put(key, number);
    return this;
  }

  public JSMap put(String key, Boolean bool) {
    mMap.put(key, bool);
    return this;
  }

  public JSMap put(String key, AbstractData abstractData) {
    mMap.put(key, abstractData.toJson());
    return this;
  }

  public JSMap putIfAbsentUnsafe(String key, Object objectOfUnknownType) {
    mMap.putIfAbsent(key, objectOfUnknownType);
    return this;
  }

  public JSMap putUnsafe(String key, Object objectOfUnknownType) {
    mMap.put(key, objectOfUnknownType);
    return this;
  }

  public JSMap putNumbered(String keyName, Object objectOfUnknownType) {
    int numKeys = size();
    String prefix = String.format("%3d", numKeys);
    if (keyName != null)
      prefix = prefix + spaces(13 - keyName.length()) + keyName;
    return putUnsafe(prefix, objectOfUnknownType);
  }

  public JSMap putNumbered(Object objectOfUnknownType) {
    return putNumbered(null, objectOfUnknownType);
  }

  /**
   * Add all key/value pairs from another map to this one
   */
  public JSMap putAll(JSMap map) {
    mMap.putAll(map.mMap);
    return this;
  }

  /**
   * Copy all key/value pairs from another map to this one, skipping any whose
   * keys already exist
   */
  public JSMap putAllMissing(JSMap otherMap) {
    for (Map.Entry<String, Object> otherEntry : otherMap.mMap.entrySet()) {
      if (!mMap.containsKey(otherEntry.getKey()))
        mMap.put(otherEntry.getKey(), otherEntry.getValue());
    }
    return this;
  }

  /**
   * If boolean value is true, stores it; otherwise, remove key if it exists
   */
  public JSMap putRemove(String key, Boolean booleanValue) {
    if (booleanValue)
      return put(key, booleanValue);
    else
      mMap.remove(key);
    return this;
  }

  public JSMap createMapIfMissing(String key) {
    JSMap map = optJSMap(key);
    if (map == null) {
      map = new JSMap();
      put(key, map);
    }
    return map;
  }

  public JSList createListIfMissing(String key) {
    JSList list = optJSList(key);
    if (list == null) {
      list = new JSList();
      put(key, list);
    }
    return list;
  }

  // ------------------------------------------------------------------
  // Keys
  // ------------------------------------------------------------------

  public boolean containsKey(String key) {
    return mMap.containsKey(key);
  }

  public Set<String> keySet() {
    return mMap.keySet();
  }

  // ------------------------------------------------------------------
  // Reading key/value pairs
  // ------------------------------------------------------------------

  public JSMap getMap(String key) {
    return (JSMap) getValueFor(key, true);
  }

  public JSList getList(String key) {
    return (JSList) getValueFor(key, true);
  }

  public double getDouble(String key) {
    return number(key).doubleValue();
  }

  public float getFloat(String key) {
    return number(key).floatValue();
  }

  public long getLong(String key) {
    return number(key).longValue();
  }

  public int getInt(String key) {
    return number(key).intValue();
  }

  public boolean getBoolean(String key) {
    return (Boolean) getValueFor(key, true);
  }

  public String get(String key) {
    return (String) getValueFor(key, true);
  }

  public Object getUnsafe(String key) {
    return getValueFor(key, true);
  }

  public Object optUnsafe(String key) {
    return getValueFor(key, false);
  }

  public JSList optJSList(String key) {
    return (JSList) getValueFor(key, false);
  }

  public JSList optJSListOrEmpty(String key) {
    JSList result = optJSList(key);
    if (result == null)
      result = new JSList();
    return result;
  }

  public JSMap optJSMap(String key) {
    return (JSMap) getValueFor(key, false);
  }

  public JSMap optJSMapOrEmpty(String key) {
    JSMap result = optJSMap(key);
    if (result == null)
      result = new JSMap();
    return result;
  }

  public String opt(String key, String defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return (String) value;
  }

  public Boolean opt(String key, Boolean defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return (Boolean) value;
  }

  public Boolean opt(String key) {
    return opt(key, Boolean.FALSE);
  }

  // ------------------------------------------------------------------
  // Read primitive value, returning a default if missing
  // ------------------------------------------------------------------

  public byte opt(String key, byte defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).byteValue();
  }

  public short opt(String key, short defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).shortValue();
  }

  public int opt(String key, int defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).intValue();
  }

  public long opt(String key, long defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).longValue();
  }

  public float opt(String key, float defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).floatValue();
  }

  public double opt(String key, double defaultValue) {
    Object value = getValueFor(key, false);
    if (value == null)
      return defaultValue;
    return ((Number) value).doubleValue();
  }

  // ------------------------------------------------------------------
  // Read primitive value, returning null if missing
  // ------------------------------------------------------------------

  public Byte optByte(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.byteValue();
  }

  public Short optShort(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.shortValue();
  }

  public Integer optInt(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.intValue();
  }

  public Long optLong(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.longValue();
  }

  public Float optFloat(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.floatValue();
  }

  public Double optDouble(String key) {
    Number number = (Number) getValueFor(key, false);
    if (number == null)
      return null;
    return number.doubleValue();
  }

  public JSMap remove(String key) {
    mMap.remove(key);
    return this;
  }

  public Map<String, Object> wrappedMap() {
    return mMap;
  }

  // ------------------------------------------------------------------
  // Equals / hashcode
  // ------------------------------------------------------------------

  // It is possible that some values are Integers and others are Longs, 
  // or Floats and Doubles, and their differing types will cause a 
  // more naive method to fail.  So, for equals and hashCode, we
  // convert the objects to a (non-pretty printed) string
  // and compare those strings.  Slower but robust.

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    JSMap m2 = (JSMap) o;
    return this.toString().equals(m2.toString());
  }

  @Override
  public int hashCode() {
    return toString().hashCode();
  }

  private Object getValueFor(String key, boolean mustExist) {
    Object value = mMap.get(key);
    if (value == null && mustExist)
      throw new IllegalArgumentException("missing value for key " + key);
    return value;
  }

  private Number number(String key) {
    return ((Number) getValueFor(key, true));
  }

  private List<String> sortedKeys() {
    List<String> sortedKeysList = new ArrayList<>();
    sortedKeysList.addAll(keySet());
    Collections.sort(sortedKeysList);
    return sortedKeysList;
  }

  private void prettyPrintWithIndent(StringBuilder target, int indentColumns) {
    indentColumns += 2;
    var sortedKeysList = sortedKeys();
    List<String> escapedKeysList = new ArrayList<>(sortedKeysList.size());

    // Create a corresponding list of keys in escaped form, suitable for printing
    {
      StringBuilder keyWorkBuffer = new StringBuilder();
      for (String key : sortedKeysList) {
        keyWorkBuffer.setLength(0);
        JSUtils.printAsQuotedJsonString(key, keyWorkBuffer);
        escapedKeysList.add(keyWorkBuffer.substring(1, keyWorkBuffer.length() - 1));
      }
    }

    target.append("{ ");
    int longestKeyLength = 0;
    for (String string : sortedKeysList)
      longestKeyLength = Math.max(longestKeyLength, string.length());

    for (int i = 0; i < sortedKeysList.size(); i++) {
      String keyStr = sortedKeysList.get(i);
      String escKeyStr = escapedKeysList.get(i);
      int effectiveKeyLength = Math.min(longestKeyLength, escKeyStr.length());

      int keyIndent = longestKeyLength;
      if (i != 0) {
        target.append(",\n");
        keyIndent += indentColumns;
      }

      target.append(spaces(keyIndent - effectiveKeyLength));
      target.append("\"");
      target.append(escKeyStr);
      target.append("\" : ");

      int valIndent = indentColumns + longestKeyLength + 5;

      // If the key we just printed was longer than our maximum,
      // print the value on the next line (indented to the appropriate column)
      if (escKeyStr.length() > longestKeyLength) {
        target.append("\n");
        target.append(spaces(valIndent));
      }
      Object value = mMap.get(keyStr);
      JSUtils.printValueTo(value, target, valIndent);
    }
    indentColumns -= 2;
    if (sortedKeysList.size() >= 2) {
      target.append("\n");
      target.append(spaces(indentColumns));
    } else
      target.append(' ');
    target.append("}");
  }

  private Map<String, Object> mMap = new ConcurrentHashMap<>();

}
