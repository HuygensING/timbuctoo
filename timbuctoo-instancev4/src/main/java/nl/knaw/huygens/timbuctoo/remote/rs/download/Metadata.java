package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Metadata {
  @JacksonXmlProperty(localName = "capability", isAttribute = true)
  private String capability;
  @JacksonXmlProperty(localName = "type", isAttribute = true)
  private String mimeType = "unknown"; // set default value make sure it is not null

  public String getCapability() {
    return capability;
  }

  public void setCapability(String capability) {
    this.capability = capability;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }
}
