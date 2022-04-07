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
package js.data;

import static js.base.Tools.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import js.base.Pair;
import js.base.Tools;
import js.file.Files;
import js.json.JSList;
import js.json.JSMap;

/**
 * Utility methods in support of AbstractData class
 */
public final class DataUtil {

  /**
   * The number of bytes in a kilobyte.
   */
  public static final long ONE_KB = 1024;

  /**
   * The number of bytes in a megabyte.
   */
  public static final long ONE_MB = ONE_KB * ONE_KB;

  public static final boolean NULL_LIST_ELEMENTS_ALLOWED = false;

  static {
    loadTools();
  }

  /**
   * Get a compact string representation of an AbstractData object. Avoids
   * pretty printing
   */
  public static String toString(AbstractData data) {
    return data.toJson().toString();
  }

  /**
   * Construct a 'fresh' builder from an AbstractData, by building it (in case
   * it's already a builder) and re-converting to a builder
   */
  public static <T extends AbstractData> T defensiveBuilder(AbstractData data) {
    return defensiveBuilder(data, null);
  }

  /**
   * Construct a 'fresh' builder from an AbstractData, by building it (in case
   * it's already a builder) and re-converting to a builder. If the supplied
   * data is null, uses the supplied prototype
   */
  public static <T extends AbstractData> T defensiveBuilder(AbstractData dataOrNull, AbstractData prototype) {
    if (dataOrNull == null) {
      if (prototype == null)
        throw badArg("No prototype supplied");
      dataOrNull = prototype;
    }
    @SuppressWarnings("unchecked")
    T result = (T) dataOrNull.build().toBuilder();
    return result;
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractData> T defaultValue(AbstractData data) {
    checkNotNull(data);
    try {
      Field field = data.getClass().getField("DEFAULT_INSTANCE");
      return (T) field.get(data);
    } catch (Throwable t) {
      throw badArgWithCause(t, "Failed to get default value for AbstractData", data.getClass());
    }
  }

  public static String convertCamelCaseToUnderscores(String string) {
    List<String> words = new ArrayList<>();
    int wordStart = 0;
    for (int cursor = 0; cursor < string.length(); cursor++) {
      char c = string.charAt(cursor);
      if (c >= 'A' && c <= 'Z') {
        if (wordStart < cursor) {
          words.add(string.substring(wordStart, cursor).toLowerCase());
          wordStart = cursor;
        }
      }
    }
    words.add(string.substring(wordStart).toLowerCase());
    return String.join("_", words);
  }

  public static String convertUnderscoresToCamelCase(String string) {
    List<String> words = new ArrayList<>();
    int cursor = 0;
    while (cursor < string.length()) {
      int i = string.indexOf('_', cursor);
      if (i < 0)
        i = string.length();
      String word = string.substring(cursor, i);
      if (word.length() > 0)
        words.add(word);
      cursor = i + 1;
    }
    StringBuilder sb = new StringBuilder();
    for (String word : words) {
      sb.append(capitalizeFirst(word));
    }
    return sb.toString();
  }

  public static String capitalizeFirst(String s) {
    return s.substring(0, 1).toUpperCase() + s.substring(1);
  }

  public static String lowerFirst(String s) {
    return s.substring(0, 1).toLowerCase() + s.substring(1);
  }

  /**
   * Construct int array of a particular size, or use supplied one (which must
   * be the requested size)
   */
  public static int[] intArray(int size, int[] optionalExistingArray) {
    if (optionalExistingArray != null)
      return assertLength(optionalExistingArray, size, null);
    return new int[size];
  }

  /**
   * Verify int array has a particular length
   */
  @Deprecated // Use assertLength(int arrayLength)
  public static int[] assertLength(int[] array, int expectedLength, String contextOrNull) {
    assertLength(array.length, expectedLength, contextOrNull);
    return array;
  }

  /**
   * Verify array has a particular length
   */
  public static void assertLength(int arrayLength, int expectedLength, String contextOrNull) {
    if (arrayLength != expectedLength)
      throw badArg("Unexpected array length", arrayLength, ", expected", expectedLength,
          ifNullOrEmpty(contextOrNull, "(no context given)"));
  }

  /**
   * Construct float array of a particular size, or use supplied one (which must
   * be the requested size)
   */
  public static float[] floatArray(int size, float[] optionalExistingArray) {
    if (optionalExistingArray != null)
      return assertLength(optionalExistingArray, size, null);
    return new float[size];
  }

  /**
   * Verify float array has a particular length
   */
  public static float[] assertLength(float[] array, int expectedLength, String contextOrNull) {
    if (array.length != expectedLength)
      throw badArg("Unexpected array length", array.length, ", expected", expectedLength,
          ifNullOrEmpty(contextOrNull, "(no context given)"));
    return array;
  }

  /**
   * Build primitive array of ints from a collection of Numbers
   */
  public static int[] intArray(Collection numberCollection) {
    int[] primitiveArray = new int[numberCollection.size()];
    int i = 0;
    for (Object y : numberCollection) {
      Number x = (Number) y;
      primitiveArray[i] = x.intValue();
      i++;
    }
    return primitiveArray;
  }

  public static short[] shortArray(int size, short[] optionalExistingArray) {
    if (optionalExistingArray != null) {
      if (optionalExistingArray.length != size)
        throw new IllegalArgumentException("unexpected size");
      return optionalExistingArray;
    }
    return new short[size];
  }

  public static short[] shortArray(Collection numberCollection) {
    short[] primitiveArray = new short[numberCollection.size()];
    int i = 0;
    for (Object y : numberCollection) {
      Number x = (Number) y;
      primitiveArray[i] = x.shortValue();
      i++;
    }
    return primitiveArray;
  }

  public static String[] toStringArray(List<? extends Object> listOfStrings) {
    return listOfStrings.toArray(EMPTY_STRING_ARRAY);
  }

  public static final String[] EMPTY_STRING_ARRAY = new String[0];
  public static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
  public static final short[] EMPTY_SHORT_ARRAY = new short[0];
  public static final int[] EMPTY_INT_ARRAY = new int[0];
  public static final long[] EMPTY_LONG_ARRAY = new long[0];
  public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
  public static final double[] EMPTY_DOUBLE_ARRAY = new double[0];
  public static final String CHARS_ALPHA = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";
  public static final String CHARS_ALPHANUMERIC = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  private static final List IMMUTABLE_EMPTY_LIST = new ArrayList(0);
  private static final Map IMMUTABLE_EMPTY_MAP = Collections.unmodifiableMap(hashMap());

  @SuppressWarnings("unchecked")
  public static <T> List<T> emptyList() {
    return IMMUTABLE_EMPTY_LIST;
  }

  @SuppressWarnings("unchecked")
  public static <K, V> Map<K, V> emptyMap() {
    return IMMUTABLE_EMPTY_MAP;
  }

  public static <T> List<T> mutableCopyOf(List<T> sourceListOrNull) {
    if (sourceListOrNull == null)
      return null;
    return new ArrayList<T>(sourceListOrNull);
  }

  public static <T> List<T> immutableCopyOf(List<T> sourceListOrNull) {
    if (sourceListOrNull == null)
      return null;
    return Collections.unmodifiableList(sourceListOrNull);
  }

  public static <K, V> Map<K, V> mutableCopyOf(Map<K, V> sourceMapOrNull) {
    if (sourceMapOrNull == null)
      return null;
    return new ConcurrentHashMap<K, V>(sourceMapOrNull);
  }

  public static <K, V> Map<K, V> immutableCopyOf(Map<K, V> sourceMapOrNull) {
    if (sourceMapOrNull == null)
      return null;
    return Collections.unmodifiableMap(sourceMapOrNull);
  }

  /**
   * Convert a bit sequence to a string, for development purposes;
   * 
   * @param wordSize
   *          the size of the bit sequence
   * @param bits
   *          integer containing the bit sequence, in its least significant bits
   * @return string with set bits represented by '1', cleared bits by '.'
   */
  public static String bitString(int wordSize, int bits) {
    StringBuilder sb = new StringBuilder();
    for (int s = wordSize - 1; s >= 0; s--) {
      int val = bits & (1 << s);
      if (val != 0)
        sb.append('1');
      else
        sb.append('.');
    }
    return sb.toString();
  }

  /**
   * Convert all bits of an int to a string representation; see bitString(int,
   * int)
   */
  public static String bitString(int bits) {
    return bitString(Integer.SIZE, bits);
  }

  /**
   * Construct string containing a representation of a sequence of integers, one
   * on each line; see bitString(int)
   */
  public static String bitString(int[] ints) {
    StringBuilder sb = new StringBuilder();
    for (int x : ints) {
      sb.append(bitString(x));
      sb.append('\n');
    }
    return sb.toString();
  }

  /**
   * Pack a little-endian array of bytes to an array of shorts
   */
  public static short[] bytesToShortsLittleEndian(byte[] bytes) {
    if ((bytes.length & 1) != 0)
      throw badArg("byte array length not multiple of 2:", bytes.length);
    int outputCount = bytes.length >> 1;
    short[] output = new short[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += 2) {
      int low = ((int) bytes[i]) & 0xff;
      int high = ((int) bytes[i + 1]) & 0xff;
      output[j++] = (short) ((high << 8) | low);
    }
    return output;
  }

  /**
   * Pack a big-endian array of bytes to an array of shorts
   */
  public static short[] bytesToShortsBigEndian(byte[] bytes) {
    if ((bytes.length & 1) != 0)
      throw badArg("byte array length not multiple of 2:", bytes.length);
    int outputCount = bytes.length >> 1;
    short[] output = new short[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += 2) {
      int high = ((int) bytes[i]) & 0xff;
      int low = ((int) bytes[i + 1]) & 0xff;
      output[j++] = (short) ((high << 8) | low);
    }
    return output;
  }

  /**
   * Pack a little-endian array of bytes to an array of ints
   */
  public static int[] bytesToIntsLittleEndian(byte[] bytes) {
    if ((bytes.length & (Integer.BYTES - 1)) != 0)
      throw badArg("byte array length not multiple of 4:", bytes.length);
    int outputCount = bytes.length >> 2;
    int[] output = new int[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += 4) {
      int b0 = ((int) bytes[i]) & 0xff;
      int b1 = ((int) bytes[i + 1]) & 0xff;
      int b2 = ((int) bytes[i + 2]) & 0xff;
      int b3 = ((int) bytes[i + 3]) & 0xff;
      output[j++] = ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
    }
    return output;
  }

  /**
   * Pack a big-endian array of bytes to an array of ints
   */
  public static int[] bytesToIntsBigEndian(byte[] bytes) {
    if ((bytes.length & (Integer.BYTES - 1)) != 0)
      throw badArg("byte array length not multiple of 4:", bytes.length);
    int outputCount = bytes.length >> 2;
    int[] output = new int[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += 4) {
      int b3 = ((int) bytes[i]) & 0xff;
      int b2 = ((int) bytes[i + 1]) & 0xff;
      int b1 = ((int) bytes[i + 2]) & 0xff;
      int b0 = ((int) bytes[i + 3]) & 0xff;
      output[j++] = ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
    }
    return output;
  }

  /**
   * Extract little-endian bytes from an array of shorts
   */
  public static byte[] shortsToBytesLittleEndian(short[] shorts) {
    int numShorts = shorts.length;
    byte[] bytes = new byte[numShorts * Short.BYTES];
    int bi = 0;
    for (int si = 0; si != numShorts; si++, bi += 2) {
      bytes[bi] = (byte) (shorts[si] & 0x00ff);
      bytes[bi + 1] = (byte) ((shorts[si] & 0xff00) >> 8);
    }
    return bytes;
  }

  /**
   * Extract little-endian bytes from an array of ints
   */
  public static byte[] intsToBytesLittleEndian(int[] ints) {
    int numInts = ints.length;
    byte[] bytes = new byte[numInts * Integer.BYTES];
    int bi = 0;
    for (int si = 0; si != numInts; si++, bi += Integer.BYTES) {
      int source = ints[si];
      bytes[bi + 0] = (byte) (source >>> (8 * 0));
      bytes[bi + 1] = (byte) (source >>> (8 * 1));
      bytes[bi + 2] = (byte) (source >>> (8 * 2));
      bytes[bi + 3] = (byte) (source >>> (8 * 3));
    }
    return bytes;
  }

  /**
   * Extract big-endian bytes from an array of ints
   */
  public static byte[] intsToBytesBigEndian(int[] integers) {
    byte[] result = new byte[integers.length << 2];
    int byteIndex = 0;
    for (int d : integers) {
      // It doesn't really matter whether we use >>> or >>, but to stress that
      // we are treating the ints as unsigned, use >>>
      result[byteIndex] = (byte) (d >>> 24);
      result[byteIndex + 1] = (byte) (d >>> 16);
      result[byteIndex + 2] = (byte) (d >>> 8);
      result[byteIndex + 3] = (byte) (d);
      byteIndex += 4;
    }
    return result;
  }

  /**
   * Pack a little-endian array of bytes to an array of longs
   */
  public static long[] bytesToLongsLittleEndian(byte[] bytes) {
    if ((bytes.length & (Long.BYTES - 1)) != 0)
      throw badArg("byte array length not multiple of 8:", bytes.length);
    int outputCount = bytes.length / Long.BYTES;
    long[] output = new long[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += Long.BYTES) {

      int b0 = ((int) bytes[i]) & 0xff;
      int b1 = ((int) bytes[i + 1]) & 0xff;
      int b2 = ((int) bytes[i + 2]) & 0xff;
      int b3 = ((int) bytes[i + 3]) & 0xff;

      int b4 = ((int) bytes[i + 4]) & 0xff;
      int b5 = ((int) bytes[i + 5]) & 0xff;
      int b6 = ((int) bytes[i + 6]) & 0xff;
      int b7 = ((int) bytes[i + 7]) & 0xff;

      int upper_int = (b7 << (8 * 3)) | (b6 << (8 * 2)) | (b5 << (8 * 1)) | (b4 << (8 * 0));
      int lower_int = (b3 << (8 * 3)) | (b2 << (8 * 2)) | (b1 << (8 * 1)) | (b0 << (8 * 0));

      // We must mask out the upper 32 sign extension bits that might have been set after promoting the 
      // lower_int to a long: 
      //
      output[j++] = (((long) upper_int) << 32) | (((long) lower_int) & 0xffffffffL);
    }
    return output;
  }

  /**
   * Pack a big-endian array of bytes to an array of longs
   */
  public static long[] bytesToLongsBigEndian(byte[] bytes) {
    if ((bytes.length & (Long.BYTES - 1)) != 0)
      throw badArg("byte array length not multiple of 8:", bytes.length);
    int outputCount = bytes.length / Long.BYTES;
    long[] output = new long[outputCount];
    int j = 0;
    for (int i = 0; i < bytes.length; i += Long.BYTES) {

      int b7 = ((int) bytes[i]) & 0xff;
      int b6 = ((int) bytes[i + 1]) & 0xff;
      int b5 = ((int) bytes[i + 2]) & 0xff;
      int b4 = ((int) bytes[i + 3]) & 0xff;

      int b3 = ((int) bytes[i + 4]) & 0xff;
      int b2 = ((int) bytes[i + 5]) & 0xff;
      int b1 = ((int) bytes[i + 6]) & 0xff;
      int b0 = ((int) bytes[i + 7]) & 0xff;

      int upper_int = (b7 << (8 * 3)) | (b6 << (8 * 2)) | (b5 << (8 * 1)) | (b4 << (8 * 0));
      int lower_int = (b3 << (8 * 3)) | (b2 << (8 * 2)) | (b1 << (8 * 1)) | (b0 << (8 * 0));

      // We must mask out the upper 32 sign extension bits that might have been set after promoting the 
      // lower_int to a long: 
      //
      output[j++] = (((long) upper_int) << 32) | (((long) lower_int) & 0xffffffffL);
    }
    return output;
  }

  /**
   * Extract little-endian bytes from an array of longs
   */
  public static byte[] longsToBytesLittleEndian(long[] longs) {
    byte[] result = new byte[longs.length * Long.BYTES];
    int byteIndex = 0;
    for (long d : longs) {
      // It doesn't really matter whether we use >>> or >>, but to stress that
      // we are treating the ints as unsigned, use >>>
      result[byteIndex + 0] = (byte) (d >>> (8 * 0));
      result[byteIndex + 1] = (byte) (d >>> (8 * 1));
      result[byteIndex + 2] = (byte) (d >>> (8 * 2));
      result[byteIndex + 3] = (byte) (d >>> (8 * 3));
      result[byteIndex + 4] = (byte) (d >>> (8 * 4));
      result[byteIndex + 5] = (byte) (d >>> (8 * 5));
      result[byteIndex + 6] = (byte) (d >>> (8 * 6));
      result[byteIndex + 7] = (byte) (d >>> (8 * 7));
      byteIndex += Long.BYTES;
    }
    return result;
  }

  /**
   * Extract big-endian bytes from an array of longs
   */
  public static byte[] longsToBytesBigEndian(long[] longs) {
    byte[] result = new byte[longs.length * Long.BYTES];
    int byteIndex = 0;
    for (long d : longs) {
      // It doesn't really matter whether we use >>> or >>, but to stress that
      // we are treating the ints as unsigned, use >>>
      result[byteIndex + 0] = (byte) (d >>> (8 * 7));
      result[byteIndex + 1] = (byte) (d >>> (8 * 6));
      result[byteIndex + 2] = (byte) (d >>> (8 * 5));
      result[byteIndex + 3] = (byte) (d >>> (8 * 4));
      result[byteIndex + 4] = (byte) (d >>> (8 * 3));
      result[byteIndex + 5] = (byte) (d >>> (8 * 2));
      result[byteIndex + 6] = (byte) (d >>> (8 * 1));
      result[byteIndex + 7] = (byte) (d >>> (8 * 0));
      byteIndex += Long.BYTES;
    }
    return result;
  }

  /**
   * Read big-endian floats from byte array
   */
  public static float[] bytesToFloatsBigEndian(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.BIG_ENDIAN);
    float[] dest = new float[bytes.length / Float.BYTES];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  /**
   * Read little-endian floats from byte array
   */
  public static float[] bytesToFloatsLittleEndian(byte[] bytes) {
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    float[] dest = new float[bytes.length / Float.BYTES];
    byteBuffer.asFloatBuffer().get(dest);
    return dest;
  }

  public static byte[] toByteArray(float[] floats) {
    return toByteArray(floats, ByteOrder.BIG_ENDIAN);
  }

  public static byte[] toByteArray(float[] floats, ByteOrder order) {
    ByteBuffer buffer = ByteBuffer.allocate(floats.length * Float.BYTES).order(order);
    for (float value : floats)
      buffer.putFloat(value);
    return buffer.array();
  }

  private static String removeDataTypeSuffix(String string, String optionalSuffix) {
    if (string.length() >= 2) {
      int suffixStart = string.length() - 2;
      if (string.charAt(suffixStart) == '`') {
        char existingSuffixChar = string.charAt(suffixStart + 1);
        if (existingSuffixChar != optionalSuffix.charAt(1))
          throw badArg("string has suffix", quote(string.charAt(suffixStart)), "expected",
              quote(optionalSuffix));
        string = string.substring(0, suffixStart);
      }
    }
    return string;
  }

  /**
   * Parse a base64 string to an array of bytes
   */
  public static byte[] parseBase64(String string) {
    string = removeDataTypeSuffix(string, DATA_TYPE_SUFFIX_BYTE);
    return Base64.getDecoder().decode(string);
  }

  public static final String DATA_TYPE_DELIMITER = "`";
  public static final String DATA_TYPE_SUFFIX_BYTE = DATA_TYPE_DELIMITER + "b";
  public static final String DATA_TYPE_SUFFIX_SHORT = DATA_TYPE_DELIMITER + "s";
  public static final String DATA_TYPE_SUFFIX_INT = DATA_TYPE_DELIMITER + "i";
  public static final String DATA_TYPE_SUFFIX_LONG = DATA_TYPE_DELIMITER + "l";

  /**
   * Encode a byte array as a Base64 string, with our data type suffix added
   */
  public static String encodeBase64(byte[] byteArray) {
    return Base64.getEncoder().encodeToString(byteArray) + DATA_TYPE_SUFFIX_BYTE;
  }

  /**
   * Parse a base64 string to an array of shorts (using little-endian bytes as
   * an intermediate encoding)
   */
  public static short[] parseBase64Shorts(String string) {
    string = removeDataTypeSuffix(string, DATA_TYPE_SUFFIX_SHORT);
    return bytesToShortsLittleEndian(Base64.getDecoder().decode(string));
  }

  /**
   * Encode an array of shorts to a Base64 string (using little-endian bytes as
   * an intermediate encoding); add our data type suffix
   */
  public static String encodeBase64(short[] shortArray) {
    return Base64.getEncoder().encodeToString(shortsToBytesLittleEndian(shortArray)) + DATA_TYPE_SUFFIX_SHORT;
  }

  /**
   * Parse a base64 string to an array of ints (using little-endian bytes as an
   * intermediate encoding)
   */
  public static int[] parseBase64Ints(String string) {
    string = removeDataTypeSuffix(string, DATA_TYPE_SUFFIX_INT);
    return bytesToIntsLittleEndian(Base64.getDecoder().decode(string));
  }

  /**
   * Parse a base64 string to an array of longs (using little-endian bytes as an
   * intermediate encoding)
   */
  public static long[] parseBase64Longs(String string) {
    string = removeDataTypeSuffix(string, DATA_TYPE_SUFFIX_LONG);
    return bytesToLongsLittleEndian(Base64.getDecoder().decode(string));
  }

  /**
   * Encode an array of longs to a Base64 string (using little-endian bytes as
   * an intermediate encoding); add our data type suffix
   */
  public static String encodeBase64(long[] intArray) {
    byte[] bytes = longsToBytesLittleEndian(intArray);
    return Base64.getEncoder().encodeToString(bytes) + DATA_TYPE_SUFFIX_LONG;
  }

  /**
   * Encode an array of ints to a Base64 string (using little-endian bytes as an
   * intermediate encoding); add our data type suffix
   */
  public static String encodeBase64(int[] intArray) {
    return Base64.getEncoder().encodeToString(intsToBytesLittleEndian(intArray)) + DATA_TYPE_SUFFIX_INT;
  }

  /**
   * Attempt to parse a value from a string, to agree with a particular data
   * type
   */
  public static Object parseValueFromString(String valueAsString, Class dataTypeClass) {
    Function<String, Object> parser = sDataTypeParserMap.get(dataTypeClass);
    if (parser == null) {
      // If it's an enum type, use an enum parser
      if (dataTypeClass.getSuperclass() == java.lang.Enum.class) {
        parser = new Function<String, Object>() {
          @SuppressWarnings("unchecked")
          @Override
          public Object apply(String t) {
            return Enum.valueOf(dataTypeClass, valueAsString.toUpperCase());
          }
        };
      }
    }
    if (parser == null) {
      throw badArg("Cannot parse", quote(valueAsString), "-- no parser found for class",
          dataTypeClass.getName());
    }
    try {
      Object result = parser.apply(valueAsString);
      return result;
    } catch (Throwable t) {
      throw badArg("Unable to parse", quote(valueAsString), "as instance of", dataTypeClass.getName());
    }
  }

  @SuppressWarnings("unchecked")
  public static <T> List<T> parseListOfObjects(JSList sourceListOrNull, boolean nullIfSourceNull) {
    if (sourceListOrNull == null)
      return nullIfSourceNull ? null : emptyList();
    return (List<T>) immutableCopyOf(sourceListOrNull.wrappedList());
  }

  @SuppressWarnings("unchecked")
  public static <T extends AbstractData> List<T> parseListOfObjects(T defaultInstance,
      JSList sourceListOrNull, boolean nullIfSourceNull) {
    if (sourceListOrNull == null)
      return nullIfSourceNull ? null : emptyList();
    List<T> items = new ArrayList<>(sourceListOrNull.size());
    for (Object obj : sourceListOrNull.wrappedList()) {
      if (NULL_LIST_ELEMENTS_ALLOWED) {
        T result = null;
        if (obj != null)
          result = (T) defaultInstance.parse(obj);
        items.add(result);
      } else {
        T result = (T) defaultInstance.parse(obj);
        // I think it is a bad idea to return a null object if parsing failed.
        if (result == null)
          badArg("null item parsed from:", obj, CR, "source:", INDENT, sourceListOrNull, OUTDENT,
              "defaultInstance:", INDENT, defaultInstance);
        items.add(result);
      }
    }
    return Collections.unmodifiableList(items);
  }

  /**
   * Construct an interable for a specific type
   */
  public static <T> Iterable<T> castingIterable(final Iterator generalIterator) {
    return new Iterable<T>() {
      @Override
      public Iterator<T> iterator() {
        return new Iterator<T>() {

          @Override
          public boolean hasNext() {
            return generalIterator.hasNext();
          }

          @SuppressWarnings("unchecked")
          @Override
          public T next() {
            return (T) generalIterator.next();
          }
        };
      }
    };
  }

  private static final Map<Class, Function<String, Object>> sDataTypeParserMap = hashMap();

  static {
    Map<Class, Function<String, Object>> m = sDataTypeParserMap;
    m.put(String.class, (x) -> x);
    m.put(File.class, (x) -> new File(x));
    m.put(Integer.class, (x) -> Integer.parseInt(x));
    m.put(Long.class, (x) -> Long.parseLong(x));
    m.put(Float.class, DataUtil::guardedParseFloat);
    m.put(Double.class, DataUtil::guardedParseDouble);
  }

  private static float guardedParseFloat(String x) {
    float result = Float.parseFloat(x);
    if (!Float.isFinite(result))
      throw badArg("not finite");
    return result;
  }

  private static double guardedParseDouble(String x) {
    double result = Double.parseDouble(x);
    if (!Double.isFinite(result))
      throw badArg("not finite");
    return result;
  }

  // ------------------------------------------------------------------
  // Calculating hash values
  // ------------------------------------------------------------------

  /**
   * Get a 31 bit, unsigned checksum of a byte array
   */
  public static int checksum(byte[] bytes) {
    Checksum cs = new CRC32();
    cs.update(bytes, 0, bytes.length);
    return (int) (cs.getValue() & 0x7fff);
  }

  /**
   * Get a 31 bit, unsigned checksum of a file's contents
   */
  public static int checksum(File file) {
    return checksum(Files.toByteArray(file, "checksum"));
  }

  public static String hashOf(byte[] bytes) {
    return hashOf(bytes, 1000);
  }

  public static String hashOf(byte[] bytes, int maxLengthInBytes) {
    try {
      MessageDigest md = MessageDigest.getInstance("SHA-256");
      md.update(bytes);
      byte[] mdbytes = md.digest();
      StringBuilder sb = new StringBuilder();
      maxLengthInBytes = Math.min(maxLengthInBytes, mdbytes.length);
      for (int i = 0; i < maxLengthInBytes; i++)
        byteToHexString(mdbytes[i], sb);
      return sb.toString();
    } catch (Throwable t) {
      throw asRuntimeException(t);
    }
  }

  public static String hashOfString(String string) {
    return hashOf(string.getBytes());
  }

  /**
   * We supply our own version of this, since Integer.toHexString() doesn't
   * insert leading zeros
   */
  private static void byteToHexString(byte b, StringBuilder dest) {
    String hexChars = "0123456789abcdef";
    dest.append(hexChars.charAt((b >> 4) & 0xf));
    dest.append(hexChars.charAt(b & 0xf));
  }

  private static final int MAX_JSON_LENGTH = 100000;

  /**
   * Parse bytes as json header + payload.
   *
   * @param byteArray
   *          bytes, with LITTLE_ENDIAN byte order, with format:
   * 
   *          [4] length (n) of json [n] json map [x] binary payload
   */
  public static Pair<JSMap, byte[]> parseJsonAndPayload(byte[] byteArray) {

    ByteBuffer bytes = ByteBuffer.wrap(byteArray);
    bytes.order(ByteOrder.LITTLE_ENDIAN);

    int jsonLength = bytes.getInt();

    if (jsonLength < 0 || jsonLength > MAX_JSON_LENGTH) {
      JSMap m = map();
      m.put("", "Bad json+payload");
      m.put("length", byteArray.length);
      m.put("json length", jsonLength);
      String message = m.prettyPrint();
      //reportInfrequently("bad_post_request", 5 * 60, m.get(""), message);
      throw new IllegalArgumentException(message);
    }
    JSMap json = readJsonMap(jsonLength, bytes);
    byte[] payload = new byte[bytes.remaining()];
    bytes.get(payload);
    return new Pair<>(json, payload);
  }

  private static JSMap readJsonMap(int length, ByteBuffer buffer) {
    byte[] jsonBytes = new byte[length];
    buffer.get(jsonBytes);
    String jsonString = new String(jsonBytes);
    return new JSMap(jsonString);
  }

  /**
   * Encode a JSMap and payload as a byte array (i.e. the reverse operation of
   * parseJsonAndPayload)
   */
  public static byte[] encodeJsonAndPayload(JSMap json, byte[] payload) {
    byte[] jsonBytes = json.toString().getBytes();
    checkArgument(jsonBytes.length <= MAX_JSON_LENGTH, "JSMap is too big");

    byte[] bytes = new byte[Integer.BYTES + jsonBytes.length + payload.length];
    ByteBuffer byteBuffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN);
    byteBuffer.putInt(jsonBytes.length);
    byteBuffer.put(jsonBytes);
    byteBuffer.put(payload);
    return bytes;
  }

