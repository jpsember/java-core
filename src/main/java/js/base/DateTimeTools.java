/**
 * MIT License
 * 
 * Copyright (c) 2021 Jeff Sember
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 * 
 **/
package js.base;

import static js.base.Tools.*;

import java.time.*;
import java.time.format.DateTimeFormatter;

public final class DateTimeTools {

  // ------------------------------------------------------------------
  // Epoch time
  // ------------------------------------------------------------------

  /**
   * Determine if a time is reasonable: sometime on or after 2016, etc
   */
  public static boolean isSane(long epochTimeMs) {
    final long minReasonableTime = 1451606400000L;
    final long maxReasonableTime = 1735689600000L;
    return epochTimeMs >= minReasonableTime && epochTimeMs < maxReasonableTime;
  }

  /**
   * Construct a duration in milliseconds from milliseconds
   */
  public static final long MILLISECONDS(int milliseconds) {
    return milliseconds;
  }

  /**
   * Construct a duration in milliseconds from seconds
   */
  public static final long SECONDS(int seconds) {
    return seconds * 1000L;
  }

  /**
   * Construct a duration in milliseconds from minutes
   */
  public static final long MINUTES(int minutes) {
    return minutes * 60 * 1000L;
  }

  /**
   * Construct a duration in milliseconds from hours
   */
  public static final long HOURS(int hours) {
    return hours * 3600 * 1000L;
  }

  /**
   * Construct a duration in milliseconds from days
   */
  public static final long DAYS(int days) {
    return days * 24 * 3600 * 1000L;
  }

  // ------------------------------------------------------------------
  // ZonedDateTime conversion
  // ------------------------------------------------------------------
  // See https://blog.joda.org/2014/11/converting-from-joda-time-to-javatime.html

  /**
   * Get epoch milliseconds from ZonedDateTime
   */
  public static long millis(ZonedDateTime t) {
    return t.toInstant().toEpochMilli();
  }

  /**
   * Convert epoch milliseconds to ZonedDateTime, UTC
   */
  public static ZonedDateTime zonedDateTime(long millis) {
    Instant instant = Instant.ofEpochMilli(millis);
    return ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
  }

  // ------------------------------------------------------------------
  // Time zones
  // ------------------------------------------------------------------

  public static final ZoneId OUR_TIME_ZONE = ZoneId.of("Canada/Pacific");

  // ------------------------------------------------------------------
  // Formatting
  // ------------------------------------------------------------------

  /**
   * A succinct human-readable description, without the date
   */
  public static String humanTimeString() {
    return humanTimeString(getRealMs());
  }

  /**
   * A succinct human-readable description, without the date
   */
  public static String humanTimeString(long epoch) {
    return humanTimeString(epoch, OUR_TIME_ZONE, FMT_SUCCINCT);
  }

  public static String humanTimeString(long epoch, ZoneId zoneIdOrNull, DateTimeFormatter formatterOrNull) {
    ZoneId zoneId = nullTo(zoneIdOrNull, OUR_TIME_ZONE);
    DateTimeFormatter formatter = nullTo(formatterOrNull, FMT_SUCCINCT);
    Instant instant = Instant.ofEpochMilli(epoch);
    return humanTimeString(instant, zoneId, formatter);
  }

  public static String humanTimeString(Instant instant, ZoneId zoneId, DateTimeFormatter formatter) {
    ZonedDateTime dateTime = instant.atZone(zoneId);
    return formatter.format(dateTime);
  }

  public static String humanDuration(long epoch) {
    StringBuilder sb = new StringBuilder();
    if (epoch < 0) {
      sb.append("- ");
      epoch = -epoch;
    }
    long day = DAYS(1);
    if (epoch >= day) {
      int n = (int) (epoch / day);
      epoch = epoch % day;
      sb.append(n);
      sb.append("d");
    }
    long hour = HOURS(1);
    if (epoch >= hour) {
      int n = (int) (epoch / hour);
      epoch = epoch % hour;
      sb.append(n);
      sb.append("h");
    }
    long min = MINUTES(1);
    if (epoch >= min) {
      int n = (int) (epoch / min);
      epoch = epoch % min;
      sb.append(n);
      sb.append("m");
    }
    sb.append(String.format("%4.1fs", ((int) epoch) / 1000f));
    return sb.toString();
  }

  public static String toString(long millis) {
    return FMT_PREFERRED.format(Instant.ofEpochMilli(millis));
  }

  /**
   * Formatter for ISO_INSTANT, in UTC time zone
   */
  public static final DateTimeFormatter FMT_ISO_INSTANT = DateTimeFormatter
      .ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC);

  // See: https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html

  /**
   * Our preferred formatter, with our preferred time zone
   */
  public static final DateTimeFormatter FMT_PREFERRED = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
      .withZone(OUR_TIME_ZONE);
  public static final DateTimeFormatter FMT_PREPIMG_S = DateTimeFormatter.ofPattern("yyyy_MM_dd HH_mm_ss")
      .withZone(DateTimeTools.OUR_TIME_ZONE);
  public static final DateTimeFormatter FMT_PREPIMG_MS = DateTimeFormatter
      .ofPattern("yyyy_MM_dd HH_mm_ss_SSS").withZone(DateTimeTools.OUR_TIME_ZONE);

  private static final DateTimeFormatter FMT_PREFERRED_WITH_UTC = FMT_PREFERRED.withZone(ZoneOffset.UTC);
  public static final DateTimeFormatter FMT_SUCCINCT = DateTimeFormatter.ofPattern("E h:mm a' (+'ss.S's)'");
  public static final DateTimeFormatter FMT_MONTH_MIN_FILENAME = DateTimeFormatter.ofPattern("MMM_dd_HH_mm");
  public static final DateTimeFormatter FMT_HOUR_MS_DISPLAY = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");

  // ------------------------------------------------------------------
  // Parsing
  // ------------------------------------------------------------------

  /**
   * Parse a DateTime when we're not sure of the format
   */
  public static ZonedDateTime parseSloppyDateTime(String s) {
    DateTimeFormatter format = FMT_ISO_INSTANT;
    if (s.contains("/"))
      format = FMT_PREFERRED_WITH_UTC;
    return ZonedDateTime.parse(s, format);
  }

  /**
   * Get system time, bypassing any clocks
   */
  public static long getRealMs() {
    return System.currentTimeMillis();
  }

  /**
   * Have current thread sleep, bypassing any clocks
   */
  public static long sleepForRealMs(long millis) {
    try {
      if (millis > 0)
        Thread.sleep(millis);
      return millis;
    } catch (InterruptedException e) {
      throw asRuntimeException(e);
    }
  }

}
