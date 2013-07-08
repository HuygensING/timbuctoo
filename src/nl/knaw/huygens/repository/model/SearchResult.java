package nl.knaw.huygens.repository.model;

import java.util.List;

import nl.knaw.huygens.repository.annotations.DocumentTypeName;
import nl.knaw.huygens.repository.annotations.IDPrefix;
import nl.knaw.huygens.solr.FacetCount;

@IDPrefix("QRY")
@DocumentTypeName("search")
public class SearchResult extends SystemDocument implements Persistent {

  private List<String> ids;
  private String term;
  private String sort;
  private String date;
  private String searchType;
  private List<FacetCount> facets;

  public SearchResult() {}

  public SearchResult(List<String> ids, String type, String term, String sort, String date) {
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
  public String getDisplayName() {
    return "Search " + getId();
  }

  public List<FacetCount> getFacets() {
    return facets;
  }

  public void setFacets(List<FacetCount> facets) {
    this.facets = facets;
  }

}
