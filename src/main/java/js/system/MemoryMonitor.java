package js.system;

import static js.base.Tools.*;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import js.base.BaseObject;
import js.base.DateTimeTools;
import js.json.JSMap;

public final class MemoryMonitor extends BaseObject {

  public static MemoryMonitor sharedInstance() {
    return sSharedInstance;
  }

  private static final MemoryMonitor sSharedInstance = new MemoryMonitor();

  public MemoryMonitor setActive(boolean active) {
    mActive = active;
    return this;
  }

  public MemoryMonitor setInitialReportDelay(long delayMs) {
    assertNotStarted();
    mInitialReportDelay = delayMs;
    return this;
  }

  public MemoryMonitor setMinMaintenanceInterval(long delayMs) {
    assertNotStarted();
    mMaintenanceIntervalMs = delayMs;
    return this;
  }

  private void assertNotStarted() {
    if (mStarted)
      throw badState("MemoryMonitor already started");
  }

  public boolean active() {
    return mActive;
  }

  /**
   * Set the minimum size for generating an alert for a particular class of
   * object
   * 
   * Thread safe
   */
  public void alertSize(Class klass, int minSizeForAlert) {
    if (!active())
      return;
    ClassUsage usage = getUsage(klass);
    usage.setMinSizeForAlert(minSizeForAlert);
  }

  /**
   * Thread safe
   */
  public void register(Object object) {
    register(object, null);
  }

  private static final int DEFAULT_MIN_ALERT_SIZE = 10;

  public void monitorSize(String name, Collection collection) {
    monitorSize(name, collection, DEFAULT_MIN_ALERT_SIZE);
  }

  public void monitorSize(String name, Collection collection, int minAlertSize) {
    if (active())
      auxMonitorSize(name, minAlertSize, collection.size());
  }

  public void monitorSize(String name, Map map) {
    monitorSize(name, map, DEFAULT_MIN_ALERT_SIZE);
  }

  public void monitorSize(String name, Map map, int minAlertSize) {
    if (active())
      auxMonitorSize(name, minAlertSize, map.size());
  }

  private void auxMonitorSize(String name, int minAlertSize, int currentSize) {
    // Ensure that a monitor exists with this name; and update its size to currentSize
    Tracker t = getMonitor(name);
    t.setMinSizeForAlert(minAlertSize);
    t.updateSize(currentSize);
  }

  /**
   * Thread safe
   */
  public void register(Object object, Object messageObjectOrNull) {
    if (!active())
      return;
    if (object == null)
      return;

    ClassUsage usage = getUsage(object.getClass());
    String message = "";
    if (messageObjectOrNull != null)
      message = messageObjectOrNull.toString();
    usage.register(object, message);
  }

  @Override
  public JSMap toJson() {
    JSMap m = map();
    for (Tracker tracker : buildTrackerList()) {
      tracker.readStatus(false);
      m.put(tracker.label(), tracker.statusMap());
    }
    return m;
  }

  private List<Tracker> buildTrackerList() {
    List<Tracker> trackers = arrayList();

    synchronized (mClassMap) {
      trackers.addAll(mClassMap.values());
    }

    synchronized (mCollectionMap) {
      trackers.addAll(mCollectionMap.values());
    }
    return trackers;
  }

  public void doMaintenance() {
    mStarted = true;
    long currentTime = System.currentTimeMillis();
    if (mPrevTime != 0 && currentTime - mPrevTime < mMaintenanceIntervalMs)
      return;
    mPrevTime = currentTime;
    if (mFirstMemoryTime == 0)
      mFirstMemoryTime = currentTime;

    if (currentTime - mFirstMemoryTime < mInitialReportDelay)
      return;

    JSMap m = map();
    JSMap allocMap = map();

    for (Tracker usage : buildTrackerList()) {
      usage.readStatus(true);
      if (usage.statusMap() != null)
        m.put(usage.label(), usage.statusMap());
    }

    if (!allocMap.isEmpty())
      m.put("objects", allocMap);

    updateMemoryLeakDetection(m);

    if (!m.isEmpty()) {
      m.put("", "================= Memory Monitor =================");
      pr(m);
    }
  }

  /**
   * Get monitor for a collection
   * 
   * Thread safe
   */
  private Tracker getMonitor(String name) {
    Tracker usage = mCollectionMap.get(name);
    if (usage == null) {
      // Avoid race condition that could cause construction of two maps for a particular class
      synchronized (mCollectionMap) {
        usage = mCollectionMap.get(name);
        if (usage == null)
          mCollectionMap.put(name, new Tracker(name));
      }
      usage = mCollectionMap.get(name);
    }
    return usage;
  }

