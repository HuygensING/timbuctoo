package nl.knaw.huygens.repository.model;

public class Change {
  public Change() {
    // Need an empty constructor for JSON deserialization...
  }
  public Change(long dateStamp, String authorId, String authorPrettyName) {
    this.dateStamp = dateStamp;
    this.authorId = authorId;
    this.authorPrettyName = authorPrettyName;
  }
  public void setDateStamp(long dateStamp) {
    this.dateStamp = dateStamp;
  }
  public void setAuthorId(String authorId) {
    this.authorId = authorId;
  }
  public void setAuthorPrettyName(String authorPrettyName) {
    this.authorPrettyName = authorPrettyName;
  }
  public long dateStamp;
  public String authorId;
  public String authorPrettyName;
}