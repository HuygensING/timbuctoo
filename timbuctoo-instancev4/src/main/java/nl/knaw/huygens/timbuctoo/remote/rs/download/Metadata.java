package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Metadata {

  @JacksonXmlProperty(localName = "capability", isAttribute = true)
  private String capability;

  public String getCapability() {
    return capability;
  }

  public void setCapability(String capability) {
    this.capability = capability;
  }
}
