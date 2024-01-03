package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import js.json.JSList;
import js.json.JSMap;

/**
 * I think I need to use a trie data structure
 */
public class PalindromePairs extends LeetCode {

  public static void main(String[] args) {
    new PalindromePairs().run();
  }

  public void run() {

    if (false) {
      for (int s = 1; s < 5000; s++) {
        if (s % 1000 == 0)
          pr("s:", s);
        r(s * -3723, 50);
      }
      return;
    }

    x("[\"ab\",\"ba\"]");
    x("[\"abcd\",\"dcba\",\"lls\",\"s\",\"sssll\"]");
    x("[\"a\",\"\"]");
    x("[\"z\",\"a\",\"bcd\",\"cb\"]");
    x("[\"zy\",\"abx\",\"ba\",\"bb\"]");
    x("[\"abc\",\"ba\",\"cba\",\"dcba\"]");

    x("[\"a\",\"abc\",\"aba\",\"\"]");
    x("[\"jcbbha\",\"afjg\",\"hhdc\",\"acffiaiidggfjahd\",\"hddgdgdg\",\"ceaidddhcjcbhihhacgc\",\"ghfdb\",\"hai\",\"j\",\"hicfeebdjfhccjhadeih\",\"jfgfaehbhidjfggdfb\",\"bejcfhbcdfdejdhf\",\"dh\",\"ijcf\",\"gj\",\"dhedijfhfhgbecgg\",\"hhehadhbfbfbigg\",\"daehhjcaiiaebgcgd\",\"gjcj\",\"ajh\",\"gajea\",\"hjcbgedbffgga\",\"hbjibge\",\"gabbdejaecjbceih\",\"jdhjdja\",\"abdaibbcgbb\",\"efbbfichj\",\"aaaachheagihe\",\"adgi\",\"cjhcagbhjdcchaaaaifh\",\"ebg\",\"afdffeag\",\"jdebghbfejgf\",\"daghaghhcfjaec\",\"jceef\",\"bjhf\",\"djjibceajgcadfibcga\",\"bieifhcagid\",\"edfeaeidbjibgihi\",\"agcdabhdcfacjibeb\",\"dafaifeddgadacaic\",\"gaghddaebiicefeahij\",\"gdj\",\"bic\",\"bafffajdcffcjb\",\"bagcgggejjffgdifba\",\"hb\",\"fjgdihjjacbaagec\",\"b\",\"fgihfaffhb\",\"bffjafig\",\"jbhibcbejjd\",\"gjdcee\",\"cdeecbbhbfabhiebdd\",\"cijbdjaediccg\",\"aejahhdhbcbiaicaccif\",\"eeedjjabhibdah\",\"fgdij\",\"bcdhdghddhbfjbaccg\",\"dbigdhagifjic\",\"hghcabbaajei\",\"ea\",\"hgah\",\"eacg\",\"jeafhafggjhegjiabjhj\",\"hffhihcdhecabefeaa\",\"egdjihdc\",\"gddjjcdeifcfhcch\",\"achaifgbb\",\"bgejgiaafgfajjehdjdb\",\"ccieedfdegefdjcaifg\",\"hebjhghjafbhcjg\",\"hcibafcchig\",\"ahhdhabee\",\"iaicfii\",\"bgafgejh\",\"gjfaejjaibgiifg\",\"fgihijba\",\"aadgiajfiifg\",\"cegjgibjafiifaijdb\",\"afdihddihh\",\"ecaheeic\",\"eahedei\",\"ifgjbajaccdifhg\",\"eajaij\",\"ffcfgfbhiaebaeaaacb\",\"fdcgdbhfbihgicdhbf\",\"acj\",\"bggdibjgaciijccdaii\",\"fagfdfhfccbjeighf\",\"cffffdcffchg\",\"iadbdbfaijji\",\"cdech\",\"ajgjfjbab\",\"ihefbbcc\",\"cgadda\",\"fajabajafjedjdacb\",\"ch\",\"f\",\"eigafjadfgbfjcadbhcd\",\"ajbcfbaiihacec\",\"abjbbjifca\",\"gdfgefeeiedbhdbfhci\",\"egigcihfjbaiiffdibg\",\"eejfgeigaabbcaaiedi\",\"acgbfh\",\"dbfcihd\",\"hi\",\"cg\",\"edcbgbddjfccfghjacbc\",\"jedjacijfjiacefhi\",\"fibiicheeicdeehicg\",\"fbahafhjchbi\",\"haacgejdijhbid\",\"ajbhj\",\"fehjfbehddcdadbdb\",\"cihficdjjehiahgfaa\",\"bgbijfgeae\",\"hfdhdceccbiaccb\",\"jcibchfg\",\"ddjabaigh\",\"bb\",\"fhhciadihc\",\"digfbhg\",\"hbg\",\"dfeciajfjfgighi\",\"debaij\",\"fc\",\"gbjiabfjbgfgjaa\",\"abgjfheb\",\"jjehi\",\"iidace\",\"ijecaagffiae\",\"cgahbii\",\"igb\",\"ahedhifceachccibj\",\"h\",\"jbjgaajiedff\",\"bc\",\"eiifj\",\"ejeidigiiibec\",\"dbchbddggb\",\"fbabhj\",\"igh\",\"hegddcj\",\"gddgah\",\"egjdiidjgc\",\"abhfhagcifhcaiidf\",\"jfi\",\"i\",\"fdbiaieab\",\"hgcccdchadje\",\"ahihhafi\",\"ddgeegjijbebeeibbdg\",\"fcabgbejfdjaj\",\"jfibdiafgfgecjee\",\"idejgface\",\"ecieajdagcbbc\",\"eiehghedcg\",\"jdeedjaieebaedih\",\"gifbdecjcda\",\"ggibecafcebedifd\",\"adcbghdhh\",\"hjdhd\",\"hbeejddd\",\"jdceejbiccbgefidc\",\"ebe\",\"dg\",\"fiicdbibjced\",\"fdbfceibccdgcicgcjgh\",\"cfbheeadeiciaa\",\"agiddbbijjieb\",\"cabcbddf\",\"effci\",\"fcgbcfj\",\"cddacbcfcfd\",\"fabbeigjijciche\",\"ed\",\"ehdcfeejeae\",\"gbagecegcjaiefdc\",\"ggghjecjiijcc\",\"ehheec\",\"cjajgicccbffffhaai\",\"eecgfdahha\",\"bf\",\"eidcghceihhfhdeih\",\"dfdiaegficdiihigaifb\",\"fiiadifh\",\"djgfiagggj\",\"hgcghe\",\"gibafbccbbcdedjaeed\",\"egji\",\"efbcbdcgbgg\",\"aidbaaaeeajhjechcc\",\"cgcb\",\"hbcbgcdbejdicijaee\",\"cih\",\"ibjecgjjbd\",\"ijjdhgjagf\",\"a\",\"ediggeieghhijbe\",\"ifgfjaidieifhcgd\",\"dibfabcdeebjgjba\",\"gahcabhbcfhd\",\"bicegiehj\",\"aaahbjifgcea\",\"bdbhaehgbejdf\",\"iiahiia\",\"egibgiadi\",\"eficbcacbh\",\"hahhhadjabeacea\",\"cejhhe\",\"dhabbjgcjehbgff\",\"ffdjghd\",\"eadjdfjdhcgjifedeb\",\"hgihajefhfbbhcai\",\"jdaeieecgijhifjejfd\",\"hbgidf\",\"ifhjgigiaai\",\"fgiadieigcdbcj\",\"hgdjfhgh\",\"hfifhbehhfa\",\"hafjaegjdj\",\"dgeiiibfjciadd\",\"eh\",\"hjijjihbafh\",\"cjjccfjfeahifecffa\",\"ciabgchjdgcccgf\",\"jcdabhcdjfcagdga\",\"d\",\"g\",\"aahidacabheejdhjg\",\"gbaegcffhegihbi\",\"ae\",\"ggbhjcgbdgffegagfag\",\"ejabehcfbfddjhc\",\"addida\",\"ebhcgejjjjgj\",\"aebaibfijibhecjc\",\"ciehjcgjigbae\",\"ibjebbdbeg\",\"cdcjbbicehjee\",\"gjffaccgadjh\",\"fifhjg\",\"efi\",\"befgii\",\"bcghgchhhdfgi\",\"efgghcbhgjh\",\"aabghdbgj\",\"cche\",\"ij\",\"aghadedcji\",\"ehgdajhfeicahig\",\"hd\",\"agbiehefjdfbijg\",\"djeiighaceidgdbjhj\",\"fabbjhj\",\"ghjfbdehggbfeceggaf\",\"hcfjhajdfbgceceddhcf\",\"jee\",\"ifcjaacfbijjgacdih\",\"gedhefbea\",\"bha\",\"fbjjfieedbdfbdeaaba\",\"fdhecficdajcjddd\",\"iahcie\",\"cdhdabchegjhhgcdede\",\"jifjhcbfgahhffggh\",\"jajdefcifaejij\",\"ibiegjiiffcj\",\"fjab\",\"addfagdjiehcf\",\"acefjda\",\"bh\",\"iegea\",\"eefegfbddf\",\"geedeejcgcghiee\",\"cjjgbdjibhhgbgjecjb\",\"jgbdfjjeg\",\"diijgfe\",\"bbejehacgfjccidcbh\",\"dagcihcfbbibffjje\",\"jidgfaajbgcahde\",\"ef\",\"hjh\",\"biabbcjacfcihce\",\"dbhaigjajefabhhfiid\",\"bfbfdiajb\",\"dijdcbb\",\"fbhjbic\",\"bggehggefjdcagbjj\",\"ehfd\",\"jciddhj\",\"fejgjagjdiai\",\"fiaef\",\"hffhdg\",\"feeffgj\",\"ebgcaaagcefdbhbbci\",\"jicjbbbecdjh\",\"cbjfefbg\",\"jacfcfi\",\"cieb\",\"dgghiddj\",\"ibebbegigidbddadadfg\",\"ddfjiccj\",\"ichj\",\"chddgfiadcdgajfjafa\",\"cicdbfidi\",\"ghjbibddjef\",\"hcdaiicbd\",\"hda\",\"cejjeejieebegaid\",\"cadiff\",\"cebd\",\"af\",\"jfggiab\",\"eeaacbbcjhfgge\",\"ijecagdb\",\"iecjbbiebehbibg\",\"fiacjagf\",\"icghdiadggdagi\",\"cibgdhfbahcihbhjei\",\"ejbceehedid\",\"hbiihhiffdf\",\"icadi\",\"geffhg\",\"cdifdhafjceafcha\",\"dcjhbdgdjhfaceahfhcg\",\"fa\",\"iefdcc\",\"ehaddejhbcabiacbi\",\"geahhbbfdjegjccd\",\"jbfdggjhfcdjdbigei\",\"ihjeeegiebjbihhf\",\"dcj\",\"jebcc\",\"igaaciee\",\"cefaibfd\",\"gddeabfedjgdcdfchhc\",\"bjfhdgidhg\",\"cbai\",\"bafdhbdbfhajihfhjdb\",\"dffcaedjecjgeecfdcec\",\"fefd\",\"hffcdhgfedhgbidejce\",\"bfhj\",\"c\",\"cidcghdcecfcgfdfgbif\",\"jdjggeicb\",\"bff\",\"achejibgiggahfhede\",\"hehgeeifgfcbadjdab\",\"jdei\",\"ifcbhjhdhidgdjii\",\"digibfcagbcgijafabji\",\"fchjhbejaibefhegf\",\"idjac\",\"bchjebeejjic\",\"dheahe\",\"chcijggbfjbdc\",\"cjabcibcajjeifedieeh\",\"bhecabgaffdidgeccdaj\",\"agaefjhjhcc\",\"icihfhaaifbdgehhbg\",\"jfajhi\",\"eccic\",\"iecbbeggadjcdjfgbdeg\",\"e\",\"ijijaahacahj\",\"hdheidggdjg\",\"caegbf\",\"jbhchhbchci\",\"aiihcddbghgjijbhf\",\"cfagaebcigdchfjahhfg\",\"eb\",\"ijghecgaifcgd\",\"hdcejafhgda\",\"fefaifd\",\"iiiigfdbbcgcij\",\"ibcgafaheadeacjbh\",\"fijjbaidjeiaj\",\"gdiedchbid\",\"idcjgfdbgjf\",\"edjaibgcie\",\"aifhadhfdh\",\"aajeb\",\"hc\",\"aiiafhece\",\"dfbig\",\"hif\",\"gjbjeffbcjbcbejejdcf\",\"fjedhbibah\",\"bijhd\",\"ice\",\"dcia\",\"fehfjedj\",\"igidafjiejdheiehg\",\"hihcdje\",\"ghcbighfgbijbigci\",\"hcbhjacacbbjbcbhgdcd\",\"djfabdbabjbabgidacdi\",\"ddjibhfjiied\",\"acfbagiai\",\"ibjijedccbeebbcggdgb\",\"egjabieicjgjabdcaced\",\"dbbiiegh\",\"haegai\",\"dfgfijibfhjbieebdh\",\"jfifccifjaej\",\"gibceajgiihj\",\"fhbfaijjbhig\",\"eebagjggjfdcfbebdhb\",\"dcdajbcfdfccaadfj\",\"ehhdbibiefdbadcecjj\",\"chgbbhifbha\",\"bbagddefdegcjc\",\"ecbidjdgaedgh\",\"idg\",\"haafe\",\"eeeheabiahgcc\",\"ffcdja\",\"eed\",\"ceggbeace\",\"fhgfb\",\"ccdedfdfchjb\",\"jfjfefddfehef\",\"dejhbed\",\"cfdhjij\",\"jdahafaec\",\"jcccjadfcig\",\"edfbbbaeiccfff\",\"fbfdbhhchfjfeiad\",\"hcifddgddefaj\",\"gjafighje\",\"id\",\"dcffefbaehj\",\"gcaac\",\"cjaggigcabaaeegjdef\",\"hbcjejfdeiiheeghi\",\"abababffeajifaea\",\"bbihbhfaf\",\"gjihbe\",\"afabadfjcee\",\"ihbjhfajf\",\"fjchejfg\",\"dc\",\"gaajagfaadeacdjcgd\",\"fb\",\"fjhgfiehib\",\"cibcgbcjdgiibdjbf\",\"gbjdfhihfjj\",\"gjgcabiicijihggbeafi\",\"fdeh\",\"ijiibbecjcjeicd\",\"bgecihj\",\"gbdiificgccgh\",\"deebegbfgcdbjjcdjh\",\"ihfgbhiaahhfggcjcdh\",\"cabj\",\"cahaf\",\"jhicdadegjgjed\",\"cecjceghgigjgejacj\",\"didhi\",\"if\",\"fgadbhgech\",\"bgchbchfeibdcid\",\"cjefhigjdgggegghbeic\",\"cgg\",\"fiagcgjdbcf\",\"ejbabaedgbiba\",\"gccadcjjaadej\",\"fd\",\"jbghjdicbeaaa\",\"fg\",\"jefi\",\"dabcaad\",\"aaicbhhdicf\",\"defjf\",\"bieije\",\"bghdghhhhdbba\",\"ieajfh\",\"cff\",\"adehegigacdg\",\"affi\",\"jdaejihbifbbddd\",\"df\",\"cb\",\"dbee\",\"cj\",\"cjd\",\"fde\",\"dhejdfighhejhgc\",\"fdd\",\"ie\",\"ecbjbjajagi\",\"fgbiieeeccfcgb\",\"bbifdhehcaj\",\"fjchi\",\"ab\",\"ggcbdggciaigbhgegdhh\",\"cee\",\"cecjjhcbge\",\"iejf\",\"beeahabi\",\"ce\",\"gedbacfcfhgdhcagd\",\"ggd\",\"decdfcgjaagjifjb\",\"gjhbjajgcfgcfjgb\",\"jhfaejjcga\",\"gcag\",\"hhaafheeecfg\",\"gihajbebhgbcji\",\"afjh\",\"hccbbige\",\"ifaih\",\"fcdjhj\",\"aajeibbefghajje\",\"ghj\",\"jfaacdggacbhdif\",\"eefcaefhaijebchddej\",\"gggiiagfijjffeiccbd\",\"aeheifif\",\"cfcdjjedjefjcig\",\"hhceccjfbjebdbbgad\",\"egjhjjaajdciiehicd\",\"jgjedigaj\",\"abdjhjhd\",\"jahgcghhiccbf\",\"jdaigcdha\",\"ifjdci\",\"ghjhghbgdhghhdadehjd\",\"ccejgigceidb\",\"gejcgdaebfjdj\",\"gjjhiehdig\",\"idcebjagc\",\"aedchhdgdagjhbg\",\"ejghchgbegcdjjh\",\"jijbeacegdgaiegec\",\"bcdehbjaf\",\"ge\",\"beabcgicibagjddeg\",\"ddgcggbgf\",\"aeciafhchhdgcifhda\",\"cgcgaigdifjgj\",\"cehjhhbaehfab\",\"ehacaf\",\"ggiheda\",\"jafhabhdijejj\",\"igbghjaiefg\",\"iejicejecfgacieb\",\"ghadfggbhaecfibggd\",\"ehbieibfcfdgchgfhhij\",\"dhedicbgdjai\",\"dcddcejahagabhdgfefj\",\"djjidbagfdgadbdcadbg\",\"ajfihcaajbibgja\",\"ecjgegbifha\",\"chahafbfhebjadjhifai\",\"hfaeaejdihfjjdfgafhc\",\"jai\",\"aeabaiai\",\"iehe\",\"ieeejb\",\"dbehfcgfgbceef\",\"beefg\",\"ifhfdabfhjeb\",\"edbjabda\",\"bbbhbdciiaeaefia\",\"hdehfjhfgggbccf\",\"ibhhfgdbadd\",\"ahcfigciefjjadc\",\"hgcgaebjdafhi\",\"idbgg\",\"jfee\",\"hegchgecdajabfh\",\"gjjh\",\"jdfacjhfiajd\",\"jddefjh\",\"eefgacgdcbj\",\"agjhjecdgghccf\",\"ecjceih\",\"ieai\",\"dbgah\",\"eeb\",\"dbedhffjjhhdg\",\"higfdjgdcefihjjhcjic\",\"cjfbcbbc\",\"fbjfdjiagdhead\",\"geejhgdacig\",\"bjgjcdhbdfdejf\",\"hgedeb\",\"abi\",\"ji\",\"jc\",\"cajgjacgfbbd\",\"agjgfii\",\"bheaeaafceidhcb\",\"giijcgjdbb\",\"hfbeeceadijjjadb\",\"gacghgiadgh\",\"fcdedaabbge\",\"egfdbjiiaafe\",\"jej\",\"bdcacifcfjjafhajeh\",\"fajjja\",\"gagigecgedchg\",\"bdefaihfjbdjjgchidg\",\"idjdiiaed\",\"bhjfajfggbaf\",\"hdaeidgc\",\"jh\",\"cha\",\"ijeicagebcci\",\"hbfaafig\",\"dhhc\",\"bcfcaeefbafbbe\",\"cghdhchechebjcic\",\"fedaiggh\",\"jcgjbidcefg\",\"ggagcbbfafbcbd\",\"facee\",\"jijfdfecfbidcciai\",\"hedfgahgcdbech\",\"cc\",\"ghjffahcajaga\",\"ajgaea\",\"bbjgbaacd\",\"adcea\",\"befahhbhb\"]");
  }

