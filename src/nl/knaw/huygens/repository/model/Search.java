package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.model.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.model.annotations.IDPrefix;

import com.fasterxml.jackson.annotation.JsonProperty;

@IDPrefix("QRY")
@DocumentTypeName("search")
public class Search extends PersistentDocument {

  private List<String> ids;
  private String term;
  private String sort;
  private String date;
  private String searchType;
  private String defaultVRE;

  public Search() {}

  public Search(List<String> ids, String type, String term, String sort, String date) {
    this.ids = ids;
    this.term = term;
    this.sort = sort;
    this.date = date;
    this.searchType = type;
  }

  public List<String> getIds() {
    return ids;
  }

  public void setIds(List<String> ids) {
    this.ids = ids;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public String getSort() {
    return sort;
  }

  public void setSort(String sort) {
    this.sort = sort;
  }

  public String getDate() {
    return date;
  }

  public void setDate(String date) {
    this.date = date;
  }

  public String getSearchType() {
    return searchType;
  }

  public void setSearchType(String type) {
    this.searchType = type;
  }

  @Override
  public String getDescription() {
    return "Search " + getId();
  }

  @Override
  @JsonProperty("!currentVariation")
  public String getCurrentVariation() {
    return defaultVRE;
  }

  @Override
  @JsonProperty("!currentVariation")
  public void setCurrentVariation(String defaultVRE) {
    this.defaultVRE = defaultVRE;
  }

}
