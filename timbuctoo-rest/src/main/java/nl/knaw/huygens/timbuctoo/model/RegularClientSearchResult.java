package nl.knaw.huygens.timbuctoo.model;

import java.util.List;
import java.util.Set;

import nl.knaw.huygens.facetedsearch.model.Facet;

// extends SystemEntity for easy (de)serialization
public class RegularClientSearchResult extends SystemEntity {

  private String term;
  private List<Facet> facets;
  private Set<String> sortableFields;
  private List<EntityRef> refs;
  private int numFound;
  private List<? extends DomainEntity> results;
  private List<String> ids;
  private int start;
  private int rows;
  private String nextLink;
  private String prevLink;

  @Override
  public String getDisplayName() {

    return null;
  }

  public List<? extends DomainEntity> getResults() {
    return results;
  }

  public String getTerm() {

    return term;
  }

  public List<Facet> getFacets() {

    return facets;
  }

  public int getNumFound() {

    return numFound;
  }

  public List<String> getIds() {

    return ids;
  }

  public List<EntityRef> getRefs() {

    return refs;
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

  public void setRefs(List<EntityRef> refs) {
    this.refs = refs;

  }

  public void setSortableFields(Set<String> sortableFields) {
    this.sortableFields = sortableFields;

  }

  public void setFacets(List<Facet> facets) {
    this.facets = facets;

  }

  public void setTerm(String term) {
    this.term = term;

  }

  public void setPrevLink(String prevLink) {
    this.prevLink = prevLink;

  }

  public void setNextLink(String nextLink) {
    this.nextLink = nextLink;

  }

}
