package nl.knaw.huygens.timbuctoo.search.converters;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetField;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import com.google.common.collect.Lists;

public class FacetFieldConverter implements SearchParametersFieldConveter {

  @Override
  public void addToV1(SearchParameters searchParameters, SearchParametersV1 searchParametersV1) {
    List<FacetField> facetFields = Lists.newArrayList();
    for (String fieldName : searchParameters.getFacetFields()) {
      facetFields.add(new FacetField(fieldName));
    }
    searchParametersV1.setFacetFields(facetFields);
  }

}
