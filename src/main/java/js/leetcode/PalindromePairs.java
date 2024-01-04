package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import js.json.JSList;
import js.json.JSMap;

/**
 * Beats 96.42% of users with Java (CPU); memory is poor, about 11%
 */
public class PalindromePairs extends LeetCode {

  public static void main(String[] args) {
    new PalindromePairs().run();
  }

  public void run() {
    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]");
    x("ab bade def");
    x("[\"dab\", \"ad\", \"abc\",\"aba\",\"\"]");
    x("[\"a\",\"abc\",\"aba\",\"\"]");
    x("[\"z\",\"a\",\"bcd\",\"cb\"]");
    x("[\"abc\",\"\"]");
    x("[\"ab\",\"ba\"]");
    x("[\"a\",\"\"]");
    x("[\"z\",\"a\",\"bcd\",\"cb\"]");
    x("[\"zy\",\"abx\",\"ba\",\"bb\"]");
    x("[\"abc\",\"ba\",\"cba\",\"dcba\"]");
    x("[\"jcbbha\",\"afjg\",\"hhdc\",\"acffiaiidggfjahd\",\"hddgdgdg\",\"ceaidddhcjcbhihhacgc\",\"ghfdb\",\"hai\",\"j\",\"hicfeebdjfhccjhadeih\",\"jfgfaehbhidjfggdfb\",\"bejcfhbcdfdejdhf\",\"dh\",\"ijcf\",\"gj\",\"dhedijfhfhgbecgg\",\"hhehadhbfbfbigg\",\"daehhjcaiiaebgcgd\",\"gjcj\",\"ajh\",\"gajea\",\"hjcbgedbffgga\",\"hbjibge\",\"gabbdejaecjbceih\",\"jdhjdja\",\"abdaibbcgbb\",\"efbbfichj\",\"aaaachheagihe\",\"adgi\",\"cjhcagbhjdcchaaaaifh\",\"ebg\",\"afdffeag\",\"jdebghbfejgf\",\"daghaghhcfjaec\",\"jceef\",\"bjhf\",\"djjibceajgcadfibcga\",\"bieifhcagid\",\"edfeaeidbjibgihi\",\"agcdabhdcfacjibeb\",\"dafaifeddgadacaic\",\"gaghddaebiicefeahij\",\"gdj\",\"bic\",\"bafffajdcffcjb\",\"bagcgggejjffgdifba\",\"hb\",\"fjgdihjjacbaagec\",\"b\",\"fgihfaffhb\",\"bffjafig\",\"jbhibcbejjd\",\"gjdcee\",\"cdeecbbhbfabhiebdd\",\"cijbdjaediccg\",\"aejahhdhbcbiaicaccif\",\"eeedjjabhibdah\",\"fgdij\",\"bcdhdghddhbfjbaccg\",\"dbigdhagifjic\",\"hghcabbaajei\",\"ea\",\"hgah\",\"eacg\",\"jeafhafggjhegjiabjhj\",\"hffhihcdhecabefeaa\",\"egdjihdc\",\"gddjjcdeifcfhcch\",\"achaifgbb\",\"bgejgiaafgfajjehdjdb\",\"ccieedfdegefdjcaifg\",\"hebjhghjafbhcjg\",\"hcibafcchig\",\"ahhdhabee\",\"iaicfii\",\"bgafgejh\",\"gjfaejjaibgiifg\",\"fgihijba\",\"aadgiajfiifg\",\"cegjgibjafiifaijdb\",\"afdihddihh\",\"ecaheeic\",\"eahedei\",\"ifgjbajaccdifhg\",\"eajaij\",\"ffcfgfbhiaebaeaaacb\",\"fdcgdbhfbihgicdhbf\",\"acj\",\"bggdibjgaciijccdaii\",\"fagfdfhfccbjeighf\",\"cffffdcffchg\",\"iadbdbfaijji\",\"cdech\",\"ajgjfjbab\",\"ihefbbcc\",\"cgadda\",\"fajabajafjedjdacb\",\"ch\",\"f\",\"eigafjadfgbfjcadbhcd\",\"ajbcfbaiihacec\",\"abjbbjifca\",\"gdfgefeeiedbhdbfhci\",\"egigcihfjbaiiffdibg\",\"eejfgeigaabbcaaiedi\",\"acgbfh\",\"dbfcihd\",\"hi\",\"cg\",\"edcbgbddjfccfghjacbc\",\"jedjacijfjiacefhi\",\"fibiicheeicdeehicg\",\"fbahafhjchbi\",\"haacgejdijhbid\",\"ajbhj\",\"fehjfbehddcdadbdb\",\"cihficdjjehiahgfaa\",\"bgbijfgeae\",\"hfdhdceccbiaccb\",\"jcibchfg\",\"ddjabaigh\",\"bb\",\"fhhciadihc\",\"digfbhg\",\"hbg\",\"dfeciajfjfgighi\",\"debaij\",\"fc\",\"gbjiabfjbgfgjaa\",\"abgjfheb\",\"jjehi\",\"iidace\",\"ijecaagffiae\",\"cgahbii\",\"igb\",\"ahedhifceachccibj\",\"h\",\"jbjgaajiedff\",\"bc\",\"eiifj\",\"ejeidigiiibec\",\"dbchbddggb\",\"fbabhj\",\"igh\",\"hegddcj\",\"gddgah\",\"egjdiidjgc\",\"abhfhagcifhcaiidf\",\"jfi\",\"i\",\"fdbiaieab\",\"hgcccdchadje\",\"ahihhafi\",\"ddgeegjijbebeeibbdg\",\"fcabgbejfdjaj\",\"jfibdiafgfgecjee\",\"idejgface\",\"ecieajdagcbbc\",\"eiehghedcg\",\"jdeedjaieebaedih\",\"gifbdecjcda\",\"ggibecafcebedifd\",\"adcbghdhh\",\"hjdhd\",\"hbeejddd\",\"jdceejbiccbgefidc\",\"ebe\",\"dg\",\"fiicdbibjced\",\"fdbfceibccdgcicgcjgh\",\"cfbheeadeiciaa\",\"agiddbbijjieb\",\"cabcbddf\",\"effci\",\"fcgbcfj\",\"cddacbcfcfd\",\"fabbeigjijciche\",\"ed\",\"ehdcfeejeae\",\"gbagecegcjaiefdc\",\"ggghjecjiijcc\",\"ehheec\",\"cjajgicccbffffhaai\",\"eecgfdahha\",\"bf\",\"eidcghceihhfhdeih\",\"dfdiaegficdiihigaifb\",\"fiiadifh\",\"djgfiagggj\",\"hgcghe\",\"gibafbccbbcdedjaeed\",\"egji\",\"efbcbdcgbgg\",\"aidbaaaeeajhjechcc\",\"cgcb\",\"hbcbgcdbejdicijaee\",\"cih\",\"ibjecgjjbd\",\"ijjdhgjagf\",\"a\",\"ediggeieghhijbe\",\"ifgfjaidieifhcgd\",\"dibfabcdeebjgjba\",\"gahcabhbcfhd\",\"bicegiehj\",\"aaahbjifgcea\",\"bdbhaehgbejdf\",\"iiahiia\",\"egibgiadi\",\"eficbcacbh\",\"hahhhadjabeacea\",\"cejhhe\",\"dhabbjgcjehbgff\",\"ffdjghd\",\"eadjdfjdhcgjifedeb\",\"hgihajefhfbbhcai\",\"jdaeieecgijhifjejfd\",\"hbgidf\",\"ifhjgigiaai\",\"fgiadieigcdbcj\",\"hgdjfhgh\",\"hfifhbehhfa\",\"hafjaegjdj\",\"dgeiiibfjciadd\",\"eh\",\"hjijjihbafh\",\"cjjccfjfeahifecffa\",\"ciabgchjdgcccgf\",\"jcdabhcdjfcagdga\",\"d\",\"g\",\"aahidacabheejdhjg\",\"gbaegcffhegihbi\",\"ae\",\"ggbhjcgbdgffegagfag\",\"ejabehcfbfddjhc\",\"addida\",\"ebhcgejjjjgj\",\"aebaibfijibhecjc\",\"ciehjcgjigbae\",\"ibjebbdbeg\",\"cdcjbbicehjee\",\"gjffaccgadjh\",\"fifhjg\",\"efi\",\"befgii\",\"bcghgchhhdfgi\",\"efgghcbhgjh\",\"aabghdbgj\",\"cche\",\"ij\",\"aghadedcji\",\"ehgdajhfeicahig\",\"hd\",\"agbiehefjdfbijg\",\"djeiighaceidgdbjhj\",\"fabbjhj\",\"ghjfbdehggbfeceggaf\",\"hcfjhajdfbgceceddhcf\",\"jee\",\"ifcjaacfbijjgacdih\",\"gedhefbea\",\"bha\",\"fbjjfieedbdfbdeaaba\",\"fdhecficdajcjddd\",\"iahcie\",\"cdhdabchegjhhgcdede\",\"jifjhcbfgahhffggh\",\"jajdefcifaejij\",\"ibiegjiiffcj\",\"fjab\",\"addfagdjiehcf\",\"acefjda\",\"bh\",\"iegea\",\"eefegfbddf\",\"geedeejcgcghiee\",\"cjjgbdjibhhgbgjecjb\",\"jgbdfjjeg\",\"diijgfe\",\"bbejehacgfjccidcbh\",\"dagcihcfbbibffjje\",\"jidgfaajbgcahde\",\"ef\",\"hjh\",\"biabbcjacfcihce\",\"dbhaigjajefabhhfiid\",\"bfbfdiajb\",\"dijdcbb\",\"fbhjbic\",\"bggehggefjdcagbjj\",\"ehfd\",\"jciddhj\",\"fejgjagjdiai\",\"fiaef\",\"hffhdg\",\"feeffgj\",\"ebgcaaagcefdbhbbci\",\"jicjbbbecdjh\",\"cbjfefbg\",\"jacfcfi\",\"cieb\",\"dgghiddj\",\"ibebbegigidbddadadfg\",\"ddfjiccj\",\"ichj\",\"chddgfiadcdgajfjafa\",\"cicdbfidi\",\"ghjbibddjef\",\"hcdaiicbd\",\"hda\",\"cejjeejieebegaid\",\"cadiff\",\"cebd\",\"af\",\"jfggiab\",\"eeaacbbcjhfgge\",\"ijecagdb\",\"iecjbbiebehbibg\",\"fiacjagf\",\"icghdiadggdagi\",\"cibgdhfbahcihbhjei\",\"ejbceehedid\",\"hbiihhiffdf\",\"icadi\",\"geffhg\",\"cdifdhafjceafcha\",\"dcjhbdgdjhfaceahfhcg\",\"fa\",\"iefdcc\",\"ehaddejhbcabiacbi\",\"geahhbbfdjegjccd\",\"jbfdggjhfcdjdbigei\",\"ihjeeegiebjbihhf\",\"dcj\",\"jebcc\",\"igaaciee\",\"cefaibfd\",\"gddeabfedjgdcdfchhc\",\"bjfhdgidhg\",\"cbai\",\"bafdhbdbfhajihfhjdb\",\"dffcaedjecjgeecfdcec\",\"fefd\",\"hffcdhgfedhgbidejce\",\"bfhj\",\"c\",\"cidcghdcecfcgfdfgbif\",\"jdjggeicb\",\"bff\",\"achejibgiggahfhede\",\"hehgeeifgfcbadjdab\",\"jdei\",\"ifcbhjhdhidgdjii\",\"digibfcagbcgijafabji\",\"fchjhbejaibefhegf\",\"idjac\",\"bchjebeejjic\",\"dheahe\",\"chcijggbfjbdc\",\"cjabcibcajjeifedieeh\",\"bhecabgaffdidgeccdaj\",\"agaefjhjhcc\",\"icihfhaaifbdgehhbg\",\"jfajhi\",\"eccic\",\"iecbbeggadjcdjfgbdeg\",\"e\",\"ijijaahacahj\",\"hdheidggdjg\",\"caegbf\",\"jbhchhbchci\",\"aiihcddbghgjijbhf\",\"cfagaebcigdchfjahhfg\",\"eb\",\"ijghecgaifcgd\",\"hdcejafhgda\",\"fefaifd\",\"iiiigfdbbcgcij\",\"ibcgafaheadeacjbh\",\"fijjbaidjeiaj\",\"gdiedchbid\",\"idcjgfdbgjf\",\"edjaibgcie\",\"aifhadhfdh\",\"aajeb\",\"hc\",\"aiiafhece\",\"dfbig\",\"hif\",\"gjbjeffbcjbcbejejdcf\",\"fjedhbibah\",\"bijhd\",\"ice\",\"dcia\",\"fehfjedj\",\"igidafjiejdheiehg\",\"hihcdje\",\"ghcbighfgbijbigci\",\"hcbhjacacbbjbcbhgdcd\",\"djfabdbabjbabgidacdi\",\"ddjibhfjiied\",\"acfbagiai\",\"ibjijedccbeebbcggdgb\",\"egjabieicjgjabdcaced\",\"dbbiiegh\",\"haegai\",\"dfgfijibfhjbieebdh\",\"jfifccifjaej\",\"gibceajgiihj\",\"fhbfaijjbhig\",\"eebagjggjfdcfbebdhb\",\"dcdajbcfdfccaadfj\",\"ehhdbibiefdbadcecjj\",\"chgbbhifbha\",\"bbagddefdegcjc\",\"ecbidjdgaedgh\",\"idg\",\"haafe\",\"eeeheabiahgcc\",\"ffcdja\",\"eed\",\"ceggbeace\",\"fhgfb\",\"ccdedfdfchjb\",\"jfjfefddfehef\",\"dejhbed\",\"cfdhjij\",\"jdahafaec\",\"jcccjadfcig\",\"edfbbbaeiccfff\",\"fbfdbhhchfjfeiad\",\"hcifddgddefaj\",\"gjafighje\",\"id\",\"dcffefbaehj\",\"gcaac\",\"cjaggigcabaaeegjdef\",\"hbcjejfdeiiheeghi\",\"abababffeajifaea\",\"bbihbhfaf\",\"gjihbe\",\"afabadfjcee\",\"ihbjhfajf\",\"fjchejfg\",\"dc\",\"gaajagfaadeacdjcgd\",\"fb\",\"fjhgfiehib\",\"cibcgbcjdgiibdjbf\",\"gbjdfhihfjj\",\"gjgcabiicijihggbeafi\",\"fdeh\",\"ijiibbecjcjeicd\",\"bgecihj\",\"gbdiificgccgh\",\"deebegbfgcdbjjcdjh\",\"ihfgbhiaahhfggcjcdh\",\"cabj\",\"cahaf\",\"jhicdadegjgjed\",\"cecjceghgigjgejacj\",\"didhi\",\"if\",\"fgadbhgech\",\"bgchbchfeibdcid\",\"cjefhigjdgggegghbeic\",\"cgg\",\"fiagcgjdbcf\",\"ejbabaedgbiba\",\"gccadcjjaadej\",\"fd\",\"jbghjdicbeaaa\",\"fg\",\"jefi\",\"dabcaad\",\"aaicbhhdicf\",\"defjf\",\"bieije\",\"bghdghhhhdbba\",\"ieajfh\",\"cff\",\"adehegigacdg\",\"affi\",\"jdaejihbifbbddd\",\"df\",\"cb\",\"dbee\",\"cj\",\"cjd\",\"fde\",\"dhejdfighhejhgc\",\"fdd\",\"ie\",\"ecbjbjajagi\",\"fgbiieeeccfcgb\",\"bbifdhehcaj\",\"fjchi\",\"ab\",\"ggcbdggciaigbhgegdhh\",\"cee\",\"cecjjhcbge\",\"iejf\",\"beeahabi\",\"ce\",\"gedbacfcfhgdhcagd\",\"ggd\",\"decdfcgjaagjifjb\",\"gjhbjajgcfgcfjgb\",\"jhfaejjcga\",\"gcag\",\"hhaafheeecfg\",\"gihajbebhgbcji\",\"afjh\",\"hccbbige\",\"ifaih\",\"fcdjhj\",\"aajeibbefghajje\",\"ghj\",\"jfaacdggacbhdif\",\"eefcaefhaijebchddej\",\"gggiiagfijjffeiccbd\",\"aeheifif\",\"cfcdjjedjefjcig\",\"hhceccjfbjebdbbgad\",\"egjhjjaajdciiehicd\",\"jgjedigaj\",\"abdjhjhd\",\"jahgcghhiccbf\",\"jdaigcdha\",\"ifjdci\",\"ghjhghbgdhghhdadehjd\",\"ccejgigceidb\",\"gejcgdaebfjdj\",\"gjjhiehdig\",\"idcebjagc\",\"aedchhdgdagjhbg\",\"ejghchgbegcdjjh\",\"jijbeacegdgaiegec\",\"bcdehbjaf\",\"ge\",\"beabcgicibagjddeg\",\"ddgcggbgf\",\"aeciafhchhdgcifhda\",\"cgcgaigdifjgj\",\"cehjhhbaehfab\",\"ehacaf\",\"ggiheda\",\"jafhabhdijejj\",\"igbghjaiefg\",\"iejicejecfgacieb\",\"ghadfggbhaecfibggd\",\"ehbieibfcfdgchgfhhij\",\"dhedicbgdjai\",\"dcddcejahagabhdgfefj\",\"djjidbagfdgadbdcadbg\",\"ajfihcaajbibgja\",\"ecjgegbifha\",\"chahafbfhebjadjhifai\",\"hfaeaejdihfjjdfgafhc\",\"jai\",\"aeabaiai\",\"iehe\",\"ieeejb\",\"dbehfcgfgbceef\",\"beefg\",\"ifhfdabfhjeb\",\"edbjabda\",\"bbbhbdciiaeaefia\",\"hdehfjhfgggbccf\",\"ibhhfgdbadd\",\"ahcfigciefjjadc\",\"hgcgaebjdafhi\",\"idbgg\",\"jfee\",\"hegchgecdajabfh\",\"gjjh\",\"jdfacjhfiajd\",\"jddefjh\",\"eefgacgdcbj\",\"agjhjecdgghccf\",\"ecjceih\",\"ieai\",\"dbgah\",\"eeb\",\"dbedhffjjhhdg\",\"higfdjgdcefihjjhcjic\",\"cjfbcbbc\",\"fbjfdjiagdhead\",\"geejhgdacig\",\"bjgjcdhbdfdejf\",\"hgedeb\",\"abi\",\"ji\",\"jc\",\"cajgjacgfbbd\",\"agjgfii\",\"bheaeaafceidhcb\",\"giijcgjdbb\",\"hfbeeceadijjjadb\",\"gacghgiadgh\",\"fcdedaabbge\",\"egfdbjiiaafe\",\"jej\",\"bdcacifcfjjafhajeh\",\"fajjja\",\"gagigecgedchg\",\"bdefaihfjbdjjgchidg\",\"idjdiiaed\",\"bhjfajfggbaf\",\"hdaeidgc\",\"jh\",\"cha\",\"ijeicagebcci\",\"hbfaafig\",\"dhhc\",\"bcfcaeefbafbbe\",\"cghdhchechebjcic\",\"fedaiggh\",\"jcgjbidcefg\",\"ggagcbbfafbcbd\",\"facee\",\"jijfdfecfbidcciai\",\"hedfgahgcdbech\",\"cc\",\"ghjffahcajaga\",\"ajgaea\",\"bbjgbaacd\",\"adcea\",\"befahhbhb\"]");
    x("[\"ab\",\"ba\"]");
    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]");
    x("[\"a\",\"\"]");
    x("[\"z\",\"a\",\"bcd\",\"cb\"]");
    x("[\"zy\",\"abx\",\"ba\",\"bb\"]");
    x("[\"abc\",\"ba\",\"cba\",\"dcba\"]");
    x("[\"a\",\"abc\",\"aba\",\"\"]");
  }

  private void x(String s) {
    String[] words = null;
    try {
      var words2 = new JSList(s);
      words = words2.asStringArray();
    } catch (Exception e) {
      words = extractStrings(s);
    }
    y(words);
  }

  private JSMap asMap(List<IntPair> x, String[] wlist) {
    var m = map();
    for (var pt : x) {
      m.put(pt.toString(), wlist[pt.a] + " " + wlist[pt.b]);
    }
    return m;
  }

  private void y(String[] wlist) {

    var un = new HashSet<String>();
    for (var s : wlist)
      un.add(s);
    checkState(un.size() == wlist.length);

    var result2 = intPairs(new SLOWPairs().palindromePairs(wlist));
    var exp = asMap(result2, wlist);
    db = true;
    checkpoint("start");
    var pp = palindromePairs(wlist);
    checkpoint("end");
    var result = intPairs(pp);
    var res = asMap(result, wlist);
    var exps = exp.prettyPrint();
    var ress = res.prettyPrint();

    if (!exps.equals(ress)) {
      pr("Expected:", INDENT, exps);
      pr("Got:", INDENT, ress);

      var missing = exp.deepCopy();
      missing.wrappedMap().keySet().removeAll(res.keySet());

      var unexp = res.deepCopy();
      unexp.wrappedMap().keySet().removeAll(exp.keySet());

      if (!missing.isEmpty())
        pr("results are MISSING:", INDENT, missing);
      if (!unexp.isEmpty())
        pr("results are UNEXPECTED:", INDENT, unexp);
      halt();
    }

    verify(res, exp);
  }

  private class SLOWPairs {
    public List<List<Integer>> palindromePairs(String[] wordsw) {
      result.clear();
      {
        int i = INIT_INDEX;
        for (var a : wordsw) {
          i++;
          int j = INIT_INDEX;
          for (var b : wordsw) {
            j++;
            if (i == j)
              continue;
            var s = a + b;
            if (isPal(s)) {
              var r = new ArrayList<Integer>(2);
              r.add(i);
              r.add(j);
              result.add(r);
            }
          }
        }
      }
      return result;
    }

    private List<List<Integer>> result = new ArrayList<>();

    private boolean isPal(String s) {
      int i = 0;
      int j = s.length() - 1;
      while (i < j) {
        if (s.charAt(i) != s.charAt(j))
          return false;
        i++;
        j--;
      }
      return true;
    }
  }

  // ------------------------------------------------------------------

  public List<List<Integer>> palindromePairs(String[] wordsw) {
    result.clear();

    // for a pair words VW to form a palindrome, then either
    //    V = W'X where W' is W reversed, and X is a palindrome;
    // or W = XV'.
    //
    // We can use tries to detect where V has W' as a prefix, or
    // W has V' as a suffix.  Then we can extract the appropriate 
    // substrings X, and determine if they are palindromes.

    byte[][] wordsAsBytes = new byte[wordsw.length][];
    for (int i = 0; i < wordsw.length; i++)
      wordsAsBytes[i] = stringToBytes(wordsw[i]);

    // Construct two tries: one with forward words, one with reversed words.

    var trie1 = new Trie();
    var trie2 = new Trie();
    for (int i = 0; i < wordsAsBytes.length; i++) {
      var wb = wordsAsBytes[i];
      trie1.add(wb, i, true);
      trie2.add(wb, i, false);
    }

    for (int i = 0; i < wordsAsBytes.length; i++) {
      var wb = wordsAsBytes[i];

      // Determine if word V has the form W'X for any W and palindrome X
      {
        var t1 = trie1;
        var t2 = trie2;
        int cursor = 0;
        while (true) {
          if (t2 == null)
            break;
          if (t2.index >= 0 && isPalindrome(wb, cursor, wb.length))
            addResult(i, t2.index);
          if (cursor == wb.length)
            break;
          t1 = t1.children[wb[cursor]];
          t2 = t2.children[wb[cursor]];
          cursor++;
        }
      }

      // Determine if word V has the form XW' for any W and palindrome X,
      // by walking tree for V'
      {
        var t1 = trie2;
        var t2 = trie1;
        int cursor = wb.length;
        while (true) {
          if (t2 == null)
            break;
          if (t2.index >= 0 && isPalindrome(wb, 0, cursor))
            addResult(t2.index, i);
          if (cursor-- == 0)
            break;
          t1 = t1.children[wb[cursor]];
          t2 = t2.children[wb[cursor]];
        }
      }
    }

    return new ArrayList<List<Integer>>(result.values());
  }

  private boolean isPalindrome(byte[] b, int i, int iEnd) {
    var j = iEnd - 1;
    while (i < j) {
      if (b[i] != b[j])
        return false;
      i++;
      j--;
    }
    return true;
  }

  private void addResult(int v, int w) {
    if (v == w)
      return;
    var r = new ArrayList<Integer>(2);
    r.add(v);
    r.add(w);
    // Use a unique key.  The word indices use log2 (5000) bits = 13; 16 for safety
    var key = (v << 16) | w;
    result.put(key, r);
  }

  private Map<Integer, List<Integer>> result = new HashMap<>();

  private static class Trie {

    public void add(byte[] word, int index, boolean fwd) {
      var node = this;
      final int wl = word.length - 1;
      for (int i = 0; i <= wl; i++) {
        var c = word[fwd ? i : wl - i];
        var child = node.children[c];
        if (child == null) {
          child = new Trie();
          node.children[c] = child;
        }
        node = child;
      }
      checkState(node.index < 0);
      node.index = index;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      aux(sb, 2);
      return sb.toString();
    }

    private void aux(StringBuilder sb, int indent) {
      if (index >= 0) {
        sb.append(" [");
        sb.append(index);
        sb.append("] ");
      }
      boolean anyChild = false;
      for (int i = 0; i < 26; i++) {
        var child = children[i];
        if (child == null)
          continue;
        if (!anyChild) {
          sb.append("{");
          anyChild = true;
        }
        sb.append('\n');
        sb.append(spaces(indent));
        sb.append((char) (i + 'a'));
        child.aux(sb, indent + 2);
      }
      if (anyChild)
        sb.append("}");
    }

    private Trie[] children = new Trie[26];
    private int index = -1;
  }

  private static byte[] stringToBytes(String s) {
    var res = new byte[s.length()];
    for (int i = 0; i < s.length(); i++)
      res[i] = (byte) (s.charAt(i) - 'a');
    return res;
  }

}
