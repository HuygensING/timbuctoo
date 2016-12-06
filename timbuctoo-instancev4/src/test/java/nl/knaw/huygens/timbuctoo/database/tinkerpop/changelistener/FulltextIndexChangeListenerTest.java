package nl.knaw.huygens.timbuctoo.database.tinkerpop.changelistener;

import nl.knaw.huygens.timbuctoo.core.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.database.tinkerpop.IndexHandler;
import nl.knaw.huygens.timbuctoo.model.vre.vres.VresBuilder;
import nl.knaw.huygens.timbuctoo.server.GraphWrapper;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;
import org.mockito.InOrder;

import java.util.Optional;

import static nl.knaw.huygens.timbuctoo.model.properties.PropertyTypes.localProperty;
import static nl.knaw.huygens.timbuctoo.util.TestGraphBuilder.newGraph;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class FulltextIndexChangeListenerTest {

  @Test
  public void onCreateAddsItemsToTheIndexOfTheProvidedCollection() {
    IndexHandler indexHandler = mock(IndexHandler.class);
    GraphWrapper graphWrapper = newGraph()
      .withVertex(v -> v
        .withProperty("vrecoll_name", "foo")
      )
      .wrap();
    FulltextIndexChangeListener instance = new FulltextIndexChangeListener(indexHandler, graphWrapper);
    Collection collection = new VresBuilder()
      .withVre("thevre", "vre", vre -> vre
        .withCollection("vrecolls", coll -> coll
          .withDisplayName(localProperty("vrecoll_name"))
        )
      )
      .build().getCollection("vrecolls").get();
    Vertex vertex = graphWrapper.getGraph().traversal().V().next();

    instance.onCreate(collection, vertex);

    verify(indexHandler).insertIntoQuickSearchIndex(collection, "foo", vertex);
  }

  @Test
  public void onCreateHandlesWwDocumentsUsingCustomLogic() {
    IndexHandler indexHandler = mock(IndexHandler.class);
    GraphWrapper graphWrapper = newGraph()
      .withVertex("doc", v -> v
        .withProperty("wwdocument_name", "foo")
        .withOutgoingRelation("isCreatedBy", "authorA")
        .withOutgoingRelation("isCreatedBy", "authorB")
      )
      .withVertex("authorA", v -> v
        .withProperty("wwperson_name", "authorA")
      )
      .withVertex("authorB", v -> v
        .withProperty("wwperson_name", "authorB")
      )
      .wrap();
    FulltextIndexChangeListener instance = new FulltextIndexChangeListener(indexHandler, graphWrapper);

    Collection collection = new VresBuilder()
      .withVre("womenwriters", "ww", vre -> vre
        .withCollection("wwdocuments", coll -> coll
          .withDisplayName(localProperty("wwdocument_name"))
        )
        .withCollection("wwpersons", coll -> coll
          .withDisplayName(localProperty("wwperson_name"))
        )
      )
      .build().getCollection("wwdocuments").get();
    Vertex vertex = graphWrapper.getGraph().traversal().V().next();
    instance.onCreate(collection, vertex);

    verify(indexHandler).insertIntoQuickSearchIndex(collection, "authorA; authorB foo", vertex);
  }

  @Test
  public void onPropertyUpdateRemovesTheOldVertexFromTheIndexBeforeAddingTheNewOne() {
    IndexHandler indexHandler = mock(IndexHandler.class);
    GraphWrapper graphWrapper = newGraph()
      .withVertex("newVertex", v -> v
        .withProperty("vrecoll_name", "new")
      )
      .withVertex(v -> v
        .withProperty("vrecoll_name", "old")
        .withOutgoingRelation("VERSION_OF", "newVertex")
      )
      .wrap();
    FulltextIndexChangeListener instance = new FulltextIndexChangeListener(indexHandler, graphWrapper);
    Collection collection = new VresBuilder()
      .withVre("thevre", "vre", vre -> vre
        .withCollection("vrecolls", coll -> coll
          .withDisplayName(localProperty("vrecoll_name"))
        )
      )
      .build().getCollection("vrecolls").get();
    Vertex oldVertex = graphWrapper.getGraph().traversal().V().has("vrecoll_name", "old").next();
    Vertex newVertex = graphWrapper.getGraph().traversal().V().has("vrecoll_name", "new").next();

    instance.onPropertyUpdate(collection, Optional.of(oldVertex), newVertex);

    InOrder inOrder = inOrder(indexHandler);
    inOrder.verify(indexHandler).removeFromQuickSearchIndex(collection, oldVertex);
    inOrder.verify(indexHandler).insertIntoQuickSearchIndex(collection, "new", newVertex);
  }

}
