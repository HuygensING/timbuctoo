package nl.knaw.huygens.timbuctoo.storage.graph;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Map;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQueryTest {

  private static final Object VALUE = "value";
  private static final String NAME = SubADomainEntity.VALUEA3_NAME;
  private Map<String, Object> hasProperties;
  private TimbuctooQuery instance;
  private AbstractGraphQueryBuilder<Object> queryBuilderMock;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    queryBuilderMock = mock(AbstractGraphQueryBuilder.class);
    when(queryBuilderMock.build()).thenReturn(mock(GraphQuery.class));
    hasProperties = Maps.newHashMap();
    instance = new TimbuctooQuery(hasProperties);
  }

  @Test
  public void hasNotNullPropertyReturnsItsInstance() {
    // action
    TimbuctooQuery returnValue = instance.hasNotNullProperty(NAME, VALUE);

    // verify
    assertThat(returnValue, is(sameInstance(instance)));
  }

  @Test
  public void hasNotNullPropertyAddsTheValueToHasProperties() throws Exception {
    // action
    instance.hasNotNullProperty(NAME, VALUE);

    // verify
    assertThat(hasProperties.keySet(), contains(NAME));
    assertThat(hasProperties.get(NAME), is(VALUE));
  }

  @Test
  public void hasNotNullPropertyDoesNotAddTheValueToHasPropertiesWhenTheValueIsNull() {
    // action
    instance.hasNotNullProperty(NAME, null);

    // verify
    assertThat(hasProperties.keySet(), not(contains(NAME)));
  }

  @Test
  public void createGraphQuerySetsTheTypeAndHasPropertiesToTheQueryBuilder() throws Exception {
    // setup
    instance.searchByType(true);

    String administrativeProperty = Entity.ID_DB_PROPERTY_NAME;
    Object value2 = "value2";
    instance.hasNotNullProperty(administrativeProperty, value2);

    Object query = instance.createGraphQuery(queryBuilderMock);

    // verify
    assertThat(query, is(not(nullValue())));
    verify(queryBuilderMock).setHasProperties(hasProperties);
    verify(queryBuilderMock).setSearchByType(true);
    verify(queryBuilderMock).build();
  }

  @Test(expected = NoSuchFieldException.class)
  public void createGraphQueryThrowsANoSuchFieldExceptionWhenTheGraphQueryBuilderThrowsOne() {
    // setup
    doThrow(NoSuchFieldException.class).when(queryBuilderMock).build();

    // action
    instance.createGraphQuery(queryBuilderMock);
  }
}
