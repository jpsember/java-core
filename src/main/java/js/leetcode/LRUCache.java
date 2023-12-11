package js.leetcode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static js.base.Tools.*;

public class LRUCache {

  public static void main(String[] args) {

    ["LRUCache","put","put","get","put","put","get"]
        [[2],[2,1],[2,2],[2],[1,1],[4,1],[2]]
            
    String[] cmds = { "LRUCache", "put", "put", "get", "put", "get", "put", "get", "get", "get" };
    int[] arg = { 2, 1, 1, 2, 2, 1, 3, 3, 2, 4, 4, 1, 3, 4, };
    List<Object> out = new ArrayList<>();

    LRUCache c = null;
    int ac = 0;
    for (var s : cmds) {
      Object value = null;
      switch (s) {
      case "LRUCache":
        c = new LRUCache(arg[ac++]);
        break;
      case "put": {
        var key = arg[ac++];
        var val = arg[ac++];
        c.put(key, val);
      }
        break;
      case "get": {
        var key = arg[ac++];
        var val = c.get(key);
        value = val;
      }
        break;
      }
      out.add(value);
    }
    pr(out);
  }

  public LRUCache(int capacity) {
    mCapacity = capacity;
  }

  public int get(int key) {
    int result = -1;
    var node = mMap.get(key);
    if (node != null) {
      moveToHead(node);
      result = node.value;
    }
    pr("get:", key, "got:", result, INDENT, this);
    return result;
  }

  public void put(int key, int value) {
    var node = mMap.get(key);
    if (node == null) {
      node = new ListItem(key, value);
      mMap.put(key, node);
    }
    moveToHead(node);
    if (mMap.size() > mCapacity) {
      var preTail = mTail.prev;
      unlink(mTail);
      mMap.remove(mTail.key);
      mTail = preTail;
      //      checkState(mMap.size() == mCapacity);
    }
    //    pr("put:", key, value, INDENT, this);
  }

  //  @Override
  //  public String toString() {
  //    StringBuilder sb = new StringBuilder();
  //    sb.append("size:");
  //    sb.append(mMap.size());
  //    sb.append(' ');
  //    if (mHead != null) {
  //      sb.append(" head:");
  //      sb.append(mHead.key);
  //    }
  //    if (mTail != null) {
  //      sb.append(" tail:");
  //      sb.append(mTail.key);
  //    }
  //    sb.append(" nodes:");
  //    var node = mHead;
  //    boolean first = true;
  //    while (node != null) {
  //      if (!first) {
  //        sb.append(" => ");
  //      }
  //      first = false;
  //      sb.append(node.key);
  //      sb.append(":");
  //      sb.append(node.value);
  //      var pn = node;
  //      node = node.next;
  //      if (node != null)
  //        checkState(node.prev == pn);
  //    }
  //    return sb.toString();
  //  }

  private void moveToHead(ListItem node) {
    // Move this to the head of the list
    if (mHead == node)
      return;
    if (mHead == null) {
      mHead = node;
      mTail = node;
      return;
    }

    if (node == mTail)
      mTail = node.prev;

    unlink(node);
    join(node, mHead);
    mHead = node;
  }

  private static class ListItem {
    ListItem(int key, int value) {
      this.key = key;
      this.value = value;
    }

    ListItem prev;
    ListItem next;
    int value;
    int key;
  }

  private void join(ListItem a, ListItem b) {
    if (a != null) {
      a.next = b;
    }
    if (b != null) {
      b.prev = a;
    }
  }

  private void unlink(ListItem a) {
    join(a.prev, a.next);
    a.prev = null;
    a.next = null;
  }

  private Map<Integer, ListItem> mMap = new HashMap<>();
  private int mCapacity;
  private ListItem mHead, mTail;

}
