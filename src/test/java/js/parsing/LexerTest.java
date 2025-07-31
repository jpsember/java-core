package js.parsing;

import js.json.JSUtils;
import org.junit.*;

import static org.junit.Assert.assertEquals;
import static js.base.Tools.*;

public class LexerTest {

  @Test
  public void testJsonMap() {
    loadTools();
    var input = "{ \"alpha\" 12.32 }";
    var s = new Lexer(JSUtils.JSON_DFA);
    s.withText(input);

    StringBuilder sb = new StringBuilder();
    while (s.hasNext()) {
      var t = s.read();
      sb.append(t.id());
      sb.append(" '");
      sb.append(t.text());
      sb.append("' ");
    }
    var result = sb.toString().trim();
//    pr("constructed result:",INDENT,result);
    assertEquals(result,"6 '{' 10 '\"alpha\"' 11 '12.32' 7 '}'");
  }

}
