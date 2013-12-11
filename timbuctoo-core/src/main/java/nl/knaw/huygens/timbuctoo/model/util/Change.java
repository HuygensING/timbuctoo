package nl.knaw.huygens.timbuctoo.model.util;

import java.util.Date;

public class Change {

  /**
   * Returns a new {@code Change} instance with a generated time stamp.
   */
  public static Change newInstance() {
    Change instance = new Change();
    instance.setTimeStamp(new Date().getTime());
    return instance;
  }

  public static Change newInternalInstance() {
    Change instance = newInstance();
    instance.setAuthorId("timbuctoo");
    instance.setVreId("timbuctoo");
    return instance;
  }

  private long timeStamp;
  private String authorId;
  private String vreId;

  public Change() {}

  public Change(long timeStamp, String authorId, String vreId) {
    this.timeStamp = timeStamp;
    this.authorId = authorId;
    this.vreId = vreId;
  }

  public long getTimeStamp() {
    return timeStamp;
  }

  public void setTimeStamp(long timeStamp) {
    this.timeStamp = timeStamp;
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

}
