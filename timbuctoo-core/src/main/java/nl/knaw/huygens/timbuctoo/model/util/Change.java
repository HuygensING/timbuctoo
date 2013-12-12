package nl.knaw.huygens.timbuctoo.model.util;

import java.util.Date;

public class Change {

  /**
   * Returns a new {@code Change} instance for internal use.
   */
  public static Change newInternalInstance() {
    return new Change("timbuctoo", "timbuctoo");
  }

  /**
   * Returns a new time stamp.
   */
  public static long newTimeStamp() {
    return new Date().getTime();
  }

  // -------------------------------------------------------------------

  private long timeStamp;
  private String userId;
  private String vreId;

  public Change() {}

  public Change(long timeStamp, String userId, String vreId) {
    this.timeStamp = timeStamp;
    this.userId = userId;
    this.vreId = vreId;
  }

  public Change(String userId, String vreId) {
    this(newTimeStamp(), userId, vreId);
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

}
