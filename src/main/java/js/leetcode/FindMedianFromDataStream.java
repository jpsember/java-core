package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.json.JSList;
import js.json.JSMap;
import js.json.JSObject;

/**
 * The running time is in the bottom 20%.
 * 
 * Now using an approach where we add values to a bucket, unsorted, until a
 * median request happens. At that point, if there are multiple buckets, we walk
 * from the left and right sides of the bucket list, "chopping" off buckets
 * since a 'low' bucket will be balanced by a 'high' bucket of the same size.
 * When both cursors reach the same bucket, then we either split that bucket (if
 * its size is large) or we sort the bucket and extract the median (with special
 * care for the cursor positions with which we arrived at that bucket). A
 * special case is made for the cursors pointing to values that are adjacent but
 * are split between two buckets.
 *
 *
 * To calculate a median, we can discard equal-sized "chunks" of numbers that
 * lie on opposite sides of the median, and that these chunks do not need to be
 * sorted.
 * 
 * The underlying data structure is an array of buckets, where each bucket
 * contains an array of unsorted numbers. The array of buckets is sorted so that
 * B(i)'s min and max numbers are strictly less than that of B(i+1)'s.
 * 
 * With each AddNum(x) call, we determine which of the existing buckets should
 * receive x. We choose the bucket whose min and max range already contain x, or
 * if no such bucket exists, the bucket whose distance from this range is
 * minimized.
 * 
 * For FindMedian(), we set up a pair of cursors to walk from the first and last
 * buckets towards the center. Each cursor has a pointer to a bucket and a count
 * of the numbers processed by the procedure. At each step, we add the smaller
 * bucket's population to each count, and move the bucket pointers inward when
 * all the bucket's numbers are consumed. We do this until either the two
 * cursors point at the same bucket. At this point, if the bucket is small
 * enough, we sort that bucket's contents, and use the cursor counts to
 * determine the median. A special case exists when one cursor is pointing to
 * the max value in one bucket, and the next cursor is pointing to the minimum
 * value in the adjacent bucket. The median is then the average of one bucket's
 * min() and the other bucket's max().
 * 
 * If the above procedure reaches a bucket whose size is large enough, then
 * instead of sorting the bucket, we split the bucket by partitioning its
 * numbers based on the average of the bucket's min and max values (if min =
 * max, then all the numbers in the bucket are identical, and no splitting or
 * sorting is required). We replace the original bucket with the two smaller
 * buckets, and repeat the FindMedian() procedure.
 * 
 * The expensive sorting step in FindMedian() can be optimized by keeping track
 * of how many of a bucket's numbers are already sorted. When adding new numbers
 * to an already-sorted bucket, the numbers are appended to the end of the
 * array. When sorting is next required, we sort only the range of new numbers
 * (at the end of the array), then perform a linear, in-space merge of the two
 * subarrays.
 * 
 * 
 * $$O(n log n)$$ in the worst case, due to the bucket sorting. (The linear scan
 * of buckets in AddNum() might imply a running time of $$O(n^2)$$, but this
 * could only happen if the bucket count was a significant fraction of $$n$$,
 * which is not the case: buckets are only created when a bucket gets large,
 * which implies the bucket count is bounded by $$O(log n)$$).
 * 
 */
public class FindMedianFromDataStream extends LeetCode {

  public static void main(String[] args) {
    new FindMedianFromDataStream().run();
  }

