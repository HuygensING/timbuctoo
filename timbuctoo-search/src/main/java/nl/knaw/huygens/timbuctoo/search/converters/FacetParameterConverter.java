package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import com.google.common.collect.Lists;

public class FacetParameterConverter implements SearchParametersFieldConveter {

  @Override
  public void addToV1(SearchParameters searchParameters, SearchParametersV1 searchParametersV1) {
    List<FacetParameter> facetParameters = createFacetParameterList();
    for (nl.knaw.huygens.solr.FacetParameter facetParameter : searchParameters.getFacetValues()) {
      facetParameters.add(new DefaultFacetParameter(facetParameter.getName(), facetParameter.getValues()));
    }
    searchParametersV1.setFacetParameters(facetParameters);
  }

  protected List<FacetParameter> createFacetParameterList() {
    return Lists.newArrayList();
  }

}
