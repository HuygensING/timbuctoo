package nl.knaw.huygens.timbuctoo.remote.rs.xml;

import javax.xml.bind.annotation.adapters.XmlAdapter;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

/**
 * Adapter for conversion between a {@link ZonedDateTime} and an ISO 8601 profile know as W3C Datetime format.
 * <p>
 * The unmarshal proces will convert given datetime strings to UTC (Coordinated Universal Time).
 * If a datetime string has no timezone info, we assume that the datetime is in the timezone
 * that is returned by the static {@link ZonedDateTimeAdapter#getZoneId()}. This timezone will default to
 * {@link ZoneId#systemDefault()}, and can be set with {@link ZonedDateTimeAdapter#setZoneId(ZoneId)}.
 *  </p>
 * The following examples illustrate the conversion of several valid W3C Datetime strings (unmarshal -> marshal).
 * Datetimes without timezone info were calculated with an offset of UTC+10:00.
 * <pre>
 *    2016 -> 2015-12-31T14:00Z
 *    2015-08 -> 2015-07-31T14:00Z
 *    2014-08-09 -> 2014-08-08T14:00Z
 *    2013-03-09T14 -> 2013-03-09T04:00Z
 *    2012-03-09T14:30 -> 2012-03-09T04:30Z
 *    2011-03-09T14:30:29 -> 2011-03-09T04:30:29Z
 *    2010-01-09T14:30:29.1 -> 2010-01-09T04:30:29.100Z
 *    2010-03-09T14:30:29.123 -> 2010-03-09T04:30:29.123Z
 *    2010-04-09T14:30:29.1234 -> 2010-04-09T04:30:29.123400Z
 *    2010-06-09T14:30:29.123456 -> 2010-06-09T04:30:29.123456Z
 *
 *    2009-03-09T14:30:29.123+01:00 -> 2009-03-09T13:30:29.123Z
 *    2008-03-09T14:30:29.123-01:00 -> 2008-03-09T15:30:29.123Z
 *    2007-03-09T14:30:29.123Z -> 2007-03-09T14:30:29.123Z
 *    2006-03-09T14:30:29.123456789Z -> 2006-03-09T14:30:29.123456789Z
 *    2005-03-09T14:30Z -> 2005-03-09T14:30Z
 * </pre>
 *
 * @see
 * <a href="https://www.w3.org/TR/1998/NOTE-datetime-19980827">https://www.w3.org/TR/1998/NOTE-datetime-19980827</a>
 * <a href="https://tools.ietf.org/html/rfc3339">https://tools.ietf.org/html/rfc3339</a>
 *
*/
public class ZonedDateTimeAdapter extends XmlAdapter<String, ZonedDateTime> {

  private static ZoneId ZONE_ID;

  public static ZoneId getZoneId() {
    if (ZONE_ID == null) {
      ZONE_ID = ZoneId.systemDefault();
    }
    return ZONE_ID;
  }

  public static ZoneId setZoneId(ZoneId zoneId) {
    ZoneId oldZoneId = ZONE_ID;
    ZONE_ID = zoneId;
    return oldZoneId;
  }

  private DateTimeFormatter localFormat = new DateTimeFormatterBuilder()
    .appendPattern("yyyy[-MM[-dd['T'HH[:mm[:ss]]]]]")
    .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
    .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
    .parseDefaulting(ChronoField.HOUR_OF_DAY, 0)
    .optionalStart()
    .appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true)
    .toFormatter();

  @Override
  public ZonedDateTime unmarshal(String value) throws Exception {
    if (value == null) {
      return null;
    }
    if (value.matches(".*([Z]|[+-][0-9]{1,2}:[0-9]{1,2})$")) {
      return ZonedDateTime.parse(value).withZoneSameInstant(ZoneOffset.UTC);
    } else {
      LocalDateTime local = LocalDateTime.parse(value, localFormat);
      ZonedDateTime localZ = ZonedDateTime.of(local, getZoneId());
      return localZ.withZoneSameInstant(ZoneOffset.UTC);
    }
  }

  @Override
  public String marshal(ZonedDateTime value) throws Exception {
    if (value == null) {
      return null;
    }
    return value.toString();
  }
}