  public void run() {

    if (false) {
      var m = new MedianFinder();
      m.addNum(20);
      m.addNum(25);
      for (int i = 0; i < 49000; i++) {
        m.addNum(23);
      }
      pr("median:", m.findMedian());
      return;
    }

    if (false) {
      var m = new MedianFinder();
      for (int i = 100000; i > 50000; i--) {
        m.addNum(i);
        if (i % 375 == 23) {
          m.findMedian();
        }
      }
      pr("median:", m.findMedian());
      pr(m);
      return;
    }

    if (false) {
      var m = new MedianFinder();
      for (int i = 0; i < 30; i++) {
        m.addNum(i + 20);
        pr("median:", m.findMedian());
      }
      for (int i = 0; i < 30; i++) {
        m.addNum(19 - i);
        pr("median:", m.findMedian());
      }
      pr(m);
      return;
    }

    x("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[18],[],[251],[],[158],[],[158],[],[529],[],[180],[],[134],[],[529],[],[240],[],[435],[],[435],[],[316],[],[350],[],[537],[],[490],[],[198],[],[359],[],[493],[],[585],[],[614],[],[21],[],[583],[],[106],[],[549],[],[271],[],[174],[],[430],[],[222],[],[117],[],[159],[],[206],[],[100],[],[496],[],[129],[],[550],[],[411],[],[216],[],[271],[],[98],[],[119],[],[232],[],[629],[],[101],[],[218],[],[53],[],[468],[],[447],[],[402],[],[603],[],[584],[],[306],[],[269],[],[623],[],[88],[],[79],[],[521],[],[261],[],[544],[],[628],[],[121],[],[278],[],[132],[],[105],[],[72],[],[459],[],[408],[],[111],[],[291],[],[437],[],[7],[],[276],[],[34],[],[16],[],[254],[],[177],[],[550],[],[632],[],[464],[],[191],[],[167],[],[158],[],[331],[],[187],[],[123],[],[274],[],[321],[],[182],[],[93],[],[277],[],[595],[],[459],[],[349],[],[80],[],[195],[],[195],[],[214],[],[14],[],[384],[],[68],[],[427],[],[84],[],[609],[],[283],[],[59],[],[234],[],[327],[],[330],[],[509],[],[568],[],[65],[],[442],[],[380],[],[321],[],[589],[],[603],[],[64],[],[486],[],[175],[],[204],[],[421],[],[381],[],[209],[],[578],[],[580],[],[294],[],[287],[],[518],[],[261],[],[619],[],[155],[],[636],[],[287],[],[250],[],[209],[],[586],[],[299],[],[589],[],[65],[],[34],[],[216],[],[507],[],[380],[],[127],[],[186],[],[135],[],[533],[],[272],[],[563],[],[249],[],[350],[],[516],[],[229],[],[307],[],[127],[],[306],[],[195],[],[537],[],[269],[],[485],[],[446],[],[206],[],[222],[],[515],[],[240],[],[266],[],[417],[],[472],[],[140],[],[185],[],[439],[],[592],[],[283],[],[273],[],[188],[],[230],[],[195],[],[217],[],[321],[],[246],[],[563],[],[113],[],[411],[],[290],[],[351],[],[221],[],[76],[],[399],[],[19],[],[142],[],[544],[],[47],[],[270],[],[614],[],[36],[],[538],[],[179],[],[370],[],[387],[],[513],[],[570],[],[546],[],[347],[],[181],[],[257],[],[182],[],[412],[],[588],[],[66],[],[276],[],[381],[],[94],[],[461],[],[570],[],[550],[],[432],[],[192],[],[153],[],[382],[],[568],[],[233],[],[375],[],[186],[],[469],[],[402],[],[266],[],[143],[],[146],[],[44],[],[512],[],[540],[],[297],[],[520],[],[545],[],[436],[],[412],[],[597],[],[535],[],[13],[],[12],[],[109],[],[128],[],[84],[],[619],[],[85],[],[203],[],[478],[],[367],[],[77],[],[485],[],[640],[],[415],[],[533],[],[628],[],[338],[],[542],[],[560],[],[11],[],[80],[],[277],[],[148],[],[113],[],[23],[],[246],[],[453],[],[221],[],[310],[],[486],[],[262],[],[87],[],[330],[],[390],[],[199],[],[451],[],[540],[],[283],[],[466],[],[37],[],[639],[],[515],[],[319],[],[520],[],[289],[],[620],[],[470],[],[216],[],[249],[],[229],[],[130],[],[134],[],[637],[],[350],[],[605],[],[481],[],[220],[],[373],[],[60],[],[100],[],[205],[],[270],[],[305],[],[166],[],[525],[],[373],[],[627],[],[607],[],[111],[],[91],[],[491],[],[143],[],[353],[],[549],[],[434],[],[182],[],[386],[],[33],[],[540],[],[364],[],[456],[],[635],[],[317],[],[637],[],[0],[],[310],[],[559],[],[591],[],[8],[],[243],[],[331],[],[222],[],[117],[],[396],[],[163],[],[568],[],[309],[],[176],[],[61],[],[560],[],[102],[],[614],[],[586],[],[569],[],[261],[],[436],[],[45],[],[606],[],[549],[],[170],[],[123],[],[249],[],[296],[],[291],[],[486],[],[158],[],[300],[],[603],[],[371],[],[170],[],[498],[],[378],[],[480],[],[62],[],[586],[],[317],[],[502],[],[563],[],[399],[],[266],[],[93],[],[231],[],[22],[],[531],[],[245],[],[599],[],[189],[],[297],[],[637],[],[233],[],[237],[],[345],[],[387],[],[379],[],[419],[],[212],[],[323],[],[96],[],[64],[],[59],[],[144],[],[369],[],[252],[],[442],[],[597],[],[494],[],[438],[],[100],[],[157],[],[184],[],[478],[],[497],[],[107],[],[39],[],[263],[],[239],[],[143],[],[134],[],[224],[],[498],[],[79],[],[254],[],[240],[],[28],[],[533],[],[484],[],[389],[],[89],[],[621],[],[151],[],[313],[],[28],[],[274],[],[105],[],[601],[],[265],[],[548],[],[401],[],[254],[],[539],[],[113],[],[118],[],[165],[],[77],[],[478],[],[534],[],[65],[],[165],[],[580],[],[380],[],[83],[],[455],[],[506],[],[336],[],[445],[],[398],[],[384],[],[389],[],[209],[],[207],[],[537],[],[383],[],[176],[],[92],[],[478],[],[455],[],[503],[],[526],[],[623],[],[260],[],[522],[],[326],[],[335],[],[336],[],[221],[],[119],[],[426],[],[561],[],[301],[],[168],[],[377],[],[75],[],[615],[],[390],[],[88],[],[29],[],[343],[],[572],[],[608],[],[358],[],[91],[],[534],[],[336],[],[395],[],[580],[],[478],[],[448],[],[305],[],[640],[],[339],[],[139],[],[526],[],[592],[],[542],[],[215],[],[7],[],[168],[],[39],[],[622],[],[386],[],[81],[],[314],[],[599],[],[77],[],[400],[],[359],[],[622],[],[147],[],[203],[],[487],[],[612],[],[111],[],[413],[],[487],[],[95],[],[469],[],[475],[],[6],[],[9],[],[437],[],[502],[],[488],[],[77],[],[424],[],[625],[],[223],[],[102],[],[371],[],[408],[],[389],[],[312],[],[134],[],[77],[],[629],[],[318],[],[12],[],[533],[],[309],[],[575],[],[616],[],[62],[],[303],[],[85],[],[36],[],[20],[],[447],[],[388],[],[463],[],[543],[],[439],[],[204],[],[479],[],[9],[],[598],[],[437],[],[9],[],[480],[],[580],[],[450],[],[318],[],[521],[],[466],[],[214],[],[236],[],[44],[],[345],[],[276],[],[227],[],[378],[],[83],[],[562],[],[45],[],[514],[],[260],[],[370],[],[562],[],[352],[],[164],[],[450],[],[418],[],[114],[],[41],[],[169],[],[577],[],[412],[],[239],[],[75],[],[633],[],[439],[],[371],[],[566],[],[507],[],[214],[],[435],[],[39],[],[86],[],[215],[],[239],[],[621],[],[39],[],[377],[],[297],[],[156],[],[607],[],[342],[],[18],[],[374],[],[359],[],[560],[],[98],[],[343],[],[287],[],[418],[],[371],[],[308],[],[349],[],[607],[],[470],[],[568],[],[14],[],[302],[],[605],[],[635],[],[543],[],[471],[],[241],[],[519],[],[169],[],[381],[],[344],[],[159],[],[164],[],[624],[],[93],[],[123],[],[474],[],[184],[],[296],[],[619],[],[179],[],[194],[],[468],[],[245],[],[0],[]]");

    x("[\"MedianFinder\", \"addNum\", \"addNum\", \"findMedian\", \"addNum\", \"findMedian\"]\n",
        "[[], [1], [2], [], [3], []]");

    x("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[12],[],[10],[],[13],[],[11],[],[5],[],[15],[],[1],[],[11],[],[6],[],[17],[],[14],[],[8],[],[17],[],[6],[],[4],[],[16],[],[8],[],[10],[],[2],[],[12],[],[0],[]]");
  }

