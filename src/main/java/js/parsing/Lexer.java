package js.parsing;

import static js.base.Tools.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import js.base.BaseObject;
import js.data.ByteArray;
import js.data.IntArray;
import js.json.JSList;

public class Lexer extends BaseObject {

  private static final boolean DEBUG2 = false && alert("DEBUG in effect");

  private static void p2(Object... messages) {
    if (DEBUG2)
      pr(insertStringToFront("Scanner>>>", messages));
  }

  public Lexer(DFA dfa) {
    mDfa = dfa;
  }

  public Lexer withText(CharSequence text) {
    return withBytes(text.toString().getBytes(StandardCharsets.UTF_8));
  }

  public Lexer withSourceDescription(String descr) {
    mSourceDescription = descr;
    return this;
  }

  public Lexer withBytes(byte[] sourceBytes) {
    mBytes = normalizeNewlines(sourceBytes);
    mStarted = false;
    return this;
  }

  public Lexer withNoSkip() {
    mSkipId = Lexeme.ID_SKIP_NONE;
    return this;
  }

  public Lexer withSkipId(int skipId) {
    checkArgument(skipId >= 0, "bad skipId");
    mSkipId = skipId;
    return this;
  }

  public Lexer withAcceptUnknownTokens() {
    mAcceptUnknownTokens = true;
    return this;
  }

  private static final byte LF = 0x0a;
  private static final byte CR = 0x0d;

  public static byte[] normalizeNewlines(byte[] sourceBytes) {
    var output = ByteArray.newBuilder();
    var srcLen = sourceBytes.length;
    var srcCursor = 0;
    while (srcCursor < srcLen) {
      var srcByte = sourceBytes[srcCursor];
      if (srcByte == CR && srcCursor + 1 < srcLen && sourceBytes[srcCursor + 1] == LF) {
        // We're at a ...CR, LF... pair.
        // Don't write anything; increment the source cursor only (to skip the CR)
      } else {
        // Copy the source byte to the destination, and increment both cursors
        output.add(srcByte);
      }
      srcCursor++;
    }
    // Add zero marking the end of input
    output.add((byte) 0);
    return output.array();
  }


  static final int //
      F_TOKEN_OFFSET = 0,
      F_TOKEN_ID = 1,
      F_LINE_NUMBER = 2,
      F_TOTAL = 3;

  public static int TOKEN_INFO_REC_LEN = F_TOTAL;

  public boolean hasNext() {
    start();
    return mReadIndex < mTokenCount;
  }

  /**
   * Look at an upcoming token, without reading it
   *
   * @param distance number of tokens to look ahead (0 is next token, 1 is the one after that, ...)
   * @return Lexeme
   */
  public Lexeme peek(int distance) {
    return checkUnknownAllowed(auxPeek(distance));
  }

  /**
   * Look at an upcoming token, without reading it
   *
   * @param distance number of tokens to look ahead (0 is next token, 1 is the one after that, ...)
   * @return Lexeme
   */
  private Lexeme auxPeek(int distance) {
    start();
    resetAction();
    int i = distance + mReadIndex;
    if (i < 0 || i >= mTokenCount) {
      return new Lexeme(Lexeme.ID_END_OF_INPUT);
    } else {
      var j = filteredIndexToAddress(i);
      return constructLexeme(j);
    }
  }

  /**
   * Look at next token, without reading it
   */
  public Lexeme peek() {
    return peek(0);
  }

  /**
   * Read the next token
   */
  public Lexeme read() {
    return read(Lexeme.ID_SKIP_NONE);
  }


  /**
   * Read the next token.  Throws an exception if token is missing or id doesn't match the
   * expected type.  This is the same as read(int ... expectedIds), except that it returns
   * the Token as a scalar value, instead of being within a List
   *
   * @param expectedId id of expected token, or Lexeme.ID_SKIP_NONE
   * @return the read token (as a scalar value, instead of being within a List)
   */
  public Lexeme read(int expectedId) {
    var x = auxPeek(0);
    if (x.isEndOfInput())
      x.failWith("end of input");
    mReadIndex++;
    checkUnknownAllowed(x);
    if (expectedId != Lexeme.ID_SKIP_NONE && expectedId != x.id())
      x.failWith("expected id:", expectedId);
    return x;
  }

