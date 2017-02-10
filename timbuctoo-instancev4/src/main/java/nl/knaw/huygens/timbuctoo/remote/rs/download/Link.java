package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Link {
  @JacksonXmlProperty(isAttribute = true)
  private String rel;
  @JacksonXmlProperty(isAttribute = true)
  private String href;
}
