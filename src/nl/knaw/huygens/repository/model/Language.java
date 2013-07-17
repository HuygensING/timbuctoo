package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;

import com.google.common.collect.Lists;

// TODO add validation

/**
 * Denotes a language, catering for ISO 639-2 and 639-1 codes.
 * 
 * See: <code>http://www.loc.gov/standards/iso639-2/php/code_list.php</code>.
 */
@IDPrefix("LAN")
@DocumentTypeName("language")
public class Language extends DomainDocument {

  /** Codes, at least one, first is default. */
  private List<String> codes;
  /** English name. */
  private String nameEng;
  /** French name. */
  private String nameFra;
  /** ISO code, or not. */
  boolean iso;

  public Language() {
    codes = Lists.newArrayListWithCapacity(3);
  }

  @Override
  public String getDisplayName() {
    return nameEng;
  }

  public List<String> getCodes() {
    return codes;
  }

  public void setCodes(List<String> codes) {
    this.codes = codes;
  }

  public void addCode(String code) {
    codes.add(code);
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