  static int k;

  private void x(String cmdStr, String argStr) {
    var cmdList = new JSList(cmdStr);
    var argsList = new JSList(argStr);

    var resultList = list();

    var expList = list();

    MedianFinder m = null;
    MedianFinder s = null;
    int i = INIT_INDEX;
    for (var cmd : cmdList.asStringArray()) {
      i++;
      Object output = null;
      Object output2 = null;
      switch (cmd) {
      default:
        throw die(cmd);
      case "MedianFinder":
        m = new MedianFinder();
        s = new SlowMedianFinder();
        break;
      case "addNum": {
        var num = argsList.getList(i).getInt(0);
        m.addNum(num);
        s.addNum(num);
      }
        break;
      case "findMedian":
        k++;
        output = m.findMedian();
        output2 = s.findMedian();
        if (!output.equals(output2))
          die("k:", k, "failed to find median; got:", output, "but expected:", output2);
        break;
      }
      resultList.addUnsafe(output);
      expList.addUnsafe(output2);
    }
    pr(resultList);
    verify(resultList, expList);
    //db("m:", INDENT, m);
  }

  class SlowMedianFinder extends MedianFinder {

    public SlowMedianFinder() {

    }

    public void addNum(int num) {
      vals.add(num);
    }

    public double findMedian() {
      vals.sort(null);
      int i = vals.size() / 2;
      int j = i;
      if (vals.size() % 2 == 0)
        j--;
      return (vals.get(i) + vals.get(j)) / 2.0;
    }

