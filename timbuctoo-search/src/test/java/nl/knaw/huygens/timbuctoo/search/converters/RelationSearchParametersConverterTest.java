package nl.knaw.huygens.timbuctoo.search.converters;

import static nl.knaw.huygens.timbuctoo.model.Relation.RELATION_TYPE_ID_FACET_NAME;
import static nl.knaw.huygens.timbuctoo.search.converters.DefaultFacetParameterMatcher.likeFacetParameter;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter;
import nl.knaw.huygens.solr.RelationSearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class RelationSearchParametersConverterTest {
  @Mock
  List<FacetParameter> facetParameterList;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void toSearchParamtersV1() {
    // setup
    final SearchParametersV1 searchParametersV1Mock = mock(SearchParametersV1.class);

    RelationSearchParametersConverter instance = new RelationSearchParametersConverter() {
      @Override
      protected SearchParametersV1 createSearchParametersV1() {
        return searchParametersV1Mock;
      }

      @Override
      protected List<FacetParameter> createFacetParameterList() {
        return facetParameterList;
      }
    };

    RelationSearchParameters relationSearchParameters = new RelationSearchParameters();
    List<String> relationTypeIds = Lists.newArrayList("id1", "id2");
    relationSearchParameters.setRelationTypeIds(relationTypeIds);

    // action
    SearchParametersV1 actualSearchParametersV1 = instance.toSearchParamtersV1(relationSearchParameters);

    // verify
    assertThat(actualSearchParametersV1, is(notNullValue()));
    verify(facetParameterList).add(argThat(likeFacetParameter(RELATION_TYPE_ID_FACET_NAME, relationTypeIds)));
    verify(searchParametersV1Mock).setFacetParameters(facetParameterList);
  }
}
