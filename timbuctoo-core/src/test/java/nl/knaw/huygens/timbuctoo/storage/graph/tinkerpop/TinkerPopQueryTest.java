package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopQueryTest {

  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String INTERNAL_NAME = TypeNames.getInternalName(TYPE);
  private static final Object VALUE = "value";
  private static final String NAME = SubADomainEntity.VALUEA3_NAME;
  private Map<String, Object> hasProperties;
  private TinkerPopQuery instance;
  private Graph db;
  private PropertyBusinessRules businessRules;

  @Before
  public void setup() {
    businessRules = new PropertyBusinessRules();
    hasProperties = Maps.newHashMap();
    instance = new TinkerPopQuery(TYPE, businessRules, hasProperties);
    setupGraphDB();
  }

  private void setupGraphDB() {
    db = mock(Graph.class);
    when(db.query()).thenReturn(mock(GraphQuery.class));
  }

  @Test
  public void hasNotNullPropertyReturnsItsInstance() {
    // action
    TinkerPopQuery returnValue = instance.hasNotNullProperty(NAME, VALUE);

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

  private String getExpectedPropertyName(Class<SubADomainEntity> type, String name) throws Exception {
    Field field = type.getDeclaredField(name);
    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).propertyName(type, fieldName);
  }

  @Test
  public void hasNotNullPropertyDoesNotAddTheValueToHasPropertiesWhenTheValueIsNull() {
    // action
    instance.hasNotNullProperty(NAME, null);

    // verify
    assertThat(hasProperties.keySet(), not(contains(NAME)));
  }

  @Test
  public void createGraphQueryLetsDBCreateAGraphQueryAndAddsTheAddedProperties() throws Exception {
    // setup
    instance.hasNotNullProperty(NAME, VALUE);
    String administrativeProperty = Entity.ID_DB_PROPERTY_NAME;
    Object value2 = "value2";
    instance.hasNotNullProperty(administrativeProperty, value2);

    // action
    GraphQuery query = instance.createGraphQuery(db);

    // verify
    verify(query).has(getExpectedPropertyName(TYPE, NAME), VALUE);
    verify(query).has(administrativeProperty, value2);
  }

  @Test
  public void createGraphQueryAddsTheTypeIfTheValueIsNotNull() {
    instance.hasType(TYPE);

    GraphQuery query = instance.createGraphQuery(db);

    // verify
    verify(query).has( //
        argThat(is(ELEMENT_TYPES)), //
        any(IsOfTypePredicate.class), //
        argThat(is(INTERNAL_NAME)));

  }
}
