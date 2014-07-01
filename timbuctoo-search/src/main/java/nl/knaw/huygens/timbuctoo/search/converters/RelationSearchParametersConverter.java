package nl.knaw.huygens.timbuctoo.search.converters;

import static nl.knaw.huygens.timbuctoo.model.Relation.RELATION_TYPE_ID_FACET_NAME;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.DefaultFacetParameter;
import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import com.google.common.collect.Lists;

public class RelationSearchParametersConverter {

  public SearchParametersV1 toSearchParamtersV1(RelationSearchParameters relationSearchParameters) {
    SearchParametersV1 searchParametersV1 = createSearchParametersV1();

    List<FacetParameter> facetParameters = createFacetParameterList();

    facetParameters.add(new DefaultFacetParameter(RELATION_TYPE_ID_FACET_NAME, relationSearchParameters.getRelationTypeIds()));

    searchParametersV1.setFacetParameters(facetParameters);

    return searchParametersV1;
  }

  protected SearchParametersV1 createSearchParametersV1() {
    return new SearchParametersV1();
  }

  protected List<FacetParameter> createFacetParameterList() {
    return Lists.newArrayList();
  }

}
