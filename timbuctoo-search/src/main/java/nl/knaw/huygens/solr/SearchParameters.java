package nl.knaw.huygens.solr;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

public class SearchParameters {

  private String scopeId = "";
  private String term = "*";
  private String sort = SolrFields.SCORE;
  private String sortDir = "asc";
  private String typeString;
  private boolean caseSensitive = false;
  private String[] facetFields = new String[] {};
  private List<FacetParameter> facetParameters = Lists.newArrayList();
  private Map<String, FacetInfo> facetInfoMap;
  private List<String> orderLevels = Lists.newArrayList();
  private boolean fuzzy = false;

  public SearchParameters setScopeId(String scopeId) {
    this.scopeId = scopeId;
    return this;
  }

  public String getScopeId() {
    return scopeId;
  }

  public SearchParameters setTerm(final String term) {
    if ("".equals(term)) {
      this.term = "*";
    } else {
      this.term = term;
    }
    return this;
  }

  public String getTerm() {
    return term;
  }

  public SearchParameters setSort(final String sort) {
    this.sort = sort;
    return this;
  }

  public String getSort() {
    return sort;
  }

  public SearchParameters setSortDir(final String sortDir) {
    this.sortDir = sortDir;
    return this;
  }

  public String getSortDir() {
    return sortDir;
  }

  public String getTypeString() {
    return typeString;
  }

  public void setTypeString(String typeString) {
    this.typeString = typeString;
  }

  @JsonIgnore
  public boolean isAscending() {
    return "asc".equals(sortDir);
  }

  public SearchParameters setCaseSensitive(boolean matchCase) {
    this.caseSensitive = matchCase;
    return this;
  }

  public boolean isCaseSensitive() {
    return caseSensitive;
  }

  public SearchParameters setFacetFields(String[] _facetFields) {
    this.facetFields = _facetFields;
    return this;
  }

  public String[] getFacetFields() {
    return facetFields;
  }

  public SearchParameters setOrderLevels(List<String> orderLevels) {
    this.orderLevels = orderLevels;
    return this;
  }

  public List<String> getOrderLevels() {
    return orderLevels;
  }

  public boolean isFuzzy() {
    return fuzzy;
  }

  public SearchParameters setFuzzy(Boolean fuzzy) {
    this.fuzzy = fuzzy;
    return this;
  }

  public List<FacetParameter> getFacetValues() {
    return facetParameters;
  }

  public SearchParameters setFacetValues(List<FacetParameter> fp) {
    this.facetParameters = fp;
    return this;
  }

  public Map<String, FacetInfo> getFacetInfoMap() {
    return facetInfoMap;
  }

  public SearchParameters setFacetInfoMap(Map<String, FacetInfo> facetInfoMap) {
    this.facetInfoMap = facetInfoMap;
    return this;
  }

}
