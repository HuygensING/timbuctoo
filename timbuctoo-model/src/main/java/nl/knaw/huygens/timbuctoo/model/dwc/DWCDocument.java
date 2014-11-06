package nl.knaw.huygens.timbuctoo.model.dwc;

import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.util.Datable;


public class DWCDocument extends Document {

  private String pageNumbers;
  private String canonicalUrl;
  private String filename;

  public void setDocumentType(String type) {
    setDocumentType(DocumentType.valueOf(type));
  }

  public String getYear() {
    return getDate().toString();
  }

  public void setYear(String year) {
    setDate(new Datable(year));
  }

  public String getPageNumbers() {
    return pageNumbers;
  }

  public void setPageNumbers(String page_numbers) {
    this.pageNumbers = page_numbers;
  }

  public String getCanonicalUrl() {
    return canonicalUrl;
  }

  public void setCanonicalUrl(String canonical_url) {
    this.canonicalUrl = canonical_url;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

}
