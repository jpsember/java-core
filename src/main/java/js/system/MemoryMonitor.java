package js.system;

import static js.base.Tools.*;

import java.util.Map;
import java.util.Map.Entry;
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

  /**
   * Thread safe
   */
  public void register(Object object, String message) {
    if (!active())
      return;
    if (object == null)
      return;

    ClassUsage usage = getUsage(object.getClass());
    usage.register(object, message);
    if (false && verbose())
      log("registering:", object.getClass(), "count:", usage.size(), nullToEmpty(message));
  }

  @Override
  public JSMap toJson() {
    JSMap m = map();
    synchronized (mClassMap) {
      for (Entry<Class, ClassUsage> entry : mClassMap.entrySet()) {
        ClassUsage usage = entry.getValue();
        usage.readStatus(false);
        m.put(entry.getKey().getName(), usage.statusMessage());
      }
    }
    return m;
  }

  public void doMaintenance() {
    todo("Add ability to report on growth of objects such as maps or arrays");
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

    synchronized (mClassMap) {
      for (Entry<Class, ClassUsage> entry : mClassMap.entrySet()) {
        ClassUsage usage = entry.getValue();
        usage.readStatus(true);
        if (usage.statusAlert() != null)
          allocMap.put(entry.getKey().getName(), usage.statusMessage());
      }
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
          mClassMap.put(klass, new ClassUsage());
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

    // Every increase of a couple of megabytes should be reported
    if ((memUsed - mPrevMemoryUsed) > 2 * 1024) {
      m.put("memory used (kb)", memUsed);
      mPrevMemoryUsed = memUsed;
    }
  }

  private static class ClassUsage {

    public void setMinSizeForAlert(int size) {
      synchronized (mUsageMap) {
        if (mMinSizeForAlert == 0)
          mMinSizeForAlert = size;
        else
          mMinSizeForAlert = Math.min(size, mMinSizeForAlert);
      }
    }

    public void register(Object object, String message) {
      message = ifNullOrEmpty(message, "");
      synchronized (mUsageMap) {
        mUsageMap.put(object, message);
      }
    }

    public int size() {
      synchronized (mUsageMap) {
        return mUsageMap.size();
      }
    }

    private static final float ALERT_GROWTH_FACTOR = 1.1f;

    public void readStatus(boolean update) {
      synchronized (mUsageMap) {
        int size = size();
        String alert = null;

        if (mNextAlertSize == 0)
          mNextAlertSize = Math.max(1, mMinSizeForAlert);
        if (size > mNextAlertSize) {
          if (update)
            mNextAlertSize = Math.round(size * ALERT_GROWTH_FACTOR);
          alert = "***";
        }
        mStatusAlert = alert;
        String message = String.format("%3s %5d", nullToEmpty(alert), size);
        mStatusMessage = message;
      }
    }

    public String statusMessage() {
      return mStatusMessage;
    }

    public String statusAlert() {
      return mStatusAlert;
    }

    private WeakHashMap<Object, String> mUsageMap = new WeakHashMap<>();
    private int mNextAlertSize;
    private int mMinSizeForAlert;
    private String mStatusAlert;
    private String mStatusMessage;
  }

  private boolean mActive;
  private Map<Class, ClassUsage> mClassMap = concurrentHashMap();
  private boolean mStarted;
  private long mPrevTime;
  private long mInitialReportDelay = DateTimeTools.SECONDS(30);
  private long mMaintenanceIntervalMs = DateTimeTools.SECONDS(30);
  private long mFirstMemoryTime;
  private long mPrevMemoryUsed;
  private long mBaselineMemUsedKb;

}