    private List<Integer> vals = new ArrayList<>();
  }

  private static //

  // ----------------------------------------------

  class MedianFinder {

    public void addNum(int num) {
      Bucket b = null;
      if (buckets.isEmpty()) {
        b = new Bucket();
        buckets.add(b);
      } else {
        int minDist = Integer.MAX_VALUE;
        for (var b2 : buckets) {
          var d0 = b2.minVal() - num;
          var d1 = num - b2.maxVal();
          if (d0 > 0) {
            if (d0 < minDist) {
              minDist = d0;
              b = b2;
            }
          } else if (d1 > 0) {
            if (d1 < minDist) {
              minDist = d1;
              b = b2;
            }
          } else {
            b = b2;
            break;
          }
        }
      }
      b.add(num);
    }

    private Bucket buck(int index) {
      return buckets.get(index);
    }

    public double findMedian() {
      var cleft = 0;
      int pleft = 0;
      var cright = buckets.size() - 1;
      int pright = buck(cright).size() - 1;
      Double median = null;
      while (median == null) {
        if (cleft == cright) {
          if (split(cleft)) {
            break;
          }
          median = buck(cleft).median(pleft, pright);
        } else {
          var bleft = buck(cleft);
          var bright = buck(cright);
          var dist = Math.min(bleft.size() - pleft, pright + 1);
          pleft += dist;
          if (pleft == bleft.size()) {
            cleft++;
            pleft = 0;
          }
          pright -= dist;
          if (pright < 0) {
            cright--;
            pright = buck(cright).size() - 1;
          }

          // If left has moved past the right, we can't subtract anything, the median straddles these two
          if (cleft > cright) {
            median = (bleft.maxVal() + bright.minVal()) / 2.0;
            break;
          }
        }
      }
      if (median == null)
        median = findMedian();
      return median;
    }

