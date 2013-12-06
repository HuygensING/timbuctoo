package nl.knaw.huygens.timbuctoo.model.util;

public class Change {

  private long dateStamp;
  private String authorId;
  private String authorPrettyName;
  private String vreId;
  private String vrePrettyName;

  public Change() {}

  public Change(long dateStamp, String authorId, String authorPrettyName, String vreId, String vrePrettyName) {
    this.dateStamp = dateStamp;
    this.authorId = authorId;
    this.authorPrettyName = authorPrettyName;
    this.vreId = vreId;
    this.vrePrettyName = vrePrettyName;
  }

  public long getDateStamp() {
    return dateStamp;
  }

  public void setDateStamp(long dateStamp) {
    this.dateStamp = dateStamp;
  }

  public String getAuthorId() {
    return authorId;
  }

  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }

  public String getAuthorPrettyName() {
    return authorPrettyName;
  }

  public void setAuthorPrettyName(String authorPrettyName) {
    this.authorPrettyName = authorPrettyName;
  }

  public String getVreId() {
    return vreId;
  }

  public void setVreId(String vreId) {
    this.vreId = vreId;
  }

  public String getVrePrettyName() {
    return vrePrettyName;
  }

  public void setVrePrettyName(String vrePrettyName) {
    this.vrePrettyName = vrePrettyName;
  }

}
