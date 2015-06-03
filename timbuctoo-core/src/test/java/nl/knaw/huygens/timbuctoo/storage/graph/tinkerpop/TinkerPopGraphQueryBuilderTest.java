package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopGraphQueryBuilderTest {
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String INTERNAL_NAME = TypeNames.getInternalName(TYPE);
  private static final Object VALUE = "value";
  private static final String NAME = SubADomainEntity.VALUEA3_NAME;

  private TinkerPopGraphQueryBuilder instance;
  private PropertyBusinessRules businessRules;
  private Graph db;
  private Map<String, Object> properties;

  @Before
  public void setup() {
    this.setupGraphDB();

    properties = Maps.newHashMap();
    businessRules = new PropertyBusinessRules();
    instance = new TinkerPopGraphQueryBuilder(TYPE, businessRules, db);
  }

  private void setupGraphDB() {
    db = mock(Graph.class);
    when(db.query()).thenReturn(mock(GraphQuery.class));
  }

  @Test
  public void buildLetsDBCreateAGraphQueryAndAddsTheAddedProperties() throws Exception {
    // setup
    properties.put(NAME, VALUE);
    String administrativeProperty = Entity.ID_DB_PROPERTY_NAME;
    Object value2 = "value2";
    properties.put(administrativeProperty, value2);
    instance.setHasProperties(properties);

    // action
    GraphQuery query = instance.build();

    // verify
    verify(query).has(getExpectedPropertyName(TYPE, NAME), VALUE);
    verify(query).has(administrativeProperty, value2);
  }

  @Test(expected = NoSuchFieldException.class)
  public void buildThrowsANoSuchFieldExceptionIfHasPropertiesContainsANonExistingField() {
    // setup
    properties.put("nonExistingField", VALUE);
    instance.setHasProperties(properties);

    // action
    instance.build();
  }

  private String getExpectedPropertyName(Class<SubADomainEntity> type, String name) throws Exception {
    Field field = type.getDeclaredField(name);
    String fieldName = businessRules.getFieldName(type, field);
    return businessRules.getFieldType(type, field).propertyName(type, fieldName);
  }

  @Test
  public void buildAddsTheTypeIfSearchByTypeIsTrue() {
    instance.setSearchByType(true);

    GraphQuery query = instance.build();

    // verify
    verify(query).has( //
        argThat(is(ELEMENT_TYPES)), //
        any(IsOfTypePredicate.class), //
        argThat(is(INTERNAL_NAME)));
  }

}
