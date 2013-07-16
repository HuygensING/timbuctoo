package nl.knaw.huygens.repository.model;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;

// TODO add validation

/**
 * Denotes a language, catering for ISO 639-2 and 639-1 codes.
 * 
 * See: <code>http://www.loc.gov/standards/iso639-2/php/code_list.php</code>.
 */
@IDPrefix("LAN")
@DocumentTypeName("language")
public class Language extends DomainDocument {

  /** Two-letter code; usually ISO 639-1. */
  private String code2;
  /** Three-letter code; usually ISO 639-2. */
  private String code3;
  /** English name. */
  private String nameEng;
  /** French name. */
  private String nameFra;
  /** ISO code, or not. */
  boolean iso;

  @Override
  public String getDisplayName() {
    return nameEng;
  }

  public String getCode2() {
    return code2;
  }

  public void setCode2(String code) {
    code2 = code;
  }

  public String getCode3() {
    return code3;
  }

  public void setCode3(String code) {
    code3 = code;
  }

  public String getNameEng() {
    return nameEng;
  }

  public void setNameEng(String name) {
    nameEng = name;
  }

  public String getNameFra() {
    return nameFra;
  }

  public void setNameFra(String name) {
    nameFra = name;
  }

  public boolean isIso() {
    return iso;
  }

  public void setIso(boolean iso) {
    this.iso = iso;
  };

}
