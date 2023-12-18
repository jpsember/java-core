package js.leetcode;

import static js.base.Tools.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import js.json.JSList;
import js.json.JSObject;

/**
 * The running time is in the bottom 20%.
 * 
 * Try another approach: a small, sorted central bucket that contains the
 * median, and two unsorted buckets on either side
 *
 */
public class FindMedianFromDataStream extends LeetCode {

  public static void main(String[] args) {
    new FindMedianFromDataStream().run();
  }

  public void run() {

    x("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[18],[],[251],[],[158],[],[158],[],[529],[],[180],[],[134],[],[529],[],[240],[],[435],[],[435],[],[316],[],[350],[],[537],[],[490],[],[198],[],[359],[],[493],[],[585],[],[614],[],[21],[],[583],[],[106],[],[549],[],[271],[],[174],[],[430],[],[222],[],[117],[],[159],[],[206],[],[100],[],[496],[],[129],[],[550],[],[411],[],[216],[],[271],[],[98],[],[119],[],[232],[],[629],[],[101],[],[218],[],[53],[],[468],[],[447],[],[402],[],[603],[],[584],[],[306],[],[269],[],[623],[],[88],[],[79],[],[521],[],[261],[],[544],[],[628],[],[121],[],[278],[],[132],[],[105],[],[72],[],[459],[],[408],[],[111],[],[291],[],[437],[],[7],[],[276],[],[34],[],[16],[],[254],[],[177],[],[550],[],[632],[],[464],[],[191],[],[167],[],[158],[],[331],[],[187],[],[123],[],[274],[],[321],[],[182],[],[93],[],[277],[],[595],[],[459],[],[349],[],[80],[],[195],[],[195],[],[214],[],[14],[],[384],[],[68],[],[427],[],[84],[],[609],[],[283],[],[59],[],[234],[],[327],[],[330],[],[509],[],[568],[],[65],[],[442],[],[380],[],[321],[],[589],[],[603],[],[64],[],[486],[],[175],[],[204],[],[421],[],[381],[],[209],[],[578],[],[580],[],[294],[],[287],[],[518],[],[261],[],[619],[],[155],[],[636],[],[287],[],[250],[],[209],[],[586],[],[299],[],[589],[],[65],[],[34],[],[216],[],[507],[],[380],[],[127],[],[186],[],[135],[],[533],[],[272],[],[563],[],[249],[],[350],[],[516],[],[229],[],[307],[],[127],[],[306],[],[195],[],[537],[],[269],[],[485],[],[446],[],[206],[],[222],[],[515],[],[240],[],[266],[],[417],[],[472],[],[140],[],[185],[],[439],[],[592],[],[283],[],[273],[],[188],[],[230],[],[195],[],[217],[],[321],[],[246],[],[563],[],[113],[],[411],[],[290],[],[351],[],[221],[],[76],[],[399],[],[19],[],[142],[],[544],[],[47],[],[270],[],[614],[],[36],[],[538],[],[179],[],[370],[],[387],[],[513],[],[570],[],[546],[],[347],[],[181],[],[257],[],[182],[],[412],[],[588],[],[66],[],[276],[],[381],[],[94],[],[461],[],[570],[],[550],[],[432],[],[192],[],[153],[],[382],[],[568],[],[233],[],[375],[],[186],[],[469],[],[402],[],[266],[],[143],[],[146],[],[44],[],[512],[],[540],[],[297],[],[520],[],[545],[],[436],[],[412],[],[597],[],[535],[],[13],[],[12],[],[109],[],[128],[],[84],[],[619],[],[85],[],[203],[],[478],[],[367],[],[77],[],[485],[],[640],[],[415],[],[533],[],[628],[],[338],[],[542],[],[560],[],[11],[],[80],[],[277],[],[148],[],[113],[],[23],[],[246],[],[453],[],[221],[],[310],[],[486],[],[262],[],[87],[],[330],[],[390],[],[199],[],[451],[],[540],[],[283],[],[466],[],[37],[],[639],[],[515],[],[319],[],[520],[],[289],[],[620],[],[470],[],[216],[],[249],[],[229],[],[130],[],[134],[],[637],[],[350],[],[605],[],[481],[],[220],[],[373],[],[60],[],[100],[],[205],[],[270],[],[305],[],[166],[],[525],[],[373],[],[627],[],[607],[],[111],[],[91],[],[491],[],[143],[],[353],[],[549],[],[434],[],[182],[],[386],[],[33],[],[540],[],[364],[],[456],[],[635],[],[317],[],[637],[],[0],[],[310],[],[559],[],[591],[],[8],[],[243],[],[331],[],[222],[],[117],[],[396],[],[163],[],[568],[],[309],[],[176],[],[61],[],[560],[],[102],[],[614],[],[586],[],[569],[],[261],[],[436],[],[45],[],[606],[],[549],[],[170],[],[123],[],[249],[],[296],[],[291],[],[486],[],[158],[],[300],[],[603],[],[371],[],[170],[],[498],[],[378],[],[480],[],[62],[],[586],[],[317],[],[502],[],[563],[],[399],[],[266],[],[93],[],[231],[],[22],[],[531],[],[245],[],[599],[],[189],[],[297],[],[637],[],[233],[],[237],[],[345],[],[387],[],[379],[],[419],[],[212],[],[323],[],[96],[],[64],[],[59],[],[144],[],[369],[],[252],[],[442],[],[597],[],[494],[],[438],[],[100],[],[157],[],[184],[],[478],[],[497],[],[107],[],[39],[],[263],[],[239],[],[143],[],[134],[],[224],[],[498],[],[79],[],[254],[],[240],[],[28],[],[533],[],[484],[],[389],[],[89],[],[621],[],[151],[],[313],[],[28],[],[274],[],[105],[],[601],[],[265],[],[548],[],[401],[],[254],[],[539],[],[113],[],[118],[],[165],[],[77],[],[478],[],[534],[],[65],[],[165],[],[580],[],[380],[],[83],[],[455],[],[506],[],[336],[],[445],[],[398],[],[384],[],[389],[],[209],[],[207],[],[537],[],[383],[],[176],[],[92],[],[478],[],[455],[],[503],[],[526],[],[623],[],[260],[],[522],[],[326],[],[335],[],[336],[],[221],[],[119],[],[426],[],[561],[],[301],[],[168],[],[377],[],[75],[],[615],[],[390],[],[88],[],[29],[],[343],[],[572],[],[608],[],[358],[],[91],[],[534],[],[336],[],[395],[],[580],[],[478],[],[448],[],[305],[],[640],[],[339],[],[139],[],[526],[],[592],[],[542],[],[215],[],[7],[],[168],[],[39],[],[622],[],[386],[],[81],[],[314],[],[599],[],[77],[],[400],[],[359],[],[622],[],[147],[],[203],[],[487],[],[612],[],[111],[],[413],[],[487],[],[95],[],[469],[],[475],[],[6],[],[9],[],[437],[],[502],[],[488],[],[77],[],[424],[],[625],[],[223],[],[102],[],[371],[],[408],[],[389],[],[312],[],[134],[],[77],[],[629],[],[318],[],[12],[],[533],[],[309],[],[575],[],[616],[],[62],[],[303],[],[85],[],[36],[],[20],[],[447],[],[388],[],[463],[],[543],[],[439],[],[204],[],[479],[],[9],[],[598],[],[437],[],[9],[],[480],[],[580],[],[450],[],[318],[],[521],[],[466],[],[214],[],[236],[],[44],[],[345],[],[276],[],[227],[],[378],[],[83],[],[562],[],[45],[],[514],[],[260],[],[370],[],[562],[],[352],[],[164],[],[450],[],[418],[],[114],[],[41],[],[169],[],[577],[],[412],[],[239],[],[75],[],[633],[],[439],[],[371],[],[566],[],[507],[],[214],[],[435],[],[39],[],[86],[],[215],[],[239],[],[621],[],[39],[],[377],[],[297],[],[156],[],[607],[],[342],[],[18],[],[374],[],[359],[],[560],[],[98],[],[343],[],[287],[],[418],[],[371],[],[308],[],[349],[],[607],[],[470],[],[568],[],[14],[],[302],[],[605],[],[635],[],[543],[],[471],[],[241],[],[519],[],[169],[],[381],[],[344],[],[159],[],[164],[],[624],[],[93],[],[123],[],[474],[],[184],[],[296],[],[619],[],[179],[],[194],[],[468],[],[245],[],[0],[]]");

    xx("[\"MedianFinder\", \"addNum\", \"addNum\", \"findMedian\", \"addNum\", \"findMedian\"]\n",
        "[[], [1], [2], [], [3], []]");

    xx("[\"MedianFinder\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\",\"addNum\",\"findMedian\"]",
        "[[],[12],[],[10],[],[13],[],[11],[],[5],[],[15],[],[1],[],[11],[],[6],[],[17],[],[14],[],[8],[],[17],[],[6],[],[4],[],[16],[],[8],[],[10],[],[2],[],[12],[],[0],[]]");
  }

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
        output = m.findMedian();
        output2 = s.findMedian();
        if (!output.equals(output2))
          die("failed to find median; got:", output, "but expected:", output2);
        break;
      }
      resultList.addUnsafe(output);
      expList.addUnsafe(output2);
    }
    pr(resultList);
    verify(resultList, expList);
  }

  public static JSList dz(short[] array) {
    return JSList.with(array);
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

    private static final int MIN_VALUE = -100001;
    private static final int MAX_VALUE = 100001;

    public MedianFinder() {
      midBucket = new Bucket(true);
      lowBucket = new Bucket(false);
      highBucket = new Bucket(false);
    }

    public void addNum(int num) {
      db(VERT_SP, "addNum:", num, "pop:", population);

      // Determine which bucket this will go into
      Bucket b = midBucket;
      if (population >= BUCKET_SIZE_CENTRAL) {
        boolean lower = num <= midBucket.minVal();
        boolean higher = num >= midBucket.maxVal();
        if (lower)
          b = lowBucket;
        else if (higher)
          b = highBucket;
      }

      b.add(num);

      population++;

      if (b == lowBucket)
        medianOrd++;

      db("after adding:", INDENT, this);
    }

    public double findMedian() {
      int leftOrder = (population - 1) >> 1;
      boolean odd = (population & 1) != 0;
      int rightOrder = leftOrder + (odd ? 0 : 1);
      //
      db("findMedian pop:", population, "lowSize:", lowBucket.size(), "mid:", midBucket.size(), "high:",
          highBucket.size());

      // If the left and right orders are not within the mid bucket, rejigger them

      if (leftOrder < medianOrd || rightOrder >= medianOrd + midBucket.size()) {
        halt("rejigger not implemented");
      }
      int leftValue = midBucket.array[leftOrder - medianOrd];
      int rightValue = midBucket.array[rightOrder - medianOrd];
      var median = (leftValue + rightValue) / 2.0;
      pr(INDENT, "median:", median);
      return median;
      //      Bucket b = midBucket;
      //      int offset = 0;
      //      var lowSize = lowBucket.size();
      //      if (leftOrder < lowSize) {
      //        b = lowBucket;
      //        db("...searching in low bucket");
      //      } else {
      //        offset += lowSize;
      //        var midSize = midBucket.size();
      //        if (rightOrder - offset >= midSize) {
      //          offset += midSize;
      //          b = highBucket;
      //          db("...searching in high bucket");
      //        }
      //      }
      //      return b.median(leftOrder - offset, rightOrder - offset);
      //         

      //      var leftValue = medianEnt.value;
      //      // Get order of the left median value (which is equal to the right value if the population is odd)
      //      var leftOrder = (population - 1) >> 1;
      //
      //      boolean odd = (population & 1) != 0;
      //      var rightValue = leftValue;
      //
      //      int rightOrder = leftOrder + (odd ? 0 : 1);
      //      if (rightOrder >= medianOrd + medianEnt.frequency)
      //        rightValue = medianEnt.next.value;
      //      return (leftValue + rightValue) / 2.0;
    }

    @Override
    public String toString() {
      var m = map();
      m.put("", "MedianFinder");
      m.put("pop", population);
      m.put("median_ord", medianOrd);
      m.put("mid bucket", midBucket.toJson());
      return m.prettyPrint();
    }

    private static int BUCKET_SIZE_CENTRAL = 50;
    private static int BUCKET_SIZE_EDGES = 200;

    private static class Bucket {
      public Bucket(boolean sorted) {
        this.sorted = sorted;
        this.array = new short[sorted ? BUCKET_SIZE_CENTRAL : BUCKET_SIZE_EDGES];
      }

      public int size() {
        return used;
      }

      public int add(int num) {
        if (used + 1 == array.length) {
          var newArray = new short[array.length * 2];
          System.arraycopy(array, 0, newArray, 0, used);
          array = newArray;
        }
        int slot = used;
        if (sorted) {
          int min = 0;
          int max = used;
          pr("used:", used);
          while (min < max) {
            int q = (min + max) / 2;
            int qn = array[q];
            pr("min:", min, "max:", max, "q:", q, "qn:", qn);
            if (qn == num) {
              min = q + 1;
              break;
            } else if (qn < num)
              min = q + 1;
            else if (qn > num)
              max = q;
            pr("min now:", min, "max:", max);
          }
          pr("...storing in slot:", min);
          System.arraycopy(array, min, array, min + 1, used - min);
          slot = min;
        }
        used++;
        array[slot] = (short) num;
        return slot;
      }

      public int minVal() {
        checkState(sorted && used != 0);
        return array[0];
      }

      public int maxVal() {
        checkState(sorted && used != 0);
        return array[used - 1];
      }

      @Override
      public String toString() {
        return toJson().prettyPrint();
      }

      public JSObject toJson() {
        var m = map();
        m.put("", "Bucket" + (sorted ? " mid" : "lo or high"));
        m.put("used", used);
        if (used < 20)
          m.put("nums", JSList.with(Arrays.copyOfRange(array, 0, used)));
        return m;
      }

      private boolean sorted;
      private int used;
      private short[] array;

    }

    private static class Ent {
      Ent(int value) {
        this.value = value;
      }

      int value;
      int frequency;

      @Override
      public String toString() {
        var s = "" + value;
        if (frequency > 1)
          s += " (x" + frequency + ")";
        return s;
      }
    }

    private Bucket lowBucket;
    private Bucket highBucket;
    private Bucket midBucket;

    // Number of numbers processed from data stream
    private int population;
    // The (zero-based) index of the first occurrence of the median value in a sorted list
    // of all the values
    private int medianOrd;
  }

}