  /**
   * Determine if the next n tokens exist and match the specified ids
   */
  public boolean peekIf(int... tokenIds) {
    //p2("peekIs, tokenIds:", tokenIds);
    start();

    resetAction();

    boolean success = true;

    var distance = INIT_INDEX;
    for (var seekId : tokenIds) {
      distance++;
      var i = mReadIndex + distance;
      if (i >= mTokenCount) {
        success = false;
        break;
      }
      var ii = filteredIndexToAddress(i);
      if (!idMatch(tokenId(ii), seekId)) {
        success = false;
        break;
      }
    }

    if (success) {
      mActionLength = tokenIds.length;
      //p2("...setting prev token count:", mActionLength);
    }
    return success;
  }

  /**
   * If the next n tokens exist and match the specified ids, read them, and return true
   */
  public boolean readIf(int... tokenIds) {
    var result = peekIf(tokenIds);
    if (result) {
      mReadIndex += mActionLength;
      //p2("...readIf, advance cursor by prev token count", mActionLength, "to", mReadIndex);
    }
    return result;
  }

  /**
   * Consume the next token read or matched via call to peekIf() or readIf()
   */
  public Lexeme token() {
    start();
    if (mActionLength == 0)
      throw badState("no previous action");
    if (mActionCursor == mActionLength)
      throw badState("no tokens remain in action");
    var x = constructLexeme(filteredIndexToAddress(mActionCursor + mActionCursorStart));
    mActionCursor++;
    return checkUnknownAllowed(x);
  }

  /**
   * Return a particular token read or matched via call to peekIf() or readIf(),
   * of those that haven't already been consumed by a call to token()
   */
  public Lexeme token(int position) {
    start();
    if (mActionLength == 0)
      throw badState("no previous action");
    if (mActionCursor + position >= mActionLength)
      throw badState("no token at position");
    return checkUnknownAllowed(constructLexeme(filteredIndexToAddress(mActionCursorStart + mActionCursor + position)));
  }

  private Lexeme checkUnknownAllowed(Lexeme x) {
    if (!mAcceptUnknownTokens && x.isUnknown())
      x.failWith("unknown token");
    return x;
  }

  private void resetAction() {
    mActionCursor = 0;
    mActionLength = 0;
    mActionCursorStart = mReadIndex;
  }

  private Lexeme constructLexeme(int infoIndex) {
    return Lexeme.construct(this, infoIndex);
  }

  int[] tokenInfo() {
    return mTokenInfo;
  }

  void start() {
    if (!mStarted) {
      extractTokens();
      resetAction();
      mStarted = true;
    }
  }

