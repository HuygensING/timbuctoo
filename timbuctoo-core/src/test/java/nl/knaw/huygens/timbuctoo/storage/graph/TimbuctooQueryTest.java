package nl.knaw.huygens.timbuctoo.storage.graph;

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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.GraphQuery;

public class TimbuctooQueryTest {

  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final Object VALUE = "value";
  private static final String NAME = SubADomainEntity.VALUEA3_NAME;
  private Map<String, Object> hasProperties;
  private TimbuctooQuery instance;
  private AbstractGraphQueryBuilder<Object> queryBuilderMock;
  private Set<String> disitinctValues;
  private HashMap<String, List<?>> inCollectionProperties;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    queryBuilderMock = mock(AbstractGraphQueryBuilder.class);
    when(queryBuilderMock.build()).thenReturn(mock(GraphQuery.class));
    hasProperties = Maps.newHashMap();
    disitinctValues = Sets.newHashSet();
    inCollectionProperties = Maps.<String, List<?>> newHashMap();
    instance = new TimbuctooQuery(TYPE, hasProperties, disitinctValues, inCollectionProperties);
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

    String administrativeProperty = Entity.DB_ID_PROP_NAME;
    Object value2 = "value2";
    instance.hasNotNullProperty(administrativeProperty, value2);

    Object query = instance.createGraphQuery(queryBuilderMock);

    // verify
    assertThat(query, is(not(nullValue())));
    verify(queryBuilderMock).setHasProperties(hasProperties);
    verify(queryBuilderMock).setInCollectionProperties(inCollectionProperties);
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

  @Test
  public void addFilterOptionsToResultFilterAddsTheDistinctValuesToTheResultFilter() {
    // setup
    ResultFilter resultFilter = mock(ResultFilter.class);

    // action
    instance.addFilterOptionsToResultFilter(resultFilter);

    // verify
    verify(resultFilter).setDistinctFields(disitinctValues);
  }

  @Test
  public void addFilterOptionsToResultFilterAddsTheTypeToTheResultFilter() {
    // setup
    ResultFilter resultFilter = mock(ResultFilter.class);

    // action
    instance.addFilterOptionsToResultFilter(resultFilter);

    // verify
    verify(resultFilter).setType(TYPE);
  }

}
