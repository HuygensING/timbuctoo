package nl.knaw.huygens.timbuctoo.server.mediatypes.v2.search;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;

import java.util.List;

public class SearchRequestV2_1 {

  private List<FacetValue> facetValues;
  private List<SortParameter> sortParameters;
  private String term;
  private List<FullTextSearchParameter> fullTextSearchParameters;

  public SearchRequestV2_1() {
    // set default value, so the users do not have to handle a null value.
    // Jackson will not reset it.
    facetValues = Lists.newArrayList();
    fullTextSearchParameters = Lists.newArrayList();
    sortParameters = Lists.newArrayList();
  }

  public List<FacetValue> getFacetValues() {
    return facetValues;
  }

  public void setFacetValues(List<FacetValue> facetValues) {
    this.facetValues = facetValues;
  }

  public List<SortParameter> getSortParameters() {
    return sortParameters;
  }

  public void setSortParameters(List<SortParameter> sortParameters) {
    this.sortParameters = sortParameters;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }

  public void setFullTextSearchParameters(List<FullTextSearchParameter> fullTextSearchParameters) {
    this.fullTextSearchParameters = fullTextSearchParameters;
  }

  public List<FullTextSearchParameter> getFullTextSearchParameters() {
    return fullTextSearchParameters;
  }
}
