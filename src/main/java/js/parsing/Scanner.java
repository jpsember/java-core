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

package js.parsing;

import static js.base.Tools.*;

import java.nio.charset.StandardCharsets;
import java.util.List;

import js.base.BaseObject;

public class Scanner extends BaseObject {

  private static final int SKIP_ID_NONE = -2;
  private static final boolean DEBUG = false && alert("DEBUG in effect");

  private static void p(Object... messages) {
    if (DEBUG)
      pr(insertStringToFront("Scanner>>>", messages));
  }

  public Scanner(DFA dfa, String string, int skipId) {
    mDfa = dfa;
    mSkipId = (skipId < 0 ? SKIP_ID_NONE : skipId);
    mBytes = string.getBytes(StandardCharsets.UTF_8);
  }

  public Scanner(DFA dfa, String string) {
    this(dfa, string, 0);
  }

  public void setAcceptUnknownTokens() {
    mAcceptUnknownTokens = true;
  }

  public void setSourceDescription(String description) {
    mSourceDescription = description;
  }

  protected String supplyName() {
    return mSourceDescription;
  }

  private String mSourceDescription;

  /**
   * Determine a token ahead, without reading it
   *
   * @return token, or null if end of input
   */
  public Token peek(int distance) {
    // Repeat until we've filled the history buffer with enough (non-skipped) tokens,
    // or we've reached the end of the input
    while (mHistoryCursor + distance >= mHistory.size()) {
      Token token = peekAux();
      if (token == null)
        break;

      // Advance the column, row numbers
      {
        for (int i = mLastTokenOffset; i < mLastTokenOffset + mLastTokenByteCount; i++) {
          // For windows, unix, and (modern) osx, checking for a LF is sufficient
          mColumn++;
          if (mBytes[i] == 0x0a) {
            mLineNumber++;
            mColumn = 0;
          }
        }
      }
      if (!token.id(mSkipId))
        mHistory.add(token);
    }

    Token ret = null;
    if (mHistoryCursor + distance < mHistory.size()) {
      ret = mHistory.get(mHistoryCursor + distance);
    }
    return ret;
  }

  /**
   * Determine next token, without reading it
   *
   * @return token, or null if end of input
   */
  public Token peek() {
    return peek(0);
  }

  private Token peekAux() {
    p("peekAux, nextTokenStart", mNextTokenStart, "peekByte:", peekByte(0));
    if (peekByte(0) == 0)
      return null;
    int bestLength = 1;
    int bestId = Token.ID_UNKNOWN;
    int byteOffset = 0;

    var graph = mDfa.graph();

    // <graph> ::= <state>*
    //
    // <state> ::= <1 + token id, or 0> <edge count> <edge>*
    //
    // <edge>  ::= <char_range count> <char_range>* <dest_state_offset, low byte first>
    //
    // <char_range> ::= <start of range (1..127)> <size of range>
    //
    //


    // The first state is always the start state
    int statePtr = 0;

    while (true) {
      p(VERT_SP, "byte offset:", byteOffset);
      int ch = peekByte(byteOffset);

      // If the byte is -128...-1, set it to 127.
      // The convention is that any range that includes 127 will also include these bytes.
      if (ch < 0)
        ch = 127;

      p("nextByte:", ch, "state_ptr:", statePtr, "max:", graph.length);
      int nextState = -1;

      int newTokenId = -1;
      var tokenCode = graph[statePtr++];
      if (tokenCode != 0) {
        newTokenId = (tokenCode  & 0xff) - 1;
        p("..........token:", newTokenId, "offset:", byteOffset, "best:", bestLength);
        if (newTokenId >= bestId || byteOffset > bestLength) {
          bestLength = byteOffset;
          bestId = newTokenId;
          p("...........setting bestId:", mDfa.tokenName(bestId));
        }
      }

      int edgeCount = graph[statePtr++];
      p("...edge count:", edgeCount);

      // Iterate over the edges
      for (var en = 0; en < edgeCount; en++) {


        //
        // <edge>  ::= <char_range count> <char_range>* <dest_state_offset, low byte first>
        //
        // <char_range> ::= <start of range (1..127)> <size of range>
        //

        p("...edge #:", en);
        boolean followEdge = false;

        // Iterate over the char_ranges
        //
        var rangeCount = graph[statePtr++];
        p("......ranges:", rangeCount);
        for (var rn = 0; rn < rangeCount; rn++) {
          int first = graph[statePtr++];
          int rangeSize = graph[statePtr++];
          int posWithinRange = ch - first;

            p("......range #", rn, " [", first, "...", first+rangeSize, "]");
            if (posWithinRange >= 0 && posWithinRange < rangeSize) {
              followEdge = true;
              p("......contains char, following edge");
            }
        }
        var edgeDest = (graph[statePtr++] & 0xff) | ((graph[statePtr++] & 0xff) << 8);
        if (followEdge) {
          p("...following edge to:", edgeDest);
          nextState = edgeDest;
        }
      }
      statePtr = nextState;
      p("...advanced to next state:", statePtr);
      if (statePtr < 0) {
        break;
      }
      byteOffset++;
    }

    String tokenText =
        new String(mBytes, mNextTokenStart, bestLength);
    mLastTokenOffset = mNextTokenStart;

    mLastTokenByteCount = bestLength;
    mNextTokenStart += bestLength;

    Token peekToken = new Token(mSourceDescription, bestId, mDfa.tokenName(bestId), tokenText,
        1 + mLineNumber,
        1 + mColumn);
    p("peek token:", INDENT, peekToken);
    if (peekToken.isUnknown() && !mAcceptUnknownTokens) {
      throw new ScanException(peekToken, "unknown token");
    }
    return peekToken;
  }

  public Token read() {
    return read(-1);
  }

  public Token read(int tokenId) {
    Token token = peek();
    if (verbose()) {
      log("read", token, tokenId >= 0 ? "(expected: " + tokenId + ")" : "");
    }

    if (token == null)
      throw new ScanException(null, "no more tokens");
    if (!mAcceptUnknownTokens && token.isUnknown())
      throw new ScanException(token, "unknown token");
    if (tokenId >= 0) {
      if (token.id() != tokenId)
        throw new ScanException(token, "unexpected token");
    }
    mHistoryCursor++;
    return token;
  }

  public Token readIf(int tokenId) {
    Token token = peek();
    boolean readIt = (token != null && tokenId == token.id());
    if (readIt) {
      read();
      return token;
    }
    return null;
  }

  public boolean hasNext() {
    return peek() != null;
  }

  public void unread() {
    unread(1);
  }

  public void unread(int count) {
    if (mHistoryCursor < count)
      throw new ScanException(null, "Token unavailable");
    mHistoryCursor -= count;
  }

  private byte peekByte(int index) {
    var absIndex = index + mNextTokenStart;
    if (absIndex < mBytes.length) {
      return mBytes[absIndex];
    }
    return 0;
  }

  private DFA mDfa;
  private byte[] mBytes;
  private int mNextTokenStart;
  private int mLastTokenOffset;
  private int mLastTokenByteCount;
  private int mSkipId;
  private int mLineNumber;
  private int mColumn;
  private boolean mAcceptUnknownTokens;
  private List<Token> mHistory = arrayList();
  private int mHistoryCursor;
}
