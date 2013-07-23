package nl.knaw.huygens.repository.model;

import java.util.Map;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;

import com.google.common.collect.Maps;

/**
 * Denotes a language, catering for ISO 639-2 and 639-1 codes.
 */
@IDPrefix("LAN")
@DocumentTypeName("language")
public class Language extends DomainDocument {

  /** Codes, e.g. "iso_639_2". */
  private Map<String, String> codes;
  /** English name. */
  private String name;

  public Language() {
    codes = Maps.newHashMapWithExpectedSize(2);
  }

  @Override
  public String getDisplayName() {
    return getName();
  }

  public Map<String, String> getCodes() {
    return codes;
  }

  public void setCodes(Map<String, String> codes) {
    this.codes = codes;
  }

  public void addCode(String key, String value) {
    codes.put(key, value);
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

}
