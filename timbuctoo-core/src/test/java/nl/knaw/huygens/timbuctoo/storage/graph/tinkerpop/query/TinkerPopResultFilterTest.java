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
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;
import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;
import org.junit.Before;
import org.junit.Test;
import test.model.projecta.SubADomainEntity;

import java.lang.reflect.Field;
import java.util.List;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TinkerPopResultFilterTest {
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String REGULAR_FIELD = SubADomainEntity.VALUEA3_NAME;
  private static final String ADMIN_FIELD = Entity.ID_PROPERTY_NAME;
  private static final String DB_ADMIN_FIELD = Entity.DB_ID_PROP_NAME;
  private PipeFunctionFactory pipeFunctionFactory;
  private PropertyBusinessRules businessRules;
  private TinkerPopResultFilter<Vertex> instance;
  private GremlinPipeline<Iterable<Vertex>, Vertex> pipeline;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() {
    pipeFunctionFactory = mock(PipeFunctionFactory.class);
    businessRules = new PropertyBusinessRules();
    pipeline = mock(GremlinPipeline.class);

    instance = new TinkerPopResultFilter<Vertex>(pipeFunctionFactory, businessRules) {
      @Override
      GremlinPipeline<Iterable<Vertex>, Vertex> createPipeline(Iterable<Vertex> iterableToFilter) {
        return pipeline;
      }
    };
  }

  @Test
  public void filterCreatesAPipeFunctionForEachDistinctFieldAndExecutesItThrowAPipeLine() throws Exception {
    // setup
    Vertex vertex1 = aVertex().withId("id1").build();
    Vertex vertex2 = aVertex().withId("id2").build();
    List<Vertex> result = Lists.newArrayList(vertex1, vertex2);

    instance.setDistinctFields(Sets.newHashSet(ADMIN_FIELD, REGULAR_FIELD));
    instance.setType(TYPE);

    PipeFunction<Vertex, Object> adminPipeFunction = pipeFunctionfor(DB_ADMIN_FIELD);
    PipeFunction<Vertex, Object> regularPipeFunction = pipeFunctionfor(getPropertyNameFor(REGULAR_FIELD));

    // action
    Iterable<Vertex> filteredResult = instance.filter(result);

    // verify
    assertThat(filteredResult, is(notNullValue()));

    verify(pipeline).dedup(adminPipeFunction);
    verify(pipeline).dedup(regularPipeFunction);

  }

  @Test(expected = NoSuchFieldException.class)
  public void filterThrowsANoSuchFieldExceptionWhenAnUnknownFieldIsAddedToTheDistinctProperties() throws Exception {
    // setup
    Vertex vertex1 = aVertex().build();
    Vertex vertex2 = aVertex().build();
    List<Vertex> result = Lists.newArrayList(vertex1, vertex2);

    instance.setDistinctFields(Sets.newHashSet("unknownField"));
    instance.setType(TYPE);

    // action
    instance.filter(result);

  }

  private String getPropertyNameFor(String regularField) throws Exception {
    Field field = TYPE.getDeclaredField(regularField);
    String fieldName = businessRules.getFieldName(TYPE, field);
    return businessRules.getFieldType(TYPE, field).completePropertyName(TYPE, fieldName);
  }

  private PipeFunction<Vertex, Object> pipeFunctionfor(String property) {
    @SuppressWarnings("unchecked")
    PipeFunction<Vertex, Object> pipefunction = mock(PipeFunction.class);
    when(pipeFunctionFactory.<Vertex, Object> forDistinctProperty(property)).thenReturn(pipefunction);

    return pipefunction;
  }
}
