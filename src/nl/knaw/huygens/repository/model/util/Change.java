package nl.knaw.huygens.repository.model.util;

public class Change {

  public long dateStamp;
  public String authorId;
  public String authorPrettyName;
  public String vreId;
  public String vrePrettyName;

  public Change() {
    // Need an empty constructor for JSON deserialization...
  }

  public Change(long dateStamp, String authorId, String authorPrettyName, String vreId, String vrePrettyName) {
    this.dateStamp = dateStamp;
    this.authorId = authorId;
    this.authorPrettyName = authorPrettyName;
    this.vreId = vreId;
    this.vrePrettyName = vrePrettyName;
  }

}
