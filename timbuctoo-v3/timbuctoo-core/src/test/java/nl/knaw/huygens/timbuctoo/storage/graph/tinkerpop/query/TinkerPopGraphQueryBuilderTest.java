package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query;

/*
 * #%L
 * Timbuctoo core
 * =======
 * Copyright (C) 2012 - 2015 Huygens ING
 * =======
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.tinkerpop.blueprints.Graph;
import com.tinkerpop.blueprints.GraphQuery;
import nl.knaw.huygens.timbuctoo.config.TypeNames;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.query.TinkerPopGraphQueryBuilder.InCollectionPredicate;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.SubADomainEntity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.ElementFields.ELEMENT_TYPES;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TinkerPopGraphQueryBuilderTest {
  private static final String VALUE2 = "value2";
  private static final String ADMINISTRATIVE_PROPERTY = Entity.ID_PROPERTY_NAME;
  private static final String DB_ADMIN_PROP = Entity.DB_ID_PROP_NAME;
  private static final Object VALUE = "value";
  private static final String REGULAR_PROPERTY = SubADomainEntity.VALUEA3_NAME;
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String INTERNAL_NAME = TypeNames.getInternalName(TYPE);

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
    verify(query).has(DB_ADMIN_PROP, VALUE2);
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
    return businessRules.getFieldType(type, field).completePropertyName(type, fieldName);
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
    verify(query).has(argThat(is(DB_ADMIN_PROP)), any(InCollectionPredicate.class), argThat(is(collection2)));

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
