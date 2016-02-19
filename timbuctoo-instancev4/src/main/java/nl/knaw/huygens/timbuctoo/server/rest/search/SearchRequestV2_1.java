package nl.knaw.huygens.timbuctoo.server.rest.search;

import com.google.common.collect.Lists;
import nl.knaw.huygens.timbuctoo.search.FacetValue;

import java.util.List;

public class SearchRequestV2_1 {

  private List<FacetValue> facetValues;
  private List<Object> sortParameters;
  private String term;

  public SearchRequestV2_1() {
    // set default value, so the users do not have to handle a null value.
    // Jackson will not reset it.
    facetValues = Lists.newArrayList();
  }

  public List<FacetValue> getFacetValues() {
    return facetValues;
  }

  public void setFacetValues(List<FacetValue> facetValues) {
    this.facetValues = facetValues;
  }

  public List<Object> getSortParameters() {
    return sortParameters;
  }

  public void setSortParameters(List<Object> sortParameters) {
    this.sortParameters = sortParameters;
  }

  public String getTerm() {
    return term;
  }

  public void setTerm(String term) {
    this.term = term;
  }
}
