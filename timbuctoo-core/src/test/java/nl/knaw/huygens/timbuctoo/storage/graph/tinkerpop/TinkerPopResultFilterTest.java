package nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop;

import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMatcher.likeVertex;
import static nl.knaw.huygens.timbuctoo.storage.graph.tinkerpop.VertexMockBuilder.aVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

import java.util.List;

import nl.knaw.huygens.timbuctoo.model.Entity;

import org.junit.Ignore;
import org.junit.Test;

import com.google.common.collect.Lists;
import com.tinkerpop.blueprints.Vertex;
import com.tinkerpop.pipes.PipeFunction;

public class TinkerPopResultFilterTest {
  private static final String ID2 = "id2";
  private static final String ID1 = "id1";

  @Ignore
  @SuppressWarnings("unchecked")
  @Test
  public void filterCreatesAPipeLineAndFiltersTheIncommingResults() {
    // setup
    Vertex vertex1WithId1 = aVertex().withId(ID1).build();
    Vertex vertex2WithId1 = aVertex().withId(ID1).build();

    Vertex vertex1WithId2 = aVertex().withId(ID2).build();
    Vertex vertex2WithId2 = aVertex().withId(ID2).build();

    List<Vertex> result = Lists.newArrayList(vertex1WithId1, vertex1WithId2, vertex2WithId1, vertex2WithId2);

    List<PipeFunction<Vertex, Object>> distinctPropertyFilters = Lists.newArrayList(filterByIdFunction());

    TinkerPopResultFilter<Vertex> instance = new TinkerPopResultFilter<Vertex>();

    // action
    Iterable<Vertex> filteredResult = instance.filter(result);

    // verify
    List<Vertex> filteredResultList = Lists.newArrayList(filteredResult);

    assertThat(filteredResultList, hasSize(2));
    assertThat(filteredResultList, containsInAnyOrder(//
        likeVertex().withId(ID1), //
        likeVertex().withId(ID2)));
  }

  private PipeFunction<Vertex, Object> filterByIdFunction() {
    return new PipeFunction<Vertex, Object>() {

      @Override
      public Object compute(Vertex argument) {
        return argument.getProperty(Entity.ID_DB_PROPERTY_NAME);
      }
    };
  }

}
