package nl.knaw.huygens.timbuctoo.model.dwc;

import nl.knaw.huygens.timbuctoo.model.Document;
import nl.knaw.huygens.timbuctoo.model.util.Datable;


public class DWCPublication extends Document {

  private String summary;
  private String page_numbers;
  private String canonical_url;
  private String filename;

  public void setDocumentType(String type) {
    setDocumentType(DocumentType.valueOf(type));
  }

  public String getSummary() {
    return summary;
  }

  public void setSummary(String summary) {
    this.summary = summary;
  }

  public String getYear() {
    return getDate().toString();
  }

  public void setYear(String year) {
    setDate(new Datable(year));
  }

  public String getPage_numbers() {
    return page_numbers;
  }

  public void setPage_numbers(String page_numbers) {
    this.page_numbers = page_numbers;
  }

  public String getCanonical_url() {
    return canonical_url;
  }

  public void setCanonical_url(String canonical_url) {
    this.canonical_url = canonical_url;
  }

  public String getFilename() {
    return filename;
  }

  public void setFilename(String filename) {
    this.filename = filename;
  }

}
