package js.data;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.base.BaseObject;
import js.parsing.RegExp;

public final class BinaryCodec {

  private static Pattern BASE64_PATTERN = RegExp.pattern("\"[A-Za-z0-9+\\/]+={0,2}(?:`[bsilfd])?\"");
  private static int VERSION_1 = 0xfe;

  public static byte[] encode(CharSequence c) {
    return new OurEncoder(c).result();
  }

  private static class Chunk {
    int length; // length of uncompressed bytes
    int offset; // where to insert the base64 sequence within the json template
    byte[] bytes; // optional, bytes decoded from base64
  }

  public static String decode(byte[] encodedBytes) {
    ByteParser parser = new ByteParser(encodedBytes);
    int header = parser.readInt();
    int version = (int) ((header >> 24) & 0xff);
    if (version != VERSION_1)
      badArg("expected version:", VERSION_1, "got:", version);
    int chunkCount = header & ((1 << 24) - 1);
    if (chunkCount < 1)
      badArg("bad chunk count in header:", header);

    StringBuilder sb = new StringBuilder((int) (encodedBytes.length * 4f / 3));

    // Read chunk #0, the json template
    int len = parser.readInt();
    parser.readInt(); // offset for json template is unused
    String template = parser.readString(len);
    int cursor = 0;

    for (int chunkNumber = 1; chunkNumber < chunkCount; chunkNumber++) {
      int chunkLen = parser.readInt();
      int offset = parser.readInt();
      if (cursor < offset) {
        sb.append(template.substring(cursor, offset));
        cursor = offset;
      }
      String base64String = Base64.getEncoder().encodeToString(parser.readBytes(chunkLen));
      sb.append(base64String);
    }
    int offset = template.length();
    if (cursor < offset) {
      sb.append(template.substring(cursor, offset));
    }
    return sb.toString();
  }

  private static class ByteParser {

    ByteParser(byte[] buffer) {
      mBuffer = buffer;
    }

    int readInt() {
      if (mCursor + 4 > mBuffer.length)
        throw badState("reached end of byte buffer");
      int b3 = ((int) mBuffer[mCursor + 0]) & 0xff;
      int b2 = ((int) mBuffer[mCursor + 1]) & 0xff;
      int b1 = ((int) mBuffer[mCursor + 2]) & 0xff;
      int b0 = ((int) mBuffer[mCursor + 3]) & 0xff;
      mCursor += 4;
      return ((b3 << 24) | (b2 << 16) | (b1 << 8) | b0);
    }

    String readString(int length) {
      String result = new String(mBuffer, mCursor, length);
      mCursor += length;
      return result;
    }

    byte[] readBytes(int length) {
      if (mCursor + length > mBuffer.length)
        throw badState("reached end of byte buffer");
      int newCursor = mCursor + length;
      byte[] result = Arrays.copyOfRange(mBuffer, mCursor, newCursor);
      mCursor = newCursor;
      return result;
    }

    private byte[] mBuffer;
    private int mCursor;
  }

  private static class OurEncoder extends BaseObject {

    OurEncoder(CharSequence input) {
      asText = input.toString();
    }

    byte[] result() {
      if (mResult != null)
        return mResult;

      List<Chunk> chunks = arrayList();

      StringBuilder trimmedJsonMap = new StringBuilder();

      int cursor = 0;
      Matcher m = BASE64_PATTERN.matcher(asText);

      while (m.find()) {
        int matchStart = m.start();
        int matchEnd = m.end();

        int base64Start = matchStart + 1; // past   "
        int base64End;

        // Determine if it included the optional `[bsildf]
        //
        base64End = matchEnd - 3;
        if (base64End < base64Start || asText.charAt(base64End) != '`')
          base64End = matchEnd - 1; // before "

        int base64Length = base64End - base64Start;

        if (verbose())
          log("found base64 section of from:", base64Start, "to:", base64End, "length:", base64Length,
              quote(asText.substring(base64Start, base64End)));

        // If the substring isn't a multiple of 4, assume it isn't base64 (we expect any base64
        // encoded data to have appropriate padding characters '=')
        //
        if (base64Length % 4 != 0)
          continue;

        // Don't encode it if the overhead of doing so means we aren't saving memory
        //
        // n (base64) input bytes -> 3n/4 + 8 output bytes
        //
        //  breakeven is when (3n/4 + 8) = n,  or n = 32
        //
        if (base64Length <= 32) {
          log("not bothering to encode it");
          continue;
        }

        String base64Substring = asText.substring(base64Start, base64End);

        if (verbose())
          log("potential base64 string:", quote(base64Substring));
        byte[] decodedBase64Bytes;
        try {
          decodedBase64Bytes = Base64.getDecoder().decode(base64Substring);
          log("decoded base64 to byte array of length:", decodedBase64Bytes.length);
        } catch (IllegalArgumentException e) {
          // this wasn't a base64 encoded string, so ignore this match
          continue;
        }

        // append fragment preceding this section to the json template
        trimmedJsonMap.append(asText.substring(cursor, base64Start));
        log("appended fragment preceding section, now:", INDENT, quote(trimmedJsonMap));
        cursor = base64End;

        Chunk c = new Chunk();
        c.length = decodedBase64Bytes.length;
        c.bytes = decodedBase64Bytes;
        c.offset = trimmedJsonMap.length();
        chunks.add(c);
      }

      // append any remaining fragment
      if (cursor < asText.length())
        trimmedJsonMap.append(asText.substring(cursor, asText.length()));

      log("json template:", INDENT, trimmedJsonMap);

      // Now use chunks to produce result

      mBytesBuffer = ByteArray.newBuilder();
      // ib.add(VERSION_1);

      // Encode number of chunks in next three bytes (in big-endian order)

      // Include one for the template chunk, which is first
      int chunkCount = 1 + chunks.size();
      checkArgument(chunkCount < (1 << 24), "too many chunks");

      addInt(chunkCount | (VERSION_1 << 24));

      String jsonTemplate = trimmedJsonMap.toString();
      byte[] jsonTemplateBytes = jsonTemplate.getBytes();

      // Store chunk #0: the json template
      addInt(jsonTemplateBytes.length);
      addInt(0); // offset for chunk zero is not used
      mBytesBuffer.add(jsonTemplateBytes);

      //int offset = jsonTemplateBytes.length;

      for (Chunk c : chunks) {
        addInt(c.length);
        addInt(c.offset);
        mBytesBuffer.add(c.bytes);
      }
      mResult = mBytesBuffer.array();
      if (verbose())
        log("result:", INDENT, DataUtil.hexDump(mResult));
      return mResult;
    }

    private void addInt(int value) {
      ByteArray.Builder ib = mBytesBuffer;
      ib.add((byte) (value >> 24));
      ib.add((byte) (value >> 16));
      ib.add((byte) (value >> 8));
      ib.add((byte) (value));
    }

    private final String asText;
    private byte[] mResult;
    private ByteArray.Builder mBytesBuffer;
  }
}
