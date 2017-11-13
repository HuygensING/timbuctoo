package nl.knaw.huygens.timbuctoo.remote.rs.discover;

import nl.knaw.huygens.timbuctoo.remote.rs.xml.RsLn;

import java.net.URI;

/**
 * Created on 2017-10-24 13:14.
 */
public class Description {

  private String rawContent;
  private URI describes;
  private RsLn describedByLink;

  public Description(String rawContent) {
    this.rawContent = rawContent;
  }

  public String getRawContent() {
    return rawContent;
  }

  public URI getDescribes() {
    return describes;
  }

  public RsLn getDescribedByLink() {
    return describedByLink;
  }

  void setDescribes(URI uri) {
    this.describes = uri;
  }

  void setDescribedByLink(RsLn describedByLink) {
    this.describedByLink = describedByLink;
  }

}
