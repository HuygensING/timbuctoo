package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import java.net.URI;

/**
 * Created on 2017-10-24 13:14.
 */
public class Description {

  private String rawContent;
  private URI describes;

  public Description(String rawContent) {
    this.rawContent = rawContent;
  }

  public String getRawContent() {
    return rawContent;
  }

  public URI getDescribes() {
    return describes;
  }

  void setDescribes(URI uri) {
    this.describes = uri;
  }
}
