package nl.knaw.huygens.timbuctoo.experimental.bulkupload.savers;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import nl.knaw.huygens.timbuctoo.model.vre.Vres;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

public class TinkerpopSaverTest {

  private static final String VRE_NAME = "vre";
  private static final int MAX_VERTICES_PER_TRANSACTION = 100;
  private Vres vres;

  @Before
  public void setUp() throws Exception {
    vres = mock(Vres.class);
  }

  @Test
  public void theCreationAddsAVreToTheDatabase() {
    GraphWrapper graphWrapper = newGraph().wrap();
    new TinkerpopSaver(vres, graphWrapper, VRE_NAME, MAX_VERTICES_PER_TRANSACTION);

    assertThat(graphWrapper.getGraph().traversal().V().hasLabel(Vre.DATABASE_LABEL).next(), is(
      likeVertex().withProperty(Vre.VRE_NAME_PROPERTY_NAME, VRE_NAME)
    ));
  }

  @Test
  public void addEntityAddsEachEntityToTheCollection() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withProperty("name", "rawCollection")).wrap();
    Vertex rawCollection = graphWrapper.getGraph().traversal().V().has("name", "rawCollection").next();
    TinkerpopSaver instance = new TinkerpopSaver(vres, graphWrapper, VRE_NAME, MAX_VERTICES_PER_TRANSACTION);

    Vertex first = instance.addEntity(rawCollection, Maps.newHashMap());
    Vertex second = instance.addEntity(rawCollection, Maps.newHashMap());

    List<Vertex> items = Lists.newArrayList(rawCollection.vertices(Direction.OUT, "hasItem"));
    assertThat(items, containsInAnyOrder(first, second));
  }

  @Test
  public void addEntityAddsARelationThatIndicatesTheFirstRelation() {
    GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withProperty("name", "rawCollection")).wrap();
    Vertex rawCollection = graphWrapper.getGraph().traversal().V().has("name", "rawCollection").next();
    TinkerpopSaver instance = new TinkerpopSaver(vres, graphWrapper, VRE_NAME, MAX_VERTICES_PER_TRANSACTION);

    Vertex first = instance.addEntity(rawCollection, Maps.newHashMap());
    instance.addEntity(rawCollection, Maps.newHashMap());

    List<Vertex> hasFirstItems = Lists.newArrayList(rawCollection.vertices(Direction.OUT, "hasFirstItem"));
    assertThat(hasFirstItems, hasSize(1));
    assertThat(hasFirstItems, contains(first));
  }

  @Test
  public void addEntityAppendsTheNewVertexToThePrevious() {
    final GraphWrapper graphWrapper = newGraph().withVertex(v -> v.withProperty("name", "rawCollection")).wrap();
    final Vertex rawCollection = graphWrapper.getGraph().traversal().V().has("name", "rawCollection").next();
    final TinkerpopSaver instance = new TinkerpopSaver(vres, graphWrapper, VRE_NAME, MAX_VERTICES_PER_TRANSACTION);

    final Vertex first = instance.addEntity(rawCollection, Maps.newHashMap());
    final Vertex second = instance.addEntity(rawCollection, Maps.newHashMap());
    final Vertex third = instance.addEntity(rawCollection, Maps.newHashMap());

    assertThat(first.vertices(Direction.OUT, "hasNextItem").hasNext(), is(true));
    assertThat(first.vertices(Direction.OUT, "hasNextItem").next(), is(second));
    assertThat(second.vertices(Direction.OUT, "hasNextItem").hasNext(), is(true));
    assertThat(second.vertices(Direction.OUT, "hasNextItem").next(), is(third));
  }

}