  private void x(String s) {
    db = true;
    var words = new JSList(s);
    y(words);
  }

  private void y(JSList words) {
    y(words.asStringArray());
  }

  private void y(Collection<String> words) {
    var set = new HashSet<String>(words);
    y(set.toArray(new String[0]));
  }

  private JSMap asMap(List<IntPair> x, String[] wlist) {
    var m = map();
    for (var pt : x) {
      m.put(pt.toString(), wlist[pt.a] + " " + wlist[pt.b]);
    }
    pr("asMap, x:", x, INDENT, "wlist:", wlist, CR, "result:", m);
    return m;
  }

  private void y(String[] wlist) {

    pr(VERT_SP);
    var result2 = intPairs(new SLOWPairs().palindromePairs(wlist));
    var exp = asMap(result2, wlist);
    pr("Expected map:", exp);

    var result = intPairs(palindromePairs(wlist));
    var res = asMap(result, wlist);
    pr("Result map:", res);

    var exps = exp.prettyPrint();
    var ress = res.prettyPrint();

    pr("Expected:", INDENT, exps);
    pr("Results:", INDENT, ress);

    if (!exps.equals(ress)) {
      var missing = exp.deepCopy();
      pr("res.keyset:", res.keySet());
      pr("missing keyset:", missing.keySet());
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

  private void r(int seed, int count) {
    rand(seed);
    var words = randWords(count);
    Set<String> combined = hashSet();
    while (combined.size() < count) {
      var a = words.get(rand().nextInt(words.size()));
      if (rand().nextInt(4) == 0) {
        combined.add(a);
        continue;
      }
      var b = words.get(rand().nextInt(words.size()));
      combined.add(a + b);
    }
    y(combined);
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

  public List<List<Integer>> palindromePairs(String[] words) {

    results.clear();
    Map<String, Integer> fwdWordsSet = new HashMap<>();
    Map<String, Integer> bwdWordsSet = new HashMap<>();

    int i = -1;
    for (var s : words) {
      i++;
      fwdWordsSet.put(s, i);
      bwdWordsSet.put(reverse(s), i);
    }
    pr("fwdWordsSet:", fwdWordsSet);
    pr("bwdWordsSet:", bwdWordsSet);

    for (var pass = 0; pass < 2; pass++) {
      if (false && pass == 1 && alert("skipping pass 2"))
        continue;

      for (var entry : fwdWordsSet.entrySet()) {
        var s = entry.getKey();
        var index = entry.getValue();
        pr("fwdWord index:", index, quote(s));
        int sLength = s.length();
        for (int slen = 0; slen <= sLength; slen++) {
          var prefix = s.substring(0, sLength - slen);
          var bwdIndex = bwdWordsSet.getOrDefault(prefix, -1);
          pr("...prefix", slen, quote(prefix), "bwdIndex:", bwdIndex);
          if (bwdIndex < 0 || bwdIndex == index)
            continue;
          if (slen > 1) {
            pr("......checking if suffix is palindrome:", quote(s.substring(sLength - slen)));
            if (!isPal(s, sLength - slen, sLength)) {
              continue;
            }
          }
          pr("...adding result:", index, bwdIndex);
          addResult(index, bwdIndex);
        }
      }
      var tmp = fwdWordsSet;
      fwdWordsSet = bwdWordsSet;
      bwdWordsSet = tmp;
    }

    List<List<Integer>> out = new ArrayList<List<Integer>>(results.size());
    for (var x : results) {
      var y = new ArrayList<Integer>(2);
      y.add(x >> 16);
      y.add(x & 0xffff);
      out.add(y);
    }

    return out;
  }

  private boolean isPal(String s, int start, int stop) {

    int i = start;
    int j = stop - 1;
    while (i < j) {
      if (s.charAt(i) != s.charAt(j))
        return false;
      i++;
      j--;
    }
    return true;
  }

  private Set<Integer> results = new HashSet<>();

  private void addResult(int a, int b) {
    var key = (a << 16) | b;
    results.add(key);
  }

  private StringBuilder sbWork = new StringBuilder();

  private String reverse(String s) {
    var sb = sbWork;
    sb.setLength(0);
    for (int i = s.length() - 1; i >= 0; i--)
      sb.append(s.charAt(i));
    return sb.toString();
  }

}
