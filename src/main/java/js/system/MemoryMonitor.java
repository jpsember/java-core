package js.system;

import static js.base.Tools.*;

import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import js.base.BaseObject;
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

  public boolean active() {
    return mActive;
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

    ClassUsage usage = mClassMap.get(object.getClass());
    if (usage == null) {
      // Avoid race condition that could cause construction of two maps for a particular class
      synchronized (mClassMap) {
        usage = mClassMap.get(object.getClass());
        if (usage == null)
          mClassMap.put(object.getClass(), new ClassUsage(object.getClass()));
      }
      usage = mClassMap.get(object.getClass());
    }
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

    if (!m.isEmpty()) {
      m.put("", "================= Memory Monitor =================");
      pr(m);
    }
  }

  private static class ClassUsage {

    public ClassUsage(Class klass) {
      //mClass = klass;
    }

    //    private final Class mClass;

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
        if (size > mLargestSize) {
          if (size > mLargestSize * ALERT_GROWTH_FACTOR) {
            if (update)
              mLargestSize = size;
            alert = "!!! ";
          }
        }
        mStatusAlert = alert;
        String message = String.format("%5s %5d", nullToEmpty(alert), size);
        mStatusMessage = message;
        //      
        //      if (nonEmpty(alert))
        //        pr("=== Object count for", mClass.getName(), ":", message);
        //      }
      }
    }

    public String statusMessage() {
      return mStatusMessage;
    }

    public String statusAlert() {
      return mStatusAlert;
    }

    private WeakHashMap<Object, String> mUsageMap = new WeakHashMap<>();
    private int mLargestSize;

    private String mStatusAlert;
    private String mStatusMessage;
  }

  private boolean mActive;
  private Map<Class, ClassUsage> mClassMap = concurrentHashMap();

}
