package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;
import nl.knaw.huygens.timbuctoo.storage.graph.NoSuchFieldException;
import nl.knaw.huygens.timbuctoo.storage.graph.PropertyBusinessRules;

import org.junit.Before;
import org.junit.Test;

import test.model.projecta.SubADomainEntity;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.gremlin.java.GremlinPipeline;
import com.tinkerpop.pipes.PipeFunction;

public class TinkerPopResultFilterTest {
  private static final Class<SubADomainEntity> TYPE = SubADomainEntity.class;
  private static final String REGULAR_FIELD = SubADomainEntity.VALUEA3_NAME;
  private static final String ADMIN_FIELD = Entity.ID_DB_PROPERTY_NAME;
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

    PipeFunction<Vertex, Object> adminPipeFunction = pipeFunctionfor(ADMIN_FIELD);
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
    return businessRules.getFieldType(TYPE, field).propertyName(TYPE, fieldName);
  }

  private PipeFunction<Vertex, Object> pipeFunctionfor(String property) {
    @SuppressWarnings("unchecked")
    PipeFunction<Vertex, Object> pipefunction = mock(PipeFunction.class);
    when(pipeFunctionFactory.<Vertex, Object> forDistinctProperty(property)).thenReturn(pipefunction);

    return pipefunction;
  }
}