  /**
   * Get ClassUsage for a class, creating one if necessary
   * 
   * Thread safe
   */
  private ClassUsage getUsage(Class klass) {
    ClassUsage usage = mClassMap.get(klass);
    if (usage == null) {
      // Avoid race condition that could cause construction of two maps for a particular class
      synchronized (mClassMap) {
        usage = mClassMap.get(klass);
        if (usage == null)
          mClassMap.put(klass, new ClassUsage(klass.getName()));
      }
      usage = mClassMap.get(klass);
    }
    return usage;
  }

  private void updateMemoryLeakDetection(JSMap m) {

    long memUsed = SystemUtil.memoryUsed() / 1024;
    if (mBaselineMemUsedKb == 0) {
      mBaselineMemUsedKb = memUsed;
      mPrevMemoryUsed = memUsed;
      m.put("memory_init", "Baseline memory set to: " + memUsed + " kb");
    }

    final double ALERT_GROWTH_FACTOR = 1.1;
    if (memUsed > mPrevMemoryUsed * ALERT_GROWTH_FACTOR) {
      m.put("memory used (kb)", memUsed);
      mPrevMemoryUsed = memUsed;
    }
  }

  private static class Tracker {

    protected void storeAlertInfo(JSMap m) {
    }

    public Tracker(String label) {
      mLabel = label;
    }

    public final void setMinSizeForAlert(int size) {
      synchronized (this) {
        if (mMinSizeForAlert == 0)
          mMinSizeForAlert = size;
        else
          mMinSizeForAlert = Math.min(size, mMinSizeForAlert);
      }
    }

    public int size() {
      return mSize;
    }

    public String label() {
      return mLabel;
    }

    public final void readStatus(boolean update) {
      final float ALERT_GROWTH_FACTOR = 1.1f;
      synchronized (this) {
        int size = size();
        String alert = null;

        if (mNextAlertSize == 0) {
          mNextAlertSize = mMinSizeForAlert;
          if (mNextAlertSize == 0)
            mNextAlertSize = DEFAULT_MIN_ALERT_SIZE;
        }

        if (size > mNextAlertSize) {
          if (update)
            mNextAlertSize = Math.round(size * ALERT_GROWTH_FACTOR);
          alert = "***";
        }
        mStatusMap = null;
        if (alert != null) {
          JSMap m = map();
          m.put("*** size", size);
          storeAlertInfo(m);
          mStatusMap = m;
        }
      }
    }

    public final JSMap statusMap() {
      return mStatusMap;
    }

    public void updateSize(int size) {
      mSize = size;
    }

    private int mSize;
    private int mNextAlertSize;
    private int mMinSizeForAlert;
    private JSMap mStatusMap;
    private final String mLabel;
  }

  private static class ClassUsage extends Tracker {

    public ClassUsage(String label) {
      super(label);
    }

    public void register(Object object, String message) {
      message = ifNullOrEmpty(message, "");
      synchronized (this) {
        mUsageMap.put(object, message);
      }
    }

    @Override
    public int size() {
      synchronized (this) {
        return mUsageMap.size();
      }
    }

    @Override
    protected void storeAlertInfo(JSMap m) {

      Map<String, Integer> freqMap = hashMap();
      List<String> labels = arrayList();
      synchronized (this) {
        labels.addAll(mUsageMap.values());
      }
      for (String label : labels) {
        Integer val = freqMap.get(label);
        if (val == null)
          val = 0;
        freqMap.put(label, val + 1);
      }

      List<String> sortedKeys = arrayList();
      sortedKeys.addAll(freqMap.keySet());
      sortedKeys.sort((a, b) -> freqMap.get(b) - freqMap.get(a));
      for (String label : sortedKeys)
        m.putNumbered(label, freqMap.get(label));
    }

    private WeakHashMap<Object, String> mUsageMap = new WeakHashMap<>();
  }

  private boolean mActive;
  private Map<Class, ClassUsage> mClassMap = concurrentHashMap();
  private Map<String, Tracker> mCollectionMap = concurrentHashMap();

  private boolean mStarted;
  private long mPrevTime;
  private long mInitialReportDelay = DateTimeTools.SECONDS(30);
  private long mMaintenanceIntervalMs = DateTimeTools.SECONDS(30);
  private long mFirstMemoryTime;
  private long mPrevMemoryUsed;
  private long mBaselineMemUsedKb;

}
