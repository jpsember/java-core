package js.json;

import org.junit.*;

import static js.base.Tools.*;

public class JSMapTest {

  @Test
  public void testEmptyMap() {
    var text = "{}";
    var mp = new JSMap(text);
    checkArgument(mp.isEmpty());
  }

  @Test
  public void testSimpleMap() {
    loadTools();
    var text = "{\"a\":17}";
    var mp = new JSMap(text);
    checkArgument(mp.size() == 1);
  }


  @Test
  public void parseJsonStrings() {
    String[] samples = {
        "\\u001b",
        "------\\u001b",
        "------\\u001b[39m",
    };

    var result = map();
    for (var s : samples) {
      s = quote(s);
      var x = JSUtils.parseStringFrom(s);
      result.putNumbered(x);
    }
  }

}
