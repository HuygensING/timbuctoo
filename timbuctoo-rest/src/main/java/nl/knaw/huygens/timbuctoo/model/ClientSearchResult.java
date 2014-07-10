package nl.knaw.huygens.timbuctoo.model;

import java.util.List;
import java.util.Set;

public abstract class ClientSearchResult {

  private Set<String> sortableFields;
  private int numFound;
  private List<? extends DomainEntity> results;
  private List<String> ids;
  private int start;
  private int rows;
  private String nextLink;
  private String prevLink;

  public ClientSearchResult() {
    super();
  }

  public List<String> getIds() {

    return ids;
  }

  public List<? extends DomainEntity> getResults() {
    return results;
  }

  public int getNumFound() {

    return numFound;
  }

  public int getStart() {

    return start;
  }

  public int getRows() {
    return rows;
  }

  public Set<String> getSortableFields() {
    return sortableFields;
  }

  public String getNextLink() {
    return nextLink;
  }

  public String getPrevLink() {
    return prevLink;
  }

  public void setRows(int rows) {
    this.rows = rows;

  }

  public void setStart(int start) {
    this.start = start;

  }

  public void setIds(List<String> ids) {
    this.ids = ids;

  }

  public void setResults(List<? extends DomainEntity> results) {
    this.results = results;

  }

  public void setNumFound(int numFound) {
    this.numFound = numFound;

  }

  public void setSortableFields(Set<String> sortableFields) {
    this.sortableFields = sortableFields;

  }

  public void setNextLink(String nextLink) {
    this.nextLink = nextLink;

  }

  public void setPrevLink(String prevLink) {
    this.prevLink = prevLink;

  }

}