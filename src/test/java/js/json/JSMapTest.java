package js.json;

import js.parsing.DFA;
import org.junit.*;

import static js.base.Tools.*;

public class JSMapTest {

  @Test
  public void testEmptyMap() {
    var text = "{}";
    var mp = new JSMap(text);
    assert (mp.isEmpty());
  }

  @Test
  public void testSimpleMap() {
    pr("DFA:", DASHES);
    pr(DFA.describe(JSUtils.JSON_DFA));
    pr(DASHES);
    var text = "{\"a\":17}";
    var mp = new JSMap(text);

    pr(mp);
  }
}
