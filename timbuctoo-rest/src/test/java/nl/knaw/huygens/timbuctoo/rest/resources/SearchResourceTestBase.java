package nl.knaw.huygens.timbuctoo.rest.resources;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Set;

import javax.ws.rs.core.MediaType;

import nl.knaw.huygens.solr.SearchParametersV1;
import nl.knaw.huygens.timbuctoo.model.DomainEntity;
import nl.knaw.huygens.timbuctoo.model.Person;
import nl.knaw.huygens.timbuctoo.model.SearchResult;
import nl.knaw.huygens.timbuctoo.rest.model.TestRelation;
import nl.knaw.huygens.timbuctoo.rest.util.search.RegularClientSearchResultCreator;
import nl.knaw.huygens.timbuctoo.rest.util.search.RelationClientSearchResultCreator;
import nl.knaw.huygens.timbuctoo.rest.util.search.SearchRequestValidator;
import nl.knaw.huygens.timbuctoo.vre.VRE;
import nl.knaw.huygens.timbuctoo.vre.VREManager;

import org.junit.Before;
import org.mockito.Matchers;
import org.mockito.Mockito;

import com.google.common.collect.Sets;
import com.sun.jersey.api.client.WebResource;

public abstract class SearchResourceTestBase extends WebServiceTestSetup {

  private static final String SCOPE_ID = "base";
  protected static final Set<String> SORTABLE_FIELDS = Sets.newHashSet("test1", "test");
  protected static final String TERM = "dynamic_t_name:Huygens";
  protected static final String LOCATION_HEADER = "Location";
  protected static final String ID = "QURY0000000001";
  protected VREManager vreManager;
  protected SearchRequestValidator searchRequestValidator;
  protected RegularClientSearchResultCreator regularClientSearchResultCreatorMock;
  protected RelationClientSearchResultCreator relationClientSearchResultCreatorMock;
  protected static final String RELATION_SEARCH_RESULT_TYPE = "testrelation";
  protected static final String SEARCH_RESULT_TYPE_STRING = "person";
  protected static final Class<? extends DomainEntity> SEARCH_RESULT_TYPE = Person.class;
  protected static final Class<TestRelation> TEST_RELATION_TYPE = TestRelation.class;

  public SearchResourceTestBase() {
    super();
  }

  @Before
  public void setUpSearchRequestValidator() {
    searchRequestValidator = injector.getInstance(SearchRequestValidator.class);
  }

  @Before
  public void instantiateVREManager() {
    vreManager = injector.getInstance(VREManager.class);
  }

  protected void setSearchResult(VRE vreMock, SearchResult searchResult) throws Exception {
    when(vreMock.search(Matchers.<Class<? extends DomainEntity>> any(), any(SearchParametersV1.class))).thenReturn(searchResult);
  }

  protected VRE setUpVREManager(boolean isTypeInScope, boolean isVREKnown) {
    VRE vre = null;

    if (isVREKnown) {
      vre = mock(VRE.class);
      when(vre.getName()).thenReturn(VRE_ID);
      when(vre.getScopeId()).thenReturn(SCOPE_ID);
      when(vre.inScope(Mockito.<Class<? extends DomainEntity>> any())).thenReturn(isTypeInScope);

    }

    when(vreManager.getVREById(anyString())).thenReturn(vre);

    return vre;
  }

  protected abstract WebResource searchResource(String... pathElements);

  protected WebResource.Builder searchResourceBuilder(String... pathElements) {
    return searchResource(pathElements).type(MediaType.APPLICATION_JSON);
  }

  @Before
  public void setUpClientSearchResultCreators() {
    regularClientSearchResultCreatorMock = injector.getInstance(RegularClientSearchResultCreator.class);
    relationClientSearchResultCreatorMock = injector.getInstance(RelationClientSearchResultCreator.class);
  }

}