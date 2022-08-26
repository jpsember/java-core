package js.data;

import static js.base.Tools.*;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import js.json.JSMap;
import js.parsing.RegExp;

public final class BinaryCodec {

  private static Pattern BASE64_PATTERN = RegExp.pattern("\"[A-Za-z0-9+\\/]+={0,2}(?:`[bsilfd])?\"");
  private static int VERSION_1 = 0xfe;

  public static byte[] encode(JSMap json) {
    String asText = json.toString();

    List<Chunk> chunks = arrayList();

    StringBuilder trimmedJsonMap = new StringBuilder();

    int cursor = 0;
    Matcher m = BASE64_PATTERN.matcher(asText);
    while (m.find()) {
      int s = m.start();
      int e = m.end();

      int bstart = s + 1; // past   "
      int bend;

      // Determine if it included the optional `[bsildf]
      int optStart = e - 2;
      if (optStart >= bstart && asText.charAt(optStart) == '`')
        bend = e - 3; // before `[bsildf]"
      else
        bend = e - 1; // before "

      // don't bother encoding things that won't save much
      int blen = bend - bstart;
      if (blen < 8)
        continue;

      String base64Substring = asText.substring(bstart, bend);
      byte[] decodedBase64Bytes;
      try {
        decodedBase64Bytes = Base64.getDecoder().decode(base64Substring);
      } catch (IllegalArgumentException exc) {
        // this wasn't a base64 encoded string, so ignore this match
        continue;
      }

      // append fragment preceding this section to the json template
      trimmedJsonMap.append(asText.substring(cursor, bstart));
      cursor = bend;

      Chunk base64Chunk = new Chunk();
      //   base64Chunk.offset = trimmedJsonMap.length();
      base64Chunk.length = decodedBase64Bytes.length;
      base64Chunk.bytes = decodedBase64Bytes;
      chunks.add(base64Chunk);
    }

    // append any remaining fragment
    if (cursor < asText.length())
      trimmedJsonMap.append(asText.substring(cursor, asText.length()));

    // Now use chunks to produce result

    ByteArray.Builder ib = ByteArray.newBuilder();
    // ib.add(VERSION_1);

    // Encode number of chunks in next three bytes (in big-endian order)

    // Include one for the template chunk, which is first
    int chunkCount = 1 + chunks.size();
    checkArgument(chunkCount < (1 << 24), "too many chunks");

    addInt(ib, chunkCount | (VERSION_1 << 24));
    //    ib.add((byte) (chunkCount >> 16));
    //    ib.add((byte) (chunkCount >> 8));
    //    ib.add((byte) (chunkCount >> 0));

    String jsonTemplate = trimmedJsonMap.toString();
    byte[] jsonTemplateBytes = jsonTemplate.getBytes();

    // Store chunk #0: the json template
    addInt(ib, jsonTemplateBytes.length);
    addInt(ib, 0); // offset for chunk zero is not used
    ib.add(jsonTemplateBytes);

    int offset = jsonTemplateBytes.length;

    for (Chunk c : chunks) {
      addInt(ib, c.length);
      addInt(ib, offset);
      ib.add(c.bytes);
    }
    return ib.array();
  }

  private static void addInt(ByteArray.Builder ib, int value) {
    ib.add((byte) (value >> 24));
    ib.add((byte) (value >> 16));
    ib.add((byte) (value >> 8));
    ib.add((byte) (value));
  }

  private static class Chunk {
    int length; // length of uncompressed bytes
    //  int offset; // where to insert the base64 sequence within the json template
    byte[] bytes; // optional, bytes decoded from base64
  }

  public static JSMap decode(byte[] encodedBytes) {

    ByteParser parser = new ByteParser(encodedBytes);
    int header = parser.readInt();
    int version = (int) ((header >> 24) & 0xff);
    if (version != VERSION_1)
      badArg("expected version:", VERSION_1, "got:", version);
    int chunkCount = header & ((1 << 24) - 1);
    if (chunkCount < 1)
      badArg("bad chunk count in header:", header);

    StringBuilder sb = new StringBuilder();

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

      String base64String = 
      Base64.getEncoder().encodeToString(parser.readBytes(chunkLen));
      sb.append(base64String);
    }

    return new JSMap(sb);
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

}