    private static final int DEFAULT_BUCKET_SIZE = 800;

    /**
     * Determine if a bucket should be split. If its size is small, or its
     * values are all the same, then return false. Otherwise, split it into two
     * and return true.
     */
    private boolean split(int bucketIndex) {
      var b = buck(bucketIndex);
      if (b.size() <= DEFAULT_BUCKET_SIZE || b.minVal() == b.maxVal())
        return false;
      var b1 = new Bucket();
      var b2 = new Bucket();
      b1.ensureCapacity(b.size());
      b2.ensureCapacity(b.size());

      int splitVal = (b.minVal() + b.maxVal()) / 2;

      var a1 = b1.array;
      int s1 = 0;
      var a2 = b2.array;
      int s2 = 0;
      var a0 = b.array;
      var astop = b.used;
      int a1Min = Integer.MAX_VALUE;
      int a1Max = Integer.MIN_VALUE;
      int a2Min = Integer.MAX_VALUE;
      int a2Max = Integer.MIN_VALUE;

      for (int i = 0; i < astop; i++) {
        var val = a0[i];
        if (val <= splitVal) {
          a1[s1++] = val;
          if (val < a1Min)
            a1Min = val;
          if (val > a1Max)
            a1Max = val;
        } else {
          a2[s2++] = val;
          if (val < a2Min)
            a2Min = val;
          if (val > a2Max)
            a2Max = val;
        }
      }
      b1.min = a1Min;
      b1.max = a1Max;
      b1.used = s1;

      b2.min = a2Min;
      b2.max = a2Max;
      b2.used = s2;
      buckets.set(bucketIndex, b1);
      buckets.add(bucketIndex + 1, b2);
      return true;
    }

    public JSMap toJson() {
      var m = map();
      m.put("", "MedianFinder");
      int i = INIT_INDEX;
      for (var b : buckets) {
        i++;
        m.put("bucket #" + i, b.toJson());
      }
      return m;
    }

    @Override
    public String toString() {
      return toJson().prettyPrint();
    }

    private static class Bucket {

      public Bucket() {
        this.array = new int[DEFAULT_BUCKET_SIZE];
      }

      public int size() {
        return used;
      }

      public void add(int num) {
        ensureCapacity(used + 1);
        array[used++] = num;
        if (used == 1 || num < min)
          min = num;
        if (used == 1 || num > max)
          max = num;
      }

      public void ensureCapacity(int cap) {
        if (array.length < cap) {
          var newArray = new int[cap * 2];
          System.arraycopy(array, 0, newArray, 0, used);
          array = newArray;
        }
      }

      public int minVal() {
        return min;
      }

      public int maxVal() {
        return max;
      }

      @Override
      public String toString() {
        return toJson().prettyPrint();
      }

      public JSObject toJson() {
        var m = map();
        m.put("used", used);
        if (used != 0) {
          m.put(",min", minVal());
          m.put(".max", maxVal());
          if (used < 40)
            m.put("nums", JSList.with(Arrays.copyOfRange(array, 0, used)));
        }
        return m;
      }

      public void sort() {
        if (sortedTo < used) {
          var a = array;
          // Sort the new items in the list
          if (used - sortedTo > 1)
            Arrays.sort(a, sortedTo, used);
          if (sortedTo != 0) {
            // Merge newly sorted items into previously sorted
            int ca = sortedTo - 1;
            int cb = used - 1;
            while (ca >= 0 && ca != cb) {
              var va = a[ca];
              var vb = a[cb];
              if (va > vb) {
                a[cb] = va;
                a[ca] = vb;
                ca--;
                cb--;
              } else {
                cb--;
              }
            }
          }
          sortedTo = used;
        }
      }

      public double median(int li, int ri) {
        sort();
        int middle = (li + ri) / 2;
        int add = middle - li;
        li += add;
        ri -= add;
        return (array[li] + array[ri]) / 2.0;
      }

      private int used;
      private int[] array;
      private int min, max;
      private int sortedTo;
    }

    private List<Bucket> buckets = new ArrayList<>();
  }

}