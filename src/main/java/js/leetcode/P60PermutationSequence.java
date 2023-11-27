
package js.leetcode;

import static js.base.Tools.*;

import js.json.JSList;

/**
 * My initial intuition was wrong... this will need more work.
 * 
 * The 'permutation sequence' depends upon the numerical value of the symbols
 * being permuted, which is something that probably prevents some simple
 * symmetry arguments.
 * 
 * I've been misled by an assumption that there is a relationship between n and
 * log k.
 */
public class P60PermutationSequence {

  public static void main(String[] args) {
    new P60PermutationSequence().run();
  }

  private void run() {

//    var n = 4;
//    for (int k = 0; k < fact(n); k++)
//      pr(getPermutation(n, k),"n:",n,"k:",k);

    x(4, 1, "1234");

    //    1 2 3
    //    1 3 2
    //    2 1 3
    //    2 3 1
    //    3 2 1
    //    3 1 2

   x(3, 5, "312");
    //    x(9, 1, "123456789");
    //    x(4, 1, "1234");
    //
         x(3, 3, "213");
       x(4, 9, "2314");
       x(3, 1, "123");

  }

  /* private */void x(int n, int k, String expected) {
    String result = getPermutation(n, k);
    pr(VERT_SP, "n:", n, "k:", k, result);
    checkState(result.equals(expected), "expected:", expected);
  }

  public String getPermutation(int n, int k) {
    init(n, k);

    // Attempt #1
    if (false) {
      for (int i = n - 1; i >= 0; i--) {
        if ((k & (1 << i)) != 0) {
          pr("swap", i, i + 1);
          swap(i, i + 1);
        }
      }
    }
    // Attempt #2
    if (false) {
      for (int i = n - 1; i >= 0; i--) {
        if ((k & (1 << i)) != 0) {
          pr("swap", i, i + 1);
          swap(i, i + 1);
          reverse(i);
        }
      }
    }
    // Attempt #3
    if (false) {
      for (int i = n - 1; i >= 0; i--) {
        if ((k & (1 << i)) != 0) {
          moveToFront(i);
        }
      }
    }
    // Attempt #4
    if (false) {

      int effN = n;
      int effK = k;

      if (kHasBit(n - 1)) {
        moveToHighest(0);
        effK ^= -1;
        effN = n - 1;
      }
      for (int i = effN - 1; i >= 0; i--) {
        if ((effK & (1 << i)) != 0) {
          pr("swap", i, i + 1);
          swap(i, i + 1);
        }
      }
    }

    // Attempt #5
    if (true) {

      k--;
     // k = 7;
      
      pr(VERT_SP,"k:", k, "n:", n, str());

      var kEff = k;
      for (int jz = 0; jz<n-1; jz++) {
        int base = jz;
        int effN = n - jz;
        
        var divisor = fact(effN-1);
        var numer = kEff;
        
        int slot =  base + numer / divisor;
        pr(VERT_SP,"j:",jz,"divisor:",divisor,"numer:",numer,"slot:",slot);
        
        var targSlot = base; //jz;
        var c = seq[slot];
         pr("k':", kEff, "j:", jz, "slot:", slot, "target:", targSlot);
         pr("before moving:",str());
        while (slot > targSlot) {
          seq[slot ] = seq[slot-1];
          slot--;
        }
        seq[slot] = c;
        pr("after moving:",str());
        kEff = kEff % divisor;
        pr("reduced k':",kEff);
      }
      //halt();
    }
    return str();
  }
  //
  //  private void attempt5(int effectiveK, int effectiveN) {
  //    int k0 = effectiveK;
  //    int n0 = effectiveN;
  //    for (int j = n0; j > 0; j--) {
  //      int slot = k0 / fact(j-1);
  //      pr("k0:",k0,"j:",j,"slot:",slot);
  //      var c = seq[n-1-slot];
  //      while (slot != n0) {
  //        seq[n-1-slot-1] = seq[n-1-slot];
  //        slot++;
  //      }
  //      seq[n-1-slot] = c;
  //      
  //      
  //    }
  //  }

  private static int[] sFact;

  private boolean kHasBit(int bitNum) {
    return (k & (1 << bitNum)) != 0;
  }

  public int fact(int n) {
    if (sFact == null) {
      final int nMax = 9;
      sFact = new int[nMax + 1];
      int f = 1;
      for (int i = 0; i <= nMax; i++) {
        if (i != 0)
          f *= i;
        sFact[i] = f;
      }
    }
    return sFact[n];
  }

  public void reverse(int pos) {
    pr("reverse", pos, "orig:", str());
    pos = mod(n - 1 - pos, n);
    var posEnd = n - 1;
    while (pos < posEnd) {
      swap0(pos, posEnd);
      pos++;
      posEnd--;
    }
    pr("after rev:", str());
  }

  public void swap(int a, int b) {
    a = mod(n - 1 - a, n);
    b = mod(n - 1 - b, n);
    swap0(a, b);
  }

  private void swap0(int a, int b) {
    var tmp = seq[a];
    seq[a] = seq[b];
    seq[b] = tmp;
  }

  private void moveToHighest(int pos) {
    pr("moveToHighest", pos, str());
    int aStop = n - 1;
    var v = seq[pos];
    while (pos != aStop) {
      seq[pos] = seq[pos + 1];
      pos++;
    }
    seq[aStop] = v;
    pr("after moveToHighest:", str());
  }

  private void moveToFront(int pos) {
    pr("moveToFront", pos, str());
    int a = n - 1;
    int aStop = mod(n - 1 - pos, n);
    var v = seq[a];
    while (a != aStop) {
      seq[a] = seq[a + 1];
      a++;
    }
    seq[aStop] = v;
    pr("after move to front:", str());
  }

  private int mod(int value, int divisor) {
    int k = value % divisor;
    if (value < 0 && k != 0) {
      k += divisor;
    }
    return k;
  }

  public String str() {
    StringBuilder sb = new StringBuilder();
    for (var b : seq)
      sb.append((char) b);
    return sb.toString();
  }

  private void init(int n, int k) {
    this.n = n;
    this.k = k;
    this.seq = "123456789".substring(0, n).getBytes();
  }

  private byte[] seq;
  private int n;
  private int k;
}
