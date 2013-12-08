package nl.knaw.huygens.timbuctoo.model.util;

public class Change {

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