  // ------------------------------------------------------------------
  // Displaying bytes as hex 
  // ------------------------------------------------------------------

  public static String hex8(int value) {
    return String.format("%02x", value & 0xff);
  }

  public static String hex16(int value) {
    return String.format("%04x", value & 0xffff);
  }

  public static String hex32(int value) {
    return String.format("%08x", value & 0xffffffff);
  }

  /**
   * Convert value to hex, store in StringBuilder
   *
   * @param sb
   *          where to store result, or null
   * @param value
   *          value to convert
   * @param digits
   *          number of hex digits to output
   * @return result
   */
  public static StringBuilder toHex(StringBuilder sb, int value, int digits, boolean stripLeadingZeros,
      boolean groupsOfFour) {
    if (sb == null)
      sb = new StringBuilder();

    boolean nonZeroSeen = !stripLeadingZeros;

    while (digits-- > 0) {
      int shift = digits << 2;
      int v = (value >> shift) & 0xf;
      if (v != 0 || digits == 0)
        nonZeroSeen = true;

      char c;
      if (!nonZeroSeen) {
        c = ' ';

      } else {
        if (v < 10) {
          c = (char) ('0' + v);
        } else {
          c = (char) ('a' + (v - 10));
        }
      }
      sb.append(c);
      if (groupsOfFour && (digits & 3) == 0 && digits != 0) {
        if (!nonZeroSeen)
          sb.append(' ');
        else
          sb.append('_');
      }
    }
    return sb;
  }

