package nl.knaw.huygens.repository.model.atlg;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.Legislation;

@DocumentTypeName("atlglegislation")
public class ATLGLegislation extends Legislation {

  private String origFilename;
  private String reference;
  private String pages;

  public String getOrigFilename() {
    return origFilename;
  }

  public void setOrigFilename(String origFilename) {
    this.origFilename = origFilename;
  }

  public String getReference() {
    return reference;
  }

  public void setReference(String reference) {
    this.reference = reference;
  }

  public String getPages() {
    return pages;
  }

  public void setPages(String pages) {
    this.pages = pages;
  }

}
