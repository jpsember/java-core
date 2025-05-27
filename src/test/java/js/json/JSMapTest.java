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
}