  public static String hexDump(int[] intArray, int offset, int length) {
    byte[] b = new byte[length * 4];
    int j = offset;
    int i = 0;
    while (j < offset + length) {
      int v = intArray[j];
      b[i++] = (byte) (v & 0xff);
      b[i++] = (byte) ((v >> 8) & 0xff);
      b[i++] = (byte) ((v >> 16) & 0xff);
      b[i++] = (byte) ((v >> 24) & 0xff);
      j++;
    }
    return hexDump(b);
  }

  public static String hexDump(byte[] byteArray) {
    return hexDump(byteArray, 0, byteArray.length);
  }

  public static String hexDump(byte[] byteArray, int offset, int length) {

    int groupSize = (1 << 2); // Must be power of 2

    int rowSize = 16;
    boolean hideZeroes = true;
    boolean groups = true;
    boolean absoluteIndex = false;
    boolean withASCII = false;

    StringBuilder sb = new StringBuilder("\n");
    int i = 0;
    while (i < length) {
      int rSize = rowSize;
      if (rSize + i > length)
        rSize = length - i;
      int address = absoluteIndex ? i + offset : i;
      toHex(sb, address, 4, true, false);
      sb.append(": ");
      if (groups)
        sb.append("| ");
      for (int j = 0; j < rowSize; j++) {
        if (j < rSize) {
          byte val = byteArray[offset + i + j];
          if (hideZeroes && val == 0) {
            sb.append("  ");
          } else {
            toHex(sb, val, 2, false, false);
          }
        } else {
          sb.append("  ");
        }
        sb.append(' ');
        if (groups) {
          if ((j & (groupSize - 1)) == groupSize - 1)
            sb.append("| ");
        }
      }
      if (withASCII) {
        sb.append(' ');
        for (int j = 0; j < rSize; j++) {
          byte v = byteArray[offset + i + j];
          if (v < 0x20 || v >= 0x80)
            v = '.';
          sb.append((char) v);
          if (groups && ((j & (groupSize - 1)) == groupSize - 1)) {
            sb.append(' ');
          }
        }
      }
      sb.append('\n');
      i += rSize;
    }
    return sb.toString();
  }

