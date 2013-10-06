package nl.knaw.huygens.timbuctoo.model;

import java.util.Date;
import java.util.List;

import nl.knaw.huygens.timbuctoo.annotations.EntityTypeName;
import nl.knaw.huygens.timbuctoo.annotations.IDPrefix;
import nl.knaw.huygens.timbuctoo.facet.FacetCount;

@IDPrefix("QURY")
@EntityTypeName("search")
public class SearchResult extends SystemEntity implements Persistent {

  public static final String DATE_FIELD = "date";

  private List<String> ids;
  private String term;
  private String sort;
  private Date date;
  private String searchType;
  private List<FacetCount> facets;

  public SearchResult() {}

  public SearchResult(List<String> ids, String type, String term, String sort, Date date) {
    this.ids = ids;
    this.term = term;
    this.sort = sort;
    this.date = date;
    searchType = type;
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

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getSearchType() {
    return searchType;
  }

  public void setSearchType(String type) {
    searchType = type;
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