  private void extractTokens() {
    checkState(mBytes != null, "no input bytes yet");

    var inputBytes = mBytes;

    var ti = IntArray.newBuilder();
    var filteredPtrs = IntArray.newBuilder();
    var lineNumber = 0;
    var tokenOffset = 0;
    var graph = mDfa.graph();

    while (inputBytes[tokenOffset] != 0) {

      // Id of best lexeme found so far
      int bestId = Lexeme.ID_UNKNOWN;

      var tokLenCurr = 0;
      var tokLenBest = 1;

      // <graph> ::= <state>*
      //
      // <state> ::= <1 + token id, or 0> <edge count> <edge>*
      //
      // <edge>  ::= <char_range count> <char_range>* <dest_state_offset, low byte first>
      //
      // <char_range> ::= <start of range (1..127)> <size of range>
      //

      // The first state is always the start state
      int statePtr = 0;

      while (true) {
        byte cursorByte = inputBytes[tokenOffset + tokLenCurr];

        // If the byte is -128...-1, set it to 127.
        // The convention is that any range that includes 127 will also include these bytes.
        if (cursorByte < 0)
          cursorByte = 127;

        // Initially, we have no next state
        int nextState = -1;

        var tokenCode = graph[statePtr];
        int edgeCount = graph[statePtr + 1];
        statePtr += 2;

        if (tokenCode != 0) {
          int newTokenId = (tokenCode & 0xff) - 1;
          if (newTokenId >= bestId || tokLenCurr > tokLenBest) {
            tokLenBest = tokLenCurr;
            bestId = newTokenId;
          }
        }

        // Iterate over the edges
        for (var edgeIndex = 0; edgeIndex < edgeCount; edgeIndex++) {

          // <edge>  ::= <char_range count> <char_range>* <dest_state_offset, low byte first>
          //
          // <char_range> ::= <start of range (1..127)> <size of range>
          //
          boolean followEdge = false;

          // Iterate over the char_ranges
          //
          var rangeCount = graph[statePtr];
          statePtr += 1;

          for (var rangeIndex = 0; rangeIndex < rangeCount; rangeIndex++) {
            var sp = statePtr + rangeIndex * 2;
            int first = graph[sp];
            int rangeSize = graph[sp + 1];
            int posWithinRange = cursorByte - first;

            if (posWithinRange >= 0 && posWithinRange < rangeSize) {
              followEdge = true;
              break;
            }
          }
          statePtr += rangeCount * 2;

          if (followEdge) {
            nextState = (graph[statePtr] & 0xff) | ((graph[statePtr + 1] & 0xff) << 8);
          }
          statePtr += 2;
        }

        if (nextState < 0)
          break;

        statePtr = nextState;
        tokLenCurr++;
      }

      if (bestId != mSkipId) {
        filteredPtrs.add(ti.size());
      }

      ti.add(tokenOffset);
      ti.add(bestId);
      ti.add(lineNumber);

      // increment line number for each linefeed encountered
      for (var j = tokenOffset; j < tokenOffset + tokLenBest; j++)
        if (inputBytes[j] == LF)
          lineNumber++;

      tokenOffset += tokLenBest;
    }

    // Add a final entry so we can calculate the length of the last token
    ti.add(tokenOffset);
    ti.add(Lexeme.ID_END_OF_INPUT);
    ti.add(lineNumber);

    mTokenInfo = ti.array();
    mFilteredOffsets = filteredPtrs.array();
    mTokenCount = mFilteredOffsets.length;
  }

  private static boolean idMatch(int tokenId, int matchExpr) {
    return matchExpr == Lexeme.ID_UNKNOWN || matchExpr == tokenId;
  }

  public int tokenTextStart(int infoAddress) {
    return mTokenInfo[infoAddress + F_TOKEN_OFFSET];
  }

  String getText(int address) {
    var startOffset = tokenTextStart(address);
    var len = tokenLength(address);
    return new String(mBytes, startOffset, len, StandardCharsets.UTF_8);
  }

  DFA dfa() {
    return mDfa;
  }


  int tokenStartLineNumber(int address) {
    return mTokenInfo[address + F_LINE_NUMBER];
  }

  int tokenId(int address) {
    return mTokenInfo[address + F_TOKEN_ID];
  }

  int tokenLength(int address) {
    var r0 = address;
    var r1 = address + F_TOTAL;
    var result = tokenTextStart(r1) - tokenTextStart(r0);
    checkArgument(result > 0, "token length <= 0! Address:", address);
    return result;
  }