  // ------------------------------------------------------------------
  // Compression
  // ------------------------------------------------------------------

  /**
   * Compress an array of bytes
   */
  public static byte[] compress(byte[] data) {
    Deflater deflater = new Deflater();
    deflater.setInput(data);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(data.length);
    deflater.finish();
    byte[] buffer = new byte[1024];
    while (!deflater.finished()) {
      int count = deflater.deflate(buffer);
      outputStream.write(buffer, 0, count);
    }
    Files.close(outputStream);
    return outputStream.toByteArray();
  }

  /**
   * Decompress an array of bytes
   */
  public static byte[] decompress(byte[] compressedData) {
    Inflater inflater = new Inflater();
    inflater.setInput(compressedData);
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream(compressedData.length);
    byte[] buffer = new byte[1024];
    try {
      while (!inflater.finished()) {
        int count = inflater.inflate(buffer);
        outputStream.write(buffer, 0, count);
      }
    } catch (DataFormatException e) {
      throw asRuntimeException(e);
    }
    Files.close(outputStream);
    return outputStream.toByteArray();
  }

  public static byte[] compress(BitSet bitSet) {
    return compress(bitSet.toByteArray());
  }

  public static BitSet decompressBitSet(byte[] compressedBitSet) {
    byte[] bytes = decompress(compressedBitSet);
    return BitSet.valueOf(bytes);
  }

