package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopGraphQueryBuilder.InCollectionPredicate;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;

public class TinkerPopGraphQueryBuilderTest {
  private static final String VALUE2 = "value2";
  private static final String ADMINISTRATIVE_PROPERTY = Entity.DB_ID_PROP_NAME;
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String INTERNAL_NAME = TypeNames.getInternalName(TYPE);
  private static final Object VALUE = "value";
  private static final String REGULAR_PROPERTY = SubADomainEntity.VALUEA3_NAME;

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
    properties.put(REGULAR_PROPERTY, VALUE);
    properties.put(ADMINISTRATIVE_PROPERTY, VALUE2);
    instance.setHasProperties(properties);

    // action
    GraphQuery query = instance.build();

    // verify
    verify(query).has(getExpectedPropertyName(TYPE, REGULAR_PROPERTY), VALUE);
    verify(query).has(ADMINISTRATIVE_PROPERTY, VALUE2);
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

  @Test
  public void buildAddsAHasPropertyWithAInCollectionPredicateForEachEntryInTheInCollectionProperties() throws Exception {
    // setup
    Map<String, List<?>> inCollectionProperties = Maps.newHashMap();
    ArrayList<Object> collection1 = Lists.newArrayList(VALUE);
    inCollectionProperties.put(REGULAR_PROPERTY, collection1);
    ArrayList<String> collection2 = Lists.newArrayList(VALUE2);
    inCollectionProperties.put(ADMINISTRATIVE_PROPERTY, collection2);
    instance.setInCollectionProperties(inCollectionProperties);

    // action
    GraphQuery query = instance.build();

    // verify
    verify(query).has(argThat(is(getExpectedPropertyName(TYPE, REGULAR_PROPERTY))), any(InCollectionPredicate.class), argThat(is(collection1)));
    verify(query).has(argThat(is(ADMINISTRATIVE_PROPERTY)), any(InCollectionPredicate.class), argThat(is(collection2)));

  }

  @Test
  public void inCollectionPredicatesEvaluateReturnsTrueIfTheSecondValueContainsTheFirst() {
    // setup
    String first = "firstValue";
    List<String> second = Lists.newArrayList(first);

    InCollectionPredicate inCollectionPredicate = new InCollectionPredicate();

    // action
    boolean evaluate = inCollectionPredicate.evaluate(first, second);

    // verify
    assertThat(evaluate, is(true));

  }

  @Test
  public void inCollectionPredicatesEvaluateReturnsFalseIfTheSecondValueDoesNotContainTheFirst() {
    // setup
    String first = "firstValue";
    List<String> second = Lists.newArrayList("other");

    InCollectionPredicate inCollectionPredicate = new InCollectionPredicate();

    // action
    boolean evaluate = inCollectionPredicate.evaluate(first, second);

    // verify
    assertThat(evaluate, is(false));
  }
}