  String plotContext(LexemePlotContext context) {
    // Try to keep the start of the token within view.
    // Later we might get fancy and try to center the entire token
    // (if it doesn't contain linefeeds)...

    // Todo: make this a parameter?
    final int TEXT_COLUMNS = 90;

    var textLeft = Math.max(0, context.tokenColumn - (int) (TEXT_COLUMNS * .4f));

    var lineNumberFormatString = "%" + context.maxLineNumberDigits + "d";

    var maxTextColumns = TEXT_COLUMNS - context.maxLineNumberDigits;

    var sb = new StringBuilder();

    {
      var sourceInfo = mSourceDescription;
      if (nonEmpty(sourceInfo)) {
        sb.append(sourceInfo);
        addLF(sb);
      }
    }

    int maxIndex = Math.max(context.rows.size(), context.tokenRow + 2);
    for (var index = 0; index < maxIndex; index++) {
      String r = null;
      if (index < context.rows.size())
        r = context.rows.get(index);

      if (index == 1 + context.tokenRow) {
        int paddingSp = 2;
        sb.append(spaces(context.maxLineNumberDigits + paddingSp));
        var arrLength = context.tokenColumn - textLeft;
        for (int j = 0; j < arrLength; j++)
          sb.append('-');
        sb.append('^');
        sb.append('\n');
      }
      if (r == null) continue;
      sb.append(String.format(lineNumberFormatString, index + context.firstRowLineNumber));
      sb.append(": ");

      var textRight = Math.min(r.length(), maxTextColumns + textLeft);
      if (textRight > textLeft) {
        sb.append(r.substring(textLeft, textRight));
      }
      sb.append('\n');
    }
    return sb.toString();
  }

  private int determineMaxDigits() {
    int maxLineNumber;
    {
      var info = tokenInfo();
      var lastLinePtr = info.length - Lexer.TOKEN_INFO_REC_LEN;
      maxLineNumber = tokenStartLineNumber(lastLinePtr);
    }
    int reqDigits = (int) Math.floor(1 + Math.log10(1 + maxLineNumber)); // Add 1 since internal line numbers start at 0
    reqDigits = Math.max(reqDigits, 4);
    return reqDigits;
  }

  /**
   * Find last info pointer that has a line number <= n
   */
  private int findLastTokenAtLine(int n) {
    // A good place to use invariants.
    //
    // Invariant:
    //    window is a start index and a size, where size >= 1
    //    window always contains search result
    //    window gets smaller with each iteration
    //

    // We will refer to the window start and size as a record count (i.e. / F_TOTAL),
    // to simplify the calculations
    //
    int searchMin = 0;
    int windowSize = tokenAddressToIndex(mTokenInfo.length);
    while (windowSize > 1) {
      var mid = searchMin + windowSize / 2;
      var midPtr = tokenIndexToAddress(mid);
      var midLineNumber = tokenStartLineNumber(midPtr);
      if (midLineNumber > n) {
        windowSize = mid - searchMin;
      } else {
        windowSize -= (mid - searchMin);
        searchMin = mid;
      }
    }
    return tokenIndexToAddress(searchMin);
  }

  private int filteredIndexToAddress(int index) {
    return mFilteredOffsets[index];
  }

  private static int tokenAddressToIndex(int address) {
    return address / F_TOTAL;
  }

  private static int tokenIndexToAddress(int index) {
    return index * F_TOTAL;
  }

  int calculateColumnNumber(int lexemeInfoAddress) {
    var targetLineNumber = tokenStartLineNumber(lexemeInfoAddress);

    // Look for last token that appears on line n-1, then
    // march forward
    var seekLine = targetLineNumber - 1;
    int currentTokenAddress;
    if (seekLine < 0) {
      currentTokenAddress = 0;
    } else {
      currentTokenAddress = findLastTokenAtLine(seekLine);
    }

    int column = 0;
    var textBytes = mBytes;

    final int TAB_WIDTH = 4;

    while (currentTokenAddress != lexemeInfoAddress) {
      var charIndex = tokenTextStart(currentTokenAddress);
      var tokLength = tokenLength(currentTokenAddress);

      for (int j = 0; j < tokLength; j++) {
        var ch = textBytes[charIndex + j];
        if (ch == '\n') {
          column = 0;
        } else if (ch == '\t') {
          int tabMod = column % TAB_WIDTH;
          if (tabMod == 0) tabMod = TAB_WIDTH;
          column += tabMod;
        } else if (ch >= ' ') {
          column++;
        }
      }
      currentTokenAddress += Lexer.TOKEN_INFO_REC_LEN;
    }
    return column;
  }


