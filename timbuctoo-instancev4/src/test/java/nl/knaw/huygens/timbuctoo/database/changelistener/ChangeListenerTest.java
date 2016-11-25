package nl.knaw.huygens.timbuctoo.database.changelistener;

import com.google.common.collect.Sets;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperations;
import nl.knaw.huygens.timbuctoo.database.DataStoreOperationsStubs;
import nl.knaw.huygens.timbuctoo.database.dto.CreateEntityStubs;
import nl.knaw.huygens.timbuctoo.database.dto.UpdateEntity;
import nl.knaw.huygens.timbuctoo.database.dto.dataset.Collection;
import nl.knaw.huygens.timbuctoo.model.Change;
import nl.knaw.huygens.timbuctoo.model.vre.Vre;
import org.apache.tinkerpop.gremlin.structure.Direction;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.junit.Test;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.google.common.collect.Lists.newArrayList;
import static nl.knaw.huygens.timbuctoo.database.DataStoreOperationsStubs.forDeleteCall;
import static nl.knaw.huygens.timbuctoo.database.DataStoreOperationsStubs.forReplaceCall;
import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.IS_LATEST;
import static nl.knaw.huygens.timbuctoo.database.VertexDuplicator.VERSION_OF;
import static nl.knaw.huygens.timbuctoo.util.StreamIterator.stream;
import static nl.knaw.huygens.timbuctoo.util.VertexMatcher.likeVertex;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ChangeListenerTest {
  @Test
  public void callsOnCreateOnCreateEntity() throws Exception {
    ChangeListener changeListener = new ChangeListenerImpl(vertex -> {
      assertThat(vertex, likeVertex()
        .withProperty(IS_LATEST, true)
        .withProperty("rev", 1)
      );

      Long prevVersions = stream(vertex.vertices(Direction.BOTH, VERSION_OF)).collect(Collectors.counting());
      assertThat(prevVersions, is(0L));
    });
    ChangeListener spy = spy(changeListener);
    DataStoreOperations instance = DataStoreOperationsStubs.forChangeListenerMock(spy);

    instance.createEntity(mock(Collection.class), Optional.empty(), CreateEntityStubs.dummy());

    verify(spy).onCreate(any());
  }

  @Test
  public void callsOnUpdateOnReplaceEntity() throws Exception {
    ChangeListener changeListener = new ChangeListenerImpl(vertex -> {
      assertThat(vertex, likeVertex()
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
      );

      Long prevVersions = stream(vertex.vertices(Direction.BOTH, VERSION_OF)).collect(Collectors.counting());
      assertThat(prevVersions, is(1L));
    });
    UUID id = UUID.randomUUID();
    ChangeListener spy = spy(changeListener);
    DataStoreOperations instance = forReplaceCall(spy, id, 1);

    UpdateEntity updateEntity = new UpdateEntity(id, newArrayList(), 1);
    updateEntity.setModified(new Change());

    instance.replaceEntity(mock(Collection.class), updateEntity);

    verify(spy).onUpdate(any(), any());
  }

  @Test
  public void callsOnUpdateOnDeleteEntity() throws Exception {
    ChangeListener changeListener = new ChangeListenerImpl(vertex -> {
      assertThat(vertex, likeVertex()
        .withProperty("isLatest", true)
        .withProperty("rev", 2)
      );

      Long prevVersions = stream(vertex.vertices(Direction.BOTH, VERSION_OF)).collect(Collectors.counting());
      assertThat(prevVersions, is(1L));
    });

    UUID id = UUID.randomUUID();

    ChangeListener spy = spy(changeListener);
    DataStoreOperations instance = forDeleteCall(spy, id, 1, "someName");

    Collection collection = mock(Collection.class);
    when(collection.getEntityTypeName()).thenReturn("someName");
    when(collection.getVre()).thenReturn(mock(Vre.class));
    instance.deleteEntity(collection, id, new Change());

    verify(spy).onUpdate(any(), any());
  }

  @Test
  public void isCalledForAllMethods() {
    Set<String> expectations = Sets.newHashSet(
      "createEntity",
      "replaceEntity",
      "deleteEntity",

      //FIXME should these also be implemented?
      "removeCollectionsAndEntities",
      "replaceRelation",
      "acceptRelation",
      "doQuickSearch",
      "doKeywordQuickSearch",

      "close",
      "success",
      "rollback",
      "getEntity",
      "getCollection",
      "loadVres",
      "databaseIsEmptyExceptForMigrations",
      "initDb",
      "saveRelationTypes",
      "saveVre",
      "ensureVreExists",
      "addPid"
    );
    Method[] allMethods = DataStoreOperations.class.getDeclaredMethods();
    for (Method method : allMethods) {
      if (Modifier.isPublic(method.getModifiers())) {
        if (!expectations.contains(method.getName())) {
          throw new IllegalStateException("This test is not implemented for #" + method.getName());
        }
      }
    }
  }

  private class ChangeListenerImpl implements ChangeListener {

    private final Consumer<Vertex> validateVertex;

    private ChangeListenerImpl(Consumer<Vertex> validateVertex) {
      this.validateVertex = validateVertex;
    }


    @Override
    public void onCreate(Vertex vertex) {
      validateVertex.accept(vertex);
    }

    @Override
    public void onUpdate(Optional<Vertex> oldVertex, Vertex newVertex) {
      validateVertex.accept(newVertex);
    }
  }
}
