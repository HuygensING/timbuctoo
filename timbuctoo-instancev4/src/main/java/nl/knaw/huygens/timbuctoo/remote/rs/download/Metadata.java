package nl.knaw.huygens.timbuctoo.remote.rs.download;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import org.slf4j.LoggerFactory;

import java.util.Date;

public class Metadata {
  @JacksonXmlProperty(localName = "capability", isAttribute = true)
  private String capability;
  @JacksonXmlProperty(localName = "type", isAttribute = true)
  private String mimeType = "unknown"; // set default value make sure it is not null
  @JacksonXmlProperty(localName = "datetime", isAttribute = true)
  private Date dateTime;

  @JacksonXmlProperty(localName = "isDataset", isAttribute = true)
  private boolean isDataset = false;

  public String getCapability() {
    return capability;
  }

  public boolean isDataset() {
    return isDataset;
  }

  public void setCapability(String capability) {
    this.capability = capability;
  }

  public String getMimeType() {
    LoggerFactory.getLogger(Metadata.class).info("getMimeType '{}'", mimeType);
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public void setIsDataset(boolean isDataset) {
    this.isDataset = isDataset;
  }

  public void setDateTime(Date dateTime) {
    this.dateTime = dateTime;
  }

  public Date getDateTime() {
    return dateTime;
  }
}
