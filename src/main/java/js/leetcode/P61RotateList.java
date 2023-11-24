
package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

public class P61RotateList {

  public static class ListNode {
    int val;
    ListNode next;

    ListNode() {
    }

    ListNode(int val) {
      this.val = val;
    }

    ListNode(int val, ListNode next) {
      this.val = val;
      this.next = next;
    }
  }

  public static void main(String[] args) {
    new P61RotateList().run();
  }

  private void run() {
    x(2, 10, 20, 30, 40, 50);
  }

  private void x(int k, int... nums) {
    ListNode result = null;
    if (nums.length > 0) {
      ListNode tail = null;
      for (var i : nums) {
        if (result == null) {
          result = new ListNode(i);
          tail = result;
        } else {
          ListNode n = new ListNode(i);
          tail.next = n;
          tail = n;
        }
      }
    }
    show("orig:",result);
    show("rot :",rotateRight(result, k));
  }

  private void show(String prompt, ListNode result) {
    JSList x = list();
    while (result != null) {
      x.add(result.val);
      result = result.next;
    }
    pr(prompt,x);
  }

  public ListNode rotateRight(ListNode head, int k) {
    if (head == null)
      return null;

    ListNode tail = null;
    
    // Determine length of list, and tail node
    int length = 0;
    {
      var node = head;
      while (node != null) {
        tail = node;
        node = node.next;
        length++;
      }
    }

    // Determine the smallest positive equivalent value for k

    k = k % length;
    if (k == 0)
      return head;

    int steps = length - 1 - k;
    ListNode newTail = head;
    while (steps-- != 0)
      newTail = newTail.next;

    var newHead = newTail.next;

    newTail.next = null;
    tail.next = head;

    //    pr("returning:", newHead.val);
    //    show(newHead);
    return newHead;
  }

}