  public static BitSet decompressBitSet(ByteArray compressedBitSet) {
    return decompressBitSet(compressedBitSet.array());
  }

  public static short[] copyOf(short[] source) {
    return Arrays.copyOf(source, source.length);
  }

  public static int[] copyOf(int[] source) {
    return Arrays.copyOf(source, source.length);
  }

  // ------------------------------------------------------------------
  // Field links
  // ------------------------------------------------------------------

  /**
   * <pre>
   * 
   * Field links are a mechanism to allow a data type to point to some other file containing the data type's value.
   * For example, let us define a data type Foo that wants to have a field representing an instance of type Bar.
   * 
   * By convention, Foo's field link to a Bar instance consists of two fields, an optional value, and a link
   * to an external value:
   * 
   * # foo.dat
   * #
   * fields {
   *       :
   *    ? Bar bar;
   *    File bar_path;
   *       :
   * }
   * 
   * If the optional field bar is non null, then that is the value of Bar that is used.  Otherwise, the field link
   * is *resolved* by parsing the bar_path field.  This is a File with the structure:
   *      
   *      <target_file> ( ',' <key> )*
   * 
   * <target_file> is the path to a file containing a JSMap (typically the serialization of some other data type),
   * and <key> is a list of keys defining a path through the JSMap to reach the desired JSMap representing the
   * Foo to be returned.
   * 
   * </pre>
   */