  LexemePlotContext buildPlotContext(Lexeme lexeme, int width) {
    var ret = new LexemePlotContext();
    ret.maxLineNumberDigits = determineMaxDigits();
    ret.token = lexeme;
    ret.rows = arrayList();
    ret.tokenRow = -1;

    // Determine line number for the target lexeme
    var targetLineNumber = tokenStartLineNumber(lexeme.infoAddress());

    // Look for last token that appears on line n-c-1, then
    // march forward, plotting tokens intersecting lines n-c through n+c
    var seekLine = targetLineNumber - width - 1;
    int startAddress = 0;
    if (seekLine >= 0)
      startAddress = findLastTokenAtLine(seekLine);

    var textBytes = mBytes;
    int currentCursorPos = 0;
    var currentTokenAddress = startAddress;
    StringBuilder destSb = null;

    final int TAB_WIDTH = 4;

    final boolean SHOW_TABS = false;

    while (true) {

      // If no more tokens, stop
      if (tokenId(currentTokenAddress) == Lexeme.ID_END_OF_INPUT)
        break;

      if (currentTokenAddress == lexeme.infoAddress()) {
        ret.tokenColumn = currentCursorPos;
      }

      var currentLineNum = tokenStartLineNumber(currentTokenAddress);

      // If beyond context window, stop
      if (currentLineNum > targetLineNumber + width) {
        break;
      }

      // If there's no receiver for the text we're going to plot, determine if
      // we should create one
      if (destSb == null) {
        destSb = new StringBuilder();
        ret.firstRowLineNumber = currentLineNum + 1;
        if (currentLineNum == targetLineNumber)
          ret.tokenRow = ret.rows.size();
      }
      var charIndex = tokenTextStart(currentTokenAddress);
      var tokLength = tokenLength(currentTokenAddress);

      for (int j = 0; j < tokLength; j++) {
        var ch = textBytes[charIndex + j];
        if (ch == '\n') {
          if (destSb != null) {
            ret.rows.add(destSb.toString());
          }
          currentCursorPos = 0;
          currentLineNum++;
          destSb = null;
          if (currentLineNum >= targetLineNumber - width && currentLineNum <= targetLineNumber + width) {
            destSb = new StringBuilder();
            if (currentLineNum == targetLineNumber)
              ret.tokenRow = ret.rows.size();
          }
        } else if (ch == '\t') {
          int tabMod = currentCursorPos % TAB_WIDTH;
          if (tabMod == 0) tabMod = TAB_WIDTH;
          if (destSb != null) {
            for (int k = 0; k < tabMod; k++) {
              final char TAB_CHAR = SHOW_TABS ? '#' : ' ';
              destSb.append(TAB_CHAR);
            }
          }
          currentCursorPos += tabMod;
        } else if (ch >= ' ') {
          if (destSb != null) {
            destSb.append((char) ch);
          }
          currentCursorPos++;
        }
      }

      // We've plotted a single token

      currentTokenAddress += Lexer.TOKEN_INFO_REC_LEN;
    }
    if (destSb != null)
      ret.rows.add(destSb.toString());
    return ret;
  }

  private static class LexemePlotContext {
    Lexeme token;
    List<String> rows;
    int tokenRow;
    int tokenColumn;
    int maxLineNumberDigits;
    int firstRowLineNumber;

    @Override
    public String toString() {
      var m = map();
      m.put("", "LexemePlotContext");
      m.put("rows", JSList.with(rows));
      m.put("tokenRow", tokenRow);
      m.put("tokenColumn", tokenColumn);
      m.put("maxLineNumberDigits", maxLineNumberDigits);
      m.put("firstRowLineNumber", firstRowLineNumber);
      return m.prettyPrint();
    }
  }

  private DFA mDfa;
  private byte[] mBytes;
  private int mSkipId;
  private boolean mAcceptUnknownTokens;
  private int mReadIndex;
  private int[] mTokenInfo;
  private int[] mFilteredOffsets;
  private int mTokenCount;
  private boolean mStarted;
  private String mSourceDescription;
  // Action info
  private int mActionLength;
  private int mActionCursor;
  private int mActionCursorStart;

}
