package nl.knaw.huygens.timbuctoo.search.converters;

import static nl.knaw.huygens.timbuctoo.search.converters.DefaultFacetParameterMatcher.likeFacetParameter;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

import nl.knaw.huygens.solr.FacetParameter;
import nl.knaw.huygens.solr.SearchParameters;
import nl.knaw.huygens.solr.SearchParametersV1;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.google.common.collect.Lists;

public class FacetParameterConverterTest {

  @Mock
  private List<nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter> newParametersListMock;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testAddToV1() {
    SearchParametersV1 searchParametersV1 = mock(SearchParametersV1.class);

    SearchParameters searchParameters = new SearchParameters();

    String name1 = "name1";
    ArrayList<String> values1 = Lists.newArrayList("value1", "value2");
    FacetParameter fp1 = new FacetParameter().setName(name1).setValues(values1);
    String name2 = "name2";
    ArrayList<String> values2 = Lists.newArrayList("value3", "value4");
    FacetParameter fp2 = new FacetParameter().setName(name2).setValues(values2);
    List<FacetParameter> facetParameters = Lists.newArrayList(fp1, fp2);
    searchParameters.setFacetValues(facetParameters);

    FacetParameterConverter instance = new FacetParameterConverter() {
      @Override
      protected List<nl.knaw.huygens.facetedsearch.model.parameters.FacetParameter> createFacetParameterList() {
        return newParametersListMock;
      }
    };

    // action
    instance.addToV1(searchParameters, searchParametersV1);

    // verify
    verify(newParametersListMock).add(argThat(likeFacetParameter(name1, values1)));
    verify(newParametersListMock).add(argThat(likeFacetParameter(name2, values2)));
    verify(searchParametersV1).setFacetParameters(newParametersListMock);
  }
}