  /**
   * Resolve a field link. See discussion above.
   * 
   * @param baseDirectoryOrNull
   *          if not null, a directory to serve as the base directory for the
   *          path argument (if it is relative)
   * @param fieldPrototype
   *          an instance of the returned type, to use as a parser
   * @param optionalField
   *          if non-null, this value is returned
   * @param path
   *          if optionalField is null, this path is parsed as a comma-delimited
   *          sequence of strings, the first of which is a path to a file
   *          containing a JSMap (A), and the subsequent strings are a set of
   *          keys pointing to a nested JSMap (B) within (A). B is parsed and
   *          returned
   * @return value, either optionalField or B
   */
  @SuppressWarnings("unchecked")
  public static <F extends AbstractData> F resolveField(File baseDirectoryOrNull, F fieldPrototype,
      F optionalField, File path) {
    if (optionalField != null)
      return optionalField;
    JSMap jsonMap = resolveJSMap(baseDirectoryOrNull, path);
    if (jsonMap == null)
      return null;
    return (F) fieldPrototype.parse(jsonMap);
  }

  private static JSMap resolveJSMap(File baseDirectoryOrNull, File linkedFile) {
    if (Files.empty(linkedFile))
      return null;

    String pathStr = linkedFile.toString();
    List<String> elements = Tools.split(pathStr, ',');

    String relativePath = elements.get(0);
    remove(elements, 0, 1);
    linkedFile = Files.fileWithinOptionalDirectory(new File(relativePath), baseDirectoryOrNull);
    JSMap sourceMap = JSMap.from(linkedFile);

    JSMap nestedMap = sourceMap;
    for (String key : elements) {
      JSMap childMap = nestedMap.optJSMap(key);
      if (childMap == null)
        badArg("no key found:", key, "parsing:", INDENT, sourceMap, OUTDENT, "link path:", pathStr);
      nestedMap = childMap;
    }
    return nestedMap;
  }

}
